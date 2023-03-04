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
package org.netbeans.modules.html.knockout;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class HelpItemImpl implements HelpItem {

    private KOHelpItem item;

    public HelpItemImpl(KOHelpItem binding) {
        this.item = binding;
    }

    @Override
    public String getHelpHeader() {
        return null; //no header
    }

    @Override
    public String getHelpContent() {
        return KODoc.getDefault().getDirectiveDocumentation(item);
    }

    @Override
    public URL getHelpURL() {
        try {
            return new URL(item.getExternalDocumentationURL());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public HelpResolver getHelpResolver() {
        return KOHelpResolver.getDefault();
    }
}
