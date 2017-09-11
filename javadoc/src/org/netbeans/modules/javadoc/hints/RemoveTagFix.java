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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        DocCommentTree newDoc = make.DocComment(docComment.getFirstSentence(), docComment.getBody(), blockTags);
        Tree tree = ctx.getPath().getLeaf();
        javac.rewrite(tree, docComment, newDoc);
    }
    private static final Logger LOG = Logger.getLogger(RemoveTagFix.class.getName());
}
