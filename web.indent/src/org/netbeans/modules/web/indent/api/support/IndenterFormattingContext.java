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

package org.netbeans.modules.web.indent.api.support;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.editor.BaseDocument;

/**
 *
 * @since org.netbeans.modules.css.editor/1 1.3
 */
public final class IndenterFormattingContext {

    private boolean firstIndenter = false;
    private boolean lastIndenter = false;
    private boolean initialized = false;
    private BaseDocument doc;

    private DocumentListener listener;
    private List<Change> changes;
    private List<List<AbstractIndenter.Line>> indentedLines;

    private IndenterFormattingContext delegate;

    public IndenterFormattingContext(BaseDocument doc) {
        this.doc = doc;
    }

    void initFirstIndenter() {
        assert !initialized;
        if (isInitialized()) {
            return;
        }
        firstIndenter = true;
        initialized = true;
        changes = new ArrayList<Change>();
        listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                changes.add(new Change(e.getOffset(), e.getLength()));
            }

            public void removeUpdate(DocumentEvent e) {
                changes.add(new Change(e.getOffset(), -e.getLength()));
            }

            public void changedUpdate(DocumentEvent e) {
                // ignore
            }
        };
        doc.addDocumentListener(listener);
        indentedLines = new ArrayList<List<AbstractIndenter.Line>>();
    }

    void setDelegate(IndenterFormattingContext delegate) {
        assert !initialized;
        assert delegate.isFirstIndenter();
        initialized = true;
        this.delegate = delegate;
    }

    List<Change> getAndClearChanges() {
        if (delegate != null) {
            return delegate.getAndClearChanges();
        }
        List<Change> result = new ArrayList<Change>(changes);
        changes.clear();
        return result;
    }

    public boolean isFirstIndenter() {
        return firstIndenter;
    }

    void setLastIndenter() {
        this.lastIndenter = true;
    }

    public boolean isLastIndenter() {
        return lastIndenter;
    }

    boolean isInitialized() {
        return initialized;
    }

    void disableListener() {
        if (delegate != null) {
            delegate.disableListener();
            return;
        }
        assert listener != null;
        doc.removeDocumentListener(listener);
    }

    void enableListener() {
        if (delegate != null) {
            delegate.enableListener();
            return;
        }
        assert listener != null;
        doc.addDocumentListener(listener);
    }

    void removeListener() {
        if (delegate != null) {
            delegate.removeListener();
            initialized = false;
            return;
        }
        assert listener != null;
        doc.removeDocumentListener(listener);
        initialized = false;
        listener = null;
    }

    List<List<AbstractIndenter.Line>> getIndentationData() {
        if (delegate != null) {
            return delegate.getIndentationData();
        }
        return indentedLines;
    }

    static class Change {
        public int offset;
        // positive value is insert; negative removal
        public int change;

        public Change(int offset, int change) {
            this.offset = offset;
            this.change = change;
        }

        @Override
        public String toString() {
            return "Change["+offset+":"+change+"]";
        }

    }

}
