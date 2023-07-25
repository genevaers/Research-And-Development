       IDENTIFICATION DIVISION.
       PROGRAM-ID. TESTUR70.
      ******************************************************************
      *
      * (c) Copyright IBM Corporation 2023.
      *     Copyright Contributors to the GenevaERS Project.
      * SPDX-License-Identifier: Apache-2.0
      *
      ******************************************************************
      *
      *   Licensed under the Apache License, Version 2.0 (the "License");
      *   you may not use this file except in compliance with the License.
      *   You may obtain a copy of the License at
      *
      *     http://www.apache.org/licenses/LICENSE-2.0
      *
      *   Unless required by applicable law or agreed to in writing,
      *   software distributed under the License is distributed on an 
      *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
      *    either express or implied.
      *   See the License for the specific language governing
      *   permissions and limitations under the License.
      ******************************************************************
      **                                                               *
      ** DESCRIPTION: COBOL TEST PROGRAM FOR GVBUR70                   *
      **                                                               *
      ** MODULES CALLED: GVBTP90 - I/O HANDLER                         *
      **                 GVBUR70 - ASM/3GL/NATURAL TO JAVA INTERFACE   *
      **                                                               *
      ** INPUT FILES:   D001                                           *
      **                                                               *
      ** OUTPUT FILES:  D001                                           *
      **                                                               *
      ** REPORTS:       NONE                                           *
      **                                                               *
      ** RETURN CDS:  0000 - SUCCESSFUL PROCESSING                     *
      **              0004 - WARNING                                   *
      **              0008 - ERROR                                     *
      **              0016 - CATASTROPHIC FAILURE                      *
      **                                                               *
      ******************************************************************
      **                ISSUANCE INFORMATION                           *
      ******************************************************************
      ** VERSION  PROJECT  ANALYST  EFF        REASON                  *
      **                                                               *
      ** 418  RTC22964    BEESLEY    02/02/2023 INITIAL                *
      **                                                               *
      ******************************************************************
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       DATA DIVISION.
       FILE SECTION.
      *
       WORKING-STORAGE SECTION.
      *
       01  WS-DISPLAY-MASK-1      PIC ZZ,ZZZ,ZZZ,ZZ9.
      *
       01  WS-ABEND-CD            PIC X(4) VALUE '0016'.
      *
       01 UR70-PARAMETER-AREA.
          05  UR70-FUNCTION               PIC X(8).
          05  UR70-OPTION                 PIC X(8).
          05  UR70-OPTIONS REDEFINES UR70-OPTION.
             10 UR70-OPTION1              PIC S9(4) USAGE IS BINARY.
          05  UR70-CLASS                  PIC X(32).
          05  UR70-METHOD                 PIC X(32).
          05  UR70-LEN-SEND               PIC S9(9) USAGE IS BINARY.
          05  UR70-LEN-RECV               PIC S9(9) USAGE IS BINARY.
          05  UR70-LEN-RETN               PIC S9(9) USAGE IS BINARY.
          05  UR70-RETC                   PIC S9(9) USAGE IS BINARY.
          05  UR70-ANCHOR                 POINTER.
          05  UR70-SPARE                  PIC X(4).
      *
       01 GVBUR70                         PIC X(8)  VALUE 'GVBUR70'.
      *
       01  UR70-RETURN-CODES.
           05  UR70-VALUE-SUCCESSFUL      PIC S9(9) COMP VALUE ZEROES.

       01  UR70-SEND-AREA.
           05  UR70-A80-SEND-AREA         PIC  X(80)      VALUE SPACES.

       01  UR70-RECV-AREA.
           05  UR70-A80-RECV-AREA         PIC  X(80)      VALUE SPACES.

      *****************************************************************
      *                                                               *
      *  COMMAREA FOR SUBROUTINE FIFIX   - VSAM/QSAM I/O HANDLER.     *
      *                                                               *
      *  FUNCTION CODES:                                              *
      *                                                               *
      *        OP  - OPEN                                             *
      *        CL  - CLOSE                                            *
      *        IN  - FILE INFORMATION                                 *
      *        SB  - START BROWSE                                     *
      *        LO  - LOCATE                                           *
      *        RD  - READ RECORD                                      *
      *        BR  - READ NEXT                                        *
      *        WR  - WRITE RECORD                                     *
      *        UP  - UPDATE RECORD                                    *
      *        DL  - DELETE RECORD                                    *
      *        RI  - RELEASE HELD RECORD                              *
      *                                                               *
      *  RETURN CODES:                                                *
      *                                                               *
      *        0   - SUCCESSFUL                                       *
      *        1   - NOT FOUND                                        *
      *        2   - END-OF-FILE                                      *
      *        B   - BAD PARAMETER                                    *
      *        E   - I/O ERROR                                        *
      *        L   - LOGIC ERROR                                      *
      *                                                               *
      *****************************************************************

       01  FIFIX-RECORD-AREA.
           05  FIFIX-FB-RECORD-AREA     PIC  X(80)      VALUE SPACES.

       01  FIFIX-RECORD-KEY             PIC  X(15).

       01  FIFIX-INFO-RETURN-DATA.
           05  FIFIX-KEY-OFFSET         PIC  S9(08) COMP VALUE ZEROES.
           05  FIFIX-KEY-LENGTH         PIC  S9(08) COMP VALUE ZEROES.
           05  FIFIX-MAX-RECLEN         PIC  S9(08) COMP VALUE ZEROES.
           05  FIFIX-NUM-RECORDS        PIC  S9(08) COMP VALUE ZEROES.
           05  FILLER                   PIC  X(4230)     VALUE SPACES.

       01  FIFIX-INPUT-RECORD-LENGTH    PIC  S9(04) COMP VALUE ZEROES.
       01  FIFIX-FILE-RECORD-LENGTH     PIC  S9(04) COMP VALUE ZEROES.
       01  FIFIX-INPUT-RECORD-FMT       PIC  X(01)       VALUE SPACES.

       01  FIFIX-MAX-FB-RECORD-LENGTH PIC    S9(04) COMP VALUE +4240.
       01  FIFIX-MAX-VB-RECORD-LENGTH PIC    S9(04) COMP VALUE +4244.

       01  FIFIX-FUNCTION-CODES.
           05  FIFIX-VALUE-CLOSE          PIC  X(02) VALUE 'CL'.
           05  FIFIX-VALUE-DELETE         PIC  X(02) VALUE 'DL'.
           05  FIFIX-VALUE-INFO           PIC  X(02) VALUE 'IN'.
           05  FIFIX-VALUE-LOCATE         PIC  X(02) VALUE 'LO'.
           05  FIFIX-VALUE-OPEN           PIC  X(02) VALUE 'OP'.
           05  FIFIX-VALUE-READ           PIC  X(02) VALUE 'RD'.
           05  FIFIX-VALUE-READNEXT       PIC  X(02) VALUE 'BR'.
           05  FIFIX-VALUE-START-BROWSE PIC    X(02) VALUE 'SB'.
           05  FIFIX-VALUE-UPDATE         PIC  X(02) VALUE 'UP'.
           05  FIFIX-VALUE-WRITE          PIC  X(02) VALUE 'WR'.
           05  FIFIX-VALUE-RELEASE        PIC  X(02) VALUE 'RI'.

       01  FIFIX-FILE-TYPES.
           05  FIFIX-VALUE-SEQUENTIAL     PIC  X(01) VALUE 'S'.
           05  FIFIX-VALUE-VSAM           PIC  X(01) VALUE 'V'.

       01  FIFIX-FILE-MODES.
           05  FIFIX-VALUE-INPUT          PIC  X(02) VALUE 'I '.
           05  FIFIX-VALUE-OUTPUT         PIC  X(02) VALUE 'O '.
           05  FIFIX-VALUE-IO             PIC  X(02) VALUE 'IO'.
           05  FIFIX-VALUE-EXTEND         PIC  X(02) VALUE 'EX'.

       01  FIFIX-RETURN-CODES.
           05  FIFIX-VALUE-SUCCESSFUL     PIC  X(01) VALUE '0'.
           05  FIFIX-VALUE-NOT-FOUND      PIC  X(01) VALUE '1'.
           05  FIFIX-VALUE-END-OF-FILE    PIC  X(01) VALUE '2'.
           05  FIFIX-VALUE-BAD-PARAMETER  PIC  X(01) VALUE 'B'.
           05  FIFIX-VALUE-IO-ERROR       PIC  X(01) VALUE 'E'.
           05  FIFIX-VALUE-LOGIC-ERROR    PIC  X(01) VALUE 'L'.

       01  FIFIX-RECORD-FORMATS.
           05  FIFIX-VALUE-FIXED-LEN      PIC X(01) VALUE 'F'.
           05  FIFIX-VALUE-VARIABLE-LEN   PIC X(01) VALUE 'V'.
      *
       01 WS-WORK-AREA-LNGTH              PIC S9(08) COMP.
       01 FIFIX                           PIC X(8) VALUE 'GVBTP90 '.
      *
       01  FIFIX-PARAMETER-AREA.
           05  FIFIX-ANCHOR             POINTER.
           05  FIFIX-DDNAME               PIC  X(08).
           05  FIFIX-FUNCTION-CODE        PIC  X(02).
           05  FIFIX-FILE-TYPE            PIC  X(01).
           05  FIFIX-FILE-MODE            PIC  X(02).
           05  FIFIX-RETURN-CODE          PIC  X(01).
           05  FIFIX-VSAM-RETURN-CODE     PIC S9(04)  COMP.
           05  FIFIX-RECORD-LENGTH        PIC S9(04)  COMP.
           05  FIFIX-RECFM                PIC  X(01).
           05  FIFIX-ESDS                 PIC  X(01).
      *
      *
       PROCEDURE DIVISION.
      *
       000-MAIN.
      *
      ******************************************************************
      * MAINLINE                                                       *
      ******************************************************************
      *
           DISPLAY 'TESTUR70 STARTING'
      *
      *    PERFORM 200-ASSIGN-RECORD        THRU 200-EXIT
      *
           PERFORM 102-SPEC-NUMBER-SUBTASK  THRU 102-EXIT
      *
           PERFORM 104-CALL-CLASS-METHOD    THRU 104-EXIT
      *
           PERFORM 110-OPEN-FILE            THRU 110-EXIT
      *
      *    PERFORM 300-WRITE-RECORD         THRU 300-EXIT
           PERFORM 400-READ-RECORD          THRU 400-EXIT
      *
           PERFORM 120-CLOSE-FILE           THRU 120-EXIT
      *
           .
       000-GOBACK.
           GOBACK.


      ******************************************************************
      * SPECIFY NUMBER SUB TASKS                                       *
      ******************************************************************
       102-SPEC-NUMBER-SUBTASK.

           DISPLAY 'TESTUR70 SETTING SUBTASK(S)'

           SET  UR70-ANCHOR               TO NULL
           MOVE 'INIT    '                TO UR70-FUNCTION
           MOVE +1                        TO UR70-OPTION1
           MOVE +0                        TO UR70-RETC

           CALL GVBUR70    USING UR70-PARAMETER-AREA,
                                 UR70-SEND-AREA,
                                 UR70-RECV-AREA.

           IF UR70-RETC   NOT = UR70-VALUE-SUCCESSFUL
              DISPLAY 'UR70:'
                      ', SET SUBTASKS FAILED, '
                      ' RET CD = ', UR70-RETC
                      ' FUNCTION = ', UR70-FUNCTION
           END-IF.

       102-EXIT.
           EXIT.

      ******************************************************************
      * CALL JAVA CLASS METHOD                                         *
      ******************************************************************
       104-CALL-CLASS-METHOD.

           DISPLAY 'TESTUR70 CALLING CLASS|METHOD'

           MOVE 'CALL    '                         TO UR70-FUNCTION
           MOVE SPACES                             TO UR70-OPTION
           MOVE 'MyClass                         ' TO UR70-CLASS
           MOVE 'Method1                         ' TO UR70-METHOD
           MOVE +10                                TO UR70-LEN-SEND
           MOVE +22                                TO UR70-LEN-RECV
           MOVE +0                                 TO UR70-RETC
