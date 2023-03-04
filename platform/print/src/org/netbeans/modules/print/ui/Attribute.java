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
package org.netbeans.modules.print.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.openide.DialogDescriptor;
import org.netbeans.modules.print.util.Percent;
import org.netbeans.modules.print.util.Macro;
import org.netbeans.modules.print.util.Config;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.02.14
 */
final class Attribute extends Dialog implements Macro.Listener, Percent.Listener {

    Attribute(Preview preview) {
        myPreview = preview;

        myBorderColorValue = Config.getDefault().getBorderColor();
        myTextColorValue = Config.getDefault().getTextColor();
        myTextFontValue = Config.getDefault().getTextFont();
        myBackgroundColorValue = Config.getDefault().getBackgroundColor();

        myHeaderColorValue = Config.getDefault().getHeaderColor();
        myHeaderFontValue = Config.getDefault().getHeaderFont();
        myFooterColorValue = Config.getDefault().getFooterColor();
        myFooterFontValue = Config.getDefault().getFooterFont();
    }

    public void invalidValue(String value) {
//out("INVALID value: " + value);
        printError(i18n("ERR_Zoom_Value_Is_Invalid")); // NOI18N
    }

    @Override
    protected DialogDescriptor createDescriptor() {
        myDescriptor = new DialogDescriptor(
            getResizableX(createPanel()),
            i18n("LBL_Print_Options"), // NOI18N
            true,
            getButtons(),
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (DialogDescriptor.OK_OPTION == event.getSource()) {
                        if (updatePreview()) {
                            myDescriptor.setClosingOptions(new Object[] { DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION });
                        }
                        else {
                            myDescriptor.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
                        }
                    }
                }
            }
        );
        return myDescriptor;
    }

    private Object[] getButtons() {
        return new Object[] {
            DialogDescriptor.OK_OPTION,
            createButton(new ButtonAction(i18n("LBL_Apply"), i18n("TLT_Apply")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    updatePreview();
                }
            }),
            DialogDescriptor.CANCEL_OPTION
        };
    }

    private boolean updatePreview() {
        int zoomWidth = getInt(myZoomWidth.getText());
        int zoomHeight = getInt(myZoomHeight.getText());

        if ( !checkValue(zoomWidth, zoomHeight)) {
            return false;
        }
        Config.getDefault().setBorder(myBorder.isSelected());
        Config.getDefault().setBorderColor(myBorderColorValue);

        Config.getDefault().setHeader(myHeader.isSelected());
        Config.getDefault().setHeaderLeft(myHeaderLeft.getText());
        Config.getDefault().setHeaderCenter(myHeaderCenter.getText());
        Config.getDefault().setHeaderRight(myHeaderRight.getText());
        Config.getDefault().setHeaderColor(myHeaderColorValue);
        Config.getDefault().setHeaderFont(myHeaderFontValue);

        Config.getDefault().setFooter(myFooter.isSelected());
        Config.getDefault().setFooterLeft(myFooterLeft.getText());
        Config.getDefault().setFooterCenter(myFooterCenter.getText());
        Config.getDefault().setFooterRight(myFooterRight.getText());
        Config.getDefault().setFooterColor(myFooterColorValue);
        Config.getDefault().setFooterFont(myFooterFontValue);

        Config.getDefault().setWrapLines(myWrapLines.isSelected());
        Config.getDefault().setLineNumbers(myLineNumbers.isSelected());
        Config.getDefault().setUseFont(myUseFont.isSelected());
        Config.getDefault().setUseColor(myUseColor.isSelected());
        Config.getDefault().setTextColor(myTextColorValue);
        Config.getDefault().setTextFont(myTextFontValue);
        Config.getDefault().setBackgroundColor(myBackgroundColorValue);
        Config.getDefault().setLineSpacing(getDouble(myLineSpacing.getValue()));
        Config.getDefault().setSelection(mySelection.isSelected());
        Config.getDefault().setAsEditor(myAsEditor.isSelected());

        double zoom = 0.0;

        if (myZoomFactor.isEnabled()) {
            zoom = myZoomFactor.getValue();
        }
        else if (myZoomWidth.isEnabled()) {
            zoom = Percent.createZoomWidth(zoomWidth);
        }
        else if (myZoomHeight.isEnabled()) {
            zoom = Percent.createZoomHeight(zoomHeight);
        }
        else if (myFitToPage.isSelected()) {
            zoom = 0.0;
        }
        Config.getDefault().setZoom(zoom);
        myPreview.updated();
//out("SET zoom: " + zoom);

        return true;
    }

    private boolean checkValue(int zoomWidth, int zoomHeight) {
        if (myHeaderFontValue.getSize() > MAX_HEADER_LENGTH) {
            printError(i18n("ERR_Header_Size_Is_Too_Big")); // NOI18N
            return false;
        }
        if (myFooterFontValue.getSize() > MAX_FOOTER_LENGTH) {
            printError(i18n("ERR_Footer_Size_Is_Too_Big")); // NOI18N
            return false;
        }
        if (zoomWidth <= 0 || zoomHeight <= 0) {
            printError(i18n("ERR_Page_Number_Is_Invalid")); // NOI18N
            return false;
        }
        if (zoomWidth > MAX_PAGE_NUBER || zoomHeight > MAX_PAGE_NUBER) {
            printError(i18n("ERR_Page_Number_Is_Too_Big")); // NOI18N
            return false;
        }
        return true;
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;

        createSection(getBorderSection(), "LBL_Border", panel, c); // NOI18N
        createSection(getTitleSection(), "LBL_Header_Footer", panel, c); // NOI18N
        createSection(getTextSection(), "LBL_Text", panel, c); // NOI18N
        createSection(getZoomSection(), "LBL_Zoom", panel, c); // NOI18N

        updateControl();

        return panel;
    }

    private void createSection(JPanel section, String key, JPanel panel, GridBagConstraints c) {
        c.insets = new Insets(TINY_SIZE, 0, 0, 0);
        panel.add(createSeparator(i18n(key)), c);
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(section, c);
        c.insets = new Insets(0, 0, 0, 0);
    }

    private JPanel getBorderSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;

        // border
        myBorder = createCheckBox(
            new ButtonAction(i18n("LBL_Print_Border")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    myBorderColor.setEnabled(myBorder.isSelected());
                }
            }
        );
        panel.add(myBorder, c);

        // border.color
        c.weightx = 1.0;
        c.insets = new Insets(0, LARGE_SIZE, TINY_SIZE, 0);
        myBorderColor = createButton(
            new ButtonAction(
            icon(Config.class, "color"), // NOI18N
            i18n("TLT_Border_Color")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    borderColor();
                }
            }
        );
        panel.add(myBorderColor, c);

        return panel;
    }

    private JPanel getTitleSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        setLabelPanel(panel, c);
        setHeaderPanel(panel, c);
        setFooterPanel(panel, c);
        setMacroPanel(panel, c);
