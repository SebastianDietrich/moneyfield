package org.vaadin.addons;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;

import java.util.Locale;

@Route("")
public class View extends Div {
    private static final long serialVersionUID = -5499894068176661247L;
    
    private MoneyField calculableMoney, money, calculableFixedCurrency, readOnlyMoney;
    private Binder<Model> binder;
    private Model model;

    public View() {
        UI.getCurrent().setLocale(new Locale("de", "DE"));
        calculableMoney = new MoneyField("Calculable", "EUR", true);
        calculableMoney.setLabel("Calculable");
        calculableMoney.setId("calculableMoney");
        calculableMoney.setMinWidth(11f, Unit.REM);
        calculableMoney.setWidthFull();
        calculableMoney.setErrorMessage("ungültige Eingabe");
        
        money = new MoneyField("Not-Calculable", "EUR");
        money.setId("money");
        money.setMinWidth(11.5f, Unit.REM);
        money.setWidthFull();
        
        calculableFixedCurrency = new MoneyField("Calculable with fixed currency", "EUR", true);
        calculableFixedCurrency.setCurrencyReadOnly(true);
        calculableFixedCurrency.setId("calculableFixedCurrency");
        calculableFixedCurrency.setMinWidth(6f, Unit.REM);
        calculableFixedCurrency.setWidthFull();
        calculableFixedCurrency.setErrorMessage("ungültige Eingabe");
        
      
        readOnlyMoney = new MoneyField("ReadOnly Money", "EUR");
        readOnlyMoney.setId("readOnlyMoney");
        readOnlyMoney.setAmount(8888);
        readOnlyMoney.setReadOnly(true);
        readOnlyMoney.setMinWidth(6f, Unit.REM);
        readOnlyMoney.setWidthFull();
        
        Button ok = new Button("Ok");
        Button reload = new Button("Reload");
        add(new HorizontalLayout(calculableMoney, money, new TextField("TextField"), calculableFixedCurrency, readOnlyMoney), ok, reload);
        
        model = new Model();
        binder = new Binder<>(Model.class);
        binder.bindInstanceFields(this);
        binder.setBean(model);
        
        ok.addClickListener(buttonClickEvent -> {
            try {
                binder.writeBean(model);
            } catch (ValidationException e) {
                assert false: "ValidationException thrown";
            }
        });
        
        reload.addClickListener(buttonClickEvent -> {
            binder.refreshFields();
        });
    }

    public Model getModel() {
        return model;
    }
    
   
}
