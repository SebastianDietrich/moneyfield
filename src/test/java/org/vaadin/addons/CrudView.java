package org.vaadin.addons;

import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.FastMoney;

@Route("test")
public class CrudView extends VerticalLayout {
    private static final long serialVersionUID = 8001190389759468890L;

    public static class TestDto {
        private int id;
        private String name;
        private MonetaryAmount price;

        public TestDto(int id, String name, FastMoney price) {
            this.setId(id);
            this.setName(name);
            this.setPrice(price);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public MonetaryAmount getPrice() {
            return price;
        }

        public void setPrice(MonetaryAmount price) {
            this.price = price;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }

    // UI
    private final Crud<TestDto> crud;

    // data
    private List<TestDto> database = new ArrayList<>();

    public CrudView() {
        crud = new Crud<>(TestDto.class, createEditor());

        setupDataProvider();
        this.crud.addDeleteListener(event -> this.delete(event.getItem()));

        database.addAll(List.of(
            new TestDto(1, "Item 1", FastMoney.of(0, "EUR")),
            new TestDto(2, "Item 2", FastMoney.of(1, "EUR")),
            new TestDto(3, "Item 3", FastMoney.of(2, "EUR"))
                ));
        add(crud);
    }

    private void setupDataProvider() {
        var dataProvider = new AbstractBackEndDataProvider<TestDto, CrudFilter>() {

            @Override
            protected Stream<TestDto> fetchFromBackEnd(Query<TestDto, CrudFilter> query) {
                return database.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<TestDto, CrudFilter> query) {
                return database.size();
            }
        };
        this.crud.setDataProvider(dataProvider);
    }

    private void delete(TestDto item) {
        database.remove(item);
    }

    private CrudEditor<TestDto> createEditor() {
        Binder<TestDto> binder = new Binder<>(TestDto.class);

        TextField nameField = new TextField("Name");
        binder.forField(nameField)
        .bind(TestDto::getName, TestDto::setName);

        MoneyField priceField = new MoneyField("Price");
        binder.forField(priceField)
        .bind(TestDto::getPrice, TestDto::setPrice);

        FormLayout form = new FormLayout(nameField, priceField);
        return new BinderCrudEditor<>(binder, form);
    }

}