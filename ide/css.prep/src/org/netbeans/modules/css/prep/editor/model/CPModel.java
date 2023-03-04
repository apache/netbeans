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
package org.netbeans.modules.css.prep.editor.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import static org.netbeans.modules.css.lib.api.NodeType.cp_variable_declaration;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.prep.editor.CPType;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 * naive temporary impl.
 *
 * @author marekfukala
 */
public class CPModel {

    public static String topLevelSnapshotMimetype; //set by unit tests
    private static final Collection<String> SASS_DIRECTIVES = Arrays.asList(new String[]{
        "@content",
        "@debug",
        "@each",
        "@extend",
        "@if",
        "@include",
        "@for",
        "@else",
        "@else if",
        "@function",
        "@mixin",
        "@return",
        "@warn",
        "@while",
        "@at-root"
    });

    public static CPModel getModel(CssParserResult result) {
        CPModel curr = result.getProperty(CPModel.class);
        if (curr == null) {
            curr = new CPModel(result);
            result.setProperty(CPModel.class, curr);
        }
        return curr;
    }

    /**
     * Will run parsing task!.
     *
     * @param file
     * @return
     */
    public static CPModel getModel(FileObject file) throws ParseException {
        final AtomicReference<CPModel> model_ref = new AtomicReference<>();
        Source source = Source.create(file);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator cssRI = WebUtils.getResultIterator(resultIterator, "text/css");
                if (cssRI != null) {
                    CssParserResult result = (CssParserResult) cssRI.getParserResult();
                    if (result != null) {
                        model_ref.set(CPModel.getModel(result));
                    }
                }
            }
        });

        return model_ref.get();
    }
    private CPType cpType;
    private final Snapshot snapshot;
    private final Collection<CPElement> variables, mixins;

    private CPModel(CssParserResult result) {
        snapshot = result.getSnapshot();
        variables = findVariables(result);
        mixins = findMixins(result);
        cpType = findPreprocessorType();
    }

    /**
     * Gets all variables from this file's model.
     *
     * @return
     */
    public Collection<CPElement> getVariables() {
        return variables;
    }

    public Collection<CPElement> getMixins() {
        return mixins;
    }

    public CPType getPreprocessorType() {
        return cpType;
    }

    public FileObject getFile() {
        return snapshot.getSource().getFileObject();
    }

    private CPType findPreprocessorType() {
        CPType type;
        String fileMimetype = topLevelSnapshotMimetype != null
                ? topLevelSnapshotMimetype //unit tests - fileless snapshots
                : snapshot.getSource().getFileObject().getMIMEType();
        if (fileMimetype == null) {
            type = CPType.NONE;
        } else {
            switch (fileMimetype) {
                case "text/less":
                    //NOI18N
                    type = CPType.LESS;
                    break;
                case "text/scss":
                    //NOI18N
                    type = CPType.SCSS;
                    break;
                default:
                    type = CPType.NONE;
                    break;
            }
        }
        return type;
    }

    /**
     * Gets both var and mixin elements.
     *
     * @return
     */
    public Collection<CPElement> getElements() {
        Collection<CPElement> all = new ArrayList<>();
        all.addAll(getVariables());
        all.addAll(getMixins());
        return all;
    }

    /**
     * Gets a collection of variables accessible(visible) at the given location.
     *
     * @param offset
     * @return
     */
    public Collection<CPElement> getVariables(int offset) {
        Collection<CPElement> visible = new ArrayList<>();
        for (CPElement var : getVariables()) {
            OffsetRange context = var.getScope();
            if (context == null || context.containsInclusive(offset)) {
                visible.add(var);
            }
        }
        return visible;
    }

    /**
     * Returns a variable at the given position if there's any.
     *
     * @param offset
     * @return found variable or null
     */
    public CPElement getVariableAtOffset(int offset) {
        for (CPElement var : getVariables()) {
            OffsetRange context = var.getRange();
            if (context == null || context.containsInclusive(offset)) {
                return var;
            }
        }
        return null;
    }

    /**
     * Gets all variables from this file's model.
     *
     * @return
     */
    private Collection<CPElement> findVariables(CssParserResult result) {
        final Collection<CPElement> vars = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            private boolean in_cp_variable_declaration, in_block_control, in_block;
            private Stack<OffsetRange> contexts = new Stack<>();
            private Collection<CPElement> elementsAwaitingBlockNode = new ArrayList<>();

            @Override
            public boolean visit(Node node) {

                switch (node.type()) {
                    case sass_control_block:
                    case declarations:
                        OffsetRange range = new OffsetRange(node.from(), node.to());
                        contexts.push(range);

                        //set scope to the elements preceeding this block node but defining its scope as the block content
                        for (CPElement e : elementsAwaitingBlockNode) {
                            e.setScope(range);
                        }
                        elementsAwaitingBlockNode.clear();

                        //the declarations node represents a content of a code block
                        in_block = true;

                        _visitChildren(this, node);

                        contexts.pop();
                        in_block = false;
                        break;

                    case cp_variable_declaration:
                    case sass_map: //also represents a variable declaration
                        in_cp_variable_declaration = true;

                        _visitChildren(this, node);

                        in_cp_variable_declaration = false;
                        break;

                    case sass_control:
                    case cp_args_list:
                        in_block_control = true;

                        _visitChildren(this, node);

                        in_block_control = false;
                        break;

                    case cp_variable:
                        //determine the variable type
                        CPElementType type;
                        if (in_block_control && !in_block /* for sass_control which contains also the block */) {
                            type = CPElementType.VARIABLE_DECLARATION_IN_BLOCK_CONTROL;
                        } else {
                            if (in_cp_variable_declaration) {
                                if (in_block) {
                                    type = CPElementType.VARIABLE_LOCAL_DECLARATION;
                                } else {
                                    type = CPElementType.VARIABLE_GLOBAL_DECLARATION;
                                }
                            } else {
                                type = CPElementType.VARIABLE_USAGE;
                            }
                        }
                        OffsetRange scope = contexts.isEmpty() ? null : contexts.peek();

                        CPElementHandle handle = new CPElementHandle(getFile(), node.image().toString().trim(), type, NodeUtil.getElementId(node));
                        OffsetRange variableRange = new OffsetRange(node.from(), node.to());
                        CPElement element = new CPElement(handle, variableRange, scope);

                        switch (type) {
                            case VARIABLE_DECLARATION_IN_BLOCK_CONTROL:
                                //scope is null as the variable is declared in sass control before the actual block node
                                //just remember the element and we will set the proper block scope later during parsing
                                elementsAwaitingBlockNode.add(element);
                                break;
                        }

                        vars.add(element);
                        break;

                    case token:
                        //ignore toke nodes
                        break;

                    default:
                        _visitChildren(this, node);

                }
                return false;
            }
        };
        visitor.visit(result.getParseTree());
        return vars;
    }

    private static void _visitChildren(NodeVisitor visitor, Node node) {
        List<Node> children = node.children();
        if (children != null) {
            for (Node child : children) {
                visitor.visit(child);
            }
        }
    }

    //XXX mixin usages!
    private Collection<CPElement> findMixins(CssParserResult result) {
        final Collection<CPElement> items = new ArrayList<>();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case cp_mixin_declaration:
                        Node mixin_name = NodeUtil.getChildByType(node, NodeType.cp_mixin_name);
                        if (mixin_name != null) {
                            CPElementHandle handle = new CPElementHandle(getFile(), mixin_name.image().toString().trim(), CPElementType.MIXIN_DECLARATION, NodeUtil.getElementId(node));
                            OffsetRange variableRange = new OffsetRange(mixin_name.from(), mixin_name.to());
                            OffsetRange scope = null; //TODO implement!
                            CPElement element = new CPElement(handle, variableRange, scope);
                            items.add(element);
                        }
                        break;

                    case cp_mixin_call:
                        mixin_name = NodeUtil.getChildByType(node, NodeType.cp_mixin_name);
                        if (mixin_name != null) {
                            CPElementHandle handle = new CPElementHandle(getFile(), mixin_name.image().toString().trim(), CPElementType.MIXIN_USAGE, NodeUtil.getElementId(node));
                            OffsetRange variableRange = new OffsetRange(mixin_name.from(), mixin_name.to());
                            OffsetRange scope = null; //TODO implement!
                            CPElement element = new CPElement(handle, variableRange, scope);
                            items.add(element);
                        }
                        break;

                    default:
                        //visit children
                        List<Node> children = node.children();
                        if (children != null) {
                            for (Node child : children) {
                                visit(child);
                            }
                        }
                        break;
                }
                return false;
            }
        };
        visitor.visit(result.getParseTree());
        return items;
    }

    public Collection<String> getMixinNames() {
        return getElementNames(getMixins());
    }

    public Collection<String> getVarNames() {
        return getElementNames(getVariables());
    }

    private static Collection<String> getElementNames(Collection<? extends CPElement> elements) {
        Collection<String> names = new HashSet<>();
        for (CPElement e : elements) {
            names.add(e.getName().toString());
        }
        return names;
    }

    public Collection<String> getDirectives() {
        switch (getPreprocessorType()) {
            case SCSS:
                return SASS_DIRECTIVES;
            default:
                //nothing
                return Collections.emptyList();
        }
    }
}
