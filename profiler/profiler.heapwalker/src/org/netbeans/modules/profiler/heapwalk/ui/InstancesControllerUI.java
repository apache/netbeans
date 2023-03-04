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

import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.lib.profiler.ui.components.JExtendedSplitPane;
import org.netbeans.modules.profiler.heapwalk.InstancesController;
import org.netbeans.modules.profiler.heapwalk.LegendPanel;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.net.URL;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "InstancesControllerUI_NoClassDefinedMsg=<b>No class defined.</b><br><br>To view instances, double-click a class or choose Show in Instances View from pop-up menu in {0}&nbsp;<a href='#'>Classes</a> view first.",
    "InstancesControllerUI_ViewCaption=Instances",
    "InstancesControllerUI_ViewDescr=List of instances of the selected class"
})
public class InstancesControllerUI extends JPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    // --- Presenter -------------------------------------------------------------
    private static class Presenter extends JToggleButton {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Presenter() {
            super();
            setText(Bundle.InstancesControllerUI_ViewCaption());
            setToolTipText(Bundle.InstancesControllerUI_ViewDescr());
            setIcon(BrowserUtils.ICON_INSTANCE);
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

    // --- Legend utils ----------------------------------------------------------
    private class LegendUpdater implements HierarchyListener {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void hierarchyChanged(HierarchyEvent e) {
            JPanel fieldsBrowserPanel = instancesController.getFieldsBrowserController().getPanel();
            JPanel referencesBrowserPanel = instancesController.getReferencesBrowserController().getPanel();

            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                legendPanel.setGCRootVisible(referencesBrowserPanel.isShowing());
                legendPanel.setVisible(fieldsBrowserPanel.isShowing() || referencesBrowserPanel.isShowing());
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // --- UI definition ---------------------------------------------------------
    private static final String DATA = "Data"; // NOI18N
    private static final String NO_DATA = "No data"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractButton presenter;
    private CardLayout contents;
    private InstancesController instancesController;
    private JPanel dataPanel;
    private JPanel noDataPanel;
    private JSplitPane browsersSplit;
    private JSplitPane contentsSplit;
    private LegendPanel legendPanel;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public InstancesControllerUI(InstancesController instancesController) {
        this.instancesController = instancesController;

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

    // --- Public interface ------------------------------------------------------
    public void update() {
        if (contents != null) { // ui already initialized

            if (instancesController.getSelectedClass() == null) {
                contents.show(this, NO_DATA);
            } else {
                contents.show(this, DATA);
            }
        }
    }

    private void initComponents() {
        JPanel fieldsBrowserPanel = instancesController.getFieldsBrowserController().getPanel();
        JPanel referencesBrowserPanel = instancesController.getReferencesBrowserController().getPanel();
        JPanel instancesListPanel = instancesController.getInstancesListController().getPanel();

        browsersSplit = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT, fieldsBrowserPanel, referencesBrowserPanel);
        tweakSplitPaneUI(browsersSplit);
        browsersSplit.setResizeWeight(0.5d);

        contentsSplit = new JExtendedSplitPane(JSplitPane.HORIZONTAL_SPLIT, instancesListPanel, browsersSplit);
        tweakSplitPaneUI(contentsSplit);
        contentsSplit.setDividerLocation(instancesListPanel.getPreferredSize().width);

        JPanel classPresenterPanel = instancesController.getClassPresenterPanel();
        classPresenterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 3, 0,
                                                                                                         getBackground()),
                                                                         classPresenterPanel.getBorder()));

        legendPanel = new LegendPanel(true);

        dataPanel = new JPanel(new BorderLayout());
        dataPanel.setOpaque(false);
        dataPanel.add(classPresenterPanel, BorderLayout.NORTH);
        dataPanel.add(contentsSplit, BorderLayout.CENTER);
        dataPanel.add(legendPanel, BorderLayout.SOUTH);

        noDataPanel = new JPanel(new BorderLayout());
        noDataPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        HTMLTextArea hintArea = new HTMLTextArea() {
            protected void showURL(URL url) {
                instancesController.getHeapFragmentWalker().switchToClassesView();
            }
        };

        hintArea.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 8));

        String classesRes = Icons.getResource(LanguageIcons.CLASS);
        String hintText = Bundle.InstancesControllerUI_NoClassDefinedMsg(
                            "<a href='#'><img border='0' align='bottom' src='nbresloc:/" + classesRes + "'></a>"); // NOI18N
        hintArea.setText(hintText);
        noDataPanel.add(hintArea, BorderLayout.CENTER);

        contents = new CardLayout();
        setLayout(contents);
        add(noDataPanel, NO_DATA);
        add(dataPanel, DATA);

        LegendUpdater legendUpdater = new LegendUpdater();
        fieldsBrowserPanel.addHierarchyListener(legendUpdater);
        referencesBrowserPanel.addHierarchyListener(legendUpdater);
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
