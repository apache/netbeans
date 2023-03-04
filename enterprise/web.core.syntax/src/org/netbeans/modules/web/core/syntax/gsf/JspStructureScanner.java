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
package org.netbeans.modules.web.core.syntax.gsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.web.core.syntax.parser.JspSyntaxElement;

/**
 * @author mfukala@netbeans.org
 */
public class JspStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        JspParserResult jspParserResult = (JspParserResult)info;
        
        List<OffsetRange> comments = new ArrayList<OffsetRange>();
        List<OffsetRange> tags = new ArrayList<OffsetRange>();
        List<OffsetRange> scriptlets = new ArrayList<OffsetRange>();

        Stack<JspSyntaxElement.OpenTag> openTagsStack = new Stack<JspSyntaxElement.OpenTag>();

        List<JspSyntaxElement> elements = jspParserResult.elements();
        for (JspSyntaxElement element : elements) {
            switch (element.kind()) {
                case COMMENT:
                    comments.add(new OffsetRange(element.from(), element.to()));
                    break;
                case OPENTAG:
                    handleOpenTagElement((JspSyntaxElement.OpenTag) element, openTagsStack, tags);
                    break;
                case ENDTAG:
                    handleEndTagElement((JspSyntaxElement.EndTag) element, openTagsStack, tags);
                    break;
                case SCRIPTING:
                    scriptlets.add(new OffsetRange(element.from(), element.to()));
                    break;
            }
        }

        Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();

        folds.put("tags", tags); //NOI18N
        folds.put("comments", comments); //NOI18N
        folds.put("codeblocks", scriptlets);//NOI18N
        
        return folds;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    private void handleOpenTagElement(JspSyntaxElement.OpenTag openTag,
            Stack<JspSyntaxElement.OpenTag> openTagsStack, List<OffsetRange> tags) {
        if (openTag.isEmpty()) {
            //create element - do not put into stack
            tags.add(new OffsetRange(openTag.from(), openTag.to()));
        } else {
            openTagsStack.push(openTag);
        }
    }

    private void handleEndTagElement(JspSyntaxElement.EndTag endTag,
            Stack<JspSyntaxElement.OpenTag> openTagsStack, List<OffsetRange> tags) {
        if (openTagsStack.isEmpty()) {
            return; //stray end tag
        }

        JspSyntaxElement.OpenTag top = openTagsStack.peek();
        if (endTag.name().equals(top.name())) {
            //match
            tags.add(new OffsetRange(top.from(), endTag.to()));
            openTagsStack.pop();
        } else {
            //I need to save the pop-ed elements for the case that there isn't
            //any matching start tag found
            ArrayList<JspSyntaxElement.OpenTag> savedElements = new ArrayList<JspSyntaxElement.OpenTag>();
            //this semaphore is used behind the loop to detect whether a
            //matching start has been found
            boolean foundStartTag = false;

            while (!openTagsStack.isEmpty()) {
                JspSyntaxElement.OpenTag start = openTagsStack.pop();
                savedElements.add(start);
                
                if (start.name().equals(endTag.name())) {
                    //found a matching start tag
                    tags.add(new OffsetRange(start.from(), endTag.to()));
                    foundStartTag = true;
                    break; //break the while loop
                }
            }
            if (!foundStartTag) {
                //we didn't find any matching start tag =>
                //return all elements back to the stack
                for (int i = savedElements.size() - 1; i >= 0; i--) {
                    openTagsStack.push(savedElements.get(i));
                }
            }
        }


    }
}
