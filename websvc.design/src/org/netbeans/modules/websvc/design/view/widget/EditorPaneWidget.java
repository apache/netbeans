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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentListener;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Ajit
 */
public class EditorPaneWidget extends Widget {
    
    private JEditorPane editorPane;
    private JScrollPane scrollPane;
    private boolean componentAdded;
    private float origoinalFontSize = 0;
    private ComponentSceneListener validateListener;
    private ComponentComponentListener componentListener;
    
    /** Creates a new instance of EditorPaneWidget 
     * @param scene 
     * @param text 
     * @param contentType 
     */
    public EditorPaneWidget(Scene scene, String text, String contentType) {
        super(scene);
        editorPane = new JEditorPane(contentType,text);
        scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        editorPane.setVisible(false);
        scrollPane.setVisible(false);
        componentAdded = false;
        origoinalFontSize = editorPane.getFont().getSize2D();
        editorPane.setMaximumSize(new Dimension(0,(int)origoinalFontSize*6));
        componentListener = new ComponentComponentListener ();
    }

    /**
     * 
     * @param flag 
     */
    public void setEditable(boolean flag) {
        editorPane.setEditable(flag);
    }

    public boolean isEditable() {
        return editorPane.isEditable();
    }

    public String getText() {
        return editorPane.getText();
    }

    protected void notifyAdded() {
        editorPane.setVisible(true);
        scrollPane.setVisible(true);
        if(validateListener==null) {
            validateListener = new ComponentSceneListener ();
            getScene ().addSceneListener (validateListener);
        }
    }

    protected void notifyRemoved() {
        editorPane.setVisible(false);
        scrollPane.setVisible(false);
        if(validateListener!=null) {
            getScene ().removeSceneListener (validateListener);
            validateListener = null;
        }
    }

    /**
     * Calculates a client area from the preferred size of the component.
     * @return the calculated client area
     */
    protected final Rectangle calculateClientArea () {
        return new Rectangle (editorPane.getPreferredSize ());
    }

    /**
     * Paints the component widget.
     */
    protected final void paintWidget () {
        if(!componentAdded) {
            setLayout( LayoutFactory.createHorizontalFlowLayout() );
            getScene().getView().add(scrollPane);
            componentAdded = true;
        }
        scrollPane.setBounds (getScene().convertSceneToView (convertLocalToScene (getClientArea())));
        editorPane.setFont(editorPane.getFont().deriveFont((float)getScene().getZoomFactor()*origoinalFontSize));
        editorPane.repaint();
    }
    
    void addDocumentListener(DocumentListener listener){
        editorPane.getDocument().addDocumentListener(listener);
    }

    void removeDocumentListener(DocumentListener listener){
        editorPane.getDocument().removeDocumentListener(listener);
    }

    private final class ComponentSceneListener implements Scene.SceneListener {

        public void sceneRepaint () {
        }

        public void sceneValidating () {
            if(componentAdded) {
                getScene().getView().remove(scrollPane);
                componentAdded = false;
                scrollPane.removeComponentListener (componentListener);
            }
        }

        public void sceneValidated () {
            if(componentAdded) {
                scrollPane.addComponentListener (componentListener);
            }
        }
    }

    private final class ComponentComponentListener implements ComponentListener {

        public void componentResized (ComponentEvent e) {
            revalidate ();
        }

        public void componentMoved (ComponentEvent e) {
            revalidate ();
        }

        public void componentShown (ComponentEvent e) {
        }

        public void componentHidden (ComponentEvent e) {
        }

    }
}
