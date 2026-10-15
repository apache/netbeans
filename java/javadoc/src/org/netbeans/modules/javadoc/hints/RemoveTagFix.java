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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocTreePath;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.DocTreePathHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.util.NbBundle;
import static org.netbeans.modules.javadoc.hints.Bundle.*;
import org.netbeans.spi.java.hints.JavaFix;

/**
 *
 * @author Jan Pokorsky
 * @author Ralph Benjamin Ruijs
 */
final class RemoveTagFix extends JavaFix {

    private String tagName;
    private final DocTreePathHandle dtph;

    RemoveTagFix(DocTreePathHandle dtph, String tagName) {
        super(dtph.getTreePathHandle());
        this.dtph = dtph;
        this.tagName = tagName;
    }

    @Override
    @NbBundle.Messages({"# {0} - tag name {@param|@throws|...}", "REMOVE_TAG_HINT=Remove {0} tag"})
    public String getText() {
        return REMOVE_TAG_HINT(tagName); // NOI18N
    }

    @Override
    protected void performRewrite(TransformationContext ctx) throws Exception {
        WorkingCopy javac = ctx.getWorkingCopy();
        DocTreePath path = dtph.resolve(javac);
        if(path == null) {
            LOG.log(Level.WARNING, "Cannot resolve DocTreePathHandle: {0}", dtph);
            return;
        }
        DocCommentTree docComment = path.getDocComment();
        TreeMaker make = javac.getTreeMaker();
        final List<DocTree> blockTags = new LinkedList<DocTree>();
        for (DocTree docTree : docComment.getBlockTags()) {
            if (docTree != path.getLeaf()) {
                blockTags.add(docTree);
            }
        }
        DocCommentTree newDoc = make.DocComment(docComment.getFullBody(), blockTags);
        Tree tree = ctx.getPath().getLeaf();
        javac.rewrite(tree, docComment, newDoc);
    }
    private static final Logger LOG = Logger.getLogger(RemoveTagFix.class.getName());
}
