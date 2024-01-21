package org.vaadin.addons;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Locale;

import org.javamoney.moneta.FastMoney;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the View containing the MoneyField. Uses the Browserless testing approach as provided by the
 * [Karibu Testing](https://github.com/mvysny/karibu-testing) library.
 */
public class MoneyFieldTest {
    private static Routes routes;

    @BeforeAll
    public static void discoverRoutes() {
        // Route discovery involves classpath scanning and is an expensive operation.
        // Running the discovery process only once per test run speeds up the test runtime considerably.
        // Discover the routes once and cache the result.
        routes = new Routes().autoDiscoverViews("org.vaadin.addons");
    }

    @BeforeEach
    public void mockVaadin() {
        // MockVaadin.setup() registers all @Routes, prepares the Vaadin instances for us
        // (the UI, the VaadinSession, VaadinRequest, VaadinResponse, ...) and navigates to the root route.
        MockVaadin.setup(routes);
    }

    @AfterEach
    public void tearDownVaadin() {
        MockVaadin.tearDown();
    }

    @Test
    void smokeTest() {
        // Smoke test is a quick test to check that the basic machinery is in place and works.
        // The analogy would be to turn on an electric device (e.g. a coffee maker)
        // then turn it off immediately without even checking whether it actually works or not,
        // and watch whether there is any smoke. If yes, the coffee maker is
        // probably burning from a short-circuit and any further tests are pointless.

        // The root route should be set directly in the UI; let's check whether it is so.
        // This demoes the direct access to the UI and its children and grand-children,
        // which encompasses all visible Vaadin components.
        /*View main = (View)*/ UI.getCurrent().getChildren().findFirst().get();

        // However when using this kind of low-level lookups, the code quickly gets
        // pretty complicated. Let's use the _get() function instead,
        // which will walk the UI tree for us.
        _assertOne(View.class);
    }
    
    @Test
    void testSetAmount() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(1, "EUR"));
        
        assertEquals("1,00", _get(TextField.class, spec -> spec.withId("money.amount")).getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(1.0, model.getMoney().getNumber().doubleValue());
    }
    
    private void setValue(TextField amount, String value) {
        _setValue(amount, value);
        _fireValueChange(amount);
    }
    
    @Test
    void testSetNegativeAmount() {
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        setValue(amount, "-123,456");
        
        assertEquals("-123,46", amount.getValue());
        
        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(-123.46, ((View)UI.getCurrent().getChildren().findFirst().get()).getModel().getMoney().getNumber().doubleValue());
    }
    
    @Test
    void testLocale() {
        Locale locale = UI.getCurrent().getLocale();
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        
        setValue(amount, "123456,789");
        assertEquals("123.456,79", amount.getValue());
        setValue(amount, " 123456,789");
        assertEquals("123.456,79", amount.getValue());
        setValue(amount, " 123.456,79");
        assertEquals("123.456,79", amount.getValue());

      
        UI.getCurrent().setLocale(new Locale("pl", "PL"));
        setValue(amount, "123 456,789");
        assertEquals("123\u00a0456,79", amount.getValue(), "€ Money in polish locale should accept ' ' as thousands separator.");
        setValue(amount, "123\u00a0456,789");
        assertEquals("123\u00a0456,79", amount.getValue(), "€ Money in polish locale should accept '\\u00a0' as thousands separator.");
        setValue(amount, "123456,789");
        assertEquals("123\u00a0456,79", amount.getValue(), "€ Money in polish locale should correct to '\u00a0' (non breaking space) as thousands separator.");
        
        UI.getCurrent().setLocale(new Locale("hi", "IN"));
        setValue(amount, "1,23,456.789"); //indians use a variable group-length
        assertEquals("1,23,456.79",amount.getValue(), "€ Money in indian locale should accept variable group-length.");
        setValue(amount, "987654.321"); //indians use a variable group-length
        assertEquals("9,87,654.32", amount.getValue(), "€ Money in indian locale should correct to variable group-length.");

        UI.getCurrent().setLocale(new Locale("en", "US"));
        setValue(amount, "123,456.789");
        assertEquals("123,456.79", amount.getValue());
        setValue(amount, "123456.789");
        assertEquals("123,456.79", amount.getValue());
        
        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(123456.79, ((View)UI.getCurrent().getChildren().findFirst().get()).getModel().getMoney().getNumber().doubleValue());
        
        UI.getCurrent().setLocale(locale);
    }
    
    
    @Test
    void testChangeAmount() {
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        setValue(amount, "543,21");
        assertEquals("543,21", amount.getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(543.21, ((View)UI.getCurrent().getChildren().findFirst().get()).getModel().getMoney().getNumber().doubleValue());
    }
    
    @Test
    void testChangeAmountInModel() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        model.setMoney(FastMoney.of(987.65, "USD"));

        _click(_get(Button.class, spec -> spec.withCaption("Reload")));
        
        assertEquals("987,65", _get(TextField.class, spec -> spec.withId("money.amount")).getValue());
        assertEquals("USD", _get(ComboBox.class, spec -> spec.withId("money.currency")).getValue());
    }
    
    @Test
    void testSetAmountWithDotComma() {
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        setValue(amount, "-5.214,12");
        assertEquals("-5.214,12", amount.getValue());
        
        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(-5214.12, ((View)UI.getCurrent().getChildren().findFirst().get()).getModel().getMoney().getNumber().doubleValue());
    }
    
    @Test
    void testSetAmountWithNonNumericValue() {
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        setValue(amount, "5..214,1234");
        assertEquals("5..214,1234", amount.getValue(), "amount should not be changed when entering illegal value");
        
        assertTrue(_get(MoneyField.class, spec -> spec.withCaption("Amount")).isInvalid());
    }
    
    @Test
    void testSetAmountWithMatchingButStillNonNumericValue() {
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        setValue(amount, "5..214,12");
        assertTrue(_get(MoneyField.class, spec -> spec.withCaption("Amount")).isInvalid());
    }
    
    @Test
    void testSetAmountWithBigDecimal() {
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        setValue(amount, "71000000000000,01");
        assertEquals("71.000.000.000.000,01", amount.getValue());
    }
    
    @Test
    void testCalculateAmount() {
        TextField amount = _get(TextField.class, spec -> spec.withId("calculableMoney.amount"));
        setValue(amount, "1+2+3");
        assertEquals("6,00", amount.getValue());
    }
    
    @Test
    void testCalculateAmountWithBigDecimal() {
        TextField amount = _get(TextField.class, spec -> spec.withId("calculableMoney.amount"));
        setValue(amount, "71000000000000,01+71000000000000,01");
        assertEquals("142.000.000.000.000,02", amount.getValue());
    }
    
    @Test
    void testCalculateAmountWithDoubles() {
        TextField amount = _get(TextField.class, spec -> spec.withId("calculableMoney.amount"));
        setValue(amount, "1,123+2,456+3,789");
        assertEquals("7,37", amount.getValue());
    }
    
    @Test
    void testCalculateAmountWithSpaces() {
        TextField amount = _get(TextField.class, spec -> spec.withId("calculableMoney.amount"));
        setValue(amount, "1,1 + 2,2 + 3,3");
        assertEquals("6,60", amount.getValue());
    }
    
    @Test
    void testCalculateAmountWithAll5OperatorsAndParenthesis() {
        TextField amount = _get(TextField.class, spec -> spec.withId("calculableMoney.amount"));
        setValue(amount, "(((1+2) * 3) / (4-1))^2,5");
        assertEquals("15,59", amount.getValue());
    }
    
    @Test
    void testCalculateAmountWithGrouping() {
        TextField amount = _get(TextField.class, spec -> spec.withId("calculableMoney.amount"));
        setValue(amount, "1.000,12 * 2.000,34");
        assertEquals("2.000.580,04", amount.getValue());
    }
    
    @Test
    void testReadOnlyCurrency() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        money.setCurrencyReadOnly(true);
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        
        _setValue(money, FastMoney.of(-123.456, "EUR"));
        assertEquals("€", amount.getPrefixComponent().getElement().getText());
        
        _setValue(money, FastMoney.of(-123.456, "ATS"));
        assertEquals("öS", amount.getPrefixComponent().getElement().getText());
        
        _setValue(money, FastMoney.of(-123.456, "CNY"));
        assertEquals("CN¥", amount.getPrefixComponent().getElement().getText());
        
        _setValue(money, FastMoney.of(-123.456, "INR"));
        assertEquals("₹", amount.getPrefixComponent().getElement().getText());
        
        _setValue(money, FastMoney.of(-123.456, "USD"));
        assertEquals("$", amount.getPrefixComponent().getElement().getText());
        
        _setValue(money, FastMoney.of(-123.456, "PLN"));
        assertEquals("PLN", amount.getPrefixComponent().getElement().getText(), "interestingly this is not zł but PLN");
    }
    
    @Test
    void testClear() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        money.clear();

        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        assertEquals("", amount.getValue());
        assertNull(_get(ComboBox.class, spec -> spec.withId("money.currency")).getValue());
    }
}