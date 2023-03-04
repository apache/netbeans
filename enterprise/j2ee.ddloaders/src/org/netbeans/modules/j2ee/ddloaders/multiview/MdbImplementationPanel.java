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

import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.MdbImplementationForm;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.LinkButton;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.openide.filesystems.FileObject;

/**
 * @author pfiala
 */
public class MdbImplementationPanel extends MdbImplementationForm {
    private XmlMultiViewDataObject dataObject;
    private static final String LINK_BEAN = "linkBean";
    private MessageDriven messageDriven;
    private NonEditableDocument beanClassDocument;

    /**
     * Creates new form MdbImplementationForm
     *
     * @param sectionNodeView enclosing SectionNodeView object
     */
    public MdbImplementationPanel(final SectionNodeView sectionNodeView, final MessageDriven messageDriven) {
        super(sectionNodeView);
        this.messageDriven = messageDriven;
        dataObject = sectionNodeView.getDataObject();
        beanClassDocument = new NonEditableDocument() {
            protected String retrieveText() {
                return messageDriven.getEjbClass();
            }
        };
        getBeanClassTextField().setDocument(beanClassDocument);
        LinkButton.initLinkButton(getBeanClassLinkButton(), this, null, LINK_BEAN);
    }

    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        scheduleRefreshView();
    }

    @Override
    public void refreshView() {
         beanClassDocument.init();
    }

    @Override
    public void linkButtonPressed(Object ddBean, String ddProperty) {
        if(LINK_BEAN.equals(ddProperty)) {
            final FileObject ejbJarFile = dataObject.getPrimaryFile();
            Utils.openEditorFor(ejbJarFile, messageDriven.getEjbClass());
        }
    }
}
