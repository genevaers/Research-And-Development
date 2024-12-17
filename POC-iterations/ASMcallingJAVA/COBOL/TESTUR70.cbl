       IDENTIFICATION DIVISION.
       PROGRAM-ID. TESTUR70.
      *****************************************************************
      **
      ** (c) Copyright IBM Corporation 2024.
      ** Copyright Contributors to the GenevaERS Project.
      ** SPDX-License-Identifier: Apache-2.0
      **
      *****************************************************************
      **
      **  Licensed under the Apache License, Version 2.0
      **  (the "License");
      **  you may not use this file except in compliance with the
      **  License.
      **  You may obtain a copy of the License at
      **
      **     http://www.apache.org/licenses/LICENSE-2.0
      **
      **   Unless required by applicable law or agreed to in writing,
      **   software distributed under the License is distributed on an
      **   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
      **   either express or implied.
      **   See the License for the specific language governing
      **   permissions and limitations under the License.
      **
      ******************************************************************
      **  GVBUR70 IVP COBOL PROGRAM TESTUR70 TO CALL JAVA CLASS METHOD
      ******************************************************************
      **
      ** DESCRIPTION: MAKES CALLS TO GVBUR70:
      **
      **              INIT: initializes the interface and specifies the
      **                    required number of threads (one for COBOL);
      **
      **              CALL: Invokes the spefified Java class and method
      **                    providing a SEND and RECEIVE buffer.
      **
      ** MODULES CALLED: GVBUR70 - ASM/3GL/4GL TO JAVA INTERFACE
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
          05  UR70-VERSION                PIC S9(4) USAGE IS BINARY.
          05  UR70-FLAG1                  PIC X(1).
          05  UR70-FLAG2                  PIC X(1).
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
          05  UR70-JRETC                  PIC S9(9) USAGE IS BINARY.
          05  UR70-LREQD                  PIC S9(9) USAGE IS BINARY. 
      *
       01 GVBUR70                         PIC X(8)  VALUE 'GVBUR70'.
      *
       01  UR70-RETURN-CODES.
           05  UR70-VALUE-SUCCESSFUL      PIC S9(9) COMP VALUE ZEROES.

       01  UR70-SEND-AREA.
           05  UR70-A80-SEND-AREA         PIC  X(80)      VALUE SPACES.

       01  UR70-RECV-AREA.
           05  UR70-A80-RECV-AREA         PIC  X(80)      VALUE SPACES.

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
           PERFORM 102-INIT-NUMBER-SUBTASK  THRU 102-EXIT
      *
           PERFORM 104-CALL-CLASS-METHOD    THRU 104-EXIT
      *
           DISPLAY 'TESTUR70 ENDING'
                  .
       000-GOBACK.
           GOBACK.


      ******************************************************************
      * SPECIFY NUMBER SUB TASKS                                       *
      ******************************************************************
       102-INIT-NUMBER-SUBTASK.

           DISPLAY 'TESTUR70 SETTING SUBTASK(S)'

           SET  UR70-ANCHOR               TO NULL
           MOVE +1                        TO UR70-VERSION
           MOVE 'U'                       TO UR70-FLAG1
           MOVE '0'                       TO UR70-FLAG2
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

           MOVE +1                                 TO UR70-VERSION
           MOVE 'U'                                TO UR70-FLAG1
           MOVE '0'                                TO UR70-FLAG2
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
              DISPLAY 'UR70-JRETC = ' UR70-JRETC
           END-IF.

       104-EXIT.
           EXIT.
