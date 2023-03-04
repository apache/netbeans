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
