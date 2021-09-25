package org.vaadin.addons;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class View extends Div {
    private static final long serialVersionUID = -5499894068176661247L;

    public View() {
        MoneyField moneyField = new MoneyField();
        add(moneyField);
    }
}
