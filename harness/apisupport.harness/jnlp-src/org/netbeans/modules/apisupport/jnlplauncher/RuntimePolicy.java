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

package org.netbeans.modules.apisupport.jnlplauncher;

import java.security.*;

/**
 * Policy giving all permissions to all of the code.
 *
 * @author David Strupl
 */
class RuntimePolicy extends Policy {
    /** PermissionCollection with an instance of AllPermission. */
    private static PermissionCollection permissions;
    /** @return initialized set of AllPermissions */
    private static synchronized PermissionCollection getAllPermissionCollection() {
        if (permissions == null) {
            permissions = new Permissions();
            permissions.add(new AllPermission());
            permissions.setReadOnly();
        }
        return permissions;
    }

    public PermissionCollection getPermissions(CodeSource codesource) {
        return getAllPermissionCollection();
    }
    
    public boolean implies(ProtectionDomain domain, Permission permission) {
        return getPermissions(domain.getCodeSource()).implies(permission);
    }
    
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        return getPermissions(domain.getCodeSource());
    }
    
    public void refresh() {
    }
}
