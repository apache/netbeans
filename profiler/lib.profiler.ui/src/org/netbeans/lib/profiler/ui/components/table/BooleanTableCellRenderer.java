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

package org.netbeans.lib.profiler.ui.components.table;

import org.netbeans.lib.profiler.ui.components.*;
import java.awt.*;
import javax.swing.*;


/**
 *
 * @author  Jiri Sedlacek
 */
public class BooleanTableCellRenderer extends EnhancedTableCellRenderer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JCheckBox checkBox;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BooleanTableCellRenderer */
    public BooleanTableCellRenderer() {
        super();
        super.setLayout(new BorderLayout(0, 0));

        checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(JCheckBox.CENTER);
        checkBox.setOpaque(false);

        add(checkBox, BorderLayout.CENTER);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Component getTableCellRendererComponentPersistent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                             int row, int column) {
        return null;
    }

    protected void setState(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if ((supportsFocusBorder) && (hasFocus) && (isSelected)) {
            checkBox.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
        } else {
            checkBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
    }

    protected void setValue(javax.swing.JTable table, Object value, int row, int column) {
        checkBox.setSelected(((value != null) && ((Boolean) value).booleanValue()));
    }
}
