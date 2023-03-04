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

package org.netbeans.modules.html.editor.lib.api;

import java.net.URL;

/**
 *
 * @author marekfukala
 */
public class DefaultHelpItem implements HelpItem {

    private String header, helpContent;
    private URL helpUrl;
    private HelpResolver helpResolver;

    public DefaultHelpItem(URL helpUrl, HelpResolver helpResolver, String header) {
        this(helpUrl, helpResolver, header, null);
    }

    public DefaultHelpItem(URL helpUrl, HelpResolver helpResolver, String header, String helpContent) {
        this.helpUrl = helpUrl;
        this.helpResolver = helpResolver;
        this.header = header;
        this.helpContent = helpContent;
    }

    @Override
    public URL getHelpURL() {
        return helpUrl;
    }

    @Override
    public HelpResolver getHelpResolver() {
        return helpResolver;
    }

    @Override 
    public String getHelpHeader() {
        return header;
    }

    @Override
    public String getHelpContent() {
        return helpContent;
    }

}
