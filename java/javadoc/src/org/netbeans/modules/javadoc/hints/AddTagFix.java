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
package org.netbeans.modules.javadoc.hints;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocTreePath;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.DocTreePathHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;
import static org.netbeans.modules.javadoc.hints.Bundle.*;

/**
 *
 * @author Jan Pokorsky
 * @author Ralph Benjamin Ruijs
 */
@NbBundle.Messages({"# {0} - @param name", "MISSING_PARAM_HINT=Add @param {0} tag",
    "# {0} - @param name", "MISSING_TYPEPARAM_HINT=Add @param {0} tag",
    "MISSING_RETURN_HINT=Add @return tag",
    "# {0} - Throwable name", "MISSING_THROWS_HINT=Add @throws {0} tag",
    "MISSING_DEPRECATED_HINT=Add @deprecated tag"})
abstract class AddTagFix extends JavaFix {

    private final String message;
    private final DocTreePathHandle dtph;
    private final int index;

    private AddTagFix(DocTreePathHandle dtph, String message, int index) {
        super(dtph.getTreePathHandle());
        this.dtph = dtph;
        this.message = message;
        this.index = index;
    }
    
    @Override
    protected void performRewrite(TransformationContext ctx) throws Exception {
        WorkingCopy javac = ctx.getWorkingCopy();
        DocTreePath path = dtph.resolve(javac);
        DocCommentTree docComment = path.getDocComment();
        TreeMaker make = javac.getTreeMaker();
        TagComparator comparator = new TagComparator();
        final List<DocTree> blockTags = new LinkedList<DocTree>();
        DocTree newTree = getNewTag(make);
        
        boolean added = false;
        int count = 0;
        for (DocTree docTree : docComment.getBlockTags()) {
            if (!added && comparator.compare(newTree, docTree) == TagComparator.HIGHER) {
                blockTags.add(newTree);
                added = true;
            }
            if (!added && comparator.compare(newTree, docTree) == TagComparator.EQUAL &&
                    index == count++) {
                blockTags.add(newTree);
                added = true;
            }
            blockTags.add(docTree);
        }
        if (!added) {
            blockTags.add(newTree);
        }
        
        DocCommentTree newDoc = make.DocComment(docComment.getFirstSentence(), docComment.getBody(), blockTags);
        Tree tree = ctx.getPath().getLeaf();
        javac.rewrite(tree, docComment, newDoc);
    }
    
    protected abstract DocTree getNewTag(TreeMaker make);

    public static JavaFix createAddParamTagFix(DocTreePathHandle dtph, final String name, final boolean isTypeParam, int index) {
        return new AddTagFix(dtph, isTypeParam? MISSING_TYPEPARAM_HINT("<" + name + ">"):MISSING_PARAM_HINT(name), index) {
            @Override
            protected DocTree getNewTag(TreeMaker make) {
                return make.Param(isTypeParam, make.DocIdentifier(name), Collections.emptyList());
            }
        };
    }

    public static JavaFix createAddReturnTagFix(DocTreePathHandle dtph) {
        return new AddTagFix(dtph, MISSING_RETURN_HINT(), -1) {
            @Override
            protected DocTree getNewTag(TreeMaker make) {
                return make.DocReturn(Collections.emptyList());
            }
        };
    }

    public static JavaFix createAddThrowsTagFix(DocTreePathHandle dtph, final String fqn, int throwIndex) {
        return new AddTagFix(dtph, MISSING_THROWS_HINT(fqn), throwIndex) {
            @Override
            protected DocTree getNewTag(TreeMaker make) {
                return make.Throws(make.Reference(make.Identifier(fqn), null, null), Collections.emptyList());
            }
        };
    }

    public static JavaFix createAddDeprecatedTagFix(DocTreePathHandle dtph) {
        return new AddTagFix(dtph, MISSING_DEPRECATED_HINT(), -1) {
            @Override
            protected DocTree getNewTag(TreeMaker make) {
                return make.Deprecated(Collections.emptyList());
            }
        };
    }
    
    
    /**
     * Orders tags as follows
     * <ul>
     * <li>@author (classes and interfaces only, required)</li>
     * <li>@version (classes and interfaces only, required. See footnote 1)</li>
     * <li>@param (methods and constructors only)</li>
     * <li>@return (methods only)</li>
     * <li>@exception (</li>
     * <li>@throws is a synonym added in Javadoc 1.2)</li>
     * <li>@see</li>
     * <li>@since</li>
     * <li>@serial (or @serialField or @serialData)</li>
     * <li>@deprecated (see How and When To Deprecate APIs)</li>
     * </ul>
     */
    private static class TagComparator implements Comparator<DocTree> {
        
        private final static int HIGHER = -1;
        private final static int EQUAL = 0;
        private final static int LOWER = 1;

        @Override
        public int compare(DocTree t, DocTree t1) {
            if(t.getKind() == t1.getKind()) {
                if(t.getKind() == DocTree.Kind.PARAM) {
                    ParamTree p = (ParamTree) t;
                    ParamTree p1 = (ParamTree) t1;
                    if(p.isTypeParameter() && !p1.isTypeParameter()) {
                        return HIGHER;
                    } else if(!p.isTypeParameter() && p1.isTypeParameter()) {
                        return LOWER;
                    }
                }
                return EQUAL;
            }
            switch(t.getKind()) {
                case AUTHOR:
                    return HIGHER;
                case VERSION:
                    if(t1.getKind() == DocTree.Kind.AUTHOR) {
                        return LOWER;
                    }
                    return HIGHER;
                case PARAM:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION) {
                        return LOWER;
                    }
                    return HIGHER;
                case RETURN:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM) {
                        return LOWER;
                    }
                    return HIGHER;
                case EXCEPTION:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN) {
                        return LOWER;
                    }
                    return HIGHER;
                case THROWS:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN
                            || t1.getKind() == DocTree.Kind.EXCEPTION) {
                        return LOWER;
                    }
                    return HIGHER;
                case SEE:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN
                            || t1.getKind() == DocTree.Kind.EXCEPTION
                            || t1.getKind() == DocTree.Kind.THROWS) {
                        return LOWER;
                    }
                    return HIGHER;
                case SINCE:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN
                            || t1.getKind() == DocTree.Kind.EXCEPTION
                            || t1.getKind() == DocTree.Kind.THROWS
                            || t1.getKind() == DocTree.Kind.SEE) {
                        return LOWER;
                    }
                    return HIGHER;
                case SERIAL:
                case SERIAL_DATA:
                case SERIAL_FIELD:
                    if(t1.getKind() == DocTree.Kind.AUTHOR
                            || t1.getKind() == DocTree.Kind.VERSION
                            || t1.getKind() == DocTree.Kind.PARAM
                            || t1.getKind() == DocTree.Kind.RETURN
                            || t1.getKind() == DocTree.Kind.EXCEPTION
                            || t1.getKind() == DocTree.Kind.THROWS
                            || t1.getKind() == DocTree.Kind.SEE
                            || t1.getKind() == DocTree.Kind.SINCE) {
                        return LOWER;
                    }
                    return HIGHER;
                case DEPRECATED:
                    if(t1.getKind() == DocTree.Kind.UNKNOWN_BLOCK_TAG) {
                        return HIGHER;
                    }
                    return LOWER;
                case UNKNOWN_BLOCK_TAG:
                    return LOWER;
            }
            return LOWER;
        }
        
    }
    
    @Override
    public String getText() {
        return message;
    }
}
