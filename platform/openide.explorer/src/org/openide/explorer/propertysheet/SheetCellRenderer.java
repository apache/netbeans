/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
* SheetCellRenderer.java
*
* Created on April 22, 2003, 5:35 PM
*/
package org.openide.explorer.propertysheet;

import org.openide.awt.HtmlRenderer;
import org.openide.nodes.Node.*;

import java.awt.*;

import java.beans.FeatureDescriptor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import org.openide.util.ImageUtilities;


/** An implementation of SheetCellRenderer that wraps custom InplaceEditors
 * to efficiently render properties.
 *
 * @author  Tim Boudreau
 */
final class SheetCellRenderer implements TableCellRenderer {
    private RendererFactory factory = null;
    private boolean tableUI;
    boolean includeMargin = false;
    private ReusablePropertyEnv reusableEnv;
    private ReusablePropertyModel reusableModel;
    boolean suppressButton = false;
    int rbMax = 0;
    private JLabel htmlLabel = HtmlRenderer.createLabel();

    SheetCellRenderer(boolean tableUI, ReusablePropertyEnv env, ReusablePropertyModel reusableModel) {
        this.tableUI = tableUI;
        this.reusableEnv = env;
        this.reusableModel = reusableModel;
    }

    void setIncludeMargin(boolean val) {
        includeMargin = val;
    }

    void setSuppressButton(boolean val) {
        suppressButton = val;
    }

    void setRadioButtonMax(int i) {
        rbMax = i;
    }

    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean selected, boolean hasFocus, int row, int column
    ) {
        FeatureDescriptor fd = (FeatureDescriptor) value;
        Component result;

        //Use selection color for both columns
        selected |= (hasFocus && (table.getSelectedRow() == row));

        if (fd == null || fd instanceof PropertySet) {
            //#118372 - don't return null, SynthLaF asks for some properties from 
            //the renderer component under JDK1.6_05
            return new JLabel();
        } else {
            if (column == 0) {
                String txt = ((Property) fd).getHtmlDisplayName();
                boolean isHtml = txt != null;

                if (!isHtml) {
                    txt = fd.getDisplayName();
                }

                JLabel lbl = htmlLabel;

                HtmlRenderer.Renderer ren = (HtmlRenderer.Renderer) lbl;

                ren.setHtml(isHtml);

                lbl.setText(txt);

                if (selected) {
                    lbl.setBackground(table.getSelectionBackground());
                    lbl.setForeground(table.getSelectionForeground());
                } else {
                    lbl.setBackground(table.getBackground());
                    lbl.setForeground(table.getForeground());
                }

                lbl.setOpaque(selected);

                if (includeMargin) {
                    lbl.setBorder(
                        BorderFactory.createMatteBorder(0, PropUtils.getMarginWidth() + 2, 0, 0, lbl.getBackground())
                    );
                } else {
                    lbl.setBorder(
                        BorderFactory.createMatteBorder(0, PropUtils.getTextMargin(), 0, 0, lbl.getBackground())
                    );
                }

                //Support for name marking with icon requested by form editor
                Object o = fd.getValue("nameIcon"); //NOI18N

                if (o instanceof Icon) {
                    lbl.setIcon((Icon) o);
                } else if (o instanceof Image) {
                    lbl.setIcon(ImageUtilities.image2Icon((Image) o));
                } else {
                    lbl.setIcon(null);
                }

                result = lbl;
            } else {
                result = factory().getRenderer((Property) fd);

                if (selected) {
                    result.setBackground(table.getSelectionBackground());
                    result.setForeground(table.getSelectionForeground());
                } else {
                    result.setBackground(table.getBackground());
                    result.setForeground(table.getForeground());
                }

                ((JComponent) result).setOpaque(selected);
            }
        }

        return result;
    }

    RendererFactory factory() {
        if (factory == null) {
            factory = new RendererFactory(true, reusableEnv, reusableModel);
        }

        return factory;
    }
}
