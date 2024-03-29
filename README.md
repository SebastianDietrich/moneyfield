[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/moneyfield)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/moneyfield.svg)](https://vaadin.com/directory/component/moneyfield)
[![Build Status](https://github.com/SebastianDietrich/moneyfield/actions/workflows/maven.yml/badge.svg)](https://github.com/SebastianDietrich/moneyfield)

# Moneyfield

Vaadin 23+ implementation of a field for JSR354 javax.money.MonetaryAmount that includes both an amount and a currency.

Pure java implementation of a composite field based on AbstractCompositeField<Div, MoneyField, MonetaryAmount>.

The entered amount must match the locale (with optional grouping) - e.g. 1 234,56 or 1234,56 for Poland 1,234.56 or 1234.56 for US.
The amount gets rounded and formatted (with grouping) upon losing focus (e.g. 1,23,456.789 -> 1,23,456.79 for India).

Optionally primitive arithmetic calculations (e.g. ((-1+2)*3 - 1)/-4) are allowed and calculated upon losing focus 


## Publishing to Vaadin Directory

You can create the zip package needed for [Vaadin Directory](https://vaadin.com/directory/) using

```
mvn versions:set -DnewVersion=<version> # version must be MAJOR.MINOR.PATCH, no snapshot versions 
mvn install -Pdirectory
```

The package is created as `target/moneyfield-<version>.zip`

Upload it via [Vaadin Directory upload](https://vaadin.com/directory/component/edit/moneyfield/versions).


Icon made by [Freepik](https://www.freepik.com") from [Flaticon](www.flaticon.com)