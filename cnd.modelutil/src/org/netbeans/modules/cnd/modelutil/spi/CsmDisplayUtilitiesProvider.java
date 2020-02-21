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
package org.netbeans.modules.cnd.modelutil.spi;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.openide.util.Lookup;


/**
 *
 */
public abstract class CsmDisplayUtilitiesProvider {
    
    public static CsmDisplayUtilitiesProvider getDefault() {
        return DEFAULT;
    }
    
    public abstract CharSequence getTooltipText(CsmObject item);
    
    public abstract CharSequence getTypeText(CsmType type, boolean expandInstantiations, boolean evaluateExpressions);
    
//<editor-fold defaultstate="collapsed" desc="Implementation">    
    private static final CsmDisplayUtilitiesProvider DEFAULT = new Default();
    
    private final static class Default extends CsmDisplayUtilitiesProvider {
        
        private final Lookup.Result<CsmDisplayUtilitiesProvider> res;
        
        private CsmDisplayUtilitiesProvider delegate;
        
        private Default() {
            res = Lookup.getDefault().lookupResult(CsmDisplayUtilitiesProvider.class);
        }
        
        private CsmDisplayUtilitiesProvider getDelegate(){
            CsmDisplayUtilitiesProvider service = delegate;
            if (service == null) {
                for (CsmDisplayUtilitiesProvider resolver : res.allInstances()) {
                    service = resolver;
                    break;
                }
                delegate = service;
            }
            return service;
        }
        
        @Override
        public CharSequence getTooltipText(CsmObject item) {
            return getDelegate().getTooltipText(item);
        }

        @Override
        public CharSequence getTypeText(CsmType type, boolean expandInstantiations, boolean evaluateExpressions) {
            return getDelegate().getTypeText(type, expandInstantiations, evaluateExpressions);
        }
    }
//</editor-fold>
}
