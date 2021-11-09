[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/moneyfield)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/moneyfield.svg)](https://vaadin.com/directory/component/moneyfield)

# Moneyfield

Vaadin 21+ implementation of a field for JSR354 javax.money.MonetaryAmount that includes both an amount and a currency.

## Development instructions
[![Build Status](https://app.travis-ci.com/SebastianDietrich/moneyfield.svg?branch=main)](https://travis-ci.com/SebastianDietrich/moneyfield)

Pure java implementation of a composite field based on AbstractCompositeField<Div, MoneyField, MonetaryAmount>. Formatting of amount can be set in constructor and is done using javascript on-the-fly during text input using [TextField Formatter](https://vaadin.com/directory/component/textfield-formatter).

## Publishing to Vaadin Directory

You can create the zip package needed for [Vaadin Directory](https://vaadin.com/directory/) using

```
mvn versions:set -DnewVersion=<version> # version must be MAJOR.MINOR.PATCH, no snapshot versions 
mvn install -Pdirectory
```

The package is created as `target/moneyfield-<version>.zip`

Upload it via [Vaadin Directory upload](https://vaadin.com/directory/component/edit/moneyfield/versions).


Icon made by [Freepik](https://www.freepik.com") from [Flaticon](www.flaticon.com)