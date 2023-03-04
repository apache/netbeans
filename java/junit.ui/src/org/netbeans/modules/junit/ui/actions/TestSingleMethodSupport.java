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
                if (f.isDone() && task.getSingleMethod() != null){
                    sm = task.getSingleMethod();
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
                        text = text.replace("\n", "").replace(" ", "");
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
