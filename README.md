[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/moneyfield)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/moneyfield.svg)](https://vaadin.com/directory/component/moneyfield)

# Moneyfield

Vaadin 21+ implementation of a field for JSR354 javax.money.MonetaryAmount that includes both an amount and a currency.

## Development instructions
[![Build Status](https://travis-ci.org/SebastianDietrich/moneyfield.svg?branch=master)](https://travis-ci.org/SebastianDietrich/moneyfield)

Pure java implementation of a composite field based on AbstractCompositeField<Div, MoneyField, MonetaryAmount>

## Publishing to Vaadin Directory

You can create the zip package needed for [Vaadin Directory](https://vaadin.com/directory/) using

```
mvn versions:set -DnewVersion=<version> # You cannot publish snapshot versions 
mvn install -Pdirectory
```

The package is created as `target/moneyfield-<version>.zip`

For more information or to upload the package, visit https://vaadin.com/directory/my-components?uploadNewComponent
