/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.openide.util.Lookup;

/**
 * entry point to resolve function definition
 */
public abstract class CsmFunctionDefinitionResolver {
    private static final CsmFunctionDefinitionResolver DEFAULT = new Default();
    
    protected CsmFunctionDefinitionResolver() {
    }

    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmFunctionDefinitionResolver getDefault() {
        return DEFAULT;
    }

    /**
     * Returns reference on function definition for function declaration.
     */
    public abstract CsmReference getFunctionDefinition(CsmFunction referencedFunction);
    
    /**
     * Returns collection of function declarations by name
     */
    public abstract Collection<CsmOffsetableDeclaration> findDeclarationByName(CsmProject project, String name);
    
    //
    // Implementation of the default resolver
    //
    private static final class Default extends CsmFunctionDefinitionResolver {
        private final Lookup.Result<CsmFunctionDefinitionResolver> res;

        private Default() {
            res = Lookup.getDefault().lookupResult(CsmFunctionDefinitionResolver.class);
        }

        @Override
        public CsmReference getFunctionDefinition(CsmFunction referencedFunction) {
            for (CsmFunctionDefinitionResolver resolver : res.allInstances()) {
                CsmReference out = resolver.getFunctionDefinition(referencedFunction);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }
        
        @Override
        public Collection<CsmOffsetableDeclaration> findDeclarationByName(CsmProject project, String name) {
            for (CsmFunctionDefinitionResolver resolver : res.allInstances()) {
                Collection<CsmOffsetableDeclaration> out = resolver.findDeclarationByName(project, name);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }
    }    
}
