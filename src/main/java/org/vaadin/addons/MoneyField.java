package org.vaadin.addons;

import com.ibm.icu.text.NumberFormat; //don't use java.text.NumberFormat, since it does not support variable-width groups (as e.g. for indian formats)
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

import java.text.ParseException;
import java.util.Currency;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.StringUtils;
import org.javamoney.moneta.Money;

/**
 * Composite component for a JSR-354 {@code MonetaryAmount} consisting of a {@code TextField} for the amount and a {@code ComboBox} for the
 * currency.
 * Note that money entries are automatically formatted and rounded according to the <code>java.util.Locale</code> of this component.
 * This means that for e.g. 1234.567 with Locale("en", "US") results in 1,234.57 but with Locale("de", "DE") it results in 1.234.567
 *
 * @author Sebastian Dietrich
 */
@Tag("money-field")
public class MoneyField extends AbstractCompositeField<Div, MoneyField, MonetaryAmount> implements HasLabel, HasSize, HasValidation {
    private static final long serialVersionUID = -6563463270512422984L;
    
    //depending on locale amounts can have different delimiters and group-length (e.g. 1,23,450 for India, 1 234 567 for Poland (\\h = whitespace))
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\s*([-+]?)(\\d{1,4}([.,\\h]?\\d{2,4})*([.,]\\d+)?)?$");
    private static final String NUMBER_CHARS = "0123456789., \u00a0"; //all allowed characters in a number (including space and &nbsp; for polish numbers)
    private static final Pattern CALCULABLE_AMOUNT_PATTERN = Pattern.compile("^\\s*\\(*([-+]?(\\d{1,4}([.,\\h]?\\d{2,4})*([.,]\\d+)?)?)(\\h*([-+*/]\\h*\\(*(\\h*[-+]?\\d{1,4}([.,\\h]?\\d{2,4})*([.,]\\d+)?)\\h*\\)*\\h*)*)$");
    
    
    /**
     * The amount part of this component.
     */
    private final TextField amount;
    
    /**
     * The currency part of this component.
     */
    private final ComboBox<String> currency;

    /**
     * Constructs an empty {@code MoneyField}.
     */
    public MoneyField() {
        this(false);
    }
    
    /**
     * Constructs an empty {@code MoneyField} that can possibly be calculated.
     * @param calculable if the field allows basic arithmetic expressions to be calculated
     */
    public MoneyField(boolean calculable) {
        this((MonetaryAmount) null, calculable);
    }