//      panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.red));

        return panel;
    }

    private void setLabelPanel(JPanel panel, GridBagConstraints c) {
        // []
        c.gridy++;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, LARGE_SIZE, TINY_SIZE, 0);
        panel.add(new JLabel(), c);

        // left
        panel.add(createLabel(i18n("LBL_Left")), c); // NOI18N

        // center
        panel.add(createLabel(i18n("LBL_Center")), c); // NOI18N

        // right
        panel.add(createLabel(i18n("LBL_Right")), c); // NOI18N
    }

    private void setHeaderPanel(JPanel panel, GridBagConstraints c) {
        // header
        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        myHeader = createCheckBox(
            new ButtonAction(i18n("LBL_Print_Header")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    boolean enabled = myHeader.isSelected();
                    myHeaderLeft.setEnabled(enabled);
                    myHeaderCenter.setEnabled(enabled);
                    myHeaderRight.setEnabled(enabled);
                    myHeaderColor.setEnabled(enabled);
                    myHeaderFont.setEnabled(enabled);
                }
            }
        );
        panel.add(myHeader, c);

        // header left
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        myHeaderLeft = new JTextField();
        setWidth(myHeaderLeft, FIELD_WIDTH);
        panel.add(myHeaderLeft, c);
        myLastField = myHeaderLeft;

        // header center
        myHeaderCenter = new JTextField();
        setWidth(myHeaderCenter, FIELD_WIDTH);
        panel.add(myHeaderCenter, c);

        // header right
        myHeaderRight = new JTextField();
        setWidth(myHeaderRight, FIELD_WIDTH);
        panel.add(myHeaderRight, c);

        // header.color
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        myHeaderColor = createButton(
            new ButtonAction(
            icon(Config.class, "color"), // NOI18N
            i18n("TLT_Header_Color")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    headerColor();
                }
            }
        );
        panel.add(myHeaderColor, c);

        // header font
        myHeaderFont = createButton(
            new ButtonAction(icon(Config.class, "font"), i18n("TLT_Header_Font")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    headerFont();
                }
            }
        );
        panel.add(myHeaderFont, c);
    }

    private void setFooterPanel(JPanel panel, GridBagConstraints c) {
        // footer
        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        myFooter = createCheckBox(
            new ButtonAction(i18n("LBL_Print_Footer")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    boolean enabled = myFooter.isSelected();
                    myFooterLeft.setEnabled(enabled);
                    myFooterCenter.setEnabled(enabled);
                    myFooterRight.setEnabled(enabled);
                    myFooterColor.setEnabled(enabled);
                    myFooterFont.setEnabled(enabled);
                }
            }
        );
        panel.add(myFooter, c);

        c.weightx = 1.0;
        c.insets = new Insets(0, LARGE_SIZE, TINY_SIZE, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        myFooterLeft = new JTextField();
        setWidth(myFooterLeft, FIELD_WIDTH);
        panel.add(myFooterLeft, c);

        // footer center
        myFooterCenter = new JTextField();
        setWidth(myFooterCenter, FIELD_WIDTH);
        panel.add(myFooterCenter, c);

        // footer right
        myFooterRight = new JTextField();
        setWidth(myFooterRight, FIELD_WIDTH);
        panel.add(myFooterRight, c);

        // footer color
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        myFooterColor = createButton(
            new ButtonAction(
            icon(Config.class, "color"), // NOI18N
            i18n("TLT_Footer_Color")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    footerColor();
                }
            }
        );
        panel.add(myFooterColor, c);

        // footer font
        myFooterFont = createButton(
            new ButtonAction(icon(Config.class, "font"), i18n("TLT_Footer_Font")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    footerFont();
                }
            }
        );
        panel.add(myFooterFont, c);
    }

    private void setMacroPanel(JPanel panel, GridBagConstraints c) {
        JPanel p = new JPanel(new GridBagLayout());

        // macros
        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.CENTER;
        panel.add(createLabel(i18n("LBL_Insert_Macros")), c); // NOI18N

        // buttons
        c.anchor = GridBagConstraints.CENTER;

        for (Macro macro : Macro.values()) {
            JButton button = macro.getButton(this);
            setWidth(button, MACROS_WIDTH);
            p.add(button, c);
        }
        //
        c.weightx = 1.0;
        c.insets = new Insets(LARGE_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        c.gridwidth = 1 + 1 + 1;
        panel.add(p, c);
        c.gridwidth = 1;
    }

    public void pressed(Macro macro) {
        JTextField focusable = getFocusableTextField();

        if (focusable == null) {
            myLastField.requestFocus();
        }
        else {
            myLastField = focusable;
        }
//out("Set macro: " + macro);
//out("   select: " + myLastField.getSelectionStart() + " " + myLastField.getSelectionEnd());
        String text = myLastField.getText();
        String head = text.substring(0, myLastField.getSelectionStart());
        String tail = text.substring(myLastField.getSelectionEnd());

        myLastField.setText(head + macro.getName() + tail);
    }

    private JTextField getFocusableTextField() {
        if (myHeaderLeft.hasFocus()) {
            return myHeaderLeft;
        }
        if (myHeaderCenter.hasFocus()) {
            return myHeaderCenter;
        }
        if (myHeaderRight.hasFocus()) {
            return myHeaderRight;
        }
        if (myFooterLeft.hasFocus()) {
            return myFooterLeft;
        }
        if (myFooterCenter.hasFocus()) {
            return myFooterCenter;
        }
        if (myFooterRight.hasFocus()) {
            return myFooterRight;
        }
        return null;
    }

    private JPanel getTextSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;

        createTopTextPanel(panel, c);
        createBottomTextPanel(panel, c);

        return panel;
    }

    private void createTopTextPanel(JPanel panel, GridBagConstraints c) {
        // line numbers
        c.gridy++;
        myLineNumbers = createCheckBox(
            new ButtonAction(i18n("LBL_Line_Numbers")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                }
            }
        );
        panel.add(myLineNumbers, c);

        // use color
        myUseColor = createCheckBox(
            new ButtonAction(i18n("LBL_Use_Color"), i18n("TLT_Use_Color")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                }
            }
        );
        panel.add(myUseColor, c);

        // font and color label
        myTextFontColorLabel = createLabel(i18n("LBL_Text_Font_and_Color")); // NOI18N
        panel.add(myTextFontColorLabel, c);

        // text color
        c.insets = new Insets(0, LARGE_SIZE, TINY_SIZE, 0);
        myTextColor = createButton(
            new ButtonAction(icon(Config.class, "color"), i18n("TLT_Text_Color")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    textColor();
                }
            }
        );
        panel.add(myTextColor, c);

        // text font
        myTextFont = createButton(
            new ButtonAction(icon(Config.class, "font"), i18n("TLT_Text_Font")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    textFont();
                }
            }
        );
        panel.add(myTextFont, c);
    }

    private void createBottomTextPanel(JPanel panel, GridBagConstraints c) {
        // wrap lines
        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        myWrapLines = createCheckBox(
            new ButtonAction(i18n("LBL_Wrap_Lines")) { // NOI18N
                public void actionPerformed(ActionEvent event) {}
            }
        );
        panel.add(myWrapLines, c);

        // use font
        myUseFont = createCheckBox(
            new ButtonAction(i18n("LBL_Use_Font"), i18n("TLT_Use_Font")) { // NOI18N
                public void actionPerformed(ActionEvent event) {}
            }
        );
        panel.add(myUseFont, c);

        // background label
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        myBackgroundColorLabel = createLabel(i18n("LBL_Background_Color")); // NOI18N
        panel.add(myBackgroundColorLabel, c);

        // background color
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, LARGE_SIZE, TINY_SIZE, 0);
        myBackgroundColor = createButton(
            new ButtonAction(
            icon(Config.class, "color"), // NOI18N
            i18n("TLT_Background_Color")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    backgroundColor();
                }
            }
        );
        panel.add(myBackgroundColor, c);

        // as editor
        c.gridy++;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 0, 0);
        myAsEditor = createCheckBox(
            new ButtonAction(i18n("LBL_As_Editor"), i18n("TLT_As_Editor")) { // NOI18N
                public void actionPerformed(ActionEvent event) {}
            }
        );
        myAsEditor.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                updateText();
            }
        });
        panel.add(myAsEditor, c);

        // selection
        mySelection = createCheckBox(
            new ButtonAction(i18n("LBL_Selection"), i18n("TLT_Selection")) { // NOI18N
                public void actionPerformed(ActionEvent event) {}
            }
        );
        panel.add(mySelection, c);

        // line spacing
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        myLineSpacingLabel = createLabel(i18n("LBL_Line_Spacing")); // NOI18N
        panel.add(myLineSpacingLabel, c);

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, LARGE_SIZE, TINY_SIZE, 0);
        double value = Config.getDefault().getLineSpacing();

        if (value < 0) {
            value = 1.0;
        }
        myLineSpacing = new JSpinner(new SpinnerNumberModel(value, SPACING_MIN, SPACING_MAX, SPACING_STEP));
        Dimension size = new Dimension(myLineSpacing.getPreferredSize().width, myTextColor.getPreferredSize().height);
        setSize(myLineSpacing, size);
        myLineSpacingLabel.setLabelFor(myLineSpacing);
        panel.add(myLineSpacing, c);
    }

    private JPanel getZoomSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        ButtonGroup group = new ButtonGroup();
        double zoom = Config.getDefault().getZoom();
        c.anchor = GridBagConstraints.WEST;
