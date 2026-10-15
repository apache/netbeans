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
package org.netbeans.modules.gsf.testrunner.ui.api;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.ui.annotation.TestMethodAnnotation;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.lookup.Lookups;

/**
 * Provider for running focused test method in editor
 * @author Theofanis Oikonomou
 */
public abstract class TestMethodRunnerProvider {

    private final String command = SingleMethod.COMMAND_RUN_SINGLE_METHOD;
    private RequestProcessor.Task singleMethodTask;
    private SingleMethod singleMethod;

    /**
     * @param activatedNode the selected node.
     * @return <code>true</code> if selected {@link Node} can be handled/run by this provider, 
     * <code>false</code> otherwise.
     */
    public abstract boolean canHandle(Node activatedNode);
    
    /**
     *
     * @param doc The active document
     * @param caret the position of the caret
     * @return the SingleMethod representing the method in the document containing the caret, {@code null} otherwise
     */
    public abstract SingleMethod getTestMethod(Document doc, int caret);
    
    /**
     * Return <code>true</code> if selected {@link Node} is a test class, enabling 
     * the "Run Focused Test Method" popup menu item in editor. It should return fast.
     * The default implementation returns <code>true</code>.
     * @param activatedNode the selected node.
     * @return <code>true</code> if selected {@link Node} is a test class, 
     * <code>false</code> otherwise.
     * @since 1.36
     */
    public boolean isTestClass(Node activatedNode) {
        return true;
    }

    /**
     * Handle/Run the selected test method
     * @param activatedNode the selected node
     */
    @NbBundle.Messages({"Search_For_Test_Method=Searching for test method",
	"No_Test_Method_Found=No test method found"})
    public final void runTestMethod(Node activatedNode) {
        final Node activeNode = activatedNode;
        final Document doc;
        final int caret;

        final EditorCookie ec = activeNode.getLookup().lookup(EditorCookie.class);
        if (ec != null) {
            JEditorPane pane = Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
		@Override
		public JEditorPane run() {
		    return NbDocument.findRecentEditorPane(ec);
		}
	    });
            if (pane != null) {
                doc = pane.getDocument();
                caret = pane.getCaret().getDot();
            } else {
                doc = null;
                caret = -1;
            }
        } else {
            doc = null;
            caret = -1;
        }

	singleMethod = activeNode.getLookup().lookup(SingleMethod.class);
	if (singleMethod == null) {
	    RequestProcessor RP = new RequestProcessor("TestMethodRunnerProvider", 1, true);   // NOI18N
	    singleMethodTask = RP.create(new Runnable() {
		@Override
		public void run() {
		    singleMethod = findTestMethod(doc, caret, TestMethodRunnerProvider.this::getTestMethod);
		}
	    });
	    final ProgressHandle ph = ProgressHandle.createHandle(Bundle.Search_For_Test_Method(), singleMethodTask);
	    singleMethodTask.addTaskListener(new TaskListener() {
		@Override
		public void taskFinished(org.openide.util.Task task) {
		    ph.finish();
		    if (singleMethod == null) {
			StatusDisplayer.getDefault().setStatusText(Bundle.No_Test_Method_Found());
		    } else {
			Mutex.EVENT.readAccess(new Runnable() {
			    @Override
			    public void run() {
				ActionProvider ap = CommonUtils.getInstance().getActionProvider(singleMethod.getFile());
				if (ap != null) {
				    if (Arrays.asList(ap.getSupportedActions()).contains(command) && ap.isActionEnabled(command, Lookups.singleton(singleMethod))) {
					ap.invokeAction(command, Lookups.singleton(singleMethod));
				    }
				}
			    }
			});
		    }
		}
	    });
	    ph.start();
	    singleMethodTask.schedule(0);
	}
    }

    static SingleMethod findTestMethod(Document doc, int cursor, BiFunction<Document, Integer, SingleMethod> fallback) {
        List<TestMethod> methods = (List<TestMethod>) doc.getProperty(TestMethodAnnotation.DOCUMENT_METHODS_KEY);
        if (methods != null) {
            int s = 0;
            int e = methods.size();
            while (e > s) {
                int m = s + (e - s) / 2;
                TestMethod cand = methods.get(m);
                if (cand.start().getOffset() <= cursor && cursor <= cand.end().getOffset()) {
                    return cand.method();
                }
                if (cursor < cand.start().getOffset()) {
                    e = m - 1;
                } else {
                    s = m + 1;
                }
            }
        }
        return fallback.apply(doc, cursor);
    }
}
