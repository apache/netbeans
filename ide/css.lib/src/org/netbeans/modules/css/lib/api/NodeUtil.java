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
package org.netbeans.modules.css.lib.api;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.css.lib.TokenNode;

/**
 * Utility methods to work with the css source parse trees.
 *
 * @author mfukala@netbeans.org
 */
public final class NodeUtil {
    
    private static final char ELEMENT_PATH_ELEMENTS_DELIMITER = '/'; //NOI18N
    private static final char ELEMENT_PATH_INDEX_DELIMITER = '|'; //NOI18N
    

    private static final String INDENT = "    ";//NOI18N

    private NodeUtil() {
    }

    public static int[] getTrimmedNodeRange(Node node) {
        CharSequence text = node.image();
        int from_diff;
        int to_diff;
        for (from_diff = 0; from_diff < text.length(); from_diff++) {
            if (!Character.isWhitespace(text.charAt(from_diff))) {
                break;
            }
        }

        for (to_diff = 0; to_diff < text.length() - from_diff; to_diff++) {
            if (!Character.isWhitespace(text.charAt(text.length() - 1 - to_diff))) {
                break;
            }
        }

        return new int[]{node.from() + from_diff, node.to() - to_diff};
    }

    public static Node findNonTokenNodeAtOffset(Node node, int offset) {
        Node found = findNodeAtOffset(node, offset);
        return found instanceof TokenNode
                ? found.parent()
                : found;

    }

    public static Node findNodeAtOffset(Node node, int astOffset) {
        int so = node.from();
        int eo = node.to();


        if (astOffset < so || astOffset > eo) {
            //we are out of the scope - may happen just with the first client call
            return null;
        }

        if (astOffset >= so && astOffset <= eo && node.children().isEmpty()) {
            //if the node matches and has no children we found it
            return node;
        }

        for (Node child : node.children()) {

            int ch_so = child.from();
            int ch_eo = child.to();
            if (astOffset >= ch_so && astOffset <= ch_eo) {
                //the child is or contains the searched node
                return findNodeAtOffset(child, astOffset);
            }

        }

        return node;
    }

    /** @return first child of the node with the specified kind. */
    public static Node getChildByType(Node node, NodeType type) {
        Node[] children = getChildrenByType(node, type);
        return children.length == 0 ? null : children[0];
    }

    /**
     * 
     * @param node
     * @param tokenId
     * @return 
     */
    public static Node getChildTokenNode(Node node, CssTokenId tokenId) {
        Node[] children = getChildrenByType(node, NodeType.token);
        for (Node n : children) {
            TokenNode tn = (TokenNode) n;
            if (tn.getTokenId() == tokenId) {
                return n;
            }
        }
        return null;
    }

    public static CssTokenId getTokenNodeTokenId(Node node) {
        //strange, possibly better expose TokenNode!
        if (node.type() != NodeType.token) {
            throw new IllegalArgumentException(
                    String.format("The argument must by of NodeType.token type. The actual type is %s", node.type()));
        }
        return ((TokenNode) node).getTokenId();
    }

    public static Node getAncestorByType(Node node, final NodeType type) {
        AtomicReference<Node> found = new AtomicReference<>();
        NodeVisitor<AtomicReference<Node>> visitor = new NodeVisitor<AtomicReference<Node>>(found) {

            @Override
            public boolean visit(Node node) {
                if (node.type() == type) {
                    getResult().set(node);
                    return true;
                }
                return false;
            }
        };
        visitor.visitAncestors(node);
        return found.get();
    }
    
    public static List<Node> getChildrenRecursivelyByType(Node root, final NodeType... type) {
        final EnumSet<NodeType> nodeTypes = EnumSet.of(type[0], type);
        List<Node> found = new ArrayList<>();
        NodeVisitor<List<Node>> visitor = new NodeVisitor<List<Node>>(found) {

            @Override
            public boolean visit(Node node) {
                if(nodeTypes.contains(node.type())) {
                    getResult().add(node);
                }
                return false;
            }
        };
        
        visitor.visitChildren(root);
        return visitor.getResult();
    }

    /** @return list of children of the node with the specified kind. */
    public static Node[] getChildrenByType(Node node, NodeType type) {
        ArrayList<Node> list = new ArrayList<>(node.children().size() / 4);
        for (Node child : node.children()) {
            if (child.type() == type) {
                list.add(child);
            }
        }
        return list.toArray(new Node[]{});
    }

    /** @return A sibling node before or after the given node. */
    public static Node getSibling(Node node, boolean before) {
        Node parent = node.parent();
        if (parent == null) {
            return null;
        }
        Node sibling = null;
        for (int i = 0; i < parent.children().size(); i++) {
            List<Node> children = parent.children();
            Node child = children.get(i);
            if (child == node) {
                //we found myself
                if (before) {
                    if (i == 0) {
                        //we are first node, no sibling before
                        return null;
                    } else {
                        return children.get(i - 1);
                    }
                } else {
                    //after
                    if (i == children.size() - 1) {
                        //we are last node, no sibling after
                        return null;
                    } else {
                        return children.get(i + 1);
                    }
                }
            }
        }
        return sibling;
    }

    /**
     * Gets a element identification id for the given element which may be used to resolve the element in another parse tree.
     * 
     * @since 1.42
     */
    public static String getElementId(Node node) {
        TreePath tp = new TreePath(node);
        return encodeToString(tp);
    }
    
