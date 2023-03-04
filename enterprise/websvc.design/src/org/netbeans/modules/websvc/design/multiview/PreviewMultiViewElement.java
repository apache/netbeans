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
package org.netbeans.modules.websvc.design.multiview;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.text.Document;
import org.openide.awt.UndoRedo;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.openide.awt.Toolbar;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Pospisil
 */
public class PreviewMultiViewElement extends CloneableEditor
        implements MultiViewElement {

    private static final long serialVersionUID = 13L;
    private transient DataObject dataObject;
    private transient DataEditorSupport des;
    private transient Lookup myLookup;
    private transient MultiViewElementCallback multiViewCallback;
    private transient JToolBar toolbar;
    private ServiceModel serviceModel;

    public PreviewMultiViewElement() {
        super(null);
    }
    //private transient DesignView designView;

    public PreviewMultiViewElement(DataEditorSupport des) {
        super(des);
        this.des = des;
        this.dataObject = this.des.getDataObject();
        initialize();
    }

    private void initialize() {

        if (des != null) {

            myLookup = Lookups.fixed(dataObject, des);
        }
    }

    public JComponent getVisualRepresentation() {
        if (des == null) {
            JPanel err = new JPanel();
            JLabel emptyLabel = new JLabel(NbBundle.getMessage(PreviewMultiViewElement.class, "LBL_wsdlPreviewErrMsg"));
            err.add(emptyLabel, BorderLayout.CENTER);
            return err;
        } else {
            return this;
        }
    }

    public JComponent getToolbarRepresentation() {
        if (des != null) {
            Document doc = getEditorPane().getDocument();
            if (doc instanceof NbDocument.CustomToolbar) {
                if (toolbar == null) {
                    toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
                }
                return toolbar;
            }
        }
        Toolbar tb = new Toolbar();
        return tb;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        if (des != null) {
            multiViewCallback = callback;
        }
    }

    public CloseOperationState canCloseElement() {

        return CloseOperationState.STATE_OK;

    }

    @Override
    public void componentActivated() {
        if (des != null) {
            super.componentActivated();
            setEditableDisabled();
        }
    }

    @Override
    public void componentDeactivated() {
        if (des != null) {
            super.componentDeactivated();
        }
    }

    @Override
    public void componentOpened() {
        if (des != null) {
            super.componentOpened();
            setEditableDisabled();
        }
    }

    @Override
    public void componentClosed() {
        if (des != null) {
            super.componentClosed();
        }
    }

    @Override
    public void componentShowing() {
        if (des != null) {
            super.componentShowing();
            setActivatedNodes(dataObject.isValid() ? new Node[]{dataObject.getNodeDelegate()} : new Node[]{});
            setEditableDisabled();
        }
    }

    @Override
    public void componentHidden() {
        if (des != null) {
            super.componentHidden();
            setActivatedNodes(new Node[]{});
        }
    }

    @Override
    public UndoRedo getUndoRedo() {
        if (des != null) {
            return super.getUndoRedo();
        }
        return null;
    }
    private Lookup lookup;

//    @Override
//    public Lookup getLookup() {
//
//
//        if (lookup == null) {
//            lookup = new ProxyLookup(super.getLookup(), myLookup);
//        }
//        return lookup;
//
//    }
    /**
     *  Sets CloneableEditor instance not editable, according to component specification.
     *  CloneableEditor isn't working properly with MultiViewComponent and part of editor 
     *  functionality couldn't work, otherwise.
     */
    public void setEditableDisabled() {
        JEditorPane prev = this.getEditorPane();
        prev.setEditable(false);
    }
}