    /**
     * Constructs an empty {@code MoneyField} with the given initial value and currencyCodes. Amounts are formatted on-the-fly using the current locale.
     *
     * @param initialValue the initial {@code MonetaryAmount}
     * @param currencyCodes the currencyCodes to set in the currency selection
     * @param calculable if the field allows basic arithmetic expressions to be calculated
     */
    public MoneyField(MonetaryAmount initialValue, List<String> currencyCodes, boolean calculable) {
        super(initialValue);

        if (initialValue != null && !currencyCodes.contains(initialValue.getCurrency().getCurrencyCode())) {
            throw new IllegalArgumentException(
                "The initial values currency code '" +
                    initialValue.getCurrency().getCurrencyCode() +
                    "' is not in the list of currency codes.");
        }
        amount = new TextField();
        amount.setId("amount");
        amount.setWidth(3.60f, Unit.REM);  //smallest size so that small amounts like € 1,00 are just visible

        currency = new ComboBox<>();
        currency.setId("currency");
        currency.setItems(currencyCodes);
        currency.setWidth(5.45f, Unit.REM);  //smallest size so that currencies MWK, MMK, TMM just fit into the field

        setValue(initialValue);
        
        amount.addValueChangeListener(listener -> {
            if (!listener.isFromClient()) return;
            evaluateAndSetAmount(calculable);
        });
        
        currency.addValueChangeListener(listener -> {
            if (StringUtils.isEmpty(amount.getValue()) || StringUtils.isEmpty(currency.getValue())) return;
            evaluateAndSetAmount(calculable);
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setSizeUndefined();
        layout.add(amount, currency);
        layout.setFlexGrow(1, amount);
        layout.setFlexGrow(0, currency);
        layout.setAlignItems(Alignment.END); // so that amount with label is aligned to currency

        getContent().add(layout);
    }

    private void evaluateAndSetAmount(boolean calculable) {
        com.ibm.icu.text.NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(getLocale());
        com.ibm.icu.text.NumberFormat numberFormat = NumberFormat.getNumberInstance(getLocale());

        String textualAmount = amount.getValue();
        if (textualAmount.isBlank()) {
            setModelValue(null, true);
            return;
        }
        if ((calculable ? CALCULABLE_AMOUNT_PATTERN : AMOUNT_PATTERN).matcher(textualAmount).matches()) {
          try {
              String formattedAmount = currencyFormat.format(calculable ? eval(textualAmount, numberFormat) : numberFormat.parse(textualAmount));
              setAmount(formattedAmount);
              if (StringUtils.isEmpty(currency.getValue())) return;
              setModelValue(Money.of(currencyFormat.parse(formattedAmount), currency.getValue()), true);
              this.setInvalid(false);
              return;
          } catch (ParseException e) {
              //do nothing, just set the field invalid
          }
        }
        this.setInvalid(true);
    }
    
   
    /**
     * Evaluate arithmetic expression including +, -, *, /, ()
     * 
     * @see https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form (removed functions like sin, sqrt, ...)
     * @throws ParseException when the expression cannot be parsed
     */
    private double eval(final String str, final com.ibm.icu.text.NumberFormat numberFormat) throws ParseException {
        return new Object() {
            private int pos = -1;
            private int ch;
            
            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : (char)-1;
            }
            
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }
            
            double parse() throws ParseException {
                nextChar();
                double number = parseExpression();
                if (pos < str.length()) throw new ParseException("Unexpected: " + (char)ch, pos);
                return number;
            }
            
            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | factor `^` factor
            
            double parseExpression() throws ParseException {
                double term = parseTerm();
                for (;;) {
                    if      (eat('+')) term += parseTerm(); // addition
                    else if (eat('-')) term -= parseTerm(); // subtraction
                    else return term;
                }
            }
            
            double parseTerm() throws ParseException {
                double factor = parseFactor();
                for (;;) {
                    if      (eat('*')) factor *= parseFactor(); // multiplication
                    else if (eat('/')) factor /= parseFactor(); // division
                    else return factor;
                }
            }
            
            double parseFactor() throws ParseException {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus
                
                double number;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    number = parseExpression();
                    if (!eat(')')) throw new ParseException("Missing ')'", pos);
                } else if (NUMBER_CHARS.indexOf(ch) >= 0) {
                    while (NUMBER_CHARS.indexOf(ch) >= 0) nextChar();
                    number = numberFormat.parse(str.substring(startPos, pos)).doubleValue();
                } else {
                    throw new ParseException("Unexpected: " + (char)ch, pos);
                }
                
                if (eat('^')) number = Math.pow(number, parseFactor()); // exponentiation
                
                return number;
            }
        }.parse();
    }
    
    /**
     * Constructs an empty {@code MoneyField} with the given initial value and formatter.
     *
     * @param initialValue the initial value
     */
    public MoneyField(MonetaryAmount initialValue) {
        this(initialValue, getAvailableCurrencyCodes(), false);
    }
    
    /**
     * Constructs an empty {@code MoneyField} with the given initial value and formatter.
     *
     * @param initialValue the initial value
     * @param calculable if the field allows basic arithmetic expressions to be calculated
     */
    public MoneyField(MonetaryAmount initialValue, boolean calculable) {
        this(initialValue, getAvailableCurrencyCodes(), calculable);
    }

    /**
     * Constructs an empty {@code MoneyField} with the given initial value for the currency.
     *
     * @param currency the initial currency
     */
    public MoneyField(CurrencyUnit currency) {
        this();
        setCurrency(currency);
    }

    /**
     * Constructs an empty {@code MoneyField} with the given initial value for the currency.
     *
     * @param currency the initial currency
     */
    public MoneyField(Currency currency) {
        this();
        setCurrency(currency);
    }
    
    /**
     * Constructs an empty {@code MoneyField} with the given label and initial value for the currency.
     *
     * @param label the text to set as the label
     * @param currency the initial currency
     */
    public MoneyField(String label, Currency currency) {
        this();
        setLabel(label);
        setCurrency(currency);
    }
    
    /**
     * Constructs an empty {@code MoneyField} with the given label and initial value for the currency.
     *
     * @param label the text to set as the label
     * @param currency the initial currency
     */
    public MoneyField(String label, CurrencyUnit currency) {
        this();
        setLabel(label);
        setCurrency(currency);
    }
    
    /**
     * Constructs an empty {@code MoneyField} with the given label and initial value for the currency.
     *
     * @param label the text to set as the label
     * @param currencyCode the ISO-4217 three letter currency code.
     */
    public MoneyField(String label, String currencyCode) {
        this();
        setLabel(label);
        setCurrency(currencyCode);
    }
    
    /**
     * Constructs an empty, possibly calculable {@code MoneyField} with the given label and initial value for the currency.
     *
     * @param label the text to set as the label
     * @param currencyCode the ISO-4217 three letter currency code.
     * @param calculable if the field allows basic arithmetic expressions to be calculated
     */
    public MoneyField(String label, String currencyCode, boolean calculable) {
        this(calculable);
        setLabel(label);
        setCurrency(currencyCode);
    }

    private static List<String> getAvailableCurrencyCodes() {
        return Currency.getAvailableCurrencies().stream().map(Currency::getCurrencyCode).sorted().collect(Collectors.toList());
    }

    /**
     * Constructs an empty {@code MoneyField} with the given label.
     *
     * @param label the text to set as the label
     */
    public MoneyField(String label) {
        this(label, false);
    }
    
    /**
     * Constructs an empty {@code MoneyField} with the given label that can be possibly calculated.
     *
     * @param label the text to set as the label
     * @param calculable if the field allows basic arithmetic expressions to be calculated
     */
    public MoneyField(String label, boolean calculable) {
        this(calculable);
        setLabel(label);
    }

    /**
     * Constructs {@code MoneyField} with the given label and initial value.
     *
     * @param label the text to set as the label
     * @param initialValue the initial value
     */
    public MoneyField(String label, MonetaryAmount initialValue) {
        this(label, initialValue, false);
    }
    
    /**
     * Constructs {@code MoneyField} with the given label and initial value.
     *
     * @param label the text to set as the label
     * @param initialValue the initial value
     * @param calculable if the field allows basic arithmetic expressions to be calculated
     */
    public MoneyField(String label, MonetaryAmount initialValue, boolean calculable) {
        this(initialValue, calculable);
        setLabel(label);
    }

    /**
     * Constructs a {@code MoneyField} with the given label, an initial value and placeholder text.
     *
     * @param label the text to set as the label
     * @param initialValue the initial value
     * @param placeholder the placeholder text to set
     * @see #setValue(Object)
     * @see #setPlaceholder(String)
     */
    public MoneyField(String label, MonetaryAmount initialValue, String placeholder) {
        this(label, initialValue, placeholder, false);
    }
    
    /**
     * Constructs a {@code MoneyField} with the given label, an initial value and placeholder text.
     *
     * @param label the text to set as the label
     * @param initialValue the initial value
     * @param placeholder the placeholder text to set
     * @param calculable if the field allows basic arithmetic expressions to be calculated
     * 
     * @see #setValue(Object)
     * @see #setPlaceholder(String)
     */
    public MoneyField(String label, MonetaryAmount initialValue, String placeholder, boolean calculable) {
        this(label, initialValue, calculable);
        amount.setPlaceholder(placeholder);
    }

    @Override
    protected void setPresentationValue(MonetaryAmount monetaryAmount) {
        setAmount(monetaryAmount.getNumber());
        setCurrency(monetaryAmount.getCurrency().getCurrencyCode());
    }

    /**
     * Sets the currency.
     * 
     * @param currencyCode the ISO-4217 three letter currency code.
     */
    public void setCurrency(String currencyCode) {
        currency.setValue(currencyCode);
        if (!currency.isVisible()) {
            showCurrencyInAmount(true);
        }
    }

    /**
     * Shows currency symbols as prefix in amount field.
     */
    private void showCurrencyInAmount(boolean show) {
        if (show && currency.getValue() != null) {
            amount.setPrefixComponent(new Div(new Text(Currency.getInstance(currency.getValue()).getSymbol(getLocale()))));
        } else {
            amount.setPrefixComponent(null);
        }
    }

    /**
     * Sets the currency.
     * 
     * @param currency the {@code CurrencyUnit} to set.
     */
    public void setCurrency(CurrencyUnit currency) {
        setCurrency(currency.getCurrencyCode());
    }

    /**
     * Sets the currency.
     * 
     * @param currency the {@code Currency} to set.
     */
    public void setCurrency(Currency currency) {
        setCurrency(currency.getCurrencyCode());
    }

    /**
     * Sets the amount. The amount will be formatted given the currency-format of the current locale excluding the currency symbols.
     * 
     * @param amount the {@code Number} to set as amount.
     */
    public void setAmount(Number amount) {
        setAmount(NumberFormat.getCurrencyInstance(getLocale()).format(amount));
    }
    
    /**
     * Sets the amount. Removes all characters possibly added by NumberFormat.format (e.g. currency-signs) i.e. leading/tailing spaces and everything besides numbers, '-', comma and grouping symbols ('., ')
     * 
     * @param amount the {@code String} to set as amount.
     */
    public void setAmount(String amount) {
        this.amount.setValue(amount.replaceAll("[^\\d.,\\h-]", "").replaceAll("^\\h", "").replaceAll("\\h+$", ""));
    }
    
    /**
     * Set the label of the component to the given text.
     *
     * @param label the label text to set or {@code null} to clear
     */
    @Override
    public void setLabel(String label) {
        amount.setLabel(label);
    }

    /**
     * Gets the label of the component.
     *
     * @return the label of the component or {@code null} if no label has been set
     */
    @Override
    public String getLabel() {
        return amount.getLabel();
    }

    /**
     * Set the placeholder of the component to the given text.
     *
     * @param placeholder the placeholder text to set or {@code null} to clear
     */
    public void setPlaceholder(String placeholder) {
        amount.setPlaceholder(placeholder);
    }

    /**
     * Gets the placeholder of the component.
     *
     * @return the placeholder of the component or {@code null} if no placeholder has been set
     */
    public String getPlaceholder() {
        return amount.getPlaceholder();
    }

    /**
     * Handle component enable state when the enabled state changes.
     * <p>
     * This sets the enabled state of both the amount and currency fields.
     *
     * @param enabled the new enabled state of the component
     */
    @Override
    public void onEnabledStateChanged(boolean enabled) {
        amount.setEnabled(enabled);
        currency.setEnabled(enabled);
    }

    /**
     * Sets the read-only mode of this {@code MoneyField} to given mode. The user can't change the values when in read-only mode.
     *
     * @param readOnly a boolean value specifying whether the component is put in read-only mode or not
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        amount.setReadOnly(readOnly);
        setCurrencyReadOnly(readOnly);
    }
    
    /**
     * Hides the currency combobox and instead shows the currency as pre- or postfix (according to locale) in amount field.
     *
     * @param readOnly a boolean value specifying whether the currency is put in read-only mode or not
     */
    public void setCurrencyReadOnly(boolean readOnly) {
        showCurrencyInAmount(readOnly);
        currency.setVisible(!readOnly);
        currency.setWidth(readOnly ? 0f : 5.45f, Unit.REM);
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        amount.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants theme variants to add
     */
    public void addThemeVariants(TextFieldVariant... variants) {
        amount.addThemeVariants(variants);
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants theme variants to remove
     */
    public void removeThemeVariants(TextFieldVariant... variants) {
        amount.removeThemeVariants(variants);
    }
    
    @Override
    public void setId(String id) {
        super.setId(id);
        amount.setId(id+".amount");
        currency.setId(id+".currency");
    }

    /**
     * Gets the error to show when the input value is invalid.
     * <p>
     * This property is not synchronized automatically from the client side, so the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code errorMessage} property from the webcomponent
     */
    @Override
    public String getErrorMessage() {
        return amount.getErrorMessage();
    }

    /**
     * Sets the error to show when the input value is invalid.
     *
     * @param errorMessage the String value to set
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        amount.setErrorMessage(errorMessage);
    }

    /**
     * This property is set to true when the control value is invalid.
     *
     * @return the {@code invalid} property from the webcomponent
     */
    @Override
    public boolean isInvalid() {
        return amount.isInvalid();
    }

    /**
     * This property is set to true when the control value is invalid.
     *
     * @param invalid the boolean value to set
     */
    @Override
    public void setInvalid(boolean invalid) {
        amount.setInvalid(invalid);
    }


}
