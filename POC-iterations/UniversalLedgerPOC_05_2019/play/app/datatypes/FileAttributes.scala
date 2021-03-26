package datatypes
// (c) Copyright IBM Corporation. 2019
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell

// Used in listing files within the upload directory.

case class FileAttributes (
       fileName: String        ,
       creationTime: String    ,
       isDirectory: Boolean    ,
       isOther: Boolean        ,
       isRegularFile: Boolean  ,
       isSymbolicLink: Boolean ,
       lastAccessTime: String  ,
       lastModifiedTime: String,
       size: Long
       )