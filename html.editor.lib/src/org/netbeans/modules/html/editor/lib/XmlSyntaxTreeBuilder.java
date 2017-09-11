/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
