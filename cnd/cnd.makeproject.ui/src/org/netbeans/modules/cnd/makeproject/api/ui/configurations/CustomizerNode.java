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

package org.netbeans.modules.cnd.makeproject.api.ui.configurations;

import javax.swing.JComponent;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public class CustomizerNode {
    public static final String iconbase = "org/netbeans/modules/cnd/makeproject/ui/resources/general"; // NOI18N
    public static final String icon = "org/netbeans/modules/cnd/makeproject/ui/resources/general.gif"; // NOI18N

    private final String name;
    private final String displayName;
    private CustomizerNode[] children;
    protected final Lookup lookup;

    public enum CustomizerStyle {SHEET, PANEL};
        
    public final MakeContext getContext(){
        return lookup.lookup(MakeContext.class);
    }

    public CustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        this.name = name;
        this.displayName = displayName;
        this.children = children;
        this.lookup = lookup;
    }
    
    public CustomizerStyle customizerStyle() {
        return CustomizerStyle.SHEET; // Backward compatible
    }

    public Sheet[] getSheets(Configuration configuration) {
        return null;
    }
    
    public JComponent getPanel(Configuration configuration) {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(""); // NOI18N // See CR 6718766
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CustomizerNode[] getChildren() {
        return children;
    }
    
    public void setChildren(CustomizerNode[] children) {
        this.children = children;
    }
}
