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
package org.netbeans.modules.gsf.testrunner.ui.api;

import java.util.Arrays;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.lookup.Lookups;

/**
 * Provider for debugging focused test method in editor
 * @author Theofanis Oikonomou
 */
public abstract class TestMethodDebuggerProvider {
    
    private final String command = SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;
    private RequestProcessor.Task singleMethodTask;
    private SingleMethod singleMethod;
    
    /**
     * @param activatedNode the selected node.
     * @return <code>true</code> if selected {@link Node} can be handled/debugged by this provider, 
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
     * the "Debug Focused Test Method" popup menu item in editor. It should return fast.
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
     * Handle/Debug the selected test method
     * @param activatedNode the selected node
     */
    public final void debugTestMethod(Node activatedNode) {
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
	    RequestProcessor RP = new RequestProcessor("TestMethodDebuggerProvider", 1, true);   // NOI18N
	    singleMethodTask = RP.create(new Runnable() {
		@Override
		public void run() {
		    singleMethod = TestMethodRunnerProvider.findTestMethod(doc, caret, TestMethodDebuggerProvider.this::getTestMethod);
		}
	    });
	    final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Search_For_Test_Method(), singleMethodTask);
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
    
}
