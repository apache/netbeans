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

package org.netbeans.modules.cnd.spi.model.services;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmReferenceStorage {
    private static final CsmReferenceStorage DEFAULT = new Default();

    protected CsmReferenceStorage() {
    }

    /** Static method to obtain the Repository.
     * @return the Repository
     */
    public static CsmReferenceStorage getDefault() {
        /*no need for sync synchronized access*/
        return DEFAULT;
    }

    public abstract boolean put(CsmReference ref, CsmObject referencedObject);

    public abstract CsmReference get(CsmOffsetable ref);
    
    /**
     * Implementation of the default selector
     */
    private static final class Default extends CsmReferenceStorage {
        private final Lookup.Result<CsmReferenceStorage> res;
        private static final boolean FIX_SERVICE = true;
        private CsmReferenceStorage fixedStorage;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmReferenceStorage.class);
        }

        private CsmReferenceStorage getService(){
            CsmReferenceStorage service = fixedStorage;
            if (service == null) {
                for (CsmReferenceStorage selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    // I see that it is ugly solution, but NB core cannot fix performance of FolderInstance.waitFinished()
                    // Fixed service gives about 3% performance improvement.
                    // I assume that exactly one service implementor exists.
                    fixedStorage = service;
                }
            }
            return service;
        }

        @Override
        public boolean put(CsmReference ref, CsmObject referencedObject) {
            CsmReferenceStorage storage = getService();
            if (storage != null) {
                return storage.put(ref, referencedObject);
            }
            return false;
        }

        @Override
        public CsmReference get(CsmOffsetable ref) {
            CsmReferenceStorage storage = getService();
            if (storage != null) {
                return storage.get(ref);
            }
            return null;
        }
    }
}
