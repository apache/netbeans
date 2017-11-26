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
package org.netbeans.modules.hibernate.completion;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.hibernate.completion.CompletionContext.CompletionType;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateMappingCompletionQuery extends AsyncCompletionQuery {

    private int queryType;
    private int caretOffset;
    private JTextComponent component;

    public HibernateMappingCompletionQuery(int queryType, int caretOffset) {
        this.queryType = queryType;
        this.caretOffset = caretOffset;
    }

    @Override
    protected void preQueryUpdate(JTextComponent component) {
        //XXX: look for invalidation conditions
        this.component = component;
    }

    @Override
    protected void prepareQuery(JTextComponent component) {
        this.component = component;
    }

    @Override
    protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
        List<HibernateCompletionItem> completionItems = new ArrayList<HibernateCompletionItem>();
        int anchorOffset = getCompletionItems(doc, caretOffset, completionItems);
        resultSet.addAllItems(completionItems);
        if(anchorOffset != -1) {
            resultSet.setAnchorOffset(anchorOffset);
        }
        
        resultSet.finish();
    }
    
    // This method is here for unit testing purpose
    int getCompletionItems(Document doc, int caretOffset, List<HibernateCompletionItem> completionItems) {
        int anchorOffset = -1;
        
        CompletionContext context = new CompletionContext(doc, caretOffset);
        
        if (context.getCompletionType() == CompletionType.NONE) {
            return anchorOffset;
        }

        switch (context.getCompletionType()) {
            case ATTRIBUTE_VALUE:
                anchorOffset = HibernateMappingCompletionManager.getDefault().completeAttributeValues(context, completionItems);
                break;
            case ATTRIBUTE:
                anchorOffset = HibernateMappingCompletionManager.getDefault().completeAttributes(context, completionItems);
                break;
            case TAG:
                anchorOffset = HibernateMappingCompletionManager.getDefault().completeElements(context, completionItems);
                break;
            }
        
        return anchorOffset;
    }
}
