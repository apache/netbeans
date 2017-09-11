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

package org.netbeans.modules.xml.catalog;

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
import javax.swing.JButton;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.ViewCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/** 
 *
 * @author Pavel Buzek
 */
@ActionID(id = "org.netbeans.modules.xml.catalog.CatalogAction", category = "System")
@ActionRegistration(displayName = "#LBL_CatalogAction_Name", iconInMenu=false, asynchronous=false)
@ActionReference(path = "Menu/Tools", position = 1100)
public class CatalogAction implements ActionListener {

    /** Weak reference to the dialog showing singleton Template Manager. */
    private Reference<Dialog> dialogWRef = new WeakReference<Dialog> (null);
    
    public @Override void actionPerformed(ActionEvent e) {
        
        Dialog dialog = dialogWRef.get ();

        if (dialog == null || ! dialog.isShowing ()) {

            final CatalogPanel cp = new CatalogPanel ();
            JButton closeButton = new JButton ();
            Mnemonics.setLocalizedText (closeButton,NbBundle.getMessage (CatalogAction.class, "BTN_CatalogPanel_CloseButton")); // NOI18N
            JButton openInEditor = new JButton ();
            openInEditor.setEnabled (false);
            OpenInEditorListener l = new OpenInEditorListener (cp, openInEditor);
            openInEditor.addActionListener (l);
            cp.getExplorerManager ().addPropertyChangeListener (l);
            Mnemonics.setLocalizedText (openInEditor,NbBundle.getMessage (CatalogAction.class, "BTN_CatalogPanel_OpenInEditorButton")); // NOI18N
            DialogDescriptor dd = new DialogDescriptor (cp,NbBundle.getMessage (CatalogAction.class, "LBL_CatalogPanel_Title"),  // NOI18N
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
            dialogWRef = new WeakReference<Dialog> (dialog);
            
        } else {
            dialog.toFront ();
        }
        
    }
    
    // helper classes
    private static class OpenInEditorListener implements ActionListener, PropertyChangeListener {
        CatalogPanel cp;
        JButton b;
        public OpenInEditorListener (CatalogPanel panel, JButton button) {
            cp = panel;
            b = button;
        }
        
        // ActionListener
        public void actionPerformed (ActionEvent ev) {
            Node [] nodes = (Node []) cp.getExplorerManager ().getSelectedNodes ();
            assert nodes != null && nodes.length > 0 : "Selected templates cannot be null or empty.";
            Set nodes2open = getNodes2Open (nodes);
            assert ! nodes2open.isEmpty () : "Selected templates to open cannot by empty for nodes " + Arrays.asList (nodes);
            Iterator/*<Node>*/ it = nodes2open.iterator ();
            while (it.hasNext ()) {
                Node n = (Node) it.next ();
                ViewCookie vc = (ViewCookie) n.getLookup ().lookup (ViewCookie.class);
                if (vc != null) {
                    vc.view();
                } else {
                    assert false : "Node " + n + " has to have a VewCookie.";
                }
            }
        }

        // PropertyChangeListener
        public void propertyChange (java.beans.PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
                Node [] nodes = (Node []) evt.getNewValue ();
                boolean res = nodes != null;
                int i = 0;
                while (res && i < nodes.length) {
                    Node n = nodes [i];
//                    EditCookie ec = (EditCookie) n.getLookup ().lookup (EditCookie.class);
//                    OpenCookie oc = (OpenCookie) n.getLookup ().lookup (OpenCookie.class);
                    ViewCookie vc = (ViewCookie) n.getLookup ().lookup (ViewCookie.class);
                    res = vc != null; //ec != null || oc != null;
                    
                    i++;
                }
                b.setEnabled (res);
            }
        }
    }
    
    static private Set<Node> getNodes2Open (Node [] nodes) {
        Set<Node> nodes2open = new HashSet<Node> (nodes.length);
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
