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
package org.netbeans.modules.php.analysis.ui;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;

public class PHPStanLevelListCellRenderer implements ListCellRenderer<String> {

    private final ListCellRenderer<? super String> defaultRenderer;

    public PHPStanLevelListCellRenderer(ListCellRenderer<? super String> defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        String level = value;
        if (String.valueOf(AnalysisOptions.PHPSTAN_MAX_LEVEL).equals(level)) {
            level += " (max)"; // NOI18N
        }
        return defaultRenderer.getListCellRendererComponent(list, level, index, isSelected, cellHasFocus);
    }

}
