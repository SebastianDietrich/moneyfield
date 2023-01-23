package org.vaadin.addons;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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

        // no need - Karibu-Testing will automatically navigate to view mapped to the root path "/".
//        UI.getCurrent().navigate(MainView.class);
    }

    @AfterEach
    public void tearDownVaadin() {
        MockVaadin.tearDown();
    }

    @Test
    public void smokeTest() {
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
    public void testSetAmount() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(1, "EUR"));
        assertEquals("1,00", _get(TextField.class, spec -> spec.withId("amount")).getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(1.0, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testSetNegativeAmount() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));

        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "-123,456");
        assertEquals("-123,46", amount.getValue());
        
        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(-123.46, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testLocale() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(123.456, "EUR"));

        Locale locale = UI.getCurrent().getLocale();
        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
      
        UI.getCurrent().setLocale(new Locale("pl", "PL"));
        _setValue(amount, "123 456,789");
        assertEquals("123\u00a0456,79", _get(TextField.class, spec -> spec.withId("amount")).getValue(), "€ Money in polish locale should accept ' ' as thousands separator.");
        _setValue(amount, "123\u00a0456,789");
        assertEquals("123\u00a0456,79", _get(TextField.class, spec -> spec.withId("amount")).getValue(), "€ Money in polish locale should accept '\\u00a0' as thousands separator.");
        _setValue(amount, "123456,789");
        assertEquals("123\u00a0456,79", _get(TextField.class, spec -> spec.withId("amount")).getValue(), "€ Money in polish locale should correct to '\u00a0' (non breaking space) as thousands separator.");
        
        UI.getCurrent().setLocale(new Locale("hi", "IN"));
        _setValue(amount, "1,23,456.789"); //indians use a variable group-length
        assertEquals("1,23,456.79", _get(TextField.class, spec -> spec.withId("amount")).getValue(), "€ Money in indian locale should accept variable group-length.");
        _setValue(amount, "987654.321"); //indians use a variable group-length
        assertEquals("9,87,654.32", _get(TextField.class, spec -> spec.withId("amount")).getValue(), "€ Money in indian locale should correct to variable group-length.");

        UI.getCurrent().setLocale(new Locale("en", "US"));
        _setValue(amount, "123,456.789");
        assertEquals("123,456.79", _get(TextField.class, spec -> spec.withId("amount")).getValue());
        _setValue(amount, "123456.789");
        assertEquals("123,456.79", _get(TextField.class, spec -> spec.withId("amount")).getValue());
        
        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(123456.79, model.getMoney().getNumber().doubleValue());
        
        UI.getCurrent().setLocale(locale);
    }
    
    
    @Test
    public void testChangeAmount() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));
        
        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "543,21");
        assertEquals("543,21", amount.getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(543.21, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testSetAmountWithDotComma() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));

        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "-5.214,12");
        assertEquals("-5.214,12", amount.getValue());
        
        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(-5214.12, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testSetAmountWithNonNumericValue() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));

        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "5..214,1234");
        assertEquals("5..214,1234", amount.getValue(), "amount should not be changed when entering illegal value");
        
        assertTrue(money.isInvalid());
    }
    
    @Test
    public void testSetAmountWithMatchingButStillNonNumericValue() {
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));

        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "5..214,12");
        assertTrue(money.isInvalid());
    }
    
    @Test
    public void testCalculateAmount() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));
        
        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "1+2+3");
        assertEquals("6,00", amount.getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(6, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testCalculateAmountWithDoubles() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));
        
        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "1,123+2,456+3,789");
        assertEquals("7,37", amount.getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(7.37, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testCalculateAmountWithSpaces() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));
        
        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "1,1 + 2,2 + 3,3");
        assertEquals("6,60", amount.getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(6.6, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testCalculateAmountWithAll4OperatorsAndParenthesis() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));
        
        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "((1+2) * 3) / (4-1)");
        assertEquals("3,00", amount.getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(3, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testCalculateAmountWithGrouping() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("Amount"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));
        
        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "1.000,12 * 2.000,34");
        assertEquals("2.000.580,04", amount.getValue());

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(2000580.04, model.getMoney().getNumber().doubleValue());
    }
}