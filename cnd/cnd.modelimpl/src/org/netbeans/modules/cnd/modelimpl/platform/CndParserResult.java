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

package org.netbeans.modules.cnd.modelimpl.platform;

import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.util.WeakListeners;

/**
 *
 */
public class CndParserResult  extends Result implements TokenHierarchyListener {

    private final CsmFile file;
    private final long fileVersion;
    private final long docVersion;
    private boolean invalid = false;

    /*package*/CndParserResult(CsmFile file, Snapshot snapshot, long fileVersion, long docVersion) {
        super(snapshot);
        this.file = file;
        this.fileVersion = fileVersion;
        this.docVersion = docVersion;
        // when snapshot is document based we'd like to be sensitive to
        // rebuild of TokenHierarchy, i.e. when language flavor of document
        // is changed and document is relexed;
        // or DocumentLanguageFlavorProvider.addProperty took long time
        // and Parsing API was invoked on the document with default language flavor
        final Document doc = snapshot.getSource().getDocument(false);
        if (doc != null) {
            TokenHierarchy<Document> hierarchy = TokenHierarchy.get(doc);
            if (hierarchy != null) {
                hierarchy.addTokenHierarchyListener(WeakListeners.create(TokenHierarchyListener.class, CndParserResult.this, hierarchy));
            }
        }
    }

    @Override
    protected void invalidate() {
    }

    public CsmFile getCsmFile() {
        return file;
    }
    
    /*package*/ long getFileVersion() {
        return fileVersion;
    }
    
    /*package*/ long getDocumentVersion() {
        return docVersion;
    }

    /*package*/ boolean isInvalid() {
        return invalid;
    }

    @Override
    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        if (evt.type() == TokenHierarchyEventType.REBUILD) {
            this.invalid = true;
        }
    }
}
