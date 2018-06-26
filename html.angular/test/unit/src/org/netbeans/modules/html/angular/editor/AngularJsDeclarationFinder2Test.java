/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.angular.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DeclarationFinder;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.angular.TestProjectSupport;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class AngularJsDeclarationFinder2Test extends JsCodeCompletionBase {

    public AngularJsDeclarationFinder2Test(String testName) throws Exception {
        super(testName);
    }

    static private boolean isSetup = false;
    
    @Override
    protected void setUp() throws Exception {
        if (!isSetup) {
            // only for the first run index all sources
            super.setUp(); 
            isSetup = true;
        }
        FileObject folder = getTestFile("angularTestProject");
        Project tp = new TestProjectSupport.TestProject(folder, null);
        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(tp));
        MockLookup.setInstances(lookupAll.toArray());
    }

    public void testIssue243888_01() throws Exception {
        checkDeclaration("angularTestProject/public_html/issue243888.html", "<h1>{{na^me}}</h1>{{lastName}}", "controllers243888.js", 56);
    }
    
    public void testIssue243888_02() throws Exception {
        checkDeclaration("angularTestProject/public_html/issue243888.html", "Name: <input type=\"text\" ng-model=\"na^me\"/>", "controllers243888.js", 56);
    }
    
    public void testIssue243888_03() throws Exception {
        checkDeclaration("angularTestProject/public_html/issue243888.html", "<li>[ <a href=\"\" ng-dblclick=\"\" ng-click=\"addC^ontact()\">add</a> ]</li>", "controllers243888.js", 316);
    }
    
    public void testIssue243888_04() throws Exception {
        checkDeclaration("angularTestProject/public_html/issue243888.html", "[ <a href=\"\" ng-click=\"gre^et()\">greet</a> ]<br/>", "controllers243888.js", 250);
    }
    
    public void testIssue243888_05() throws Exception {
        checkDeclaration("angularTestProject/public_html/issue243888.html", "<li ng-repeat=\"contact in con^tacts\">", "controllers243888.js", 118);
    }
    
    public void testIssue243888_06() throws Exception {
        checkDeclaration("angularTestProject/public_html/issue243888.html", "<div ng-controller=\"Ctrl_24^3888\">", "controllers243888.js", 734);
    }
    
    // XXX These two tests are commented out until there will not be corrected functionality in the js editor.
//    public void testIssue243888_07() throws Exception {
//        checkDeclaration("angularTestProject/public_html/issue243888.html", "<div ng-click=\"pri^nt()\" ng-model=\"\"></div>", "controllers243888.js", 888);
//    }
//    
//    public void testIssue243888_08() throws Exception {
//        checkDeclaration("angularTestProject/public_html/issue243888.html", "<span ng-bind=\"printAt^tempt\"></span>", "controllers243888.js", 788);
//    }
    
    @Override
    protected void checkDeclaration(String relFilePath, String caretLine, String file, int offset) throws Exception {
         
        super.checkDeclaration(relFilePath, caretLine, file, offset); 
    }
    
    protected DeclarationFinder.DeclarationLocation findDeclaration(String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        final int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
        enforceCaretOffset(testSource, caretOffset);

        final DeclarationFinder.DeclarationLocation [] location = new DeclarationFinder.DeclarationLocation[] { null };
        ParserManager.parseWhenScanFinished(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                
                Parser.Result r = resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                DeclarationFinder finder = getFinder();
                location[0] = finder.findDeclaration(pr, caretOffset);
            }
        });
        return location[0];
    }
    
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>(ClasspathProviderImplAccessor.getJsStubs());
        
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/angularTestProject/public_html")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[cpRoots.size()]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }

    @Override
    protected boolean cleanCacheDir() {
        return false;
    }
    
    
}
