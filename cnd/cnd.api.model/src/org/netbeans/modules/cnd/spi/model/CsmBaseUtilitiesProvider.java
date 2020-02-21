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

package org.netbeans.modules.cnd.spi.model;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmBaseUtilitiesProvider {
    private static CsmBaseUtilitiesProvider DEFAULT = new Default();

    public static CsmBaseUtilitiesProvider getDefault() {
        return DEFAULT;
    }
    
    public abstract boolean isGlobalVariable(CsmVariable var);

    public abstract CsmFunction getFunctionDeclaration(CsmFunction fun);
    
    public abstract CsmNamespace getFunctionNamespace(CsmFunction fun);

    public abstract CsmNamespace getClassNamespace(CsmClassifier cls);
    
    public abstract CsmClass getFunctionClass(CsmFunction fun);
    
    public abstract boolean isUnresolved(Object obj);
    
    public abstract boolean isDummyForwardClass(CsmDeclaration decl);

    public abstract CharSequence getDummyForwardSimpleQualifiedName(CsmDeclaration decl);
    
    public abstract CsmExpression getDecltypeExpression(CsmType type);

    /**
     * Implementation of the compound provider
     */
    private static final class Default extends CsmBaseUtilitiesProvider {
        private final Collection<? extends CsmBaseUtilitiesProvider> svcs;
        private static final boolean FIX_SERVICE = true;
        private CsmBaseUtilitiesProvider fixedResolver;
        
        Default() {
            svcs = Lookup.getDefault().lookupAll(CsmBaseUtilitiesProvider.class);
        }
        
        private CsmBaseUtilitiesProvider getService(){
            CsmBaseUtilitiesProvider service = fixedResolver;
            if (service == null) {
                for (CsmBaseUtilitiesProvider selector : svcs) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    // I see that it is ugly solution, but NB core cannot fix performance of FolderInstance.waitFinished()
                    fixedResolver = service;
                }
            }
            return service;
        }
        
        @Override
        public boolean isGlobalVariable(CsmVariable var) {
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                return provider.isGlobalVariable(var);
            }
            return true;
        }

        @Override
        public CsmFunction getFunctionDeclaration(CsmFunction fun) {
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                return provider.getFunctionDeclaration(fun);
            }
            return null;
        }

        @Override
        public CsmNamespace getFunctionNamespace(CsmFunction fun) {
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                return provider.getFunctionNamespace(fun);
            }
            return null;
        }

        @Override
        public CsmNamespace getClassNamespace(CsmClassifier cls) {
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                return provider.getClassNamespace(cls);
            }
            return null;
        }

        @Override
        public CsmClass getFunctionClass(CsmFunction fun) {
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                return provider.getFunctionClass(fun);
            }
            return null;
        }

        @Override
        public boolean isUnresolved(Object obj) {
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                return provider.isUnresolved(obj);
            }
            return false;
        }

        @Override
        public boolean isDummyForwardClass(CsmDeclaration decl) {
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                return provider.isDummyForwardClass(decl);
            }
            return false;
        }

        @Override
        public CharSequence getDummyForwardSimpleQualifiedName(CsmDeclaration decl) {
            CharSequence qname = null;
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                qname = provider.getDummyForwardSimpleQualifiedName(decl);
            }
            return (qname == null) ? decl.getName() : qname;
        }

        @Override
        public CsmExpression getDecltypeExpression(CsmType type) {
            CsmBaseUtilitiesProvider provider = getService();
            if (provider != null) {
                return provider.getDecltypeExpression(type);
            }
            return null;
        }
    }
}
