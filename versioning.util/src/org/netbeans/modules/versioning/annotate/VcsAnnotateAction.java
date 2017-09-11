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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.versioning.annotate;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.io.File;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Action to embed in your menus, provides Show/Hide Annotations functionality.
 * 
 * @author Maros Sandor
 */
public class VcsAnnotateAction extends AbstractAction {
    
    private final VcsAnnotationsProvider     provider;
    private final Collection<? extends Node> nodes;

    public VcsAnnotateAction(VCSContext context, VcsAnnotationsProvider provider) {
        this.provider = provider;
        nodes = context.getElements().lookupAll(Node.class);
        putValue(Action.NAME, cumputeActionName());
    }

    public boolean isEnabled() {
        return activatedEditorCookie() != null;
    }

    public void actionPerformed(ActionEvent e) {
        if (visible()) {
            JEditorPane pane = activatedEditorPane();
            AnnotationBarManager.hideAnnotationBar(pane);
        } else {
            EditorCookie ec = activatedEditorCookie();
            if (ec == null) return;
            
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes == null) {
                ec.open();
            }
            panes = ec.getOpenedPanes();
            if (panes == null) {
                return;
            }
            final JEditorPane currentPane = panes[0];
            
            AnnotationBar ab = AnnotationBarManager.showAnnotationBar(currentPane);
            ab.setAnnotationMessage(NbBundle.getMessage(VcsAnnotateAction.class, "CTL_AnnotationSubstitute")); // NOI18N;
            
            computeAnnotationsAsync(ab);
        }
    }

    private void computeAnnotationsAsync(final AnnotationBar ab) {
        Utils.postParallel(new Runnable() {
            @Override
            public void run() {
                computeAnnotations(ab);
            }
        }, 0);
    }
    
    private void computeAnnotations(AnnotationBar ab) {
        VcsAnnotations annotations = provider.getAnnotations();
        File file = new File("");
        ab.annotationLines(file, annotations);
        
    }

    private String cumputeActionName() {
        if (visible()) {
            return NbBundle.getMessage(VcsAnnotateAction.class, "Action_HideAnnotations");  // NOI18N
        } else {
            return NbBundle.getMessage(VcsAnnotateAction.class, "Action_ShowAnnotations"); // NOI18N
        }
    }

    public boolean visible() {
        JEditorPane currentPane = activatedEditorPane();
        return AnnotationBarManager.annotationBarVisible(currentPane);
    }

    private JEditorPane activatedEditorPane() {
        EditorCookie ec = activatedEditorCookie();        
        if (ec != null && SwingUtilities.isEventDispatchThread()) {              
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                return panes[0];
            }
        }
        return null;
    }

    private EditorCookie activatedEditorCookie() {
        if (nodes != null && nodes.size() == 1) {
            Node node = nodes.iterator().next();
            return (EditorCookie) node.getCookie(EditorCookie.class);
        }
        return null;
    }
}
