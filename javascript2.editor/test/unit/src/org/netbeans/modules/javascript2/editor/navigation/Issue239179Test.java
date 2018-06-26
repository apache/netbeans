/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.navigation;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
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
public class Issue239179Test extends JsWithBase{

    public Issue239179Test(String testName) {
        super(testName);
    }
    
    public void testGoTo_01() throws Exception {
        checkDeclaration("/testfiles/navigation/239179/fileB.js", "with(MySpace.cl^s1) {", "fileA.js", 27);
    }
    
    public void testGoTo_02() throws Exception {
        checkDeclaration("/testfiles/navigation/239179/fileB.js", "console.log(first^Name);", "fileA.js", 40);
    }

    public void testGoTo_03() throws Exception {
        checkDeclaration("/testfiles/navigation/239179/fileB.js", "with(MySpace.cl^s2) {", "fileA.js", 82);
    }
    
    public void testGoTo_04() throws Exception {
        checkDeclaration("/testfiles/navigation/239179/fileB.js", "console.log(fo^o);", "fileA.js", 63);
    }
    
    public void testGoTo_05() throws Exception {
        checkDeclaration("/testfiles/navigation/239179/fileB.js", "first^Name;", "fileA.js", 40);
    }
    
    public void testGoTo_06() throws Exception {
        checkDeclaration("/testfiles/navigation/239179/fileB.js", "second^Name;", "fileA.js", 95);
    }
    
    public void testGoTo_07() throws Exception {
        checkDeclaration("/testfiles/navigation/239179/fileB.js", "with(MySpac^e.cls1) {", "fileA.js", 4);
    }
    
    public void testGoTo_08() throws Exception {
        checkDeclaration("/testfiles/navigation/239179/fileB.js", "with(MySpa^ce.cls2) {", "fileA.js", 4);
    }
    
    public void testSemanticFileB() throws Exception {
        checkSemantic("testfiles/navigation/239179/fileB.js");
    }
    
    public void testMarkOccurrences_01() throws Exception {
        checkOccurrences("testfiles/navigation/239179/fileB.js", "firstNa^me;", true);
    }
    
    public void testMarkOccurrences_02() throws Exception {
        checkOccurrences("testfiles/navigation/239179/fileB.js", "with(MySpac^e.cls1) {", true);
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>(ClasspathProviderImplAccessor.getJsStubs());
        
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/navigation/239179")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[cpRoots.size()]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }
}
