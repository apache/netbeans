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
package org.netbeans.modules.html.ojet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.ojet.data.DataItem;
import org.netbeans.modules.html.ojet.data.DataProvider;
import org.netbeans.modules.html.ojet.data.DataProviderImpl;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Petr Pisl
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/xhtml", service = HtmlExtension.class)
})
public class OJETHtmlExtension extends HtmlExtension {

    private static String DATA_BINDING = "data-bind";

    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        String attribute = context.getAttributeName();
        if (DATA_BINDING.equals(attribute)) {

            Document document = context.getResult().getSnapshot().getSource().getDocument(true);
            int offset = context.getOriginalOffset();
            OJETContext ojContext = OJETContext.findContext(document, offset);
            switch (ojContext) {
                case DATA_BINDING:
                    String prefix = OJETUtils.getPrefix(ojContext, document, offset);
                    Collection<DataItem> data = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getBindingOptions(), prefix);
                    List<CompletionItem> result = new ArrayList<CompletionItem>();
                    for (DataItem item : data) {
                        result.add(new OJETCompletionHtmlItem(item, OJETUtils.getPrefixOffset(ojContext, document, offset)));
                    }
                    return result;
            }
        }
        return Collections.emptyList();
    }
}
