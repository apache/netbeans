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

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.refactoring.java.test.Utilities.*;

/**
 * Test that all java-source instances are disposed at the end.
 *
 * @author Pavel Flaska
 */
public class FindSubclassesTest extends RefPerfTestCase {

    public FindSubclassesTest(String name) {
        super(name);
    }

    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        String work = getWorkDirPath();
        System.setProperty("netbeans.user", work);
        projectDir = openProject("SimpleJ2SEApp", getDataDir());
    }

    public void testFindSub()
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

        FileObject testFile = getProjectDir().getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        final CharSequence[] symbolName = new CharSequence[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                TypeMirror mirror = klass.getSuperclass();
                Element object = controller.getTypes().asElement(mirror);
                symbolName[0] = object.getSimpleName();
                wuq[0] = new WhereUsedQuery(Lookups.singleton(TreePathHandle.create(object, controller)));
                ClasspathInfo cpi = RefactoringUtils.getClasspathInfoFor(TreePathHandle.create(object, controller));
                wuq[0].getContext().add(cpi);
            }
        }, false).get();
        
        wuq[0].putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, true);
        wuq[0].putValue(WhereUsedQuery.FIND_REFERENCES, false);

        RefactoringSession rs = RefactoringSession.create("Session");
        wuq[0].prepare(rs);
        rs.doRefactoring(true);
        Collection<RefactoringElement> elems = rs.getRefactoringElements();
        StringBuilder sb = new StringBuilder();
        sb.append("Symbol: '").append(symbolName[0]).append("'");
        sb.append('\n').append("Number of usages: ").append(elems.size()).append('\n');
        try {
            long prepare = getHandler().get("refactoring.prepare");
            NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
            d.name = "refactoring.prepare"+" (" + symbolName[0] + ", usages:" + elems.size() + ")";
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
        
        src = null;
        wuq[0] = null;
        symbolName[0] = null;
        
        System.gc(); System.gc();
        Log.assertInstances("Some instances of parser were not GCed");
    }

    public static Test suite() throws InterruptedException {
//        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().addTest(FindSubclassesTest.class, "testFindSub").gui(false));
        return NbTestSuite.createTest(Noop.class, "noop");
    }
}
