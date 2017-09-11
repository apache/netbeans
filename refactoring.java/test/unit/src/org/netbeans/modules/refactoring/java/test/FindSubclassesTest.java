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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().addTest(FindSubclassesTest.class, "testFindSub").gui(false));
    }
}
