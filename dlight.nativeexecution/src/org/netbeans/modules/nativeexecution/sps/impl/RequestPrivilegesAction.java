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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.sps.impl;

import java.security.acl.NotOwnerException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport;
import org.netbeans.modules.nativeexecution.support.ObservableAction;
import org.openide.util.NbBundle;

public final class RequestPrivilegesAction
        extends ObservableAction<Boolean> {

    private final static ConcurrentHashMap<Key, RequestPrivilegesAction> cache =
            new ConcurrentHashMap<>();
    private final SPSCommonImpl support;
    private final Collection<String> requestedPrivileges;

    public static RequestPrivilegesAction getInstance(SPSCommonImpl support, Collection<String> requestedPrivileges) {
        Key key = new Key(support, requestedPrivileges);
        RequestPrivilegesAction result = cache.get(key);

        if (result == null) {
            result = new RequestPrivilegesAction(support, requestedPrivileges);
            RequestPrivilegesAction oldRef = cache.putIfAbsent(key, result);
            if (oldRef != null) {
                result = oldRef;
            }
        }
        return result;
    }

    private RequestPrivilegesAction(
            final SPSCommonImpl support,
            final Collection<String> requestedPrivileges) {

        super(loc("TaskPrivilegesSupport_GrantPrivileges_Action")); // NOI18N
        this.support = support;
        this.requestedPrivileges = requestedPrivileges;
    }

    @Override
    public synchronized Boolean performAction() {
        if (support.isCanceled()) {
            return Boolean.FALSE;
        }

        try {
            support.requestPrivileges(requestedPrivileges, true);
        } catch (CancellationException ex) {
            return Boolean.FALSE; // TODO:CancellationException error processing
        } catch (NotOwnerException ex) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(RequestPrivilegesAction.class, key, params);
    }

    private static class Key {

        SolarisPrivilegesSupport support;
        Collection<String> requestedPrivileges;

        public Key(SolarisPrivilegesSupport support, Collection<String> requestedPrivileges) {
            this.support = support;
            this.requestedPrivileges = requestedPrivileges;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                throw new IllegalArgumentException();
            }
            Key k = (Key) obj;
            return k.requestedPrivileges.containsAll(requestedPrivileges) &&
                    requestedPrivileges.containsAll(k.requestedPrivileges) &&
                    k.support == support;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.support != null ? this.support.hashCode() : 0);
            return hash;
        }
    }
}
