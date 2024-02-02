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
package org.netbeans.modules.html.editor.lib.api.elements;

import java.util.*;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marek
 */
public class ElementUtils {

    private static final char ELEMENT_PATH_ELEMENTS_DELIMITER = '/';
    private static final char ELEMENT_PATH_INDEX_DELIMITER = '|';
    
    private static final String INDENT = "   "; //NOI18N

    private ElementUtils() {
    }
    
    /**
     * Provides a collection of possible open tags in the context
     * 
     * @since 3.6
     * 
     * @param model
     * @param afterNode
     * @return 
     */
    public static Collection<HtmlTag> getPossibleOpenTags(HtmlModel model, Element afterNode) {
        if (afterNode.type() != ElementType.OPEN_TAG) {
            return Collections.emptyList();
        }

        OpenTag openTag = (OpenTag) afterNode;

        HtmlTag tag = model.getTag(openTag.unqualifiedName().toString());
        if (tag == null) {
            return Collections.emptyList();
        }

        //skip empty tags - this is mailny a workaround for bad logical context of empty nodes
        //however not easily fixable since the HtmlCompletionQuery uses the XmlSyntaxTreeBuilder
        //when the parse tree is broken and the builder has no notion of such metadata.
        while (tag != null && tag.isEmpty()) {
            afterNode = afterNode.parent();
            if (afterNode == null) {
                return Collections.emptyList();
            }
            if (afterNode.type() != ElementType.OPEN_TAG) {
                return Collections.emptyList();
            }
            OpenTag ot = (OpenTag) afterNode;
            tag = model.getTag(ot.unqualifiedName().toString());
        }
        if (tag == null) {
            return Collections.emptyList();
        }

        Collection<HtmlTag> possibleChildren = new LinkedHashSet<>();
        addPossibleTags(tag, possibleChildren);
        return possibleChildren;
    }

     /**
     * Provides a map of possible html tag to existing matching open tag node 
     * or null if the end tag doesn't have to have an open tag
     * 
     * @since 3.6
     * 
     * @param model
     * @param node
     * @return 
     */
    public static Map<HtmlTag, OpenTag> getPossibleCloseTags(HtmlModel model, Element node) {
        //Bug 197608 - Non-html tags offered as closing tags using code completion
        //XXX define of what type can be the node argument
        if (node.type() != ElementType.OPEN_TAG) {
            node = node.parent();
            if (node == null) {
                return Collections.emptyMap();
            }
        }
        //<<<

        OpenTag openTag = (OpenTag) node;
        String openTagName = openTag.unqualifiedName().toString();

        HtmlTag tag = model.getTag(openTagName);
        if (tag == null) {
            return Collections.emptyMap();
        }

        Map<HtmlTag, OpenTag> possible = new LinkedHashMap<>();
        //end tags
        do {
            if (!ElementUtils.isVirtualNode(node)) {
                String tName = ((OpenTag)node).unqualifiedName().toString();
                tag = model.getTag(tName);
                if (tag != null) {
                    if (!tag.isEmpty()) {
                        possible.put(tag, (OpenTag) node);
                    }
                    if (!tag.hasOptionalEndTag()) {
                        //since the end tag is required, the parent elements cannot be closed here
                        break;
                    }
                }
            }
        } while ((node = node.parent()) != null && node.type() == ElementType.OPEN_TAG);

        return possible;

    }

    private static void addPossibleTags(HtmlTag tag, Collection<HtmlTag> possible) {
        //1.add all children of the tag
        //2.if a child has optional end, add its possible children
        //3.if a child is transparent, add its possible children
        Collection<HtmlTag> children = tag.getChildren();
        possible.addAll(children);
        for (HtmlTag child : children) {
            if (child.hasOptionalOpenTag()) {
                addPossibleTags(child, possible);
            }
            //TODO add the transparent check
        }
    }

    public static String getNamespace(Element element) {
        FeaturedNode root = getRoot(element);
        return (String) root.getProperty("namespace");
    }

    
    
