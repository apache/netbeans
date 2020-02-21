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

package org.netbeans.modules.cnd.asm.core.assistance;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.modules.cnd.asm.core.ui.top.RegisterUsagesPanel;
import org.netbeans.modules.editor.NbEditorUtilities;

public class LiveRangeSidebarFactory implements SideBarFactory {

    public static final String LIVE_RANGE_SIDEBAR = "LIVE_RANGE_SIDEBAR"; // NOI18N
    
    public JComponent createSideBar(JTextComponent target) {        
        CodeAnnotationSidebar sb = new CodeAnnotationSidebar(target);        
        target.putClientProperty(LIVE_RANGE_SIDEBAR, sb);
        
        new RegisterUsageAssistance(target, RegisterUsagesPanel.getInstance());
                
        BaseDocument doc = (BaseDocument) target.getDocument();
        
        LiveRangesAssistance ass = new LiveRangesAssistance(doc);
        doc.putProperty(RegisterChooserListener.class, ass);

        RegisterUsagesPanel.getInstance().setDocument(NbEditorUtilities.getDataObject(doc));
        
        return sb;
    }  
}
