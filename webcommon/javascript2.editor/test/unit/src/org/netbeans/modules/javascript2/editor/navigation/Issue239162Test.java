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
package org.netbeans.modules.javascript2.editor.navigation;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.editor.JsWithBase;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class Issue239162Test extends JsWithBase{

    public Issue239162Test(String testName) {
        super(testName);
    }
    
    public void testGoTo_01() throws Exception {
        checkDeclaration("/testfiles/navigation/239162/fileB.js", "with(cl^s1) {", "fileA.js", 4);
    }
    
    public void testGoTo_02() throws Exception {
        checkDeclaration("/testfiles/navigation/239162/fileB.js", "console.log(first^Name);", "fileA.js", 17);
    }

    public void testGoTo_03() throws Exception {
        checkDeclaration("/testfiles/navigation/239162/fileB.js", "    with(cl^s2) {", "fileA.js", 55);
    }
    
    public void testGoTo_04() throws Exception {
        checkDeclaration("/testfiles/navigation/239162/fileB.js", "console.log(fo^o);", "fileA.js", 40);
    }
    
    public void testGoTo_05() throws Exception {
        checkDeclaration("/testfiles/navigation/239162/fileB.js", "first^Name;", "fileA.js", 17);
    }
    
    public void testGoTo_06() throws Exception {
        checkDeclaration("/testfiles/navigation/239162/fileB.js", "second^Name;", "fileA.js", 68);
    }
    
    public void testSemanticFileB() throws Exception {
        checkSemantic("testfiles/navigation/239162/fileB.js");
    }
    
    public void testMarkOccurrences_01() throws Exception {
        checkOccurrences("testfiles/navigation/239162/fileB.js", "firstNa^me;", true);
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<>(ClasspathProviderImplAccessor.getJsStubs());
        
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/navigation/239162")));
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
