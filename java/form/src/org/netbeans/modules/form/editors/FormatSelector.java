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
package org.netbeans.modules.form.editors;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DateFormatter;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import org.openide.util.NbBundle;

/**
 * Component that allows you to select a format. It fires property change
 * events when the selected value is changed.
 *
 * @author Jan Stola
 */
public class FormatSelector {
    /** Name of the property fired when the customized value is changed. */
    public static final String PROP_FORMAT = "format"; // NOI18N
    /** Property change support used to fire property changes. */
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    /** Set of formats offered by the dialog. */
    private FormatInfo[][] formats;
    /** Set of examples offered by the dialog.  */
    private String[][] examples;

    /**
     * Created new <code>FormatSelector</code>.
     */
    public FormatSelector() {
        initComponents();
        initFormats();
        initExamples();
        formatList.setCellRenderer(new FormatInfoRenderer());
        initCategoryList();
    }

    /**
     * Adds property change listener. 
     * 
     * @param listener property change listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes property change listener. 
     * 
     * @param listener property change listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fires property change. 
     */
    private void firePropertyChange() {
        changeSupport.firePropertyChange(PROP_FORMAT, null, null);
    }

    /**
     * Returns selected format.
     * 
     * @return selected format. 
     */
    public FormatInfo getFormat() {
        FormatInfo format = (FormatInfo)formatList.getSelectedValue();
        format = new FormatInfo(format.getType(), format.getSubtype(), formatField.getText());
        return format;
    }

    /**
     * Sets selected format. 
     * 
     * @param format format to select.
     */
    public void setFormat(FormatInfo format) {
        categoryList.setSelectedIndex(format.getType());
        if (format.getSubtype() == FormatInfo.NONE)  {
            String f = format.getFormat();
            ListModel model = formatList.getModel();
            int i;
            for (i=0; i<model.getSize(); i++) {
                FormatInfo info = (FormatInfo)model.getElementAt(i);
                if (info.isCustomizable() && f.equals(info.getFormat())) break;
            }
            if (i == model.getSize()) {
                i--; // Not found = select custom (last element)
            }
            formatList.setSelectedIndex(i);
            formatField.setText(f);
        } else {
            formatList.setSelectedIndex(format.getSubtype());
            formatField.setText(((FormatInfo)formatList.getSelectedValue()).getFormat());
        }
    }

    /**
     * Initializes category list. 
     */
    private void initCategoryList() {
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        DefaultListModel model = new DefaultListModel();
        model.addElement(bundle.getString("LBL_FormatSelector_Category_number")); // NOI18N
        model.addElement(bundle.getString("LBL_FormatSelector_Category_date")); // NOI18N
        model.addElement(bundle.getString("LBL_FormatSelector_Category_time")); // NOI18N
        model.addElement(bundle.getString("LBL_FormatSelector_Category_percent")); // NOI18N
        model.addElement(bundle.getString("LBL_FormatSelector_Category_currency")); // NOI18N
        model.addElement(bundle.getString("LBL_FormatSelector_Category_mask")); // NOI18N
        categoryList.setModel(model);
        categoryList.setSelectedIndex(0);
    }

    /**
     * Initializes examples. 
     */
    private void initExamples() {
        examples = new String[6][];
        int i = 0;
        NumberFormat nf = new DecimalFormat("0.###"); // NOI18N
        examples[i] = new String[] {
            nf.format(1234.567),
            nf.format(-1234.56),
            nf.format(0.123),
            nf.format(-0.123)
        };
        DateFormat df = DateFormat.getDateInstance();
        GregorianCalendar c1 = new GregorianCalendar(2000, 0, 1);
        GregorianCalendar c2 = new GregorianCalendar(1999, 11, 31);
        examples[++i] = new String[] {
            df.format(new Date()),
            df.format(c1.getTime()),
            df.format(c2.getTime())
        };
        DateFormat tf = DateFormat.getTimeInstance();
        GregorianCalendar c3 = new GregorianCalendar();
        c3.set(Calendar.HOUR, 12);
        c3.set(Calendar.MINUTE, 34);
        c3.set(Calendar.SECOND, 56);
        c3.set(Calendar.MILLISECOND, 789);
        GregorianCalendar c4 = new GregorianCalendar();
        c4.set(Calendar.HOUR, 23);
        c4.set(Calendar.MINUTE, 59);
        c4.set(Calendar.SECOND, 59);
        c4.set(Calendar.MILLISECOND, 999);
        GregorianCalendar c5 = new GregorianCalendar();
        c5.set(Calendar.HOUR, 1);
        c5.set(Calendar.MINUTE, 2);
        c5.set(Calendar.SECOND, 3);
        c5.set(Calendar.MILLISECOND, 4);
        examples[++i] = new String[] {
            tf.format(new Date()),
            tf.format(c3.getTime()),
            tf.format(c4.getTime()),
            tf.format(c5.getTime())
        };
        examples[++i] = examples[0];
        examples[++i] = examples[0];
        examples[++i] = new String[0];
    }

