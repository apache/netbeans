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
package org.netbeans.modules.php.blade.editor;

import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.util.ImageUtilities;

/**
 *
 * @author bhaidu
 */
public final class ResourceUtilities {

    public static final String FOLDER = "org/openide/loaders/defaultFolder.gif";//NOI18N
    public static final String ICON_BASE = "org/netbeans/modules/php/blade/resources/"; //NOI18N
    public static final String DIRECTIVE_ICON = ICON_BASE + "icons/at.png"; //NOI18N
    public static final String BLADE_VIEW = ICON_BASE + "icons/blade_file.png"; //NOI18N
    public static final String LAYOUT_IDENTIFIER = ICON_BASE + "icons/layout.png"; //NOI18N
    public static final String COMPONENT_TAG = "org/netbeans/modules/html/custom/resources/custom_html_element.png"; //NOI18N
    
    private ResourceUtilities() {
        
    }

    @CheckForNull
    public static ImageIcon loadResourceIcon(String path){
        return ImageUtilities.loadImageIcon(ICON_BASE + path, false);
    }
    
    @CheckForNull
    public static ImageIcon loadLayoutIcon(){
        return ImageUtilities.loadImageIcon(LAYOUT_IDENTIFIER, false);
    }
}
