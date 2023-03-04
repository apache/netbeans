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

package org.netbeans.modules.quicksearch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

/**
 * Quick search toolbar component
 * @author  Jan Becicka
 */
public class QuickSearchComboBar extends AbstractQuickSearchComboBar {

    //@StaticResource(searchClasspath=true)
    private static final String ICON_FIND = "org/openide/awt/resources/quicksearch/findMenu.png";             // NOI18N
    //@StaticResource(searchClasspath=true)
    private static final String ICON_PROGRESS_0 = "org/openide/awt/resources/quicksearch/progress_0.png"; // NOI18N
    //@StaticResource(searchClasspath=true)
    private static final String ICON_PROGRESS_1 = "org/openide/awt/resources/quicksearch/progress_1.png"; // NOI18N
    //@StaticResource(searchClasspath=true)
    private static final String ICON_PROGRESS_2 = "org/openide/awt/resources/quicksearch/progress_2.png"; // NOI18N
    //@StaticResource(searchClasspath=true)
    private static final String ICON_PROGRESS_3 = "org/openide/awt/resources/quicksearch/progress_3.png"; // NOI18N
    //@StaticResource(searchClasspath=true)
    private static final String ICON_PROGRESS_4 = "org/openide/awt/resources/quicksearch/progress_4.png"; // NOI18N
    //@StaticResource(searchClasspath=true)
    private static final String ICON_PROGRESS_5 = "org/openide/awt/resources/quicksearch/progress_5.png"; // NOI18N
    //@StaticResource(searchClasspath=true)
    private static final String ICON_PROGRESS_6 = "org/openide/awt/resources/quicksearch/progress_6.png"; // NOI18N
    //@StaticResource(searchClasspath=true)
    private static final String ICON_PROGRESS_7 = "org/openide/awt/resources/quicksearch/progress_7.png"; // NOI18N
    private static final String[] ICON_PROGRESS = new String[] {
        ICON_PROGRESS_0, ICON_PROGRESS_1, ICON_PROGRESS_2, ICON_PROGRESS_3,
        ICON_PROGRESS_4, ICON_PROGRESS_5, ICON_PROGRESS_6, ICON_PROGRESS_7
    };
    
    private final ImageIcon findIcon = ImageUtilities.loadImageIcon(ICON_FIND, false);
    /** Timer used for progress animation (see #143019).  */
    private final Timer animationTimer = new Timer(100, new ActionListener() {

        ImageIcon icons[];
        int index = 0;

        public void actionPerformed(ActionEvent e) {
            if (icons == null) {
                icons = new ImageIcon[8];
                for (int i = 0; i < 8; i++) {
                    icons[i] = ImageUtilities.loadImageIcon(ICON_PROGRESS[i], false);
                }
            }
            jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 6));
            jLabel2.setIcon(icons[index]);
            //mac os x
            jLabel2.repaint();
            
            index = (index + 1) % 8;
        }
    });

    public QuickSearchComboBar(KeyStroke ks) {
        super( ks );

        initComponents();
    }


    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSeparator1 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        Color background = UIManager.getColor("nb.quicksearch.background"); //NOI18N
        if (background != null) {
            jPanel1.setBackground(background);
        }
        jPanel1.setBorder(getQuickSearchBorder());
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setIcon(findIcon);
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(QuickSearchComboBar.class, "QuickSearchComboBar.jLabel2.toolTipText")); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel2.setName("jLabel2"); // NOI18N
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel2MousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel1.add(jLabel2, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setViewportBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(2, 18));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jScrollPane1.setViewportView(command);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }

    /**
     * Get customizable border for QuickSearch. See bug 246907.
     *
     * @return Custom border if defined in {@link UIManager} or default line
     * border.
     */
    private Border getQuickSearchBorder() {
        Border border = UIManager.getBorder("nb.quicksearch.border"); //NOI18N
        return border != null
                ? border
                : BorderFactory.createLineBorder(getComboBorderColor());
    }

    private void jLabel2MousePressed(java.awt.event.MouseEvent evt) {
        maybeShowPopup(evt);
    }

    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;

    @Override
    protected JTextComponent createCommandField() {
        JTextArea res = new DynamicWidthTA();
        res.setRows(1);
        res.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 1));
        // disable default Swing's Ctrl+Shift+O binding to enable our global action
        InputMap curIm = res.getInputMap(JComponent.WHEN_FOCUSED);
        while (curIm != null) {
            curIm.remove(KeyStroke.getKeyStroke(
                    KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
            curIm = curIm.getParent();
        }
        return res;
    }

    @Override
    protected JComponent getInnerComponent() {
        return jPanel1;
    }

    void startProgressAnimation() {
        if (animationTimer != null && !animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    void stopProgressAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
            jLabel2.setIcon(findIcon);
            jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
    }

    private final class DynamicWidthTA extends JTextArea {
        private Dimension prefWidth;

        @Override
        public Dimension getPreferredSize() {
            if (prefWidth == null) {
                Dimension orig = super.getPreferredSize();
                prefWidth = new Dimension(computePrefWidth(), orig.height);
            }
            return prefWidth;
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }
}