    /**
     * Encodes the given {@link TreePath} into a string form. 
     * 
     * The encoded path can be later used as an argument of {@link #query(org.netbeans.modules.css.lib.api.elements.Node, java.lang.String) }
     * 
     * The root node is not listed in the path.

     * @since 1.42
     * @param treePath
     * @return string representation of the {@link TreePath}
     */
    public static String encodeToString(TreePath treePath) {
        StringBuilder sb = new StringBuilder();
        List<Node> p = treePath.path();
        for(int i = p.size() - 2; i >= 0; i-- ) { //do not include the root element
            Node node = p.get(i);
            Node parent = node.parent();
            int myIndex = parent == null ? 0 : getIndexInSimilarNodes(node.parent(), node);
            sb.append(node.name());
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
    
    private static int getIndexInSimilarNodes(Node parent, Node node) {
        int index = -1;
        for(Node child : parent.children()) {
            if(node.name().equals(child.name()) && node.type() == child.type()) {
                index++;
            }
            if(child == node) {
                break;
            }
        }
        return index;
    }
    
    public static Node query(Node base, String path) {
        return query(base, path, false);
    }

    /** find an Node according to the given tree path
     * example of path: declaration/property|1/color -- find a second color property  (index from zero)
     */
    public static Node query(Node base, String path, boolean caseInsensitive) {
        StringTokenizer st = new StringTokenizer(path, "/");
        Node found = base;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int indexDelim = token.indexOf('|');

            String nodeName = indexDelim >= 0 ? token.substring(0, indexDelim) : token;
            if (caseInsensitive) {
                nodeName = nodeName.toLowerCase(Locale.ENGLISH);
            }
            String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1) : "0";
            int index = Integer.parseInt(sindex);

            int count = 0;
            Node foundLocal = null;
            for (Node child : found.children()) {
                String childName = child.name();
                if ((caseInsensitive ? childName = childName.toLowerCase(Locale.ENGLISH) : childName).equals(nodeName) && count++ == index) {
                    foundLocal = child;
                    break;
                }
            }
            if (foundLocal != null) {
                found = foundLocal;

                if (!st.hasMoreTokens()) {
                    //last token, we may return
                    assert found.name().equals(nodeName);
                    return found;
                }

            } else {
                return null; //no found
            }
        }

        return null;
    }

    public static void dumpTree(Node node) {
        PrintWriter pw = new PrintWriter(System.out);
        dumpTree(node, pw);
        pw.flush();
    }

    public static void dumpTree(Node node, PrintWriter pw) {
        dump(node, 0, pw);

    }

    private static void dump(Node tree, int level, PrintWriter pw) {
        for (int i = 0; i < level; i++) {
            pw.print(INDENT);
        }
        pw.print(tree.toString());
        pw.println();
        for (Node c : tree.children()) {
            dump(c, level + 1, pw);
        }
    }

    public static boolean isSelectorNode(Node node) {
        switch (node.type()) {
            case elementName:
            case cssClass:
            case cssId:
                return true;
            default:
                return false;
        }

    }

    /**
     * finds the rule curly bracket range
     * @param node representing a rule node
     */
    public static int[] getRuleBodyRange(Node node) {
        if (node.type() != NodeType.rule) {
            throw new IllegalArgumentException("Only selector node is allowed as a parameter!"); //NOI18N
        }

        Node lbrace = NodeUtil.getChildTokenNode(node, CssTokenId.LBRACE);
        Node rbrace = NodeUtil.getChildTokenNode(node, CssTokenId.RBRACE);

        if (lbrace == null || rbrace == null) {
            return null;
        }

        return new int[]{lbrace.from(), rbrace.to()};
    }

    public static boolean isOfType(Node node, NodeType... types) {
        for(NodeType type : types) {
            if(node.type() == type) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean containsError(Node node) {
        AtomicBoolean error = new AtomicBoolean(false);
        NodeVisitor<AtomicBoolean> visitor = new NodeVisitor<AtomicBoolean>(error) {

            @Override
            public boolean visit(Node node) {
                if(node.type() == NodeType.error) {
                    getResult().set(true);
                    return true;
                }
                return false;
            }
        };
        
        visitor.visitChildren(node);
        
        return visitor.getResult().get();
    }

    // CSS escapes are always prefixed with a backslash. The backslash is either
    // followed by 1-6 hexadecimal characters (0-9, A-F, a-f) and a trailing
    // whitespace character, that is required, if the following charachter is
    // from the hexadecimal range and not all 6 hexadecimal characters are used.
    // The alternative is a single character, that is not from the list of
    // hexadecimal characters.
    private static final Pattern ESCAPE = Pattern.compile("\\\\(?:(?:(?<hex>[0-9a-fA-F]{1,6})[\n\r\f\t ]?)|(?<direct>[^\n\r0-9A-Fa-f]))");
    public static String unescape(CharSequence image) {
        if(image == null) {
            return null;
        }
        Matcher m = ESCAPE.matcher(image);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            if (m.group("hex") != null) {
                String hexString = m.group(1);
                try {
                    int codePoint = Integer.parseInt(hexString, 16);
                    if (Character.isBmpCodePoint(codePoint)) {
                        m.appendReplacement(sb, "");
                        sb.append((char) codePoint);
                    } else if (Character.isValidCodePoint(codePoint)) {
                        m.appendReplacement(sb, "");
                        sb.append(Character.highSurrogate(codePoint));
                        sb.append(Character.lowSurrogate(codePoint));
                    } else {
                        m.appendReplacement(sb, m.group());
                    }
                } catch (NumberFormatException ex) {
                    return m.group();
                }
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group("direct")));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
