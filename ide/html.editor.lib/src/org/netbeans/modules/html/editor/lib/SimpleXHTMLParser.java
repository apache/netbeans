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
package org.netbeans.modules.html.editor.lib;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.html.editor.lib.api.*;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides XHTML files parsing. 
 * 
 * This parser provides no error reporting. All validity checks are done by the validator service.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = HtmlParser.class, position = 50)
public class SimpleXHTMLParser implements HtmlParser {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean canParse(HtmlVersion version) {
        return version.isXhtml();
    }

    @Override
    public HtmlParseResult parse(HtmlSource source, final HtmlVersion preferedVersion, Lookup lookup) throws ParseException {
        Node root = XmlSyntaxTreeBuilder.makeUncheckedTree(source, null, lookup);
        return new DefaultHtmlParseResult(source, root, Collections.<ProblemDescription>emptyList(), preferedVersion) {

            @Override
            public HtmlModel model() {
                return getModel(version());
            }

            @Override
            public Collection<HtmlTag> getPossibleOpenTags(Element context) {
                return ElementUtils.getPossibleOpenTags(model(), context);
            }

            @Override
            public Map<HtmlTag, OpenTag> getPossibleCloseTags(Element context) {
                return ElementUtils.getPossibleCloseTags(model(), context);
            }
        };
    }

    @Override
    public HtmlModel getModel(HtmlVersion version) {
        return HtmlModelFactory.getModel(version);
    }
    
}
