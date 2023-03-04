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

package org.netbeans.modules.profiler.heapwalk;

import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ClassesListController_GcRootString=GC Root",
    "ClassesListController_ArrayTypeString=Array type",
    "ClassesListController_ObjectTypeString=Object type",
    "ClassesListController_PrimitiveTypeString=Primitive type",
    "ClassesListController_StaticFieldString=Static field",
    "ClassesListController_LoopString=Loop"
})
public class LegendPanel extends JPanel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JLabel gcRootLegend;
    private JLabel gcRootLegendDivider;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public LegendPanel(boolean showGCRoot) {
        initComponents();
        setGCRootVisible(showGCRoot);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setGCRootVisible(boolean showGCRoot) {
        gcRootLegend.setVisible(showGCRoot);
        gcRootLegendDivider.setVisible(showGCRoot);
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 5));

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 0));
        legendPanel.setOpaque(false);

        gcRootLegend = new JLabel(Bundle.ClassesListController_GcRootString(), BrowserUtils.ICON_GCROOT, SwingConstants.LEFT);
        gcRootLegendDivider = new JLabel("|"); // NOI18N

        legendPanel.add(new JLabel(Bundle.ClassesListController_ArrayTypeString(), BrowserUtils.ICON_ARRAY, SwingConstants.LEFT));
        legendPanel.add(new JLabel("|")); // NOI18N
        legendPanel.add(new JLabel(Bundle.ClassesListController_ObjectTypeString(), BrowserUtils.ICON_INSTANCE, SwingConstants.LEFT));
        legendPanel.add(new JLabel("|")); // NOI18N
        legendPanel.add(new JLabel(Bundle.ClassesListController_PrimitiveTypeString(), BrowserUtils.ICON_PRIMITIVE, SwingConstants.LEFT));
        legendPanel.add(new JLabel("|")); // NOI18N
        legendPanel.add(new JLabel(Bundle.ClassesListController_StaticFieldString(), BrowserUtils.ICON_STATIC, SwingConstants.LEFT));
        legendPanel.add(new JLabel("|")); // NOI18N
        legendPanel.add(gcRootLegend);
        legendPanel.add(gcRootLegendDivider);
        legendPanel.add(new JLabel(Bundle.ClassesListController_LoopString(), BrowserUtils.ICON_LOOP, SwingConstants.LEFT));

        //add(new JLabel("Legend:"), BorderLayout.WEST);
        add(legendPanel, BorderLayout.EAST);
    }
}
