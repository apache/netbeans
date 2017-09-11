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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