    /**
     * Initializes formats.
     */
    private void initFormats() {
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        formats = new FormatInfo[6][];
        int i = 0;
        // number
        formats[i] = new FormatInfo[8];
        formats[i][0] = new FormatInfo(
            FormatInfo.NUMBER,
            FormatInfo.DEFAULT,
            bundle.getString("LBL_FormatSelector_default"), // NOI18N
            new NumberFormatter()
        );
        formats[i][1] = new FormatInfo(
            FormatInfo.NUMBER,
            FormatInfo.INTEGER,
            bundle.getString("LBL_FormatSelector_default_integer"),  // NOI18N
            new NumberFormatter(NumberFormat.getIntegerInstance())
        );
        formats[i][2] = new FormatInfo(
            FormatInfo.NUMBER,
            FormatInfo.NONE,
            "0", // NOI18N
            new NumberFormatter(new DecimalFormat("0")) // NOI18N
        );
        formats[i][3] = new FormatInfo(
            FormatInfo.NUMBER,
            FormatInfo.NONE,
            "0.00", // NOI18N
            new NumberFormatter(new DecimalFormat("0.00")) // NOI18N
        );
        formats[i][4] = new FormatInfo(
            FormatInfo.NUMBER,
            FormatInfo.NONE,
            "#,##0", // NOI18N
            new NumberFormatter(new DecimalFormat("#,##0")) // NOI18N
        );
        formats[i][5] = new FormatInfo(
            FormatInfo.NUMBER,
            FormatInfo.NONE,
            "#,##0.00", // NOI18N
            new NumberFormatter(new DecimalFormat("#,##0.00")) // NOI18N
        );
        formats[i][6] = new FormatInfo(
            FormatInfo.NUMBER,
            FormatInfo.NONE,
            "#,###.00", // NOI18N
            new NumberFormatter(new DecimalFormat("#,###.00")) // NOI18N
        );
        formats[i][7] = new FormatInfo(
            FormatInfo.NUMBER,
            FormatInfo.NONE,
            bundle.getString("LBL_FormatSelector_custom"), // NOI18N
            null
        );
        // date
        formats[++i] = new FormatInfo[6];
        formats[i][0] = new FormatInfo(
            FormatInfo.DATE,
            FormatInfo.DEFAULT,
            bundle.getString("LBL_FormatSelector_default"), // NOI18N
            new DateFormatter()
        );
        formats[i][1] = new FormatInfo(
            FormatInfo.DATE,
            FormatInfo.SHORT,
            bundle.getString("LBL_FormatSelector_short"), // NOI18N
            new DateFormatter(DateFormat.getDateInstance(DateFormat.SHORT))
        );
        formats[i][2] = new FormatInfo(
            FormatInfo.DATE,
            FormatInfo.MEDIUM,
            bundle.getString("LBL_FormatSelector_medium"), // NOI18N
            new DateFormatter(DateFormat.getDateInstance(DateFormat.MEDIUM))
        );
        formats[i][3] = new FormatInfo(
            FormatInfo.DATE,
            FormatInfo.LONG,
            bundle.getString("LBL_FormatSelector_long"), // NOI18N
            new DateFormatter(DateFormat.getDateInstance(DateFormat.LONG))
        );
        formats[i][4] = new FormatInfo(
            FormatInfo.DATE,
            FormatInfo.FULL,
            bundle.getString("LBL_FormatSelector_full"), // NOI18N
            new DateFormatter(DateFormat.getDateInstance(DateFormat.FULL))
        );
        formats[i][5] = new FormatInfo(
            FormatInfo.DATE,
            FormatInfo.NONE,
            bundle.getString("LBL_FormatSelector_custom"), // NOI18N
            null
        );
        // time
        formats[++i] = new FormatInfo[6];
        formats[i][0] = new FormatInfo(
            FormatInfo.TIME,
            FormatInfo.DEFAULT,
            bundle.getString("LBL_FormatSelector_default"), // NOI18N
            new DateFormatter(DateFormat.getTimeInstance())
        );
        formats[i][1] = new FormatInfo(
            FormatInfo.TIME,
            FormatInfo.SHORT,
            bundle.getString("LBL_FormatSelector_short"), // NOI18N
            new DateFormatter(DateFormat.getTimeInstance(DateFormat.SHORT))
        );
        formats[i][2] = new FormatInfo(
            FormatInfo.TIME,
            FormatInfo.MEDIUM,
            bundle.getString("LBL_FormatSelector_medium"), // NOI18N
            new DateFormatter(DateFormat.getTimeInstance(DateFormat.MEDIUM))
        );
        formats[i][3] = new FormatInfo(
            FormatInfo.TIME,
            FormatInfo.LONG,
            bundle.getString("LBL_FormatSelector_long"), // NOI18N
            new DateFormatter(DateFormat.getTimeInstance(DateFormat.LONG))
        );
        formats[i][4] = new FormatInfo(
            FormatInfo.TIME,
            FormatInfo.FULL,
            bundle.getString("LBL_FormatSelector_full"), // NOI18N
            new DateFormatter(DateFormat.getTimeInstance(DateFormat.FULL))
        );
        formats[i][5] = new FormatInfo(
            FormatInfo.TIME,
            FormatInfo.NONE,
            bundle.getString("LBL_FormatSelector_custom"), // NOI18N
            null
        );
        // percent
        formats[++i] = new FormatInfo[4];
        formats[i][0] = new FormatInfo(
            FormatInfo.PERCENT,
            FormatInfo.DEFAULT,
            bundle.getString("LBL_FormatSelector_default"), // NOI18N
            new NumberFormatter(NumberFormat.getPercentInstance())
        );
        formats[i][1] = new FormatInfo(
            FormatInfo.PERCENT,
            FormatInfo.NONE,
            "0%", // NOI18N
            new NumberFormatter(new DecimalFormat("0%")) // NOI18N
        );
        formats[i][2] = new FormatInfo(
            FormatInfo.PERCENT,
            FormatInfo.NONE,
            "0.00%", // NOI18N
            new NumberFormatter(new DecimalFormat("0.00%")) // NOI18N
        );
        formats[i][3] = new FormatInfo(
            FormatInfo.PERCENT,
            FormatInfo.NONE,
            bundle.getString("LBL_FormatSelector_custom"), // NOI18N
            null
        );
        // currency
        formats[++i] = new FormatInfo[8];
        formats[i][0] = new FormatInfo(
            FormatInfo.CURRENCY,
            FormatInfo.DEFAULT,
            bundle.getString("LBL_FormatSelector_default"), // NOI18N
            new NumberFormatter(NumberFormat.getCurrencyInstance())
        );
        formats[i][1] = new FormatInfo(
            FormatInfo.CURRENCY,
            FormatInfo.NONE,
            "\u00A4#,##0", // NOI18N
            new NumberFormatter(new DecimalFormat("\u00A4#,##0")) // NOI18N
        );
        formats[i][2] = new FormatInfo(
            FormatInfo.CURRENCY,
            FormatInfo.NONE,
            "\u00A4#,##0.00", // NOI18N
            new NumberFormatter(new DecimalFormat("\u00A4#,##0.00")) // NOI18N
        );
        formats[i][3] = new FormatInfo(
            FormatInfo.CURRENCY,
            FormatInfo.NONE,
            "\u00A4#,##0.--", // NOI18N
            new NumberFormatter(new DecimalFormat("\u00A4#,##0.--")) // NOI18N
        );
        formats[i][4] = new FormatInfo(
            FormatInfo.CURRENCY,
            FormatInfo.NONE,
            "\u00A4\u00A4#,##0", // NOI18N
            new NumberFormatter(new DecimalFormat("\u00A4\u00A4#,##0")) // NOI18N
        );
        formats[i][5] = new FormatInfo(
            FormatInfo.CURRENCY,
            FormatInfo.NONE,
            "\u00A4\u00A4#,##0.00", // NOI18N
            new NumberFormatter(new DecimalFormat("\u00A4\u00A4#,##0.00")) // NOI18N
        );
        formats[i][6] = new FormatInfo(
            FormatInfo.CURRENCY,
            FormatInfo.NONE,
            "\u00A4\u00A4#,##0.--", // NOI18N
            new NumberFormatter(new DecimalFormat("\u00A4\u00A4#,##0.--")) // NOI18N
        );
        formats[i][7] = new FormatInfo(
            FormatInfo.CURRENCY,
            FormatInfo.NONE,
            bundle.getString("LBL_FormatSelector_custom"), // NOI18N
            null
        );
        try {
            // mask
            formats[++i] = new FormatInfo[2];
            formats[i][0] = new FormatInfo(
                FormatInfo.MASK,
                FormatInfo.NONE,
                "###-####", // NOI18N
                new MaskFormatter("###-####") // NOI18N
            );
            formats[i][1] = new FormatInfo(
                FormatInfo.MASK,
                FormatInfo.NONE,
                bundle.getString("LBL_FormatSelector_custom"), // NOI18N
                null
            );
            Date now = new Date();
            Number n = -1234.56;
            for (int k=0; k<formats.length-1; k++) {
                for (int j=0; j<formats[k].length; j++) {
                    if ((k==1) || (k==2)) {
                        formats[k][j].calculateExample(now);
                    } else {
                        formats[k][j].calculateExample(n);
                    }
                }
            }
        } catch (ParseException pex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, pex.getMessage(), pex);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selector = new javax.swing.JPanel();
        categoryTitle = new javax.swing.JLabel();
        categoryPane = new javax.swing.JScrollPane();
        categoryList = new javax.swing.JList();
        formatPane = new javax.swing.JScrollPane();
        formatList = new javax.swing.JList();
        formatTitle = new javax.swing.JLabel();
        formatLabel = new javax.swing.JLabel();
        formatField = new javax.swing.JTextField();
        examplePanel = new javax.swing.JPanel();
        previewField = new javax.swing.JTextField();
        previewLabel = new javax.swing.JLabel();
        exampleCombo = new javax.swing.JComboBox();
        formatButton = new javax.swing.JButton();
        exampleLabel = new javax.swing.JLabel();

        categoryTitle.setLabelFor(categoryList);
        org.openide.awt.Mnemonics.setLocalizedText(categoryTitle, org.openide.util.NbBundle.getMessage(FormatSelector.class, "LBL_FormatSelector_CategoryTitle")); // NOI18N

        categoryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categoryList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                categoryListValueChanged(evt);
            }
        });
        categoryPane.setViewportView(categoryList);
        categoryList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormatSelector.class, "ACSD_FormatSelector_Category")); // NOI18N

        formatList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        formatList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                formatListValueChanged(evt);
            }
        });
        formatPane.setViewportView(formatList);
        formatList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormatSelector.class, "ACSD_FormatSelector_Format")); // NOI18N

        formatTitle.setLabelFor(formatList);
        org.openide.awt.Mnemonics.setLocalizedText(formatTitle, org.openide.util.NbBundle.getMessage(FormatSelector.class, "LBL_FormatSelector_FormatTitle")); // NOI18N

        formatLabel.setLabelFor(formatField);
        org.openide.awt.Mnemonics.setLocalizedText(formatLabel, org.openide.util.NbBundle.getMessage(FormatSelector.class, "LBL_FormatSelector_Format")); // NOI18N

        formatField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formatFieldFocusLost(evt);
            }
        });

        examplePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FormatSelector.class, "LBL_FormatSelector_Example"))); // NOI18N

        previewField.setEditable(false);

        previewLabel.setLabelFor(previewField);
        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(FormatSelector.class, "LBL_FormatSelector_Preview")); // NOI18N

        exampleCombo.setEditable(true);
        exampleCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                exampleComboItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(formatButton, org.openide.util.NbBundle.getMessage(FormatSelector.class, "LBL_FormatSelector_Test")); // NOI18N
        formatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatButtonActionPerformed(evt);
            }
        });

        exampleLabel.setLabelFor(exampleCombo);
        org.openide.awt.Mnemonics.setLocalizedText(exampleLabel, org.openide.util.NbBundle.getMessage(FormatSelector.class, "LBL_FormatSelector_ValueToFormat")); // NOI18N

        javax.swing.GroupLayout examplePanelLayout = new javax.swing.GroupLayout(examplePanel);
        examplePanel.setLayout(examplePanelLayout);
        examplePanelLayout.setHorizontalGroup(
            examplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(examplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(examplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exampleLabel)
                    .addComponent(previewLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(examplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewField, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .addComponent(exampleCombo, 0, 20, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formatButton)
                .addContainerGap())
        );
        examplePanelLayout.setVerticalGroup(
            examplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(examplePanelLayout.createSequentialGroup()
                .addGroup(examplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exampleLabel)
                    .addComponent(formatButton)
                    .addComponent(exampleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(examplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previewLabel)
                    .addComponent(previewField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        previewField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormatSelector.class, "ACSD_FormatSelector_Preview")); // NOI18N
        exampleCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormatSelector.class, "ACSD_FormatSelector_ValueToFormat")); // NOI18N
        formatButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormatSelector.class, "ACSD_FormatSelector_Test")); // NOI18N

        javax.swing.GroupLayout selectorLayout = new javax.swing.GroupLayout(selector);
        selector.setLayout(selectorLayout);
        selectorLayout.setHorizontalGroup(
            selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(categoryPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(categoryTitle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(formatTitle)
                    .addGroup(selectorLayout.createSequentialGroup()
                        .addComponent(formatPane, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                        .addGroup(selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(selectorLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(formatLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(formatField, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                            .addGroup(selectorLayout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(examplePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        selectorLayout.setVerticalGroup(
            selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryTitle)
                    .addComponent(formatTitle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(selectorLayout.createSequentialGroup()
                        .addGroup(selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(formatField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(formatLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(examplePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(selectorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(formatPane, 0, 0, Short.MAX_VALUE)
                        .addComponent(categoryPane)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        formatField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormatSelector.class, "ACSD_FormatSelector_Format2")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    /** Determines whether the selector should fire property changes. */
    private boolean formatChangeFromCode = false;
    private void formatFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formatFieldFocusLost
        FormatInfo info = (FormatInfo)formatList.getSelectedValue();
        int size = formatList.getModel().getSize();
        if (formatList.getSelectedIndex() != size-1) {
            if (!formatField.getText().equals(info.getFormat())) {
                try {
                    formatChangeFromCode = true;
                    formatList.setSelectedIndex(size-1);
                } finally {
                    formatChangeFromCode = false;
                }
            }
        }
        firePropertyChange();
    }//GEN-LAST:event_formatFieldFocusLost

    private void formatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatButtonActionPerformed
        updatePreview();
    }//GEN-LAST:event_formatButtonActionPerformed

    private void exampleComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_exampleComboItemStateChanged
        updatePreview();
    }//GEN-LAST:event_exampleComboItemStateChanged

    /** Index of selected format - used to force non-empty selection of format. */
    private int lastFormat = 0;
    private void formatListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_formatListValueChanged
        if (formatChangeFromCode) return;
        FormatInfo info = (FormatInfo)formatList.getSelectedValue();
        if (info == null) {
            formatList.setSelectedIndex(lastFormat);
            return;
        }
        lastFormat = formatList.getSelectedIndex();
        //exampleCombo.setEnabled(info != null);
        //formatButton.setEnabled(info != null);
        if (info == null) {
            formatField.setText(null);
            formatField.setEnabled(false);
            previewField.setText(null);
        } else {
            String format = info.getFormat();
            if ((format == null) && !info.isCustomizable()) {
                formatField.setText(info.getDisplayName());
                formatField.setEnabled(false);
            } else {
                formatField.setText(info.getFormat());
                formatField.setEnabled(true);
                updatePreview();
            }
        }
        firePropertyChange();
    }//GEN-LAST:event_formatListValueChanged

    private void categoryListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_categoryListValueChanged
        fillExampleCombo();
        fillFormatList();
    }//GEN-LAST:event_categoryListValueChanged

    /**
     * Updates preview of format. 
     */
    private void updatePreview() {
        int category = categoryList.getSelectedIndex();
        try {
            Object selectedItem = exampleCombo.getSelectedItem();
            String preview = ""; // NOI18N
            if (selectedItem != null) {
                String selected = selectedItem.toString();
                if (category == 1) {
                    DateFormat original = DateFormat.getDateInstance();
                    SimpleDateFormat f = new SimpleDateFormat(formatField.getText());
                    preview = f.format(original.parse(selected));
                } else if (category == 2) {
                    DateFormat original = DateFormat.getTimeInstance();
                    SimpleDateFormat f = new SimpleDateFormat(formatField.getText());
                    preview = f.format(original.parse(selected));
                } else if (category == 5) {
                    MaskFormatter f = new MaskFormatter(formatField.getText());
                    preview = f.valueToString(selected);
                } else {
                    DecimalFormat original = new DecimalFormat("0.###"); // NOI18N
                    DecimalFormat f = new DecimalFormat(formatField.getText());
                    preview = f.format(original.parse(selected));

                }
            }
            previewField.setText(preview);
        } catch (ParseException pex) {
            previewField.setText(pex.getMessage());
        } catch (IllegalArgumentException iaex) {
            previewField.setText(iaex.getMessage());
        }
    }

    /** Index of selected category - used to force non-empty selection of category. */
    private int lastCategory = 0;
    /**
     * Fills format list according to selected category. 
     */
    private void fillFormatList() {
        int index = categoryList.getSelectedIndex();
        if (index == -1) {
            categoryList.setSelectedIndex(lastCategory);
            return;
        }
        lastCategory = index;
        DefaultListModel model = new DefaultListModel();
        for (FormatInfo format : formats[index]) {
            model.addElement(format);
        }
        ((FormatInfoRenderer)formatList.getCellRenderer()).reinitialize(model);
        formatList.setModel(model);
        formatList.setSelectedIndex(0);
    }

    /**
     * Fills example combo according to selected category. 
     */
    private void fillExampleCombo() {
        int index = categoryList.getSelectedIndex();
        if (index == -1) return;
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String example : examples[index]) {
            model.addElement(example);
        }        
        formatChangeFromCode = true;
        try {
            exampleCombo.setModel(model);
        } finally {
            formatChangeFromCode = false;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList categoryList;
    private javax.swing.JScrollPane categoryPane;
    private javax.swing.JLabel categoryTitle;
    private javax.swing.JComboBox exampleCombo;
    private javax.swing.JLabel exampleLabel;
    private javax.swing.JPanel examplePanel;
    private javax.swing.JButton formatButton;
    private javax.swing.JTextField formatField;
    private javax.swing.JLabel formatLabel;
    private javax.swing.JList formatList;
    private javax.swing.JScrollPane formatPane;
    private javax.swing.JLabel formatTitle;
    private javax.swing.JTextField previewField;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel selector;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns UI component representing the selector.
     * 
     * @return UI component representring the selector. 
     */
    public JPanel getSelectorPanel() {
        return selector;
    }

    /**
     * Information about format. 
     */
    public static class FormatInfo {
        /** Number format (type). */
        public static final int NUMBER = 0;
        /** Date format (type). */
        public static final int DATE = 1;
        /** Time format (type). */
        public static final int TIME = 2;
        /** Percent format (type). */
        public static final int PERCENT = 3;
        /** Currency format (type). */
        public static final int CURRENCY = 4;
        /** Mask format (type). */
        public static final int MASK = 5;
        /** No special subtype. */
        public static final int NONE = -1;
        /** Default format (subtype). */
        public static final int DEFAULT = 0;
        /** Short format (subtype). */
        public static final int SHORT = 1;
        /** Medium format (subtype). */
        public static final int MEDIUM = 2;
        /** Long format (subtype). */
        public static final int LONG = 3;
        /** Full format (subtype). */
        public static final int FULL = 4;
        /** Integer format (subtype). */
        public static final int INTEGER = 1;
        /** Type of the format. */
        private int type;
        /** Subtype of the format. */
        private int subtype;
        /** Display name of the format. */
        private String displayName;
        /** Example formatted by this format. */
        private String example;
        /** Format's pattern. */
        private String format;
        /** Formatter. */
        private JFormattedTextField.AbstractFormatter formatter;
        
        /**
         * Creates new <code>FormatInfo</code>.
         * 
         * @param type type of the format.
         * @param subtype subtype of the format.
         * @param displayName display name of the format.
         * @param formatter formatter.
         */
        FormatInfo(int type, int subtype, String displayName, JFormattedTextField.AbstractFormatter formatter) {
            this.type = type;
            this.subtype = subtype;
            this.displayName = displayName;
            this.formatter = formatter;
        }

        /**
         * Creates new <code>FormatInfo</code>.
         * 
         * @param type type of the format.
         * @param subtype subtype of the format.
         * @param format pattern of the format.
         */
        public FormatInfo(int type, int subtype, String format) {
            this.type = type;
            this.subtype = subtype;
            this.format = format;
        }

        /**
         * Determines whether this format is customizable.
         * 
         * @return <code>true</code> if the format is customizable,
         * returns <code>false</code> otherwise.
         */
        boolean isCustomizable() {
            return subtype == NONE;
        }

        /**
         * Returns display name of the format.
         * 
         * @return display name of the format.
         */
        String getDisplayName() {
            return displayName;
        }

        /**
         * Returns example formatted by this format.
         * 
         * @return example formatted by this format.
         */
        String getExample() {
            return example;
        }

        /**
         * Calculates example.
         * 
         * @param value value used to calculate example.
         */
        void calculateExample(Object value) {
            if (formatter == null) return;
            try {
                example = formatter.valueToString(value);
            } catch (ParseException pex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, pex.getMessage(), pex);
            }
        }

        /**
         * Returns format's pattern. 
         * 
         * @return format's pattern.
         */
        public String getFormat() {
            if (format != null) return format;
            String fmt = null;
            if (formatter instanceof MaskFormatter) {
                fmt = ((MaskFormatter)formatter).getMask();
            } else if (formatter instanceof InternationalFormatter) {
                Format f = ((InternationalFormatter)formatter).getFormat();
                if (f instanceof DecimalFormat) {
                    fmt = ((DecimalFormat)f).toPattern();
                } else if (f instanceof SimpleDateFormat) {
                    fmt = ((SimpleDateFormat)f).toPattern();
                }
            }
            return fmt;
        }

        /**
         * Returns type of this format. 
         * 
         * @return type of this format.
         */
        public int getType() {
            return type;
        }

        /**
         * Returns subtype of this format.
         * 
         * @return subtype of this format. 
         */
        public int getSubtype() {
            return subtype;
        }

    }

    /**
     * Renderer used in format list. 
     */
    static class FormatInfoRenderer implements ListCellRenderer {
        /** Delegate renderer used to have rendering consistent with the L&F in use. */
        private DefaultListCellRenderer delegate;
        /** The actual list cell renderer component. */
        private JPanel panel;
        /** Label used to render the second column (the first column is rendered by delegate's label). */
        private JLabel label;
        /** Width of the first column. */
        private int width1;
        /** Width of the second column. */
        private int width2;
    
        /**
         * Creates new <code>FormatInfoRenderer</code>.
         */
        FormatInfoRenderer() {
            panel = new JPanel();
            BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
            panel.setLayout(layout);
            label = new JLabel();
            label.setOpaque(false);
            delegate = new DefaultListCellRenderer();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            FormatInfo info = (FormatInfo)value;
            Component comp = delegate.getListCellRendererComponent(list, info.getDisplayName(), index, isSelected, cellHasFocus);
            if (comp instanceof JComponent) {
                JComponent jcomp = (JComponent)comp;
                Border border = jcomp.getBorder();
                if ((border != null) && (border.getBorderInsets(panel) != null)) { // Issue 161997
                    panel.setBorder(border);
                } else {
                    panel.setBorder(BorderFactory.createEmptyBorder());
                }
                panel.setOpaque(jcomp.isOpaque());
                jcomp.setBorder(null);
            }
            panel.removeAll();
            panel.setBackground(comp.getBackground());
            // 1st column            
            comp.setPreferredSize(null);
            Dimension prefSize = comp.getPreferredSize();
            comp.setPreferredSize(new Dimension(width1, prefSize.height));
            comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            panel.add(comp);
            panel.add(Box.createHorizontalStrut(5));
            // 2nd column
            label.setText(info.getExample());
            label.setForeground(comp.getForeground());
            label.setFont(comp.getFont());
            label.setPreferredSize(null);
            prefSize = label.getPreferredSize();
            label.setPreferredSize(new Dimension(width2, prefSize.height));
            label.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            panel.add(label);
            return panel;
        }

        /**
         * Recalculates the preferred widths of columns.
         * 
         * @param model list model used for recalculation.
         */
        private void reinitialize(ListModel model) {
            label.setPreferredSize(null);
            width1 = 0;
            width2 = 0;
            for (int i=0; i<model.getSize(); i++) {
                FormatInfo info = (FormatInfo)model.getElementAt(i);
                label.setText(info.getDisplayName());
                width1 = Math.max(width1, label.getPreferredSize().width);
                label.setText(info.getExample());
                width2 = Math.max(width2, label.getPreferredSize().width);
            }
        }
    }

}
