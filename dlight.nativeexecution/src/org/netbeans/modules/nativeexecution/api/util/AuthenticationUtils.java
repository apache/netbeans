/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author in220748
 */
public final class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static void usePasswordAuthenticationFor(ExecutionEnvironment env) {
        Authentication auth = Authentication.getFor(env);
        auth.setPassword();
        auth.apply();
    }

    public static void useSSHKeyAuthenticationFor(ExecutionEnvironment env, String sshKeyFile) {
        Authentication auth = Authentication.getFor(env);
        auth.setSSHKeyFile(sshKeyFile);
        auth.apply();
    }
    
    /**
     * Returns ssh key file name for the specified ExecutionEnvironment or null if it is not of SSH_KEY auth type
     */
    public static String getSSHKeyFileFor(ExecutionEnvironment env) {
        Authentication auth = Authentication.getFor(env);
        if (auth.getType() == Authentication.Type.SSH_KEY) {
            String file = auth.getSSHKeyFile();
            if (file == null) {
                file = "";
            }
            return file;
        } else {
            return null;
        }
    }
    
    public static void changeAuth(ExecutionEnvironment env, Authentication auth) {
        ConnectionManagerAccessor.getDefault().changeAuth(env, auth);
    }
}
