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


import org.netbeans.lib.profiler.ui.components.JTitledPanel;
import org.netbeans.modules.profiler.heapwalk.HintsController;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.netbeans.lib.profiler.ui.components.NoCaret;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.heapwalk.ui.icons.HeapWalkerIcons;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
@NbBundle.Messages({
    "HintsControllerUI_ViewTitleHints=Inspect",
    "HintsControllerUI_FindButton=Find",
    "HintsControllerUI_FindButtonTooltip=Find objects with biggest retained size",
    "HintsControllerUI_Label1String=Find",
    "HintsControllerUI_Label2String=biggest objects by retained size:",
    "HintsControllerUI_BiggestObjectsCaption=Biggest objects:"
})
public class HintsControllerUI extends JTitledPanel {
    private static final Number OBJECTS_DEFAULT = 20;
    private static final int OBJECTS_MAX = 100;
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    
    private HintsController hintsController;
    // --- UI definition ---------------------------------------------------------
    private HTMLTextArea componentsArea;
    private JPanel hintsTextContainer;
    private JSpinner spinner;
    private JLabel textLabel1;
    private JLabel textLabel2;
    private JButton findButton;
    private HTMLTextArea dataArea;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------
    
    // --- Constructors ----------------------------------------------------------
    public HintsControllerUI(HintsController hintsController) {
        super(Bundle.HintsControllerUI_ViewTitleHints(), Icons.getIcon(GeneralIcons.FIND), true);
        
        this.hintsController = hintsController;
       
        initComponents();
        
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    // --- Internal interface ----------------------------------------------------
    
    
    // --- Public interface ------------------------------------------------------
    public void setComponents(String components) {
        componentsArea.setText(components);
        try { componentsArea.setCaretPosition(0); } catch (Exception e) {}
        componentsArea.setVisible(true);
    }
    
    public void setResult(String result) {
        dataArea.setText(result);
        try { dataArea.setCaretPosition(0); } catch (Exception e) {}
        findButton.setEnabled(true);
    }
    
    private void initComponents() {
        GridBagConstraints constraints;
        final int SPINNER_HEIGHT = new JTextField().getPreferredSize().height;
        
        setLayout(new BorderLayout());
        
        // hintsTextContainer
        hintsTextContainer = new JPanel(new GridBagLayout());
        hintsTextContainer.setOpaque(false);
        hintsTextContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 10, 0,
                                        UIUtils.getProfilerResultsBackground()));

        // text
        textLabel1 = new JLabel(Bundle.HintsControllerUI_Label1String());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 5, 0, 5);
        hintsTextContainer.add(textLabel1, constraints);
        
        // Spinner
        spinner = new JExtendedSpinner(new SpinnerNumberModel(OBJECTS_DEFAULT, 1, OBJECTS_MAX, 1)) {
            public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, SPINNER_HEIGHT); }
            public Dimension getMinimumSize()   { return getPreferredSize(); }
        };
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        hintsTextContainer.add(spinner, constraints);
        
        // text
        textLabel2 = new JLabel(Bundle.HintsControllerUI_Label2String());
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 5, 0, 5);
        hintsTextContainer.add(textLabel2, constraints);

        // findButton
        findButton = new JButton(Bundle.HintsControllerUI_FindButton()) {
            protected void fireActionPerformed(ActionEvent event) {
                findButton.setEnabled(false);
                int selectedValue = ((Number)spinner.getValue()).intValue();
                hintsController.computeBiggestObjects(selectedValue);
            }
        };
        findButton.setToolTipText(Bundle.HintsControllerUI_FindButtonTooltip());
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 5, 0, 0);
        hintsTextContainer.add(findButton, constraints);
        
        // Filler panel
        JPanel fillerPanel = new JPanel(null);
        fillerPanel.setOpaque(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        hintsTextContainer.add(fillerPanel, constraints);
        
        // dataArea
        dataArea = new HTMLTextArea() {
            protected void showURL(URL url) {
                if (url == null) return;
                hintsController.showURL(url);
            }
        };
        
        // componentsArea
        componentsArea = new HTMLTextArea() {
            protected void showURL(URL url) {
                if (url == null) return;
                hintsController.showURL(url);
            }
        };
        componentsArea.setVisible(false);
        
        JPanel contentsPanel = new JPanel(new GridBagLayout());
        contentsPanel.setOpaque(true);
        contentsPanel.setBackground(dataArea.getBackground());
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentsPanel.add(componentsArea, constraints);
        
        String biggestObjectsRes = Icons.getResource(HeapWalkerIcons.BIGGEST_OBJECTS);
        HTMLTextArea hintsCaption = new HTMLTextArea("<b><img border='0' align='bottom' src='nbresloc:/" +
                biggestObjectsRes + "'>&nbsp;&nbsp;" + Bundle.HintsControllerUI_BiggestObjectsCaption() + "</b><br><hr>");
        hintsCaption.setCaret(new NoCaret());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentsPanel.add(hintsCaption, constraints);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 9, 0, 0);
        contentsPanel.add(hintsTextContainer, constraints);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 9, 0, 0);
        contentsPanel.add(dataArea, constraints);

        JScrollPane contentsPanelScrollPane = new JScrollPane(contentsPanel,
                                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentsPanelScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, getTitleBorderColor()),
                BorderFactory.createMatteBorder(5, 4, 5, 5, UIUtils.getProfilerResultsBackground())));
        contentsPanelScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        contentsPanelScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        contentsPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(10);

        setLayout(new BorderLayout());
        add(contentsPanelScrollPane, BorderLayout.CENTER);

        // UI tweaks
        setBackground(dataArea.getBackground());
        
    }
    
    
    // --- Private implementation ------------------------------------------------
    
        
}