//out("GET ZOOM: " + zoom);

        // (o) Fit width to
        c.gridy++;
        c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
        JRadioButton buttonWidth = createRadioButton(i18n("LBL_Fit_Width_to"), i18n("TLT_Fit_Width_to")); // NOI18N
        buttonWidth.addItemListener(createItemListener(true, false, false));
        panel.add(buttonWidth, c);
        group.add(buttonWidth);

        // [width]
        c.insets = new Insets(LARGE_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        myZoomWidth = new JTextField(getString(Percent.getZoomWidth(zoom, 1)));
        setWidth(myZoomWidth, TEXT_WIDTH);
        panel.add(myZoomWidth, c);

        // page(s)
        c.weightx = 1.0;
        panel.add(createLabel(i18n("LBL_Pages")), c); // NOI18N

        // (o) Zoom to
        c.weightx = 0.0;
        c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
        JRadioButton buttonFactor = createRadioButton(i18n("LBL_Zoom_to"), i18n("TLT_Zoom_to")); // NOI18N
        buttonFactor.addItemListener(createItemListener(false, false, true));
        panel.add(buttonFactor, c);
        group.add(buttonFactor);

        // [zoom]
        c.insets = new Insets(LARGE_SIZE, LARGE_SIZE, TINY_SIZE, 0);
//out("ZOOM:"  + Percent.getZoomFactor(zoom, 1.0));
        myZoomFactor = new Percent(this, Percent.getZoomFactor(zoom, 1.0), PERCENTS, 0, null, i18n("TLT_Print_Zoom")); // NOI18N
        panel.add(myZoomFactor, c);

        // (o) Fit height to
        c.gridy++;
        c.weightx = 0.0;
        c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
        JRadioButton buttonHeight = createRadioButton(i18n("LBL_Fit_Height_to"), i18n("TLT_Fit_Height_to")); // NOI18N
        buttonHeight.addItemListener(createItemListener(false, true, false));
        panel.add(buttonHeight, c);
        group.add(buttonHeight);

        // [height]
        c.insets = new Insets(LARGE_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        myZoomHeight = new JTextField(getString(Percent.getZoomHeight(zoom, 1)));
        setWidth(myZoomHeight, TEXT_WIDTH);
        panel.add(myZoomHeight, c);

        // page(s)
        panel.add(createLabel(i18n("LBL_Pages")), c); // NOI18N

        // (o) Fit to page
        c.weightx = 0.0;
        c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
        myFitToPage = createRadioButton(i18n("LBL_Fit_to_Page"), i18n("TLT_Fit_to_Page")); // NOI18N
        myFitToPage.addItemListener(createItemListener(false, false, false));
        panel.add(myFitToPage, c);
        group.add(myFitToPage);

        buttonFactor.setSelected(Percent.isZoomFactor(zoom));
        buttonWidth.setSelected(Percent.isZoomWidth(zoom));
        buttonHeight.setSelected(Percent.isZoomHeight(zoom));
        myFitToPage.setSelected(Percent.isZoomPage(zoom));
//      panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));

        return panel;
    }

    private ItemListener createItemListener(final boolean width, final boolean height, final boolean factor) {
        return new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (myZoomWidth != null) {
                    myZoomWidth.setEnabled(width);
                }
                if (myZoomHeight != null) {
                    myZoomHeight.setEnabled(height);
                }
                if (myZoomFactor != null) {
                    myZoomFactor.setEnabled(factor);
                }
            }
        };
    }

    private void updateAttribute() {}

    private void updateText() {
        boolean enabled = !myAsEditor.isSelected();

        mySelection.setEnabled(enabled);
        myWrapLines.setEnabled(enabled);
        myUseColor.setEnabled(enabled);
        myUseFont.setEnabled(enabled);
        myTextFont.setEnabled(enabled);
        myTextColor.setEnabled(enabled);
        myTextFontColorLabel.setEnabled(enabled);
        myBackgroundColor.setEnabled(enabled);
        myBackgroundColorLabel.setEnabled(enabled);
        myLineSpacing.setEnabled(enabled);
        myLineSpacingLabel.setEnabled(enabled);
    }

    private String getString(int value) {
        if (value < 0) {
            return Integer.toString(-value);
        }
        return Integer.toString(value);
    }

    private void headerFont() {
        Font font = font(myHeaderFontValue);

        if (font != null) {
            myHeaderFontValue = font;
        }
    }

    private void footerFont() {
        Font font = font(myFooterFontValue);

        if (font != null) {
            myFooterFontValue = font;
        }
    }

    private void textFont() {
        Font font = font(myTextFontValue);

        if (font != null) {
            myTextFontValue = font;
        }
    }

    private Font font(Font font) {
        return (Font) new Editor(Font.class, i18n("LBL_Choose_Font"), font).getValue(); // NOI18N
    }

    private void borderColor() {
        Color color = color(myBorderColorValue);

        if (color != null) {
            myBorderColorValue = color;
        }
    }

    private void headerColor() {
        Color color = color(myHeaderColorValue);

        if (color != null) {
            myHeaderColorValue = color;
        }
    }

    private void footerColor() {
        Color color = color(myFooterColorValue);

        if (color != null) {
            myFooterColorValue = color;
        }
    }

    private void textColor() {
        Color color = color(myTextColorValue);

        if (color != null) {
            myTextColorValue = color;
        }
    }

    private void backgroundColor() {
        Color color = color(myBackgroundColorValue);

        if (color != null) {
            myBackgroundColorValue = color;
        }
    }

    private Color color(Color color) {
        return (Color) new Editor(Color.class, i18n("LBL_Choose_Color"), color).getValue(); // NOI18N
    }

    private void updateControl() {
        myBorder.setSelected(Config.getDefault().hasBorder());
        myBorderColor.setEnabled(Config.getDefault().hasBorder());

        myHeader.setSelected(Config.getDefault().hasHeader());
        myHeaderLeft.setText(Config.getDefault().getHeaderLeft());
        myHeaderLeft.setEnabled(Config.getDefault().hasHeader());
        myHeaderCenter.setText(Config.getDefault().getHeaderCenter());
        myHeaderCenter.setEnabled(Config.getDefault().hasHeader());
        myHeaderRight.setText(Config.getDefault().getHeaderRight());
        myHeaderRight.setEnabled(Config.getDefault().hasHeader());
        myHeaderColor.setEnabled(Config.getDefault().hasHeader());
        myHeaderFont.setEnabled(Config.getDefault().hasHeader());

        myFooter.setSelected(Config.getDefault().hasFooter());
        myFooterLeft.setText(Config.getDefault().getFooterLeft());
        myFooterLeft.setEnabled(Config.getDefault().hasFooter());
        myFooterCenter.setText(Config.getDefault().getFooterCenter());
        myFooterCenter.setEnabled(Config.getDefault().hasFooter());
        myFooterRight.setText(Config.getDefault().getFooterRight());
        myFooterRight.setEnabled(Config.getDefault().hasFooter());
        myFooterColor.setEnabled(Config.getDefault().hasFooter());
        myFooterFont.setEnabled(Config.getDefault().hasFooter());

        myLineNumbers.setSelected(Config.getDefault().isLineNumbers());
        myWrapLines.setSelected(Config.getDefault().isWrapLines());
        myUseFont.setSelected(Config.getDefault().isUseFont());
        myUseColor.setSelected(Config.getDefault().isUseColor());
        mySelection.setSelected(Config.getDefault().isSelection());
        myAsEditor.setSelected(Config.getDefault().isAsEditor());

        updateText();
    }

    @Override
    protected void opened() {
        myHeaderLeft.requestFocus();
    }

    public double getCustomValue(int index) {
        return 0.0;
    }

    public void valueChanged(double value, int index) {}

    private double getDouble(Object value) {
        if ( !(value instanceof Double)) {
            return -1.0;
        }
        return ((Double) value).doubleValue();
    }

    private JCheckBox myHeader;
    private JTextField myHeaderLeft;
    private JTextField myHeaderCenter;
    private JTextField myHeaderRight;
    private JButton myHeaderFont;
    private JButton myHeaderColor;
    private Color myHeaderColorValue;
    private Font myHeaderFontValue;
    private JCheckBox myFooter;
    private DialogDescriptor myDescriptor;
    private JTextField myFooterLeft;
    private JTextField myFooterCenter;
    private JTextField myFooterRight;
    private JButton myFooterFont;
    private JButton myFooterColor;
    private Color myFooterColorValue;
    private Font myFooterFontValue;
    private JCheckBox myBorder;
    private JButton myBorderColor;
    private Color myBorderColorValue;
    private JCheckBox myLineNumbers;
    private JCheckBox myWrapLines;
    private JCheckBox myUseFont;
    private JCheckBox myUseColor;
    private JButton myTextFont;
    private JButton myTextColor;
    private JButton myBackgroundColor;
    private JSpinner myLineSpacing;
    private Font myTextFontValue;
    private Color myTextColorValue;
    private Color myBackgroundColorValue;
    private JCheckBox mySelection;
    private JCheckBox myAsEditor;
    private JLabel myTextFontColorLabel;
    private JLabel myBackgroundColorLabel;
    private JLabel myLineSpacingLabel;
    private Percent myZoomFactor;
    private JTextField myZoomWidth;
    private JTextField myZoomHeight;
    private JRadioButton myFitToPage;
    private Preview myPreview;
    private JTextField myLastField;

    private static final int TEXT_WIDTH = 30;
    private static final int FIELD_WIDTH = 136;
    private static final int MACROS_WIDTH = 41;
    private static final int MAX_PAGE_NUBER = 32;
    private static final int MAX_HEADER_LENGTH = 100;
    private static final int MAX_FOOTER_LENGTH = 100;
    private static final double SPACING_MIN = 0.1;
    private static final double SPACING_MAX = 10.0;
    private static final double SPACING_STEP = 0.1;
    private static final int[] PERCENTS = new int[] { 25, 50, 75, 100, 125, 150, 200, 300, 500 };
}
