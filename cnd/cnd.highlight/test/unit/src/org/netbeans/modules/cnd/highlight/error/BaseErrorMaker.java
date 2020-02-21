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

package org.netbeans.modules.cnd.highlight.error;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;

/**
 * Base class for ErrorMaker implementations
 */
public abstract class BaseErrorMaker implements ErrorMaker  {

    private BaseDocument document;
    private CsmFile csmFile;
    
    @Override
    public void init(BaseDocument document, CsmFile csmFile) {
        this.document = document;
        this.csmFile = csmFile;
    }
    
    protected BaseDocument getDocument() {
        return document;
    }
    
    protected CsmFile getCsmFile() {
        return csmFile;
    }

    @Override
    public void undone() {
    }

    protected void remove(final int offs, final int len) throws BadLocationException {
        final BaseDocument doc = getDocument();
        final BadLocationException ex[] = new BadLocationException[] { null };
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    doc.remove(offs, len);
                } catch (BadLocationException e) {
                    ex[0] = e;
                }
            }
        });
        if (ex[0] != null) {
            throw ex[0];
        }
    }
    
    protected void insert(final int offset, final String text) throws BadLocationException {
        final BaseDocument doc = getDocument();
        final BadLocationException ex[] = new BadLocationException[] { null };
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    doc.insertString(offset, text, null);
                } catch (BadLocationException e) {
                    ex[0] = e;
                }
            }
        });
        if (ex[0] != null) {
            throw ex[0];
        }        
    }
//    public void analyze(Collection<CsmErrorInfo> errors) {
//    }
    

}
