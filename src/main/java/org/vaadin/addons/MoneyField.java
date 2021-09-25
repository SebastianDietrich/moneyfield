package org.vaadin.addons;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.StringUtils;
import org.javamoney.moneta.FastMoney;
import org.vaadin.textfieldformatter.NumeralFieldFormatter;

@Tag("money-field")
// @JsModule("@polymer/money-field/money-field.js")
// @NpmPackage(value = "@polymer/money-field", version = "^3.0.1")
/*
 * If you wish to include your own JS modules in the add-on jar, add the module files to './src/main/resources/META-INF/resources/frontend'
 * and insert an annotation @JsModule("./my-module.js") here.
 */
public class MoneyField extends AbstractCompositeField<Div, MoneyField, MonetaryAmount> implements HasLabel, HasSize {
    private static final long serialVersionUID = -6563463270512422984L;

    private TextField amount;
    private ComboBox<String> currency;

    /**
     * Constructs an empty {@code MoneyField}.
     */
    public MoneyField() {
        this((MonetaryAmount) null);
    }

    /**
     * Constructs an empty {@code MoneyField} with the given initial value and currencyCodes.
     *
     * @param initialValue the initial value
     * @param currencyCodes the currencyCodes to set in the currency selection
     */
    public MoneyField(MonetaryAmount initialValue, List<String> currencyCodes) {
        super(initialValue);

        if (initialValue != null && !currencyCodes.contains(initialValue.getCurrency().getCurrencyCode())) {
            throw new IllegalArgumentException(
                "The initial values currency code '" +
                    initialValue.getCurrency().getCurrencyCode() +
                    "' is not in the list of currency codes.");
        }

        amount = new TextField();
        amount.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        amount.setSizeUndefined();
        new NumeralFieldFormatter(".", ",", 3).extend(amount);

        currency = new ComboBox<>();
        currency.setItems(currencyCodes);
        currency.setWidth(6, Unit.EM);

        setValue(initialValue);

        amount.addValueChangeListener(listener -> {
            setModelValue(asMonetaryAmount(), true);
        });
        currency.addValueChangeListener(listener -> {
            setModelValue(asMonetaryAmount(), true);
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setSizeUndefined();
        layout.add(amount, currency);
        layout.setFlexGrow(1, amount);
        layout.setAlignItems(Alignment.END); // so that amount with label is aligned to currency

        getContent().add(layout);
    }

    private FastMoney asMonetaryAmount() {
        if (StringUtils.isEmpty(amount.getValue()) || StringUtils.isEmpty(currency.getValue())) return null;

        return FastMoney.of(new BigDecimal(amount.getValue().replace(".", "").replace(',', '.')), currency.getValue());
    }

    /**
     * Constructs an empty {@code MoneyField} with the given initial value.
     *
     * @param initialValue the initial value
     */
    public MoneyField(MonetaryAmount initialValue) {
        this(initialValue, getAvailableCurrencyCodes());
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

    private static List<String> getAvailableCurrencyCodes() {
        return Currency.getAvailableCurrencies().stream().map(Currency::getCurrencyCode).sorted().collect(Collectors.toList());
    }

    /**
     * Constructs an empty {@code MoneyField} with the given label.
     *
     * @param label the text to set as the label
     */
    public MoneyField(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code MoneyField} with the given label and placeholder text.
     *
     * @param label the text to set as the label
     * @param placeholder the placeholder text to set
     */
    public MoneyField(String label, String placeholder) {
        this(label);
        amount.setPlaceholder(placeholder);
    }

    /**
     * Constructs {@code MoneyField} with the given label and initial value.
     *
     * @param label the text to set as the label
     * @param initialValue the initial value
     */
    public MoneyField(String label, MonetaryAmount initialValue) {
        this(initialValue);
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
        this(label, initialValue);
        amount.setPlaceholder(placeholder);
    }

    @Override
    protected void setPresentationValue(MonetaryAmount monetaryAmount) {
        NumberFormat amountFormat = NumberFormat.getNumberInstance(Locale.GERMAN);
        amount.setValue(amountFormat.format(monetaryAmount.getNumber().numberValue(BigDecimal.class)));
        currency.setValue(monetaryAmount.getCurrency().getCurrencyCode());
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

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        amount.setEnabled(enabled);
        currency.setEnabled(enabled);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        amount.setReadOnly(readOnly);
        currency.setReadOnly(readOnly);
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        amount.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    public void setCurrency(String currencyCode) {
        currency.setValue(currencyCode);
    }

    public void setCurrency(CurrencyUnit currency) {
        setCurrency(currency.getCurrencyCode());
    }

    public void setCurrency(Currency currency) {
        setCurrency(currency.getCurrencyCode());
    }

    public void setAmount(Number amount) {
        this.amount.setValue(amount.toString());
    }

}
