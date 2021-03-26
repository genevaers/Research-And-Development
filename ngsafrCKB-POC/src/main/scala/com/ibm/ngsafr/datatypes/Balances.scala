package com.ibm.ngsafr.datatypes

// ngsafr Common Key Buffer in Scala POC
// (c) Copyright IBM Corporation. 2018
// SPDX-License-Identifier: Apache-2.0
// Written by: Sibasis Das

case class Balances(

                // Element        Data Type                 Default Value           Description
                balinstID: String,                          // "0",                 Instrument ID
                balbalID: String,                           // "0",                 Balance ID
                balledgerID: String,                        // "ACTUALS",           Ledger
                baljrnlType: String,                        // "FIN",               Jrnl Type
                balbookCodeID: String,                      // "SHRD-3RD-PARTY",  Book-code / Basis
                ballegalEntityID: String,                   // "STATE-OF-VA",     Legal Entity (CO. / Owner)
                balcenterID: String,                        // "0",               Center ID
                balprojectID: String,                       // "0",               Project ID
                balproductID: String,                       // "0",               Product / Material ID
                balaccountID: String,                       // "0",               Nominal Account
                balcurrencyCodeSourceID: String,            // "USD",             Curr. Code Source
                balcurrencyTypeCodeSourceID: String,        // "TXN",             Currency Type Code Source
                balcurrencyCodeTargetID: String,            // "USD",             Curr. Code Target
                balcurrencyTypeCodeTargetID: String,        // "BASE-LE",         Currency Type Code Target
                balfiscalYear: String,                      // "0",               Fiscal Year - main bucket key
                baltransAmount: String,                     // "0",               Transaction Amount
                balmovementFlg: String,                     // "N",               Movement Flag
                balunitOfMeasure: String,                   // " ",               Unit of Measure
                balstatisticAmount: String)                 // " ",               Statistical Amount

