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
package org.netbeans.modules.php.latte.completion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteCustomAttribute implements CustomAttribute {
    private static final Logger LOGGER = Logger.getLogger(LatteCustomAttribute.class.getName());
    private final String name;

    public LatteCustomAttribute(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isValueRequired() {
        return false;
    }

    @Override
    public HelpItem getHelp() {
        return new HelpItemImpl(LatteDocumentation.Factory.createFromBundle(name, name));
    }

    private static final class HelpItemImpl implements HelpItem {
        private static final String DOCUMENTATION_URL = "http://doc.nette.org/en/"; //NOI18N
        private final LatteDocumentation documentation;

        public HelpItemImpl(LatteDocumentation documentation) {
            this.documentation = documentation;
        }

        @Override
        public String getHelpHeader() {
            return documentation.getHeader();
        }

        @Override
        public String getHelpContent() {
            return documentation.getContent();
        }

        @Override
        public URL getHelpURL() {
            try {
                return new URL(DOCUMENTATION_URL);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.FINE, null, ex);
                return null;
            }
        }

        @Override
        public HelpResolver getHelpResolver() {
            return null;
        }
    }

}
