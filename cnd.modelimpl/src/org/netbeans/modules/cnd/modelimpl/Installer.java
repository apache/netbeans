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
package org.netbeans.modules.cnd.modelimpl;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.util.NbBundle;

/**
 * start/stop csm model support.
 *
 */
public final class Installer {
    @OnStart
    public static final class Start implements Runnable {

        @Override
        public void run() {
            CndUtils.assertNonUiThread();
            if (TraceFlags.TRACE_MODEL_STATE) {
                System.err.println("=== Installer.Start"); // NOI18N
            }
            ModelSupport.instance().startup();
        }
    }

    @OnStop
    public static class Stop implements Runnable, Callable<Boolean> {

        @Override
        public void run() {
            CndUtils.assertNonUiThread();
            final Runnable runnable = new RunnableImpl();
            if (CndUtils.isStandalone() || CndUtils.isUnitTestMode() || !ModelSupport.instance().hasOpenedProjects()) {
                runnable.run();
            } else {
                ProgressUtils.showProgressDialogAndRun(runnable, NbBundle.getMessage(Installer.class, "CLOSE_PROJECT_DIALOG_MESSAGE")); //NOI18N
            }
        }

        @Override
        public Boolean call() throws Exception {
            if (TraceFlags.TRACE_MODEL_STATE) {
                System.err.println("=== Installer.AskStop"); // NOI18N
            }
            ModelSupport.instance().notifyClosing();
            return true;
        }

        private static final class RunnableImpl implements Runnable/*, org.openide.util.Cancellable*/ {

            public RunnableImpl() {
            }

            @Override
            public void run() {
                if (TraceFlags.TRACE_MODEL_STATE) {
                    System.err.println("=== Installer.Stop"); // NOI18N
                }
                ModelSupport.instance().shutdown();
            }
        }
    }
}
