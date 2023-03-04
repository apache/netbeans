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
package org.netbeans.modules.css.editor.module.main;

import java.net.URL;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class PropertyCompatibilityHelpResolver extends HelpResolver {

    @Override
    public String getHelp(FileObject context, PropertyDefinition property) {
        StringBuilder sb = new StringBuilder();
        //XXX using legacy html code instead of css styling due to the jdk swingbrowser
        sb.append("<table width=\"100%\" border=\"0\"><tr><td><div style=\"font-size: large; font-weight: bold\">"); //NOI18N
        sb.append(property.getName());
        sb.append("</div></td>"); //NOI18N
        sb.append("<td width=\"125\">"); //NOI18N
        for (Browser browser : CssModuleSupport.getBrowsers(context)) {
            URL browserIcon = CssModuleSupport.isPropertySupported(property.getName(), browser)
                    ? browser.getActiveIcon()
                    : browser.getInactiveIcon();
            if(browserIcon != null) {
                sb.append("<img src=\""); //NOI18N
                sb.append(browserIcon.toExternalForm());
                sb.append("\">"); // NOI18N
            }
        }
        sb.append("</td></tr></table>"); //NOI18N
        
        return sb.toString();
    }

    @Override
    public URL resolveLink(FileObject context, PropertyDefinition property, String link) {
        return null;
    }

    @Override
    public int getPriority() {
        return 10; //high priority
    }
}
