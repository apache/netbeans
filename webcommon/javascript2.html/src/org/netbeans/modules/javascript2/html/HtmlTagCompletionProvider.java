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
package org.netbeans.modules.javascript2.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.netbeans.modules.javascript2.editor.spi.CompletionProviderEx;
import org.netbeans.modules.javascript2.editor.spi.ProposalRequest;

/**
 *
 * @author sdedic
 */
@CompletionProvider.Registration(priority = 0)
public class HtmlTagCompletionProvider implements CompletionProviderEx {

    @Override
    public List<CompletionProposal> complete(ProposalRequest request) {
        if (!request.getSelectors().contains("Element")) { // NOI18N
            return Collections.emptyList();
        }
        List<CompletionProposal> resultList = new ArrayList<>();
        for(HtmlTagAttribute attribute: getAllAttributes())  {
            if (attribute.getName().startsWith(request.getPrefix())) {
                resultList.add(new HtmlCompletionItem(request.getInfo(), attribute, request.getAnchor()));
            }
        }
        return resultList;
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (!(element instanceof HtmlAttrElement)) {
            return null;
        }
        return ((HtmlAttrElement)element).getDocumentation().toString();
    }
    
    private Collection<HtmlTagAttribute> getAllAttributes() {
        HtmlModel htmlModel = HtmlModelFactory.getModel(HtmlVersion.HTML5);
        Map<String, HtmlTagAttribute> result = new HashMap<String, HtmlTagAttribute>();
        for (HtmlTag htmlTag : htmlModel.getAllTags()) {
            for (HtmlTagAttribute htmlTagAttribute : htmlTag.getAttributes()) {
                // attributes can probably differ per tag so we can just offer some of them,
                // at least for the CC purposes it should be complete list of attributes for unknown tag
                if (!result.containsKey(htmlTagAttribute.getName())) {
                    result.put(htmlTagAttribute.getName(), htmlTagAttribute);
                }
            }
        }
        return result.values();
    }

}
