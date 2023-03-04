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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.ErrorManager;


/**
 * @author pfiala
 */
public abstract class DDMultiViewElement extends ToolBarMultiViewElement {
    
    private static final long serialVersionUID = 20110810L;

    private SectionView view;
    protected ToolBarDesignEditor comp;
    protected SunDescriptorDataObject dataObject;

    public DDMultiViewElement(SunDescriptorDataObject dataObject) {
        super(dataObject);
        this.dataObject = dataObject;
        comp = new ToolBarDesignEditor();
        setVisualEditor(comp);
    }

    @Override
    public void componentShowing() {
        if (view == null) {
            view = createView();
            if (view instanceof SectionNodeView) {
                dataObject.getDDRoot().addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        ((SectionNodeView) view).dataModelPropertyChange(evt.getSource(), evt.getPropertyName(),
                                evt.getOldValue(), evt.getNewValue());
                    }
                });
            }
        }
        comp.setContentView(view);
        if (view instanceof SectionNodeView) {
            ((SectionNodeView) view).refreshView();
        }
        view.checkValidity();
        super.componentShowing();
        Object lastActive = comp.getLastActive();
        final SectionNode node;
        final SectionNodeView sectionNodeView = ((SectionNodeView) view);
        if (lastActive instanceof SectionNode) {
            node = (SectionNode) lastActive;
        } else {
            node = sectionNodeView.getRootNode();
        }
        
        // TODO possible race condition -- occasionally nodes being opened by
        // this call do not exist at the time the call is made (possibly they have
        // been replaced by the background updates.)
        try {
            sectionNodeView.openPanel(node);
        } catch(IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        dataObject.checkParseable();
    }

    protected abstract SectionView createView();
    
    @Override
    public SectionView getSectionView() {
        return view;
    }
}
