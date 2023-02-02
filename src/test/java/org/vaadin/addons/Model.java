package org.vaadin.addons;

import javax.money.MonetaryAmount;

public class Model {
    private MonetaryAmount money;
    private MonetaryAmount calculableMoney;
    
    public MonetaryAmount getMoney() {
        return money;
    }

    public void setMoney(MonetaryAmount money) {
        this.money = money;
    }

    public MonetaryAmount getCalculableMoney() {
        return calculableMoney;
    }

    public void setCalculableMoney(MonetaryAmount calculableMoney) {
        this.calculableMoney = calculableMoney;
    }
   
}
