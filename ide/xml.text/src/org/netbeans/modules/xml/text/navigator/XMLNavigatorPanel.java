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

package org.netbeans.modules.xml.text.navigator;

import org.netbeans.api.editor.EditorRegistry;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent;
import org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorPanel;

/** An implementation of NavigatorPanel for XML navigator.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class XMLNavigatorPanel extends AbstractXMLNavigatorPanel {
      
    /**
     * public no arg constructor needed for system to instantiate the provider.
     */
    public XMLNavigatorPanel() {
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(XMLNavigatorPanel.class, "XML_files_navigator");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(XMLNavigatorPanel.class, "XML_View");
    }
    
    protected  AbstractXMLNavigatorContent getNavigatorUI() {
	if (navigator == null) {
	    navigator = new NavigatorContent();
            EditorRegistry.addPropertyChangeListener(navigator);
	}
	return navigator;
    }
    
}
