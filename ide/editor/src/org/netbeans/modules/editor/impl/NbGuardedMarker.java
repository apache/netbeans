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

package org.netbeans.modules.editor.impl;

import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.document.implspi.DocumentServiceFactory;
import org.netbeans.spi.editor.guards.GuardedRegionMarker;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of Swing marker for Guarded blocks, which maintains
 * appropriate Swing element attributes.
 * 
 * @author sdedic
 */
public class NbGuardedMarker implements GuardedRegionMarker {
    private final StyledDocument doc;

    public NbGuardedMarker(StyledDocument doc) {
        this.doc = doc;
    }
    
    @Override
    public void protectRegion(int start, int len) {
        NbDocument.markGuarded(doc, start, len);
    }

    @Override
    public void unprotectRegion(int start, int len) {
        NbDocument.unmarkGuarded(doc, start, len);
    }
    
    @ServiceProvider(service = DocumentServiceFactory.class, path = "Editors/Documents/org.netbeans.editor.GuardedDocument")
    public static class F implements DocumentServiceFactory<BaseDocument> {

        @Override
        public Lookup forDocument(BaseDocument doc) {
            if (!(doc instanceof StyledDocument)) {
                return null;
            }
            return Lookups.fixed(new NbGuardedMarker((StyledDocument)doc));
        }
    }
}
