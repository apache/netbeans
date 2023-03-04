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

package org.netbeans.modules.profiler.ppoints.ui;

import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.netbeans.modules.profiler.ppoints.TriggeredGlobalProfilingPoint;
import org.openide.util.NbBundle;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "TriggerCustomizer_HeapUsgRelKey=used heap",
    "TriggerCustomizer_HeapUsgRelUnit=% available memory",
    "TriggerCustomizer_HeapSizeAbsKey=allocated heap",
    "TriggerCustomizer_HeapSizeAbsUnit=MB",
    "TriggerCustomizer_SurvgenCountKey=surviving generations",
    "TriggerCustomizer_SurvgenCountUnit=generations",
    "TriggerCustomizer_LdClassCountKey=loaded classes",
    "TriggerCustomizer_LdClassCountUnit=classes",
    "TriggerCustomizer_ThreadsCountKey=threads",
    "TriggerCustomizer_ThreadsCountUnit=threads",
    "TriggerCustomizer_CpuTimeKey=cpu time",
    "TriggerCustomizer_CpuTimeUnit=%",
    "TriggerCustomizer_GcTimeKey=gc time",
    "TriggerCustomizer_GcTimeUnit=%",
    "TriggerCustomizer_TakeWhenLabelText=Take &when",
    "TriggerCustomizer_ExceedsLabelText=e&xceeds",
    "TriggerCustomizer_TakeOnceRadioText=Take &once",
    "TriggerCustomizer_TakeAlwaysRadioText=Take &every time"
})
public class TriggerCustomizer extends ValidityAwarePanel implements ActionListener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static int defaultTextComponentHeight = -1;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final SpinnerModel percentsModel = new SpinnerNumberModel(1, 1, 99, 1);

    // --- Implementation --------------------------------------------------------
    private final SpinnerModel unitsModel = new SpinnerNumberModel(1, 1, 9999, 1);
    private JComboBox triggerWhenCombo;
    private JLabel triggerExceedsLabel;
    private JLabel triggerGenerationsLabel;
    private JLabel triggerWhenLabel;
    private JRadioButton triggerAlwaysRadio;
    private JRadioButton triggerOnceRadio;
    private JSpinner triggerValueSpinner;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public TriggerCustomizer() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getPreferredCaptionAreaWidth() {
        // Not used for this customizer
        return -1;
    }

    public void setTriggerCondition(TriggeredGlobalProfilingPoint.TriggerCondition condition) {
        switch (condition.getMetric()) {
            case TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_HEAPUSG:
                triggerWhenCombo.setSelectedItem(Bundle.TriggerCustomizer_HeapUsgRelKey());
                triggerValueSpinner.setValue((int) condition.getValue());

                break;
            case TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_HEAPSIZ:
                triggerWhenCombo.setSelectedItem(Bundle.TriggerCustomizer_HeapSizeAbsKey());
                triggerValueSpinner.setValue((int) (condition.getValue() / (1024 * 1024)));

                break;
            case TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_SURVGEN:
                triggerWhenCombo.setSelectedItem(Bundle.TriggerCustomizer_SurvgenCountKey());
                triggerValueSpinner.setValue((int) condition.getValue());

                break;
            case TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_LDCLASS:
                triggerWhenCombo.setSelectedItem(Bundle.TriggerCustomizer_LdClassCountKey());
                triggerValueSpinner.setValue((int) condition.getValue());

                break;
            case TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_CPUUSG:
                triggerWhenCombo.setSelectedItem(Bundle.TriggerCustomizer_CpuTimeKey());
                triggerValueSpinner.setValue((int) condition.getValue());

                break;
            case TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_GCUSG:
                triggerWhenCombo.setSelectedItem(Bundle.TriggerCustomizer_GcTimeKey());
                triggerValueSpinner.setValue((int) condition.getValue());

                break;
            case TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_THREADS:
                triggerWhenCombo.setSelectedItem(Bundle.TriggerCustomizer_ThreadsCountKey());
                triggerValueSpinner.setValue((int) condition.getValue());

                break;
            default:
                break;
        }

        triggerOnceRadio.setSelected(condition.isOnetime());
        triggerAlwaysRadio.setSelected(!condition.isOnetime());
    }

    public TriggeredGlobalProfilingPoint.TriggerCondition getTriggerCondition() {
        TriggeredGlobalProfilingPoint.TriggerCondition condition = new TriggeredGlobalProfilingPoint.TriggerCondition();

        Object key = triggerWhenCombo.getSelectedItem();

        if (Bundle.TriggerCustomizer_HeapUsgRelKey().equals(key)) {
            condition.setMetric(TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_HEAPUSG);
            condition.setValue(((Integer) triggerValueSpinner.getValue()).intValue());
        } else if (Bundle.TriggerCustomizer_HeapSizeAbsKey().equals(key)) {
            condition.setMetric(TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_HEAPSIZ);
            condition.setValue(((Integer) triggerValueSpinner.getValue()).intValue() * (1024L * 1024L));
        } else if (Bundle.TriggerCustomizer_SurvgenCountKey().equals(key)) {
            condition.setMetric(TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_SURVGEN);
            condition.setValue(((Integer) triggerValueSpinner.getValue()).intValue());
        } else if (Bundle.TriggerCustomizer_LdClassCountKey().equals(key)) {
            condition.setMetric(TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_LDCLASS);
            condition.setValue(((Integer) triggerValueSpinner.getValue()).intValue());
        } else if (Bundle.TriggerCustomizer_CpuTimeKey().equals(key)) {
            condition.setMetric(TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_CPUUSG);
            condition.setValue(((Integer) triggerValueSpinner.getValue()).intValue());
        } else if (Bundle.TriggerCustomizer_GcTimeKey().equals(key)) {
            condition.setMetric(TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_GCUSG);
            condition.setValue(((Integer) triggerValueSpinner.getValue()).intValue());
        } else if (Bundle.TriggerCustomizer_ThreadsCountKey().equals(key)) {
            condition.setMetric(TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_THREADS);
            condition.setValue(((Integer) triggerValueSpinner.getValue()).intValue());
        }

        condition.setOnetime(triggerOnceRadio.isSelected());

        return condition;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == triggerWhenCombo) {
            Object key = triggerWhenCombo.getSelectedItem();

            if (Bundle.TriggerCustomizer_HeapUsgRelKey().equals(key)) {
                triggerGenerationsLabel.setText(Bundle.TriggerCustomizer_HeapUsgRelUnit());
                triggerValueSpinner.setModel(percentsModel);
            } else if (Bundle.TriggerCustomizer_HeapSizeAbsKey().equals(key)) {
                triggerGenerationsLabel.setText(Bundle.TriggerCustomizer_HeapSizeAbsUnit());
                triggerValueSpinner.setModel(unitsModel);
            } else if (Bundle.TriggerCustomizer_SurvgenCountKey().equals(key)) {
                triggerGenerationsLabel.setText(Bundle.TriggerCustomizer_SurvgenCountUnit());
                triggerValueSpinner.setModel(unitsModel);
            } else if (Bundle.TriggerCustomizer_LdClassCountKey().equals(key)) {
                triggerGenerationsLabel.setText(Bundle.TriggerCustomizer_LdClassCountUnit());
                triggerValueSpinner.setModel(unitsModel);
            } else if (Bundle.TriggerCustomizer_CpuTimeKey().equals(key)) {
                triggerGenerationsLabel.setText(Bundle.TriggerCustomizer_CpuTimeUnit());
                triggerValueSpinner.setModel(percentsModel);
            } else if (Bundle.TriggerCustomizer_GcTimeKey().equals(key)) {
                triggerGenerationsLabel.setText(Bundle.TriggerCustomizer_GcTimeUnit());
                triggerValueSpinner.setModel(percentsModel);
            } else if (Bundle.TriggerCustomizer_ThreadsCountKey().equals(key)) {
                triggerGenerationsLabel.setText(Bundle.TriggerCustomizer_ThreadsCountUnit());
                triggerValueSpinner.setModel(unitsModel);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("plaf.metal.MetalLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //NOI18N
        } catch (Exception e) {
        }

        ;

        TriggerCustomizer main = new TriggerCustomizer();

        //    main.addValidityListener(new ValidityListener() {
        //      public void validityChanged(boolean isValid) { System.err.println(">>> Validity changed to " + isValid); }
        //    });
        JFrame frame = new JFrame("Customize Profiling Point"); //NOI18N
        frame.getContentPane().add(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void normalizeCaptionAreaWidth() {
        // Not used for this customizer
    }

    //  private int getDefaultTextComponentHeight() {
    //    if (defaultTextComponentHeight == -1) defaultTextComponentHeight = new JComboBox().getPreferredSize().height;
    //    return defaultTextComponentHeight;
    //  }
    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        JPanel triggerSettingsContainer = new JPanel(new GridBagLayout());

        // triggerWhenLabel
        triggerWhenLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(triggerWhenLabel, Bundle.TriggerCustomizer_TakeWhenLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        triggerSettingsContainer.add(triggerWhenLabel, constraints);

        // triggerWhenCombo
        triggerWhenCombo = new JComboBox(new Object[] {
            Bundle.TriggerCustomizer_CpuTimeKey(),
            Bundle.TriggerCustomizer_GcTimeKey(),
            Bundle.TriggerCustomizer_HeapUsgRelKey(), 
            Bundle.TriggerCustomizer_HeapSizeAbsKey(), 
            Bundle.TriggerCustomizer_SurvgenCountKey(), 
            Bundle.TriggerCustomizer_ThreadsCountKey(),
            Bundle.TriggerCustomizer_LdClassCountKey()}) {
                public Dimension getPreferredSize() {
                    return new Dimension(Math.min(super.getPreferredSize().width, 200), super.getPreferredSize().height);
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        triggerWhenLabel.setLabelFor(triggerWhenCombo);
        triggerWhenCombo.addActionListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        triggerSettingsContainer.add(triggerWhenCombo, constraints);

        // triggerExceedsLabel
        triggerExceedsLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(triggerExceedsLabel, Bundle.TriggerCustomizer_ExceedsLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        triggerSettingsContainer.add(triggerExceedsLabel, constraints);

        // lineNumberSpinner
        triggerValueSpinner = new JExtendedSpinner(percentsModel) {
                public Dimension getPreferredSize() {
                    return new Dimension(Math.max(super.getPreferredSize().width, 55),
                                         getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        triggerExceedsLabel.setLabelFor(triggerValueSpinner);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        triggerSettingsContainer.add(triggerValueSpinner, constraints);

        // triggerGenerationsLabel
        triggerGenerationsLabel = new JLabel(Bundle.TriggerCustomizer_HeapUsgRelKey());
        constraints = new GridBagConstraints();
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        triggerSettingsContainer.add(triggerGenerationsLabel, constraints);

        JPanel triggerFillerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 5;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        triggerSettingsContainer.add(triggerFillerPanel, constraints);

        // triggerSettingsContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 5, 0);
        add(triggerSettingsContainer, constraints);

        // --- next row ----------------------------------------------------------
        ButtonGroup triggerRadiosGroup = new ButtonGroup();
        JPanel triggerRadiosContainer = new JPanel(new GridBagLayout());

        // triggerOnceRadio
        triggerOnceRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(triggerOnceRadio, Bundle.TriggerCustomizer_TakeOnceRadioText());
        triggerRadiosGroup.add(triggerOnceRadio);
        triggerOnceRadio.setSelected(true);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        triggerRadiosContainer.add(triggerOnceRadio, constraints);

        // triggerAlwaysRadio
        triggerAlwaysRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(triggerAlwaysRadio, Bundle.TriggerCustomizer_TakeAlwaysRadioText());
        triggerRadiosGroup.add(triggerAlwaysRadio);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        triggerRadiosContainer.add(triggerAlwaysRadio, constraints);

        JPanel takeRadiosSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        triggerRadiosContainer.add(takeRadiosSpacer, constraints);

        // takeRadiosContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(triggerRadiosContainer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel fillerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(fillerPanel, constraints);
    }

    private void updateValidity() {
        boolean isValid = true;

        if (isValid != TriggerCustomizer.this.areSettingsValid()) {
            fireValidityChanged(isValid);
        }
    }
}
