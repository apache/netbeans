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

// XXX is this really an appropriate package? Perhaps move to e.g. org.netbeans.jnlplauncher.
package org.netbeans.modules.apisupport.jnlplauncher;

import java.io.File;
import java.security.Policy;

/** The JNLP entry point. Does not do much, in future it can do more
 * of JNLP related stuff.
 *
 * @author Jaroslav Tulach
 */
public class Main extends Object {

    /** Starts NetBeans
     * @param args the command line arguments
     * @throws Exception for lots of reasons
     */
    public static void main (String args[]) throws Exception {
        fixPolicy();
        fixNetBeansUser();
        org.netbeans.Main.main(args);
    }
    
    /** Fixes value of netbeans.user property.
     */
    final static void fixNetBeansUser() {
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir == null) {
            userDir = System.getProperty("jnlp.netbeans.user"); // NOI18N
        }
        if (userDir == null) {
            return;
        }
        final String PREFIX = "${user.home}/"; // NOI18N
        int uh = userDir.indexOf(PREFIX);
        if (uh == -1) {
            return;
        }
        String newDir = 
            userDir.substring(0, uh) + 
            System.getProperty("user.home") + // NOI18N
            File.separator + 
            userDir.substring(uh + PREFIX.length()); 
        System.setProperty("netbeans.user", newDir); // NOI18N
    }
    
    /**
     * Attempt to give the rest of NetBeans all the
     * permissions. The jars besides the one containing this class
     * don't have to be signed with this.
     */
    final static void fixPolicy() {
        if (Boolean.getBoolean("netbeans.jnlp.fixPolicy")) { // NOI18N
            // Grant all the code all persmission
            Policy.setPolicy(new RuntimePolicy());
            // Replace the security manager by a fresh copy
            // that does the delegation to the permissions system
            // -- just to make sure that there is nothing left
            //    from the JWS
            System.setSecurityManager(new SecurityManager());
        }
    }
}
