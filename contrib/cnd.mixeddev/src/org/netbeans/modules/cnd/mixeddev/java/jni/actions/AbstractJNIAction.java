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
