/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.spi.model.services.CsmOverloadingResolverImplementation;
import org.openide.util.Lookup;

/**
 *
 */
public final class CsmOverloadingResolver {
    
    /**
     * Determines what method is the best match for the given instantiation descriptor and paramTypes
     * 
     * @param methods
     * @param instantiationDescriptor
     * @param paramTypes
     * 
     * @return best matches
     */
    public static Collection<CsmFunction> resolveOverloading(Collection<CsmFunction> methods, CharSequence instantiationDescriptor, Map<CsmFunction, List<CsmType>> paramTypes) {
        return DEFAULT.resolveOverloading(methods, instantiationDescriptor, paramTypes);
    }
    
//<editor-fold defaultstate="collapsed" desc="Implementation">
    
    private static final CsmOverloadingResolverImplementation DEFAULT = new Default();
    
    private CsmOverloadingResolver() {
        throw new AssertionError("Not instantiable"); // NOI18N
    }
    
    /**
     * Default implementation (just a proxy to a real service)
     */
    private static final class Default implements CsmOverloadingResolverImplementation {
        
        private final Lookup.Result<CsmOverloadingResolverImplementation> res;
        
        private CsmOverloadingResolverImplementation delegate;
        
        
        private Default() {
            res = Lookup.getDefault().lookupResult(CsmOverloadingResolverImplementation.class);
        }
        
        private CsmOverloadingResolverImplementation getDelegate(){
            CsmOverloadingResolverImplementation service = delegate;
            if (service == null) {
                for (CsmOverloadingResolverImplementation resolver : res.allInstances()) {
                    service = resolver;
                    break;
                }
                delegate = service;
            }
            return service;
        }
        
        @Override
        public Collection<CsmFunction> resolveOverloading(Collection<CsmFunction> methods, CharSequence instantiationDescriptor, Map<CsmFunction, List<CsmType>> paramTypes) {
            return getDelegate().resolveOverloading(methods, instantiationDescriptor, paramTypes);
        }
    }
//</editor-fold>    
}
