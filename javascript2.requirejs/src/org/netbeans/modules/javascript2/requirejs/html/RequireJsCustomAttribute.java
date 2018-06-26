/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.requirejs.html;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.netbeans.modules.javascript2.requirejs.RequireJsDataProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsCustomAttribute implements CustomAttribute {

    // attribute for the script tag
    private static Collection<CustomAttribute> attributes = null;
    private static String AT_DATA_MAIN = "data-main"; //NOI18N
    private static final Logger LOG = Logger.getLogger(RequireJsCustomAttribute.class.getSimpleName()); //NOI18N

    public static synchronized Collection<CustomAttribute> getCustomAttributes() {
        if (attributes == null) {
            //init
            attributes = new ArrayList<>();
            attributes.add(new RequireJsCustomAttribute(AT_DATA_MAIN, false, true));
        }
        return attributes;
    }

    private final String name;
    private final boolean isRequired;
    private final boolean isValueRequired;

    private RequireJsCustomAttribute(final String name, final boolean isRequired, final boolean isValueRequired) {
        this.name = name;
        this.isRequired = isRequired;
        this.isValueRequired = isValueRequired;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public boolean isValueRequired() {
        return isValueRequired;
    }

    @Override
    public HelpItem getHelp() {
        return new RequireJsHelpItem(name);
    }

    private static class RequireJsHelpItem implements HelpItem {

        private final String tagName;
        private final HelpResolver helpResolver;

        public RequireJsHelpItem(String tagName) {
            this.tagName = tagName;
            this.helpResolver = new RequireJsHelpResolver();
        }

        @Override
        public String getHelpHeader() {
            return null;
        }

        @Override
        public String getHelpContent() {
            return RequireJsDataProvider.getDefault().getDocForHtmlTagAttribute(tagName);
        }

        @Override
        public URL getHelpURL() {
            try {
                URL url = new URL(RequireJsDataProvider.API_URL);
                return url;
            } catch (MalformedURLException ex) {
                LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        @Override
        public HelpResolver getHelpResolver() {
            return helpResolver;
        }

    }

    private static class RequireJsHelpResolver implements HelpResolver {

        @Override
        public URL resolveLink(URL base, String link) {
            try {
                if (!link.startsWith("http")) {  //NOI18N
                    // try to read from cachd file
                    URL url = new URL(RequireJsDataProvider.getCachedAPIFile().toURI().toURL().toString() + link);
                    return url;
                }
            } catch (MalformedURLException ex) {
                LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        @Override
        public String getHelpContent(URL url) {
            return null;
        }

    }

}
