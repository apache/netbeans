/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
