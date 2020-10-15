package com.ibm.VA_ledger.datatypes

//***************************************************************************************************************
//
//  Structure:  Ledger.scala - the ledger structure
//
//  This structure is produced by the Post module, and used in various other modules
//
//(c) Copyright IBM Corporation. 2018
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell
//
//  Created Nov 2018
//
//  Change Log:
//***************************************************************************************************************



case class Ledger (
                   // Element      Data Type
                   var ldgrIPID: String,
                   var ldgrContractID: String,
                   var ldgrCommitmentID: String,
                   var ldgrLdgrlID: String, // a surrogate ID assigned to unique combinations of all other attributes - amounts
//                   var ldgrJrnlLineID: String,  //  commented out fields are not passed from journal to ledger
//                   var ldgrBusinessEventCode: String, //  commented out fields are not passed from journal to ledger
                   var ldgrSourceSystemID: String,
//                   var ldgrOriginalDocID: String, //  commented out fields are not passed from journal to ledger
//                   var ldgrJrnlDescript: String, //  commented out fields are not passed from journal to ledger
                   var ldgrLedgerID: String,
                   var ldgrJrnlType: String,
                   var ldgrBookCodeID: String,
                   var ldgrLegalEntityID: String,
                   var ldgrCenterID: String,
                   var ldgrProjectID: String,
                   var ldgrProductID: String,
                   var ldgrNominalAccountID: String,
                   var ldgrAltAccountID: String,
                   var ldgrCurrencyCodeSourceID: String,
                   var ldgrCurrencyTypeCodeSourceID: String,
                   var ldgrCurrencyCodeTargetID: String,
                   var ldgrCurrencyTypeCodeTargetID: String,
                   var ldgrLedgerPeriod: String, // this fields populated with appropriate field from journal, any of the following
//                   var ldgrFiscalPeriod: String, //  Date to be posted chosen by posting program
//                   var ldgrAcctDate: String, //  Date to be posted chosen by posting program
//                   var ldgrTransDate: String, //  Date to be posted chosen by posting program
                   var ldgrTransAmount: BigDecimal,  // accumulated amount from journals 20th element
                   var ldgrUnitOfMeasure: String = " ",
//                 var   ldgrUnitPrice: BigDecimal, //  commented out fields are not passed from journal to ledger
                   var ldgrStatisticAmount: BigDecimal,  // accumulated amount from journals 22nd element
//                 var   tranRuleSetID: String = " ", //  commented out fields are not passed from journal to ledger
//                 var   ldgrRuleID: String = " ", //  commented out fields are not passed from journal to ledger
                   var ldgrDirVsOffsetFlg: String,
                   var ldgrReconcileFlg: String,
                   var ldgrAdjustFlg: String,
                   var ldgrExtensionIDAuditTrail: String = " ",  // The following are not accumulated from Journals
                   var ldgrExtensionIDSource: String = " ",      //  rather they are assigned to specific ledger rows
                   var ldgrExtensionIDClass: String = " ",       //  see above
                   var ldgrExtensionIDDates: String = " ",       //  see above
                   var ldgrExtensionIDCustom: String = " ")      //  see above  30th element







