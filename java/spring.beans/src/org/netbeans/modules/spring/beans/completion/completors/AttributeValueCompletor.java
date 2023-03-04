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
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.CompletorUtils;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;

/**
 * A simple completor for general attribute value items
 * 
 * Takes an array of strings, the even elements being the display text of the items
 * and the odd ones being the corresponding documentation of the items
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class AttributeValueCompletor extends Completor {

    private final String[] itemTextAndDocs;

    public AttributeValueCompletor(String[] itemTextAndDocs, int invocationOffset) {
        super(invocationOffset);
        this.itemTextAndDocs = itemTextAndDocs;
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        return context.getCurrentTokenOffset() + 1;
    }
    
    @Override
    protected void compute(CompletionContext context) throws IOException {
        int caretOffset = context.getCaretOffset();
        String typedChars = context.getTypedPrefix();
        
        for (int i = 0; i < itemTextAndDocs.length; i += 2) {
            if(isCancelled()) {
                return;
            }
            
            if (itemTextAndDocs[i].startsWith(typedChars)) {
                SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                        itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                addCacheItem(item);
            }
        }
    }
        
    @Override
    public boolean canFilter(CompletionContext context) {
        return CompletorUtils.canFilter(context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset(), CompletorUtils.CHARACTER_STRING_ACCEPTOR);
    }

    @Override
    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        return CompletorUtils.filter(getCacheItems(), context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset());
}
            
}
