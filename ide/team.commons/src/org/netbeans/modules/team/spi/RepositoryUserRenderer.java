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

package org.netbeans.modules.team.spi;

import java.awt.Component;
import java.text.MessageFormat;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.openide.util.NbBundle;

/**
 * Renderer of <code>RepositoryUser</code>.
 *
 * @author Jan Stola
 */
public final class RepositoryUserRenderer extends DefaultListCellRenderer {
    private final String pattern;
    
    public RepositoryUserRenderer() {
        pattern = NbBundle.getMessage(RepositoryUserRenderer.class, "RepositoryUserRenderer.format"); // NOI18N
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof RepositoryUser) {
            RepositoryUser user = (RepositoryUser)value;
            value = MessageFormat.format(pattern, user.getFullName(), user.getUserName());
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

}
