/**
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

package org.netbeans.installer.products.tomcat.wizard.panels;

import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;

/**
 *
 
 */
public class TomcatPanel extends DestinationPanel {
    
    
    public TomcatPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);       
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(TomcatPanel.class,
            "TP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(TomcatPanel.class,
            "TP.description"); // NOI18N
    
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(TomcatPanel.class,
            "TP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(TomcatPanel.class,
            "TP.destination.button.text"); // NOI18N  
}
