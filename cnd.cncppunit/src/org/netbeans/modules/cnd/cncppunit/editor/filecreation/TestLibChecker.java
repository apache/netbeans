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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
