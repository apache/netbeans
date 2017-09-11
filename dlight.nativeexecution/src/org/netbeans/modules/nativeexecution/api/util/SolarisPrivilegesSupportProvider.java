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
package org.netbeans.modules.nativeexecution.api.util;

import java.security.SignatureException;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.sps.impl.SPSLocalImpl;
import org.netbeans.modules.nativeexecution.sps.impl.SPSRemoteImpl;
import org.netbeans.modules.nativeexecution.support.Logger;

public final class SolarisPrivilegesSupportProvider {

    private static final ConcurrentHashMap<ExecutionEnvironment, SolarisPrivilegesSupport> instances =
            new ConcurrentHashMap<>();

    private SolarisPrivilegesSupportProvider() {
    }

    public static SolarisPrivilegesSupport getSupportFor(ExecutionEnvironment execEnv) {
        SolarisPrivilegesSupport result = instances.get(execEnv);

        if (result == null) {
            if (execEnv.isLocal()) {
                try {
                    result = SPSLocalImpl.getNewInstance(execEnv);
                } catch (SignatureException ex) {
                    Logger.getInstance().log(Level.SEVERE, "Resource signature is wrong: {0}", ex.getMessage()); // NOI18N
                } catch (MissingResourceException ex) {
                    Logger.getInstance().log(Level.SEVERE, "Resource not found: {0}", ex.getMessage()); // NOI18N
                }
            } else {
                result = SPSRemoteImpl.getNewInstance(execEnv);
            }

            if (result != null) {
                SolarisPrivilegesSupport oldRef = instances.putIfAbsent(execEnv, result);

                if (oldRef != null) {
                    result = oldRef;
                }
            }
        }

        return result;
    }
}
