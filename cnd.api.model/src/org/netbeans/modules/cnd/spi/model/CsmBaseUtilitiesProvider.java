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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
