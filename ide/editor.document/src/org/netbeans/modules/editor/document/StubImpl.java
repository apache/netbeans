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

package org.netbeans.modules.editor.document;

import javax.swing.text.Document;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.AtomicLockListener;
import org.netbeans.modules.editor.document.implspi.CharClassifier;
import org.netbeans.modules.editor.document.implspi.DocumentServiceFactory;
import org.netbeans.modules.editor.lib2.AcceptorFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
public class StubImpl implements AtomicLockDocument, CharClassifier {
    private final Document doc;

    public StubImpl(Document doc) {
        this.doc = doc;
    }
    
    @Override
    public Document getDocument() {
        return doc;
    }

    @Override
    public void atomicUndo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void runAtomic(Runnable r) {
        synchronized (doc) {
            r.run();
        }
    }

    @Override
    public void runAtomicAsUser(Runnable r) {
        runAtomic(r);
    }

    @Override
    public void addAtomicLockListener(AtomicLockListener l) {
    }

    @Override
    public void removeAtomicLockListener(AtomicLockListener l) {
    }

    @Override
    public boolean isIdentifierPart(char ch) {
        return AcceptorFactory.JAVA_IDENTIFIER.accept(ch);
    }

    @Override
    public boolean isWhitespace(char ch) {
        return AcceptorFactory.WHITESPACE.accept(ch);
    }

    @ServiceProvider(service = DocumentServiceFactory.class, path="Editors/Documents/javax.swing.text.Document")
    public static class F implements DocumentServiceFactory<Document> {

        @Override
        public Lookup forDocument(Document doc) {
            return Lookups.fixed(new StubImpl(doc));
        }
        
    }
}
