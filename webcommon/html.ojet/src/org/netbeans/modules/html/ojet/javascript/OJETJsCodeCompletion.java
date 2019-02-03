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
package org.netbeans.modules.html.ojet.javascript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.ojet.OJETContext;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.netbeans.modules.html.ojet.data.DataItem;
import org.netbeans.modules.html.ojet.data.DataItemImpl;
import org.netbeans.modules.html.ojet.data.DataProvider;
import org.netbeans.modules.html.ojet.data.DataProviderImpl;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority = 6)
public class OJETJsCodeCompletion implements CompletionProvider {

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        Document document = ccContext.getParserResult().getSnapshot().getSource().getDocument(true);
        int dOffset = ccContext.getCaretOffset();  // document offset
        ((AbstractDocument) document).readLock();
        OJETContext ojContext = OJETContext.UNKNOWN;
        try {
            ojContext = OJETContext.findContext(document, dOffset);
//            System.out.println("ojContext: " + ojContext);
            List<CompletionProposal> result = new ArrayList<>();
            switch (ojContext) {
                case COMP_CONF_COMP_NAME:
                    Collection<DataItem> components = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getComponents(), ccContext.getPrefix());
                    for (DataItem component : components) {
                        result.add(new OJETCodeCompletionItem.OJETComponentItem(component, ccContext));
                    }
                    break;
                case COMP_CONF:
                    result.add(new OJETCodeCompletionItem.OJETComponentOptionItem(new DataItemImpl("component", null), ccContext)); //NOI18N
                    break;
                case COMP_CONF_PROP_NAME:
                    String compName = OJETContext.findComponentName(document, dOffset);
                    if (compName != null && !compName.isEmpty()) {
                        Collection<DataItem> options = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getComponentOptions(compName), ccContext.getPrefix());
                        for (DataItem option : options) {
                            result.add(new OJETCodeCompletionItem.OJETComponentOptionItem(option, ccContext));
                        }
                        Collection<DataItem> events = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getComponentEvents(compName), ccContext.getPrefix());
                        for (DataItem event : events) {
                            result.add(new OJETCodeCompletionItem.OJETComponentEventItem(event, ccContext));
                        }
                        
                    }
                    break;
                case MODULE_PROP_NAME:
                    Collection<DataItem> options = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getModuleProperties(), ccContext.getPrefix());
                    for (DataItem option : options) {
                        result.add(new OJETCodeCompletionItem.OJETComponentOptionItem(option, ccContext));
                    }
                    break;
            }
            return result;
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element instanceof OJETCodeCompletionItem.DocSimpleElement) {
            return ((OJETCodeCompletionItem.DocSimpleElement) element).getDocumentation();
        }
        return null;
    }

}
