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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

/**
 * SecurityFactory.java
 *
 * Factory for creating instances of SecurityRolesPanel, SecurityConstraintPanel
 * and LoginConfigPanel.
 *
 * @author ptliu
 */
public class SecurityFactory implements InnerPanelFactory {
    private ToolBarDesignEditor editor;
    private DDDataObject dObj;
    
    
    /**
     * Creates a new instance of SecurityFactory
     */
    public SecurityFactory(ToolBarDesignEditor editor, DDDataObject dObj) {
        this.editor = editor;
        this.dObj = dObj;
    }

    public SectionInnerPanel createInnerPanel(Object key) { 
        if (key.equals("security_roles")) {  //NOI18N
            return new SecurityRolesPanel((SectionView) editor.getContentView(), dObj);
        } else if (key instanceof SecurityConstraint) {
            return new SecurityConstraintPanel((SectionView) editor.getContentView(), dObj,
                    (SecurityConstraint) key);
        } else if (key.equals("login_config")) { //NOI18N
            return new LoginConfigPanel((SectionView) editor.getContentView(), dObj);
        }
        
        return null;
    } 
}
