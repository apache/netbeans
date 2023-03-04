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
package org.netbeans.modules.css.visual.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.netbeans.modules.css.visual.RuleEditorNode;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "label.go.to.source.action=Go to Source"
})
public class GoToSourceAction extends AbstractAction {

    private RuleEditorPanel panel;
    private RuleEditorNode.DeclarationProperty propertyDescriptor;

    public GoToSourceAction(RuleEditorPanel panel, RuleEditorNode.DeclarationProperty propertyDescriptor) {
        super(Bundle.label_go_to_source_action());
        this.panel = panel;
        this.propertyDescriptor = propertyDescriptor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Model model = panel.getModel();
        model.runWriteTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                Lookup lookup = model.getLookup();
                FileObject file = lookup.lookup(FileObject.class);
                Snapshot snap = lookup.lookup(Snapshot.class);
                final Document doc = lookup.lookup(Document.class);
                if (snap != null && doc != null && file != null) {
                    PropertyDeclaration decl = propertyDescriptor.getDeclaration();
                    int ast_from = decl.getStartOffset();
                    if (ast_from != -1) {
                        //source element, not virtual which is not persisted yet
                        final int doc_from = snap.getOriginalOffset(ast_from);
                        if (doc_from != -1) {
                            try {
                                DataObject dobj = DataObject.find(file);
                                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                                if(ec != null) {
                                    JEditorPane[] openedPanes = ec.getOpenedPanes();
                                    if(openedPanes != null && openedPanes.length > 0) {
                                        //already opened
                                        ec.open(); //give it a focus 
                                        JEditorPane pane = openedPanes[0];
                                        pane.setCaretPosition(doc_from);
                                    } else {
                                        //not opened, open it
                                        try {
                                            ec.openDocument();
                                            ec.open();
                                            openedPanes = ec.getOpenedPanes();
                                            if (openedPanes != null && openedPanes.length > 0) {
                                                //now opened
                                                JEditorPane pane = openedPanes[0];
                                                pane.setCaretPosition(doc_from);
                                            }
                                        } catch (IOException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                }
                            } catch (DataObjectNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        });
    }
}
