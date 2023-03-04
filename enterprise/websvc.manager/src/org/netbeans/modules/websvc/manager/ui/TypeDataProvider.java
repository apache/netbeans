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


package org.netbeans.modules.websvc.manager.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.swing.outline.RenderDataProvider;

/**
 *
 * @author  David Botterill
 */
public class TypeDataProvider implements RenderDataProvider {

    /** Creates a new instance of TypeDataProvider */
    public TypeDataProvider() {
    }

    public java.awt.Color getBackground(Object o) {

        return null;
    }

    public String getDisplayName(Object inNode) {
        if(null == inNode) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return null;
        TypeNodeData data = (TypeNodeData)node.getUserObject();
        return data.getRealTypeName();

    }

    public java.awt.Color getForeground(Object o) {
        return null;
    }

    public javax.swing.Icon getIcon(Object o) {
        return null;
    }

    public String getTooltipText(Object o) {
        return null;
    }

    public boolean isHtmlDisplayName(Object o) {
        return false;
    }

}
