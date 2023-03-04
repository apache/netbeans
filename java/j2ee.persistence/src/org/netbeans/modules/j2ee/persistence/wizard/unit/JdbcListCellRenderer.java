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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class JdbcListCellRenderer extends DefaultListCellRenderer {
    
    public JdbcListCellRenderer() {
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        // values might be DatabaseConnections or Strings (for custom connections)
        String text = null;
        if(value == null || String.valueOf(value).length() == 0) {
            text = NbBundle.getMessage(JdbcListCellRenderer.class, "LBL_NoAvailableConnection");
        } else {
            text = String.valueOf(value);
        }
        if (value instanceof DatabaseConnection) {
            DatabaseConnection connection = (DatabaseConnection) value;
            text = connection.getName();
        }

        setText(text);
        return this;
    }
    
}
