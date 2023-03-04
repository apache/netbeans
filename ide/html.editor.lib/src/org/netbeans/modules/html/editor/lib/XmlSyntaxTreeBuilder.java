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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author marekfukala
 */
public class XmlSyntaxTreeBuilder {

    private static boolean ADD_TEXT_NODES;
    private static boolean FOLLOW_HTML_MODEL;
    private static HtmlModel MODEL;

    public static Node makeUncheckedTree(HtmlSource source, String namespace, Lookup lookup) {
        Parameters.notNull("source", source);
        Parameters.notNull("lookup", lookup);

        ElementsIteratorHandle handle = lookup.lookup(ElementsIteratorHandle.class);
        Iterator<Element> elements = handle != null ? handle.getIterator() : null;
        assert elements != null;

        Properties props = lookup.lookup(Properties.class);
        if (props != null) {
            ADD_TEXT_NODES = Boolean.parseBoolean(props.getProperty("add_text_nodes")); //NOI18N
            FOLLOW_HTML_MODEL = Boolean.parseBoolean(props.getProperty("follow_html_model")); //NOI18N
        }

        if (FOLLOW_HTML_MODEL) {
            MODEL = lookup.lookup(HtmlModel.class);
            assert MODEL != null : "add HtmlModel instance to the lookup!"; //NOI18N
        }

        CharSequence code = source.getSourceCode();
        XmlSTElements.Root rootNode = new XmlSTElements.Root(namespace, code);
        LinkedList<XmlSTElements.OT> stack = new LinkedList<>();
        stack.add(rootNode);

        while (elements.hasNext()) {
            Element element = elements.next();

            if (element.type() == ElementType.OPEN_TAG) { //open tag
                OpenTag plainOpenTag = (OpenTag) element;

                OpenTag openTagNode = plainOpenTag.isEmpty()
                        ? new XmlSTElements.EmptyOT(
                        plainOpenTag.attributes(),
                        plainOpenTag.name(),
                        code,
                        plainOpenTag.from(),
                        plainOpenTag.to())
                        : new XmlSTElements.OT(
                        plainOpenTag.attributes(),
                        plainOpenTag.name(),
                        code,
                        plainOpenTag.from(),
                        plainOpenTag.to());

                //add the node to its parent
                XmlSTElements.OT peek = stack.getLast();
                peek.addChild(openTagNode);

                if (MODEL != null) {
                    HtmlTag htmlTag = MODEL.getTag(plainOpenTag.name().toString());
                    if (htmlTag != null) {
                        if (htmlTag.isEmpty()) {
                            //imply the close tag (do not add to the nesting stack)
                            continue;
                        }
                    }
                }


                //possible add the node to the nodes stack
                if (!(plainOpenTag.isEmpty())) {
                    stack.addLast((XmlSTElements.OT) openTagNode);
                }


            } else if (element.type() == ElementType.CLOSE_TAG) { //close tag
                CloseTag plainElement = (CloseTag) element;
                CharSequence tagName = plainElement.name();

                XmlSTElements.ET endTagNode = new XmlSTElements.ET(
                        plainElement.name(),
                        code,
                        plainElement.from(),
                        plainElement.to());

                int matched_index = -1;
                for (int i = stack.size() - 1; i >= 0; i--) {
                    OpenTag node = stack.get(i);
                    if (LexerUtils.equals(tagName, node.name(), false, false)) {
                        //ok, match
                        matched_index = i;
                        break;
                    }
                }

                assert matched_index != 0; //never match root node, either -1 or > 0

                if (matched_index > 0) {
                    //something matched
                    XmlSTElements.OT match = stack.get(matched_index);

                    //remove them ALL the left elements from the stack
                    for (int i = stack.size() - 1; i > matched_index; i--) {
                        XmlSTElements.OT node = stack.get(i);
                        node.setLogicalEndOffset(endTagNode.from());
                        stack.remove(i);
                    }

                    //add the node to the proper parent
                    XmlSTElements.OT match_parent = stack.get(matched_index - 1);
                    match_parent.addChild(endTagNode);

                    //wont' help GS at all, but should be ok
                    match.setMatchingEndTag(endTagNode);
                    match.setLogicalEndOffset(endTagNode.to());
                    endTagNode.setMatchingOpenTag(match);

                    //remove the matched tag from stack
                    stack.removeLast();

                } else {
                    //add it to the last node
                    stack.getLast().addChild(endTagNode);
                }

            } else if (element.type() == ElementType.TEXT) {
                if (ADD_TEXT_NODES) {
                    XmlSTElements.OT parent = stack.getLast();
                    Element text = new XmlSTElements.Text(
                            code, element.from(), element.to());

                    parent.addChild(text);
                }

            } else {
                //rest of the syntax element types
                //XXX do we need to have these in the AST???
                // add a new AST node to the last node on the stack
//                Node.NodeType nodeType = intToNodeType(element.type());
//
//                Node node = new Node(null, nodeType, element.offset(),
//                        element.offset() + element.length(), false);
//
//                stack.getLast().addChild(node);
            }
        }

        //check the stack content and resolve left nodes
        for (int i = stack.size() - 1; i > 0; i--) { // (i > 0) == do not process the very first (root) node
            XmlSTElements.OT node = stack.get(i);
            node.setLogicalEndOffset(code.length());

        }

        return rootNode;
    }
}
