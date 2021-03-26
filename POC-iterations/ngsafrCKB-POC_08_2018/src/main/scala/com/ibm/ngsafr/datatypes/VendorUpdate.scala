package com.ibm.ngsafr.datatypes

// ngsafr Common Key Buffer in Scala POC
// (c) Copyright IBM Corporation. 2018
// SPDX-License-Identifier: Apache-2.0
// Written by: Sibasis Das

case class VendorUpdate(instinstID: String, // "0",               Instrument ID
                   instHolderName: String, // "Vendor Name",     Name of Instrument Holder
                   instTypeID: String,  // "EXP",             Vendor (EXP) or REV Reco
                   instUpdateDate: String)// "0000-00-00",      Record Effective Start Date


