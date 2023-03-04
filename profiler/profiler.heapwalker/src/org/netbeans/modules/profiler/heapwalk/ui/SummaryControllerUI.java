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

package org.netbeans.modules.profiler.heapwalk.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JToggleButton;
import org.netbeans.modules.profiler.heapwalk.SummaryController;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.netbeans.lib.profiler.ui.components.JExtendedSplitPane;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
@NbBundle.Messages({
    "SummaryControllerUI_ViewTitle=Summary",
    "SummaryControllerUI_ViewDescr=Summary information about the loaded heap dump"
})
public class SummaryControllerUI extends JPanel {
    private SummaryController summaryController;
    private Presenter presenter;
    
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------
    private static class Presenter extends JToggleButton {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static Icon ICON_INFO = Icons.getIcon(GeneralIcons.INFO);

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Presenter() {
            super();
            setText(Bundle.SummaryControllerUI_ViewTitle());
            setToolTipText(Bundle.SummaryControllerUI_ViewDescr());
            setIcon(ICON_INFO);
        }
        
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width += 4;
            return d;
        }
        
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

    // --- Constructors ----------------------------------------------------------
    public SummaryControllerUI(SummaryController summaryController) {
        this.summaryController = summaryController;

        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Public interface ------------------------------------------------------
    public AbstractButton getPresenter() {
        if (presenter == null) {
            presenter = new Presenter();
        }

        return presenter;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        final JPanel hintsPanel = summaryController.getHintsController().getPanel();

        JExtendedSplitPane contentsSplit = new JExtendedSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                               summaryController.getOverViewController().getPanel(), hintsPanel);
        contentsSplit.setResizeWeight(0.75d);

        tweakSplitPaneUI(contentsSplit);

        add(contentsSplit, BorderLayout.CENTER); 
    }
    
    private void tweakSplitPaneUI(JSplitPane splitPane) {
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(3);

        if (!(splitPane.getUI() instanceof BasicSplitPaneUI)) {
            return;
        }

        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();

        if (divider != null) {
            divider.setBorder(null);
        }
    }
}
