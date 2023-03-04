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

package org.netbeans.modules.editor.lib;

import org.netbeans.api.editor.guards.DocumentGuards;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.modules.editor.document.implspi.DocumentServiceFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
public class DocumentGuardsImpl implements DocumentGuards {
    private final GuardedDocument doc;

    public DocumentGuardsImpl(GuardedDocument doc) {
        this.doc = doc;
    }

    @Override
    public boolean isPositionGuarded(int position, boolean forInsertion) {
        return doc.isPosGuarded(position);
    }

    @Override
    public int adjustPosition(int position, boolean direction) {
        MarkBlockChain mbc = doc.getGuardedBlockChain();
        return direction ? mbc.adjustToBlockEnd(position) : 
                EditorPackageAccessor.get().MarkBlockChain_adjustPos(mbc, position, true);
    }

    @Override
    public int findNextBlock(int position, boolean direction) {
        MarkBlockChain mbc = doc.getGuardedBlockChain();
        return direction ? mbc.adjustToNextBlockStart(position) : 
                EditorPackageAccessor.get().MarkBlockChain_adjustPos(mbc, position, false);
    }
    
    @ServiceProvider(service = DocumentServiceFactory.class, path = "Editors/Documents/org.netbeans.editor.GuardedDocument")
    public static class F implements DocumentServiceFactory<GuardedDocument> {

        @Override
        public Lookup forDocument(GuardedDocument doc) {
            return Lookups.fixed(new DocumentGuardsImpl(doc));
        }
    }
}
