package org.vaadin.addons;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import org.javamoney.moneta.FastMoney;

public class DoubleToFastMoneyConverter implements Converter<Double, FastMoney> {
    private static final long serialVersionUID = 7075280384497381927L;

    @Override
    public Result<FastMoney> convertToModel(Double aDouble, ValueContext valueContext) {
        if (aDouble == null) {
            return Result.ok(null);
        }
        return Result.ok(FastMoney.of(aDouble, "EUR"));
    }

    @Override
    public Double convertToPresentation(FastMoney fastMoney, ValueContext valueContext) {
        if (fastMoney == null) {
            return null;
        }
        return fastMoney.getNumber().doubleValue();
    }
}
