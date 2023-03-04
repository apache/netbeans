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

/** Process the tokens
*
* @author Miloslav Metelka
* @version 1.00
*/


public interface TokenProcessor {

    /** Notify that the token was found.
    * @param tokenID ID of the token found
    * @param tokenContextPath Context-path in which the token that was found.
    * @param tokenBufferOffset Offset of the token in the buffer. The buffer
    *  is provided in the <tt>nextBuffer()</tt> method.
    * @param tokenLength Length of the token found
    * @return true if the next token should be searched or false if the scan should
    *   be stopped completely.
    */
    public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
                         int tokenBufferOffset, int tokenLength);

    /** Notify that end of scanned buffer was found.
    * The method decides whether to continue the scan or stop. The rest
    * of characters that were not scanned, because the is not completed
    * is also provided.
    * @param offset offset of the rest of the characters
    * @return 0 to stop token processing,
    *         &gt 0 process additional characters in the document
    */
    public int eot(int offset);

    /** Notify that the following buffer will be scanned. This method
    * is called before the buffer is being scanned.
    * @param buffer buffer that will be scanned. To get the text of the tokens
    *   the buffer should be stored in some instance variable.
    * @param offset offset in the buffer with the first character to be scanned.
    *   If doesn't reflect the possible preScan. If the preScan would be non-zero
    *   then the first buffer offset that contains the valid data is
    *   <tt>offset - preScan</tt>.
    * @param len count of the characters that will be scanned. It doesn't reflect
    *   the ppossible reScan.
    * @param startPos starting position of the scanning in the document. It
    *   logically corresponds to the <tt>offset</tt> because of the same
    *   text data both in the buffer and in the document.
    *   It again doesn't reflect the possible preScan and the <tt>startPos - preScan</tt>
    *   gives the real start of the first token. If it's necessary to know
    *   the position of each token, it's a good idea to store the value
    *   <tt>startPos - offset</tt> in an instance variable that could be called
    *   <tt>bufferStartPos</tt>. The position of the token can be then computed
    *   as <tt>bufferStartPos + tokenBufferOffset</tt>.
    * @param preScan preScan needed for the scanning.
    * @param lastBuffer whether this is the last buffer to scan in the document
    *   so there are no more characters in the document after this buffer.
    * @*/
    public void nextBuffer(char[] buffer, int offset, int len,
                           int startPos, int preScan, boolean lastBuffer);

}
