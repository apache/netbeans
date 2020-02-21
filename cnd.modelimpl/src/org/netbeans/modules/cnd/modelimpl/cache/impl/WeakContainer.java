/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.cache.impl;

import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public final class WeakContainer<T> {

    private volatile WeakReference<T> weakContainer = TraceFlags.USE_WEAK_MEMORY_CACHE ? new WeakReference<T>(null) : null;
    private int preventMultiplyDiagnosticExceptionsSorage = 0;
    private final CsmValidable stateOwner;
    private final Key storageKey;

    public WeakContainer(CsmValidable stateOwner, Key storageKey) {
        assert storageKey != null;
        this.stateOwner = stateOwner;
        this.storageKey = storageKey;
    }

    public void clear() {
        synchronized (this) {
            if (TraceFlags.USE_WEAK_MEMORY_CACHE) {
                weakContainer.clear();
            }
        }
    }

    public Key getKey() {
        return storageKey;
    }
    
    @SuppressWarnings("unchecked")
    public T getContainer() {
        T container = getFromRef();
        if (container != null) {
            return container;
        }
        synchronized (this) {
            container = getFromRef();
            if (container != null) {
                return container;
            }
            container = (T) RepositoryUtils.get(storageKey);
            if (container == null && stateOwner.isValid() && preventMultiplyDiagnosticExceptionsSorage < DiagnosticExceptoins.LimitMultiplyDiagnosticExceptions) {
                T container2 = (T) RepositoryUtils.get(storageKey);
                String postfix = ""; // NOI18N
                if (CndUtils.isDebugMode() || CndUtils.isUnitTestMode()) {
                    if (container2 == null) {
                        postfix = " TWICE";// NOI18N
                    } else {
                        postfix = " second attempt OK";// NOI18N
                    }
                }
                DiagnosticExceptoins.registerIllegalRepositoryStateException("Failed to get container sorage by key " + postfix, storageKey); // NOI18N
                preventMultiplyDiagnosticExceptionsSorage++;
//            } else{
//                System.err.printf("OK %s\n", storageKey);
            }
            if (TraceFlags.USE_WEAK_MEMORY_CACHE && container != null && weakContainer != null) {
//                WeakReference<T> weak = new WeakReference<T>(container);
//                // assign only when object is completely created
                weakContainer = new WeakReference<>(container);
            }
            return container;
        }
    }

    private T getFromRef() {
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && stateOwner.isValid()) {
            WeakReference<T> weak = weakContainer;
            if (weak != null) {
                return weak.get();
            }
        }
        return null;
    }
}
