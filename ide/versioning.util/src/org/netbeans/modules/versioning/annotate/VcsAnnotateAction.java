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
