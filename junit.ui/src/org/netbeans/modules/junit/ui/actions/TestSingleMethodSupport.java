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
package org.netbeans.modules.junit.ui.actions;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.testrunner.CommonTestUtil;
import org.netbeans.modules.java.testrunner.JavaUtils;
import org.netbeans.spi.project.SingleMethod;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;

/**
 *
 * @author Theofanis Oikonomou
 */
public class TestSingleMethodSupport {

    private TestSingleMethodSupport() {
    }private static final Logger LOGGER = Logger.getLogger(TestSingleMethodSupport.class.getName());

    public static boolean isTestClass(Node activatedNode) {
        FileObject fileObject = org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils.getFileObjectFromNode(activatedNode);
        if (fileObject != null && CommonTestUtil.isJavaFile(fileObject)) {
            Project project = FileOwnerQuery.getOwner(fileObject);
            if (project != null) {
                SourceGroup[] javaSGs = new JavaUtils(project).getJavaSourceGroups();
                for (int i = 0; i < javaSGs.length; i++) {
                    SourceGroup javaSG = javaSGs[i];
                    FileObject rootFolder = javaSG.getRootFolder();
                    URL[] testRoots = UnitTestForSourceQuery.findUnitTests(rootFolder);
                    URL[] sourceRoots = UnitTestForSourceQuery.findSources(rootFolder);
                    if (((fileObject == rootFolder) || FileUtil.isParentOf(rootFolder, fileObject)) && javaSG.contains(fileObject)) {
                        // activated FO is contained in the javaSG source group
                        if (testRoots.length == 0 && sourceRoots.length > 0) {
                            // javaSG has corresponding source root but no corresponding test root,
                            // thus the activated FO is a test class, so activate action
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public static SingleMethod getTestMethod(Document doc, int cursor){
        SingleMethod sm = null;
        if (doc != null){
            JavaSource js = JavaSource.forDocument(doc);
            if(js == null) {
                return null;
            }
            TestClassInfoTask task = new TestClassInfoTask(cursor);
            try {
                Future<Void> f = js.runWhenScanFinished(task, true);
                if (f.isDone() && task.getFileObject() != null && task.getMethodName() != null){
                    sm = new SingleMethod(task.getFileObject(), task.getMethodName());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return sm;
    }

    public static boolean canHandle(Node activatedNode) {
        FileObject fileO = org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils.getFileObjectFromNode(activatedNode);
        if (fileO != null) {
            final EditorCookie ec = activatedNode.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
		JEditorPane pane = Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
		    @Override
		    public JEditorPane run() {
			return NbDocument.findRecentEditorPane(ec);
		    }
		});
		if (pane != null) {
		    String text = pane.getText();
                    if (text != null) {  //NOI18N
                        text = text.replaceAll("\n", "").replaceAll(" ", "");
			if ((text.contains("@RunWith") || text.contains("@org.junit.runner.RunWith")) //NOI18N
			    && text.contains("Parameterized.class)")) {  //NOI18N
			    return false;
			}
                    }
                    SingleMethod sm = getTestMethod(pane.getDocument(), pane.getCaret().getDot());
                    if(sm != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
}
