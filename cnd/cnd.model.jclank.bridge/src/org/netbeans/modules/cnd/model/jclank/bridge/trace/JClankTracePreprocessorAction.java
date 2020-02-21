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
