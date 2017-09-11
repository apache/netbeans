/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.templates.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.modules.templates.ui.TemplatesPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.util.NbBundle;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/** 
 *
 * @author Jiri Rechtacek
 */
@ActionID(id = "org.netbeans.modules.templates.actions.TemplatesAction", category = "System")
@ActionRegistration(displayName = "#LBL_TemplatesAction_Name", iconInMenu=false, asynchronous=false)
@ActionReference(position = 1000, path = "Menu/Tools")
public class TemplatesAction extends AbstractAction { // XXX could be ActionListener if not using SHORT_DESCRIPTION, or maybe alwaysEnabled should support that as an option?

    /** Weak reference to the dialog showing singleton Template Manager. */
    private Reference<Dialog> dialogWRef = new WeakReference<> (null);
    private Reference<TemplatesPanel> templatesPanelRef = new WeakReference<> (null);
    
    public TemplatesAction() {
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(TemplatesAction.class, "HINT_TemplatesAction")); // NOI18N
    }    
    
    public @Override void actionPerformed(ActionEvent evt) {
        
        Dialog dialog = dialogWRef.get ();
        String pathToSelect = System.getProperty("org.netbeans.modules.templates.actions.TemplatesAction.preselect");
        System.clearProperty("org.netbeans.modules.templates.actions.TemplatesAction.preselect");

        if (dialog == null || ! dialog.isShowing ()) {

            final TemplatesPanel tp = new TemplatesPanel (pathToSelect);
            JButton closeButton = new JButton ();
            Mnemonics.setLocalizedText (closeButton, NbBundle.getMessage (TemplatesAction.class, "BTN_TemplatesPanel_CloseButton")); // NOI18N
            JButton openInEditor = new JButton ();
            openInEditor.setEnabled (false);
            OpenInEditorListener l = new OpenInEditorListener (tp, openInEditor);
            openInEditor.addActionListener (l);
            tp.getExplorerManager ().addPropertyChangeListener (l);
            Mnemonics.setLocalizedText (openInEditor, NbBundle.getMessage (TemplatesAction.class, "BTN_TemplatesPanel_OpenInEditorButton")); // NOI18N
            DialogDescriptor dd = new DialogDescriptor (tp,
                                    NbBundle.getMessage (TemplatesAction.class, "LBL_TemplatesPanel_Title"),  // NOI18N
                                    false, // modal
                                    new Object [] { openInEditor, closeButton },
                                    closeButton,
                                    DialogDescriptor.DEFAULT_ALIGN,
                                    null,
                                    null);
            dd.setClosingOptions (null);
            // set helpctx to null again, DialogDescriptor replaces null with HelpCtx.DEFAULT_HELP
            dd.setHelpCtx (null);
            
            dialog = DialogDisplayer.getDefault ().createDialog (dd);
            dialog.setVisible (true);
            dialogWRef = new WeakReference<> (dialog);
            templatesPanelRef = new WeakReference<>(tp);
            
        } else {
            if (pathToSelect != null) {
                TemplatesPanel tp = templatesPanelRef.get();
                if (tp != null) {
                    tp.select(pathToSelect);
                }
            }
            dialog.toFront ();
        }
        
    }
    
    // helper classes
    private static class OpenInEditorListener implements ActionListener, PropertyChangeListener {
        TemplatesPanel tp;
        JButton b;
        public OpenInEditorListener (TemplatesPanel panel, JButton button) {
            tp = panel;
            b = button;
        }
        
        // ActionListener
        @Override
        public void actionPerformed (ActionEvent ev) {
            Node [] nodes = tp.getExplorerManager ().getSelectedNodes ();
            assert nodes != null && nodes.length > 0 : "Selected templates cannot be null or empty.";
            Set nodes2open = getNodes2Open (nodes);
            assert ! nodes2open.isEmpty () : "Selected templates to open cannot by empty for nodes " + Arrays.asList (nodes);
            Iterator/*<Node>*/ it = nodes2open.iterator ();
            while (it.hasNext ()) {
                Node n = (Node) it.next ();
                EditCookie ec = n.getLookup ().lookup (EditCookie.class);
                if (ec != null) {
                    ec.edit ();
                } else {
                    OpenCookie oc = n.getLookup ().lookup (OpenCookie.class);
                    if (oc != null) {
                        oc.open ();
                    } else {
                        assert false : "Node " + n + " has to have a EditCookie or OpenCookie.";
                    }
                }
            }
        }

        // PropertyChangeListener
        @Override
        public void propertyChange (java.beans.PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
                Node [] nodes = (Node []) evt.getNewValue ();
                boolean res = nodes != null && nodes.length > 0;
                int i = 0;
                while (res && i < nodes.length) {
                    Node n = nodes [i];
                    EditCookie ec = n.getLookup().lookup(EditCookie.class);
                    OpenCookie oc = n.getLookup().lookup(OpenCookie.class);
                    res = ec != null || oc != null;

                    // 65037: Template Manager should not offer to Open in Editor an empty pseudotemplate
                    if (res) {
                        DataObject dobj = n.getLookup().lookup(DataObject.class);
                        assert dobj != null : "DataObject for node " + n;
                        FileObject fo = dobj.getPrimaryFile ();
                        res = fo.canRevert() || fo.getSize () > 0;
                    }

                    i++;
                }
                b.setEnabled (res);
            }
        }
    }
    
    static private Set<Node> getNodes2Open (Node [] nodes) {
        Set<Node> nodes2open = new HashSet<> (nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            if (nodes [i].isLeaf ()) {
                nodes2open.add (nodes [i]);
            } else {
                nodes2open.addAll (getNodes2Open (nodes [i].getChildren ().getNodes (true)));
            }
        }
        return nodes2open;
    }

}
