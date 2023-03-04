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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pfiala
 */
public abstract class EjbJarMultiviewElement extends ToolBarMultiViewElement {

    private SectionView view;
    protected ToolBarDesignEditor comp;
    protected EjbJarMultiViewDataObject dataObject;

    public EjbJarMultiviewElement(EjbJarMultiViewDataObject dataObject) {
        super(dataObject);
        this.dataObject = dataObject;
        comp = new ToolBarDesignEditor();
        setVisualEditor(comp);
    }

    public void componentShowing() {
        if (view == null) {
            view = createView();
            if (view instanceof SectionNodeView) {
                dataObject.getEjbJar().addPropertyChangeListener(new PropertyChangeListener() {
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
        sectionNodeView.openPanel(node);
        dataObject.checkParseable();
    }

    protected abstract SectionView createView();
    
    public SectionView getSectionView() {
        return view;
    }
}
