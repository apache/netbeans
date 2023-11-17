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
package org.netbeans.modules.php.editor.parser.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 * This is AST Utils class.
 * @author Petr Pisl
 */
public final class Utils {

    private Utils() {
    }

    /**
     *
     * @param root a Program node, where to look for the comment
     * @param node  a Node for which a commen you want to find.
     * @return appropriate comment or null, if the comment doesn't exists.
     */
    public static Comment getCommentForNode(Program root, ASTNode node) {
        List<Comment> comments = root.getComments();
        Comment possible = null;

        if (node.getEndOffset() <= root.getEndOffset()) {
            for (Comment comm : comments) {
                if (comm.getEndOffset() < node.getStartOffset()) {
                    possible = comm;
                } else {
                    break;
                }
            }
            if (possible != null && (possible.getEndOffset() + 1 < node.getStartOffset())) {
                int start = possible.getEndOffset() + 1;
                int end = getNodeRangeLocatorEndOffset(node);
                if (start <= end) {
                    List<ASTNode> nodes = (new NodeRangeLocator()).locate(root, new OffsetRange(start, end));
                    if (!nodes.isEmpty()) {
                        if (!isConstantDeclaration(nodes, node)
                                && !isFieldDeclaration(nodes, node)) {
                            possible = null;
                        }
                    }
                } else {
                    // e.g. public self|A /* comment */ |null $unionType; (start > end)
                    possible = null;
                }
            }
        }

        return possible;
    }

    private static int getNodeRangeLocatorEndOffset(ASTNode node) {
        int endOffset = node.getStartOffset() - 1;
        if (node instanceof SingleFieldDeclaration) {
            // GH-6310 start offset of SingleFieldDeclaration is start offset of a variable name
            // because we can declare multiple fields as one statement e.g. public int $field1, $field2;
            // so, check a field type
            SingleFieldDeclaration field = (SingleFieldDeclaration) node;
            Expression fieldType = field.getFieldType();
            if (fieldType != null) {
                endOffset = fieldType.getStartOffset() - 1;
            }
        }
        return endOffset;
    }

    private static boolean isConstantDeclaration(List<ASTNode> nodes, ASTNode node) {
        boolean isConstantDeclaration = false;
        if (nodes.size() == 1 && (node instanceof Scalar)) {
            ASTNode next = nodes.iterator().next();
            if (next instanceof FunctionName) {
                FunctionName fnc = (FunctionName) next;
                String functionName = CodeUtils.extractFunctionName(fnc);
                if (functionName != null && "define".equalsIgnoreCase(functionName)) { //NOI18N
                    isConstantDeclaration = true;
                }
            }
        }
        return isConstantDeclaration;
    }

    private static boolean isFieldDeclaration(List<ASTNode> nodes, ASTNode node) {
        boolean isFieldDeclaration = false;
        if (!nodes.isEmpty() && (node instanceof Identifier)) {
            Iterator<ASTNode> iterator = nodes.iterator();
            ASTNode next = iterator.next();
            if (isTypeNode(next)) {
                isFieldDeclaration = true;
                while (iterator.hasNext()) {
                    // e.g. public int $f1, $f2, $f3 = 0;
                    next = iterator.next();
                    if (!(next instanceof SingleFieldDeclaration)) {
                        isFieldDeclaration = false;
                        break;
                    }
                }
            }
        }
        return isFieldDeclaration;
    }

    private static boolean isTypeNode(ASTNode node) {
        return node instanceof NamespaceName
                || node instanceof NullableType
                || node instanceof UnionType
                || node instanceof IntersectionType;
    }

    public static Program getRoot(ParserResult result) {
        if (result == null) {
            return null;
        }

        if (result instanceof PHPParseResult) {
            return ((PHPParseResult) result).getProgram();
        } else {
            return null;
        }
    }

    public static ASTNode getNodeAtOffset(ParserResult info, int astOffset) {
        Program program = getRoot(info);
        return getNodeAtOffset(program, astOffset);
    }

    /**
     * Return an ASTNode at the given offset. It doesn't count comments.
     * @param node
     * @param astOffset
     * @return null if there is not a node on this possition or an ASTNode except comments
     */
    public static ASTNode getNodeAtOffset(ASTNode node, int offset) {
        if (node.getStartOffset() > offset || node.getEndOffset() < offset) {
            return null;
        }
        return (new NodeLocator()).locate(node, offset);

    }

    public static ASTNode getNodeAfterOffset(ParserResult info, int offset) {
        return (new AfterOffsetLocator()).locate(getRoot(info), offset);
    }

    public static ASTNode[] getNodeHierarchyAtOffset(ASTNode node, int offset) {
        if (node.getStartOffset() > offset || node.getEndOffset() < offset) {
            return null;
        }

        return (new NodeHierarchyFinder()).find(node, offset);
    }

