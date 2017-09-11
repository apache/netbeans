/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
		    singleMethod = getTestMethod(doc, caret);
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
