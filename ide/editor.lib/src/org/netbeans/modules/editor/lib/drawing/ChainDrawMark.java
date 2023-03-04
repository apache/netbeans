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

package org.netbeans.modules.editor.lib.drawing;

import javax.swing.text.Position;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.InvalidMarkException;

/** Support for draw marks chained in double linked list */
public final class ChainDrawMark extends DrawMark {

    /** Next mark in chain */
    protected ChainDrawMark next;

    /** Previous mark in chain */
    protected ChainDrawMark prev;

    public ChainDrawMark(String layerName, EditorUI editorUI) {
        this(layerName, editorUI, Position.Bias.Forward);
    }

    public ChainDrawMark(String layerName, EditorUI editorUI, Position.Bias bias) {
        super(layerName, editorUI, bias);
    }

    public final ChainDrawMark getNext() {
        return next;
    }

    public final void setNext(ChainDrawMark mark) {
        next = mark;
    }

    /** Set next mark in chain */
    public void setNextChain(ChainDrawMark mark) {
        this.next = mark;
        if (mark != null) {
            mark.prev = this;
        }
    }

    public final ChainDrawMark getPrev() {
        return prev;
    }

    public final void setPrev(ChainDrawMark mark) {
        prev = mark;
    }

    /** Set previous mark in chain */
    public void setPrevChain(ChainDrawMark mark) {
        this.prev = mark;
        if (mark != null) {
            mark.next = this;
        }
    }

    /** Insert mark before this one in chain
    * @return inserted mark
    */
    public ChainDrawMark insertChain(ChainDrawMark mark) {
        ChainDrawMark thisPrev = this.prev;
        mark.prev = thisPrev;
        mark.next = this;
        if (thisPrev != null) {
            thisPrev.next = mark;
        }
        this.prev = mark;
        return mark;
    }

    /** Remove this mark from the chain
    * @return next chain member or null for end of chain
    */
    public ChainDrawMark removeChain() {
        ChainDrawMark thisNext = this.next;
        ChainDrawMark thisPrev = this.prev;
        if (thisPrev != null) { // not the first
            thisPrev.next = thisNext;
            this.prev = null;
        }
        if (thisNext != null) { // not the last
            thisNext.prev = thisPrev;
            this.next = null;
        }
        try {
            this.remove(); // remove the mark from DocMarks
        } catch (InvalidMarkException e) {
            // already removed
        }
        return thisNext;
    }

    public String toStringChain() {
        return toString() + (next != null ? "\n" + next.toStringChain() : ""); // NOI18N
    }

    public @Override String toString() {
        return super.toString() + ", " // NOI18N
               + ((prev != null) ? ((next != null) ? "chain member" // NOI18N
                        : "last member") : ((next != null) ? "first member" // NOI18N
                                                        : "standalone member")); // NOI18N
    }

}
