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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.openide.util.Exceptions;

/**
 * Handles the bean tag's depends-on attribute value completion
 * 
 * @author Rohan Ranade
 */
public class BeanDependsOnCompletor extends BeansRefCompletor {

    public BeanDependsOnCompletor(boolean includeGlobal, int invocationOffset) {
        super(includeGlobal, invocationOffset);
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        int index = context.getCurrentTokenOffset() + 1;
        String prefix = context.getTypedPrefix();
        if (StringUtils.hasText(prefix)) {
            int sepOffset = Math.max(Math.max(prefix.lastIndexOf(','), prefix.lastIndexOf(';')), prefix.lastIndexOf(' ')); // NOI18N
            if (sepOffset != -1) {
                index += sepOffset + 1;
            }
        }

        return index;
    }

    @Override
    protected String getContextPrefix(CompletionContext context) {
        String contextPrefix = "";
        try {
            contextPrefix = context.getDocument().getText(getAnchorOffset(), context.getCaretOffset() - getAnchorOffset());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return contextPrefix;
    }

    @Override
    protected Set<String> getForbiddenNames(CompletionContext context) {
        // filter out existing entries in the value string
        String typedPrefix = context.getTypedPrefix();
        if(!StringUtils.hasText(typedPrefix)) {
            return Collections.emptySet();
        }
        
        int startIdx = context.getCurrentTokenOffset() + 1;
        int length = getAnchorOffset() - startIdx;
        
        if(length <= 0) {
            return Collections.emptySet();
        }
        
        String existingStr = typedPrefix.substring(0, length);
        List<String> names = StringUtils.tokenize(existingStr, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS);
        return new HashSet<String>(names);
    }
}
