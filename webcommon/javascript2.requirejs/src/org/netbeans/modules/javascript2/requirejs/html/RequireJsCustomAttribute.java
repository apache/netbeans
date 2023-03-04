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
