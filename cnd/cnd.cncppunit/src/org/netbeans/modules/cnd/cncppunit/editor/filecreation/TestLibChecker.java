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

package org.netbeans.modules.cnd.cncppunit.editor.filecreation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.cncppunit.LibraryChecker;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 */
/*package*/ class TestLibChecker implements Runnable {

    private static final RequestProcessor RP =
            new RequestProcessor(TestLibChecker.class.getSimpleName(), 3);

    private static final Map<TestLibChecker, RequestProcessor.Task> CHECKERS =
            new HashMap<TestLibChecker, RequestProcessor.Task>();

    private final String lib;
    private final AbstractCompiler compiler;
    private ChangeListener listener;

    private TestLibChecker(String lib, AbstractCompiler compiler, ChangeListener listener) {
        this.lib = lib;
        this.compiler = compiler;
        this.listener = listener;
    }

    @Override
    public void run() {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(TestLibChecker.class, "MSG_Checking_Library", lib, compiler.getExecutionEnvironment())); // NOI18N
        progressHandle.start();
        boolean result = false;
        try {
            result = LibraryChecker.isLibraryAvailable(lib, compiler);
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        } finally {
            progressHandle.finish();
            synchronized (CHECKERS) {
                CHECKERS.remove(this);
                if (listener != null) {
                    listener.stateChanged(new LibCheckerChangeEvent(this, result));
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TestLibChecker)) {
            return false;
        }
        final TestLibChecker that = (TestLibChecker) obj;
        return this.lib.equals(that.lib)
                && this.compiler == that.compiler;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + lib.hashCode();
        hash = 67 * hash + compiler.hashCode();
        return hash;
    }

    private static MakeConfiguration getDefaultConfiguration(Project project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        return (MakeConfiguration) cdp.getConfigurationDescriptor().getConfs().getActive();
    }

    private static CompilerSet getCompilerSet(Project project) {
        MakeConfiguration makeConfiguration = getDefaultConfiguration(project);
        return makeConfiguration == null? null : makeConfiguration.getCompilerSet().getCompilerSet();
    }

    /*package*/ static AbstractCompiler getCCompiler(Project project) {
        CompilerSet compilerSet = getCompilerSet(project);
        return compilerSet == null? null : (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
    }

    /*package*/ static AbstractCompiler getCppCompiler(Project project) {
        CompilerSet compilerSet = getCompilerSet(project);
        return compilerSet == null? null : (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
    }

    /*package*/ static ExecutionEnvironment getExecutionEnvironment(Project project) {
        MakeConfiguration makeConfiguration = getDefaultConfiguration(project);
        return makeConfiguration == null? null : makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
    }

    /*package*/ static RequestProcessor.Task asyncCheck(String lib, AbstractCompiler compiler, ChangeListener listener) {
        Parameters.notWhitespace("lib", lib); // NOI18N
        Parameters.notNull("compiler", compiler); // NOI18N
        synchronized (CHECKERS) {
            TestLibChecker checker = findChecker(lib, compiler);
            if (checker == null) {
                checker = new TestLibChecker(lib, compiler, listener);
                RequestProcessor.Task task = RP.post(checker);
                CHECKERS.put(checker, task);
                return task;
            } else {
                checker.listener = listener;
                return CHECKERS.get(checker);
            }
        }
    }

    // call only inside synchronized (CHECKERS)
    private static TestLibChecker findChecker(String lib, AbstractCompiler compiler) {
        for (TestLibChecker checker : CHECKERS.keySet()) {
            // comparison here must be consistent with equals()
            if (checker.lib.equals(lib) && checker.compiler == compiler) {
                return checker;
            }
        }
        return null;
    }

    public static final class LibCheckerChangeEvent extends ChangeEvent {

        private static final long serialVersionUID = -7102294055630832274L;

        private final boolean result;

        public LibCheckerChangeEvent(Object source, boolean result) {
            super(source);
            this.result = result;
        }

        public boolean getResult() {
            return result;
        }
    }
}
