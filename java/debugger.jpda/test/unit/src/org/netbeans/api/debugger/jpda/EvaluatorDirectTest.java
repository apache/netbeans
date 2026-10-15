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

package org.netbeans.api.debugger.jpda;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * Tests evaluation of various expressions.
 */
public class EvaluatorDirectTest extends NbTestCase {
    
    private JPDASupport     support;

    public EvaluatorDirectTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(EvaluatorDirectTest.class);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
//        //PreferredCCParser is using SourceUtils.isScanInProgress() to modify behavior; ensure indexing is not running.
        FileObject prjRoot = FileUtil.toFileObject(new File(System.getProperty("test.dir.src")));
        assertNotNull(prjRoot);
        Project prj = FileOwnerQuery.getOwner(prjRoot);
        assertNotNull(prj);
        Project annotationsPrj = FileOwnerQuery.getOwner(prj.getProjectDirectory().getParent().getParent().getFileObject("platform/api.annotations.common"));
        assertNotNull(annotationsPrj);
        OpenProjects.getDefault().open(new Project[] {annotationsPrj}, false);
        JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY)).runWhenScanFinished(p -> {}, true).get();
        System.setProperty("debugger.evaluator2", "true");
        JPDASupport.removeAllBreakpoints ();
    }

    public void testEvaluate() throws Exception {
        SpecificationVersion javaVersion = new SpecificationVersion(System.getProperty("java.specification.version"));
        SpecificationVersion version17 = new SpecificationVersion("17");
        if (javaVersion.compareTo(version17) < 0) {
            return ; //don't run on JDK < 17
        }
        String code = "public class Test {\n" +
                      "    public static void main(String... args) {\n" +
                      "        System.err.println(\"STARTED!!!\");\n" +
                      "        Object o = \"Hello!\";\n" +
                      "        if (o instanceof String s) {\n" +
                      "            System.err.println(\"BEFORE BREAKPOINT\");\n" +
                      "            System.err.println(s); //LBREAKPOINT\n" +
                      "            System.err.println(\"AFTER BREAKPOINT\");\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        clearWorkDir();
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        assertNotNull(wd);
        FileObject source = wd.createData("Test.java");
        try (OutputStream out = source.getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write(code);
        }
        Utils.BreakPositions bp = Utils.getBreakPositions(source.toURL());
        LineBreakpoint lb = bp.getLineBreakpoints().get(0);
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        support = JPDASupport.attach (
            new String[0],
            FileUtil.toFile(source).getAbsolutePath(),
            new String[0],
            new File[0]
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
        Variable value = support.getDebugger ().evaluate("s");
        assertEquals("\"Hello!\"", value.getValue());
    }

}
