/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.model.jclank.bridge.trace;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.clang.tools.services.ClankProgressHandler;
import org.clang.tools.services.support.PrintWriter_ostream;
import org.llvm.support.raw_ostream;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.model.jclank.bridge.impl.CsmJClankSerivicesImpl;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.OutputWriter;

@ActionID(id = "JClankTracePreprocessorAction", category = "NativeProjectCodeAssistance")
@ActionRegistration(lazy = false, displayName = "#CTL_JClankTracePreprocessorAction")
@ActionReference(path = "NativeProjects/CodeAssistanceActions", position = 31)
@NbBundle.Messages("CTL_JClankTracePreprocessorAction=Preprocess with JClank")
public class JClankTracePreprocessorAction extends JClankTraceProjectAbstractAction {

    @Override
    public final String getName() {
        return NbBundle.getMessage(getClass(), ("CTL_JClankTracePreprocessorAction")); // NOI18N
    }

    @Override
    protected void traceProjects(Collection<NativeProject> projects, OutputWriter out, OutputWriter err,
            ProgressHandle progress, final AtomicBoolean cancelled) {
        raw_ostream llvm_out = new PrintWriter_ostream(out);
        raw_ostream llvm_err = new PrintWriter_ostream(err);
        ClankProgressHandler handle = new JClankProgressHandler(progress);
        try {
            for (NativeProject project : projects) {
                CsmJClankSerivicesImpl.preprocess(Collections.singleton(project),
                        llvm_out, llvm_err, handle, cancelled);
            }
        } finally {
            llvm_out.flush();
            llvm_err.flush();
        }
    }
}