    /**
     * Return an ASTNode of given type at the given offset. It doesn't count comments.
     *
     * @param node
     * @param astOffset
     * @param terminus
     * @return null if there is not a node on this possition or an ASTNode except comments
     */
    public static ASTNode getNodeAtOffset(ASTNode node, int offset, Class<? extends ASTNode> terminus) {
        if (node.getStartOffset() > offset || node.getEndOffset() < offset) {
            return null;
        }

        return (new SpecificClassNodeLocator(terminus)).locate(node, offset);
    }

    private static class AfterOffsetLocator extends DefaultVisitor {
        private int offset;
        private ASTNode resultNode = null;
        private int lastStartOffset = -1;

        public ASTNode locate(ASTNode beginNode, int offset) {
            this.offset = offset;
            scan(beginNode);
            return resultNode;
        }

        @Override
        public void scan(final ASTNode node) {
            if (node != null) {
                int startOffset = node.getStartOffset();
                if (startOffset >= offset && (lastStartOffset == -1 || startOffset < lastStartOffset)) {
                    lastStartOffset = startOffset;
                    resultNode = node;
                }
                node.accept(this);
            }
        }

    }

    private static class NodeLocator extends DefaultVisitor {

        protected int offset = 0;
        protected ASTNode node = null;
        private Program programNode = null;

        public ASTNode locate(ASTNode beginNode, int astOffset) {
            offset = astOffset;
            scan(beginNode);
            if (node instanceof Program) {
                // probably no node was found except whole file.
                // try to look for a documentation node
                processComments((Program) node);
            } else if (programNode != null) {
                // we need to handle comment nodes too,
                // but we need a program node for that
                processComments((Program) programNode);
            }
            return this.node;
        }

        private void processComments(final Program program) {
            List<Comment> comments = program.getComments();
            for (Comment comment : comments) {
                if (comment.getStartOffset() <= offset && offset <= comment.getEndOffset()) {
                    scan(comment);
                }
            }
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null) {
                if (node.getStartOffset() <= offset && offset <= node.getEndOffset()) {
                    this.node = node;
                    node.accept(this);
                }
            }
        }

        @Override
        public void visit(Program node) {
            programNode = node;
            super.visit(node);
        }

        @Override
        public void visit(PHPDocTypeTag node) {
        }

        @Override
        public void visit(PHPDocVarTypeTag node) {
        }

        @Override
        public void visit(PHPDocMethodTag node) {
        }
    }

    private static class NodeHierarchyFinder extends DefaultTreePathVisitor {

        private ASTNode[] hierarchy;
        protected int offset = 0;

        public ASTNode[] find(ASTNode beginNode, int astOffset) {
            offset = astOffset;
            scan(beginNode);
            return hierarchy;
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null) {
                if (node.getStartOffset() <= offset && offset <= node.getEndOffset()) {
                    hierarchy = getPath().toArray(new ASTNode[getPath().size()]);
                    node.accept(this);
                }
            }
        }
    }

    private static class SpecificClassNodeLocator extends NodeLocator {

        private Class<? extends ASTNode> terminus;

        public SpecificClassNodeLocator(Class<? extends ASTNode> terminus) {
            this.terminus = terminus;
        }

        @Override
        public void scan(ASTNode node) {
            if (terminus.isInstance(node)) {
                if (node.getStartOffset() <= offset && offset <= node.getEndOffset()) {
                    this.node = node;
                }
            } else {
                super.scan(node);
            }
        }
    }

    private static class NodeRangeLocator extends DefaultVisitor {

        private OffsetRange range;
        private List<ASTNode> nodes = new ArrayList<>();

        public List<ASTNode> locate(ASTNode beginNode, OffsetRange range) {
            this.range = range;
            scan(beginNode);
            return nodes;
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null) {
                if (range.getStart() <= node.getStartOffset() && node.getEndOffset() <= range.getEnd()) {
                    // node is in the range
                    nodes.add(node);
                } else {
                    if ((node.getStartOffset() < range.getStart() && range.getStart() < node.getEndOffset())
                            || (node.getStartOffset() < range.getEnd() && range.getEnd() < node.getEndOffset())) {
                        // node is partialy in the range.
                        node.accept(this);
                    }
                }

            }
        }
    }

    public static String resolveVariableName(Variable variable) {
        String name = null;
        if (variable.getName() instanceof Identifier) {
            name = ((Identifier) variable.getName()).getName();
        }
        return name;
    }

    public static List<PHPDocVarTypeTag> getPropertyTags(Program root, ClassDeclaration node) {
        List<PHPDocVarTypeTag> tags = new ArrayList<>();
        Comment comment = Utils.getCommentForNode(root, node);
        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;
            for (PHPDocTag tag : phpDoc.getTags()) {
                if (tag.getKind().equals(PHPDocTag.Type.PROPERTY)
                        || tag.getKind().equals(PHPDocTag.Type.PROPERTY_READ)
                        || tag.getKind().equals(PHPDocTag.Type.PROPERTY_WRITE)) {
                    tags.add((PHPDocVarTypeTag) tag);
                }
            }
        }
        return tags;
    }
}
