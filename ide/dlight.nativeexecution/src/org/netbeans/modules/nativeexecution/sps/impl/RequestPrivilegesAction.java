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

    private static final ConcurrentHashMap<Key, RequestPrivilegesAction> cache =
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
