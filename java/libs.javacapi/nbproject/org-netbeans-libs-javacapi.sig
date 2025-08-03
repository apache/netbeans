#Signature file v4.1
#Version 8.51.0

CLSS public abstract interface com.sun.source.doctree.AttributeTree
innr public final static !enum ValueKind
intf com.sun.source.doctree.DocTree
meth public abstract com.sun.source.doctree.AttributeTree$ValueKind getValueKind()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getValue()
meth public abstract javax.lang.model.element.Name getName()

CLSS public final static !enum com.sun.source.doctree.AttributeTree$ValueKind
 outer com.sun.source.doctree.AttributeTree
fld public final static com.sun.source.doctree.AttributeTree$ValueKind DOUBLE
fld public final static com.sun.source.doctree.AttributeTree$ValueKind EMPTY
fld public final static com.sun.source.doctree.AttributeTree$ValueKind SINGLE
fld public final static com.sun.source.doctree.AttributeTree$ValueKind UNQUOTED
meth public static com.sun.source.doctree.AttributeTree$ValueKind valueOf(java.lang.String)
meth public static com.sun.source.doctree.AttributeTree$ValueKind[] values()
supr java.lang.Enum<com.sun.source.doctree.AttributeTree$ValueKind>

CLSS public abstract interface com.sun.source.doctree.AuthorTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getName()

CLSS public abstract interface com.sun.source.doctree.BlockTagTree
intf com.sun.source.doctree.DocTree
meth public abstract java.lang.String getTagName()

CLSS public abstract interface com.sun.source.doctree.CommentTree
intf com.sun.source.doctree.DocTree
meth public abstract java.lang.String getBody()

CLSS public abstract interface com.sun.source.doctree.DeprecatedTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getBody()

CLSS public abstract interface com.sun.source.doctree.DocCommentTree
intf com.sun.source.doctree.DocTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getBlockTags()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getBody()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getFirstSentence()
meth public java.util.List<? extends com.sun.source.doctree.DocTree> getFullBody()
meth public java.util.List<? extends com.sun.source.doctree.DocTree> getPostamble()
meth public java.util.List<? extends com.sun.source.doctree.DocTree> getPreamble()

CLSS public abstract interface com.sun.source.doctree.DocRootTree
intf com.sun.source.doctree.InlineTagTree

CLSS public abstract interface com.sun.source.doctree.DocTree
innr public final static !enum Kind
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(com.sun.source.doctree.DocTreeVisitor<{%%0},{%%1}>,{%%1})
meth public abstract com.sun.source.doctree.DocTree$Kind getKind()

CLSS public final static !enum com.sun.source.doctree.DocTree$Kind
 outer com.sun.source.doctree.DocTree
fld public final java.lang.String tagName
fld public final static com.sun.source.doctree.DocTree$Kind ATTRIBUTE
fld public final static com.sun.source.doctree.DocTree$Kind AUTHOR
fld public final static com.sun.source.doctree.DocTree$Kind CODE
fld public final static com.sun.source.doctree.DocTree$Kind COMMENT
fld public final static com.sun.source.doctree.DocTree$Kind DEPRECATED
fld public final static com.sun.source.doctree.DocTree$Kind DOC_COMMENT
fld public final static com.sun.source.doctree.DocTree$Kind DOC_ROOT
fld public final static com.sun.source.doctree.DocTree$Kind DOC_TYPE
fld public final static com.sun.source.doctree.DocTree$Kind END_ELEMENT
fld public final static com.sun.source.doctree.DocTree$Kind ENTITY
fld public final static com.sun.source.doctree.DocTree$Kind ERRONEOUS
fld public final static com.sun.source.doctree.DocTree$Kind ESCAPE
fld public final static com.sun.source.doctree.DocTree$Kind EXCEPTION
fld public final static com.sun.source.doctree.DocTree$Kind HIDDEN
fld public final static com.sun.source.doctree.DocTree$Kind IDENTIFIER
fld public final static com.sun.source.doctree.DocTree$Kind INDEX
fld public final static com.sun.source.doctree.DocTree$Kind INHERIT_DOC
fld public final static com.sun.source.doctree.DocTree$Kind LINK
fld public final static com.sun.source.doctree.DocTree$Kind LINK_PLAIN
fld public final static com.sun.source.doctree.DocTree$Kind LITERAL
fld public final static com.sun.source.doctree.DocTree$Kind MARKDOWN
fld public final static com.sun.source.doctree.DocTree$Kind OTHER
fld public final static com.sun.source.doctree.DocTree$Kind PARAM
fld public final static com.sun.source.doctree.DocTree$Kind PROVIDES
fld public final static com.sun.source.doctree.DocTree$Kind REFERENCE
fld public final static com.sun.source.doctree.DocTree$Kind RETURN
fld public final static com.sun.source.doctree.DocTree$Kind SEE
fld public final static com.sun.source.doctree.DocTree$Kind SERIAL
fld public final static com.sun.source.doctree.DocTree$Kind SERIAL_DATA
fld public final static com.sun.source.doctree.DocTree$Kind SERIAL_FIELD
fld public final static com.sun.source.doctree.DocTree$Kind SINCE
fld public final static com.sun.source.doctree.DocTree$Kind SNIPPET
fld public final static com.sun.source.doctree.DocTree$Kind SPEC
fld public final static com.sun.source.doctree.DocTree$Kind START_ELEMENT
fld public final static com.sun.source.doctree.DocTree$Kind SUMMARY
fld public final static com.sun.source.doctree.DocTree$Kind SYSTEM_PROPERTY
fld public final static com.sun.source.doctree.DocTree$Kind TEXT
fld public final static com.sun.source.doctree.DocTree$Kind THROWS
fld public final static com.sun.source.doctree.DocTree$Kind UNKNOWN_BLOCK_TAG
fld public final static com.sun.source.doctree.DocTree$Kind UNKNOWN_INLINE_TAG
fld public final static com.sun.source.doctree.DocTree$Kind USES
fld public final static com.sun.source.doctree.DocTree$Kind VALUE
fld public final static com.sun.source.doctree.DocTree$Kind VERSION
meth public static com.sun.source.doctree.DocTree$Kind valueOf(java.lang.String)
meth public static com.sun.source.doctree.DocTree$Kind[] values()
supr java.lang.Enum<com.sun.source.doctree.DocTree$Kind>

