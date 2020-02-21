/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.mixeddev.java.jni.actions;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import static javax.swing.Action.MNEMONIC_KEY;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.cnd.mixeddev.Triple;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport;
import org.netbeans.modules.cnd.mixeddev.java.ResolveJavaEntityTask;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public abstract class AbstractJNIAction extends AbstractAction {
    
    private static final RequestProcessor RP = new RequestProcessor(AbstractJNIAction.class.getName(), 1);
    
    private final Node[] activatedNodes;
    
    public AbstractJNIAction(Lookup context) {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        Object lookupResult = context.lookup(Node.class);
        if (lookupResult instanceof Node[]) {
            this.activatedNodes = (Node[]) lookupResult;
        } else if (lookupResult instanceof Node) {
            this.activatedNodes = new Node[]{(Node) lookupResult};
        } else {
            this.activatedNodes = null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                actionPerformedImpl(activatedNodes);
            }
        });
    }

    @Override
    public boolean isEnabled() {
        Triple<DataObject, Document, Integer> context = extractContext(activatedNodes);
        return isEnabled(context);
    }
    
    protected final Triple<DataObject, Document, Integer> extractContext(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            final Node activeNode = activatedNodes[0];
            final DataObject dobj = activeNode.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                final EditorCookie ec = activeNode.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    JEditorPane pane = Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
                        @Override
                        public JEditorPane run() {
                            return NbDocument.findRecentEditorPane(ec);
                        }
                    });
                    if (pane != null) {
                        return Triple.of(dobj, pane.getDocument(), pane.getCaret().getDot());
                    }
                }
            }
        }
        return null;
    }
    
    protected JavaEntityInfo resolveJavaEntity(Document doc, int caret) {
        return JavaContextSupport.resolveContext(doc, new ResolveJavaEntityTask(caret), false);
    }
    
    protected abstract boolean isEnabledAtPosition(Document doc, int caret);
    
    protected boolean isEnabled(Triple<DataObject, Document, Integer> context) {
        return context == null ? false : isEnabledAtPosition(context.second, context.third);
    }
    
    protected abstract void actionPerformedImpl(Node[] activatedNodes);
}
