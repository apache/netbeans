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

package org.netbeans.modules.cnd.api.model.util;

import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.spi.model.UIDProvider;
import org.openide.util.Lookup;

/**
 * Utility class to get Object UID
 */
public final class UIDs {
    private static final UIDProvider provider;
    private static final UIDProvider EMPTY = new SelfUIDProvider();
    static {
        UIDProvider ext = Lookup.getDefault().lookup(UIDProvider.class);
        provider = ext == null ? EMPTY : ext;
    }
    private UIDs() {
    }

    /**
     * returns never-null handler which can be used to restore object
     * @param <T>
     * @param obj object for which handler should be returned
     * @return never-null handler
     */
    public static <T> CsmUID<T> get(T obj) {
        return provider.get(obj);
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl details

    private final static class SelfUIDProvider implements UIDProvider {
        @Override
        public <T> CsmUID<T> get(T obj) {
            return new SelfUID<T>(obj);
        }

        private static final class SelfUID<T> implements CsmUID<T> {

            private final T element;

            SelfUID(T element) {
                assert element != null : "impossible to wrap null object";
                this.element = element;
            }

            @Override
            public T getObject() {
                return this.element;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final SelfUID other = (SelfUID) obj;
                if (this.element != other.element && !this.element.equals(other.element)) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 89 * hash + this.element.hashCode();
                return hash;
            }
        }
    }
}
