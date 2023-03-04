/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.refactoring.java.test;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ExecutionException;
import javax.lang.model.element.Element;
import org.netbeans.junit.NbPerformanceTest;
import org.openide.filesystems.FileObject;

import java.io.*;
import javax.lang.model.element.PackageElement;
import junit.framework.Test;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.ui.WhereUsedQueryUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;

/**
 * Test find usages functionality. Measure the usages time.
 * Verifies that all java-source instances are disposed at the end.
 * 
 * @author Pavel Flaska
 */
public class FindUsagesPerfTest extends RefPerfTestCase {

    public FindUsagesPerfTest(String name) {
        super(name);
    }

    public void testFindUsage()
            throws IOException, InterruptedException, ExecutionException {
        // logging is used to obtain data about consumed time
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(getHandler());

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(getHandler());

        Log.enableInstances(Logger.getLogger("TIMER"), "JavacParser", Level.FINEST);

        FileObject testFile = getProjectDir().getFileObject("/src/org/gjt/sp/jedit/jEdit.java");

        JavaSource src = JavaSource.forFileObject(testFile);

        // find usages of symbols collected below
        final List<TreePathHandle> handle = new ArrayList<TreePathHandle>();

        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                PackageElement pckg = controller.getElements().getPackageElement("org.gjt.sp.jedit");
                for (final Element element : pckg.getEnclosedElements()) {
                    handle.add(TreePathHandle.create(element, controller));
                }
            }
        }, false).get();

        // do find usages query
        for (final TreePathHandle element : handle) {

            src.runWhenScanFinished(new Task<CompilationController>() {

                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.RESOLVED);

                    final RefactoringUI ui = WhereUsedQueryUI.factory().create(controller, new TreePathHandle[]{element}, null, new NonRecursiveFolder[0]);
                    ui.getPanel(null);
                    try {
                        ui.setParameters();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final AbstractRefactoring wuq = ui.getRefactoring();
                    RefactoringSession rs = RefactoringSession.create("Session");
                    wuq.prepare(rs);
                    rs.doRefactoring(false);
                    Collection<RefactoringElement> elems = rs.getRefactoringElements();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Symbol: '").append(element.resolveElement(controller).getSimpleName()).append("'");
                    sb.append('\n').append("Number of usages: ").append(elems.size()).append('\n');
                    try {
                        long prepare = getHandler().get("refactoring.prepare");
                        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
                        d.name = "refactoring.prepare"+" ("+element.resolveElement(controller).getSimpleName()+", usages:"+elems.size()+")";
                        d.value = prepare;
                        d.unit = "ms";
                        d.runOrder = 0;
                        sb.append("Prepare phase: ").append(prepare).append(" ms.\n");
                        Utilities.processUnitTestsResults(FindUsagesPerfTest.class.getCanonicalName(), d);
                    } catch (Exception ex) {
                        sb.append("Cannot collect usages: ").append(ex.getCause());
                    }
                    getLog().append(sb);
                    System.err.println(sb);

                }
            }, false).get();
            System.gc(); System.gc();
        }
        src = null;
        Log.assertInstances("Some instances of parser were not GCed");
    }
    
    public static Test suite() throws InterruptedException {
//        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().addTest(FindUsagesPerfTest.class, "testFindUsage").gui(false));
        return NbTestSuite.createTest(Noop.class, "noop");
    }

}
