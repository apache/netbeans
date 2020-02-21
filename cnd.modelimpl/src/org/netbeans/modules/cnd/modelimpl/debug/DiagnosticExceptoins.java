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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.modelimpl.debug;

import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Allows to get control as soon as an exception occurs
 * in one of the code model threads
 * (parser thread, repository writing thread, code model request processor)
 * 
 * Use for testing purposes only
 * 
 */
public class DiagnosticExceptoins {

    public static final int LimitMultiplyDiagnosticExceptions = 3;

    private DiagnosticExceptoins() {
    }

    public interface Hook {

        /**
         * Is called whenether an exception or error occurs
         * in one of the code model threads
         * (parser thread, repository writing thread,
         * code model request processor)
         */
        void exception(Throwable thr);
    }
    private static Hook hook;

    public static void setHook(Hook aHook) {
        hook = aHook;
    }

    /**
     * This method is called from within catch(...) in code model threads.
     * See Hook.exception description for more details
     */
    public static void register(Throwable thr) {
        CndUtils.printStackTraceOnce(thr);
        Hook aHook = hook;
        if (aHook != null) {
            hook.exception(thr);
        }
    }
    
    public static void registerIllegalRepositoryStateException(String text, Key key) {
        register(new IllegalRepositoryStateException(text, key));
        RepositoryTestUtils.debugDump(key);
    }
    
    public static void registerIllegalRepositoryStateException(String text, CsmUID uid) {
        if (uid instanceof KeyBasedUID) {
            registerIllegalRepositoryStateException(text, ((KeyBasedUID) uid).getKey());
        } else {
            register(new IllegalRepositoryStateException(text, uid));
        }
    }
}
