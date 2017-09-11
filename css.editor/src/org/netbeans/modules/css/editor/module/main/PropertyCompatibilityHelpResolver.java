/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
