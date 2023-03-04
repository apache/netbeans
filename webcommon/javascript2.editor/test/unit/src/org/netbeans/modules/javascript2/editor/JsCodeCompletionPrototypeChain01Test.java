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
package org.netbeans.modules.javascript2.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class JsCodeCompletionPrototypeChain01Test extends JsCodeCompletionBase {

    public JsCodeCompletionPrototypeChain01Test(String testName) {
        super(testName);
    }

    public void testSimple01() throws Exception {
        checkCompletion("testfiles/completion/prototypeChain01/issue214556.js", "formatter.print(\"a.say(): \" + a.^help());", false);
    }

    public void testSimple02() throws Exception {
        checkCompletion("testfiles/completion/prototypeChain01/issue214556.js", "formatter.print(\"b.say(): \" + b.^help());", false);
    }

    public void testSimple03() throws Exception {
        checkCompletion("testfiles/completion/prototypeChain01/issue214556.js", "formatter.print(\"c.say(): \" + c.^help());", false);
    }

    public void testFromIndex() throws Exception {
        checkCompletion("testfiles/completion/prototypeChain01/issue214556_test.js", "cc.^toString();", false);
    }

    public void testDocument01() throws Exception {
        checkCompletion("testfiles/completion/prototypeChain01/basicDocumentCC.js", "document.getE^lementById(\"dd\").getElementsByTagName(\"*\").item(2).getFeature(\"pero\").toLocaleString().charCodeAt(10).toExponential();", false);
    }

    public void testDocument02() throws Exception {
        checkCompletion("testfiles/completion/prototypeChain01/basicDocumentCC.js", "document.getElementById(\"dd\").getElementsByTagName(\"*\").item(2).getFeature(\"pero\").toLocale^String().charCodeAt(10).toExponential();", false);
    }

    public void testDocument03() throws Exception {
        checkCompletion("testfiles/completion/prototypeChain01/basicDocumentCC.js", "document.getElementById(\"dd\").getElementsByTagName(\"*\").item(2).getFeature(\"pero\").toLocaleString().charCodeAt(10).toEx^ponential();", false);
    }

    public void testDocument04() throws Exception {
        checkCompletion("testfiles/completion/prototypeChain01/basicDocumentCC.js", "document.createTextNode(\"null\").replaceWholeText(\"flajfda\").le^ngth;", false);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>(3);
        cpRoots.add(ClasspathProviderImplAccessor.getJsStubs().get(0)); // Only use core stubs in unittests
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/prototypeChain01")));
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[0]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }
}
