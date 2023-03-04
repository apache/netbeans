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

package org.netbeans.lib.profiler.ui.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.swing.renderer.JavaNameRenderer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
final class JDBCJavaNameRenderer extends JavaNameRenderer {
    
    private static final Icon SQL_ICON = Icons.getIcon(ProfilerIcons.SQL_QUERY);
    private static final Icon SQL_ICON_DISABLED = UIManager.getLookAndFeel().getDisabledIcon(null, SQL_ICON);
    private static final Icon LEAF_ICON = Icons.getIcon(ProfilerIcons.NODE_LEAF);
    private static final Icon LEAF_ICON_DISABLED = UIManager.getLookAndFeel().getDisabledIcon(null, LEAF_ICON);
    
    private final Icon icon;
    private final Icon iconDisabled;
    
    private String currentValue;
    private JLabel currentSQLRenderer;
    
    public JDBCJavaNameRenderer() {
        this(ProfilerIcons.NODE_REVERSE);
    }
    
    public JDBCJavaNameRenderer(String iconKey) {
        this.icon = Icons.getIcon(iconKey);
        this.iconDisabled = UIManager.getLookAndFeel().getDisabledIcon(null, icon);
    }
    
    public void setValue(Object value, int row) {
        if (value instanceof PresoObjAllocCCTNode) {
            PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)value;
            
            boolean filtered = node.isFiltered();
            currentValue = node.getNodeName();
            
            if (JDBCTreeTableView.isSQL(node)) {
                JDBCTreeTableView.SQLQueryNode sqlNode = (JDBCTreeTableView.SQLQueryNode)node;
                String htmlName = sqlNode.htmlName;
                if (htmlName == null) {
                    htmlName = SQLFormatter.format(currentValue);
                    sqlNode.htmlName = htmlName;
                }
                currentSQLRenderer = sqlRenderer(htmlName, filtered ? SQL_ICON_DISABLED : SQL_ICON);
            } else {
                if (filtered) {
                    setNormalValue(""); // NOI18N
                    setBoldValue(""); // NOI18N
                    setGrayValue(currentValue);
                } else {
                    super.setValue(currentValue, row);
                }

                if (node.isLeaf()) {
                    setIcon(filtered ? LEAF_ICON_DISABLED : LEAF_ICON);
                } else {
                    setIcon(filtered ? iconDisabled : icon);
                }
                currentSQLRenderer = null;
            }
        } else {
            super.setValue(value, row);
        }
    }
    
    public JComponent getComponent() {
        return currentSQLRenderer != null ? currentSQLRenderer : super.getComponent();
    }
    
    public String toString() {
        return currentValue;
    }
    
    
    private static int CACHE_SIZE = 100;
    private List<String> sqlRenderersKeys;
    private Map<String, JLabel> sqlRenderersCache;
    
    private JLabel sqlRenderer(String text, Icon icon) {
        if (sqlRenderersCache == null) {
            sqlRenderersKeys = new ArrayList(CACHE_SIZE);
            sqlRenderersCache = new HashMap(CACHE_SIZE);
        }
        
        JLabel sqlRenderer = sqlRenderersCache.get(text);
        
        if (sqlRenderer == null) {
            if (sqlRenderersKeys.size() < CACHE_SIZE) {
                sqlRenderer = new DefaultTableCellRenderer();
            } else {
                String key = sqlRenderersKeys.remove(0);
                sqlRenderer = sqlRenderersCache.remove(key);
            }
            
            sqlRenderersKeys.add(text);
            sqlRenderersCache.put(text, sqlRenderer);
            
            sqlRenderer.setText(text);
        }
        
        sqlRenderer.setIcon(icon);
        
        return sqlRenderer;
    }
    
}
