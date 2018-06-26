/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
