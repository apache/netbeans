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

/*
 * "TermStream.java"
 * TermStream.java 1.7 01/07/10
 */

package org.netbeans.lib.terminalemulator;

/**
 * TermStream is analogous to unix STREAMS. 
 * <br>
 * It is a full duplex processing and data transfer path between a raw
 * Term (The Data Terminal Equipment, DTE) and a client of the Term, usually
 * a process (The Data Communication Equipment, DCE).
 * <p>
 * TermStream's can be chained together. This is performed using
 * @see org.netbeans.lib.terminalemulator.Term#pushStream
 * <p>
 * Streams are usually used (in the context of terminals) to do echoing,
 * line buffering, CR/NL translation and so on. See
 * @see org.netbeans.lib.terminalemulator.LineDiscipline .
 * They can also be used for logging and debugging.
 */

public abstract class TermStream {
    protected TermStream toDTE;		// delegate putChar's to toDTE
    protected TermStream toDCE;		// delegate sendChar to from_keyboard

    void setToDCE(TermStream toDCE) {
	this.toDCE = toDCE;
    } 
    void setToDTE(TermStream toDTE) {
	this.toDTE = toDTE;
    }

    void setTerm(Term term) {
	this.term = term;
    } 
    protected Term getTerm() {
	return term;
    } 
    private Term term;


    // From world (DCE) to terminal (DTE) screen
    public abstract void flush();
    public abstract void putChar(char c);
    public abstract void putChars(char buf[], int offset, int count);

    // From terminal *keyboard) (DTE) to world (DCE).
    public abstract void sendChar(char c);
    public abstract void sendChars(char c[], int offset, int count);
}
