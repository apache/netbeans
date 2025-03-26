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
package org.netbeans.modules.debugger.jpda.truffle;

import java.io.File;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.source.SourceBinaryTranslator;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public abstract class JPDATestCase extends NbTestCase {

    protected final File sourceRoot = new File(System.getProperty("test.dir.src"));

    protected static Test createSuite(Class<? extends TestCase> clazz) {
        return NbModuleSuite.createConfiguration(clazz).
                gui(false).
                failOnException(Level.INFO).
                enableClasspathModules(false). 
                clusters(".*").
                // TODO remove once polyglot tests can run on JDK 17+ and uncomment the other line again
                enableModules("(?!org.netbeans.modules.languages.hcl|org.netbeans.lib.nbjshell9|org.netbeans.lib.nbjshell|org.netbeans.libs.graalsdk.system).*").hideExtraModules(true).
//                enableModules("org.netbeans.libs.nbjavacapi").
                suite();
    }

    public JPDATestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        JPDASupport.removeAllBreakpoints();
        super.setUp();
    }

    protected String getBinariesPath(String sourcesPath) {
        return Utilities.toFile(SourceBinaryTranslator.source2Binary(FileUtil.toFileObject(new File(sourcesPath)))).getAbsolutePath();
    }

    protected final File getScriptSourceFile(String scriptName) {
        String scriptPath = "org/netbeans/modules/debugger/jpda/truffle/scripts/" + scriptName;
        scriptPath = scriptPath.replace('/', File.separatorChar);
        return new File(sourceRoot, scriptPath);
    }

    protected final File getJavaSourceFile(String javaFileName) {
        String sourcePath = "org/netbeans/modules/debugger/jpda/truffle/testapps/" + javaFileName;
        sourcePath = sourcePath.replace('/', File.separatorChar);
        return new File(sourceRoot, sourcePath);
    }

    protected final void runScriptUnderJPDA(String launcher, String scriptPath, ThrowableConsumer<JPDASupport> supportConsumer) throws Exception {
        assertTrue("'"+launcher+"' launcher not available", JPDASupport.isLauncherAvailable(launcher));
        // Translate script path from source dir to target dir:
        scriptPath = getBinariesPath(scriptPath);
        JPDASupport support = JPDASupport.attachScript(launcher, scriptPath);
        run(support, supportConsumer, false);
    }

    protected final void runJavaUnderJPDA(String mainClass, ThrowableConsumer<JPDASupport> supportConsumer) throws Exception {
        JPDASupport support = JPDASupport.attach(mainClass);
        run(support, supportConsumer, false);
    }

    private void run(JPDASupport support, ThrowableConsumer<JPDASupport> supportConsumer, boolean resume) throws Exception {
        try {
            support.waitState(JPDADebugger.STATE_STOPPED);
            if (resume) {
                support.doContinue();
                support.waitState(JPDADebugger.STATE_STOPPED);
            }
            supportConsumer.accept(support);
            support.waitState(JPDADebugger.STATE_DISCONNECTED);
        } catch (Throwable t) {
            // Report any exception just in case the finally block does not finish cleanly
            t.printStackTrace();
            throw t;
        } finally {
            support.doFinish();
        }
    }

    protected TruffleStackFrame checkStoppedAtScript(JPDAThread thread, String sourcePath, int line) {
        assertNotNull("JPDAThread was null while testing (JVM crash?): "+sourcePath, thread);
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(thread);
        assertNotNull("Missing CurrentPCInfo, suspended at " + thread.getClassName() + "." + thread.getMethodName(), currentPCInfo);
        TruffleStackFrame topFrame = currentPCInfo.getTopFrame();
        assertNotNull("No top frame", topFrame);
        SourcePosition sourcePosition = topFrame.getSourcePosition();
        if (sourcePath != null && new File(sourcePath).isAbsolute()) {
            assertEquals("Bad source", getBinariesPath(sourcePath), sourcePosition.getSource().getPath());
        }
        assertEquals("Bad line", line, sourcePosition.getStartLine());
        return topFrame;
    }

    protected static TruffleVariable findVariable(TruffleScope scope, String name) {
        for (TruffleVariable var : scope.getVariables()) {
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }

    @FunctionalInterface
    protected interface ThrowableConsumer<T> {

        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    protected interface ThrowableBiConsumer<T, U> {

        void accept(T t, U u) throws Exception;
    }
}