*
           MOVE '0123456789'       TO UR70-A80-SEND-AREA

           CALL GVBUR70    USING UR70-PARAMETER-AREA,
                                 UR70-SEND-AREA,
                                 UR70-RECV-AREA.

           IF UR70-RETC   NOT = UR70-VALUE-SUCCESSFUL
              DISPLAY 'UR70:'
                      ', CALL CLASS METHOD FAILED, '
                      ' RET CD = ', UR70-RETC
                      ' FUNCTION = ', UR70-FUNCTION
           ELSE
              DISPLAY 'RECV:' UR70-A80-RECV-AREA
           END-IF.

       104-EXIT.
           EXIT.

      ******************************************************************
      * OPEN FILE.                                                     *
      ******************************************************************
       110-OPEN-FILE.

           SET  FIFIX-ANCHOR              TO NULL
           MOVE 'D001    '                TO FIFIX-DDNAME
           MOVE FIFIX-VALUE-OPEN          TO FIFIX-FUNCTION-CODE
           MOVE FIFIX-VALUE-VSAM          TO FIFIX-FILE-TYPE
      *    MOVE FIFIX-VALUE-OUTPUT        TO FIFIX-FILE-MODE
      *    MOVE FIFIX-VALUE-EXTEND        TO FIFIX-FILE-MODE
           MOVE FIFIX-VALUE-IO            TO FIFIX-FILE-MODE
           MOVE SPACES                    TO FIFIX-RETURN-CODE
           MOVE +0                        TO FIFIX-VSAM-RETURN-CODE
           MOVE +0                        TO FIFIX-RECORD-LENGTH
           MOVE SPACES                    TO FIFIX-RECFM

           MOVE SPACES                    TO FIFIX-RECORD-KEY

           CALL FIFIX      USING FIFIX-PARAMETER-AREA,
                                 FIFIX-RECORD-AREA,
                                 FIFIX-RECORD-KEY

           IF FIFIX-RETURN-CODE NOT = FIFIX-VALUE-SUCCESSFUL
              DISPLAY 'MTEST: DD:  D001'
                      ', FIFIX FAILED, '
                      ' RET CD = ', FIFIX-RETURN-CODE
                      ' FUNCTION = ', FIFIX-FUNCTION-CODE
                      ' DDNAME = ', FIFIX-DDNAME
              DISPLAY ' TYPE   = ', FIFIX-FILE-TYPE
                      ' MODE   = ', FIFIX-FILE-MODE
                      ' REASON = ', FIFIX-VSAM-RETURN-CODE
           ELSE
              DISPLAY 'DATASET OPENED: ' FIFIX-DDNAME
           END-IF.

       110-EXIT.
           EXIT.


      ******************************************************************
      * CLOSE FILE.                                                    *
      ******************************************************************
       120-CLOSE-FILE.

           MOVE 'D001    '                TO FIFIX-DDNAME
           MOVE FIFIX-VALUE-CLOSE         TO FIFIX-FUNCTION-CODE
           MOVE FIFIX-VALUE-VSAM          TO FIFIX-FILE-TYPE
      *    MOVE FIFIX-VALUE-OUTPUT        TO FIFIX-FILE-MODE
      *    MOVE FIFIX-VALUE-EXTEND        TO FIFIX-FILE-MODE
           MOVE FIFIX-VALUE-IO            TO FIFIX-FILE-MODE
           MOVE SPACES                    TO FIFIX-RETURN-CODE
           MOVE +0                        TO FIFIX-VSAM-RETURN-CODE
           MOVE +0                        TO FIFIX-RECORD-LENGTH
           MOVE SPACES                    TO FIFIX-RECFM

           MOVE SPACES                    TO FIFIX-RECORD-KEY

           CALL FIFIX      USING FIFIX-PARAMETER-AREA,
                                 FIFIX-RECORD-AREA,
                                 FIFIX-RECORD-KEY

           IF FIFIX-RETURN-CODE NOT = FIFIX-VALUE-SUCCESSFUL
              DISPLAY 'MTEST: DD: D001'
                      ', FIFIX FAILED, '
                      ' RET CD = ', FIFIX-RETURN-CODE
                      ' FUNCTION = ', FIFIX-FUNCTION-CODE
                      ' DDNAME = ', FIFIX-DDNAME
              DISPLAY ' TYPE   = ', FIFIX-FILE-TYPE
                      ' MODE   = ', FIFIX-FILE-MODE
                      ' REASON = ', FIFIX-VSAM-RETURN-CODE
           ELSE
              DISPLAY 'DATASET CLOSED: ' FIFIX-DDNAME
           END-IF

           SET  FIFIX-ANCHOR              TO NULL

           .
       120-EXIT.
           EXIT.
      *
      *
      ******************************************************************
      * ASSIGN RECORD.                                                 *
      ******************************************************************
       200-ASSIGN-RECORD.
      *
      *
           MOVE '00000004ABCDEFGHIJKLMNOPQRSTUVWXYZ'
                              TO FIFIX-RECORD-AREA
      *    MOVE '01234567'    TO FIFIX-RECORD-KEY
           .

       200-EXIT.
           EXIT.
      *
      *
      ******************************************************************
      * WRITE RECORD.                                                  *
      ******************************************************************
       300-WRITE-RECORD.

           MOVE 'D001    '                TO FIFIX-DDNAME
           MOVE FIFIX-VALUE-WRITE         TO FIFIX-FUNCTION-CODE
           MOVE FIFIX-VALUE-VSAM          TO FIFIX-FILE-TYPE
      *    MOVE FIFIX-VALUE-OUTPUT        TO FIFIX-FILE-MODE
           MOVE FIFIX-VALUE-EXTEND        TO FIFIX-FILE-MODE
      *    MOVE FIFIX-VALUE-IO            TO FIFIX-FILE-MODE
           MOVE SPACES                    TO FIFIX-RETURN-CODE
           MOVE +0                        TO FIFIX-VSAM-RETURN-CODE
           MOVE +22                       TO FIFIX-RECORD-LENGTH
           MOVE FIFIX-VALUE-FIXED-LEN     TO FIFIX-RECFM
      *    MOVE 'D'                       TO FIFIX-ESDS

           MOVE SPACES                    TO FIFIX-RECORD-KEY
           MOVE '000000000000010@@@@@@@'
                                          TO FIFIX-FB-RECORD-AREA

           DISPLAY 'ABOUT TO WRITE RECORD USING: ' FIFIX
      *    DISPLAY 'PARMS: ' FUNCTION HEX-OF(FIFIX-ANCHOR)
           DISPLAY 'PARMS: ' FIFIX-PARAMETER-AREA
           DISPLAY 'RECRD: ' FIFIX-RECORD-AREA(1:32)
      *
           CALL FIFIX      USING FIFIX-PARAMETER-AREA,
                                 FIFIX-RECORD-AREA,
                                 FIFIX-RECORD-KEY

           DISPLAY 'WRITTEN WRITE RECORD'
      *    DISPLAY 'PARMS: ' FUNCTION HEX-OF(FIFIX-ANCHOR)
           DISPLAY 'PARMS: ' FIFIX-PARAMETER-AREA
      *
           IF FIFIX-RETURN-CODE NOT = FIFIX-VALUE-SUCCESSFUL
              DISPLAY 'MTEST DD: D001'
                      ', FIFIX FAILED, '
                      ' RET CD = ', FIFIX-RETURN-CODE
                      ' FUNCTION = ', FIFIX-FUNCTION-CODE
              DISPLAY ' DDNAME = ', FIFIX-DDNAME
                      ' TYPE   = ', FIFIX-FILE-TYPE
                      ' LRECL  = ', FIFIX-RECORD-LENGTH
                      ' MODE   = ', FIFIX-FILE-MODE
                      ' RECFM  = ', FIFIX-RECFM
                      ' REASON = ', FIFIX-VSAM-RETURN-CODE
                      ' ESDS   = ', FIFIX-ESDS
           ELSE
              DISPLAY 'RECORD WRITTEN: ' FIFIX-FB-RECORD-AREA(1:64)
           END-IF.

       300-EXIT.
           EXIT.
      *
      *
      ******************************************************************
      * READ RECORD.                                                   *
      ******************************************************************
       400-READ-RECORD.

           MOVE 'D001    '                TO FIFIX-DDNAME
           MOVE FIFIX-VALUE-READ          TO FIFIX-FUNCTION-CODE
           MOVE FIFIX-VALUE-VSAM          TO FIFIX-FILE-TYPE
      *    MOVE FIFIX-VALUE-OUTPUT        TO FIFIX-FILE-MODE
           MOVE FIFIX-VALUE-IO            TO FIFIX-FILE-MODE
           MOVE SPACES                    TO FIFIX-RETURN-CODE
           MOVE +0                        TO FIFIX-VSAM-RETURN-CODE
           MOVE +22                       TO FIFIX-RECORD-LENGTH
           MOVE FIFIX-VALUE-FIXED-LEN     TO FIFIX-RECFM

           MOVE '000000000000002'         TO FIFIX-RECORD-KEY
           MOVE SPACES
                                          TO FIFIX-FB-RECORD-AREA

           CALL FIFIX      USING FIFIX-PARAMETER-AREA,
                                 FIFIX-RECORD-AREA,
                                 FIFIX-RECORD-KEY

           IF FIFIX-RETURN-CODE NOT = FIFIX-VALUE-SUCCESSFUL
              DISPLAY 'MTEST: DD: D001'
                      ', FIFIX FAILED, '
                      ' RET CD = ', FIFIX-RETURN-CODE
                      ' FUNCTION = ', FIFIX-FUNCTION-CODE
              DISPLAY ' DDNAME = ', FIFIX-DDNAME
                      ' TYPE   = ', FIFIX-FILE-TYPE
                      ' LRECL  = ', FIFIX-RECORD-LENGTH
                      ' MODE   = ', FIFIX-FILE-MODE
                      ' RECFM  = ', FIFIX-RECFM
                      ' REASON = ', FIFIX-VSAM-RETURN-CODE
                      ' ESDS   = ', FIFIX-ESDS
              DISPLAY ' KEY    = ', FIFIX-RECORD-KEY
           ELSE
              DISPLAY 'RECORD READ: ' FIFIX-FB-RECORD-AREA(1:64)
           END-IF.

       400-EXIT.
           EXIT.
