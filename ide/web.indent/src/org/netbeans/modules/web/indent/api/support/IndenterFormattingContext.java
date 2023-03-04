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
