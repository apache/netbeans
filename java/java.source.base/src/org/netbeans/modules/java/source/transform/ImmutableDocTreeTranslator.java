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
package org.netbeans.modules.java.source.transform;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.HiddenTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.IndexTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ProvidesTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.UsesTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.doctree.SnippetTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.DCTree;
import com.sun.tools.javac.tree.DCTree.DCReference;
import com.sun.tools.javac.util.Name;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class ImmutableDocTreeTranslator extends ImmutableTreeTranslator implements DocTreeVisitor<DocTree, Object> {

    private Map<DocTree, Object> tree2Tag;

    public ImmutableDocTreeTranslator(WorkingCopy copy) {
        super(copy);
    }

    public DocTree translate(DocTree tree) {
        if (tree == null) {
            return null;
        } else if (tree.getKind().name().equals("SNIPPET")){
            return rewriteSnippetChildren(tree);
        }else {
            DocTree t = tree.accept(this, null);
            if (tree2Tag != null && tree != t) {
                tree2Tag.put(t, tree2Tag.get(tree));
            }
            return t;
        }
    }

    /**
     * Visitor method: translate a list of nodes.
     */
    @SuppressWarnings("unchecked")
    public <T extends DocTree> List<T> translateDoc(List<T> trees) {
        if (trees == null || trees.isEmpty()) {
            return trees;
        }
        List<T> newTrees = new ArrayList<T>();
        boolean changed = false;
        for (T t : trees) {
            T newT = (T) translate(t);
            if (newT != t) {
                changed = true;
            }
            if (newT != null) {
                newTrees.add(newT);
            }
        }
        return changed ? newTrees : trees;
    }

    //<editor-fold defaultstate="collapsed" desc="Rewrites">
    protected final DocCommentTree rewriteChildren(DocCommentTree tree) {
        DocCommentTree value = tree;
        List<? extends DocTree> fullBody = translateDoc(tree.getFullBody());
        List<? extends DocTree> blockTags = translateDoc(tree.getBlockTags());
        if (fullBody != tree.getFullBody()|| blockTags != tree.getBlockTags()) {
            value = make.DocComment(fullBody, blockTags);
        }
        return value;
    }

    protected final AttributeTree rewriteChildren(AttributeTree tree) {
        AttributeTree value = tree;
        List<? extends DocTree> vl = translateDoc(tree.getValue());
        if (vl != tree.getValue()) {
            value = make.Attribute((Name) tree.getName(), tree.getValueKind(), vl);
        }
        return value;
    }

    protected final AuthorTree rewriteChildren(AuthorTree tree) {
        AuthorTree value = tree;
        List<? extends DocTree> name = translateDoc(tree.getName());
        if (name != tree.getName()) {
            value = make.Author(name);
        }
        return value;
    }

    protected final CommentTree rewriteChildren(CommentTree tree) {
        return tree; // Nothing to do for a string
    }

    protected final DeprecatedTree rewriteChildren(DeprecatedTree tree) {
        DeprecatedTree value = tree;
        List<? extends DocTree> body = tree.getBody();
        if (body != tree.getBody()) {
            value = make.Deprecated(body);
        }
        return value;
    }

    protected final DocRootTree rewriteChildren(DocRootTree tree) {
        return tree; // Nothing to do for tag without attributes
    }

    protected final EndElementTree rewriteChildren(EndElementTree tree) {
        return tree; // Nothing to do for a string
    }

    protected final EntityTree rewriteChildren(EntityTree tree) {
        return tree; // Nothing to do for a string
    }

    protected final ErroneousTree rewriteChildren(ErroneousTree tree) {
        return tree; // XXX: Not implemented yet
    }

    protected final HiddenTree rewriteChildren(HiddenTree tree) {
        HiddenTree value = tree;
        List<? extends DocTree> body = translateDoc(tree.getBody());
        if (body != tree.getBody()) {
            value = make.Hidden(body);
        }
        return value;
    }

    protected final IdentifierTree rewriteChildren(IdentifierTree tree) {
        return tree; // Nothing to do for a string
    }

    protected final IndexTree rewriteChildren(IndexTree tree) {
        IndexTree value = tree;
        List<? extends DocTree> desc = translateDoc(tree.getDescription());
        DocTree term = translate(tree.getSearchTerm());
        if (desc != tree.getDescription() || term != tree.getSearchTerm()) {
            value = make.Index(term, desc);
        }
        return value;
    }

    protected final InheritDocTree rewriteChildren(InheritDocTree tree) {
        return tree; // Nothing to do for tag without attributes
    }

    protected final LinkTree rewriteChildren(LinkTree tree) {
        LinkTree value = tree;
        List<? extends DocTree> label = translateDoc(tree.getLabel());
        ReferenceTree ref = (ReferenceTree) translate(tree.getReference());
        if (label != tree.getLabel() || ref != tree.getReference()) {
            value = make.Link(ref, label);
        }
        return value;
    }

    protected final LiteralTree rewriteChildren(LiteralTree tree) {
        LiteralTree value = tree;
        TextTree body = (TextTree) translate(tree.getBody());
        if (body != tree.getBody()) {
            if(tree.getKind() == DocTree.Kind.CODE) {
                value = make.Code(body);
            } else {
                value = make.Literal(body);
            }
        }
        return value;
    }

    protected final ParamTree rewriteChildren(ParamTree tree) {
        ParamTree value = tree;
        IdentifierTree name = (IdentifierTree) translate(tree.getName());
        List<? extends DocTree> description = translateDoc(tree.getDescription());
        if (name != tree.getName() || description != tree.getDescription()) {
            value = make.Param(tree.isTypeParameter(), name, description);
        }
        return value;
    }

    protected final ProvidesTree rewriteChildren(ProvidesTree tree) {
        ProvidesTree value = tree;
        ReferenceTree name = (ReferenceTree) translate(tree.getServiceType());
        List<? extends DocTree> description = translateDoc(tree.getDescription());
        if (name != tree.getServiceType()|| description != tree.getDescription()) {
            value = make.Provides(name, description);
        }
        return value;
    }

    protected final ReferenceTree rewriteChildren(ReferenceTree tree) {
        DCReference refTree = (DCReference) tree;
        ReferenceTree value = tree;
        ExpressionTree classReference = (ExpressionTree) translate(refTree.qualifierExpression);
        List<? extends Tree> methodParameters = translate(refTree.paramTypes);
        if(classReference != refTree.qualifierExpression || methodParameters != refTree.paramTypes) {
            value = make.Reference(classReference, refTree.memberName, methodParameters);
        }
        return value;
    }

    protected final ReturnTree rewriteChildren(ReturnTree tree) {
        ReturnTree value = tree;
        List<? extends DocTree> description = translateDoc(tree.getDescription());
        if (description != tree.getDescription()) {
            value = make.Return(description);
        }
        return value;
    }

    protected final SeeTree rewriteChildren(SeeTree tree) {
        SeeTree value = tree;
        List<? extends DocTree> ref = translateDoc(tree.getReference());
        if (ref != tree.getReference()) {
            value = make.See(ref);
        }
        return value;
    }

    protected final SerialTree rewriteChildren(SerialTree tree) {
        SerialTree value = tree;
        List<? extends DocTree> desc = translateDoc(tree.getDescription());
        if (desc != tree.getDescription()) {
            value = make.Serial(desc);
        }
        return value;
    }

    protected final SerialDataTree rewriteChildren(SerialDataTree tree) {
        SerialDataTree value = tree;
        List<? extends DocTree> desc = translateDoc(tree.getDescription());
        if (desc != tree.getDescription()) {
            value = make.SerialData(desc);
        }
        return value;
    }

    protected final SerialFieldTree rewriteChildren(SerialFieldTree tree) {
        SerialFieldTree value = tree;
        IdentifierTree name = (IdentifierTree) translate(tree.getName());
        List<? extends DocTree> description = translateDoc(tree.getDescription());
        ReferenceTree ref = (ReferenceTree) translate(tree.getType());
        if (ref != tree.getType() || name != tree.getName() || description != tree.getDescription()) {
            value = make.SerialField(name, ref, description);
        }
        return value;
    }

    protected final SinceTree rewriteChildren(SinceTree tree) {
        SinceTree value = tree;
        List<? extends DocTree> body = tree.getBody();
        if (body != tree.getBody()) {
            value = make.Since(body);
        }
        return value;
    }

    protected final StartElementTree rewriteChildren(StartElementTree tree) {
        StartElementTree value = tree;
        List<? extends DocTree> attributes = translateDoc(tree.getAttributes());
        if (attributes != tree.getAttributes()) {
            value = make.StartElement((Name) tree.getName(), attributes, tree.isSelfClosing());
        }
        return value;
    }

    protected final TextTree rewriteChildren(TextTree tree) {
        return tree; // Nothing to do for a string
    }

    protected final ThrowsTree rewriteChildren(ThrowsTree tree) {
        ThrowsTree value = tree;
        ReferenceTree exception = (ReferenceTree) translate(tree.getExceptionName());
        List<? extends DocTree> description = translateDoc(tree.getDescription());
        if (exception != tree.getExceptionName() || description != tree.getDescription()) {
            value = make.Throws(exception, description);
        }
        return value;
    }

    protected final UnknownBlockTagTree rewriteChildren(UnknownBlockTagTree tree) {
        UnknownBlockTagTree value = tree;
        List<? extends DocTree> content = translateDoc(tree.getContent());
        if (content != tree.getContent()) {
            value = make.UnknownBlockTag(((DCTree.DCUnknownBlockTag) tree).name, tree.getContent());
        }
        return value;
    }

    protected final UnknownInlineTagTree rewriteChildren(UnknownInlineTagTree tree) {
        UnknownInlineTagTree value = tree;
        List<? extends DocTree> content = translateDoc(tree.getContent());
        if (content != tree.getContent()) {
            value = make.UnknownInlineTag(((DCTree.DCUnknownInlineTag) tree).name, tree.getContent());
        }
        return value;
    }

    protected final UsesTree rewriteChildren(UsesTree tree) {
        UsesTree value = tree;
        ReferenceTree name = (ReferenceTree) translate(tree.getServiceType());
        List<? extends DocTree> description = translateDoc(tree.getDescription());
        if (name != tree.getServiceType()|| description != tree.getDescription()) {
            value = make.Uses(name, description);
        }
        return value;
    }

    protected final ValueTree rewriteChildren(ValueTree tree) {
        ValueTree value = tree;
        ReferenceTree reference = (ReferenceTree) translate(tree.getReference());
        if (reference != tree.getReference()) {
            value = make.Value(reference);
        }
        return value;
    }

    protected final VersionTree rewriteChildren(VersionTree tree) {
        VersionTree value = tree;
        List<? extends DocTree> body = translateDoc(tree.getBody());
        if (body != tree.getBody()) {
            value = make.Version(body);
        }
        return value;
    }  

    protected final DocTree rewriteSnippetChildren(DocTree tree) {
        DocTree value = tree;
		SnippetTree javadocSnippet = (SnippetTree)tree;
        List<? extends DocTree> snippetTreeAttributes = translateDoc(javadocSnippet.getAttributes());
        TextTree snippetTreeText = (TextTree) translate(javadocSnippet.getBody());
        if((snippetTreeAttributes != javadocSnippet.getAttributes()) || (snippetTreeText != javadocSnippet.getBody())){
            value=make.Snippet(snippetTreeAttributes, snippetTreeText);
        }    
        return value;
    }
    
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="VisitMethods">
    @Override
    public DocTree visitAttribute(AttributeTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitAuthor(AuthorTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitComment(CommentTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitDeprecated(DeprecatedTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitDocComment(DocCommentTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitDocRoot(DocRootTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitEndElement(EndElementTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitEntity(EntityTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitErroneous(ErroneousTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitHidden(HiddenTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitIdentifier(IdentifierTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitIndex(IndexTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitInheritDoc(InheritDocTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitLink(LinkTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitLiteral(LiteralTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitParam(ParamTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitProvides(ProvidesTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitReference(ReferenceTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitReturn(ReturnTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitSee(SeeTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitSerial(SerialTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitSerialData(SerialDataTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitSerialField(SerialFieldTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitSince(SinceTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitStartElement(StartElementTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitText(TextTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitThrows(ThrowsTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitUnknownBlockTag(UnknownBlockTagTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitUnknownInlineTag(UnknownInlineTagTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitUses(UsesTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitValue(ValueTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitVersion(VersionTree tree, Object p) {
        return rewriteChildren(tree);
    }

    @Override
    public DocTree visitOther(DocTree tree, Object p) {
        throw new Error("DocTree not overloaded: " + tree);
    }
    //</editor-fold>
}
