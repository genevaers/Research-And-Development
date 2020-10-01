package com.ibm.ngsafr.datatypes

// ngsafr Common Key Buffer in Scala POC
// (c) Copyright IBM Corporation. 2018
// SPDX-License-Identifier: Apache-2.0
// Written by: Sibasis Das

case class Transaction (
                   // Element      Data Type                Default Value     KEY col Description
                   transinstID: String,                     // "0",         X  1  Instrument ID
                   transjrnlID: String,                     // "0",            2  Business Event / Journal ID
                   transjrnlLineID: String,                 // "1",            3  Jrnl Line No
                   transjrnlDescript: String,               // " ",            4  Journal / Event Description
                   transledgerID: String,                   // "ACTUALS",   X  5  Ledger
                   transjrnlType: String,                   // "FIN",       X  6  Jrnl Type
                   transbookCodeID: String,            // "SHRD-3RD-PARTY", X  7  Book-code / Basis
                   translegalEntityID: String,              // "STATE-OF-VA"X  8  Legal Entity (CO. / Owner)
                   transcenterID: String,                   // "0",         X  9  Center ID
                   transprojectID: String,                  // "0",         X  10 Project ID
                   transproductID: String,                  // "0",         X  11 Product / Material ID
                   transaccountID: String,                  // "0",         X  12 Nominal Account
                   transcurrencyCodeSourceID: String,       // "USD",       X  13 Curr. Code Source
                   transcurrencyTypeCodeSourceID: String,   // "TXN",       X  14 Currency Type Code Source
                   transcurrencyCodeTargetID: String,       // "USD",       X  15 Curr. Code Target
                   transcurrencyTypeCodeTargetID: String,   // "BASE-LE",   X  16 Currency Type Code Target
                   transtransAmount: String,                // "0",            17 Transaction Amount
                   transfiscalPeriod: String,               // "0",         X  18 Fiscal Period
                   transacctDate: String,                   // "0",               Acctg. Date
                   transtransDate: String,                  // "0",               Transaction Date
                   transdirVsOffsetFlg: String,             // "O",               Direct vs. Offset Flag
                   transreconcileFlg: String,               // "N",               Reconciliable Flag
                   transadjustFlg: String,                  // "N",               Adjustment Flag
                   transmovementFlg: String,                // "N",               Movement Flag
                   transunitOfMeasure: String,              // " ",               Unit of Measure
                   transstatisticAmount: String,            // " ",               Statistical Amount
                   transextensionIDAuditTrail: String,      // " ",               Audit Trial Extension ID
                   transextensionIDSource: String,          // " ",               Source Extension ID
                   transextensionIDClass: String,           // " ",               Classification Extension ID
                   transextensionIDDates: String,           // " ",               Date Extension ID
                   transextensionIDCustom: String)          // " "                Other Customization Ext ID

