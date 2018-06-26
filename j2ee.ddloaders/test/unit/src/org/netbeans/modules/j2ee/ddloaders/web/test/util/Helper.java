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
