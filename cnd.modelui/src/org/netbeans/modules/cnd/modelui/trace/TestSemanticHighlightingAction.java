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
package org.netbeans.modules.cnd.modelui.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.highlight.semantic.debug.TestSemanticHighlighting;
import org.netbeans.modules.cnd.highlight.semantic.debug.TestSemanticHighlighting.Highlight;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.text.CloneableEditorSupport;

import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 */
public class TestSemanticHighlightingAction extends TestProjectActionBase{

    public static Action getInstance() {
        return SharedClassObject.findObject(TestSemanticHighlightingAction.class, true);
    }

    @Override
    protected void performAction(Collection<CsmProject> projects) {
        if (projects != null) {
            for (CsmProject p : projects) {
                try {
                    testProject(p);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_TestSemanticHighlighting"); // NOI18N
    }

    private void testProject(CsmProject csmProject) {
        String taskName = "Testing Semantic Highlighting - " + csmProject.getName(); // NOI18N
        InputOutput io = IOProvider.getDefault().getIO(taskName, false); // NOI18N
        io.select();
        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final InterrupterImpl interrupter = new InterrupterImpl();

        final ProgressHandle handle = ProgressHandleFactory.createHandle(taskName, new Cancellable() {
            @Override
            public boolean cancel() {
                interrupter.cancel();
                return true;
            }
        });

        handle.start();

        long time = System.currentTimeMillis();

        if( ! csmProject.isStable(null) ) {
            out.printf("Waiting until the project is parsed"); //NOI18N
            csmProject.waitParse();
        }

        Collection<CsmFile> files = csmProject.getAllFiles();
        handle.switchToDeterminate(files.size());

        int processed = 0;
        for (CsmFile file : files) {
            handle.progress(file.getName().toString(), processed++);
            if (interrupter.cancelled()) {
                break;
            }
            testFile(file, out, err, interrupter);
        }

        handle.finish();
        out.printf("%s%n", interrupter.cancelled() ? "Cancelled" : "Done"); //NOI18N
        out.printf("%s took %d ms%n", taskName, System.currentTimeMillis() - time); // NOI18N

        err.flush();
        out.flush();
        err.close();
        out.close();
    }

    private void testFile(final CsmFile file,
            final OutputWriter out, final OutputWriter err, InterrupterImpl interrupter) {

        long time = System.currentTimeMillis();
        out.printf("%nHighlighting file %s    %s%n", file.getName(), file.getAbsolutePath()); // NOI18N

        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
        Document doc = CsmUtilities.openDocument(ces);

        List<Highlight> highlights = TestSemanticHighlighting.gethighlightsBagForTests(doc, interrupter);

        List<Highlight> sorted = new ArrayList<Highlight>(highlights);
        Collections.sort(sorted, new Comparator<Highlight>() {

            @Override
            public int compare(Highlight o1, Highlight o2) {
                return o1.getStartOffset() - o2.getStartOffset();
            }
        });

        reportHighlights(sorted, out);

        out.printf("Highlighting for file %s took %d ms%n", file.getName(), System.currentTimeMillis() - time); // NOI18N
}

    private void reportHighlights(List<Highlight> highlights, OutputWriter out) {
        int i = 1;
        for (Highlight b : highlights) {
            out.println( "Block " + (i++) + ":\tPosition " +  // NOI18N
                    b.getStartPosition() + "-" +  // NOI18N
                    b.getEndPosition() + "\t" +  // NOI18N
                    b.getType());
        }
    }
}
