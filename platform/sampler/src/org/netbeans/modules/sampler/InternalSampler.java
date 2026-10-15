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
package org.netbeans.modules.sampler;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

import static org.netbeans.modules.sampler.Bundle.*;

/**
 *
 * @author Tomas Hurka
 */
final class InternalSampler extends Sampler {
    private static final String SAMPLER_NAME = "selfsampler";  // NOI18N
    private static final String FILE_NAME = SAMPLER_NAME+SamplesOutputStream.FILE_EXT;
    private static final String X_DEBUG_ARG = "-Xdebug"; // NOI18N
    private static final String JDWP_DEBUG_ARG = "-agentlib:jdwp"; // NOI18N
    private static final String JDWP_DEBUG_ARG_PREFIX = "-agentlib:jdwp="; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(InternalSampler.class.getName());
    private static Boolean debugMode;
    private static String lastReason;

    private ProgressHandle progress;

    static InternalSampler createInternalSampler(String key) {
        if (isRunMode()) {
            return new InternalSampler(key);
        }
        return null;
    }

    private static synchronized boolean isDebugged() {
        if (debugMode == null) {
            debugMode = Boolean.FALSE;

            // check if we are debugged
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            List<String> args = runtime.getInputArguments();
            if (args.contains(X_DEBUG_ARG)) {
                debugMode = Boolean.TRUE;
            } else if (args.contains(JDWP_DEBUG_ARG)) {
                debugMode = Boolean.TRUE;
            } else {
                for (String arg : args) {
                    if (arg.startsWith(JDWP_DEBUG_ARG_PREFIX)) {
                        debugMode = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        return debugMode.booleanValue();
    }

    private static boolean isRunMode() {
        boolean runMode = true;
        String reason = null;

        if (isDebugged()) {
            reason = "running in debug mode";   // NOI18N
            runMode = false;
        }
        if (runMode) {
            // check if netbeans is profiled
            try {
                Class.forName("org.netbeans.lib.profiler.server.ProfilerServer", false, ClassLoader.getSystemClassLoader()); // NO18N
                reason = "running under profiler";   // NOI18N
                runMode = false;
            } catch (ClassNotFoundException ex) {
            }
        }
        if (!runMode && !reason.equals(lastReason)) {
            LOGGER.log(Level.INFO, "Slowness detector disabled - {0}", reason); // NOI18N
        }
        lastReason = reason;
        return runMode;
    }
    
    InternalSampler(String thread) throws LinkageError {
        super(thread);
        progress = ProgressHandle.createHandle(Save_Progress());
    }

    @Override
    protected void printStackTrace(Throwable ex) {
        Exceptions.printStackTrace(ex);
    }

    @Override
    @Messages({
        "# {0} - the file",
        "SelfSamplerAction_SavedFile=Snapshot was saved to {0}"
    })
    protected void saveSnapshot(byte[] arr) throws IOException { // save snapshot
        File outFile = Files.createTempFile(SAMPLER_NAME, SamplesOutputStream.FILE_EXT).toFile();
        File userDir = Places.getUserDirectory();
        File gestures = null;
        SelfSampleVFS fs;
        
        outFile = FileUtil.normalizeFile(outFile);
        writeToFile(outFile, arr);
        if (userDir != null) {
            gestures = new File(new File(new File(userDir, "var"), "log"), "uigestures"); // NOI18N
        }
        if (gestures != null && gestures.exists()) {
            fs = new SelfSampleVFS(new String[]{FILE_NAME, SAMPLER_NAME+".log"}, new File[]{outFile, gestures});  // NOI18N
        } else {
            fs = new SelfSampleVFS(new String[]{FILE_NAME}, new File[]{outFile});
        }
        // open snapshot
        FileObject fo = fs.findResource(FILE_NAME);
        // test for DefaultDataObject
        Openable open = fo.getLookup().lookup(Openable.class);
        if (open != null) {
            open.open();
        } else {
            IOException ex = new IOException("Cannot open " + fo + " with MIME type: " + fo.getMIMEType());
            String msg = SelfSamplerAction_SavedFile(outFile.getAbsolutePath());
            Exceptions.attachSeverity(ex, Level.WARNING);
            Exceptions.attachLocalizedMessage(ex, msg);
            Exceptions.printStackTrace(ex);
        }
    }

    private void writeToFile(File file, byte[] arr) {
        try {
            FileOutputStream fstream = new FileOutputStream(file);
            fstream.write(arr);
            fstream.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    ThreadMXBean getThreadMXBean() {
        return ManagementFactory.getThreadMXBean();
    }

    @Override
    @Messages("Save_Progress=Saving snapshot")
    void openProgress(final int steps) {
        if (isDispatchThread()) {
            // log warnining
            return;
        }
        progress.start(steps);
    }

    @Override
    void closeProgress() {
        if (isDispatchThread()) {
            return;
        }
        progress.finish();
        progress = ProgressHandle.createHandle(Save_Progress());
    }

    @Override
    void progress(int i) {
        if (isDispatchThread()) {
            return;
        }
        if (progress != null) {
            progress.progress(i);
        }
    }

    @Override
    boolean isDispatchThread() {
        return EventQueue.isDispatchThread();
    }
}
