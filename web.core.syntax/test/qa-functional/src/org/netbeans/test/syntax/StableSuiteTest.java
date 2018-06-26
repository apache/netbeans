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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.test.syntax;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jindrich Sedek
 */
public class StableSuiteTest extends J2eeTestCase {

    public StableSuiteTest(String name) {
        super(name);
    }
    
    public StableSuiteTest() {
        super("StableSuiteTest");
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        if (isRegistered(Server.ANY)){
            return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
        }else{
            return NbModuleSuite.create(conf.addTest(J2eeTestCase.class));
        }
    }

    public static final class SuiteCreator extends NbTestSuite {
        FileObject dataDir = FileUtil.createData(new StableSuiteTest().getDataDir());
        FileObject completionTestWebDir = dataDir.getFileObject("CompletionTestProjects/Jsp/web/");
        FileObject completionJSFTestWebDir = dataDir.getFileObject("CompletionTestProjects/JSF/web/");
        FileObject completionJavaEE6TestWebDir = dataDir.getFileObject("CompletionTestProjects/JavaEE6/web/");

        public SuiteCreator() throws IOException {
            addCompletionTest("stableDirectivesBasic.jsp");
            addCompletionTest("stableExpression.jsp");
            addCompletionTest("stableHTML.jsp");
            addCompletionTest("stableHTMLCompletion.html");
            addCompletionTest("stableHTMLEmbedding.html");
            addCompletionTest("stableJSPEmbedding.jsp");
            addCompletionTest("stableJSPElements.jsp");
            addCompletionTest("stableScriptletsJavaBasic.jsp");
            addCompletionTest("stableTaglibCompletion.jsp");
            addCompletionTest("stableXHTML.xhtml");
            addCompletionTest("stableIDClassCC.html");
            addJSFCompletionTest("testJSFObjects.jsp");
            addJSFCompletionTest("testJSFTag.jsp");
            addJavaEE6Test("testInjection.jsp");
            addJavaEE6Test("testInjection.xhtml");
        }
        
        private void addCompletionTest(String fileName) throws IOException{
            String name = fileName.replace('.', '_');
            addTest(new CompletionTest(name, completionTestWebDir.getFileObject(fileName)));
        }
                
        private void addJSFCompletionTest(String fileName) throws IOException{
            String name = fileName.replace('.', '_');
            addTest(new CompletionTest(name, completionJSFTestWebDir.getFileObject(fileName)));
        }

        private void addJavaEE6Test(String fileName) throws IOException{
            String name = fileName.replace('.', '_');
            addTest(new CompletionTest(name, completionJavaEE6TestWebDir.getFileObject(fileName)));
        }
    }
    
}
