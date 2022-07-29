/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.editor;

/**
* Lexical analyzer that works on a given text buffer. It allows
* to sequentially parse a given character buffer by calling
* <tt>nextToken()</tt> that returns the token-ids.
*
* After the token is found by calling the <tt>nextToken</tt> method,
* the <tt>getTokenOffset()</tt> method can be used
* to get the starting offset of the current
* token in the buffer. The <tt>getTokenLength()</tt> gives the length
* of the current token.
*
* The heart of the analyzer is the <tt>parseToken()</tt> method which
* parses the text and returns the token-id of the last token found.
* The <tt>parseToken()</tt> method is called from the <tt>nextToken()</tt>.
* It operates with two important variables. The <tt>offset</tt>
* variable identifies the currently scanned character in the buffer.
* The <tt>tokenOffset</tt> is the begining of the current token.
* The <tt>state</tt> variable that identifies the current internal
* state of the analyzer is set accordingly when the characters are parsed.
* If the <tt>parseToken()</tt> recognizes a token, it returns its ID
* and the <tt>tokenOffset</tt> is its begining in the buffer and
* <tt>offset - tokenOffset</tt> is its length. When the token is processed
* the value of <tt>tokenOffset</tt> is set to be the same as current
* value of the <tt>offset</tt> and the parsing continues.
*
* Internal states are the integer constants used internally by analyzer.
* They are assigned to the <tt>state</tt> variable to express
* that the analyzer has moved from one state to another.
* They are usually numbered starting from zero but they don't
* have to. The only reserved value is -1 which is reserved
* for the INIT state - the initial internal state of the analyzer.
*
* There is also the support for defining the persistent info about
* the current state of the analyzer. This info can be later used
* to restore the parsing from some particular state instead of
* parsing from the begining of the buffer. This feature is very
* useful if there are the modifications performed in the document.
* The info is stored in the <tt>StateInfo</tt> interface
* with the <tt>BaseStateInfo</tt> as the basic implementation.
* It enables to get and set the two important values
* from the persistent point of view.
* The first one is the value of the <tt>state</tt> variable.
* The other one is the difference <tt>offset - tokenOffset</tt>
* which is called pre-scan. The particular analyzer can define
* additional values important for the persistent storage.
* The <tt>createStateInfo()</tt> can be overriden to create
* custom state-info and <tt>loadState()</tt> and <tt>storeState()</tt>
* can be overriden to get/set the additional values.
*
* The <tt>load()</tt> method sets the buffer to be parsed.
* There is a special parameter in the load() method called position
* that allows a relation of the character buffer passed to the load()
* method and the position of the buffer's data in the document.
* For this extended functionality the document must be passed
* to the constructor of the lexical analyzer at some level.
*
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Syntax {

    /** Is the state of analyzer equal to a given state info? */
    public static final int EQUAL_STATE = 0;

    /** Is the state of analyzer different from given state info? */
    public static final int DIFFERENT_STATE = 1;


    /** Initial internal state of the analyzer */
    public static final int INIT = -1;



    /** Internal state of the lexical analyzer. At the begining
    * it's set to INIT value but it is changed by <tt>parseToken()</tt>
    * as the characters are processed one by one.
    */
    protected int state = INIT;

    /** Text buffer to scan */
    protected char buffer[];

    /** Current offset in the buffer */
    protected int offset;

    /** Offset holding the begining of the current token */
    protected int tokenOffset;

    /** This variable is the length of the token that was found */
    protected int tokenLength;

    /** Path from which the found token-id comes from.
    * The <tt>TokenContext.getContextPath()</tt> can be used
    * to get the path. If the lexical analyzer doesn't use
    * any children token-contexts it can assign
    * the path in the constructor.
    */
    protected TokenContextPath tokenContextPath;

    /** Setting this flag to true means that there are currently no more
    * buffers available so that analyzer should return all the tokens
    * including those whose successful scanning would be otherwise
    * left for later when the next buffer will be available. Setting
    * this flag to true ensures that all the characters in the current
    * buffer will be processed.
    * The lexical analyzer should on one hand process all the characters
    * but on the other hand it should "save" its context. For example
    * if the scanner finds the unclosed comment at the end of the buffer
    * it should return the comment token but
    * stay in the "being in comment" internal state.
    */
    protected boolean lastBuffer;

    /** On which offset in the buffer scanning should stop. */
    protected int stopOffset;

    /** The position in the document that logically corresponds
    * to the stopOffset value. If there's no relation
    * to the document, it's -1. The reason why the relation
    * to the document's data is expressed through
    * the stopOffset to stopPosition relation is because
    * the stopOffset is the only offset that doesn't change
    * rapidly in the operation of the lexical analyzer.
    */
    protected int stopPosition;

    /** This variable can be populated by the parseToken() method
    * in case the user types an errorneous construction but
    * it's clear what correct token he meant to write.
    * For example if the user writes a single '0x' it's an errorneous
    * construct but it's clear that the user wants to enter
    * the hexa-number. In this situation the parseToken()
    * should report error, but it should also set the supposedTokenID
    * to the hexa-number token.
    * This information is used while drawing the text. If the caret
    * stand inside or around such token, it calls the getSupposedTokenID()
    * after calling the nextToken() and if it's non-null it uses it
    * instead of the original token.
    */
    protected TokenID supposedTokenID;

    /** Function that should be called externally to scan the text.
    * It manages the call to parseToken() and cares about the proper
    * setting of the offsets.
    * It can be extended to support any custom debugging required.
    */
    public TokenID nextToken() {
        // Return immediately when at the end of buffer
        if (tokenOffset >= stopOffset) {
            tokenLength = 0;
            return null; // signal no token found
        }

        // Divide non-debug and debug sections
        supposedTokenID = null;
        TokenID tokenID = parseToken();
        if (tokenID != null) { // regular token found
            tokenLength = offset - tokenOffset;
            tokenOffset = offset; // move to the next token
            if (tokenLength == 0) { // test for empty token
                return nextToken(); // repeat until non-empty token is found
            }
        } else { // EOT reached
            tokenLength = 0;
        }

        return tokenID;
    }

    /** This is core function of analyzer and it returns either the token-id
    * or null to indicate that the end of buffer was found.
    * The function scans the active character and does one or more
    * of the following actions:
    * 1. change internal analyzer state
    * 2. set the token-context-path and return token-id
    * 3. adjust current position to signal different end of token;
    *    the character that offset points to is not included in the token
    */
    protected TokenID parseToken() {
        return null;
    }

    /** Load the state from syntax mark into analyzer. This method is used when
    * @param stateInfo info about the state of the lexical analyzer to load.
    *   It can be null to indicate there's no previous state so the analyzer
    *   starts from its initial state.
    * @param buffer buffer that will be scanned
    * @param offset offset of the first character that will be scanned
    * @param len length of the area to be scanned
    * @param lastBuffer whether this is the last buffer in the document. All the tokens
    *   will be returned including the last possibly incomplete one. If the data
    *   come from the document, the simple rule for this parameter
    *   is (doc.getLength() == stop-position) where stop-position
    *   is the position corresponding to the (offset + len) in the buffer
    *   that comes from the document data.
    * @param stopPosition position in the document that corresponds to (offset + len) offset
    *   in the provided buffer. It has only sense if the data in the buffer come from the document.
    *   It helps in writing the advanced analyzers that need to interact with some other data
    *   in the document than only those provided in the character buffer.
    *   If there is no relation to the document data, the stopPosition parameter
    *   must be filled with -1 which means an invalid value.
    *   The stop-position is passed (instead of start-position) because it doesn't
    *   change through the analyzer operation. It corresponds to the <tt>stopOffset</tt>
    *   that also doesn't change through the analyzer operation so any
    *   buffer-offset can be transferred to position by computing
    *   <tt>stopPosition + buffer-offset - stopOffset</tt>
    *   where stopOffset is the instance variable that is assigned
    *   to <tt>offset + len</tt> in the body of relocate().
    */
    public void load(StateInfo stateInfo, char buffer[], int offset, int len,
                     boolean lastBuffer, int stopPosition) {
        this.buffer = buffer;
        this.offset = offset;
        this.tokenOffset = offset;
        this.stopOffset = offset + len;
        this.lastBuffer = lastBuffer;
        this.stopPosition = stopPosition;

        if (stateInfo != null) {
            loadState(stateInfo);
        } else {
            loadInitState();
        }
    }

    /** Relocate scanning to another buffer.
    * This is used to continue scanning after previously
    * reported EOT. Relocation delta between current offset and the requested offset
    * is computed and all the offsets are relocated. If there's a non-zero preScan
    * in the analyzer, it is a caller's responsibility to provide all the preScan
    * characters in the relocation buffer.
    * @param buffer next buffer where the scan will continue.
    * @param offset offset where the scan will continue.
    *   It's not decremented by the current preScan.
    * @param len length of the area to be scanned.
    *   It's not extended by the current preScan.
    * @param lastBuffer whether this is the last buffer in the document. All the tokens
    *   will be returned including the last possibly incomplete one. If the data
    *   come from the document, the simple rule for this parameter
    *   is (doc.getLength() == stop-position) where stop-position
    *   is the position corresponding to the (offset + len) in the buffer
    *   that comes from the document data.
    * @param stopPosition position in the document that corresponds to (offset + len) offset
    *   in the provided buffer. It has only sense if the data in the buffer come from the document.
    *   It helps in writing the advanced analyzers that need to interact with some other data
    *   in the document than only those provided in the character buffer.
    *   If there is no relation to the document data, the stopPosition parameter
    *   must be filled with -1 which means an invalid value.
    *   The stop-position is passed (instead of start-position) because it doesn't
    *   change through the analyzer operation. It corresponds to the <tt>stopOffset</tt>
    *   that also doesn't change through the analyzer operation so any
    *   buffer-offset can be transferred to position by computing
    *   <tt>stopPosition + buffer-offset - stopOffset</tt>
    *   where stopOffset is the instance variable that is assigned
    *   to <tt>offset + len</tt> in the body of relocate().
    */
    public void relocate(char buffer[], int offset, int len,
    boolean lastBuffer, int stopPosition) {
        this.buffer = buffer;
        this.lastBuffer = lastBuffer;

        int delta = offset - this.offset; // delta according to current offset
        this.offset += delta;
        this.tokenOffset += delta;
        this.stopOffset = offset + len;
        this.stopPosition = stopPosition;
    }

    /** Get the current buffer */
    public char[] getBuffer() {
        return buffer;
    }

    /** Get the current scanning offset */
    public int getOffset() {
        return offset;
    }

    /** Get start of token in scanned buffer. */
    public int getTokenOffset() {
        return offset - tokenLength;
    }

    /** Get length of token in scanned buffer. */
    public int getTokenLength() {
        return tokenLength;
    }

    /** Get the token-context-path of the returned token. */
    public TokenContextPath getTokenContextPath() {
        return tokenContextPath;
    }

    public TokenID getSupposedTokenID() {
        return supposedTokenID;
    }

    /** Get the pre-scan which is a number
    * of characters between offset and tokenOffset.
    * If there's no more characters in the current buffer,
    * the analyzer returns EOT, but it can be in a state when
    * there are already some characters parsed at the end of
    * the current buffer but the token
    * is still incomplete and it cannot be returned yet.
    * The pre-scan value helps to determine how many characters
    * from the end of the current buffer should be present
    * at the begining of the next buffer so that the current
    * incomplete token can be returned as the first token
    * when parsing the next buffer.
    */
    public int getPreScan() {
        return offset - tokenOffset;
    }

    /** Initialize the analyzer when scanning from the begining
    * of the document or when the state stored in syntax mark
    * is null for some reason or to explicitly reset the analyzer
    * to the initial state. The offsets must not be touched by this method.
    */
    public void loadInitState() {
        state = INIT;
    }

    public void reset() {
        tokenLength = stopOffset = tokenOffset = offset = 0;
        loadInitState();
    }

    /** Load valid mark state into the analyzer. Offsets
    * are already initialized when this method is called. This method
    * must get the state from the mark and set it to the analyzer. Then
    * it must decrease tokenOffset by the preScan stored in the mark state.
    * @param stateInfo mark state to be loaded into syntax. It must be non-null value.
    */
    public void loadState(StateInfo stateInfo) {
        state = stateInfo.getState();
        tokenOffset -= stateInfo.getPreScan();
    }

    /** Store state of this analyzer into given mark state. */
    public void storeState(StateInfo stateInfo) {
        stateInfo.setState(state);
        stateInfo.setPreScan(getPreScan());
    }

    /** Compare state of this analyzer to given state info */
    public int compareState(StateInfo stateInfo) {
        if (stateInfo != null) {
            return ((stateInfo.getState() == state) && stateInfo.getPreScan() == getPreScan())
                   ? EQUAL_STATE : DIFFERENT_STATE;
        } else {
            return DIFFERENT_STATE;
        }
    }

    /** Create state info appropriate for particular analyzer */
    public StateInfo createStateInfo() {
        return new BaseStateInfo();
    }

    /** Get state name as string. It can be used for debugging purposes
    * by developer of new syntax analyzer. The states that this function
    * recognizes can include all constants used in analyzer so that it can
    * be used everywhere in analyzer to convert numbers to more practical strings.
    */
    public String getStateName(int stateNumber) {
        switch(stateNumber) {
        case INIT:
            return "INIT"; // NOI18N

        default:
            return "Unknown state " + stateNumber; // NOI18N
        }
    }

    /** Syntax information as String */
    public String toString() {
        return "tokenOffset=" + tokenOffset // NOI18N
               + ", offset=" + offset // NOI18N
               + ", state=" + getStateName(state) // NOI18N
               + ", stopOffset=" + stopOffset // NOI18N
               + ", lastBuffer=" + lastBuffer; // NOI18N
    }


    /** Interface that stores two basic pieces of information about
    * the state of the whole lexical analyzer - its internal state and preScan.
    */
    public interface StateInfo {

        /** Get the internal state */
        public int getState();

        /** Store the internal state */
        public void setState(int state);

        /** Get the preScan value */
        public int getPreScan();

        /** Store the preScan value */
        public void setPreScan(int preScan);

    }


    /** Base implementation of the StateInfo interface */
    public static class BaseStateInfo implements StateInfo {

        /** analyzer state */
        private int state;

        /** Pre-scan length */
        private int preScan;

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getPreScan() {
            return preScan;
        }

        public void setPreScan(int preScan) {
            this.preScan = preScan;
        }

        public String toString(Syntax syntax) {
            return "state=" // NOI18N
                + ((syntax != null)
                    ? syntax.getStateName(getState())
                    : Integer.toString(getState()))
                + ", preScan=" + getPreScan(); // NOI18N
        }
        
        public String toString() {
            return toString(null);
        }

    }

}