    /**
     * Returns most leaf open tag which semantic range fits to the given offset.
     */
    public static Node findBySemanticRange(Node node, int offset, boolean forward) {
        for (OpenTag child : node.children(OpenTag.class)) {
            if (isVirtualNode(child)) {
                //we need to recurse into every virtual branch blindly hoping there might by some
                //real nodes fulfilling our constrains
                Node sub = findBySemanticRange(child, offset, forward);
                if (!isVirtualNode(sub)) {
                    return sub;
                }
            }
            if (matchesNodeRange(child, offset, forward, false)) {
                    Node sub = findBySemanticRange(child, offset, forward);
                    if(isVirtualNode(sub)) {
                        return child;
                    } else {
                        return sub;
                    }
                }
            }
        return node;
    }
    
    /**
     * Returns most leaf element of any kind at the specified offset.
     */
    public static Element findByPhysicalRange(Node base, int offset, boolean forward) {
        for (Element child : base.children()) {
            if (matchesNodeRange(child, offset, forward, true)) {
                return child;
            } else if (base.from() > offset) {
                //already behind the possible candidates
                return null;
            } else {
                //lets try this branch
                if(child instanceof Node) {
                    Node childNode = (Node)child;
                    Element candidate = findByPhysicalRange(childNode, offset, forward);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
  
    public static boolean isVirtualNode(Element node) {
        return node.from() == -1 && node.to() == -1;
    }

    private static boolean matchesNodeRange(Element node, int offset, boolean forward, boolean physicalNodeRangeOnly) {
        int lf, lt;
        switch (node.type()) {
            case OPEN_TAG:
//            case END_TAG:
                OpenTag t = (OpenTag) node;
                lf = t.from();
                lt = t.semanticEnd();
                break;
            default:
                lf = node.from();
                lt = node.to();
        }

        int from = physicalNodeRangeOnly || lf == -1 ? node.from() : lf;
        int to = physicalNodeRangeOnly || lt == -1 ? node.to() : lt;

        if (forward) {
            if (offset >= from && offset < to) {
                return true;
            }
        } else {
            if (offset > from && offset <= to) {
                return true;
            }
        }
        return false;
    }

    public static String dumpTree(Element node) {
        return dumpTree(node, (CharSequence) null);
    }

    public static String dumpTree(Element node, CharSequence source) {
        StringBuffer buf = new StringBuffer();
        dumpTree(node, buf, source);
        System.out.println(buf.toString());
        return buf.toString();
    }

    public static void dumpTree(Element node, StringBuffer buf) {
        dumpTree(node, buf, null);
    }

    public static void dumpTree(Element node, StringBuffer buf, CharSequence source) {
        dump(node, "", buf, source);
    }

    private static void dump(Element element, String prefix, StringBuffer buf, CharSequence source) {
        buf.append(prefix);
        buf.append(element.toString());
        if (source != null && element.from() != -1 && element.to() != -1) {
            buf.append(" (");
            buf.append(source.subSequence(element.from(), element.to()));
            buf.append(")");
        }
        buf.append('\n');
        if (element instanceof Node) {
            Node node = (Node) element;
            for (Element child : node.children()) {
                dump(child, prefix + INDENT, buf, source);
            }
        }
    }

    public static FeaturedNode getRoot(Element node) {
        for (;;) {
            if (node.parent() == null) {
                return (FeaturedNode) node; //root
            } else {
                node = node.parent();
            }
        }
    }

    /**
     * Returns a list of all ancestors of the given node matching the filter.
     * Closest ancestors are at the beginning of the list.
     */
    public static List<Node> getAncestors(Node node, ElementFilter filter) {
        List<Node> matching = new ArrayList<>();
        Node n = node;
        do {
            if (filter.accepts(n)) {
                matching.add(n);
            }

            n = n.parent();
        } while (n != null);

        return matching;
    }

    public static List<Element> getChildrenRecursivelly(Element element, ElementFilter filter, boolean recurseOnlyMatching) {
        List<Element> matching = new ArrayList<>();
        getChildrenRecursivelly(matching, element, filter, recurseOnlyMatching);
        return matching;
    }

    private static void getChildrenRecursivelly(List<Element> found, Element element, ElementFilter filter, boolean recurseOnlyMatching) {
        if (!(element instanceof Node)) {
            return;
        }

        Node node = (Node) element;
        for (Element child : node.children()) {
            if (filter.accepts(child)) {
                found.add(child);
                getChildrenRecursivelly(found, child, filter, recurseOnlyMatching);
            } else {
                if (!recurseOnlyMatching) {
                    getChildrenRecursivelly(found, child, filter, recurseOnlyMatching);
                }
            }
        }
    }
    
    /**
     * Encodes the given {@link TreePath} into a string form. 
     * 
     * The encoded path can be later used as an argument of {@link #query(org.netbeans.modules.html.editor.lib.api.elements.Node, java.lang.String) }
     * 
     * The root node is not listed in the path.
     * 
     * Example: html/body/table/tbody/tr/td
     * 
     * @since 3.4
     * @param element
     * @return string representation of the {@link TreePath}
     */
    public static String encodeToString(TreePath treePath) {
        StringBuilder sb = new StringBuilder();
        List<Element> p = treePath.path();
        for(int i = p.size() - 2; i >= 0; i-- ) { //do not include the root element
            Element node = p.get(i);
            Node parent = node.parent();
            int myIndex = parent == null ? 0 : getIndexInSimilarNodes(node.parent(), node);
            sb.append(node.id());
            if(myIndex > 0) {
                sb.append(ELEMENT_PATH_INDEX_DELIMITER);
                sb.append(myIndex);
            }
            
            if(i > 0) {
                sb.append(ELEMENT_PATH_ELEMENTS_DELIMITER);
            }
        }
        return sb.toString();
    }
    
    private static int getIndexInSimilarNodes(Node parent, Element node) {
        int index = -1;
        for(Element child : parent.children()) {
            if(node.id().equals(child.id()) && node.type() == child.type()) {
                index++;
            }
            if(child == node) {
                break;
            }
        }
        return index;
    }

    public static OpenTag query(Node base, String path) {
        return query(base, path, false);
    }

    /**
     * find an Node according to the path example of path: html/body/table|2/tr
     * -- find a second table tag in body tag
     *
     * note: queries OPEN TAGS ONLY!
     */
    public static OpenTag query(Node base, String path, boolean caseInsensitive) {
        StringTokenizer st = new StringTokenizer(path, Character.toString(ELEMENT_PATH_ELEMENTS_DELIMITER));
        Node found = base;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int indexDelim = token.indexOf(ELEMENT_PATH_INDEX_DELIMITER);

            String nodeName = indexDelim >= 0 ? token.substring(0, indexDelim) : token;
            if (caseInsensitive) {
                nodeName = nodeName.toLowerCase(Locale.ENGLISH);
            }
            String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1) : "0";
            int index = Integer.parseInt(sindex);

            int count = 0;
            OpenTag foundLocal = null;
            for (Element child : found.children(ElementType.OPEN_TAG)) {
                OpenTag openTag = (OpenTag) child;
                if (LexerUtils.equals(openTag.name(), nodeName, caseInsensitive, false) && count++ == index) {
                    foundLocal = openTag;
                    break;
                }
            }
            if (foundLocal != null) {
                found = foundLocal;

                if (!st.hasMoreTokens()) {
                    //last token, we may return
                    OpenTag openTag = (OpenTag) found;
                    assert LexerUtils.equals(openTag.name(), nodeName, false, false);
                    return openTag;
                }

            } else {
                return null; //no found
            }
        }

        return null;
    }

    public static boolean isDescendant(Node ancestor, Node descendant) {
        if (ancestor == descendant) {
            return false;
        }
        Node node = descendant;
        while ((node = node.parent()) != null) {
            if (ancestor == node) {
                return true;
            }
        }
        return false;
    }

    public static void visitChildren(Element element, ElementVisitor visitor, ElementType nodeType) {
        if (!(element instanceof Node)) {
            return;
        }
        Node node = (Node) element;
        for (Element n : node.children()) {
            if (nodeType == null || n.type() == nodeType) {
                visitor.visit(n);
            }
            visitChildren(n, visitor, nodeType);
        }
    }

    public static void visitChildren(Node node, ElementVisitor visitor) {
        visitChildren(node, visitor, null);
    }

    public static void visitAncestors(Element node, ElementVisitor visitor) {
        Node parent = (Node) node.parent();
        if (parent != null) {
            visitor.visit(parent);
            visitAncestors(parent, visitor);
        }
    }
}
