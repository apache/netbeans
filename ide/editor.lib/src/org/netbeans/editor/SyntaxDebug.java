/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.editor;

import java.io.IOException;

/**
* Debugging stuff for the syntax scanners
*
* @author Miloslav Metelka
* @version 1.00
*/

public class SyntaxDebug {

    public static final String NO_STATE_ASSIGNED = "NO STATE ASSIGNED"; // NOI18N
    public static final String NULL_STATE = "NULL STATE"; // NOI18N
    public static final String NULL_SYNTAX_MARK = "NULL SYNTAX MARK"; // NOI18N

    public Syntax syntax;

    public SyntaxDebug(Syntax syntax) {
        this.syntax = syntax;
    }

    /** Scans the whole file by some syntax scanner.
    * @return number of tokens found
    */
    public int parseFile(String fileName)
    throws IOException {
        char chars[] = Analyzer.loadFile(fileName); // line separator only '\n'
        syntax.load(null, chars, 0, chars.length, true, 0);
        int tokenCnt = debugScan();
        return tokenCnt;
    }

    /** Debug scanning on the given string. Write output to console.
    * It returns number of tokens found (excluding EOL and EOT).
    */
    public int debugScan() {
        int tokenCnt = 0;
        while (true) {
            TokenID tokenID = syntax.nextToken();
            if (tokenID == null) { // end of buffer
                System.out.println("EOT at offset=" + syntax.getTokenOffset()); // NOI18N
                return tokenCnt;
            } else { // regular token
                tokenCnt++;
                System.out.println(tokenID.getName() // NOI18N
                        + " in " + syntax.getTokenContextPath() // NOI18N
                        + ": TEXT='" + EditorDebug.debugChars(syntax.getBuffer(), // NOI18N
                                syntax.getTokenOffset(), syntax.getTokenLength())
                        + "', offset=" + syntax.getTokenOffset() // NOI18N
                        + ", len=" + syntax.getTokenLength() // NOI18N
                );
            }
        }
    }

    /** Tests if the scanning returns the same number of EOLs as there's actually
    * '\n' characters in the whole buffer. The test is performed on the whole buffer.
    */
    /*  public boolean eolTest(char chars[]) {
        int lfCount = Analyzer.getLFCount(chars);
        syntax.load(null, chars, 0, chars.length, true, 0);
        int eolCnt = 0;
        TokenID tokenID;
        do {
          tokenID = syntax.nextToken();
          if (tokenID == Syntax.EOL) {
            eolCnt++;
          }
        } while (tokenID != Syntax.EOT);
        if (lfCount == eolCnt) { // test succeeded
          System.out.println("Test SUCCEEDED. " + lfCount + " new-lines found."); // NOI18N
        } else {
          System.out.println("Test FAILED! Number of '\\n' chars: " + lfCount // NOI18N
              + ", number of EOLs: " + eolCnt); // NOI18N
        }
        return lfCount == eolCnt;
      }

      /** Create array of arrays of chars containing wrong characters */
    /*  protected abstract char[][] createWrongCharsArray();

      /* Some arrays of typical wrong characters that can appear
      * in the tokens.
      */
    /*  public static final char[] WRONG_NL = new char[] { '\n' };
      public static final char[] WRONG_NL_TAB = new char[] { '\n', '\t' };
      public static final char[] WRONG_NL_TAB_SPC = new char[] { '\n', '\t', ' ' };
      
      /** Wrong character arrays for the tokens */
    /*  protected char[][] wrongCharsArray;

      public boolean checkTokenText(int tokenID) {
        boolean ok = true;
        if (wrongCharsArray == null) {
          wrongCharsArray = createWrongCharsArray();
        }
        if (wrongCharsArray != null) {
          if (tokenID >= wrongCharsArray.length) {
            return false;
          }
          char[] wrongChars = wrongCharsArray[tokenID];
          for (int i = curInd - tokenLength; i < curInd; i++) {
            for (int j = 0; j < wrongChars.length; j++) {
              if (buffer[i] == wrongChars[j]) {
                System.err.println("Token '" + getTokenName(tokenID) + "' having text " // NOI18N
                    + debugTokenArea() + " contains wrong character '" // NOI18N
                    + debugChars(wrongChars, j, 1) + "'. State: " + this); // NOI18N
                ok = false;
              }
            }
          }
        }
        return ok;
      }

      public String toStringArea() {
        return toString() + ", scan area=" + debugBufferArea(); // NOI18N
      }

      public String debugChars(char chars[], int offset, int len) {
        if (len < 0) {
          return "debugChars() ERROR: len=" + len + " < 0"; // NOI18N
        }
        StringBuffer sb = new StringBuffer(len);
        int endOffset = offset + len;
        for (; offset < endOffset; offset++) {
          switch (chars[offset]) {
            case '\n':
              sb.append("\\n"); // NOI18N
              break;
            case '\t':
              sb.append("\\t"); // NOI18N
              break;
            case '\r':
              sb.append("\\r"); // NOI18N
              break;
            default:
              sb.append(chars[offset]);
          }
        }
        return sb.toString();
      }


      /** Return string describing the area between begInd and curInd */
    /*  public String debugTokenArea() {
        return debugBufferArea(0, 0);
      }
        
      public String debugBufferArea() {
        return debugBufferArea(5, 5);
      }
      
      public String debugBufferArea(int preCnt, int postCnt) {
        StringBuffer sb = new StringBuffer();
        int preStart = Math.max(begInd - preCnt, 0);
        preCnt = begInd - preStart;
        if (preCnt > 0) {
          sb.append(" prefix='"); // NOI18N
          sb.append(debugChars(buffer, preStart, preCnt));
          sb.append("' "); // NOI18N
        }
        sb.append("'"); // NOI18N
        sb.append(debugChars(buffer, begInd, curInd - begInd));
        sb.append("'"); // NOI18N
        postCnt = stopInd - curInd;
        if (postCnt > 0) {
          sb.append(" suffix='"); // NOI18N
          sb.append(debugChars(buffer, preStart, preCnt));
          sb.append("' "); // NOI18N
        }
        return sb.toString();
      }

    */

}
