/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
