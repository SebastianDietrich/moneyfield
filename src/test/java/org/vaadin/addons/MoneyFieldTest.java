package org.vaadin.addons;

import com.github.mvysny.kaributesting.v10.BasicUtilsKt;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Locale;

import org.javamoney.moneta.FastMoney;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        MoneyField money = _get(MoneyField.class, spec -> spec.withId("money"));
        _setValue(money, FastMoney.of(1, "EUR"));

        assertEquals("1,00", _get(TextField.class, spec -> spec.withId("money.amount")).getValue());

        _click(_get(Button.class, spec -> spec.withText("Ok")));

        assertEquals(1.0, model.getMoney().getNumber().doubleValue());
    }

    @Test
    void testSetNullAmount() {
        Model model = ((View) UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withId("money"));
        _setValue(money, null);

        assertEquals("", _get(TextField.class, spec -> spec.withId("money.amount")).getValue());

        _click(_get(Button.class, spec -> spec.withText("Ok")));

        assertEquals(null, model.getMoney());
    }

    private String setAndGetAmount(String moneyFieldId, String newAmount) {
        MoneyField moneyField = _get(MoneyField.class, spec -> spec.withId(moneyFieldId));
        TextField amount = _get(TextField.class, spec -> spec.withId(moneyFieldId + ".amount"));
        _setValue(amount, newAmount); //TODO use _setValue(amount, newAmount, true); when made public - see https://github.com/mvysny/karibu-testing/issues/181
        BasicUtilsKt._fireDomEvent(moneyField, "change"); //fire manually since MoneyField = CustomField is triggered by a DOM event which is not fired since there is no DOM in karibu-testing
        return amount.getValue();
    }

    @Test
    void testSetNegativeAmount() {
        assertEquals("-123,46", setAndGetAmount("money", "-123,456"));

        _click(_get(Button.class, spec -> spec.withText("Ok")));

        assertEquals(-123.46, ((View)UI.getCurrent().getChildren().findFirst().get()).getModel().getMoney().getNumber().doubleValue());
    }

    @Test
    void testSetGetPlaceholder() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withId("money"));
        money.setPlaceholder("test placeholder");
        assertEquals("test placeholder", money.getPlaceholder());
    }

    @Test
    void testSetIsRequiredIndicatorVisible() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withId("money"));
        money.setRequiredIndicatorVisible(true);
        assertEquals(true, money.isRequiredIndicatorVisible());
        money.setRequiredIndicatorVisible(false);
        assertEquals(false, money.isRequiredIndicatorVisible());
    }

    @Test
    void testLocale() {
        Locale locale = UI.getCurrent().getLocale();

        UI.getCurrent().setLocale(new Locale("de", "AT"));
        assertEquals("123.456,79", setAndGetAmount("money", "123456,789"));
        assertEquals("123.456,79", setAndGetAmount("money", "123456,789"));
        assertEquals("123.456,79", setAndGetAmount("money", " 123.456,79"));

        UI.getCurrent().setLocale(new Locale("hi", "IN")); //indians use a variable group-length
        assertEquals("1,23,456.79",setAndGetAmount("money", "1,23,456.789"), "€ Money in indian locale should accept variable group-length.");
        assertEquals("9,87,654.32", setAndGetAmount("money", "987654.321"), "€ Money in indian locale should correct to variable group-length.");

        UI.getCurrent().setLocale(new Locale("pl", "PL")); //poles have spaces (non breaking space) as thousands separators
        assertEquals("123\u00a0456,79", setAndGetAmount("money", "123 456,789"), "€ Money in polish locale should accept ' ' as thousands separator.");
        assertEquals("123\u00a0456,79", setAndGetAmount("money", "123456,789"), "€ Money in polish locale should correct to '\u00a0' (non breaking space) as thousands separator.");
        assertEquals("123\u00a0456,79", setAndGetAmount("money", "123\u00a0456,789"), "€ Money in polish locale should accept '\\u00a0' as thousands separator.");


        UI.getCurrent().setLocale(new Locale("en", "US"));
        assertEquals("123,456.79", setAndGetAmount("money", "123,456.789"));
        assertEquals("123,456.79", setAndGetAmount("money", "123456.789"));

        _click(_get(Button.class, spec -> spec.withText("Ok")));

        assertEquals(123456.79, ((View)UI.getCurrent().getChildren().findFirst().get()).getModel().getMoney().getNumber().doubleValue());

        UI.getCurrent().setLocale(locale);
    }


    @Test
    void testChangeAmount() {
        assertEquals("543,21", setAndGetAmount("money", "543,21"));

        _click(_get(Button.class, spec -> spec.withText("Ok")));

        assertEquals(543.21, ((View)UI.getCurrent().getChildren().findFirst().get()).getModel().getMoney().getNumber().doubleValue());
    }

    @Test
    void testChangeAmountInModel() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        model.setMoney(FastMoney.of(987.65, "USD"));

        _click(_get(Button.class, spec -> spec.withText("Reload")));

        assertEquals("987,65", _get(TextField.class, spec -> spec.withId("money.amount")).getValue());
        assertEquals("USD", _get(ComboBox.class, spec -> spec.withId("money.currency")).getValue());
    }

    @Test
    void testSetAmountWithDotComma() {
        assertEquals("-5.214,12", setAndGetAmount("money", "-5.214,12"));

        _click(_get(Button.class, spec -> spec.withText("Ok")));

        assertEquals(-5214.12, ((View)UI.getCurrent().getChildren().findFirst().get()).getModel().getMoney().getNumber().doubleValue());
    }

    @Test
    void testSetAmountWithNonNumericValue() {
        assertEquals("5..214,1234", setAndGetAmount("money", "5..214,1234"), "amount should not be changed when entering illegal value");
        assertTrue(_get(MoneyField.class, spec -> spec.withId("money")).isInvalid());
    }

    @Test
    void testSetAmountWithMatchingButStillNonNumericValue() {
        assertEquals("5..214,12", setAndGetAmount("money", "5..214,12"), "amount should not be changed when entering illegal value");
        assertTrue(_get(MoneyField.class, spec -> spec.withId("money")).isInvalid());
    }

    @Test
    void testSetAmountWithBigDecimal() {
        assertEquals("71.000.000.000.000,01", setAndGetAmount("money", "71000000000000,01"));
    }

    @Test
    void testCalculateAmount() {
        assertEquals("6,00", setAndGetAmount("calculableMoney", "1+2+3"));
    }

    @Test
    void testSetCalculableAmountWithBigDecimal() {
        assertEquals("71.000.000.000.000,01", setAndGetAmount("calculableMoney", "71000000000000,01"));
    }

    @Test
    void testCalculateAmountWithBigDecimal() {
        assertEquals("142.000.000.000.000,02", setAndGetAmount("calculableMoney", "71000000000000,01+71000000000000,01"));
    }

    @Test
    void testCalculateAmountWithDoubles() {
        assertEquals("7,37", setAndGetAmount("calculableMoney", "1,123+2,456+3,789"));
    }

    @Test
    void testCalculateAmountWithDividingDoubles() {
        assertEquals("112,34", setAndGetAmount("calculableMoney", "1458,15/12,98"));
    }

    @Test
    void testCalculateAmountWithDividingByZero() {
        assertEquals("1/0", setAndGetAmount("calculableMoney", "1/0"), "amount should not be changed when entering illegal value");
        assertTrue(_get(MoneyField.class, spec -> spec.withId("calculableMoney")).isInvalid());
    }

    @Test
    void testCalculateAmountWithSpaces() {
        assertEquals("-2,20", setAndGetAmount("calculableMoney", "-1,1 + 2,2 + -3,3"));
    }

    @Test
    void testCalculateAmountWithAll5OperatorsAndParenthesis() {
        assertEquals("15,59", setAndGetAmount("calculableMoney", "(((1+2) * 3) / (4-1))^2,5"));
    }

    @Test
    void testCalculateAmountWithWrongParenthesis() {
        assertEquals("(1+2 ^ 2", setAndGetAmount("calculableMoney", "(1+2 ^ 2"));
        assertTrue(_get(MoneyField.class, spec -> spec.withId("calculableMoney")).isInvalid());
    }

    @Test
    void testCalculateAmountWithGrouping() {
        assertEquals("2.000.580,04", setAndGetAmount("calculableMoney", "1.000,12 * 2.000,34"));
    }

    @Test
    void testCalculateAmountWithIllegalCharacters() {
        assertEquals("1 + 2x", setAndGetAmount("calculableMoney", "1 + 2x"));
        assertTrue(_get(MoneyField.class, spec -> spec.withId("calculableMoney")).isInvalid());
    }

    @Test
    void testReadOnlyCurrency() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withId("money"));
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
        MoneyField money = _get(MoneyField.class, spec -> spec.withId("money"));
        money.clear();

        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        assertEquals("", amount.getValue());
        assertNull(_get(ComboBox.class, spec -> spec.withId("money.currency")).getValue());
        assertNull(_get(MoneyField.class, spec -> spec.withId("money")).getValue());
    }

    @Test
    void testAddRemoveThemeVariant() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withId("money"));

        money.addThemeVariants(CustomFieldVariant.LUMO_SMALL);
        money.removeThemeVariants(CustomFieldVariant.LUMO_SMALL);
    }

    @Test
    void testSetCurrencyReadOnly() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withId("money"));
        TextField amount = _get(TextField.class, spec -> spec.withId("money.amount"));
        ComboBox<String> currency =  _get(ComboBox.class, spec -> spec.withId("money.currency"));

        assertNull(amount.getPrefixComponent());
        assertTrue(currency.isVisible());

        money.setCurrencyReadOnly(true);

        assertNotNull(amount.getPrefixComponent());
        assertFalse(currency.isVisible());

        money.setCurrencyReadOnly(false);

        assertNull(amount.getPrefixComponent());
        assertTrue(currency.isVisible());
    }
}