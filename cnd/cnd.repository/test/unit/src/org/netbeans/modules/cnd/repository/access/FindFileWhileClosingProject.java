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
package org.netbeans.modules.cnd.repository.access;

import java.io.File;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.trace.*;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Test for accessing project while it is being closed
 * (iz #115491, http://www.netbeans.org/issues/show_bug.cgi?id=115491)
 */
public class FindFileWhileClosingProject extends RepositoryAccessTestBase {

    private final static boolean verbose;


    static {
        verbose = true; // Boolean.getBoolean("test.get.file.while.closing.project.verbose");
        if (verbose) {
            System.setProperty("cnd.modelimpl.timing", "true");
            System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
            System.setProperty("cnd.repository.listener.trace", "true");
            System.setProperty("cnd.trace.close.project", "true");
            System.setProperty("cnd.repository.workaround.nulldata", "true");
        }
    }

    public FindFileWhileClosingProject(String testName) {
        super(testName);
    }

    public void testGetFileWhileClosingProject() throws Exception {

        File projectRoot = getDataFile("quote_nosyshdr");

        int count = Integer.getInteger("test.get.file.while.closing.project.laps", 500);

        final TraceModelBase traceModel = new TraceModelBase(true);
        traceModel.processArguments(projectRoot.getAbsolutePath());
        ModelImpl model = traceModel.getModel();

        List<String> files = new ArrayList<String>(traceModel.getFiles().size());
        for (File file : traceModel.getFiles()) {
            files.add(file.getAbsolutePath());
        }

        for (int i = 0; i < count; i++) {

            System.err.printf("%s: processing project %s. Pass %d \n", getBriefClassName(), projectRoot.getAbsolutePath(), i);

            final CsmProject project = traceModel.getProject();
            project.waitParse();
            RequestProcessor.Task task = model.enqueueModelTask(new Runnable() {

                @Override
                public void run() {
                    TraceModelBase.closeProject(project);
                }
            }, "Closing Project " + i); //NOI18N
            for (String path : files) {
                try {
                    CsmFile csmFile = project.findFile(path, true, false);
                    if (verbose) {
                        System.err.printf("\tfind %s -> %s \n", path, csmFile);
                    }
                } catch (Throwable e) {
                    registerException(e);
                }
                assertNoExceptions();
            }
            if (verbose) {
                System.err.printf("Waiting util close task finishes...\n");
            }
            task.waitFinished();
            if (verbose) {
                System.err.printf("\tClose task has finished.\n");
            }
            assertNoExceptions();
            traceModel.resetProject();
        }
        assertNoExceptions();
    }
}
