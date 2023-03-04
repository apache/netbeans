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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import junit.framework.Test;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 * Test that all java-source instances are disposed at the end.
 *
 * @author Pavel Flaska
 */
public class MoveClassPerfTest extends RefPerfTestCase {

    public MoveClassPerfTest(String name) {
        super(name);
    }

    public void testMoveActionSet()
            throws IOException, InterruptedException, ExecutionException {
        // logging is used to obtain data about consumed time
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(getHandler());

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(getHandler());
        
        // check that no javac created during refactoring is left in the memory
        Log.enableInstances(Logger.getLogger("TIMER"), "JavacParser", Level.FINEST);
        
        final FileObject testFile = getProjectDir().getFileObject("/src/org/gjt/sp/jedit/ActionSet.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final MoveRefactoring[] moveRef = new MoveRefactoring[1];
        final CharSequence REFACTORED_OBJ = "org.gjt.sp.jedit.ActionSet";

        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement(REFACTORED_OBJ);
                moveRef[0] = new MoveRefactoring(Lookups.singleton(testFile));
                ClasspathInfo cpi = RefactoringUtils.getClasspathInfoFor(TreePathHandle.create(klass, controller));
                moveRef[0].getContext().add(cpi);
            }
        }, false).get();
        RefactoringSession rs = RefactoringSession.create("Session");

        File f = FileUtil.toFile(getProjectDir().getFileObject("/src/org/gjt/sp"));
        moveRef[0].setTarget(Lookups.singleton(f.toURI().toURL()));
        moveRef[0].prepare(rs);
        rs.doRefactoring(true);
        Collection<RefactoringElement> elems = rs.getRefactoringElements();
        StringBuilder sb = new StringBuilder();
        sb.append("Symbol: '").append(REFACTORED_OBJ).append("'");
        sb.append('\n').append("Number of usages: ").append(elems.size()).append('\n');
        try {
            long prepare = getHandler().get("refactoring.prepare");
            NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
            d.name = "refactoring.prepare"+" (" + REFACTORED_OBJ + ", usages:" + elems.size() + ")";
            d.value = prepare;
            d.unit = "ms";
            d.runOrder = 0;
            sb.append("Prepare phase: ").append(prepare).append(" ms.\n");
            Utilities.processUnitTestsResults(MoveClassPerfTest.class.getCanonicalName(), d);
            System.err.println("Time: " + prepare);
        } catch (Exception ex) {
            sb.append("Cannot collect usages: ").append(ex.getCause());
        }
        getLog().append(sb);
        System.err.println(sb);

        src = null;
        moveRef[0] = null;
        System.gc(); System.gc();
        
        Log.assertInstances("Some instances of parser were not GCed");
    }

    public static Test suite() throws InterruptedException {
//        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().addTest(MoveClassPerfTest.class, "testMoveActionSet").gui(false));
        return NbTestSuite.createTest(Noop.class, "noop");
    }
}
