package org.vaadin.addons;

import javax.money.MonetaryAmount;

public class Model {
    private MonetaryAmount money;
    
    public MonetaryAmount getMoney() {
        return money;
    }

    public void setMoney(MonetaryAmount money) {
        this.money = money;
    }
   
}
