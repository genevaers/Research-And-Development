      *****************************************************************
      **
      ** (c) Copyright IBM Corporation 2024.
      **     Copyright Contributors to the GenevaERS Project.
      ** SPDX-License-Identifier: Apache-2.0
      **
      *****************************************************************
      **
      **   Licensed under the Apache License,
      **   Version 2.0 (the "License");
      **   you may not use this file except in compliance with the
      **   License.
      **   You may obtain a copy of the License at
      **
      **     http://www.apache.org/licenses/LICENSE-2.0
      **
      **   Unless required by applicable law or agreed to in writing,
      **   software distributed under the License is distributed on an           
      **   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
      **   either express or implied.
      **   See the License for the specific language governing
      **   permissions and limitations under the License.
      *****************************************************************
      *
      *     CALL GVBTP90    USING TP90-PARAMETER-AREA,
      *                           TP90-RECORD-AREA,
      *                           TP90-RECORD-KEY
      *
      *****************************************************************
      *  GVBTP90 - I/O COMMUNICATION WITH OPERATING SYSTEM
      *****************************************************************
      *
       01  TP90-PARAMETER-AREA.
           05  TP90-ANCHOR                 POINTER.
           05  TP90-DDNAME                 PIC  X(08).
           05  TP90-FUNCTION-CODE          PIC  X(02).
           05  TP90-FILE-TYPE              PIC  X(01).
           05  TP90-FILE-MODE              PIC  X(02).
           05  TP90-RETURN-CODE            PIC  X(01).
           05  TP90-VSAM-RETURN-CODE       PIC S9(04)  COMP.
           05  TP90-RECORD-LENGTH          PIC S9(04)  COMP.
           05  TP90-RECFM                  PIC  X(01).
           05  TP90-ESDS                   PIC  X(01).
      *
      *****************************************************************
      *  GVBTP90 - CONSTANTS
      *****************************************************************
      *
       01  GVBTP90-FUNCTION-CODES.
           05  GVBTP90-VALUE-CLOSE         PIC X(02) VALUE 'CL'.
           05  GVBTP90-VALUE-DELETE        PIC X(02) VALUE 'DL'.
           05  GVBTP90-VALUE-INFO          PIC X(02) VALUE 'IN'.
           05  GVBTP90-VALUE-LOCATE        PIC X(02) VALUE 'LO'.
           05  GVBTP90-VALUE-OPEN          PIC X(02) VALUE 'OP'.
           05  GVBTP90-VALUE-READ          PIC X(02) VALUE 'RD'.
           05  GVBTP90-VALUE-READNEXT      PIC X(02) VALUE 'BR'.
           05  GVBTP90-VALUE-START-BROWSE  PIC X(02) VALUE 'SB'.
           05  GVBTP90-VALUE-UPDATE        PIC X(02) VALUE 'UP'.
           05  GVBTP90-VALUE-WRITE         PIC X(02) VALUE 'WR'.
           05  GVBTP90-VALUE-RELEASE       PIC X(02) VALUE 'RI'.
      *
       01  GVBTP90-FILE-TYPES.
           05  GVBTP90-VALUE-SEQUENTIAL    PIC X(01) VALUE 'S'.
           05  GVBTP90-VALUE-VSAM          PIC X(01) VALUE 'V'.
      *
       01  GVBTP90-FILE-MODES.
           05  GVBTP90-VALUE-INPUT         PIC X(02) VALUE 'I '.
           05  GVBTP90-VALUE-OUTPUT        PIC X(02) VALUE 'O '.
           05  GVBTP90-VALUE-IO            PIC X(02) VALUE 'IO'.
           05  GVBTP90-VALUE-EXTEND        PIC X(02) VALUE 'EX'.
      *
       01  GVBTP90-RETURN-CODES.
           05  GVBTP90-VALUE-SUCCESSFUL    PIC X(01) VALUE '0'.
           05  GVBTP90-VALUE-NOT-FOUND     PIC X(01) VALUE '1'.
           05  GVBTP90-VALUE-END-OF-FILE   PIC X(01) VALUE '2'.
           05  GVBTP90-VALUE-BAD-PARAMETER PIC X(01) VALUE 'B'.
           05  GVBTP90-VALUE-IO-ERROR      PIC X(01) VALUE 'E'.
           05  GVBTP90-VALUE-LOGIC-ERROR   PIC X(01) VALUE 'L'.
      *
       01  GVBTP90-RECORD-FORMATS.
           05  GVBTP90-VALUE-FIXED-LEN     PIC X(01) VALUE 'F'.
           05  GVBTP90-VALUE-VARIABLE-LEN  PIC X(01) VALUE 'V'.
      *
      *****************************************************************
      *
       01  GVBTP90-INFO-RETURN-DATA.
           05  GVBTP90-KEY-OFFSET       PIC S9(08) COMP VALUE ZEROES.
           05  GVBTP90-KEY-LENGTH       PIC S9(08) COMP VALUE ZEROES.
           05  GVBTP90-MAX-RECLEN       PIC S9(08) COMP VALUE ZEROES.
           05  GVBTP90-NUM-RECORDS      PIC S9(08) COMP VALUE ZEROES.
           05  FILLER                   PIC X(4230)     VALUE SPACES.
      *
