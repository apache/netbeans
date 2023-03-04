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

package org.netbeans.modules.j2ee.ddloaders.web.test.util;

import java.io.File;
import java.awt.*;

import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;

import javax.swing.*;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;

/**
 *
 * @author Milan Kuchtiak
 */
public class Helper {

    public static File getDDFile(File dataDir) {
        String result = dataDir.getAbsolutePath() + "/projects/webapp/web/WEB-INF/web.xml";
        return new File(result);
    }

    public static DDBeanTableModel getContextParamsTableModel(final DDDataObject dObj) {
        final ToolBarMultiViewElement multiViewElement = getMultiViewElement(dObj);
        JPanel sectionPanel = getSectionPanel(multiViewElement);
        Component[] children = sectionPanel.getComponents();
        DefaultTablePanel tablePanel = null;
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof DefaultTablePanel) {
                tablePanel = (DefaultTablePanel) children[i];
                break;
            }
        }
        return (DDBeanTableModel) tablePanel.getModel();
    }

    private static JPanel getSectionPanel(final ToolBarMultiViewElement multiViewElement) {
        return new StepIterator() {
            JPanel sectionPanel;

            public boolean step() throws Exception {
                SectionPanel outerPanel = multiViewElement.getSectionView().findSectionPanel("context_params");
                sectionPanel = outerPanel == null ? null : outerPanel.getInnerPanel();
                return sectionPanel != null;
            }
        }.sectionPanel;
    }

    public static ToolBarMultiViewElement getMultiViewElement(final DDDataObject dObj) {
        return new StepIterator() {
            ToolBarMultiViewElement multiViewElement;

            public boolean step() throws Exception {
                multiViewElement = dObj.getActiveMVElement();
                return multiViewElement != null;
            }
        }.multiViewElement;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex){}
    }

    public static void waitForDispatchThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }
        final boolean[] finished = new boolean[]{false};
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                finished[0] = true;
            }
        });
        new StepIterator() {
            public boolean step() throws Exception {
                return finished[0];
            }
        };
    }
}
