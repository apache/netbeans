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

package org.netbeans.core.execution;

import java.security.ProtectionDomain;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PermissionCollection;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Tries to get an IOProtectionDomain from an AccessControlContext.
*
* @author Ales Novak
*/
class AccController {

    /** array of ProtectionDomains */
    static Field context;

    static void init() {
    }

    static Field getContextField() throws Exception {
        if (context == null) {
            Field ctx;
            try {
                ctx = AccessControlContext.class.getDeclaredField("context"); // NOI18N
            } catch (NoSuchFieldException nsfe) { // IBM JDK1.5 has different field
                ctx = AccessControlContext.class.getDeclaredField("domainsArray"); // NOI18N
            }
            // TODO adds --add-opens=java.base/java.security=ALL-UNNAMED
            ctx.setAccessible(true);
            context = ctx;
        }
        return context;
    }


    static ProtectionDomain[] getDomains(AccessControlContext acc) throws Exception {
        Object o = getContextField().get(acc);
        if (o.getClass() == Object[].class) { // 1.2.1 fix
            Object[] array = (Object[]) o;
            ProtectionDomain[] domains = new ProtectionDomain[array.length];
            for (int i = 0; i < array.length; i++) {
                domains[i] = (ProtectionDomain) array[i];
            }
            return domains;
        }
        return (ProtectionDomain[]) o;
    }

    /** @return an IOPermissionCollection or <tt>null</tt> if not found */
    static IOPermissionCollection getIOPermissionCollection() {
        return getIOPermissionCollection(AccessController.getContext());
    }
    
    /** @return an IOPermissionCollection or <tt>null</tt> if not found */
    static IOPermissionCollection getIOPermissionCollection(AccessControlContext acc) {
        try {
            ProtectionDomain[] pds = getDomains(acc);
            PermissionCollection pc;
            for (int i = 0; i < pds.length; i++) {
                pc = pds[i].getPermissions();
                if (pc instanceof IOPermissionCollection) {
                    return (IOPermissionCollection) pc;
                }
            }
            return null;
        } catch (final Exception e) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Logger.getLogger(AccController.class.getName()).log(Level.WARNING, null, e);
                }
            });
            return null;
        }
    }
}
