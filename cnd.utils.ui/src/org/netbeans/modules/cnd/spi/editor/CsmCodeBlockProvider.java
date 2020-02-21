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
package org.netbeans.modules.cnd.spi.editor;

import javax.swing.text.Document;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmCodeBlockProvider {
    private static final CsmCodeBlockProvider DEFAULT = new CsmCodeBlockProvider.Default();

    public static CsmCodeBlockProvider getDefault(){
        return DEFAULT;
    }

    protected CsmCodeBlockProvider() {
    }

    public abstract CsmCodeBlockProvider.Scope getScope(Document doc, int position);

    public static interface Scope {
        int getStartOffset();
        int getEndOffset();
        Scope getParentScope();
    }
    
    private static final class Default extends CsmCodeBlockProvider {
        private final Lookup.Result<CsmCodeBlockProvider> res;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmCodeBlockProvider.class);
        }

        private CsmCodeBlockProvider getService(){
            for (CsmCodeBlockProvider selector : res.allInstances()) {
                return selector;
            }
            return null;
        }

        @Override
        public CsmCodeBlockProvider.Scope getScope(Document doc, int position) {
            CsmCodeBlockProvider provider = getService();
            if (provider != null) {
                return provider.getScope(doc, position);
            }
            return null;
        }
    }
}
