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

import java.awt.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker.StateEvent;
import org.openide.util.NbBundle;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ClassPresenterPanel_InstancesCountString=Instances: {0}",
    "ClassPresenterPanel_InstanceSizeString=Instance size: {0}",
    "ClassPresenterPanel_TotalSizeString=Total size: {0}",
    "ClassPresenterPanel_RetainedSizeString=Retained size: {0}",
    "ClassPresenterPanel_RetainedSizesString=Compute Retained Sizes"
})
public class ClassPresenterPanel extends JPanel implements HeapFragmentWalker.StateListener {

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class HeaderRenderer extends JPanel {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        private JLabel classIcon;
        private JLabel packageName;
        private JLabel className;
        
        
        HeaderRenderer() {
            setLayout(new BorderLayout());
            
            classIcon = new JLabel();
            classIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, classIcon.getIconTextGap()));
            packageName = new JLabel();
            packageName.setFont(packageName.getFont().deriveFont(Font.PLAIN));
            packageName.setBorder(BorderFactory.createEmptyBorder());
            className = new JLabel();
            className.setFont(packageName.getFont().deriveFont(Font.BOLD));
            className.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            
            add(classIcon, BorderLayout.WEST);
            add(packageName, BorderLayout.CENTER);
            add(className, BorderLayout.EAST);
        }
        
        public void setIcon(Icon icon) {
            classIcon.setIcon(icon);
        }

        public void setText(String text) {
            int classNameIndex = text.lastIndexOf('.'); // NOI18N

            if (classNameIndex == -1) {
                packageName.setText(""); // NOI18N
                className.setText(text);
            } else {
                classNameIndex++;
                packageName.setText(text.substring(0, classNameIndex));
                className.setText(text.substring(classNameIndex));
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static Icon ICON_CLASS = Icons.getIcon(LanguageIcons.CLASS);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HeaderRenderer headerRenderer;
    private JLabel detailsRenderer;
    private JLabel actionsDivider;
    private JButton actionsRenderer;

    private HeapFragmentWalker heapFragmentWalker;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ClassPresenterPanel() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setJavaClass(JavaClass javaClass) {
        if (javaClass != null) {
            String className = javaClass.getName();
            String instancesCount = Bundle.ClassPresenterPanel_InstancesCountString(javaClass.getInstancesCount());
            String instanceSize = (javaClass.getInstanceSize() != -1)
                                  ? ("  |  " // NOI18N
                                  + Bundle.ClassPresenterPanel_InstanceSizeString(javaClass.getInstanceSize())) : ""; // NOI18N
            String allInstancesSize = (javaClass.getAllInstancesSize() != -1)
                                      ? ("  |  " // NOI18N
                                      + Bundle.ClassPresenterPanel_TotalSizeString(javaClass.getAllInstancesSize()))
                                      : ""; // NOI18N
            String classDetails = javaClass.isArray() ? (instancesCount + allInstancesSize)
                                                      : (instancesCount + instanceSize + allInstancesSize);
            if (heapFragmentWalker.getRetainedSizesStatus() == HeapFragmentWalker.RETAINED_SIZES_COMPUTED)
                classDetails += "  |  " + Bundle.ClassPresenterPanel_RetainedSizeString(javaClass.getRetainedSizeByClass()); // NOI18N
            headerRenderer.setText(className);
            detailsRenderer.setText(classDetails);
            actionsRenderer.setPreferredSize(new Dimension(actionsRenderer.getPreferredSize().width,
                                                           detailsRenderer.getPreferredSize().height));
        }
    }


    public void setHeapFragmentWalker(HeapFragmentWalker heapFragmentWalker) {
        if (this.heapFragmentWalker != null) this.heapFragmentWalker.removeStateListener(this);
        this.heapFragmentWalker = heapFragmentWalker;
        if (this.heapFragmentWalker != null) {
            this.heapFragmentWalker.addStateListener(this);
            updateActions(heapFragmentWalker.getRetainedSizesStatus());
        } else {
            updateActions(HeapFragmentWalker.RETAINED_SIZES_UNSUPPORTED);
        }
    }
    
    public void refresh() {}

    public void stateChanged(StateEvent e) {
        updateActions(e.getRetainedSizesStatus());
    }

    public void updateActions(int retainedSizesStatus) {
        switch (retainedSizesStatus) {
            case HeapFragmentWalker.RETAINED_SIZES_UNSUPPORTED:
            case HeapFragmentWalker.RETAINED_SIZES_COMPUTED:
                actionsRenderer.setVisible(false);
                refresh();
                break;
            case HeapFragmentWalker.RETAINED_SIZES_UNKNOWN:
            case HeapFragmentWalker.RETAINED_SIZES_CANCELLED:
                actionsRenderer.setVisible(true);
                actionsRenderer.setEnabled(true);
                break;
            case HeapFragmentWalker.RETAINED_SIZES_COMPUTING:
                actionsRenderer.setVisible(true);
                actionsRenderer.setEnabled(false);
                break;
        }
    }


    private void initComponents() {
        Color borderColor = UIManager.getLookAndFeel().getID().equals("Metal") ? // NOI18N
            UIManager.getColor("Button.darkShadow") : UIManager.getColor("Button.shadow"); // NOI18N
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor), BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        setOpaque(true);
        setBackground(UIUtils.getDarker(UIUtils.getProfilerResultsBackground()));

        headerRenderer = new HeaderRenderer();
        headerRenderer.setIcon(ICON_CLASS);
        headerRenderer.setForeground(UIManager.getColor("ToolTip.foreground")); // NOI18N
        headerRenderer.setFont(UIManager.getFont("ToolTip.font")); // NOI18N
        headerRenderer.setOpaque(false);

        detailsRenderer = new JLabel();
        detailsRenderer.setForeground(UIManager.getColor("ToolTip.foreground")); // NOI18N
        detailsRenderer.setFont(UIManager.getFont("ToolTip.font")); // NOI18N
        detailsRenderer.setOpaque(false);
        
        actionsDivider = new JLabel("  |  "); // NOI18N
        actionsDivider.setForeground(UIManager.getColor("ToolTip.foreground")); // NOI18N
        actionsDivider.setFont(UIManager.getFont("ToolTip.font")); // NOI18N
        actionsDivider.setOpaque(false);

        actionsRenderer = new JButton() {
            protected void fireActionPerformed(ActionEvent e) {
                if (heapFragmentWalker != null) {
                    BrowserUtils.performTask(new Runnable() {
                        public void run() {
                            heapFragmentWalker.computeRetainedSizes(true, true);
                        }
                    });
                }
            }
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
            public void setVisible(boolean visible) {
                super.setVisible(visible);
                actionsDivider.setVisible(visible);
            }
            public boolean isContentAreaFilled() {
                return !UIUtils.isOracleLookAndFeel() ? false : isFocusOwner();
            }
            public boolean isOpaque() {
                return !UIUtils.isOracleLookAndFeel() ? false : isFocusOwner();
            }
        };
        actionsRenderer.setOpaque(false);
        actionsRenderer.setContentAreaFilled(false);
        actionsRenderer.setBorderPainted(true);
        actionsRenderer.setMargin(new Insets(0, 0, 0, 0));
        actionsRenderer.setBorder(BorderFactory.createEmptyBorder());
        actionsRenderer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        actionsRenderer.setFont(UIManager.getFont("ToolTip.font")); // NOI18N
        actionsRenderer.setText("<html><nobr><a href='#'>" + Bundle.ClassPresenterPanel_RetainedSizesString() + "</a></nobr></html>"); // NOI18N
        actionsRenderer.setVisible(false);

        JPanel detailsContainer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        detailsContainer.setOpaque(false);
        detailsContainer.add(detailsRenderer);
        detailsContainer.add(actionsDivider);
        detailsContainer.add(actionsRenderer);
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        add(headerRenderer, c);
        
        JPanel filler = new JPanel(null);
        filler.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler, c);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        add(detailsContainer, c);
    }
}