CLSS public abstract interface com.sun.source.doctree.DocTreeVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitAttribute(com.sun.source.doctree.AttributeTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitAuthor(com.sun.source.doctree.AuthorTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitComment(com.sun.source.doctree.CommentTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitDeprecated(com.sun.source.doctree.DeprecatedTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitDocComment(com.sun.source.doctree.DocCommentTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitDocRoot(com.sun.source.doctree.DocRootTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitEndElement(com.sun.source.doctree.EndElementTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitEntity(com.sun.source.doctree.EntityTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitErroneous(com.sun.source.doctree.ErroneousTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitIdentifier(com.sun.source.doctree.IdentifierTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitInheritDoc(com.sun.source.doctree.InheritDocTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitLink(com.sun.source.doctree.LinkTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitLiteral(com.sun.source.doctree.LiteralTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitOther(com.sun.source.doctree.DocTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitParam(com.sun.source.doctree.ParamTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitReference(com.sun.source.doctree.ReferenceTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitReturn(com.sun.source.doctree.ReturnTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitSee(com.sun.source.doctree.SeeTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitSerial(com.sun.source.doctree.SerialTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitSerialData(com.sun.source.doctree.SerialDataTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitSerialField(com.sun.source.doctree.SerialFieldTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitSince(com.sun.source.doctree.SinceTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitStartElement(com.sun.source.doctree.StartElementTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitText(com.sun.source.doctree.TextTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitThrows(com.sun.source.doctree.ThrowsTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitUnknownBlockTag(com.sun.source.doctree.UnknownBlockTagTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitUnknownInlineTag(com.sun.source.doctree.UnknownInlineTagTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitValue(com.sun.source.doctree.ValueTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public abstract {com.sun.source.doctree.DocTreeVisitor%0} visitVersion(com.sun.source.doctree.VersionTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitDocType(com.sun.source.doctree.DocTypeTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitEscape(com.sun.source.doctree.EscapeTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitHidden(com.sun.source.doctree.HiddenTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitIndex(com.sun.source.doctree.IndexTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitProvides(com.sun.source.doctree.ProvidesTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitRawText(com.sun.source.doctree.RawTextTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitSnippet(com.sun.source.doctree.SnippetTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitSpec(com.sun.source.doctree.SpecTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitSummary(com.sun.source.doctree.SummaryTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitSystemProperty(com.sun.source.doctree.SystemPropertyTree,{com.sun.source.doctree.DocTreeVisitor%1})
meth public {com.sun.source.doctree.DocTreeVisitor%0} visitUses(com.sun.source.doctree.UsesTree,{com.sun.source.doctree.DocTreeVisitor%1})

CLSS public abstract interface com.sun.source.doctree.DocTypeTree
intf com.sun.source.doctree.DocTree
meth public abstract java.lang.String getText()

CLSS public abstract interface com.sun.source.doctree.EndElementTree
intf com.sun.source.doctree.DocTree
meth public abstract javax.lang.model.element.Name getName()

CLSS public abstract interface com.sun.source.doctree.EntityTree
intf com.sun.source.doctree.DocTree
meth public abstract javax.lang.model.element.Name getName()

CLSS public abstract interface com.sun.source.doctree.ErroneousTree
intf com.sun.source.doctree.TextTree
meth public abstract javax.tools.Diagnostic<javax.tools.JavaFileObject> getDiagnostic()

CLSS public abstract interface com.sun.source.doctree.EscapeTree
intf com.sun.source.doctree.TextTree
meth public abstract java.lang.String getBody()

CLSS public abstract interface com.sun.source.doctree.HiddenTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getBody()

CLSS public abstract interface com.sun.source.doctree.IdentifierTree
intf com.sun.source.doctree.DocTree
meth public abstract javax.lang.model.element.Name getName()

CLSS public abstract interface com.sun.source.doctree.IndexTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract com.sun.source.doctree.DocTree getSearchTerm()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()

CLSS public abstract interface com.sun.source.doctree.InheritDocTree
intf com.sun.source.doctree.InlineTagTree
meth public com.sun.source.doctree.ReferenceTree getSupertype()

CLSS public abstract interface com.sun.source.doctree.InlineTagTree
intf com.sun.source.doctree.DocTree
meth public abstract java.lang.String getTagName()

CLSS public abstract interface com.sun.source.doctree.LinkTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract com.sun.source.doctree.ReferenceTree getReference()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getLabel()

CLSS public abstract interface com.sun.source.doctree.LiteralTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract com.sun.source.doctree.TextTree getBody()

CLSS public abstract interface com.sun.source.doctree.ParamTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract boolean isTypeParameter()
meth public abstract com.sun.source.doctree.IdentifierTree getName()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()

CLSS public abstract interface com.sun.source.doctree.ProvidesTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract com.sun.source.doctree.ReferenceTree getServiceType()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()

CLSS public abstract interface com.sun.source.doctree.RawTextTree
intf com.sun.source.doctree.DocTree
meth public abstract java.lang.String getContent()

CLSS public abstract interface com.sun.source.doctree.ReferenceTree
intf com.sun.source.doctree.DocTree
meth public abstract java.lang.String getSignature()

CLSS public abstract interface com.sun.source.doctree.ReturnTree
intf com.sun.source.doctree.BlockTagTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()
meth public boolean isInline()

CLSS public abstract interface com.sun.source.doctree.SeeTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getReference()

CLSS public abstract interface com.sun.source.doctree.SerialDataTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()

CLSS public abstract interface com.sun.source.doctree.SerialFieldTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract com.sun.source.doctree.IdentifierTree getName()
meth public abstract com.sun.source.doctree.ReferenceTree getType()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()

CLSS public abstract interface com.sun.source.doctree.SerialTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()

CLSS public abstract interface com.sun.source.doctree.SinceTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getBody()

CLSS public abstract interface com.sun.source.doctree.SnippetTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract com.sun.source.doctree.TextTree getBody()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getAttributes()

CLSS public abstract interface com.sun.source.doctree.SpecTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract com.sun.source.doctree.TextTree getURL()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getTitle()

CLSS public abstract interface com.sun.source.doctree.StartElementTree
intf com.sun.source.doctree.DocTree
meth public abstract boolean isSelfClosing()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getAttributes()
meth public abstract javax.lang.model.element.Name getName()

CLSS public abstract interface com.sun.source.doctree.SummaryTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getSummary()

CLSS public abstract interface com.sun.source.doctree.SystemPropertyTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract javax.lang.model.element.Name getPropertyName()

CLSS public abstract interface com.sun.source.doctree.TextTree
intf com.sun.source.doctree.DocTree
meth public abstract java.lang.String getBody()

CLSS public abstract interface com.sun.source.doctree.ThrowsTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract com.sun.source.doctree.ReferenceTree getExceptionName()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()

CLSS public abstract interface com.sun.source.doctree.UnknownBlockTagTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getContent()

CLSS public abstract interface com.sun.source.doctree.UnknownInlineTagTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getContent()

CLSS public abstract interface com.sun.source.doctree.UsesTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract com.sun.source.doctree.ReferenceTree getServiceType()
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getDescription()

CLSS public abstract interface com.sun.source.doctree.ValueTree
intf com.sun.source.doctree.InlineTagTree
meth public abstract com.sun.source.doctree.ReferenceTree getReference()
meth public com.sun.source.doctree.TextTree getFormat()

CLSS public abstract interface com.sun.source.doctree.VersionTree
intf com.sun.source.doctree.BlockTagTree
meth public abstract java.util.List<? extends com.sun.source.doctree.DocTree> getBody()

CLSS public abstract interface com.sun.source.tree.AnnotatedTypeTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getUnderlyingType()
meth public abstract java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations()

CLSS public abstract interface com.sun.source.tree.AnnotationTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.Tree getAnnotationType()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getArguments()

CLSS public abstract interface com.sun.source.tree.AnyPatternTree
intf com.sun.source.tree.PatternTree

CLSS public abstract interface com.sun.source.tree.ArrayAccessTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract com.sun.source.tree.ExpressionTree getIndex()

CLSS public abstract interface com.sun.source.tree.ArrayTypeTree
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.Tree getType()

CLSS public abstract interface com.sun.source.tree.AssertTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getCondition()
meth public abstract com.sun.source.tree.ExpressionTree getDetail()

CLSS public abstract interface com.sun.source.tree.AssignmentTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract com.sun.source.tree.ExpressionTree getVariable()

CLSS public abstract interface com.sun.source.tree.BinaryTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getLeftOperand()
meth public abstract com.sun.source.tree.ExpressionTree getRightOperand()

CLSS public abstract interface com.sun.source.tree.BindingPatternTree
intf com.sun.source.tree.PatternTree
meth public abstract com.sun.source.tree.VariableTree getVariable()

CLSS public abstract interface com.sun.source.tree.BlockTree
intf com.sun.source.tree.StatementTree
meth public abstract boolean isStatic()
meth public abstract java.util.List<? extends com.sun.source.tree.StatementTree> getStatements()

CLSS public abstract interface com.sun.source.tree.BreakTree
intf com.sun.source.tree.StatementTree
meth public abstract javax.lang.model.element.Name getLabel()

CLSS public abstract interface com.sun.source.tree.CaseLabelTree
intf com.sun.source.tree.Tree

CLSS public abstract interface com.sun.source.tree.CaseTree
innr public final static !enum CaseKind
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
 anno 0 java.lang.Deprecated()
meth public abstract com.sun.source.tree.ExpressionTree getGuard()
meth public abstract java.util.List<? extends com.sun.source.tree.CaseLabelTree> getLabels()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getExpressions()
meth public abstract java.util.List<? extends com.sun.source.tree.StatementTree> getStatements()
meth public com.sun.source.tree.CaseTree$CaseKind getCaseKind()
meth public com.sun.source.tree.Tree getBody()

CLSS public final static !enum com.sun.source.tree.CaseTree$CaseKind
 outer com.sun.source.tree.CaseTree
fld public final static com.sun.source.tree.CaseTree$CaseKind RULE
fld public final static com.sun.source.tree.CaseTree$CaseKind STATEMENT
meth public static com.sun.source.tree.CaseTree$CaseKind valueOf(java.lang.String)
meth public static com.sun.source.tree.CaseTree$CaseKind[] values()
supr java.lang.Enum<com.sun.source.tree.CaseTree$CaseKind>

CLSS public abstract interface com.sun.source.tree.CatchTree
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.BlockTree getBlock()
meth public abstract com.sun.source.tree.VariableTree getParameter()

CLSS public abstract interface com.sun.source.tree.ClassTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ModifiersTree getModifiers()
meth public abstract com.sun.source.tree.Tree getExtendsClause()
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getImplementsClause()
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getMembers()
meth public abstract java.util.List<? extends com.sun.source.tree.TypeParameterTree> getTypeParameters()
meth public abstract javax.lang.model.element.Name getSimpleName()
meth public java.util.List<? extends com.sun.source.tree.Tree> getPermitsClause()

CLSS public abstract interface com.sun.source.tree.CompilationUnitTree
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.ExpressionTree getPackageName()
meth public abstract com.sun.source.tree.LineMap getLineMap()
meth public abstract com.sun.source.tree.PackageTree getPackage()
meth public abstract java.util.List<? extends com.sun.source.tree.AnnotationTree> getPackageAnnotations()
meth public abstract java.util.List<? extends com.sun.source.tree.ImportTree> getImports()
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getTypeDecls()
meth public abstract javax.tools.JavaFileObject getSourceFile()
meth public com.sun.source.tree.ModuleTree getModule()

CLSS public abstract interface com.sun.source.tree.CompoundAssignmentTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract com.sun.source.tree.ExpressionTree getVariable()

CLSS public abstract interface com.sun.source.tree.ConditionalExpressionTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getCondition()
meth public abstract com.sun.source.tree.ExpressionTree getFalseExpression()
meth public abstract com.sun.source.tree.ExpressionTree getTrueExpression()

CLSS public abstract interface com.sun.source.tree.ConstantCaseLabelTree
intf com.sun.source.tree.CaseLabelTree
meth public abstract com.sun.source.tree.ExpressionTree getConstantExpression()

CLSS public abstract interface com.sun.source.tree.ContinueTree
intf com.sun.source.tree.StatementTree
meth public abstract javax.lang.model.element.Name getLabel()

CLSS public abstract interface com.sun.source.tree.DeconstructionPatternTree
intf com.sun.source.tree.PatternTree
meth public abstract com.sun.source.tree.ExpressionTree getDeconstructor()
meth public abstract java.util.List<? extends com.sun.source.tree.PatternTree> getNestedPatterns()

CLSS public abstract interface com.sun.source.tree.DefaultCaseLabelTree
intf com.sun.source.tree.CaseLabelTree

CLSS public abstract interface com.sun.source.tree.DirectiveTree
intf com.sun.source.tree.Tree

CLSS public abstract interface com.sun.source.tree.DoWhileLoopTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getCondition()
meth public abstract com.sun.source.tree.StatementTree getStatement()

CLSS public abstract interface com.sun.source.tree.EmptyStatementTree
intf com.sun.source.tree.StatementTree

CLSS public abstract interface com.sun.source.tree.EnhancedForLoopTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract com.sun.source.tree.StatementTree getStatement()
meth public abstract com.sun.source.tree.VariableTree getVariable()

CLSS public abstract interface com.sun.source.tree.ErroneousTree
intf com.sun.source.tree.ExpressionTree
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getErrorTrees()

CLSS public abstract interface com.sun.source.tree.ExportsTree
intf com.sun.source.tree.DirectiveTree
meth public abstract com.sun.source.tree.ExpressionTree getPackageName()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getModuleNames()

CLSS public abstract interface com.sun.source.tree.ExpressionStatementTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()

CLSS public abstract interface com.sun.source.tree.ExpressionTree
intf com.sun.source.tree.Tree

CLSS public abstract interface com.sun.source.tree.ForLoopTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getCondition()
meth public abstract com.sun.source.tree.StatementTree getStatement()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionStatementTree> getUpdate()
meth public abstract java.util.List<? extends com.sun.source.tree.StatementTree> getInitializer()

CLSS public abstract interface com.sun.source.tree.IdentifierTree
intf com.sun.source.tree.ExpressionTree
meth public abstract javax.lang.model.element.Name getName()

CLSS public abstract interface com.sun.source.tree.IfTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getCondition()
meth public abstract com.sun.source.tree.StatementTree getElseStatement()
meth public abstract com.sun.source.tree.StatementTree getThenStatement()

CLSS public abstract interface com.sun.source.tree.ImportTree
intf com.sun.source.tree.Tree
meth public abstract boolean isModule()
meth public abstract boolean isStatic()
meth public abstract com.sun.source.tree.Tree getQualifiedIdentifier()

CLSS public abstract interface com.sun.source.tree.InstanceOfTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract com.sun.source.tree.PatternTree getPattern()
meth public abstract com.sun.source.tree.Tree getType()

CLSS public abstract interface com.sun.source.tree.IntersectionTypeTree
intf com.sun.source.tree.Tree
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getBounds()

CLSS public abstract interface com.sun.source.tree.LabeledStatementTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.StatementTree getStatement()
meth public abstract javax.lang.model.element.Name getLabel()

CLSS public abstract interface com.sun.source.tree.LambdaExpressionTree
innr public final static !enum BodyKind
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.LambdaExpressionTree$BodyKind getBodyKind()
meth public abstract com.sun.source.tree.Tree getBody()
meth public abstract java.util.List<? extends com.sun.source.tree.VariableTree> getParameters()

CLSS public final static !enum com.sun.source.tree.LambdaExpressionTree$BodyKind
 outer com.sun.source.tree.LambdaExpressionTree
fld public final static com.sun.source.tree.LambdaExpressionTree$BodyKind EXPRESSION
fld public final static com.sun.source.tree.LambdaExpressionTree$BodyKind STATEMENT
meth public static com.sun.source.tree.LambdaExpressionTree$BodyKind valueOf(java.lang.String)
meth public static com.sun.source.tree.LambdaExpressionTree$BodyKind[] values()
supr java.lang.Enum<com.sun.source.tree.LambdaExpressionTree$BodyKind>

CLSS public abstract interface com.sun.source.tree.LineMap
meth public abstract long getColumnNumber(long)
meth public abstract long getLineNumber(long)
meth public abstract long getPosition(long,long)
meth public abstract long getStartPosition(long)

CLSS public abstract interface com.sun.source.tree.LiteralTree
intf com.sun.source.tree.ExpressionTree
meth public abstract java.lang.Object getValue()

CLSS public abstract interface com.sun.source.tree.MemberReferenceTree
innr public final static !enum ReferenceMode
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getQualifierExpression()
meth public abstract com.sun.source.tree.MemberReferenceTree$ReferenceMode getMode()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getTypeArguments()
meth public abstract javax.lang.model.element.Name getName()

CLSS public final static !enum com.sun.source.tree.MemberReferenceTree$ReferenceMode
 outer com.sun.source.tree.MemberReferenceTree
fld public final static com.sun.source.tree.MemberReferenceTree$ReferenceMode INVOKE
fld public final static com.sun.source.tree.MemberReferenceTree$ReferenceMode NEW
meth public static com.sun.source.tree.MemberReferenceTree$ReferenceMode valueOf(java.lang.String)
meth public static com.sun.source.tree.MemberReferenceTree$ReferenceMode[] values()
supr java.lang.Enum<com.sun.source.tree.MemberReferenceTree$ReferenceMode>

CLSS public abstract interface com.sun.source.tree.MemberSelectTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract javax.lang.model.element.Name getIdentifier()

CLSS public abstract interface com.sun.source.tree.MethodInvocationTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getMethodSelect()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getArguments()
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getTypeArguments()

CLSS public abstract interface com.sun.source.tree.MethodTree
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.BlockTree getBody()
meth public abstract com.sun.source.tree.ModifiersTree getModifiers()
meth public abstract com.sun.source.tree.Tree getDefaultValue()
meth public abstract com.sun.source.tree.Tree getReturnType()
meth public abstract com.sun.source.tree.VariableTree getReceiverParameter()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getThrows()
meth public abstract java.util.List<? extends com.sun.source.tree.TypeParameterTree> getTypeParameters()
meth public abstract java.util.List<? extends com.sun.source.tree.VariableTree> getParameters()
meth public abstract javax.lang.model.element.Name getName()

CLSS public abstract interface com.sun.source.tree.ModifiersTree
intf com.sun.source.tree.Tree
meth public abstract java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations()
meth public abstract java.util.Set<javax.lang.model.element.Modifier> getFlags()

CLSS public abstract interface com.sun.source.tree.ModuleTree
innr public final static !enum ModuleKind
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.ExpressionTree getName()
meth public abstract com.sun.source.tree.ModuleTree$ModuleKind getModuleType()
meth public abstract java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations()
meth public abstract java.util.List<? extends com.sun.source.tree.DirectiveTree> getDirectives()

CLSS public final static !enum com.sun.source.tree.ModuleTree$ModuleKind
 outer com.sun.source.tree.ModuleTree
fld public final static com.sun.source.tree.ModuleTree$ModuleKind OPEN
fld public final static com.sun.source.tree.ModuleTree$ModuleKind STRONG
meth public static com.sun.source.tree.ModuleTree$ModuleKind valueOf(java.lang.String)
meth public static com.sun.source.tree.ModuleTree$ModuleKind[] values()
supr java.lang.Enum<com.sun.source.tree.ModuleTree$ModuleKind>

CLSS public abstract interface com.sun.source.tree.NewArrayTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.Tree getType()
meth public abstract java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getDimensions()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getInitializers()
meth public abstract java.util.List<? extends java.util.List<? extends com.sun.source.tree.AnnotationTree>> getDimAnnotations()

CLSS public abstract interface com.sun.source.tree.NewClassTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ClassTree getClassBody()
meth public abstract com.sun.source.tree.ExpressionTree getEnclosingExpression()
meth public abstract com.sun.source.tree.ExpressionTree getIdentifier()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getArguments()
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getTypeArguments()

CLSS public abstract interface com.sun.source.tree.OpensTree
intf com.sun.source.tree.DirectiveTree
meth public abstract com.sun.source.tree.ExpressionTree getPackageName()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getModuleNames()

CLSS public abstract interface com.sun.source.tree.PackageTree
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.ExpressionTree getPackageName()
meth public abstract java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations()

CLSS public abstract interface com.sun.source.tree.ParameterizedTypeTree
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.Tree getType()
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getTypeArguments()

CLSS public abstract interface com.sun.source.tree.ParenthesizedTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()

CLSS public abstract interface com.sun.source.tree.PatternCaseLabelTree
intf com.sun.source.tree.CaseLabelTree
meth public abstract com.sun.source.tree.PatternTree getPattern()

CLSS public abstract interface com.sun.source.tree.PatternTree
intf com.sun.source.tree.Tree

CLSS public abstract interface com.sun.source.tree.PrimitiveTypeTree
intf com.sun.source.tree.Tree
meth public abstract javax.lang.model.type.TypeKind getPrimitiveTypeKind()

CLSS public abstract interface com.sun.source.tree.ProvidesTree
intf com.sun.source.tree.DirectiveTree
meth public abstract com.sun.source.tree.ExpressionTree getServiceName()
meth public abstract java.util.List<? extends com.sun.source.tree.ExpressionTree> getImplementationNames()

CLSS public abstract interface com.sun.source.tree.RequiresTree
intf com.sun.source.tree.DirectiveTree
meth public abstract boolean isStatic()
meth public abstract boolean isTransitive()
meth public abstract com.sun.source.tree.ExpressionTree getModuleName()

CLSS public abstract interface com.sun.source.tree.ReturnTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()

CLSS public abstract interface com.sun.source.tree.Scope
meth public abstract com.sun.source.tree.Scope getEnclosingScope()
meth public abstract java.lang.Iterable<? extends javax.lang.model.element.Element> getLocalElements()
meth public abstract javax.lang.model.element.ExecutableElement getEnclosingMethod()
meth public abstract javax.lang.model.element.TypeElement getEnclosingClass()

CLSS public abstract interface com.sun.source.tree.StatementTree
intf com.sun.source.tree.Tree

CLSS public abstract interface com.sun.source.tree.SwitchExpressionTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract java.util.List<? extends com.sun.source.tree.CaseTree> getCases()

CLSS public abstract interface com.sun.source.tree.SwitchTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract java.util.List<? extends com.sun.source.tree.CaseTree> getCases()

CLSS public abstract interface com.sun.source.tree.SynchronizedTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.BlockTree getBlock()
meth public abstract com.sun.source.tree.ExpressionTree getExpression()

CLSS public abstract interface com.sun.source.tree.ThrowTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()

CLSS public abstract interface com.sun.source.tree.Tree
innr public final static !enum Kind
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(com.sun.source.tree.TreeVisitor<{%%0},{%%1}>,{%%1})
meth public abstract com.sun.source.tree.Tree$Kind getKind()

CLSS public final static !enum com.sun.source.tree.Tree$Kind
 outer com.sun.source.tree.Tree
fld public final static com.sun.source.tree.Tree$Kind AND
fld public final static com.sun.source.tree.Tree$Kind AND_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind ANNOTATED_TYPE
fld public final static com.sun.source.tree.Tree$Kind ANNOTATION
fld public final static com.sun.source.tree.Tree$Kind ANNOTATION_TYPE
fld public final static com.sun.source.tree.Tree$Kind ANY_PATTERN
fld public final static com.sun.source.tree.Tree$Kind ARRAY_ACCESS
fld public final static com.sun.source.tree.Tree$Kind ARRAY_TYPE
fld public final static com.sun.source.tree.Tree$Kind ASSERT
fld public final static com.sun.source.tree.Tree$Kind ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind BINDING_PATTERN
fld public final static com.sun.source.tree.Tree$Kind BITWISE_COMPLEMENT
fld public final static com.sun.source.tree.Tree$Kind BLOCK
fld public final static com.sun.source.tree.Tree$Kind BOOLEAN_LITERAL
fld public final static com.sun.source.tree.Tree$Kind BREAK
fld public final static com.sun.source.tree.Tree$Kind CASE
fld public final static com.sun.source.tree.Tree$Kind CATCH
fld public final static com.sun.source.tree.Tree$Kind CHAR_LITERAL
fld public final static com.sun.source.tree.Tree$Kind CLASS
fld public final static com.sun.source.tree.Tree$Kind COMPILATION_UNIT
fld public final static com.sun.source.tree.Tree$Kind CONDITIONAL_AND
fld public final static com.sun.source.tree.Tree$Kind CONDITIONAL_EXPRESSION
fld public final static com.sun.source.tree.Tree$Kind CONDITIONAL_OR
fld public final static com.sun.source.tree.Tree$Kind CONSTANT_CASE_LABEL
fld public final static com.sun.source.tree.Tree$Kind CONTINUE
fld public final static com.sun.source.tree.Tree$Kind DECONSTRUCTION_PATTERN
fld public final static com.sun.source.tree.Tree$Kind DEFAULT_CASE_LABEL
fld public final static com.sun.source.tree.Tree$Kind DIVIDE
fld public final static com.sun.source.tree.Tree$Kind DIVIDE_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind DOUBLE_LITERAL
fld public final static com.sun.source.tree.Tree$Kind DO_WHILE_LOOP
fld public final static com.sun.source.tree.Tree$Kind EMPTY_STATEMENT
fld public final static com.sun.source.tree.Tree$Kind ENHANCED_FOR_LOOP
fld public final static com.sun.source.tree.Tree$Kind ENUM
fld public final static com.sun.source.tree.Tree$Kind EQUAL_TO
fld public final static com.sun.source.tree.Tree$Kind ERRONEOUS
fld public final static com.sun.source.tree.Tree$Kind EXPORTS
fld public final static com.sun.source.tree.Tree$Kind EXPRESSION_STATEMENT
fld public final static com.sun.source.tree.Tree$Kind EXTENDS_WILDCARD
fld public final static com.sun.source.tree.Tree$Kind FLOAT_LITERAL
fld public final static com.sun.source.tree.Tree$Kind FOR_LOOP
fld public final static com.sun.source.tree.Tree$Kind GREATER_THAN
fld public final static com.sun.source.tree.Tree$Kind GREATER_THAN_EQUAL
fld public final static com.sun.source.tree.Tree$Kind IDENTIFIER
fld public final static com.sun.source.tree.Tree$Kind IF
fld public final static com.sun.source.tree.Tree$Kind IMPORT
fld public final static com.sun.source.tree.Tree$Kind INSTANCE_OF
fld public final static com.sun.source.tree.Tree$Kind INTERFACE
fld public final static com.sun.source.tree.Tree$Kind INTERSECTION_TYPE
fld public final static com.sun.source.tree.Tree$Kind INT_LITERAL
fld public final static com.sun.source.tree.Tree$Kind LABELED_STATEMENT
fld public final static com.sun.source.tree.Tree$Kind LAMBDA_EXPRESSION
fld public final static com.sun.source.tree.Tree$Kind LEFT_SHIFT
fld public final static com.sun.source.tree.Tree$Kind LEFT_SHIFT_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind LESS_THAN
fld public final static com.sun.source.tree.Tree$Kind LESS_THAN_EQUAL
fld public final static com.sun.source.tree.Tree$Kind LOGICAL_COMPLEMENT
fld public final static com.sun.source.tree.Tree$Kind LONG_LITERAL
fld public final static com.sun.source.tree.Tree$Kind MEMBER_REFERENCE
fld public final static com.sun.source.tree.Tree$Kind MEMBER_SELECT
fld public final static com.sun.source.tree.Tree$Kind METHOD
fld public final static com.sun.source.tree.Tree$Kind METHOD_INVOCATION
fld public final static com.sun.source.tree.Tree$Kind MINUS
fld public final static com.sun.source.tree.Tree$Kind MINUS_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind MODIFIERS
fld public final static com.sun.source.tree.Tree$Kind MODULE
fld public final static com.sun.source.tree.Tree$Kind MULTIPLY
fld public final static com.sun.source.tree.Tree$Kind MULTIPLY_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind NEW_ARRAY
fld public final static com.sun.source.tree.Tree$Kind NEW_CLASS
fld public final static com.sun.source.tree.Tree$Kind NOT_EQUAL_TO
fld public final static com.sun.source.tree.Tree$Kind NULL_LITERAL
fld public final static com.sun.source.tree.Tree$Kind OPENS
fld public final static com.sun.source.tree.Tree$Kind OR
fld public final static com.sun.source.tree.Tree$Kind OR_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind OTHER
fld public final static com.sun.source.tree.Tree$Kind PACKAGE
fld public final static com.sun.source.tree.Tree$Kind PARAMETERIZED_TYPE
fld public final static com.sun.source.tree.Tree$Kind PARENTHESIZED
fld public final static com.sun.source.tree.Tree$Kind PATTERN_CASE_LABEL
fld public final static com.sun.source.tree.Tree$Kind PLUS
fld public final static com.sun.source.tree.Tree$Kind PLUS_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind POSTFIX_DECREMENT
fld public final static com.sun.source.tree.Tree$Kind POSTFIX_INCREMENT
fld public final static com.sun.source.tree.Tree$Kind PREFIX_DECREMENT
fld public final static com.sun.source.tree.Tree$Kind PREFIX_INCREMENT
fld public final static com.sun.source.tree.Tree$Kind PRIMITIVE_TYPE
fld public final static com.sun.source.tree.Tree$Kind PROVIDES
fld public final static com.sun.source.tree.Tree$Kind RECORD
fld public final static com.sun.source.tree.Tree$Kind REMAINDER
fld public final static com.sun.source.tree.Tree$Kind REMAINDER_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind REQUIRES
fld public final static com.sun.source.tree.Tree$Kind RETURN
fld public final static com.sun.source.tree.Tree$Kind RIGHT_SHIFT
fld public final static com.sun.source.tree.Tree$Kind RIGHT_SHIFT_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind STRING_LITERAL
fld public final static com.sun.source.tree.Tree$Kind SUPER_WILDCARD
fld public final static com.sun.source.tree.Tree$Kind SWITCH
fld public final static com.sun.source.tree.Tree$Kind SWITCH_EXPRESSION
fld public final static com.sun.source.tree.Tree$Kind SYNCHRONIZED
fld public final static com.sun.source.tree.Tree$Kind THROW
fld public final static com.sun.source.tree.Tree$Kind TRY
fld public final static com.sun.source.tree.Tree$Kind TYPE_ANNOTATION
fld public final static com.sun.source.tree.Tree$Kind TYPE_CAST
fld public final static com.sun.source.tree.Tree$Kind TYPE_PARAMETER
fld public final static com.sun.source.tree.Tree$Kind UNARY_MINUS
fld public final static com.sun.source.tree.Tree$Kind UNARY_PLUS
fld public final static com.sun.source.tree.Tree$Kind UNBOUNDED_WILDCARD
fld public final static com.sun.source.tree.Tree$Kind UNION_TYPE
fld public final static com.sun.source.tree.Tree$Kind UNSIGNED_RIGHT_SHIFT
fld public final static com.sun.source.tree.Tree$Kind UNSIGNED_RIGHT_SHIFT_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind USES
fld public final static com.sun.source.tree.Tree$Kind VARIABLE
fld public final static com.sun.source.tree.Tree$Kind WHILE_LOOP
fld public final static com.sun.source.tree.Tree$Kind XOR
fld public final static com.sun.source.tree.Tree$Kind XOR_ASSIGNMENT
fld public final static com.sun.source.tree.Tree$Kind YIELD
meth public java.lang.Class<? extends com.sun.source.tree.Tree> asInterface()
meth public static com.sun.source.tree.Tree$Kind valueOf(java.lang.String)
meth public static com.sun.source.tree.Tree$Kind[] values()
supr java.lang.Enum<com.sun.source.tree.Tree$Kind>
hfds associatedInterface

CLSS public abstract interface com.sun.source.tree.TreeVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitAnnotatedType(com.sun.source.tree.AnnotatedTypeTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitAnnotation(com.sun.source.tree.AnnotationTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitAnyPattern(com.sun.source.tree.AnyPatternTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitArrayAccess(com.sun.source.tree.ArrayAccessTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitArrayType(com.sun.source.tree.ArrayTypeTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitAssert(com.sun.source.tree.AssertTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitAssignment(com.sun.source.tree.AssignmentTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitBinary(com.sun.source.tree.BinaryTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitBindingPattern(com.sun.source.tree.BindingPatternTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitBlock(com.sun.source.tree.BlockTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitBreak(com.sun.source.tree.BreakTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitCase(com.sun.source.tree.CaseTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitCatch(com.sun.source.tree.CatchTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitClass(com.sun.source.tree.ClassTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitCompilationUnit(com.sun.source.tree.CompilationUnitTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitCompoundAssignment(com.sun.source.tree.CompoundAssignmentTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitConditionalExpression(com.sun.source.tree.ConditionalExpressionTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitConstantCaseLabel(com.sun.source.tree.ConstantCaseLabelTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitContinue(com.sun.source.tree.ContinueTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitDeconstructionPattern(com.sun.source.tree.DeconstructionPatternTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitDefaultCaseLabel(com.sun.source.tree.DefaultCaseLabelTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitDoWhileLoop(com.sun.source.tree.DoWhileLoopTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitEmptyStatement(com.sun.source.tree.EmptyStatementTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitEnhancedForLoop(com.sun.source.tree.EnhancedForLoopTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitErroneous(com.sun.source.tree.ErroneousTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitExports(com.sun.source.tree.ExportsTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitExpressionStatement(com.sun.source.tree.ExpressionStatementTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitForLoop(com.sun.source.tree.ForLoopTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitIdentifier(com.sun.source.tree.IdentifierTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitIf(com.sun.source.tree.IfTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitImport(com.sun.source.tree.ImportTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitInstanceOf(com.sun.source.tree.InstanceOfTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitIntersectionType(com.sun.source.tree.IntersectionTypeTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitLabeledStatement(com.sun.source.tree.LabeledStatementTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitLambdaExpression(com.sun.source.tree.LambdaExpressionTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitLiteral(com.sun.source.tree.LiteralTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitMemberReference(com.sun.source.tree.MemberReferenceTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitMemberSelect(com.sun.source.tree.MemberSelectTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitMethod(com.sun.source.tree.MethodTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitMethodInvocation(com.sun.source.tree.MethodInvocationTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitModifiers(com.sun.source.tree.ModifiersTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitModule(com.sun.source.tree.ModuleTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitNewArray(com.sun.source.tree.NewArrayTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitNewClass(com.sun.source.tree.NewClassTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitOpens(com.sun.source.tree.OpensTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitOther(com.sun.source.tree.Tree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitPackage(com.sun.source.tree.PackageTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitParameterizedType(com.sun.source.tree.ParameterizedTypeTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitParenthesized(com.sun.source.tree.ParenthesizedTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitPatternCaseLabel(com.sun.source.tree.PatternCaseLabelTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitPrimitiveType(com.sun.source.tree.PrimitiveTypeTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitProvides(com.sun.source.tree.ProvidesTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitRequires(com.sun.source.tree.RequiresTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitReturn(com.sun.source.tree.ReturnTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitSwitch(com.sun.source.tree.SwitchTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitSwitchExpression(com.sun.source.tree.SwitchExpressionTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitSynchronized(com.sun.source.tree.SynchronizedTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitThrow(com.sun.source.tree.ThrowTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitTry(com.sun.source.tree.TryTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitTypeCast(com.sun.source.tree.TypeCastTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitTypeParameter(com.sun.source.tree.TypeParameterTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitUnary(com.sun.source.tree.UnaryTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitUnionType(com.sun.source.tree.UnionTypeTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitUses(com.sun.source.tree.UsesTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitVariable(com.sun.source.tree.VariableTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitWhileLoop(com.sun.source.tree.WhileLoopTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitWildcard(com.sun.source.tree.WildcardTree,{com.sun.source.tree.TreeVisitor%1})
meth public abstract {com.sun.source.tree.TreeVisitor%0} visitYield(com.sun.source.tree.YieldTree,{com.sun.source.tree.TreeVisitor%1})

CLSS public abstract interface com.sun.source.tree.TryTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.BlockTree getBlock()
meth public abstract com.sun.source.tree.BlockTree getFinallyBlock()
meth public abstract java.util.List<? extends com.sun.source.tree.CatchTree> getCatches()
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getResources()

CLSS public abstract interface com.sun.source.tree.TypeCastTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()
meth public abstract com.sun.source.tree.Tree getType()

CLSS public abstract interface com.sun.source.tree.TypeParameterTree
intf com.sun.source.tree.Tree
meth public abstract java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations()
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getBounds()
meth public abstract javax.lang.model.element.Name getName()

CLSS public abstract interface com.sun.source.tree.UnaryTree
intf com.sun.source.tree.ExpressionTree
meth public abstract com.sun.source.tree.ExpressionTree getExpression()

CLSS public abstract interface com.sun.source.tree.UnionTypeTree
intf com.sun.source.tree.Tree
meth public abstract java.util.List<? extends com.sun.source.tree.Tree> getTypeAlternatives()

CLSS public abstract interface com.sun.source.tree.UsesTree
intf com.sun.source.tree.DirectiveTree
meth public abstract com.sun.source.tree.ExpressionTree getServiceName()

CLSS public abstract interface com.sun.source.tree.VariableTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getInitializer()
meth public abstract com.sun.source.tree.ExpressionTree getNameExpression()
meth public abstract com.sun.source.tree.ModifiersTree getModifiers()
meth public abstract com.sun.source.tree.Tree getType()
meth public abstract javax.lang.model.element.Name getName()

CLSS public abstract interface com.sun.source.tree.WhileLoopTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getCondition()
meth public abstract com.sun.source.tree.StatementTree getStatement()

CLSS public abstract interface com.sun.source.tree.WildcardTree
intf com.sun.source.tree.Tree
meth public abstract com.sun.source.tree.Tree getBound()

CLSS public abstract interface com.sun.source.tree.YieldTree
intf com.sun.source.tree.StatementTree
meth public abstract com.sun.source.tree.ExpressionTree getValue()

CLSS public abstract interface com.sun.source.util.DocSourcePositions
intf com.sun.source.util.SourcePositions
meth public abstract long getEndPosition(com.sun.source.tree.CompilationUnitTree,com.sun.source.doctree.DocCommentTree,com.sun.source.doctree.DocTree)
meth public abstract long getStartPosition(com.sun.source.tree.CompilationUnitTree,com.sun.source.doctree.DocCommentTree,com.sun.source.doctree.DocTree)

CLSS public abstract interface com.sun.source.util.DocTreeFactory
meth public abstract com.sun.source.doctree.AttributeTree newAttributeTree(javax.lang.model.element.Name,com.sun.source.doctree.AttributeTree$ValueKind,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.AuthorTree newAuthorTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.CommentTree newCommentTree(java.lang.String)
meth public abstract com.sun.source.doctree.DeprecatedTree newDeprecatedTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.DocCommentTree newDocCommentTree(java.util.List<? extends com.sun.source.doctree.DocTree>,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.DocCommentTree newDocCommentTree(java.util.List<? extends com.sun.source.doctree.DocTree>,java.util.List<? extends com.sun.source.doctree.DocTree>,java.util.List<? extends com.sun.source.doctree.DocTree>,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.DocRootTree newDocRootTree()
meth public abstract com.sun.source.doctree.DocTypeTree newDocTypeTree(java.lang.String)
meth public abstract com.sun.source.doctree.EndElementTree newEndElementTree(javax.lang.model.element.Name)
meth public abstract com.sun.source.doctree.EntityTree newEntityTree(javax.lang.model.element.Name)
meth public abstract com.sun.source.doctree.ErroneousTree newErroneousTree(java.lang.String,javax.tools.Diagnostic<javax.tools.JavaFileObject>)
meth public abstract com.sun.source.doctree.EscapeTree newEscapeTree(char)
meth public abstract com.sun.source.doctree.HiddenTree newHiddenTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.IdentifierTree newIdentifierTree(javax.lang.model.element.Name)
meth public abstract com.sun.source.doctree.IndexTree newIndexTree(com.sun.source.doctree.DocTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.InheritDocTree newInheritDocTree()
meth public abstract com.sun.source.doctree.LinkTree newLinkPlainTree(com.sun.source.doctree.ReferenceTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.LinkTree newLinkTree(com.sun.source.doctree.ReferenceTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.LiteralTree newCodeTree(com.sun.source.doctree.TextTree)
meth public abstract com.sun.source.doctree.LiteralTree newLiteralTree(com.sun.source.doctree.TextTree)
meth public abstract com.sun.source.doctree.ParamTree newParamTree(boolean,com.sun.source.doctree.IdentifierTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.ProvidesTree newProvidesTree(com.sun.source.doctree.ReferenceTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.RawTextTree newRawTextTree(com.sun.source.doctree.DocTree$Kind,java.lang.String)
meth public abstract com.sun.source.doctree.ReferenceTree newReferenceTree(java.lang.String)
meth public abstract com.sun.source.doctree.ReturnTree newReturnTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.SeeTree newSeeTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.SerialDataTree newSerialDataTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.SerialFieldTree newSerialFieldTree(com.sun.source.doctree.IdentifierTree,com.sun.source.doctree.ReferenceTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.SerialTree newSerialTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.SinceTree newSinceTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.SnippetTree newSnippetTree(java.util.List<? extends com.sun.source.doctree.DocTree>,com.sun.source.doctree.TextTree)
meth public abstract com.sun.source.doctree.SpecTree newSpecTree(com.sun.source.doctree.TextTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.StartElementTree newStartElementTree(javax.lang.model.element.Name,java.util.List<? extends com.sun.source.doctree.DocTree>,boolean)
meth public abstract com.sun.source.doctree.SystemPropertyTree newSystemPropertyTree(javax.lang.model.element.Name)
meth public abstract com.sun.source.doctree.TextTree newTextTree(java.lang.String)
meth public abstract com.sun.source.doctree.ThrowsTree newExceptionTree(com.sun.source.doctree.ReferenceTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.ThrowsTree newThrowsTree(com.sun.source.doctree.ReferenceTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.UnknownBlockTagTree newUnknownBlockTagTree(javax.lang.model.element.Name,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.UnknownInlineTagTree newUnknownInlineTagTree(javax.lang.model.element.Name,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.UsesTree newUsesTree(com.sun.source.doctree.ReferenceTree,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.doctree.ValueTree newValueTree(com.sun.source.doctree.ReferenceTree)
meth public abstract com.sun.source.doctree.VersionTree newVersionTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract com.sun.source.util.DocTreeFactory at(int)
meth public abstract java.util.List<com.sun.source.doctree.DocTree> getFirstSentence(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public com.sun.source.doctree.InheritDocTree newInheritDocTree(com.sun.source.doctree.ReferenceTree)
meth public com.sun.source.doctree.ReturnTree newReturnTree(boolean,java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public com.sun.source.doctree.SummaryTree newSummaryTree(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public com.sun.source.doctree.ValueTree newValueTree(com.sun.source.doctree.TextTree,com.sun.source.doctree.ReferenceTree)

CLSS public com.sun.source.util.DocTreePath
cons public init(com.sun.source.util.DocTreePath,com.sun.source.doctree.DocTree)
cons public init(com.sun.source.util.TreePath,com.sun.source.doctree.DocCommentTree)
intf java.lang.Iterable<com.sun.source.doctree.DocTree>
meth public com.sun.source.doctree.DocCommentTree getDocComment()
meth public com.sun.source.doctree.DocTree getLeaf()
meth public com.sun.source.util.DocTreePath getParentPath()
meth public com.sun.source.util.TreePath getTreePath()
meth public java.util.Iterator<com.sun.source.doctree.DocTree> iterator()
meth public static com.sun.source.util.DocTreePath getPath(com.sun.source.util.DocTreePath,com.sun.source.doctree.DocTree)
meth public static com.sun.source.util.DocTreePath getPath(com.sun.source.util.TreePath,com.sun.source.doctree.DocCommentTree,com.sun.source.doctree.DocTree)
supr java.lang.Object
hfds docComment,leaf,parent,treePath

CLSS public com.sun.source.util.DocTreePathScanner<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public com.sun.source.util.DocTreePath getCurrentPath()
meth public {com.sun.source.util.DocTreePathScanner%0} scan(com.sun.source.doctree.DocTree,{com.sun.source.util.DocTreePathScanner%1})
meth public {com.sun.source.util.DocTreePathScanner%0} scan(com.sun.source.util.DocTreePath,{com.sun.source.util.DocTreePathScanner%1})
supr com.sun.source.util.DocTreeScanner<{com.sun.source.util.DocTreePathScanner%0},{com.sun.source.util.DocTreePathScanner%1}>
hfds path

CLSS public com.sun.source.util.DocTreeScanner<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
intf com.sun.source.doctree.DocTreeVisitor<{com.sun.source.util.DocTreeScanner%0},{com.sun.source.util.DocTreeScanner%1}>
meth public {com.sun.source.util.DocTreeScanner%0} reduce({com.sun.source.util.DocTreeScanner%0},{com.sun.source.util.DocTreeScanner%0})
meth public {com.sun.source.util.DocTreeScanner%0} scan(com.sun.source.doctree.DocTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} scan(java.lang.Iterable<? extends com.sun.source.doctree.DocTree>,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitAttribute(com.sun.source.doctree.AttributeTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitAuthor(com.sun.source.doctree.AuthorTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitComment(com.sun.source.doctree.CommentTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitDeprecated(com.sun.source.doctree.DeprecatedTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitDocComment(com.sun.source.doctree.DocCommentTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitDocRoot(com.sun.source.doctree.DocRootTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitDocType(com.sun.source.doctree.DocTypeTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitEndElement(com.sun.source.doctree.EndElementTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitEntity(com.sun.source.doctree.EntityTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitErroneous(com.sun.source.doctree.ErroneousTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitEscape(com.sun.source.doctree.EscapeTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitHidden(com.sun.source.doctree.HiddenTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitIdentifier(com.sun.source.doctree.IdentifierTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitIndex(com.sun.source.doctree.IndexTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitInheritDoc(com.sun.source.doctree.InheritDocTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitLink(com.sun.source.doctree.LinkTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitLiteral(com.sun.source.doctree.LiteralTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitOther(com.sun.source.doctree.DocTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitParam(com.sun.source.doctree.ParamTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitProvides(com.sun.source.doctree.ProvidesTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitRawText(com.sun.source.doctree.RawTextTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitReference(com.sun.source.doctree.ReferenceTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitReturn(com.sun.source.doctree.ReturnTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSee(com.sun.source.doctree.SeeTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSerial(com.sun.source.doctree.SerialTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSerialData(com.sun.source.doctree.SerialDataTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSerialField(com.sun.source.doctree.SerialFieldTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSince(com.sun.source.doctree.SinceTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSnippet(com.sun.source.doctree.SnippetTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSpec(com.sun.source.doctree.SpecTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitStartElement(com.sun.source.doctree.StartElementTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSummary(com.sun.source.doctree.SummaryTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitSystemProperty(com.sun.source.doctree.SystemPropertyTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitText(com.sun.source.doctree.TextTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitThrows(com.sun.source.doctree.ThrowsTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitUnknownBlockTag(com.sun.source.doctree.UnknownBlockTagTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitUnknownInlineTag(com.sun.source.doctree.UnknownInlineTagTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitUses(com.sun.source.doctree.UsesTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitValue(com.sun.source.doctree.ValueTree,{com.sun.source.util.DocTreeScanner%1})
meth public {com.sun.source.util.DocTreeScanner%0} visitVersion(com.sun.source.doctree.VersionTree,{com.sun.source.util.DocTreeScanner%1})
supr java.lang.Object

CLSS public abstract com.sun.source.util.DocTrees
cons public init()
meth public abstract com.sun.source.doctree.DocCommentTree getDocCommentTree(com.sun.source.util.TreePath)
meth public abstract com.sun.source.doctree.DocCommentTree getDocCommentTree(javax.lang.model.element.Element)
meth public abstract com.sun.source.doctree.DocCommentTree getDocCommentTree(javax.lang.model.element.Element,java.lang.String) throws java.io.IOException
meth public abstract com.sun.source.doctree.DocCommentTree getDocCommentTree(javax.tools.FileObject)
meth public abstract com.sun.source.util.DocSourcePositions getSourcePositions()
meth public abstract com.sun.source.util.DocTreeFactory getDocTreeFactory()
meth public abstract com.sun.source.util.DocTreePath getDocTreePath(javax.tools.FileObject,javax.lang.model.element.PackageElement)
meth public abstract java.lang.String getCharacters(com.sun.source.doctree.EntityTree)
meth public abstract java.text.BreakIterator getBreakIterator()
meth public abstract java.util.List<com.sun.source.doctree.DocTree> getFirstSentence(java.util.List<? extends com.sun.source.doctree.DocTree>)
meth public abstract javax.lang.model.element.Element getElement(com.sun.source.util.DocTreePath)
meth public abstract javax.lang.model.type.TypeMirror getType(com.sun.source.util.DocTreePath)
meth public abstract javax.lang.model.util.Elements$DocCommentKind getDocCommentKind(com.sun.source.util.TreePath)
meth public abstract void printMessage(javax.tools.Diagnostic$Kind,java.lang.CharSequence,com.sun.source.doctree.DocTree,com.sun.source.doctree.DocCommentTree,com.sun.source.tree.CompilationUnitTree)
meth public abstract void setBreakIterator(java.text.BreakIterator)
meth public static com.sun.source.util.DocTrees instance(javax.annotation.processing.ProcessingEnvironment)
meth public static com.sun.source.util.DocTrees instance(javax.tools.JavaCompiler$CompilationTask)
supr com.sun.source.util.Trees

CLSS public abstract com.sun.source.util.JavacTask
cons protected init()
intf javax.tools.JavaCompiler$CompilationTask
meth public abstract java.lang.Iterable<? extends com.sun.source.tree.CompilationUnitTree> parse() throws java.io.IOException
meth public abstract java.lang.Iterable<? extends javax.lang.model.element.Element> analyze() throws java.io.IOException
meth public abstract java.lang.Iterable<? extends javax.tools.JavaFileObject> generate() throws java.io.IOException
meth public abstract javax.lang.model.type.TypeMirror getTypeMirror(java.lang.Iterable<? extends com.sun.source.tree.Tree>)
meth public abstract javax.lang.model.util.Elements getElements()
meth public abstract javax.lang.model.util.Types getTypes()
meth public abstract void addTaskListener(com.sun.source.util.TaskListener)
meth public abstract void removeTaskListener(com.sun.source.util.TaskListener)
meth public abstract void setTaskListener(com.sun.source.util.TaskListener)
meth public static com.sun.source.util.JavacTask instance(javax.annotation.processing.ProcessingEnvironment)
meth public void setParameterNameProvider(com.sun.source.util.ParameterNameProvider)
supr java.lang.Object

CLSS public abstract interface com.sun.source.util.ParameterNameProvider
meth public abstract java.lang.CharSequence getParameterName(javax.lang.model.element.VariableElement)

CLSS public abstract interface com.sun.source.util.Plugin
meth public abstract !varargs void init(com.sun.source.util.JavacTask,java.lang.String[])
meth public abstract java.lang.String getName()
meth public boolean autoStart()

CLSS public com.sun.source.util.SimpleDocTreeVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
cons protected init({com.sun.source.util.SimpleDocTreeVisitor%0})
fld protected final {com.sun.source.util.SimpleDocTreeVisitor%0} DEFAULT_VALUE
intf com.sun.source.doctree.DocTreeVisitor<{com.sun.source.util.SimpleDocTreeVisitor%0},{com.sun.source.util.SimpleDocTreeVisitor%1}>
meth protected {com.sun.source.util.SimpleDocTreeVisitor%0} defaultAction(com.sun.source.doctree.DocTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public final {com.sun.source.util.SimpleDocTreeVisitor%0} visit(com.sun.source.doctree.DocTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public final {com.sun.source.util.SimpleDocTreeVisitor%0} visit(java.lang.Iterable<? extends com.sun.source.doctree.DocTree>,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitAttribute(com.sun.source.doctree.AttributeTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitAuthor(com.sun.source.doctree.AuthorTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitComment(com.sun.source.doctree.CommentTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitDeprecated(com.sun.source.doctree.DeprecatedTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitDocComment(com.sun.source.doctree.DocCommentTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitDocRoot(com.sun.source.doctree.DocRootTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitDocType(com.sun.source.doctree.DocTypeTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitEndElement(com.sun.source.doctree.EndElementTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitEntity(com.sun.source.doctree.EntityTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitErroneous(com.sun.source.doctree.ErroneousTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitEscape(com.sun.source.doctree.EscapeTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitHidden(com.sun.source.doctree.HiddenTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitIdentifier(com.sun.source.doctree.IdentifierTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitIndex(com.sun.source.doctree.IndexTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitInheritDoc(com.sun.source.doctree.InheritDocTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitLink(com.sun.source.doctree.LinkTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitLiteral(com.sun.source.doctree.LiteralTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitOther(com.sun.source.doctree.DocTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitParam(com.sun.source.doctree.ParamTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitProvides(com.sun.source.doctree.ProvidesTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitRawText(com.sun.source.doctree.RawTextTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitReference(com.sun.source.doctree.ReferenceTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitReturn(com.sun.source.doctree.ReturnTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSee(com.sun.source.doctree.SeeTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSerial(com.sun.source.doctree.SerialTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSerialData(com.sun.source.doctree.SerialDataTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSerialField(com.sun.source.doctree.SerialFieldTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSince(com.sun.source.doctree.SinceTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSnippet(com.sun.source.doctree.SnippetTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSpec(com.sun.source.doctree.SpecTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitStartElement(com.sun.source.doctree.StartElementTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSummary(com.sun.source.doctree.SummaryTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitSystemProperty(com.sun.source.doctree.SystemPropertyTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitText(com.sun.source.doctree.TextTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitThrows(com.sun.source.doctree.ThrowsTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitUnknownBlockTag(com.sun.source.doctree.UnknownBlockTagTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitUnknownInlineTag(com.sun.source.doctree.UnknownInlineTagTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitUses(com.sun.source.doctree.UsesTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitValue(com.sun.source.doctree.ValueTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
meth public {com.sun.source.util.SimpleDocTreeVisitor%0} visitVersion(com.sun.source.doctree.VersionTree,{com.sun.source.util.SimpleDocTreeVisitor%1})
supr java.lang.Object

CLSS public com.sun.source.util.SimpleTreeVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
cons protected init({com.sun.source.util.SimpleTreeVisitor%0})
fld protected final {com.sun.source.util.SimpleTreeVisitor%0} DEFAULT_VALUE
intf com.sun.source.tree.TreeVisitor<{com.sun.source.util.SimpleTreeVisitor%0},{com.sun.source.util.SimpleTreeVisitor%1}>
meth protected {com.sun.source.util.SimpleTreeVisitor%0} defaultAction(com.sun.source.tree.Tree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public final {com.sun.source.util.SimpleTreeVisitor%0} visit(com.sun.source.tree.Tree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public final {com.sun.source.util.SimpleTreeVisitor%0} visit(java.lang.Iterable<? extends com.sun.source.tree.Tree>,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitAnnotatedType(com.sun.source.tree.AnnotatedTypeTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitAnnotation(com.sun.source.tree.AnnotationTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitAnyPattern(com.sun.source.tree.AnyPatternTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitArrayAccess(com.sun.source.tree.ArrayAccessTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitArrayType(com.sun.source.tree.ArrayTypeTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitAssert(com.sun.source.tree.AssertTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitAssignment(com.sun.source.tree.AssignmentTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitBinary(com.sun.source.tree.BinaryTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitBindingPattern(com.sun.source.tree.BindingPatternTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitBlock(com.sun.source.tree.BlockTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitBreak(com.sun.source.tree.BreakTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitCase(com.sun.source.tree.CaseTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitCatch(com.sun.source.tree.CatchTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitClass(com.sun.source.tree.ClassTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitCompilationUnit(com.sun.source.tree.CompilationUnitTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitCompoundAssignment(com.sun.source.tree.CompoundAssignmentTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitConditionalExpression(com.sun.source.tree.ConditionalExpressionTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitConstantCaseLabel(com.sun.source.tree.ConstantCaseLabelTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitContinue(com.sun.source.tree.ContinueTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitDeconstructionPattern(com.sun.source.tree.DeconstructionPatternTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitDefaultCaseLabel(com.sun.source.tree.DefaultCaseLabelTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitDoWhileLoop(com.sun.source.tree.DoWhileLoopTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitEmptyStatement(com.sun.source.tree.EmptyStatementTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitEnhancedForLoop(com.sun.source.tree.EnhancedForLoopTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitErroneous(com.sun.source.tree.ErroneousTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitExports(com.sun.source.tree.ExportsTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitExpressionStatement(com.sun.source.tree.ExpressionStatementTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitForLoop(com.sun.source.tree.ForLoopTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitIdentifier(com.sun.source.tree.IdentifierTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitIf(com.sun.source.tree.IfTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitImport(com.sun.source.tree.ImportTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitInstanceOf(com.sun.source.tree.InstanceOfTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitIntersectionType(com.sun.source.tree.IntersectionTypeTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitLabeledStatement(com.sun.source.tree.LabeledStatementTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitLambdaExpression(com.sun.source.tree.LambdaExpressionTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitLiteral(com.sun.source.tree.LiteralTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitMemberReference(com.sun.source.tree.MemberReferenceTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitMemberSelect(com.sun.source.tree.MemberSelectTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitMethod(com.sun.source.tree.MethodTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitMethodInvocation(com.sun.source.tree.MethodInvocationTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitModifiers(com.sun.source.tree.ModifiersTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitModule(com.sun.source.tree.ModuleTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitNewArray(com.sun.source.tree.NewArrayTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitNewClass(com.sun.source.tree.NewClassTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitOpens(com.sun.source.tree.OpensTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitOther(com.sun.source.tree.Tree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitPackage(com.sun.source.tree.PackageTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitParameterizedType(com.sun.source.tree.ParameterizedTypeTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitParenthesized(com.sun.source.tree.ParenthesizedTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitPatternCaseLabel(com.sun.source.tree.PatternCaseLabelTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitPrimitiveType(com.sun.source.tree.PrimitiveTypeTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitProvides(com.sun.source.tree.ProvidesTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitRequires(com.sun.source.tree.RequiresTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitReturn(com.sun.source.tree.ReturnTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitSwitch(com.sun.source.tree.SwitchTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitSwitchExpression(com.sun.source.tree.SwitchExpressionTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitSynchronized(com.sun.source.tree.SynchronizedTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitThrow(com.sun.source.tree.ThrowTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitTry(com.sun.source.tree.TryTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitTypeCast(com.sun.source.tree.TypeCastTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitTypeParameter(com.sun.source.tree.TypeParameterTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitUnary(com.sun.source.tree.UnaryTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitUnionType(com.sun.source.tree.UnionTypeTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitUses(com.sun.source.tree.UsesTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitVariable(com.sun.source.tree.VariableTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitWhileLoop(com.sun.source.tree.WhileLoopTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitWildcard(com.sun.source.tree.WildcardTree,{com.sun.source.util.SimpleTreeVisitor%1})
meth public {com.sun.source.util.SimpleTreeVisitor%0} visitYield(com.sun.source.tree.YieldTree,{com.sun.source.util.SimpleTreeVisitor%1})
supr java.lang.Object

CLSS public abstract interface com.sun.source.util.SourcePositions
meth public abstract long getEndPosition(com.sun.source.tree.CompilationUnitTree,com.sun.source.tree.Tree)
meth public abstract long getStartPosition(com.sun.source.tree.CompilationUnitTree,com.sun.source.tree.Tree)

CLSS public final com.sun.source.util.TaskEvent
cons public init(com.sun.source.util.TaskEvent$Kind)
cons public init(com.sun.source.util.TaskEvent$Kind,com.sun.source.tree.CompilationUnitTree)
cons public init(com.sun.source.util.TaskEvent$Kind,com.sun.source.tree.CompilationUnitTree,javax.lang.model.element.TypeElement)
cons public init(com.sun.source.util.TaskEvent$Kind,javax.tools.JavaFileObject)
innr public final static !enum Kind
meth public com.sun.source.tree.CompilationUnitTree getCompilationUnit()
meth public com.sun.source.util.TaskEvent$Kind getKind()
meth public java.lang.String toString()
meth public javax.lang.model.element.TypeElement getTypeElement()
meth public javax.tools.JavaFileObject getSourceFile()
supr java.lang.Object
hfds clazz,file,kind,unit

CLSS public final static !enum com.sun.source.util.TaskEvent$Kind
 outer com.sun.source.util.TaskEvent
fld public final static com.sun.source.util.TaskEvent$Kind ANALYZE
fld public final static com.sun.source.util.TaskEvent$Kind ANNOTATION_PROCESSING
fld public final static com.sun.source.util.TaskEvent$Kind ANNOTATION_PROCESSING_ROUND
fld public final static com.sun.source.util.TaskEvent$Kind COMPILATION
fld public final static com.sun.source.util.TaskEvent$Kind ENTER
fld public final static com.sun.source.util.TaskEvent$Kind GENERATE
fld public final static com.sun.source.util.TaskEvent$Kind PARSE
meth public static com.sun.source.util.TaskEvent$Kind valueOf(java.lang.String)
meth public static com.sun.source.util.TaskEvent$Kind[] values()
supr java.lang.Enum<com.sun.source.util.TaskEvent$Kind>

CLSS public abstract interface com.sun.source.util.TaskListener
meth public void finished(com.sun.source.util.TaskEvent)
meth public void started(com.sun.source.util.TaskEvent)

CLSS public com.sun.source.util.TreePath
cons public init(com.sun.source.tree.CompilationUnitTree)
cons public init(com.sun.source.util.TreePath,com.sun.source.tree.Tree)
intf java.lang.Iterable<com.sun.source.tree.Tree>
meth public com.sun.source.tree.CompilationUnitTree getCompilationUnit()
meth public com.sun.source.tree.Tree getLeaf()
meth public com.sun.source.util.TreePath getParentPath()
meth public java.util.Iterator<com.sun.source.tree.Tree> iterator()
meth public static com.sun.source.util.TreePath getPath(com.sun.source.tree.CompilationUnitTree,com.sun.source.tree.Tree)
meth public static com.sun.source.util.TreePath getPath(com.sun.source.util.TreePath,com.sun.source.tree.Tree)
supr java.lang.Object
hfds compilationUnit,leaf,parent

CLSS public com.sun.source.util.TreePathScanner<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public com.sun.source.util.TreePath getCurrentPath()
meth public {com.sun.source.util.TreePathScanner%0} scan(com.sun.source.tree.Tree,{com.sun.source.util.TreePathScanner%1})
meth public {com.sun.source.util.TreePathScanner%0} scan(com.sun.source.util.TreePath,{com.sun.source.util.TreePathScanner%1})
supr com.sun.source.util.TreeScanner<{com.sun.source.util.TreePathScanner%0},{com.sun.source.util.TreePathScanner%1}>
hfds path

CLSS public com.sun.source.util.TreeScanner<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
intf com.sun.source.tree.TreeVisitor<{com.sun.source.util.TreeScanner%0},{com.sun.source.util.TreeScanner%1}>
meth public {com.sun.source.util.TreeScanner%0} reduce({com.sun.source.util.TreeScanner%0},{com.sun.source.util.TreeScanner%0})
meth public {com.sun.source.util.TreeScanner%0} scan(com.sun.source.tree.Tree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} scan(java.lang.Iterable<? extends com.sun.source.tree.Tree>,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitAnnotatedType(com.sun.source.tree.AnnotatedTypeTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitAnnotation(com.sun.source.tree.AnnotationTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitAnyPattern(com.sun.source.tree.AnyPatternTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitArrayAccess(com.sun.source.tree.ArrayAccessTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitArrayType(com.sun.source.tree.ArrayTypeTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitAssert(com.sun.source.tree.AssertTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitAssignment(com.sun.source.tree.AssignmentTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitBinary(com.sun.source.tree.BinaryTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitBindingPattern(com.sun.source.tree.BindingPatternTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitBlock(com.sun.source.tree.BlockTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitBreak(com.sun.source.tree.BreakTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitCase(com.sun.source.tree.CaseTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitCatch(com.sun.source.tree.CatchTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitClass(com.sun.source.tree.ClassTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitCompilationUnit(com.sun.source.tree.CompilationUnitTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitCompoundAssignment(com.sun.source.tree.CompoundAssignmentTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitConditionalExpression(com.sun.source.tree.ConditionalExpressionTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitConstantCaseLabel(com.sun.source.tree.ConstantCaseLabelTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitContinue(com.sun.source.tree.ContinueTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitDeconstructionPattern(com.sun.source.tree.DeconstructionPatternTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitDefaultCaseLabel(com.sun.source.tree.DefaultCaseLabelTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitDoWhileLoop(com.sun.source.tree.DoWhileLoopTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitEmptyStatement(com.sun.source.tree.EmptyStatementTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitEnhancedForLoop(com.sun.source.tree.EnhancedForLoopTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitErroneous(com.sun.source.tree.ErroneousTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitExports(com.sun.source.tree.ExportsTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitExpressionStatement(com.sun.source.tree.ExpressionStatementTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitForLoop(com.sun.source.tree.ForLoopTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitIdentifier(com.sun.source.tree.IdentifierTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitIf(com.sun.source.tree.IfTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitImport(com.sun.source.tree.ImportTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitInstanceOf(com.sun.source.tree.InstanceOfTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitIntersectionType(com.sun.source.tree.IntersectionTypeTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitLabeledStatement(com.sun.source.tree.LabeledStatementTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitLambdaExpression(com.sun.source.tree.LambdaExpressionTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitLiteral(com.sun.source.tree.LiteralTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitMemberReference(com.sun.source.tree.MemberReferenceTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitMemberSelect(com.sun.source.tree.MemberSelectTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitMethod(com.sun.source.tree.MethodTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitMethodInvocation(com.sun.source.tree.MethodInvocationTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitModifiers(com.sun.source.tree.ModifiersTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitModule(com.sun.source.tree.ModuleTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitNewArray(com.sun.source.tree.NewArrayTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitNewClass(com.sun.source.tree.NewClassTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitOpens(com.sun.source.tree.OpensTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitOther(com.sun.source.tree.Tree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitPackage(com.sun.source.tree.PackageTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitParameterizedType(com.sun.source.tree.ParameterizedTypeTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitParenthesized(com.sun.source.tree.ParenthesizedTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitPatternCaseLabel(com.sun.source.tree.PatternCaseLabelTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitPrimitiveType(com.sun.source.tree.PrimitiveTypeTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitProvides(com.sun.source.tree.ProvidesTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitRequires(com.sun.source.tree.RequiresTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitReturn(com.sun.source.tree.ReturnTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitSwitch(com.sun.source.tree.SwitchTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitSwitchExpression(com.sun.source.tree.SwitchExpressionTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitSynchronized(com.sun.source.tree.SynchronizedTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitThrow(com.sun.source.tree.ThrowTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitTry(com.sun.source.tree.TryTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitTypeCast(com.sun.source.tree.TypeCastTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitTypeParameter(com.sun.source.tree.TypeParameterTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitUnary(com.sun.source.tree.UnaryTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitUnionType(com.sun.source.tree.UnionTypeTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitUses(com.sun.source.tree.UsesTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitVariable(com.sun.source.tree.VariableTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitWhileLoop(com.sun.source.tree.WhileLoopTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitWildcard(com.sun.source.tree.WildcardTree,{com.sun.source.util.TreeScanner%1})
meth public {com.sun.source.util.TreeScanner%0} visitYield(com.sun.source.tree.YieldTree,{com.sun.source.util.TreeScanner%1})
supr java.lang.Object

CLSS public abstract com.sun.source.util.Trees
cons public init()
meth public abstract boolean isAccessible(com.sun.source.tree.Scope,javax.lang.model.element.Element,javax.lang.model.type.DeclaredType)
meth public abstract boolean isAccessible(com.sun.source.tree.Scope,javax.lang.model.element.TypeElement)
meth public abstract com.sun.source.tree.ClassTree getTree(javax.lang.model.element.TypeElement)
meth public abstract com.sun.source.tree.MethodTree getTree(javax.lang.model.element.ExecutableElement)
meth public abstract com.sun.source.tree.Scope getScope(com.sun.source.util.TreePath)
meth public abstract com.sun.source.tree.Tree getTree(javax.lang.model.element.Element)
meth public abstract com.sun.source.tree.Tree getTree(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror)
meth public abstract com.sun.source.tree.Tree getTree(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.AnnotationValue)
meth public abstract com.sun.source.util.SourcePositions getSourcePositions()
meth public abstract com.sun.source.util.TreePath getPath(com.sun.source.tree.CompilationUnitTree,com.sun.source.tree.Tree)
meth public abstract com.sun.source.util.TreePath getPath(javax.lang.model.element.Element)
meth public abstract com.sun.source.util.TreePath getPath(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror)
meth public abstract com.sun.source.util.TreePath getPath(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.AnnotationValue)
meth public abstract java.lang.String getDocComment(com.sun.source.util.TreePath)
meth public abstract javax.lang.model.element.Element getElement(com.sun.source.util.TreePath)
meth public abstract javax.lang.model.type.TypeMirror getLub(com.sun.source.tree.CatchTree)
meth public abstract javax.lang.model.type.TypeMirror getOriginalType(javax.lang.model.type.ErrorType)
meth public abstract javax.lang.model.type.TypeMirror getTypeMirror(com.sun.source.util.TreePath)
meth public abstract void printMessage(javax.tools.Diagnostic$Kind,java.lang.CharSequence,com.sun.source.tree.Tree,com.sun.source.tree.CompilationUnitTree)
meth public static com.sun.source.util.Trees instance(javax.annotation.processing.ProcessingEnvironment)
meth public static com.sun.source.util.Trees instance(javax.tools.JavaCompiler$CompilationTask)
supr java.lang.Object

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public abstract java.lang.CharSequence subSequence(int,int)
meth public abstract java.lang.String toString()
meth public java.util.stream.IntStream chars()
meth public java.util.stream.IntStream codePoints()

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public abstract javax.annotation.processing.AbstractProcessor
cons protected init()
fld protected javax.annotation.processing.ProcessingEnvironment processingEnv
intf javax.annotation.processing.Processor
meth protected boolean isInitialized()
meth public abstract boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth public java.lang.Iterable<? extends javax.annotation.processing.Completion> getCompletions(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.ExecutableElement,java.lang.String)
meth public java.util.Set<java.lang.String> getSupportedAnnotationTypes()
meth public java.util.Set<java.lang.String> getSupportedOptions()
meth public javax.lang.model.SourceVersion getSupportedSourceVersion()
meth public void init(javax.annotation.processing.ProcessingEnvironment)
supr java.lang.Object
hfds initialized

CLSS public abstract interface javax.annotation.processing.Completion
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.String getValue()

CLSS public javax.annotation.processing.Completions
meth public static javax.annotation.processing.Completion of(java.lang.String)
meth public static javax.annotation.processing.Completion of(java.lang.String,java.lang.String)
supr java.lang.Object
hcls SimpleCompletion

CLSS public abstract interface javax.annotation.processing.Filer
meth public abstract !varargs javax.tools.FileObject createResource(javax.tools.JavaFileManager$Location,java.lang.CharSequence,java.lang.CharSequence,javax.lang.model.element.Element[]) throws java.io.IOException
meth public abstract !varargs javax.tools.JavaFileObject createClassFile(java.lang.CharSequence,javax.lang.model.element.Element[]) throws java.io.IOException
meth public abstract !varargs javax.tools.JavaFileObject createSourceFile(java.lang.CharSequence,javax.lang.model.element.Element[]) throws java.io.IOException
meth public abstract javax.tools.FileObject getResource(javax.tools.JavaFileManager$Location,java.lang.CharSequence,java.lang.CharSequence) throws java.io.IOException

CLSS public javax.annotation.processing.FilerException
cons public init(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public abstract interface !annotation javax.annotation.processing.Generated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE, TYPE, METHOD, CONSTRUCTOR, FIELD, LOCAL_VARIABLE, PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String comments()
meth public abstract !hasdefault java.lang.String date()
meth public abstract java.lang.String[] value()

CLSS public abstract interface javax.annotation.processing.Messager
meth public abstract void printMessage(javax.tools.Diagnostic$Kind,java.lang.CharSequence)
meth public abstract void printMessage(javax.tools.Diagnostic$Kind,java.lang.CharSequence,javax.lang.model.element.Element)
meth public abstract void printMessage(javax.tools.Diagnostic$Kind,java.lang.CharSequence,javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror)
meth public abstract void printMessage(javax.tools.Diagnostic$Kind,java.lang.CharSequence,javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.AnnotationValue)
meth public void printError(java.lang.CharSequence)
meth public void printError(java.lang.CharSequence,javax.lang.model.element.Element)
meth public void printNote(java.lang.CharSequence)
meth public void printNote(java.lang.CharSequence,javax.lang.model.element.Element)
meth public void printWarning(java.lang.CharSequence)
meth public void printWarning(java.lang.CharSequence,javax.lang.model.element.Element)

CLSS public abstract interface javax.annotation.processing.ProcessingEnvironment
meth public abstract java.util.Locale getLocale()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getOptions()
meth public abstract javax.annotation.processing.Filer getFiler()
meth public abstract javax.annotation.processing.Messager getMessager()
meth public abstract javax.lang.model.SourceVersion getSourceVersion()
meth public abstract javax.lang.model.util.Elements getElementUtils()
meth public abstract javax.lang.model.util.Types getTypeUtils()
meth public boolean isPreviewEnabled()

CLSS public abstract interface javax.annotation.processing.Processor
meth public abstract boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth public abstract java.lang.Iterable<? extends javax.annotation.processing.Completion> getCompletions(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.ExecutableElement,java.lang.String)
meth public abstract java.util.Set<java.lang.String> getSupportedAnnotationTypes()
meth public abstract java.util.Set<java.lang.String> getSupportedOptions()
meth public abstract javax.lang.model.SourceVersion getSupportedSourceVersion()
meth public abstract void init(javax.annotation.processing.ProcessingEnvironment)

CLSS public abstract interface javax.annotation.processing.RoundEnvironment
meth public !varargs java.util.Set<? extends javax.lang.model.element.Element> getElementsAnnotatedWithAny(javax.lang.model.element.TypeElement[])
meth public abstract boolean errorRaised()
meth public abstract boolean processingOver()
meth public abstract java.util.Set<? extends javax.lang.model.element.Element> getElementsAnnotatedWith(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public abstract java.util.Set<? extends javax.lang.model.element.Element> getElementsAnnotatedWith(javax.lang.model.element.TypeElement)
meth public abstract java.util.Set<? extends javax.lang.model.element.Element> getRootElements()
meth public java.util.Set<? extends javax.lang.model.element.Element> getElementsAnnotatedWithAny(java.util.Set<java.lang.Class<? extends java.lang.annotation.Annotation>>)

CLSS public abstract interface !annotation javax.annotation.processing.SupportedAnnotationTypes
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract interface !annotation javax.annotation.processing.SupportedOptions
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract interface !annotation javax.annotation.processing.SupportedSourceVersion
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.lang.model.SourceVersion value()

CLSS public abstract interface javax.lang.model.AnnotatedConstruct
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getAnnotation(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0}[] getAnnotationsByType(java.lang.Class<{%%0}>)
meth public abstract java.util.List<? extends javax.lang.model.element.AnnotationMirror> getAnnotationMirrors()

CLSS public final !enum javax.lang.model.SourceVersion
fld public final static javax.lang.model.SourceVersion RELEASE_0
fld public final static javax.lang.model.SourceVersion RELEASE_1
fld public final static javax.lang.model.SourceVersion RELEASE_10
fld public final static javax.lang.model.SourceVersion RELEASE_11
fld public final static javax.lang.model.SourceVersion RELEASE_12
fld public final static javax.lang.model.SourceVersion RELEASE_13
fld public final static javax.lang.model.SourceVersion RELEASE_14
fld public final static javax.lang.model.SourceVersion RELEASE_15
fld public final static javax.lang.model.SourceVersion RELEASE_16
fld public final static javax.lang.model.SourceVersion RELEASE_17
fld public final static javax.lang.model.SourceVersion RELEASE_18
fld public final static javax.lang.model.SourceVersion RELEASE_19
fld public final static javax.lang.model.SourceVersion RELEASE_2
fld public final static javax.lang.model.SourceVersion RELEASE_20
fld public final static javax.lang.model.SourceVersion RELEASE_21
fld public final static javax.lang.model.SourceVersion RELEASE_22
fld public final static javax.lang.model.SourceVersion RELEASE_23
fld public final static javax.lang.model.SourceVersion RELEASE_24
fld public final static javax.lang.model.SourceVersion RELEASE_3
fld public final static javax.lang.model.SourceVersion RELEASE_4
fld public final static javax.lang.model.SourceVersion RELEASE_5
fld public final static javax.lang.model.SourceVersion RELEASE_6
fld public final static javax.lang.model.SourceVersion RELEASE_7
fld public final static javax.lang.model.SourceVersion RELEASE_8
fld public final static javax.lang.model.SourceVersion RELEASE_9
meth public static boolean isIdentifier(java.lang.CharSequence)
meth public static boolean isKeyword(java.lang.CharSequence)
meth public static boolean isKeyword(java.lang.CharSequence,javax.lang.model.SourceVersion)
meth public static boolean isName(java.lang.CharSequence)
meth public static boolean isName(java.lang.CharSequence,javax.lang.model.SourceVersion)
meth public static javax.lang.model.SourceVersion latest()
meth public static javax.lang.model.SourceVersion latestSupported()
meth public static javax.lang.model.SourceVersion valueOf(java.lang.String)
meth public static javax.lang.model.SourceVersion[] values()
supr java.lang.Enum<javax.lang.model.SourceVersion>
hfds latestSupported

CLSS public javax.lang.model.UnknownEntityException
cons protected init(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface javax.lang.model.element.AnnotationMirror
meth public abstract java.util.Map<? extends javax.lang.model.element.ExecutableElement,? extends javax.lang.model.element.AnnotationValue> getElementValues()
meth public abstract javax.lang.model.type.DeclaredType getAnnotationType()

CLSS public abstract interface javax.lang.model.element.AnnotationValue
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(javax.lang.model.element.AnnotationValueVisitor<{%%0},{%%1}>,{%%1})
meth public abstract java.lang.Object getValue()
meth public abstract java.lang.String toString()

CLSS public abstract interface javax.lang.model.element.AnnotationValueVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visit(javax.lang.model.element.AnnotationValue,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitAnnotation(javax.lang.model.element.AnnotationMirror,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitArray(java.util.List<? extends javax.lang.model.element.AnnotationValue>,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitBoolean(boolean,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitByte(byte,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitChar(char,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitDouble(double,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitEnumConstant(javax.lang.model.element.VariableElement,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitFloat(float,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitInt(int,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitLong(long,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitShort(short,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitString(java.lang.String,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitType(javax.lang.model.type.TypeMirror,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public abstract {javax.lang.model.element.AnnotationValueVisitor%0} visitUnknown(javax.lang.model.element.AnnotationValue,{javax.lang.model.element.AnnotationValueVisitor%1})
meth public {javax.lang.model.element.AnnotationValueVisitor%0} visit(javax.lang.model.element.AnnotationValue)

CLSS public abstract interface javax.lang.model.element.Element
intf javax.lang.model.AnnotatedConstruct
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(javax.lang.model.element.ElementVisitor<{%%0},{%%1}>,{%%1})
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getAnnotation(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0}[] getAnnotationsByType(java.lang.Class<{%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.util.List<? extends javax.lang.model.element.AnnotationMirror> getAnnotationMirrors()
meth public abstract java.util.List<? extends javax.lang.model.element.Element> getEnclosedElements()
meth public abstract java.util.Set<javax.lang.model.element.Modifier> getModifiers()
meth public abstract javax.lang.model.element.Element getEnclosingElement()
meth public abstract javax.lang.model.element.ElementKind getKind()
meth public abstract javax.lang.model.element.Name getSimpleName()
meth public abstract javax.lang.model.type.TypeMirror asType()

CLSS public final !enum javax.lang.model.element.ElementKind
fld public final static javax.lang.model.element.ElementKind ANNOTATION_TYPE
fld public final static javax.lang.model.element.ElementKind BINDING_VARIABLE
fld public final static javax.lang.model.element.ElementKind CLASS
fld public final static javax.lang.model.element.ElementKind CONSTRUCTOR
fld public final static javax.lang.model.element.ElementKind ENUM
fld public final static javax.lang.model.element.ElementKind ENUM_CONSTANT
fld public final static javax.lang.model.element.ElementKind EXCEPTION_PARAMETER
fld public final static javax.lang.model.element.ElementKind FIELD
fld public final static javax.lang.model.element.ElementKind INSTANCE_INIT
fld public final static javax.lang.model.element.ElementKind INTERFACE
fld public final static javax.lang.model.element.ElementKind LOCAL_VARIABLE
fld public final static javax.lang.model.element.ElementKind METHOD
fld public final static javax.lang.model.element.ElementKind MODULE
fld public final static javax.lang.model.element.ElementKind OTHER
fld public final static javax.lang.model.element.ElementKind PACKAGE
fld public final static javax.lang.model.element.ElementKind PARAMETER
fld public final static javax.lang.model.element.ElementKind RECORD
fld public final static javax.lang.model.element.ElementKind RECORD_COMPONENT
fld public final static javax.lang.model.element.ElementKind RESOURCE_VARIABLE
fld public final static javax.lang.model.element.ElementKind STATIC_INIT
fld public final static javax.lang.model.element.ElementKind TYPE_PARAMETER
meth public boolean isClass()
meth public boolean isDeclaredType()
meth public boolean isExecutable()
meth public boolean isField()
meth public boolean isInitializer()
meth public boolean isInterface()
meth public boolean isVariable()
meth public static javax.lang.model.element.ElementKind valueOf(java.lang.String)
meth public static javax.lang.model.element.ElementKind[] values()
supr java.lang.Enum<javax.lang.model.element.ElementKind>

CLSS public abstract interface javax.lang.model.element.ElementVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {javax.lang.model.element.ElementVisitor%0} visit(javax.lang.model.element.Element,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitExecutable(javax.lang.model.element.ExecutableElement,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitPackage(javax.lang.model.element.PackageElement,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitType(javax.lang.model.element.TypeElement,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitTypeParameter(javax.lang.model.element.TypeParameterElement,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitUnknown(javax.lang.model.element.Element,{javax.lang.model.element.ElementVisitor%1})
meth public abstract {javax.lang.model.element.ElementVisitor%0} visitVariable(javax.lang.model.element.VariableElement,{javax.lang.model.element.ElementVisitor%1})
meth public {javax.lang.model.element.ElementVisitor%0} visit(javax.lang.model.element.Element)
meth public {javax.lang.model.element.ElementVisitor%0} visitModule(javax.lang.model.element.ModuleElement,{javax.lang.model.element.ElementVisitor%1})
meth public {javax.lang.model.element.ElementVisitor%0} visitRecordComponent(javax.lang.model.element.RecordComponentElement,{javax.lang.model.element.ElementVisitor%1})

CLSS public abstract interface javax.lang.model.element.ExecutableElement
intf javax.lang.model.element.Element
intf javax.lang.model.element.Parameterizable
meth public abstract boolean isDefault()
meth public abstract boolean isVarArgs()
meth public abstract java.util.List<? extends javax.lang.model.element.TypeParameterElement> getTypeParameters()
meth public abstract java.util.List<? extends javax.lang.model.element.VariableElement> getParameters()
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> getThrownTypes()
meth public abstract javax.lang.model.element.AnnotationValue getDefaultValue()
meth public abstract javax.lang.model.element.Element getEnclosingElement()
meth public abstract javax.lang.model.element.Name getSimpleName()
meth public abstract javax.lang.model.type.TypeMirror asType()
meth public abstract javax.lang.model.type.TypeMirror getReceiverType()
meth public abstract javax.lang.model.type.TypeMirror getReturnType()

CLSS public !enum javax.lang.model.element.Modifier
fld public final static javax.lang.model.element.Modifier ABSTRACT
fld public final static javax.lang.model.element.Modifier DEFAULT
fld public final static javax.lang.model.element.Modifier FINAL
fld public final static javax.lang.model.element.Modifier NATIVE
fld public final static javax.lang.model.element.Modifier NON_SEALED
fld public final static javax.lang.model.element.Modifier PRIVATE
fld public final static javax.lang.model.element.Modifier PROTECTED
fld public final static javax.lang.model.element.Modifier PUBLIC
fld public final static javax.lang.model.element.Modifier SEALED
fld public final static javax.lang.model.element.Modifier STATIC
fld public final static javax.lang.model.element.Modifier STRICTFP
fld public final static javax.lang.model.element.Modifier SYNCHRONIZED
fld public final static javax.lang.model.element.Modifier TRANSIENT
fld public final static javax.lang.model.element.Modifier VOLATILE
meth public java.lang.String toString()
meth public static javax.lang.model.element.Modifier valueOf(java.lang.String)
meth public static javax.lang.model.element.Modifier[] values()
supr java.lang.Enum<javax.lang.model.element.Modifier>

CLSS public abstract interface javax.lang.model.element.ModuleElement
innr public abstract interface static Directive
innr public abstract interface static DirectiveVisitor
innr public abstract interface static ExportsDirective
innr public abstract interface static OpensDirective
innr public abstract interface static ProvidesDirective
innr public abstract interface static RequiresDirective
innr public abstract interface static UsesDirective
innr public final static !enum DirectiveKind
intf javax.lang.model.element.Element
intf javax.lang.model.element.QualifiedNameable
meth public abstract boolean isOpen()
meth public abstract boolean isUnnamed()
meth public abstract java.util.List<? extends javax.lang.model.element.Element> getEnclosedElements()
meth public abstract java.util.List<? extends javax.lang.model.element.ModuleElement$Directive> getDirectives()
meth public abstract javax.lang.model.element.Element getEnclosingElement()
meth public abstract javax.lang.model.element.Name getQualifiedName()
meth public abstract javax.lang.model.element.Name getSimpleName()
meth public abstract javax.lang.model.type.TypeMirror asType()

CLSS public abstract interface static javax.lang.model.element.ModuleElement$Directive
 outer javax.lang.model.element.ModuleElement
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(javax.lang.model.element.ModuleElement$DirectiveVisitor<{%%0},{%%1}>,{%%1})
meth public abstract javax.lang.model.element.ModuleElement$DirectiveKind getKind()

CLSS public final static !enum javax.lang.model.element.ModuleElement$DirectiveKind
 outer javax.lang.model.element.ModuleElement
fld public final static javax.lang.model.element.ModuleElement$DirectiveKind EXPORTS
fld public final static javax.lang.model.element.ModuleElement$DirectiveKind OPENS
fld public final static javax.lang.model.element.ModuleElement$DirectiveKind PROVIDES
fld public final static javax.lang.model.element.ModuleElement$DirectiveKind REQUIRES
fld public final static javax.lang.model.element.ModuleElement$DirectiveKind USES
meth public static javax.lang.model.element.ModuleElement$DirectiveKind valueOf(java.lang.String)
meth public static javax.lang.model.element.ModuleElement$DirectiveKind[] values()
supr java.lang.Enum<javax.lang.model.element.ModuleElement$DirectiveKind>

CLSS public abstract interface static javax.lang.model.element.ModuleElement$DirectiveVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer javax.lang.model.element.ModuleElement
meth public abstract {javax.lang.model.element.ModuleElement$DirectiveVisitor%0} visitExports(javax.lang.model.element.ModuleElement$ExportsDirective,{javax.lang.model.element.ModuleElement$DirectiveVisitor%1})
meth public abstract {javax.lang.model.element.ModuleElement$DirectiveVisitor%0} visitOpens(javax.lang.model.element.ModuleElement$OpensDirective,{javax.lang.model.element.ModuleElement$DirectiveVisitor%1})
meth public abstract {javax.lang.model.element.ModuleElement$DirectiveVisitor%0} visitProvides(javax.lang.model.element.ModuleElement$ProvidesDirective,{javax.lang.model.element.ModuleElement$DirectiveVisitor%1})
meth public abstract {javax.lang.model.element.ModuleElement$DirectiveVisitor%0} visitRequires(javax.lang.model.element.ModuleElement$RequiresDirective,{javax.lang.model.element.ModuleElement$DirectiveVisitor%1})
meth public abstract {javax.lang.model.element.ModuleElement$DirectiveVisitor%0} visitUses(javax.lang.model.element.ModuleElement$UsesDirective,{javax.lang.model.element.ModuleElement$DirectiveVisitor%1})
meth public {javax.lang.model.element.ModuleElement$DirectiveVisitor%0} visit(javax.lang.model.element.ModuleElement$Directive)
meth public {javax.lang.model.element.ModuleElement$DirectiveVisitor%0} visit(javax.lang.model.element.ModuleElement$Directive,{javax.lang.model.element.ModuleElement$DirectiveVisitor%1})
meth public {javax.lang.model.element.ModuleElement$DirectiveVisitor%0} visitUnknown(javax.lang.model.element.ModuleElement$Directive,{javax.lang.model.element.ModuleElement$DirectiveVisitor%1})

CLSS public abstract interface static javax.lang.model.element.ModuleElement$ExportsDirective
 outer javax.lang.model.element.ModuleElement
intf javax.lang.model.element.ModuleElement$Directive
meth public abstract java.util.List<? extends javax.lang.model.element.ModuleElement> getTargetModules()
meth public abstract javax.lang.model.element.PackageElement getPackage()

CLSS public abstract interface static javax.lang.model.element.ModuleElement$OpensDirective
 outer javax.lang.model.element.ModuleElement
intf javax.lang.model.element.ModuleElement$Directive
meth public abstract java.util.List<? extends javax.lang.model.element.ModuleElement> getTargetModules()
meth public abstract javax.lang.model.element.PackageElement getPackage()

CLSS public abstract interface static javax.lang.model.element.ModuleElement$ProvidesDirective
 outer javax.lang.model.element.ModuleElement
intf javax.lang.model.element.ModuleElement$Directive
meth public abstract java.util.List<? extends javax.lang.model.element.TypeElement> getImplementations()
meth public abstract javax.lang.model.element.TypeElement getService()

CLSS public abstract interface static javax.lang.model.element.ModuleElement$RequiresDirective
 outer javax.lang.model.element.ModuleElement
intf javax.lang.model.element.ModuleElement$Directive
meth public abstract boolean isStatic()
meth public abstract boolean isTransitive()
meth public abstract javax.lang.model.element.ModuleElement getDependency()

CLSS public abstract interface static javax.lang.model.element.ModuleElement$UsesDirective
 outer javax.lang.model.element.ModuleElement
intf javax.lang.model.element.ModuleElement$Directive
meth public abstract javax.lang.model.element.TypeElement getService()

CLSS public abstract interface javax.lang.model.element.Name
intf java.lang.CharSequence
meth public abstract boolean contentEquals(java.lang.CharSequence)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()

CLSS public final !enum javax.lang.model.element.NestingKind
fld public final static javax.lang.model.element.NestingKind ANONYMOUS
fld public final static javax.lang.model.element.NestingKind LOCAL
fld public final static javax.lang.model.element.NestingKind MEMBER
fld public final static javax.lang.model.element.NestingKind TOP_LEVEL
meth public boolean isNested()
meth public static javax.lang.model.element.NestingKind valueOf(java.lang.String)
meth public static javax.lang.model.element.NestingKind[] values()
supr java.lang.Enum<javax.lang.model.element.NestingKind>

CLSS public abstract interface javax.lang.model.element.PackageElement
intf javax.lang.model.element.Element
intf javax.lang.model.element.QualifiedNameable
meth public abstract boolean isUnnamed()
meth public abstract java.util.List<? extends javax.lang.model.element.Element> getEnclosedElements()
meth public abstract javax.lang.model.element.Element getEnclosingElement()
meth public abstract javax.lang.model.element.Name getQualifiedName()
meth public abstract javax.lang.model.element.Name getSimpleName()
meth public abstract javax.lang.model.type.TypeMirror asType()

CLSS public abstract interface javax.lang.model.element.Parameterizable
intf javax.lang.model.element.Element
meth public abstract java.util.List<? extends javax.lang.model.element.TypeParameterElement> getTypeParameters()

CLSS public abstract interface javax.lang.model.element.QualifiedNameable
intf javax.lang.model.element.Element
meth public abstract javax.lang.model.element.Name getQualifiedName()

CLSS public abstract interface javax.lang.model.element.RecordComponentElement
intf javax.lang.model.element.Element
meth public abstract javax.lang.model.element.Element getEnclosingElement()
meth public abstract javax.lang.model.element.ExecutableElement getAccessor()
meth public abstract javax.lang.model.element.Name getSimpleName()
meth public abstract javax.lang.model.type.TypeMirror asType()

CLSS public abstract interface javax.lang.model.element.TypeElement
intf javax.lang.model.element.Element
intf javax.lang.model.element.Parameterizable
intf javax.lang.model.element.QualifiedNameable
meth public abstract java.util.List<? extends javax.lang.model.element.Element> getEnclosedElements()
meth public abstract java.util.List<? extends javax.lang.model.element.TypeParameterElement> getTypeParameters()
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> getInterfaces()
meth public abstract javax.lang.model.element.Element getEnclosingElement()
meth public abstract javax.lang.model.element.Name getQualifiedName()
meth public abstract javax.lang.model.element.Name getSimpleName()
meth public abstract javax.lang.model.element.NestingKind getNestingKind()
meth public abstract javax.lang.model.type.TypeMirror asType()
meth public abstract javax.lang.model.type.TypeMirror getSuperclass()
meth public java.util.List<? extends javax.lang.model.element.RecordComponentElement> getRecordComponents()
meth public java.util.List<? extends javax.lang.model.type.TypeMirror> getPermittedSubclasses()

CLSS public abstract interface javax.lang.model.element.TypeParameterElement
intf javax.lang.model.element.Element
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> getBounds()
meth public abstract javax.lang.model.element.Element getEnclosingElement()
meth public abstract javax.lang.model.element.Element getGenericElement()
meth public abstract javax.lang.model.type.TypeMirror asType()

CLSS public javax.lang.model.element.UnknownAnnotationValueException
cons public init(javax.lang.model.element.AnnotationValue,java.lang.Object)
meth public java.lang.Object getArgument()
meth public javax.lang.model.element.AnnotationValue getUnknownAnnotationValue()
supr javax.lang.model.UnknownEntityException
hfds av,parameter,serialVersionUID

CLSS public javax.lang.model.element.UnknownDirectiveException
cons public init(javax.lang.model.element.ModuleElement$Directive,java.lang.Object)
meth public java.lang.Object getArgument()
meth public javax.lang.model.element.ModuleElement$Directive getUnknownDirective()
supr javax.lang.model.UnknownEntityException
hfds directive,parameter,serialVersionUID

CLSS public javax.lang.model.element.UnknownElementException
cons public init(javax.lang.model.element.Element,java.lang.Object)
meth public java.lang.Object getArgument()
meth public javax.lang.model.element.Element getUnknownElement()
supr javax.lang.model.UnknownEntityException
hfds element,parameter,serialVersionUID

CLSS public abstract interface javax.lang.model.element.VariableElement
intf javax.lang.model.element.Element
meth public abstract java.lang.Object getConstantValue()
meth public abstract javax.lang.model.element.Element getEnclosingElement()
meth public abstract javax.lang.model.element.Name getSimpleName()
meth public abstract javax.lang.model.type.TypeMirror asType()
meth public boolean isUnnamed()

CLSS public abstract interface javax.lang.model.type.ArrayType
intf javax.lang.model.type.ReferenceType
meth public abstract javax.lang.model.type.TypeMirror getComponentType()

CLSS public abstract interface javax.lang.model.type.DeclaredType
intf javax.lang.model.type.ReferenceType
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> getTypeArguments()
meth public abstract javax.lang.model.element.Element asElement()
meth public abstract javax.lang.model.type.TypeMirror getEnclosingType()

CLSS public abstract interface javax.lang.model.type.ErrorType
intf javax.lang.model.type.DeclaredType

CLSS public abstract interface javax.lang.model.type.ExecutableType
intf javax.lang.model.type.TypeMirror
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> getParameterTypes()
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> getThrownTypes()
meth public abstract java.util.List<? extends javax.lang.model.type.TypeVariable> getTypeVariables()
meth public abstract javax.lang.model.type.TypeMirror getReceiverType()
meth public abstract javax.lang.model.type.TypeMirror getReturnType()

CLSS public abstract interface javax.lang.model.type.IntersectionType
intf javax.lang.model.type.TypeMirror
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> getBounds()

CLSS public javax.lang.model.type.MirroredTypeException
cons public init(javax.lang.model.type.TypeMirror)
meth public javax.lang.model.type.TypeMirror getTypeMirror()
supr javax.lang.model.type.MirroredTypesException
hfds serialVersionUID,type

CLSS public javax.lang.model.type.MirroredTypesException
cons public init(java.util.List<? extends javax.lang.model.type.TypeMirror>)
meth public java.util.List<? extends javax.lang.model.type.TypeMirror> getTypeMirrors()
supr java.lang.RuntimeException
hfds serialVersionUID,types

CLSS public abstract interface javax.lang.model.type.NoType
intf javax.lang.model.type.TypeMirror

CLSS public abstract interface javax.lang.model.type.NullType
intf javax.lang.model.type.ReferenceType

CLSS public abstract interface javax.lang.model.type.PrimitiveType
intf javax.lang.model.type.TypeMirror

CLSS public abstract interface javax.lang.model.type.ReferenceType
intf javax.lang.model.type.TypeMirror

CLSS public final !enum javax.lang.model.type.TypeKind
fld public final static javax.lang.model.type.TypeKind ARRAY
fld public final static javax.lang.model.type.TypeKind BOOLEAN
fld public final static javax.lang.model.type.TypeKind BYTE
fld public final static javax.lang.model.type.TypeKind CHAR
fld public final static javax.lang.model.type.TypeKind DECLARED
fld public final static javax.lang.model.type.TypeKind DOUBLE
fld public final static javax.lang.model.type.TypeKind ERROR
fld public final static javax.lang.model.type.TypeKind EXECUTABLE
fld public final static javax.lang.model.type.TypeKind FLOAT
fld public final static javax.lang.model.type.TypeKind INT
fld public final static javax.lang.model.type.TypeKind INTERSECTION
fld public final static javax.lang.model.type.TypeKind LONG
fld public final static javax.lang.model.type.TypeKind MODULE
fld public final static javax.lang.model.type.TypeKind NONE
fld public final static javax.lang.model.type.TypeKind NULL
fld public final static javax.lang.model.type.TypeKind OTHER
fld public final static javax.lang.model.type.TypeKind PACKAGE
fld public final static javax.lang.model.type.TypeKind SHORT
fld public final static javax.lang.model.type.TypeKind TYPEVAR
fld public final static javax.lang.model.type.TypeKind UNION
fld public final static javax.lang.model.type.TypeKind VOID
fld public final static javax.lang.model.type.TypeKind WILDCARD
meth public boolean isPrimitive()
meth public static javax.lang.model.type.TypeKind valueOf(java.lang.String)
meth public static javax.lang.model.type.TypeKind[] values()
supr java.lang.Enum<javax.lang.model.type.TypeKind>

CLSS public abstract interface javax.lang.model.type.TypeMirror
intf javax.lang.model.AnnotatedConstruct
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(javax.lang.model.type.TypeVisitor<{%%0},{%%1}>,{%%1})
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getAnnotation(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0}[] getAnnotationsByType(java.lang.Class<{%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.String toString()
meth public abstract java.util.List<? extends javax.lang.model.element.AnnotationMirror> getAnnotationMirrors()
meth public abstract javax.lang.model.type.TypeKind getKind()

CLSS public abstract interface javax.lang.model.type.TypeVariable
intf javax.lang.model.type.ReferenceType
meth public abstract javax.lang.model.element.Element asElement()
meth public abstract javax.lang.model.type.TypeMirror getLowerBound()
meth public abstract javax.lang.model.type.TypeMirror getUpperBound()

CLSS public abstract interface javax.lang.model.type.TypeVisitor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {javax.lang.model.type.TypeVisitor%0} visit(javax.lang.model.type.TypeMirror,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitArray(javax.lang.model.type.ArrayType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitDeclared(javax.lang.model.type.DeclaredType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitError(javax.lang.model.type.ErrorType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitExecutable(javax.lang.model.type.ExecutableType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitIntersection(javax.lang.model.type.IntersectionType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitNoType(javax.lang.model.type.NoType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitNull(javax.lang.model.type.NullType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitPrimitive(javax.lang.model.type.PrimitiveType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitTypeVariable(javax.lang.model.type.TypeVariable,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitUnion(javax.lang.model.type.UnionType,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitUnknown(javax.lang.model.type.TypeMirror,{javax.lang.model.type.TypeVisitor%1})
meth public abstract {javax.lang.model.type.TypeVisitor%0} visitWildcard(javax.lang.model.type.WildcardType,{javax.lang.model.type.TypeVisitor%1})
meth public {javax.lang.model.type.TypeVisitor%0} visit(javax.lang.model.type.TypeMirror)

CLSS public abstract interface javax.lang.model.type.UnionType
intf javax.lang.model.type.TypeMirror
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> getAlternatives()

CLSS public javax.lang.model.type.UnknownTypeException
cons public init(javax.lang.model.type.TypeMirror,java.lang.Object)
meth public java.lang.Object getArgument()
meth public javax.lang.model.type.TypeMirror getUnknownType()
supr javax.lang.model.UnknownEntityException
hfds parameter,serialVersionUID,type

CLSS public abstract interface javax.lang.model.type.WildcardType
intf javax.lang.model.type.TypeMirror
meth public abstract javax.lang.model.type.TypeMirror getExtendsBound()
meth public abstract javax.lang.model.type.TypeMirror getSuperBound()

CLSS public abstract javax.lang.model.util.AbstractAnnotationValueVisitor14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
supr javax.lang.model.util.AbstractAnnotationValueVisitor9<{javax.lang.model.util.AbstractAnnotationValueVisitor14%0},{javax.lang.model.util.AbstractAnnotationValueVisitor14%1}>

CLSS public abstract javax.lang.model.util.AbstractAnnotationValueVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
intf javax.lang.model.element.AnnotationValueVisitor<{javax.lang.model.util.AbstractAnnotationValueVisitor6%0},{javax.lang.model.util.AbstractAnnotationValueVisitor6%1}>
meth public final {javax.lang.model.util.AbstractAnnotationValueVisitor6%0} visit(javax.lang.model.element.AnnotationValue)
meth public final {javax.lang.model.util.AbstractAnnotationValueVisitor6%0} visit(javax.lang.model.element.AnnotationValue,{javax.lang.model.util.AbstractAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.AbstractAnnotationValueVisitor6%0} visitUnknown(javax.lang.model.element.AnnotationValue,{javax.lang.model.util.AbstractAnnotationValueVisitor6%1})
supr java.lang.Object

CLSS public abstract javax.lang.model.util.AbstractAnnotationValueVisitor7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
supr javax.lang.model.util.AbstractAnnotationValueVisitor6<{javax.lang.model.util.AbstractAnnotationValueVisitor7%0},{javax.lang.model.util.AbstractAnnotationValueVisitor7%1}>

CLSS public abstract javax.lang.model.util.AbstractAnnotationValueVisitor8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
supr javax.lang.model.util.AbstractAnnotationValueVisitor7<{javax.lang.model.util.AbstractAnnotationValueVisitor8%0},{javax.lang.model.util.AbstractAnnotationValueVisitor8%1}>

CLSS public abstract javax.lang.model.util.AbstractAnnotationValueVisitor9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
supr javax.lang.model.util.AbstractAnnotationValueVisitor8<{javax.lang.model.util.AbstractAnnotationValueVisitor9%0},{javax.lang.model.util.AbstractAnnotationValueVisitor9%1}>

CLSS public abstract javax.lang.model.util.AbstractAnnotationValueVisitorPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
supr javax.lang.model.util.AbstractAnnotationValueVisitor14<{javax.lang.model.util.AbstractAnnotationValueVisitorPreview%0},{javax.lang.model.util.AbstractAnnotationValueVisitorPreview%1}>

CLSS public abstract javax.lang.model.util.AbstractElementVisitor14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
meth public abstract {javax.lang.model.util.AbstractElementVisitor14%0} visitRecordComponent(javax.lang.model.element.RecordComponentElement,{javax.lang.model.util.AbstractElementVisitor14%1})
supr javax.lang.model.util.AbstractElementVisitor9<{javax.lang.model.util.AbstractElementVisitor14%0},{javax.lang.model.util.AbstractElementVisitor14%1}>

CLSS public abstract javax.lang.model.util.AbstractElementVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
intf javax.lang.model.element.ElementVisitor<{javax.lang.model.util.AbstractElementVisitor6%0},{javax.lang.model.util.AbstractElementVisitor6%1}>
meth public final {javax.lang.model.util.AbstractElementVisitor6%0} visit(javax.lang.model.element.Element)
meth public final {javax.lang.model.util.AbstractElementVisitor6%0} visit(javax.lang.model.element.Element,{javax.lang.model.util.AbstractElementVisitor6%1})
meth public {javax.lang.model.util.AbstractElementVisitor6%0} visitModule(javax.lang.model.element.ModuleElement,{javax.lang.model.util.AbstractElementVisitor6%1})
meth public {javax.lang.model.util.AbstractElementVisitor6%0} visitRecordComponent(javax.lang.model.element.RecordComponentElement,{javax.lang.model.util.AbstractElementVisitor6%1})
meth public {javax.lang.model.util.AbstractElementVisitor6%0} visitUnknown(javax.lang.model.element.Element,{javax.lang.model.util.AbstractElementVisitor6%1})
supr java.lang.Object

CLSS public abstract javax.lang.model.util.AbstractElementVisitor7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
supr javax.lang.model.util.AbstractElementVisitor6<{javax.lang.model.util.AbstractElementVisitor7%0},{javax.lang.model.util.AbstractElementVisitor7%1}>

CLSS public abstract javax.lang.model.util.AbstractElementVisitor8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
supr javax.lang.model.util.AbstractElementVisitor7<{javax.lang.model.util.AbstractElementVisitor8%0},{javax.lang.model.util.AbstractElementVisitor8%1}>

CLSS public abstract javax.lang.model.util.AbstractElementVisitor9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
meth public abstract {javax.lang.model.util.AbstractElementVisitor9%0} visitModule(javax.lang.model.element.ModuleElement,{javax.lang.model.util.AbstractElementVisitor9%1})
supr javax.lang.model.util.AbstractElementVisitor8<{javax.lang.model.util.AbstractElementVisitor9%0},{javax.lang.model.util.AbstractElementVisitor9%1}>

CLSS public abstract javax.lang.model.util.AbstractElementVisitorPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
supr javax.lang.model.util.AbstractElementVisitor14<{javax.lang.model.util.AbstractElementVisitorPreview%0},{javax.lang.model.util.AbstractElementVisitorPreview%1}>

CLSS public abstract javax.lang.model.util.AbstractTypeVisitor14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
supr javax.lang.model.util.AbstractTypeVisitor9<{javax.lang.model.util.AbstractTypeVisitor14%0},{javax.lang.model.util.AbstractTypeVisitor14%1}>

CLSS public abstract javax.lang.model.util.AbstractTypeVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
intf javax.lang.model.type.TypeVisitor<{javax.lang.model.util.AbstractTypeVisitor6%0},{javax.lang.model.util.AbstractTypeVisitor6%1}>
meth public final {javax.lang.model.util.AbstractTypeVisitor6%0} visit(javax.lang.model.type.TypeMirror)
meth public final {javax.lang.model.util.AbstractTypeVisitor6%0} visit(javax.lang.model.type.TypeMirror,{javax.lang.model.util.AbstractTypeVisitor6%1})
meth public {javax.lang.model.util.AbstractTypeVisitor6%0} visitIntersection(javax.lang.model.type.IntersectionType,{javax.lang.model.util.AbstractTypeVisitor6%1})
meth public {javax.lang.model.util.AbstractTypeVisitor6%0} visitUnion(javax.lang.model.type.UnionType,{javax.lang.model.util.AbstractTypeVisitor6%1})
meth public {javax.lang.model.util.AbstractTypeVisitor6%0} visitUnknown(javax.lang.model.type.TypeMirror,{javax.lang.model.util.AbstractTypeVisitor6%1})
supr java.lang.Object

CLSS public abstract javax.lang.model.util.AbstractTypeVisitor7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
meth public abstract {javax.lang.model.util.AbstractTypeVisitor7%0} visitUnion(javax.lang.model.type.UnionType,{javax.lang.model.util.AbstractTypeVisitor7%1})
supr javax.lang.model.util.AbstractTypeVisitor6<{javax.lang.model.util.AbstractTypeVisitor7%0},{javax.lang.model.util.AbstractTypeVisitor7%1}>

CLSS public abstract javax.lang.model.util.AbstractTypeVisitor8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
meth public abstract {javax.lang.model.util.AbstractTypeVisitor8%0} visitIntersection(javax.lang.model.type.IntersectionType,{javax.lang.model.util.AbstractTypeVisitor8%1})
supr javax.lang.model.util.AbstractTypeVisitor7<{javax.lang.model.util.AbstractTypeVisitor8%0},{javax.lang.model.util.AbstractTypeVisitor8%1}>

CLSS public abstract javax.lang.model.util.AbstractTypeVisitor9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
supr javax.lang.model.util.AbstractTypeVisitor8<{javax.lang.model.util.AbstractTypeVisitor9%0},{javax.lang.model.util.AbstractTypeVisitor9%1}>

CLSS public abstract javax.lang.model.util.AbstractTypeVisitorPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
supr javax.lang.model.util.AbstractTypeVisitor14<{javax.lang.model.util.AbstractTypeVisitorPreview%0},{javax.lang.model.util.AbstractTypeVisitorPreview%1}>

CLSS public javax.lang.model.util.ElementFilter
meth public static java.util.List<javax.lang.model.element.ExecutableElement> constructorsIn(java.lang.Iterable<? extends javax.lang.model.element.Element>)
meth public static java.util.List<javax.lang.model.element.ExecutableElement> methodsIn(java.lang.Iterable<? extends javax.lang.model.element.Element>)
meth public static java.util.List<javax.lang.model.element.ModuleElement$ExportsDirective> exportsIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement$Directive>)
meth public static java.util.List<javax.lang.model.element.ModuleElement$OpensDirective> opensIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement$Directive>)
meth public static java.util.List<javax.lang.model.element.ModuleElement$ProvidesDirective> providesIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement$Directive>)
meth public static java.util.List<javax.lang.model.element.ModuleElement$RequiresDirective> requiresIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement$Directive>)
meth public static java.util.List<javax.lang.model.element.ModuleElement$UsesDirective> usesIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement$Directive>)
meth public static java.util.List<javax.lang.model.element.ModuleElement> modulesIn(java.lang.Iterable<? extends javax.lang.model.element.Element>)
meth public static java.util.List<javax.lang.model.element.PackageElement> packagesIn(java.lang.Iterable<? extends javax.lang.model.element.Element>)
meth public static java.util.List<javax.lang.model.element.RecordComponentElement> recordComponentsIn(java.lang.Iterable<? extends javax.lang.model.element.Element>)
meth public static java.util.List<javax.lang.model.element.TypeElement> typesIn(java.lang.Iterable<? extends javax.lang.model.element.Element>)
meth public static java.util.List<javax.lang.model.element.VariableElement> fieldsIn(java.lang.Iterable<? extends javax.lang.model.element.Element>)
meth public static java.util.Set<javax.lang.model.element.ExecutableElement> constructorsIn(java.util.Set<? extends javax.lang.model.element.Element>)
meth public static java.util.Set<javax.lang.model.element.ExecutableElement> methodsIn(java.util.Set<? extends javax.lang.model.element.Element>)
meth public static java.util.Set<javax.lang.model.element.ModuleElement> modulesIn(java.util.Set<? extends javax.lang.model.element.Element>)
meth public static java.util.Set<javax.lang.model.element.PackageElement> packagesIn(java.util.Set<? extends javax.lang.model.element.Element>)
meth public static java.util.Set<javax.lang.model.element.RecordComponentElement> recordComponentsIn(java.util.Set<? extends javax.lang.model.element.Element>)
meth public static java.util.Set<javax.lang.model.element.TypeElement> typesIn(java.util.Set<? extends javax.lang.model.element.Element>)
meth public static java.util.Set<javax.lang.model.element.VariableElement> fieldsIn(java.util.Set<? extends javax.lang.model.element.Element>)
supr java.lang.Object
hfds CONSTRUCTOR_KIND,FIELD_KINDS,METHOD_KIND,MODULE_KIND,PACKAGE_KIND,RECORD_COMPONENT_KIND,TYPE_KINDS

CLSS public javax.lang.model.util.ElementKindVisitor14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.ElementKindVisitor14%0})
meth public {javax.lang.model.util.ElementKindVisitor14%0} visitRecordComponent(javax.lang.model.element.RecordComponentElement,{javax.lang.model.util.ElementKindVisitor14%1})
meth public {javax.lang.model.util.ElementKindVisitor14%0} visitTypeAsRecord(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementKindVisitor14%1})
meth public {javax.lang.model.util.ElementKindVisitor14%0} visitVariableAsBindingVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor14%1})
supr javax.lang.model.util.ElementKindVisitor9<{javax.lang.model.util.ElementKindVisitor14%0},{javax.lang.model.util.ElementKindVisitor14%1}>

CLSS public javax.lang.model.util.ElementKindVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.ElementKindVisitor6%0})
 anno 0 java.lang.Deprecated()
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitExecutable(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitExecutableAsConstructor(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitExecutableAsInstanceInit(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitExecutableAsMethod(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitExecutableAsStaticInit(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitPackage(javax.lang.model.element.PackageElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitType(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitTypeAsAnnotationType(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitTypeAsClass(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitTypeAsEnum(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitTypeAsInterface(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitTypeAsRecord(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitTypeParameter(javax.lang.model.element.TypeParameterElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitVariableAsBindingVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitVariableAsEnumConstant(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitVariableAsExceptionParameter(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitVariableAsField(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitVariableAsLocalVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitVariableAsParameter(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor6%1})
meth public {javax.lang.model.util.ElementKindVisitor6%0} visitVariableAsResourceVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor6%1})
supr javax.lang.model.util.SimpleElementVisitor6<{javax.lang.model.util.ElementKindVisitor6%0},{javax.lang.model.util.ElementKindVisitor6%1}>

CLSS public javax.lang.model.util.ElementKindVisitor7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.ElementKindVisitor7%0})
 anno 0 java.lang.Deprecated()
meth public {javax.lang.model.util.ElementKindVisitor7%0} visitVariableAsResourceVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementKindVisitor7%1})
supr javax.lang.model.util.ElementKindVisitor6<{javax.lang.model.util.ElementKindVisitor7%0},{javax.lang.model.util.ElementKindVisitor7%1}>

CLSS public javax.lang.model.util.ElementKindVisitor8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
cons protected init({javax.lang.model.util.ElementKindVisitor8%0})
supr javax.lang.model.util.ElementKindVisitor7<{javax.lang.model.util.ElementKindVisitor8%0},{javax.lang.model.util.ElementKindVisitor8%1}>

CLSS public javax.lang.model.util.ElementKindVisitor9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
cons protected init({javax.lang.model.util.ElementKindVisitor9%0})
meth public {javax.lang.model.util.ElementKindVisitor9%0} visitModule(javax.lang.model.element.ModuleElement,{javax.lang.model.util.ElementKindVisitor9%1})
supr javax.lang.model.util.ElementKindVisitor8<{javax.lang.model.util.ElementKindVisitor9%0},{javax.lang.model.util.ElementKindVisitor9%1}>

CLSS public javax.lang.model.util.ElementKindVisitorPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.ElementKindVisitorPreview%0})
supr javax.lang.model.util.ElementKindVisitor14<{javax.lang.model.util.ElementKindVisitorPreview%0},{javax.lang.model.util.ElementKindVisitorPreview%1}>

CLSS public javax.lang.model.util.ElementScanner14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.ElementScanner14%0})
meth public {javax.lang.model.util.ElementScanner14%0} visitExecutable(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.ElementScanner14%1})
meth public {javax.lang.model.util.ElementScanner14%0} visitRecordComponent(javax.lang.model.element.RecordComponentElement,{javax.lang.model.util.ElementScanner14%1})
meth public {javax.lang.model.util.ElementScanner14%0} visitType(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementScanner14%1})
supr javax.lang.model.util.ElementScanner9<{javax.lang.model.util.ElementScanner14%0},{javax.lang.model.util.ElementScanner14%1}>

CLSS public javax.lang.model.util.ElementScanner6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.ElementScanner6%0})
 anno 0 java.lang.Deprecated()
fld protected final {javax.lang.model.util.ElementScanner6%0} DEFAULT_VALUE
meth public final {javax.lang.model.util.ElementScanner6%0} scan(java.lang.Iterable<? extends javax.lang.model.element.Element>,{javax.lang.model.util.ElementScanner6%1})
meth public final {javax.lang.model.util.ElementScanner6%0} scan(javax.lang.model.element.Element)
meth public {javax.lang.model.util.ElementScanner6%0} scan(javax.lang.model.element.Element,{javax.lang.model.util.ElementScanner6%1})
meth public {javax.lang.model.util.ElementScanner6%0} visitExecutable(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.ElementScanner6%1})
meth public {javax.lang.model.util.ElementScanner6%0} visitPackage(javax.lang.model.element.PackageElement,{javax.lang.model.util.ElementScanner6%1})
meth public {javax.lang.model.util.ElementScanner6%0} visitRecordComponent(javax.lang.model.element.RecordComponentElement,{javax.lang.model.util.ElementScanner6%1})
meth public {javax.lang.model.util.ElementScanner6%0} visitType(javax.lang.model.element.TypeElement,{javax.lang.model.util.ElementScanner6%1})
meth public {javax.lang.model.util.ElementScanner6%0} visitTypeParameter(javax.lang.model.element.TypeParameterElement,{javax.lang.model.util.ElementScanner6%1})
meth public {javax.lang.model.util.ElementScanner6%0} visitVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementScanner6%1})
supr javax.lang.model.util.AbstractElementVisitor6<{javax.lang.model.util.ElementScanner6%0},{javax.lang.model.util.ElementScanner6%1}>

CLSS public javax.lang.model.util.ElementScanner7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.ElementScanner7%0})
 anno 0 java.lang.Deprecated()
meth public {javax.lang.model.util.ElementScanner7%0} visitVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.ElementScanner7%1})
supr javax.lang.model.util.ElementScanner6<{javax.lang.model.util.ElementScanner7%0},{javax.lang.model.util.ElementScanner7%1}>

CLSS public javax.lang.model.util.ElementScanner8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
cons protected init({javax.lang.model.util.ElementScanner8%0})
supr javax.lang.model.util.ElementScanner7<{javax.lang.model.util.ElementScanner8%0},{javax.lang.model.util.ElementScanner8%1}>

CLSS public javax.lang.model.util.ElementScanner9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
cons protected init({javax.lang.model.util.ElementScanner9%0})
meth public {javax.lang.model.util.ElementScanner9%0} visitModule(javax.lang.model.element.ModuleElement,{javax.lang.model.util.ElementScanner9%1})
supr javax.lang.model.util.ElementScanner8<{javax.lang.model.util.ElementScanner9%0},{javax.lang.model.util.ElementScanner9%1}>

CLSS public javax.lang.model.util.ElementScannerPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.ElementScannerPreview%0})
supr javax.lang.model.util.ElementScanner14<{javax.lang.model.util.ElementScannerPreview%0},{javax.lang.model.util.ElementScannerPreview%1}>

CLSS public abstract interface javax.lang.model.util.Elements
innr public final static !enum DocCommentKind
innr public final static !enum Origin
meth public abstract !varargs void printElements(java.io.Writer,javax.lang.model.element.Element[])
meth public abstract boolean hides(javax.lang.model.element.Element,javax.lang.model.element.Element)
meth public abstract boolean isDeprecated(javax.lang.model.element.Element)
meth public abstract boolean isFunctionalInterface(javax.lang.model.element.TypeElement)
meth public abstract boolean overrides(javax.lang.model.element.ExecutableElement,javax.lang.model.element.ExecutableElement,javax.lang.model.element.TypeElement)
meth public abstract java.lang.String getConstantExpression(java.lang.Object)
meth public abstract java.lang.String getDocComment(javax.lang.model.element.Element)
meth public abstract java.util.List<? extends javax.lang.model.element.AnnotationMirror> getAllAnnotationMirrors(javax.lang.model.element.Element)
meth public abstract java.util.List<? extends javax.lang.model.element.Element> getAllMembers(javax.lang.model.element.TypeElement)
meth public abstract java.util.Map<? extends javax.lang.model.element.ExecutableElement,? extends javax.lang.model.element.AnnotationValue> getElementValuesWithDefaults(javax.lang.model.element.AnnotationMirror)
meth public abstract javax.lang.model.element.Name getBinaryName(javax.lang.model.element.TypeElement)
meth public abstract javax.lang.model.element.Name getName(java.lang.CharSequence)
meth public abstract javax.lang.model.element.PackageElement getPackageElement(java.lang.CharSequence)
meth public abstract javax.lang.model.element.PackageElement getPackageOf(javax.lang.model.element.Element)
meth public abstract javax.lang.model.element.TypeElement getTypeElement(java.lang.CharSequence)
meth public boolean isAutomaticModule(javax.lang.model.element.ModuleElement)
meth public boolean isBridge(javax.lang.model.element.ExecutableElement)
meth public boolean isCanonicalConstructor(javax.lang.model.element.ExecutableElement)
meth public boolean isCompactConstructor(javax.lang.model.element.ExecutableElement)
meth public java.util.Set<? extends javax.lang.model.element.ModuleElement> getAllModuleElements()
meth public java.util.Set<? extends javax.lang.model.element.PackageElement> getAllPackageElements(java.lang.CharSequence)
meth public java.util.Set<? extends javax.lang.model.element.TypeElement> getAllTypeElements(java.lang.CharSequence)
meth public javax.lang.model.element.ModuleElement getModuleElement(java.lang.CharSequence)
meth public javax.lang.model.element.ModuleElement getModuleOf(javax.lang.model.element.Element)
meth public javax.lang.model.element.PackageElement getPackageElement(javax.lang.model.element.ModuleElement,java.lang.CharSequence)
meth public javax.lang.model.element.RecordComponentElement recordComponentFor(javax.lang.model.element.ExecutableElement)
meth public javax.lang.model.element.TypeElement getEnumConstantBody(javax.lang.model.element.VariableElement)
meth public javax.lang.model.element.TypeElement getOutermostTypeElement(javax.lang.model.element.Element)
meth public javax.lang.model.element.TypeElement getTypeElement(javax.lang.model.element.ModuleElement,java.lang.CharSequence)
meth public javax.lang.model.util.Elements$DocCommentKind getDocCommentKind(javax.lang.model.element.Element)
meth public javax.lang.model.util.Elements$Origin getOrigin(javax.lang.model.AnnotatedConstruct,javax.lang.model.element.AnnotationMirror)
meth public javax.lang.model.util.Elements$Origin getOrigin(javax.lang.model.element.Element)
meth public javax.lang.model.util.Elements$Origin getOrigin(javax.lang.model.element.ModuleElement,javax.lang.model.element.ModuleElement$Directive)
meth public javax.tools.JavaFileObject getFileObjectOf(javax.lang.model.element.Element)

CLSS public final static !enum javax.lang.model.util.Elements$DocCommentKind
 outer javax.lang.model.util.Elements
fld public final static javax.lang.model.util.Elements$DocCommentKind END_OF_LINE
fld public final static javax.lang.model.util.Elements$DocCommentKind TRADITIONAL
meth public static javax.lang.model.util.Elements$DocCommentKind valueOf(java.lang.String)
meth public static javax.lang.model.util.Elements$DocCommentKind[] values()
supr java.lang.Enum<javax.lang.model.util.Elements$DocCommentKind>

CLSS public final static !enum javax.lang.model.util.Elements$Origin
 outer javax.lang.model.util.Elements
fld public final static javax.lang.model.util.Elements$Origin EXPLICIT
fld public final static javax.lang.model.util.Elements$Origin MANDATED
fld public final static javax.lang.model.util.Elements$Origin SYNTHETIC
meth public boolean isDeclared()
meth public static javax.lang.model.util.Elements$Origin valueOf(java.lang.String)
meth public static javax.lang.model.util.Elements$Origin[] values()
supr java.lang.Enum<javax.lang.model.util.Elements$Origin>

CLSS public javax.lang.model.util.SimpleAnnotationValueVisitor14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.SimpleAnnotationValueVisitor14%0})
supr javax.lang.model.util.SimpleAnnotationValueVisitor9<{javax.lang.model.util.SimpleAnnotationValueVisitor14%0},{javax.lang.model.util.SimpleAnnotationValueVisitor14%1}>

CLSS public javax.lang.model.util.SimpleAnnotationValueVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.SimpleAnnotationValueVisitor6%0})
 anno 0 java.lang.Deprecated()
fld protected final {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} DEFAULT_VALUE
meth protected {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} defaultAction(java.lang.Object,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitAnnotation(javax.lang.model.element.AnnotationMirror,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitArray(java.util.List<? extends javax.lang.model.element.AnnotationValue>,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitBoolean(boolean,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitByte(byte,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitChar(char,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitDouble(double,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitEnumConstant(javax.lang.model.element.VariableElement,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitFloat(float,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitInt(int,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitLong(long,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitShort(short,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitString(java.lang.String,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
meth public {javax.lang.model.util.SimpleAnnotationValueVisitor6%0} visitType(javax.lang.model.type.TypeMirror,{javax.lang.model.util.SimpleAnnotationValueVisitor6%1})
supr javax.lang.model.util.AbstractAnnotationValueVisitor6<{javax.lang.model.util.SimpleAnnotationValueVisitor6%0},{javax.lang.model.util.SimpleAnnotationValueVisitor6%1}>

CLSS public javax.lang.model.util.SimpleAnnotationValueVisitor7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.SimpleAnnotationValueVisitor7%0})
 anno 0 java.lang.Deprecated()
supr javax.lang.model.util.SimpleAnnotationValueVisitor6<{javax.lang.model.util.SimpleAnnotationValueVisitor7%0},{javax.lang.model.util.SimpleAnnotationValueVisitor7%1}>

CLSS public javax.lang.model.util.SimpleAnnotationValueVisitor8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
cons protected init({javax.lang.model.util.SimpleAnnotationValueVisitor8%0})
supr javax.lang.model.util.SimpleAnnotationValueVisitor7<{javax.lang.model.util.SimpleAnnotationValueVisitor8%0},{javax.lang.model.util.SimpleAnnotationValueVisitor8%1}>

CLSS public javax.lang.model.util.SimpleAnnotationValueVisitor9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
cons protected init({javax.lang.model.util.SimpleAnnotationValueVisitor9%0})
supr javax.lang.model.util.SimpleAnnotationValueVisitor8<{javax.lang.model.util.SimpleAnnotationValueVisitor9%0},{javax.lang.model.util.SimpleAnnotationValueVisitor9%1}>

CLSS public javax.lang.model.util.SimpleAnnotationValueVisitorPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.SimpleAnnotationValueVisitorPreview%0})
supr javax.lang.model.util.SimpleAnnotationValueVisitor14<{javax.lang.model.util.SimpleAnnotationValueVisitorPreview%0},{javax.lang.model.util.SimpleAnnotationValueVisitorPreview%1}>

CLSS public javax.lang.model.util.SimpleElementVisitor14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.SimpleElementVisitor14%0})
meth public {javax.lang.model.util.SimpleElementVisitor14%0} visitRecordComponent(javax.lang.model.element.RecordComponentElement,{javax.lang.model.util.SimpleElementVisitor14%1})
supr javax.lang.model.util.SimpleElementVisitor9<{javax.lang.model.util.SimpleElementVisitor14%0},{javax.lang.model.util.SimpleElementVisitor14%1}>

CLSS public javax.lang.model.util.SimpleElementVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.SimpleElementVisitor6%0})
 anno 0 java.lang.Deprecated()
fld protected final {javax.lang.model.util.SimpleElementVisitor6%0} DEFAULT_VALUE
meth protected {javax.lang.model.util.SimpleElementVisitor6%0} defaultAction(javax.lang.model.element.Element,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitExecutable(javax.lang.model.element.ExecutableElement,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitPackage(javax.lang.model.element.PackageElement,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitType(javax.lang.model.element.TypeElement,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitTypeParameter(javax.lang.model.element.TypeParameterElement,{javax.lang.model.util.SimpleElementVisitor6%1})
meth public {javax.lang.model.util.SimpleElementVisitor6%0} visitVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.SimpleElementVisitor6%1})
supr javax.lang.model.util.AbstractElementVisitor6<{javax.lang.model.util.SimpleElementVisitor6%0},{javax.lang.model.util.SimpleElementVisitor6%1}>

CLSS public javax.lang.model.util.SimpleElementVisitor7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.SimpleElementVisitor7%0})
 anno 0 java.lang.Deprecated()
meth public {javax.lang.model.util.SimpleElementVisitor7%0} visitVariable(javax.lang.model.element.VariableElement,{javax.lang.model.util.SimpleElementVisitor7%1})
supr javax.lang.model.util.SimpleElementVisitor6<{javax.lang.model.util.SimpleElementVisitor7%0},{javax.lang.model.util.SimpleElementVisitor7%1}>

CLSS public javax.lang.model.util.SimpleElementVisitor8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
cons protected init({javax.lang.model.util.SimpleElementVisitor8%0})
supr javax.lang.model.util.SimpleElementVisitor7<{javax.lang.model.util.SimpleElementVisitor8%0},{javax.lang.model.util.SimpleElementVisitor8%1}>

CLSS public javax.lang.model.util.SimpleElementVisitor9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
cons protected init({javax.lang.model.util.SimpleElementVisitor9%0})
meth public {javax.lang.model.util.SimpleElementVisitor9%0} visitModule(javax.lang.model.element.ModuleElement,{javax.lang.model.util.SimpleElementVisitor9%1})
supr javax.lang.model.util.SimpleElementVisitor8<{javax.lang.model.util.SimpleElementVisitor9%0},{javax.lang.model.util.SimpleElementVisitor9%1}>

CLSS public javax.lang.model.util.SimpleElementVisitorPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.SimpleElementVisitorPreview%0})
supr javax.lang.model.util.SimpleElementVisitor14<{javax.lang.model.util.SimpleElementVisitorPreview%0},{javax.lang.model.util.SimpleElementVisitorPreview%1}>

CLSS public javax.lang.model.util.SimpleTypeVisitor14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.SimpleTypeVisitor14%0})
supr javax.lang.model.util.SimpleTypeVisitor9<{javax.lang.model.util.SimpleTypeVisitor14%0},{javax.lang.model.util.SimpleTypeVisitor14%1}>

CLSS public javax.lang.model.util.SimpleTypeVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.SimpleTypeVisitor6%0})
 anno 0 java.lang.Deprecated()
fld protected final {javax.lang.model.util.SimpleTypeVisitor6%0} DEFAULT_VALUE
meth protected {javax.lang.model.util.SimpleTypeVisitor6%0} defaultAction(javax.lang.model.type.TypeMirror,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitArray(javax.lang.model.type.ArrayType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitDeclared(javax.lang.model.type.DeclaredType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitError(javax.lang.model.type.ErrorType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitExecutable(javax.lang.model.type.ExecutableType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitNoType(javax.lang.model.type.NoType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitNull(javax.lang.model.type.NullType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitPrimitive(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitTypeVariable(javax.lang.model.type.TypeVariable,{javax.lang.model.util.SimpleTypeVisitor6%1})
meth public {javax.lang.model.util.SimpleTypeVisitor6%0} visitWildcard(javax.lang.model.type.WildcardType,{javax.lang.model.util.SimpleTypeVisitor6%1})
supr javax.lang.model.util.AbstractTypeVisitor6<{javax.lang.model.util.SimpleTypeVisitor6%0},{javax.lang.model.util.SimpleTypeVisitor6%1}>

CLSS public javax.lang.model.util.SimpleTypeVisitor7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.SimpleTypeVisitor7%0})
 anno 0 java.lang.Deprecated()
meth public {javax.lang.model.util.SimpleTypeVisitor7%0} visitUnion(javax.lang.model.type.UnionType,{javax.lang.model.util.SimpleTypeVisitor7%1})
supr javax.lang.model.util.SimpleTypeVisitor6<{javax.lang.model.util.SimpleTypeVisitor7%0},{javax.lang.model.util.SimpleTypeVisitor7%1}>

CLSS public javax.lang.model.util.SimpleTypeVisitor8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
cons protected init({javax.lang.model.util.SimpleTypeVisitor8%0})
meth public {javax.lang.model.util.SimpleTypeVisitor8%0} visitIntersection(javax.lang.model.type.IntersectionType,{javax.lang.model.util.SimpleTypeVisitor8%1})
supr javax.lang.model.util.SimpleTypeVisitor7<{javax.lang.model.util.SimpleTypeVisitor8%0},{javax.lang.model.util.SimpleTypeVisitor8%1}>

CLSS public javax.lang.model.util.SimpleTypeVisitor9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
cons protected init({javax.lang.model.util.SimpleTypeVisitor9%0})
supr javax.lang.model.util.SimpleTypeVisitor8<{javax.lang.model.util.SimpleTypeVisitor9%0},{javax.lang.model.util.SimpleTypeVisitor9%1}>

CLSS public javax.lang.model.util.SimpleTypeVisitorPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.SimpleTypeVisitorPreview%0})
supr javax.lang.model.util.SimpleTypeVisitor14<{javax.lang.model.util.SimpleTypeVisitorPreview%0},{javax.lang.model.util.SimpleTypeVisitorPreview%1}>

CLSS public javax.lang.model.util.TypeKindVisitor14<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.TypeKindVisitor14%0})
supr javax.lang.model.util.TypeKindVisitor9<{javax.lang.model.util.TypeKindVisitor14%0},{javax.lang.model.util.TypeKindVisitor14%1}>

CLSS public javax.lang.model.util.TypeKindVisitor6<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_6)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.TypeKindVisitor6%0})
 anno 0 java.lang.Deprecated()
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitNoType(javax.lang.model.type.NoType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitNoTypeAsModule(javax.lang.model.type.NoType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitNoTypeAsNone(javax.lang.model.type.NoType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitNoTypeAsPackage(javax.lang.model.type.NoType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitNoTypeAsVoid(javax.lang.model.type.NoType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitive(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitiveAsBoolean(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitiveAsByte(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitiveAsChar(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitiveAsDouble(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitiveAsFloat(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitiveAsInt(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitiveAsLong(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
meth public {javax.lang.model.util.TypeKindVisitor6%0} visitPrimitiveAsShort(javax.lang.model.type.PrimitiveType,{javax.lang.model.util.TypeKindVisitor6%1})
supr javax.lang.model.util.SimpleTypeVisitor6<{javax.lang.model.util.TypeKindVisitor6%0},{javax.lang.model.util.TypeKindVisitor6%1}>

CLSS public javax.lang.model.util.TypeKindVisitor7<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_7)
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init({javax.lang.model.util.TypeKindVisitor7%0})
 anno 0 java.lang.Deprecated()
meth public {javax.lang.model.util.TypeKindVisitor7%0} visitUnion(javax.lang.model.type.UnionType,{javax.lang.model.util.TypeKindVisitor7%1})
supr javax.lang.model.util.TypeKindVisitor6<{javax.lang.model.util.TypeKindVisitor7%0},{javax.lang.model.util.TypeKindVisitor7%1}>

CLSS public javax.lang.model.util.TypeKindVisitor8<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_8)
cons protected init()
cons protected init({javax.lang.model.util.TypeKindVisitor8%0})
meth public {javax.lang.model.util.TypeKindVisitor8%0} visitIntersection(javax.lang.model.type.IntersectionType,{javax.lang.model.util.TypeKindVisitor8%1})
supr javax.lang.model.util.TypeKindVisitor7<{javax.lang.model.util.TypeKindVisitor8%0},{javax.lang.model.util.TypeKindVisitor8%1}>

CLSS public javax.lang.model.util.TypeKindVisitor9<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_14)
cons protected init()
cons protected init({javax.lang.model.util.TypeKindVisitor9%0})
meth public {javax.lang.model.util.TypeKindVisitor9%0} visitNoTypeAsModule(javax.lang.model.type.NoType,{javax.lang.model.util.TypeKindVisitor9%1})
supr javax.lang.model.util.TypeKindVisitor8<{javax.lang.model.util.TypeKindVisitor9%0},{javax.lang.model.util.TypeKindVisitor9%1}>

CLSS public javax.lang.model.util.TypeKindVisitorPreview<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion value=RELEASE_24)
cons protected init()
cons protected init({javax.lang.model.util.TypeKindVisitorPreview%0})
supr javax.lang.model.util.TypeKindVisitor14<{javax.lang.model.util.TypeKindVisitorPreview%0},{javax.lang.model.util.TypeKindVisitorPreview%1}>

CLSS public abstract interface javax.lang.model.util.Types
meth public <%0 extends javax.lang.model.type.TypeMirror> {%%0} stripAnnotations({%%0})
meth public abstract !varargs javax.lang.model.type.DeclaredType getDeclaredType(javax.lang.model.element.TypeElement,javax.lang.model.type.TypeMirror[])
meth public abstract !varargs javax.lang.model.type.DeclaredType getDeclaredType(javax.lang.model.type.DeclaredType,javax.lang.model.element.TypeElement,javax.lang.model.type.TypeMirror[])
meth public abstract boolean contains(javax.lang.model.type.TypeMirror,javax.lang.model.type.TypeMirror)
meth public abstract boolean isAssignable(javax.lang.model.type.TypeMirror,javax.lang.model.type.TypeMirror)
meth public abstract boolean isSameType(javax.lang.model.type.TypeMirror,javax.lang.model.type.TypeMirror)
meth public abstract boolean isSubsignature(javax.lang.model.type.ExecutableType,javax.lang.model.type.ExecutableType)
meth public abstract boolean isSubtype(javax.lang.model.type.TypeMirror,javax.lang.model.type.TypeMirror)
meth public abstract java.util.List<? extends javax.lang.model.type.TypeMirror> directSupertypes(javax.lang.model.type.TypeMirror)
meth public abstract javax.lang.model.element.Element asElement(javax.lang.model.type.TypeMirror)
meth public abstract javax.lang.model.element.TypeElement boxedClass(javax.lang.model.type.PrimitiveType)
meth public abstract javax.lang.model.type.ArrayType getArrayType(javax.lang.model.type.TypeMirror)
meth public abstract javax.lang.model.type.NoType getNoType(javax.lang.model.type.TypeKind)
meth public abstract javax.lang.model.type.NullType getNullType()
meth public abstract javax.lang.model.type.PrimitiveType getPrimitiveType(javax.lang.model.type.TypeKind)
meth public abstract javax.lang.model.type.PrimitiveType unboxedType(javax.lang.model.type.TypeMirror)
meth public abstract javax.lang.model.type.TypeMirror asMemberOf(javax.lang.model.type.DeclaredType,javax.lang.model.element.Element)
meth public abstract javax.lang.model.type.TypeMirror capture(javax.lang.model.type.TypeMirror)
meth public abstract javax.lang.model.type.TypeMirror erasure(javax.lang.model.type.TypeMirror)
meth public abstract javax.lang.model.type.WildcardType getWildcardType(javax.lang.model.type.TypeMirror,javax.lang.model.type.TypeMirror)

CLSS public abstract interface javax.tools.Diagnostic<%0 extends java.lang.Object>
fld public final static long NOPOS = -1
innr public final static !enum Kind
meth public abstract java.lang.String getCode()
meth public abstract java.lang.String getMessage(java.util.Locale)
meth public abstract javax.tools.Diagnostic$Kind getKind()
meth public abstract long getColumnNumber()
meth public abstract long getEndPosition()
meth public abstract long getLineNumber()
meth public abstract long getPosition()
meth public abstract long getStartPosition()
meth public abstract {javax.tools.Diagnostic%0} getSource()

CLSS public final static !enum javax.tools.Diagnostic$Kind
 outer javax.tools.Diagnostic
fld public final static javax.tools.Diagnostic$Kind ERROR
fld public final static javax.tools.Diagnostic$Kind MANDATORY_WARNING
fld public final static javax.tools.Diagnostic$Kind NOTE
fld public final static javax.tools.Diagnostic$Kind OTHER
fld public final static javax.tools.Diagnostic$Kind WARNING
meth public static javax.tools.Diagnostic$Kind valueOf(java.lang.String)
meth public static javax.tools.Diagnostic$Kind[] values()
supr java.lang.Enum<javax.tools.Diagnostic$Kind>

CLSS public final javax.tools.DiagnosticCollector<%0 extends java.lang.Object>
cons public init()
intf javax.tools.DiagnosticListener<{javax.tools.DiagnosticCollector%0}>
meth public java.util.List<javax.tools.Diagnostic<? extends {javax.tools.DiagnosticCollector%0}>> getDiagnostics()
meth public void report(javax.tools.Diagnostic<? extends {javax.tools.DiagnosticCollector%0}>)
supr java.lang.Object
hfds diagnostics

CLSS public abstract interface javax.tools.DiagnosticListener<%0 extends java.lang.Object>
meth public abstract void report(javax.tools.Diagnostic<? extends {javax.tools.DiagnosticListener%0}>)

CLSS public abstract interface javax.tools.DocumentationTool
innr public abstract interface static DocumentationTask
innr public final static !enum Location
intf javax.tools.OptionChecker
intf javax.tools.Tool
meth public abstract javax.tools.DocumentationTool$DocumentationTask getTask(java.io.Writer,javax.tools.JavaFileManager,javax.tools.DiagnosticListener<? super javax.tools.JavaFileObject>,java.lang.Class<?>,java.lang.Iterable<java.lang.String>,java.lang.Iterable<? extends javax.tools.JavaFileObject>)
meth public abstract javax.tools.StandardJavaFileManager getStandardFileManager(javax.tools.DiagnosticListener<? super javax.tools.JavaFileObject>,java.util.Locale,java.nio.charset.Charset)

CLSS public abstract interface static javax.tools.DocumentationTool$DocumentationTask
 outer javax.tools.DocumentationTool
intf java.util.concurrent.Callable<java.lang.Boolean>
meth public abstract java.lang.Boolean call()
meth public abstract void addModules(java.lang.Iterable<java.lang.String>)
meth public abstract void setLocale(java.util.Locale)

CLSS public final static !enum javax.tools.DocumentationTool$Location
 outer javax.tools.DocumentationTool
fld public final static javax.tools.DocumentationTool$Location DOCLET_PATH
fld public final static javax.tools.DocumentationTool$Location DOCUMENTATION_OUTPUT
fld public final static javax.tools.DocumentationTool$Location SNIPPET_PATH
fld public final static javax.tools.DocumentationTool$Location TAGLET_PATH
intf javax.tools.JavaFileManager$Location
meth public boolean isOutputLocation()
meth public java.lang.String getName()
meth public static javax.tools.DocumentationTool$Location valueOf(java.lang.String)
meth public static javax.tools.DocumentationTool$Location[] values()
supr java.lang.Enum<javax.tools.DocumentationTool$Location>

CLSS public abstract interface javax.tools.FileObject
meth public abstract boolean delete()
meth public abstract java.io.InputStream openInputStream() throws java.io.IOException
meth public abstract java.io.OutputStream openOutputStream() throws java.io.IOException
meth public abstract java.io.Reader openReader(boolean) throws java.io.IOException
meth public abstract java.io.Writer openWriter() throws java.io.IOException
meth public abstract java.lang.CharSequence getCharContent(boolean) throws java.io.IOException
meth public abstract java.lang.String getName()
meth public abstract java.net.URI toUri()
meth public abstract long getLastModified()

CLSS public javax.tools.ForwardingFileObject<%0 extends javax.tools.FileObject>
cons protected init({javax.tools.ForwardingFileObject%0})
fld protected final {javax.tools.ForwardingFileObject%0} fileObject
intf javax.tools.FileObject
meth public boolean delete()
meth public java.io.InputStream openInputStream() throws java.io.IOException
meth public java.io.OutputStream openOutputStream() throws java.io.IOException
meth public java.io.Reader openReader(boolean) throws java.io.IOException
meth public java.io.Writer openWriter() throws java.io.IOException
meth public java.lang.CharSequence getCharContent(boolean) throws java.io.IOException
meth public java.lang.String getName()
meth public java.net.URI toUri()
meth public long getLastModified()
supr java.lang.Object

CLSS public javax.tools.ForwardingJavaFileManager<%0 extends javax.tools.JavaFileManager>
cons protected init({javax.tools.ForwardingJavaFileManager%0})
fld protected final {javax.tools.ForwardingJavaFileManager%0} fileManager
intf javax.tools.JavaFileManager
meth public !varargs javax.tools.FileObject getFileForOutputForOriginatingFiles(javax.tools.JavaFileManager$Location,java.lang.String,java.lang.String,javax.tools.FileObject[]) throws java.io.IOException
meth public !varargs javax.tools.JavaFileObject getJavaFileForOutputForOriginatingFiles(javax.tools.JavaFileManager$Location,java.lang.String,javax.tools.JavaFileObject$Kind,javax.tools.FileObject[]) throws java.io.IOException
meth public <%0 extends java.lang.Object> java.util.ServiceLoader<{%%0}> getServiceLoader(javax.tools.JavaFileManager$Location,java.lang.Class<{%%0}>) throws java.io.IOException
meth public boolean contains(javax.tools.JavaFileManager$Location,javax.tools.FileObject) throws java.io.IOException
meth public boolean handleOption(java.lang.String,java.util.Iterator<java.lang.String>)
meth public boolean hasLocation(javax.tools.JavaFileManager$Location)
meth public boolean isSameFile(javax.tools.FileObject,javax.tools.FileObject)
meth public int isSupportedOption(java.lang.String)
meth public java.lang.ClassLoader getClassLoader(javax.tools.JavaFileManager$Location)
meth public java.lang.Iterable<java.util.Set<javax.tools.JavaFileManager$Location>> listLocationsForModules(javax.tools.JavaFileManager$Location) throws java.io.IOException
meth public java.lang.Iterable<javax.tools.JavaFileObject> list(javax.tools.JavaFileManager$Location,java.lang.String,java.util.Set<javax.tools.JavaFileObject$Kind>,boolean) throws java.io.IOException
meth public java.lang.String inferBinaryName(javax.tools.JavaFileManager$Location,javax.tools.JavaFileObject)
meth public java.lang.String inferModuleName(javax.tools.JavaFileManager$Location) throws java.io.IOException
meth public javax.tools.FileObject getFileForInput(javax.tools.JavaFileManager$Location,java.lang.String,java.lang.String) throws java.io.IOException
meth public javax.tools.FileObject getFileForOutput(javax.tools.JavaFileManager$Location,java.lang.String,java.lang.String,javax.tools.FileObject) throws java.io.IOException
meth public javax.tools.JavaFileManager$Location getLocationForModule(javax.tools.JavaFileManager$Location,java.lang.String) throws java.io.IOException
meth public javax.tools.JavaFileManager$Location getLocationForModule(javax.tools.JavaFileManager$Location,javax.tools.JavaFileObject) throws java.io.IOException
meth public javax.tools.JavaFileObject getJavaFileForInput(javax.tools.JavaFileManager$Location,java.lang.String,javax.tools.JavaFileObject$Kind) throws java.io.IOException
meth public javax.tools.JavaFileObject getJavaFileForOutput(javax.tools.JavaFileManager$Location,java.lang.String,javax.tools.JavaFileObject$Kind,javax.tools.FileObject) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
supr java.lang.Object

CLSS public javax.tools.ForwardingJavaFileObject<%0 extends javax.tools.JavaFileObject>
cons protected init({javax.tools.ForwardingJavaFileObject%0})
intf javax.tools.JavaFileObject
meth public boolean isNameCompatible(java.lang.String,javax.tools.JavaFileObject$Kind)
meth public javax.lang.model.element.Modifier getAccessLevel()
meth public javax.lang.model.element.NestingKind getNestingKind()
meth public javax.tools.JavaFileObject$Kind getKind()
supr javax.tools.ForwardingFileObject<{javax.tools.ForwardingJavaFileObject%0}>

CLSS public abstract interface javax.tools.JavaCompiler
innr public abstract interface static CompilationTask
intf javax.tools.OptionChecker
intf javax.tools.Tool
meth public abstract javax.tools.JavaCompiler$CompilationTask getTask(java.io.Writer,javax.tools.JavaFileManager,javax.tools.DiagnosticListener<? super javax.tools.JavaFileObject>,java.lang.Iterable<java.lang.String>,java.lang.Iterable<java.lang.String>,java.lang.Iterable<? extends javax.tools.JavaFileObject>)
meth public abstract javax.tools.StandardJavaFileManager getStandardFileManager(javax.tools.DiagnosticListener<? super javax.tools.JavaFileObject>,java.util.Locale,java.nio.charset.Charset)

CLSS public abstract interface static javax.tools.JavaCompiler$CompilationTask
 outer javax.tools.JavaCompiler
intf java.util.concurrent.Callable<java.lang.Boolean>
meth public abstract java.lang.Boolean call()
meth public abstract void addModules(java.lang.Iterable<java.lang.String>)
meth public abstract void setLocale(java.util.Locale)
meth public abstract void setProcessors(java.lang.Iterable<? extends javax.annotation.processing.Processor>)

CLSS public abstract interface javax.tools.JavaFileManager
innr public abstract interface static Location
intf java.io.Closeable
intf java.io.Flushable
intf javax.tools.OptionChecker
meth public !varargs javax.tools.FileObject getFileForOutputForOriginatingFiles(javax.tools.JavaFileManager$Location,java.lang.String,java.lang.String,javax.tools.FileObject[]) throws java.io.IOException
meth public !varargs javax.tools.JavaFileObject getJavaFileForOutputForOriginatingFiles(javax.tools.JavaFileManager$Location,java.lang.String,javax.tools.JavaFileObject$Kind,javax.tools.FileObject[]) throws java.io.IOException
meth public <%0 extends java.lang.Object> java.util.ServiceLoader<{%%0}> getServiceLoader(javax.tools.JavaFileManager$Location,java.lang.Class<{%%0}>) throws java.io.IOException
meth public abstract boolean handleOption(java.lang.String,java.util.Iterator<java.lang.String>)
meth public abstract boolean hasLocation(javax.tools.JavaFileManager$Location)
meth public abstract boolean isSameFile(javax.tools.FileObject,javax.tools.FileObject)
meth public abstract java.lang.ClassLoader getClassLoader(javax.tools.JavaFileManager$Location)
meth public abstract java.lang.Iterable<javax.tools.JavaFileObject> list(javax.tools.JavaFileManager$Location,java.lang.String,java.util.Set<javax.tools.JavaFileObject$Kind>,boolean) throws java.io.IOException
meth public abstract java.lang.String inferBinaryName(javax.tools.JavaFileManager$Location,javax.tools.JavaFileObject)
meth public abstract javax.tools.FileObject getFileForInput(javax.tools.JavaFileManager$Location,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract javax.tools.FileObject getFileForOutput(javax.tools.JavaFileManager$Location,java.lang.String,java.lang.String,javax.tools.FileObject) throws java.io.IOException
meth public abstract javax.tools.JavaFileObject getJavaFileForInput(javax.tools.JavaFileManager$Location,java.lang.String,javax.tools.JavaFileObject$Kind) throws java.io.IOException
meth public abstract javax.tools.JavaFileObject getJavaFileForOutput(javax.tools.JavaFileManager$Location,java.lang.String,javax.tools.JavaFileObject$Kind,javax.tools.FileObject) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public boolean contains(javax.tools.JavaFileManager$Location,javax.tools.FileObject) throws java.io.IOException
meth public java.lang.Iterable<java.util.Set<javax.tools.JavaFileManager$Location>> listLocationsForModules(javax.tools.JavaFileManager$Location) throws java.io.IOException
meth public java.lang.String inferModuleName(javax.tools.JavaFileManager$Location) throws java.io.IOException
meth public javax.tools.JavaFileManager$Location getLocationForModule(javax.tools.JavaFileManager$Location,java.lang.String) throws java.io.IOException
meth public javax.tools.JavaFileManager$Location getLocationForModule(javax.tools.JavaFileManager$Location,javax.tools.JavaFileObject) throws java.io.IOException

CLSS public abstract interface static javax.tools.JavaFileManager$Location
 outer javax.tools.JavaFileManager
meth public abstract boolean isOutputLocation()
meth public abstract java.lang.String getName()
meth public boolean isModuleOrientedLocation()

CLSS public abstract interface javax.tools.JavaFileObject
innr public final static !enum Kind
intf javax.tools.FileObject
meth public abstract boolean isNameCompatible(java.lang.String,javax.tools.JavaFileObject$Kind)
meth public abstract javax.lang.model.element.Modifier getAccessLevel()
meth public abstract javax.lang.model.element.NestingKind getNestingKind()
meth public abstract javax.tools.JavaFileObject$Kind getKind()

CLSS public final static !enum javax.tools.JavaFileObject$Kind
 outer javax.tools.JavaFileObject
fld public final java.lang.String extension
fld public final static javax.tools.JavaFileObject$Kind CLASS
fld public final static javax.tools.JavaFileObject$Kind HTML
fld public final static javax.tools.JavaFileObject$Kind OTHER
fld public final static javax.tools.JavaFileObject$Kind SOURCE
meth public static javax.tools.JavaFileObject$Kind valueOf(java.lang.String)
meth public static javax.tools.JavaFileObject$Kind[] values()
supr java.lang.Enum<javax.tools.JavaFileObject$Kind>

CLSS public abstract interface javax.tools.OptionChecker
meth public abstract int isSupportedOption(java.lang.String)

CLSS public javax.tools.SimpleJavaFileObject
cons protected init(java.net.URI,javax.tools.JavaFileObject$Kind)
fld protected final java.net.URI uri
fld protected final javax.tools.JavaFileObject$Kind kind
intf javax.tools.JavaFileObject
meth public boolean delete()
meth public boolean isNameCompatible(java.lang.String,javax.tools.JavaFileObject$Kind)
meth public java.io.InputStream openInputStream() throws java.io.IOException
meth public java.io.OutputStream openOutputStream() throws java.io.IOException
meth public java.io.Reader openReader(boolean) throws java.io.IOException
meth public java.io.Writer openWriter() throws java.io.IOException
meth public java.lang.CharSequence getCharContent(boolean) throws java.io.IOException
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.net.URI toUri()
meth public javax.lang.model.element.Modifier getAccessLevel()
meth public javax.lang.model.element.NestingKind getNestingKind()
meth public javax.tools.JavaFileObject$Kind getKind()
meth public long getLastModified()
meth public static javax.tools.JavaFileObject forSource(java.net.URI,java.lang.String)
supr java.lang.Object

CLSS public abstract interface javax.tools.StandardJavaFileManager
innr public abstract interface static PathFactory
intf javax.tools.JavaFileManager
meth public !varargs java.lang.Iterable<? extends javax.tools.JavaFileObject> getJavaFileObjects(java.nio.file.Path[])
meth public abstract !varargs java.lang.Iterable<? extends javax.tools.JavaFileObject> getJavaFileObjects(java.io.File[])
meth public abstract !varargs java.lang.Iterable<? extends javax.tools.JavaFileObject> getJavaFileObjects(java.lang.String[])
meth public abstract boolean isSameFile(javax.tools.FileObject,javax.tools.FileObject)
meth public abstract java.lang.Iterable<? extends java.io.File> getLocation(javax.tools.JavaFileManager$Location)
meth public abstract java.lang.Iterable<? extends javax.tools.JavaFileObject> getJavaFileObjectsFromFiles(java.lang.Iterable<? extends java.io.File>)
meth public abstract java.lang.Iterable<? extends javax.tools.JavaFileObject> getJavaFileObjectsFromStrings(java.lang.Iterable<java.lang.String>)
meth public abstract void setLocation(javax.tools.JavaFileManager$Location,java.lang.Iterable<? extends java.io.File>) throws java.io.IOException
meth public java.lang.Iterable<? extends java.nio.file.Path> getLocationAsPaths(javax.tools.JavaFileManager$Location)
meth public java.lang.Iterable<? extends javax.tools.JavaFileObject> getJavaFileObjectsFromPaths(java.lang.Iterable<? extends java.nio.file.Path>)
 anno 0 java.lang.Deprecated()
meth public java.lang.Iterable<? extends javax.tools.JavaFileObject> getJavaFileObjectsFromPaths(java.util.Collection<? extends java.nio.file.Path>)
meth public java.nio.file.Path asPath(javax.tools.FileObject)
meth public void setLocationForModule(javax.tools.JavaFileManager$Location,java.lang.String,java.util.Collection<? extends java.nio.file.Path>) throws java.io.IOException
meth public void setLocationFromPaths(javax.tools.JavaFileManager$Location,java.util.Collection<? extends java.nio.file.Path>) throws java.io.IOException
meth public void setPathFactory(javax.tools.StandardJavaFileManager$PathFactory)

CLSS public abstract interface static javax.tools.StandardJavaFileManager$PathFactory
 outer javax.tools.StandardJavaFileManager
meth public abstract !varargs java.nio.file.Path getPath(java.lang.String,java.lang.String[])

CLSS public final !enum javax.tools.StandardLocation
fld public final static javax.tools.StandardLocation ANNOTATION_PROCESSOR_MODULE_PATH
fld public final static javax.tools.StandardLocation ANNOTATION_PROCESSOR_PATH
fld public final static javax.tools.StandardLocation CLASS_OUTPUT
fld public final static javax.tools.StandardLocation CLASS_PATH
fld public final static javax.tools.StandardLocation MODULE_PATH
fld public final static javax.tools.StandardLocation MODULE_SOURCE_PATH
fld public final static javax.tools.StandardLocation NATIVE_HEADER_OUTPUT
fld public final static javax.tools.StandardLocation PATCH_MODULE_PATH
fld public final static javax.tools.StandardLocation PLATFORM_CLASS_PATH
fld public final static javax.tools.StandardLocation SOURCE_OUTPUT
fld public final static javax.tools.StandardLocation SOURCE_PATH
fld public final static javax.tools.StandardLocation SYSTEM_MODULES
fld public final static javax.tools.StandardLocation UPGRADE_MODULE_PATH
intf javax.tools.JavaFileManager$Location
meth public boolean isModuleOrientedLocation()
meth public boolean isOutputLocation()
meth public java.lang.String getName()
meth public static javax.tools.JavaFileManager$Location locationFor(java.lang.String)
meth public static javax.tools.StandardLocation valueOf(java.lang.String)
meth public static javax.tools.StandardLocation[] values()
supr java.lang.Enum<javax.tools.StandardLocation>
hfds locations

CLSS public abstract interface javax.tools.Tool
meth public abstract !varargs int run(java.io.InputStream,java.io.OutputStream,java.io.OutputStream,java.lang.String[])
meth public abstract java.util.Set<javax.lang.model.SourceVersion> getSourceVersions()
meth public java.lang.String name()

CLSS public javax.tools.ToolProvider
meth public static java.lang.ClassLoader getSystemToolClassLoader()
 anno 0 java.lang.Deprecated()
meth public static javax.tools.DocumentationTool getSystemDocumentationTool()
meth public static javax.tools.JavaCompiler getSystemJavaCompiler()
supr java.lang.Object
hfds systemDocumentationToolModule,systemDocumentationToolName,systemJavaCompilerModule,systemJavaCompilerName

