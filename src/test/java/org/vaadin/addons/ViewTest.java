package org.vaadin.addons;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;

import org.javamoney.moneta.FastMoney;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the View containing the MoneyField. Uses the Browserless testing approach as provided by the
 * [Karibu Testing](https://github.com/mvysny/karibu-testing) library.
 */
public class ViewTest {
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
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("money"));
        _setValue(money, FastMoney.of(1, "EUR"));

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(1.0, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testSetNegativeAmount() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("money"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(-123.456, model.getMoney().getNumber().doubleValue());
    }
    
    @Test
    public void testChangeAmount() {
        Model model = ((View)UI.getCurrent().getChildren().findFirst().get()).getModel();
        MoneyField money = _get(MoneyField.class, spec -> spec.withCaption("money"));
        _setValue(money, FastMoney.of(-123.456, "EUR"));
        
        TextField amount = _get(TextField.class, spec -> spec.withId("amount"));
        _setValue(amount, "543,21");

        _click(_get(Button.class, spec -> spec.withCaption("Ok")));
        
        assertEquals(543.21, model.getMoney().getNumber().doubleValue());
    }
}