package com.ibm.VA_ledger.datatypes

//***************************************************************************************************************
//
//  Structure:  Transaction.scala - the journal entry structure
//
//  This structure is produced by the transStandardize module, and posted to ledger by the Post module
//
//(c) Copyright IBM Corporation. 2018
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell
//
//  Created July 2018
//
//  Change Log:
//***************************************************************************************************************


case class Transaction (
                   // Element      Data Type
                   var transIPID: String,
                   var transContractID: String,
                   var transCommitmentID: String,
                   var transJrnlID: String,
                   var transJrnlLineID: String,
                   var transBusinessEventCode: String,
                   var transSourceSystemID: String,
                   var transOriginalDocID: String,
                   var transJrnlDescript: String,
                       transLedgerID: String = "ACTUALS",
                       transJrnlType: String = "FIN",
                       transBookCodeID: String = "SHRD-3RD-PARTY",
                   var transLegalEntityID: String,
                   var transCenterID: String,
                   var transProjectID: String,
                       transProductID: String = "0",
                   var transNominalAccountID: String,
                       transAltAccountID:             String = "0",
                       transCurrencyCodeSourceID:     String = "USD",
                       transCurrencyTypeCodeSourceID: String = "TXN",
                       transCurrencyCodeTargetID:     String = "USD",
                       transCurrencyTypeCodeTargetID: String = "BASE-LE",
                   var transFiscalPeriod: String,
                   var transAcctDate: String,
                   var transTransDate: String,
                   var transTransAmount: BigDecimal,  // 26th element
                       transUnitOfMeasure: String = " ",
                       transUnitPrice: BigDecimal = 0,
                       transStatisticAmount: BigDecimal = 0,  // 29th element
                       tranRuleSetID: String = " ",
                       transRuleID: String = " ",
                   var transDirVsOffsetFlg: String,
                       transReconcileFlg: String = "N",
                   var transAdjustFlg: String,
                       transExtensionIDAuditTrail: String = " ",
                       transExtensionIDSource: String = " ",
                       transExtensionIDClass: String = " ",
                       transExtensionIDDates: String = " ",
                       transExtensionIDCustom: String = " ") // 39 elements

