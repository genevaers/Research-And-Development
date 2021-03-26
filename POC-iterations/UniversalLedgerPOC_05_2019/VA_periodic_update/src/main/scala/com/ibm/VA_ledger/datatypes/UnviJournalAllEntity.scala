package com.ibm.VA_ledger.datatypes

//***************************************************************************************************************
//
//  Structure:  UnviJournalAllEntity.scala - the journal entry structure
//
//  This structure is produced by the transStandardize module, and posted to ledger by the Post module
//
//(c) Copyright IBM Corporation. 2019
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell
//
//  Created August 2019
//
//  Change Log:
//***************************************************************************************************************


case class UnviJournalAllEntity (
                                    //Universal Journal
                                    var ujIPID: String,
                                    var ujInstrumentID: String,
                                    var ujCommitmentID: String,
                                    var ujJrnlID: String,
                                    var ujJrnlLineID: String,
                                    var ujBusEventCD: String,
                                    var ulSourceID: String,
                                    var ujProcessID: String,
                                    var ujOriginalDocID: String,
                                    var ujJrnlDescript: String,
                                    ujLedgerID: String      = "ACTUALS",
                                    ujJrnlType: String      = "FIN",
                                    ujBookCodeID: String    = "SHRD-3RD-PARTY",
                                    var ujLegalEntityID: String,
                                    var ujCenterID: String,
                                    var ujAffliateEntityID: String,
                                    var ujAffliateCenterID: String,
                                    var ujProjectID: String,
                                    ujProductID: String  = "0",
                                    var ujAccountID: String,
                                    ujAltAccountID: String                 = "0",
                                    ujCurrencyCodeSourceID: String         = "USD",
                                    ujCurrencyTypeCodeSourceID: String     = "TXN",
                                    ujCurrencyCodeTargetID: String         = "USD",
                                    ujCurrencyTypeCodeTargetID: String     = "BASE-LE",
                                    var ujFiscalPeriod: String,
                                    var ujAcctDate: String,
                                    var ujTransDate: String,
                                    var ujMovementFlg: Boolean,
                                    var ujDirVsOffsetFlg: Boolean,
                                    ujReconcileFlg: Boolean = false,
                                    var ujAdjustFlg: Boolean,
                                    var ujUnitOfMeasure: String,
                                    var ujUnitPrice: BigDecimal,
                                    var ujTransAmount: BigDecimal,
                                    var ujStatisticAmount: BigDecimal,
                                    ujRuleSetID: String = " ",
                                    ujRuleID: String = " ",
                                    ujExtensionIDSource: String,
                                    ujExtnesionSourceType: String,
                                    ujExtensionIDAuditTrail: String = " ",
                                    ujExtensionIDClass: String = " ",
                                    ujExtensionIDDates: String = " ",
                                    ujExtensionIDCustom: String = " ",

                                    //Universal Header
                                    var uhIPID: String,
                                    var uhInstrumentID: String,
                                    var uhCommitmentID: String,
                                    var uhHeaderID: String,
                                    var uhEntryDate: String,
                                    var uhCreateDate: String,
                                    uhUpdateDate: String = " ",
                                    uhPostedDate: String = " ",
                                    uhApproverID: String = " ",
                                    uhCreatorID: String = " ",
                                    uhCreditHashTotal: BigDecimal = 0,
                                    uhDebitHashTotal: BigDecimal = 0,

                                    //Commitment
                                    var ucIPID: String,
                                    var ucInstrumentID: String,
                                    var ucCommitmentID: String,
                                    var ucContractType: String,
                                    var ucContractStattDate: String,

                                    //Instrument/Contract
                                    var uiIPID: String,
                                    var uiInstrumentID: String,
                                    var uiInstrumentType: String,
                                    var uiInstrumentStattDate: String,

                                    //Invovled Party
                                    var ipIPID: String,
                                    var ipName: String,
                                    var ipAddress: String,
                                    var ipCity: String,
                                    var ipState: String,
                                    var ipCountry: String,
                                    var ipPostalCode: String,
                                    var ipEmail: String,
                                    var ipExternalID: String,
                                    ipSourceSystemID: String = " ",
                                    ipUnintID: String = " ",
                                    ipDistrictID: String = " ",


                                    //Trading Partners
                                    var tpIPID: String,
                                    var tpRelatedIPID: String,

                                    //Universal Journal Extension
                                    //Original PO Transaction
                                    var ujepoIPID: String,
                                    var ujepoInstrumentID: String,
                                    var ujepoCommitmentID: String,
                                    var ujepoInstID: String,
                                    var ujepoJrnlID: String,
                                    var ujepoJrnlLineID: String,
                                    var ujepoExtensionIDSource: String,
                                    var ujepoPONumber: String,
                                    var ujepoAgency: String,
                                    var ujepoOrderedDate: String,
                                    var ujepoVendorCommodityDesc: String,
                                    var ujepoQuantityOrdered: BigDecimal,
                                    var ujepoPrice: BigDecimal,
                                    var ujepoVendorID: String,
                                    var ujepoVendorName: String,
                                    var ujepoVendorAddress: String,
                                    var ujepoVendorCity: String,
                                    var ujepoVendorState: String,
                                    var ujepoVendorPostalCode: String,
                                    var ujepoVendorLocEmailAddress: String,
                                    var ujepoNIGPcode: String,
                                    var ujepoNIGPDescription: String,
                                    var ujepoUnitOfMeasureCode: String,
                                    var ujepoUnitOfMeasureDesc: String,
                                    var VendorPartNumber: String,
                                    var ujepoManPartNumber: String,

                                    //Original Payment Tran
                                    var ujepyIPID: String,
                                    var ujepyInstrumentID: String,
                                    var ujepyCommitmentID: String,
                                    var ujepyJrnlID: String,
                                    var ujepyJrnlLineID: String,
                                    var ujepyExtensionIDSource: String,
                                    var ujepyAgencyKey: String,
                                    var ujepyFundDetailKey: String,
                                    var ujepyObjectKey: String,
                                    var ujepySubProgramKey: String,
                                    var ujepyVendorName: String,
                                    var ujepyAmount: BigDecimal)

