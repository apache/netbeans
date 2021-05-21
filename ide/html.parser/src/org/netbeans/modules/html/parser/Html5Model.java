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
package org.netbeans.modules.html.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelProvider;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.NamedCharRef;
import org.netbeans.modules.html.parser.model.ElementDescriptor;
import org.netbeans.modules.html.parser.model.HtmlTagProvider;
import org.netbeans.modules.html.parser.model.NamedCharacterReference;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service = HtmlModelProvider.class, position = 10)
public final class Html5Model implements HtmlModel, HtmlModelProvider {

    private static Collection<HtmlTag> ALL_TAGS;

    @Override
    public HtmlModel getModel(HtmlVersion version) {
        switch (version) {
            case HTML5:
            case XHTML5:
                return this;
            default:
                return null;
        }
    }

    @Override
    public synchronized Collection<HtmlTag> getAllTags() {
        if (ALL_TAGS == null) {
            ALL_TAGS = new ArrayList<HtmlTag>();
            for (ElementDescriptor element : ElementDescriptor.values()) {
                ALL_TAGS.add(HtmlTagProvider.forElementDescriptor(element));
            }
        }
        return Collections.unmodifiableCollection(ALL_TAGS);
    }

    @Override
    public HtmlTag getTag(String tagName) {
        return HtmlTagProvider.getTagForElement(tagName); //cached in the provider
    }

    @Override
    public Collection<? extends NamedCharRef> getNamedCharacterReferences() {
        return EnumSet.allOf(NamedCharacterReference.class);
    }

    /**
     * 
     * @deprecated 
     */
    @Override
    @Deprecated
    public String getModelId() {
        return null;
    }
}
