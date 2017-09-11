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
