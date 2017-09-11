/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import static org.netbeans.modules.nativeexecution.api.util.ProcessUtils.getReader;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author vkvashin
 */
public class StreamsHangupTestCase extends NativeExecutionBaseTestCase {

    static {
      System.setProperty("nativeexecution.support.logger.level", "0");
    }

    private String remoteScriptPath = null;
    private static final String scriptName = "err_and_out.sh";
    private final String prefix;
    private String remoteTmpDir = null;

    public StreamsHangupTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
        prefix = "[" + testExecutionEnvironment + "] ";
    }

    private class DelegateImpl implements StreamsHangup.Delegate {

        private volatile File scriptFile;

        @Override
        public void setup(File scriptFile) throws Exception {
            this.scriptFile = scriptFile;
            ExecutionEnvironment env = getTestExecutionEnvironment();
            if (env.isLocal()) {
                scriptFile.setExecutable(true); // just in case
                remoteScriptPath = scriptFile.getAbsolutePath();
                remoteTmpDir = File.createTempFile(getClass().getSimpleName(), ".dat").getAbsolutePath();
            } else {
                ConnectionManager.getInstance().connectTo(env);
                remoteTmpDir = createRemoteTmpDir();
                int rc = CommonTasksSupport.rmDir(env, remoteTmpDir, true, new PrintWriter(System.err)).get();
                remoteScriptPath = remoteTmpDir + "/" + scriptFile.getName();
                CommonTasksSupport.UploadStatus res = CommonTasksSupport.uploadFile(scriptFile, env, remoteScriptPath, 0777, true).get();
                assertEquals("Error uploading file " + scriptFile.getAbsolutePath() + " to " + getTestExecutionEnvironment() + ":" + remoteScriptPath, 0, rc);
            }
        }


        @Override
        public Process createProcess(String... arguments) throws IOException {
            assertNotNull(remoteScriptPath);
            final ExecutionEnvironment env = getTestExecutionEnvironment();
            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
            pb.setExecutable(remoteScriptPath);
            pb.setArguments(arguments);
            NativeProcess process = pb.call();
            return process;
        }

        @Override
        public void cleanup() throws Exception {
            if (remoteTmpDir != null) {
                ExecutionEnvironment env = getTestExecutionEnvironment();
                if (env.isRemote()) { // in the case of local, we don't copy anything
                    CommonTasksSupport.rmDir(env, remoteTmpDir, true, new PrintWriter(System.err)).get();
                }
            }
        }

        @Override
        public String getScriptName() {
            return scriptName;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public void kill(Process process) throws IOException {
            final ExecutionEnvironment env = getTestExecutionEnvironment();
            int pid = ((NativeProcess) process).getPID();
            ProcessUtils.execute(env, "kill", "-9", "" + pid);        }
    }


    @ForAllEnvironments(section = "remote.platforms")
    public void testHangup() throws Exception {
        new StreamsHangup(new DelegateImpl()).test();
    }
    
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(StreamsHangupTestCase.class);
    }
}
