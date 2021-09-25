package org.vaadin.addons;

import com.vaadin.testbench.TestBenchElement;

import org.junit.Assert;
import org.junit.Test;

public class ViewIT extends AbstractViewTest {

    @Test
    public void componentWorks() {
        final TestBenchElement moneyField = $("money-field").first();

        Assert.assertTrue(moneyField.$(TestBenchElement.class).all().size() > 0);
    }
}
