package org.vaadin.addons;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;

import java.util.Locale;

@Route("")
public class View extends Div {
    private static final long serialVersionUID = -5499894068176661247L;
    
    private MoneyField calculableMoney;
    private MoneyField money;
    private Binder<Model> binder;
    private Model model;

    public View() {
        UI.getCurrent().setLocale(new Locale("de", "DE"));
        calculableMoney = new MoneyField("Calc", "EUR", true);
        calculableMoney.setCurrencyReadOnly(true);
        
        money = new MoneyField("Amount", "EUR");
        money.setCurrencyReadOnly(true);
        
        Button button = new Button("Ok");
        add(calculableMoney, money, button);
        
        model = new Model();
        binder = new Binder<>(Model.class);
        binder.bindInstanceFields(this);
        
        button.addClickListener(buttonClickEvent -> {
            try {
                binder.writeBean(model);
            } catch (ValidationException e) {
                // TODO Auto-generated catch block
                assert false: "ValidationException thrown";
            }
        });
    }

    public Model getModel() {
        return model;
    }
    
   
}
