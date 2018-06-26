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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbDetailForm;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
public class EjbJarDetailsPanel extends EjbDetailForm {

    private final EjbJar ejbJar;
    private EjbJarMultiViewDataObject dataObject;

    private class LargeIconEditorModel extends TextItemEditorModel {

        public LargeIconEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            return ejbJar.getLargeIcon();
        }

        protected void setValue(String value) {
            ejbJar.setLargeIcon(value);
        }
    }

    private class SmallIconEditorModel extends TextItemEditorModel {

        public SmallIconEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            return ejbJar.getSmallIcon();
        }

        protected void setValue(String value) {
            ejbJar.setSmallIcon(value);
        }
    }

    private class DescriptionEditorModel extends TextItemEditorModel {

        public DescriptionEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            return ejbJar.getDefaultDescription();
        }

        protected void setValue(String value) {
            ejbJar.setDescription(value);
        }
    }

    private class DisplayNameEditorModel extends TextItemEditorModel {

        public DisplayNameEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            return ejbJar.getDefaultDisplayName();
        }

        protected void setValue(String value) {
            ejbJar.setDisplayName(value);
        }
    }

    public EjbJarDetailsPanel(SectionNodeView sectionNodeView, final EjbJar ejbJar) {
        super(sectionNodeView);
        this.dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();
        this.ejbJar = ejbJar;
        XmlMultiViewDataSynchronizer synchronizer = dataObject.getModelSynchronizer();
        addRefreshable(new ItemEditorHelper(getDisplayNameTextField(), new DisplayNameEditorModel(synchronizer)));
        addRefreshable(new ItemEditorHelper(getDescriptionTextArea(), new DescriptionEditorModel(synchronizer)));
        addRefreshable(new ItemEditorHelper(getSmallIconTextField(), new SmallIconEditorModel(synchronizer)));
        addRefreshable(new ItemEditorHelper(getLargeIconTextField(), new LargeIconEditorModel(synchronizer)));
        getBrowseLargeIconButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String relativePath = Utils.browseIcon(dataObject);
                if (relativePath != null) {
                    getLargeIconTextField().setText(relativePath);
                }
            }
        });
        getBrowseSmallIconButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String relativePath = Utils.browseIcon(dataObject);
                if (relativePath != null) {
                    getSmallIconTextField().setText(relativePath);
                }
            }
        });

    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (source instanceof EjbJar || source instanceof Icon) {
            scheduleRefreshView();
        }
    }
}
