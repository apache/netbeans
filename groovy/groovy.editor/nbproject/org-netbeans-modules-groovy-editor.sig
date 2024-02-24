#Signature file v4.1
#Version 1.92

CLSS public abstract interface java.io.Serializable

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

CLSS public org.codehaus.groovy.ast.ASTNode
cons public init()
intf org.codehaus.groovy.ast.NodeMetaDataHandler
meth public int getColumnNumber()
meth public int getLastColumnNumber()
meth public int getLastLineNumber()
meth public int getLineNumber()
meth public java.lang.String getText()
meth public java.util.Map<?,?> getMetaDataMap()
meth public void copyNodeMetaData(org.codehaus.groovy.ast.ASTNode)
meth public void setColumnNumber(int)
meth public void setLastColumnNumber(int)
meth public void setLastLineNumber(int)
meth public void setLineNumber(int)
meth public void setMetaDataMap(java.util.Map<?,?>)
meth public void setSourcePosition(org.codehaus.groovy.ast.ASTNode)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr java.lang.Object
hfds columnNumber,lastColumnNumber,lastLineNumber,lineNumber,metaDataMap

CLSS public abstract org.codehaus.groovy.ast.ClassCodeVisitorSupport
cons public init()
intf org.codehaus.groovy.ast.GroovyClassVisitor
intf org.codehaus.groovy.transform.ErrorCollecting
meth protected abstract org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitAnnotation(org.codehaus.groovy.ast.AnnotationNode)
meth protected void visitClassCodeContainer(org.codehaus.groovy.ast.stmt.Statement)
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth protected void visitObjectInitializerStatements(org.codehaus.groovy.ast.ClassNode)
meth protected void visitStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitImports(org.codehaus.groovy.ast.ModuleNode)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitPackage(org.codehaus.groovy.ast.PackageNode)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr org.codehaus.groovy.ast.CodeVisitorSupport

CLSS public abstract org.codehaus.groovy.ast.CodeVisitorSupport
cons public init()
intf org.codehaus.groovy.ast.GroovyCodeVisitor
meth protected void afterSwitchConditionExpressionVisited(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitArgumentlistExpression(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public void visitArrayExpression(org.codehaus.groovy.ast.expr.ArrayExpression)
meth public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public void visitBytecodeExpression(org.codehaus.groovy.classgen.BytecodeExpression)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitClosureListExpression(org.codehaus.groovy.ast.expr.ClosureListExpression)
meth public void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitEmptyStatement(org.codehaus.groovy.ast.stmt.EmptyStatement)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitLambdaExpression(org.codehaus.groovy.ast.expr.LambdaExpression)
meth public void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression)
meth public void visitMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression)
meth public void visitMapExpression(org.codehaus.groovy.ast.expr.MapExpression)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public void visitMethodPointerExpression(org.codehaus.groovy.ast.expr.MethodPointerExpression)
meth public void visitMethodReferenceExpression(org.codehaus.groovy.ast.expr.MethodReferenceExpression)
meth public void visitNotExpression(org.codehaus.groovy.ast.expr.NotExpression)
meth public void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitShortTernaryExpression(org.codehaus.groovy.ast.expr.ElvisOperatorExpression)
meth public void visitSpreadExpression(org.codehaus.groovy.ast.expr.SpreadExpression)
meth public void visitSpreadMapExpression(org.codehaus.groovy.ast.expr.SpreadMapExpression)
meth public void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression)
meth public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitTupleExpression(org.codehaus.groovy.ast.expr.TupleExpression)
meth public void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression)
meth public void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr java.lang.Object

CLSS public abstract interface org.codehaus.groovy.ast.GroovyClassVisitor
meth public abstract void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public abstract void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public abstract void visitField(org.codehaus.groovy.ast.FieldNode)
meth public abstract void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public abstract void visitProperty(org.codehaus.groovy.ast.PropertyNode)

CLSS public abstract interface org.codehaus.groovy.ast.GroovyCodeVisitor
meth public abstract void visitArgumentlistExpression(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public abstract void visitArrayExpression(org.codehaus.groovy.ast.expr.ArrayExpression)
meth public abstract void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public abstract void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public abstract void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public abstract void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression)
meth public abstract void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public abstract void visitBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public abstract void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public abstract void visitBytecodeExpression(org.codehaus.groovy.classgen.BytecodeExpression)
meth public abstract void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public abstract void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression)
meth public abstract void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public abstract void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression)
meth public abstract void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public abstract void visitClosureListExpression(org.codehaus.groovy.ast.expr.ClosureListExpression)
meth public abstract void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public abstract void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public abstract void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public abstract void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public abstract void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public abstract void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public abstract void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public abstract void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public abstract void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression)
meth public abstract void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public abstract void visitLambdaExpression(org.codehaus.groovy.ast.expr.LambdaExpression)
meth public abstract void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression)
meth public abstract void visitMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression)
meth public abstract void visitMapExpression(org.codehaus.groovy.ast.expr.MapExpression)
meth public abstract void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public abstract void visitMethodPointerExpression(org.codehaus.groovy.ast.expr.MethodPointerExpression)
meth public abstract void visitMethodReferenceExpression(org.codehaus.groovy.ast.expr.MethodReferenceExpression)
meth public abstract void visitNotExpression(org.codehaus.groovy.ast.expr.NotExpression)
meth public abstract void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public abstract void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public abstract void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public abstract void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression)
meth public abstract void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public abstract void visitShortTernaryExpression(org.codehaus.groovy.ast.expr.ElvisOperatorExpression)
meth public abstract void visitSpreadExpression(org.codehaus.groovy.ast.expr.SpreadExpression)
meth public abstract void visitSpreadMapExpression(org.codehaus.groovy.ast.expr.SpreadMapExpression)
meth public abstract void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression)
meth public abstract void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public abstract void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public abstract void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression)
meth public abstract void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public abstract void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public abstract void visitTupleExpression(org.codehaus.groovy.ast.expr.TupleExpression)
meth public abstract void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression)
meth public abstract void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression)
meth public abstract void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public abstract void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
meth public void visitEmptyExpression(org.codehaus.groovy.ast.expr.EmptyExpression)
meth public void visitEmptyStatement(org.codehaus.groovy.ast.stmt.EmptyStatement)
meth public void visitListOfExpressions(java.util.List<? extends org.codehaus.groovy.ast.expr.Expression>)

CLSS public abstract interface org.codehaus.groovy.ast.NodeMetaDataHandler
meth public <%0 extends java.lang.Object> {%%0} getNodeMetaData(java.lang.Object)
meth public <%0 extends java.lang.Object> {%%0} getNodeMetaData(java.lang.Object,java.util.function.Function<?,? extends {%%0}>)
meth public abstract java.util.Map<?,?> getMetaDataMap()
meth public abstract void setMetaDataMap(java.util.Map<?,?>)
meth public java.lang.Object putNodeMetaData(java.lang.Object,java.lang.Object)
meth public java.util.Map<?,?> getNodeMetaData()
meth public void copyNodeMetaData(org.codehaus.groovy.ast.NodeMetaDataHandler)
meth public void removeNodeMetaData(java.lang.Object)
meth public void setNodeMetaData(java.lang.Object,java.lang.Object)

CLSS public abstract interface org.codehaus.groovy.transform.ErrorCollecting
meth public abstract void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

CLSS public abstract interface org.netbeans.modules.csl.api.CodeCompletionHandler
innr public final static !enum QueryType
meth public abstract java.lang.String document(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getPrefix(org.netbeans.modules.csl.spi.ParserResult,int,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String resolveTemplateVariable(java.lang.String,org.netbeans.modules.csl.spi.ParserResult,int,java.lang.String,java.util.Map)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract java.util.Set<java.lang.String> getApplicableTemplates(javax.swing.text.Document,int,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType getAutoQuery(javax.swing.text.JTextComponent,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.CodeCompletionResult complete(org.netbeans.modules.csl.api.CodeCompletionContext)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ElementHandle resolveLink(java.lang.String,org.netbeans.modules.csl.api.ElementHandle)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ParameterInfo parameters(org.netbeans.modules.csl.spi.ParserResult,int,org.netbeans.modules.csl.api.CompletionProposal)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()

CLSS public abstract interface org.netbeans.modules.csl.api.CodeCompletionHandler2
intf org.netbeans.modules.csl.api.CodeCompletionHandler
meth public abstract org.netbeans.modules.csl.api.Documentation documentElement(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle,java.util.concurrent.Callable<java.lang.Boolean>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.modules.csl.api.CodeCompletionResult
cons public init()
fld public final static org.netbeans.modules.csl.api.CodeCompletionResult NONE
meth public abstract boolean isFilterable()
meth public abstract boolean isTruncated()
meth public abstract java.util.List<org.netbeans.modules.csl.api.CompletionProposal> getItems()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public boolean insert(org.netbeans.modules.csl.api.CompletionProposal)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void afterInsert(org.netbeans.modules.csl.api.CompletionProposal)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void beforeInsert(org.netbeans.modules.csl.api.CompletionProposal)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.csl.api.CompletionProposal
meth public abstract boolean isSmart()
meth public abstract int getAnchorOffset()
meth public abstract int getSortPrioOverride()
meth public abstract java.lang.String getCustomInsertTemplate()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getInsertPrefix()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getLhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getSortText()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.swing.ImageIcon getIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.ElementHandle getElement()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.ElementKind getKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.csl.api.ElementHandle
innr public static UrlHandle
meth public abstract boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIn()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getMimeType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ElementKind getKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFileObject()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.csl.api.GsfLanguage
meth public abstract boolean isIdentifierChar(char)
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getLineCommentPrefix()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getPreferredExtension()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Set<java.lang.String> getBinaryLibraryPathIds()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Set<java.lang.String> getLibraryPathIds()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Set<java.lang.String> getSourcePathIds()
 anno 0 java.lang.Deprecated()
meth public abstract org.netbeans.api.lexer.Language getLexerLanguage()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.modules.csl.api.OccurrencesFinder<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract java.util.Map<org.netbeans.modules.csl.api.OffsetRange,org.netbeans.modules.csl.api.ColoringAttributes> getOccurrences()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setCaretPosition(int)
supr org.netbeans.modules.parsing.spi.ParserResultTask<{org.netbeans.modules.csl.api.OccurrencesFinder%0}>

CLSS public abstract interface org.netbeans.modules.csl.api.StructureScanner
innr public final static Configuration
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.StructureItem> scan(org.netbeans.modules.csl.spi.ParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<java.lang.String,java.util.List<org.netbeans.modules.csl.api.OffsetRange>> folds(org.netbeans.modules.csl.spi.ParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.StructureScanner$Configuration getConfiguration()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract org.netbeans.modules.csl.spi.DefaultCompletionProposal
cons public init()
fld protected boolean smart
fld protected int anchorOffset
fld protected org.netbeans.modules.csl.api.ElementKind elementKind
intf org.netbeans.modules.csl.api.CompletionProposal
meth public boolean beforeDefaultAction()
meth public boolean isSmart()
meth public int getAnchorOffset()
meth public int getSortPrioOverride()
meth public java.lang.String getCustomInsertTemplate()
meth public java.lang.String getInsertPrefix()
meth public java.lang.String getLhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getSortText()
meth public java.lang.String[] getParamListDelimiters()
meth public java.util.List<java.lang.String> getInsertParams()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
meth public void setAnchorOffset(int)
meth public void setKind(org.netbeans.modules.csl.api.ElementKind)
meth public void setSmart(boolean)
supr java.lang.Object

CLSS public org.netbeans.modules.csl.spi.DefaultCompletionResult
cons public init(java.util.List<org.netbeans.modules.csl.api.CompletionProposal>,boolean)
fld protected boolean filterable
fld protected boolean truncated
fld protected java.util.List<org.netbeans.modules.csl.api.CompletionProposal> list
meth public boolean isFilterable()
meth public boolean isTruncated()
meth public java.util.List<org.netbeans.modules.csl.api.CompletionProposal> getItems()
meth public void setFilterable(boolean)
meth public void setTruncated(boolean)
supr org.netbeans.modules.csl.api.CodeCompletionResult

CLSS public abstract org.netbeans.modules.csl.spi.DefaultLanguageConfig
cons public init()
intf org.netbeans.modules.csl.api.GsfLanguage
meth public abstract java.lang.String getDisplayName()
meth public abstract org.netbeans.api.lexer.Language getLexerLanguage()
meth public boolean hasFormatter()
meth public boolean hasHintsProvider()
meth public boolean hasOccurrencesFinder()
meth public boolean hasStructureScanner()
 anno 0 java.lang.Deprecated()
meth public boolean isIdentifierChar(char)
meth public boolean isUsingCustomEditorKit()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getLineCommentPrefix()
meth public java.lang.String getPreferredExtension()
meth public java.util.Set<java.lang.String> getBinaryLibraryPathIds()
meth public java.util.Set<java.lang.String> getLibraryPathIds()
meth public java.util.Set<java.lang.String> getSourcePathIds()
meth public org.netbeans.modules.csl.api.CodeCompletionHandler getCompletionHandler()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.DeclarationFinder getDeclarationFinder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.Formatter getFormatter()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.HintsProvider getHintsProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.IndexSearcher getIndexSearcher()
meth public org.netbeans.modules.csl.api.InstantRenamer getInstantRenamer()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.KeystrokeHandler getKeystrokeHandler()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.OccurrencesFinder getOccurrencesFinder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.OverridingMethods getOverridingMethods()
meth public org.netbeans.modules.csl.api.SemanticAnalyzer getSemanticAnalyzer()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.StructureScanner getStructureScanner()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.spi.CommentHandler getCommentHandler()
meth public org.netbeans.modules.parsing.spi.Parser getParser()
meth public org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory getIndexerFactory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.csl.spi.ParserResult
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
supr org.netbeans.modules.parsing.spi.Parser$Result

CLSS public org.netbeans.modules.groovy.editor.api.ASTUtils
cons public init()
innr public final static FakeASTNode
meth public static int getAstOffset(org.netbeans.modules.parsing.spi.Parser$Result,int)
meth public static int getOffset(org.netbeans.editor.BaseDocument,int,int)
meth public static java.lang.String getClassParentName(org.codehaus.groovy.ast.ClassNode)
meth public static java.lang.String getDefSignature(org.codehaus.groovy.ast.MethodNode)
meth public static java.lang.String getFqnName(org.netbeans.modules.groovy.editor.api.AstPath)
meth public static java.lang.String getSimpleName(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<org.codehaus.groovy.ast.ASTNode> children(org.codehaus.groovy.ast.ASTNode)
meth public static org.codehaus.groovy.ast.ASTNode getForeignNode(org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement)
meth public static org.codehaus.groovy.ast.ASTNode getScope(org.netbeans.modules.groovy.editor.api.AstPath,org.codehaus.groovy.ast.Variable)
meth public static org.codehaus.groovy.ast.ASTNode getVariable(org.codehaus.groovy.ast.ASTNode,java.lang.String,org.netbeans.modules.groovy.editor.api.AstPath,org.netbeans.editor.BaseDocument,int)
meth public static org.codehaus.groovy.ast.ClassNode getOwningClass(org.netbeans.modules.groovy.editor.api.AstPath)
meth public static org.codehaus.groovy.ast.ModuleNode getRoot(org.netbeans.modules.csl.spi.ParserResult)
meth public static org.netbeans.modules.csl.api.OffsetRange getNextIdentifierByName(org.netbeans.editor.BaseDocument,java.lang.String,int)
meth public static org.netbeans.modules.csl.api.OffsetRange getRange(org.codehaus.groovy.ast.ASTNode,org.netbeans.editor.BaseDocument)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.csl.api.OffsetRange getRangeFull(org.codehaus.groovy.ast.ASTNode,org.netbeans.editor.BaseDocument)
meth public static org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult getParseResult(org.netbeans.modules.parsing.spi.Parser$Result)
supr java.lang.Object
hfds LOGGER

CLSS public final static org.netbeans.modules.groovy.editor.api.ASTUtils$FakeASTNode
 outer org.netbeans.modules.groovy.editor.api.ASTUtils
cons public init(org.codehaus.groovy.ast.ASTNode)
cons public init(org.codehaus.groovy.ast.ASTNode,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ASTNode getOriginalNode()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.ASTNode
hfds name,node

CLSS public org.netbeans.modules.groovy.editor.api.AstPath
cons public init()
cons public init(org.codehaus.groovy.ast.ASTNode,int,int)
cons public init(org.codehaus.groovy.ast.ASTNode,int,org.netbeans.editor.BaseDocument)
cons public init(org.codehaus.groovy.ast.ASTNode,int,org.netbeans.editor.BaseDocument,boolean)
cons public init(org.codehaus.groovy.ast.ASTNode,org.codehaus.groovy.ast.ASTNode)
intf java.lang.Iterable<org.codehaus.groovy.ast.ASTNode>
meth public boolean find(org.codehaus.groovy.ast.ASTNode,org.codehaus.groovy.ast.ASTNode)
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String toString()
meth public java.util.Iterator<org.codehaus.groovy.ast.ASTNode> iterator()
meth public java.util.ListIterator<org.codehaus.groovy.ast.ASTNode> leafToRoot()
meth public java.util.ListIterator<org.codehaus.groovy.ast.ASTNode> rootToLeaf()
meth public org.codehaus.groovy.ast.ASTNode leaf()
meth public org.codehaus.groovy.ast.ASTNode leafGrandParent()
meth public org.codehaus.groovy.ast.ASTNode leafParent()
meth public org.codehaus.groovy.ast.ASTNode root()
meth public void ascend()
meth public void descend(org.codehaus.groovy.ast.ASTNode)
supr java.lang.Object
hfds columnNumber,lineNumber,path
hcls LeafToRootIterator

CLSS public final org.netbeans.modules.groovy.editor.api.ElementUtils
meth public static java.lang.String getDeclaringClassName(org.codehaus.groovy.ast.ASTNode)
meth public static java.lang.String getDeclaringClassNameWithoutPackage(org.codehaus.groovy.ast.ASTNode)
meth public static java.lang.String getNameWithoutPackage(org.codehaus.groovy.ast.ASTNode)
meth public static java.lang.String getTypeName(org.codehaus.groovy.ast.ASTNode)
meth public static java.lang.String getTypeNameWithoutPackage(org.codehaus.groovy.ast.ASTNode)
meth public static java.lang.String normalizeTypeName(java.lang.String,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode getDeclaringClass(org.codehaus.groovy.ast.ASTNode)
meth public static org.codehaus.groovy.ast.ClassNode getType(org.codehaus.groovy.ast.ASTNode)
meth public static org.netbeans.modules.csl.api.ElementKind getKind(org.netbeans.modules.groovy.editor.api.AstPath,org.netbeans.editor.BaseDocument,int)
supr java.lang.Object

CLSS public final org.netbeans.modules.groovy.editor.api.FindTypeUtils
meth public static boolean isCaretOnClassNode(org.netbeans.modules.groovy.editor.api.AstPath,org.netbeans.editor.BaseDocument,int)
meth public static org.codehaus.groovy.ast.ASTNode findCurrentNode(org.netbeans.modules.groovy.editor.api.AstPath,org.netbeans.editor.BaseDocument,int)
supr java.lang.Object

CLSS public final org.netbeans.modules.groovy.editor.api.GroovyIndex
meth protected static boolean matchCamelCase(java.lang.String,java.lang.String,boolean)
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass> getAllClasses()
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass> getClasses(java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind)
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass> getClassesFromPackage(java.lang.String)
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedField> getAllFields(java.lang.String)
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedField> getFields(java.lang.String,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind)
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedField> getStaticFields(java.lang.String)
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod> getConstructors(java.lang.String)
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod> getInheritedMethods(java.lang.String,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind)
meth public java.util.Set<org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod> getMethods(java.lang.String,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind)
meth public static org.netbeans.modules.groovy.editor.api.GroovyIndex get(java.util.Collection<org.openide.filesystems.FileObject>)
meth public static void setClusterUrl(java.lang.String)
supr java.lang.Object
hfds CLUSTER_URL,EMPTY,LOG,cachedCamelCasePattern,cachedInsensitive,cachedPrefix,clusterUrl,querySupport

CLSS public org.netbeans.modules.groovy.editor.api.GroovyIndexer
cons public init()
innr public final static Factory
meth protected void index(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.spi.Parser$Result,org.netbeans.modules.parsing.spi.indexing.Context)
meth public org.openide.filesystems.FileObject getPreindexedDb()
supr org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer
hfds CASE_INSENSITIVE_CLASS_NAME,CLASS_ATTRS,CLASS_NAME,CLASS_OFFSET,CONSTRUCTOR,FIELD_NAME,FQN_NAME,IN,LOG,METHOD_NAME,filesIndexed,indexerFirstRun,indexerRunTime,preindexedDb
hcls TreeAnalyzer

CLSS public final static org.netbeans.modules.groovy.editor.api.GroovyIndexer$Factory
 outer org.netbeans.modules.groovy.editor.api.GroovyIndexer
cons public init()
fld public final static int VERSION = 9
fld public final static java.lang.String NAME = "groovy"
meth public boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
meth public int getIndexVersion()
meth public java.lang.String getIndexerName()
meth public org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer createIndexer(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.api.Snapshot)
meth public void filesDeleted(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public void filesDirty(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public void rootsRemoved(java.lang.Iterable<? extends java.net.URL>)
meth public void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
supr org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory

CLSS public org.netbeans.modules.groovy.editor.api.Methods
cons public init()
meth public static boolean hasSameParameters(org.codehaus.groovy.ast.MethodNode,org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public static boolean hasSameParameters(org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod,org.codehaus.groovy.ast.MethodNode)
meth public static boolean hasSameParameters(org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod,org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public static boolean isSameConstructor(org.codehaus.groovy.ast.ConstructorNode,org.codehaus.groovy.ast.ConstructorNode)
meth public static boolean isSameConstructor(org.codehaus.groovy.ast.ConstructorNode,org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public static boolean isSameConstuctor(org.codehaus.groovy.ast.expr.ConstructorCallExpression,org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public static boolean isSameList(java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static boolean isSameMethod(javax.lang.model.element.ExecutableElement,org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public static boolean isSameMethod(org.codehaus.groovy.ast.MethodNode,org.codehaus.groovy.ast.MethodNode)
meth public static boolean isSameMethod(org.codehaus.groovy.ast.MethodNode,org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public static boolean isSameMethod(org.codehaus.groovy.ast.expr.MethodCallExpression,org.codehaus.groovy.ast.expr.MethodCallExpression)
supr java.lang.Object

CLSS public org.netbeans.modules.groovy.editor.api.PathFinderVisitor
cons public init(org.codehaus.groovy.control.SourceUnit,int,int)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitAnnotation(org.codehaus.groovy.ast.AnnotationNode)
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth protected void visitStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public java.util.List<org.codehaus.groovy.ast.ASTNode> getPath()
meth public void visitArgumentlistExpression(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public void visitArrayExpression(org.codehaus.groovy.ast.expr.ArrayExpression)
meth public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitClosureListExpression(org.codehaus.groovy.ast.expr.ClosureListExpression)
meth public void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitImports(org.codehaus.groovy.ast.ModuleNode)
meth public void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression)
meth public void visitMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression)
meth public void visitMapExpression(org.codehaus.groovy.ast.expr.MapExpression)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public void visitMethodPointerExpression(org.codehaus.groovy.ast.expr.MethodPointerExpression)
meth public void visitNotExpression(org.codehaus.groovy.ast.expr.NotExpression)
meth public void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
meth public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitShortTernaryExpression(org.codehaus.groovy.ast.expr.ElvisOperatorExpression)
meth public void visitSpreadExpression(org.codehaus.groovy.ast.expr.SpreadExpression)
meth public void visitSpreadMapExpression(org.codehaus.groovy.ast.expr.SpreadMapExpression)
meth public void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression)
meth public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitTupleExpression(org.codehaus.groovy.ast.expr.TupleExpression)
meth public void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression)
meth public void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds LOG,column,line,outermost,path,sourceUnit

CLSS public org.netbeans.modules.groovy.editor.api.StructureAnalyzer
cons public init()
innr public final static AnalysisResult
intf org.netbeans.modules.csl.api.StructureScanner
meth public java.util.List<? extends org.netbeans.modules.csl.api.StructureItem> scan(org.netbeans.modules.csl.spi.ParserResult)
meth public java.util.Map<java.lang.String,java.util.List<org.netbeans.modules.csl.api.OffsetRange>> folds(org.netbeans.modules.csl.spi.ParserResult)
meth public org.netbeans.modules.csl.api.StructureScanner$Configuration getConfiguration()
meth public org.netbeans.modules.groovy.editor.api.StructureAnalyzer$AnalysisResult analyze(org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult)
supr java.lang.Object
hfds LOG,classes,fields,methods,properties,structure
hcls GroovyStructureItem

CLSS public final static org.netbeans.modules.groovy.editor.api.StructureAnalyzer$AnalysisResult
 outer org.netbeans.modules.groovy.editor.api.StructureAnalyzer
cons public init()
supr java.lang.Object
hfds elements

CLSS public final !enum org.netbeans.modules.groovy.editor.api.completion.CaretLocation
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation ABOVE_FIRST_CLASS
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation ABOVE_PACKAGE
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_CLASS
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_CLOSURE
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_COMMENT
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_CONSTRUCTOR_CALL
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_IMPORT
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_METHOD
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_PACKAGE
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_PARAMETERS
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation INSIDE_STRING
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation OUTSIDE_CLASSES
fld public final static org.netbeans.modules.groovy.editor.api.completion.CaretLocation UNDEFINED
meth public static org.netbeans.modules.groovy.editor.api.completion.CaretLocation valueOf(java.lang.String)
meth public static org.netbeans.modules.groovy.editor.api.completion.CaretLocation[] values()
supr java.lang.Enum<org.netbeans.modules.groovy.editor.api.completion.CaretLocation>
hfds id

CLSS public org.netbeans.modules.groovy.editor.api.completion.CompletionHandler
cons public init()
intf org.netbeans.modules.csl.api.CodeCompletionHandler2
meth public java.lang.String document(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle)
meth public java.lang.String getPrefix(org.netbeans.modules.csl.spi.ParserResult,int,boolean)
meth public java.lang.String resolveTemplateVariable(java.lang.String,org.netbeans.modules.csl.spi.ParserResult,int,java.lang.String,java.util.Map)
meth public java.util.Set<java.lang.String> getApplicableTemplates(javax.swing.text.Document,int,int)
meth public org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType getAutoQuery(javax.swing.text.JTextComponent,java.lang.String)
meth public org.netbeans.modules.csl.api.CodeCompletionResult complete(org.netbeans.modules.csl.api.CodeCompletionContext)
meth public org.netbeans.modules.csl.api.Documentation documentElement(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle,java.util.concurrent.Callable<java.lang.Boolean>)
meth public org.netbeans.modules.csl.api.ElementHandle resolveLink(java.lang.String,org.netbeans.modules.csl.api.ElementHandle)
meth public org.netbeans.modules.csl.api.ParameterInfo parameters(org.netbeans.modules.csl.spi.ParserResult,int,org.netbeans.modules.csl.api.CompletionProposal)
meth public static java.lang.String getMethodSignature(groovy.lang.MetaMethod,boolean,boolean)
supr java.lang.Object
hfds LOG,impl

CLSS public abstract org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons protected init(org.netbeans.modules.groovy.editor.api.elements.GroovyElement,int)
fld protected final org.netbeans.modules.groovy.editor.api.elements.GroovyElement element
innr public static ConstructorItem
innr public static DynamicFieldItem
innr public static FieldItem
innr public static JavaFieldItem
innr public static KeywordItem
innr public static LocalVarItem
innr public static MetaMethodItem
innr public static NamedParameter
innr public static NewFieldItem
innr public static NewVarItem
innr public static PackageItem
innr public static TypeItem
meth public boolean equals(java.lang.Object)
meth public int getSortPrioOverride()
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
meth public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem forDynamicField(int,java.lang.String,java.lang.String)
meth public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem forDynamicMethod(int,java.lang.String,java.lang.String[],java.lang.String,boolean)
meth public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem forJavaMethod(java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.lang.String,java.util.Set<javax.lang.model.element.Modifier>,int,boolean,boolean)
meth public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem forJavaMethod(java.lang.String,java.lang.String,java.util.List<java.lang.String>,javax.lang.model.type.TypeMirror,java.util.Set<javax.lang.model.element.Modifier>,int,boolean,boolean)
supr org.netbeans.modules.csl.spi.DefaultCompletionProposal
hfds LOG,groovyIcon,javaIcon,newConstructorIcon,sortOverride
hcls DynamicMethodItem,JavaMethodItem

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$ConstructorItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,java.util.List<org.netbeans.modules.groovy.editor.api.elements.common.MethodElement$MethodParameter>,int,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isSmart()
meth public int hashCode()
meth public java.lang.String getCustomInsertTemplate()
meth public java.lang.String getLhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds NEW_CSTR,className,expand,handle,name,paramListString,parameters

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$DynamicFieldItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(int,java.lang.String,java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds name,type

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$FieldItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,java.lang.String,int,int)
cons public init(java.lang.String,java.lang.String,java.util.Set<org.netbeans.modules.csl.api.Modifier>,int)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds fieldName,modifiers,typeName

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$JavaFieldItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,java.lang.String,javax.lang.model.type.TypeMirror,java.util.Set<javax.lang.model.element.Modifier>,int,boolean)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds className,emphasise,handle,modifiers,name,type

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$KeywordItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,java.lang.String,int,org.netbeans.modules.csl.spi.ParserResult,boolean)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds JAVA_KEYWORD,description,info,isGroovy,keyword

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$LocalVarItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(org.codehaus.groovy.ast.Variable,int)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds var

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$MetaMethodItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.Class,groovy.lang.MetaMethod,int,boolean,boolean)
meth public groovy.lang.MetaMethod getMethod()
meth public int getSortPrioOverride()
meth public java.lang.String getCustomInsertTemplate()
meth public java.lang.String getLhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds isGDK,method,methodElement,nameOnly

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$NamedParameter
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,java.lang.String,int)
meth public boolean equals(java.lang.Object)
meth public boolean isSmart()
meth public int hashCode()
meth public java.lang.String getCustomInsertTemplate()
meth public java.lang.String getLhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getName()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds name,typeName

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$NewFieldItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,int)
meth public java.lang.String getName()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds fieldName

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$NewVarItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,int)
meth public java.lang.String getName()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds var

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$PackageItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,int,org.netbeans.modules.csl.spi.ParserResult)
meth public java.lang.String getCustomInsertTemplate()
meth public java.lang.String getName()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds info,packageName

CLSS public static org.netbeans.modules.groovy.editor.api.completion.CompletionItem$TypeItem
 outer org.netbeans.modules.groovy.editor.api.completion.CompletionItem
cons public init(java.lang.String,java.lang.String,int,javax.lang.model.element.ElementKind)
meth public java.lang.String getFqn()
meth public java.lang.String getName()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.completion.CompletionItem
hfds ek,fqn,handle,name

CLSS public final org.netbeans.modules.groovy.editor.api.completion.FieldSignature
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
supr java.lang.Object
hfds name

CLSS public org.netbeans.modules.groovy.editor.api.completion.GroovyCompletionResult
cons public init(java.util.List<org.netbeans.modules.csl.api.CompletionProposal>,org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public void afterInsert(org.netbeans.modules.csl.api.CompletionProposal)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.csl.spi.DefaultCompletionResult
hfds context,fo,root
hcls ImportCollector

CLSS public final !enum org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_abstract
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_as
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_assert
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_boolean
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_break
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_byte
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_case
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_catch
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_char
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_class
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_continue
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_def
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_default
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_do
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_double
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_else
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_enum
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_extends
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_final
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_finally
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_float
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_for
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_if
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_implements
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_import
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_in
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_instanceof
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_int
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_interface
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_long
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_native
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_new
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_package
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_private
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_property
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_protected
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_public
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_return
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_short
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_static
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_strictfp
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_super
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_switch
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_synchronized
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_this
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_throw
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_throws
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_trait
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_transient
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_try
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_undefined
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_void
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_volatile
fld public final static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword KEYWORD_while
meth public boolean isAboveFistClass()
meth public boolean isGroovyKeyword()
meth public boolean isInsideClass()
meth public boolean isInsideCode()
meth public boolean isOutsideClasses()
meth public java.lang.String getName()
meth public org.netbeans.modules.groovy.editor.api.completion.KeywordCategory getCategory()
meth public static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword valueOf(java.lang.String)
meth public static org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword[] values()
supr java.lang.Enum<org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword>
hfds aboveFistClass,category,insideClass,insideCode,isGroovy,name,outsideClasses

CLSS public final !enum org.netbeans.modules.groovy.editor.api.completion.KeywordCategory
fld public final static org.netbeans.modules.groovy.editor.api.completion.KeywordCategory ANY
fld public final static org.netbeans.modules.groovy.editor.api.completion.KeywordCategory KEYWORD
fld public final static org.netbeans.modules.groovy.editor.api.completion.KeywordCategory MODIFIER
fld public final static org.netbeans.modules.groovy.editor.api.completion.KeywordCategory NONE
fld public final static org.netbeans.modules.groovy.editor.api.completion.KeywordCategory PRIMITIVE
meth public static org.netbeans.modules.groovy.editor.api.completion.KeywordCategory valueOf(java.lang.String)
meth public static org.netbeans.modules.groovy.editor.api.completion.KeywordCategory[] values()
supr java.lang.Enum<org.netbeans.modules.groovy.editor.api.completion.KeywordCategory>
hfds category

CLSS public final org.netbeans.modules.groovy.editor.api.completion.MethodSignature
cons public init(java.lang.String,java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String[] getParameters()
supr java.lang.Object
hfds name,parameters

CLSS public final org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext
cons public init(org.netbeans.modules.csl.spi.ParserResult,java.lang.String,int,int,int,org.netbeans.editor.BaseDocument)
fld public boolean scriptMode
fld public final int astOffset
fld public final int lexOffset
fld public final org.netbeans.editor.BaseDocument doc
fld public java.util.Set<org.netbeans.modules.groovy.editor.completion.AccessLevel> access
fld public org.codehaus.groovy.ast.ClassNode declaringClass
fld public org.codehaus.groovy.ast.ClassNode rawDseclaringClass
fld public org.netbeans.modules.groovy.editor.api.AstPath path
fld public org.netbeans.modules.groovy.editor.api.completion.CaretLocation location
fld public org.netbeans.modules.groovy.editor.api.completion.util.CompletionSurrounding context
fld public org.netbeans.modules.groovy.editor.api.completion.util.DotCompletionContext dotContext
meth public boolean isBehindDot()
meth public boolean isBehindImportStatement()
meth public boolean isNameOnly()
meth public boolean isStaticMembers()
meth public int getAddSortOverride()
meth public int getAnchor()
meth public java.lang.String getPrefix()
meth public java.lang.String getTypeName()
meth public org.codehaus.groovy.ast.ClassNode getSurroundingClass()
meth public org.netbeans.modules.csl.spi.ParserResult getParserResult()
meth public org.openide.filesystems.FileObject getSourceFile()
meth public void init()
meth public void setAddSortOverride(int)
meth public void setAnchor(int)
meth public void setDeclaringClass(org.codehaus.groovy.ast.ClassNode,boolean)
meth public void setPrefix(java.lang.String)
meth public void setTypeName(java.lang.String)
supr java.lang.Object
hfds addSortOverride,anchor,nameOnly,parserResult,prefix,sourceFile,staticMembers,typeName

CLSS public org.netbeans.modules.groovy.editor.api.completion.util.CompletionSurrounding
cons public init(org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>)
fld public org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> active
fld public org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> after1
fld public org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> after2
fld public org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> afterLiteral
fld public org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> before1
fld public org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> before2
fld public org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> beforeLiteral
fld public org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> ts
supr java.lang.Object

CLSS public final org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper
fld protected final static java.util.logging.Logger LOG
meth public static boolean isAfterComma(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static boolean isAfterLeftParenthesis(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static boolean isConstructorCall(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static boolean isFieldNameDefinition(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static boolean isVariableNameDefinition(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static java.util.List<java.lang.String> getProperties(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static java.util.List<org.codehaus.groovy.ast.ClassNode> getDeclaredClasses(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static org.codehaus.groovy.ast.ASTNode getSurroundingClassMember(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static org.codehaus.groovy.ast.ASTNode getSurroundingMethodOrClosure(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static org.codehaus.groovy.ast.ClassNode getSurroundingClassNode(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public static org.codehaus.groovy.ast.ModuleNode getSurroundingModuleNode(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
supr java.lang.Object

CLSS public org.netbeans.modules.groovy.editor.api.completion.util.DotCompletionContext
cons public init(int,int,org.netbeans.modules.groovy.editor.api.AstPath,boolean,boolean)
meth public boolean isFieldsOnly()
meth public boolean isMethodsOnly()
meth public int getAstOffset()
meth public int getLexOffset()
meth public org.netbeans.modules.groovy.editor.api.AstPath getAstPath()
supr java.lang.Object
hfds astOffset,astPath,fieldsOnly,lexOffset,methodsOnly

CLSS public org.netbeans.modules.groovy.editor.api.elements.CommentElement
cons public init(java.lang.String)
meth public java.lang.String getName()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.elements.GroovyElement
hfds text

CLSS public org.netbeans.modules.groovy.editor.api.elements.ElementHandleSupport
cons public init()
meth public static org.netbeans.modules.csl.api.ElementHandle createHandle(java.lang.String,java.lang.String,org.netbeans.modules.csl.api.ElementKind,java.util.Set<org.netbeans.modules.csl.api.Modifier>)
meth public static org.netbeans.modules.csl.api.ElementHandle createHandle(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.groovy.editor.api.elements.GroovyElement)
meth public static org.netbeans.modules.csl.api.ElementHandle createHandle(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement)
meth public static org.netbeans.modules.groovy.editor.api.elements.GroovyElement resolveHandle(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle)
supr java.lang.Object
hcls GroovyElementHandle,SimpleElementHandle

CLSS public abstract org.netbeans.modules.groovy.editor.api.elements.GroovyElement
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld protected java.lang.String in
fld protected java.lang.String name
fld protected java.lang.String signature
fld protected org.netbeans.modules.csl.api.OffsetRange offsetRange
intf org.netbeans.modules.csl.api.ElementHandle
meth public abstract org.netbeans.modules.csl.api.ElementKind getKind()
meth public boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
meth public java.lang.String getIn()
meth public java.lang.String getMimeType()
meth public java.lang.String getName()
meth public java.lang.String getSignature()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
meth public org.openide.filesystems.FileObject getFileObject()
meth public void setOffsetRange(org.netbeans.modules.csl.api.OffsetRange)
supr java.lang.Object

CLSS public org.netbeans.modules.groovy.editor.api.elements.KeywordElement
cons public init(java.lang.String)
meth public java.lang.String getName()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.elements.GroovyElement
hfds name

CLSS public org.netbeans.modules.groovy.editor.api.elements.ast.ASTClass
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String)
intf org.netbeans.modules.groovy.editor.api.elements.common.ClassElement
meth public java.lang.String getFqn()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement
hfds fqn

CLSS public abstract org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement
cons public init(org.codehaus.groovy.ast.ASTNode)
cons public init(org.codehaus.groovy.ast.ASTNode,java.lang.String)
cons public init(org.codehaus.groovy.ast.ASTNode,java.lang.String,java.lang.String)
fld protected final java.util.List<org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement> children
fld protected final org.codehaus.groovy.ast.ASTNode node
fld protected java.util.Set<org.netbeans.modules.csl.api.Modifier> modifiers
meth public boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement> getChildren()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.codehaus.groovy.ast.ASTNode getNode()
meth public org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
meth public static org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement create(org.codehaus.groovy.ast.ASTNode)
meth public void addChild(org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement)
supr org.netbeans.modules.groovy.editor.api.elements.GroovyElement

CLSS public final org.netbeans.modules.groovy.editor.api.elements.ast.ASTField
cons public init(org.codehaus.groovy.ast.FieldNode,java.lang.String,boolean)
meth public boolean isProperty()
meth public java.lang.String getName()
meth public java.lang.String getSignature()
meth public java.lang.String getType()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement
hfds fieldType,isProperty

CLSS public org.netbeans.modules.groovy.editor.api.elements.ast.ASTMethod
cons public init(org.codehaus.groovy.ast.ASTNode)
cons public init(org.codehaus.groovy.ast.ASTNode,java.lang.Class,groovy.lang.MetaMethod,boolean)
cons public init(org.codehaus.groovy.ast.ASTNode,java.lang.String)
intf org.netbeans.modules.groovy.editor.api.elements.common.MethodElement
meth public boolean isGDK()
meth public groovy.lang.MetaMethod getMethod()
meth public java.lang.Class getClz()
meth public java.lang.String getName()
meth public java.lang.String getReturnType()
meth public java.lang.String getSignature()
meth public java.util.List<java.lang.String> getParameterTypes()
meth public java.util.List<org.netbeans.modules.groovy.editor.api.elements.common.MethodElement$MethodParameter> getParameters()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement
hfds clz,isGDK,method,parameters,returnType

CLSS public org.netbeans.modules.groovy.editor.api.elements.ast.ASTRoot
cons public init(org.openide.filesystems.FileObject,org.codehaus.groovy.ast.ModuleNode)
meth public java.lang.String getName()
meth public org.codehaus.groovy.ast.ModuleNode getModuleNode()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement
hfds fileObject,moduleNode

CLSS public abstract interface org.netbeans.modules.groovy.editor.api.elements.common.ClassElement
meth public abstract java.lang.String getFqn()

CLSS public abstract interface org.netbeans.modules.groovy.editor.api.elements.common.MethodElement
innr public final static MethodParameter
meth public abstract java.lang.String getReturnType()
meth public abstract java.util.List<java.lang.String> getParameterTypes()
meth public abstract java.util.List<org.netbeans.modules.groovy.editor.api.elements.common.MethodElement$MethodParameter> getParameters()

CLSS public final static org.netbeans.modules.groovy.editor.api.elements.common.MethodElement$MethodParameter
 outer org.netbeans.modules.groovy.editor.api.elements.common.MethodElement
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getFqnType()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
supr java.lang.Object
hfds fqnType,name,type

CLSS public final org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass
cons protected init(org.netbeans.modules.parsing.spi.indexing.support.IndexResult,java.lang.String,java.lang.String,java.lang.String,int)
fld public final static int MODULE = 64
intf org.netbeans.modules.groovy.editor.api.elements.common.ClassElement
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getFqn()
meth public java.lang.String getName()
meth public java.lang.String getSignature()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
meth public static org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass create(java.lang.String,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.IndexResult,java.lang.String,int)
supr org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement
hfds simpleName

CLSS public abstract org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement
cons protected init(org.netbeans.modules.parsing.spi.indexing.support.IndexResult,java.lang.String,java.lang.String,int)
cons protected init(org.netbeans.modules.parsing.spi.indexing.support.IndexResult,java.lang.String,java.lang.String,java.lang.String,int)
fld protected final int flags
fld protected final java.lang.String attributes
fld protected final org.netbeans.modules.parsing.spi.indexing.support.IndexResult result
fld protected java.util.Set<org.netbeans.modules.csl.api.Modifier> modifiers
meth public abstract java.lang.String getSignature()
meth public boolean isPrivate()
meth public boolean isProtected()
meth public boolean isPublic()
meth public boolean isStatic()
meth public java.lang.String toString()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.text.Document getDocument() throws java.io.IOException
meth public org.openide.filesystems.FileObject getFileObject()
meth public static char flagToFirstChar(int)
meth public static char flagToSecondChar(int)
meth public static int stringToFlag(char,char)
meth public static int stringToFlag(java.lang.String,int)
meth public static java.lang.String decodeFlags(int)
meth public static java.lang.String flagToString(int)
supr org.netbeans.modules.groovy.editor.api.elements.GroovyElement
hfds document

CLSS public org.netbeans.modules.groovy.editor.api.elements.index.IndexedField
meth public boolean equals(java.lang.Object)
meth public boolean isInherited()
meth public boolean isProperty()
meth public boolean isSmart()
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getSignature()
meth public java.lang.String getTypeName()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
meth public static org.netbeans.modules.groovy.editor.api.elements.index.IndexedField create(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.IndexResult,java.lang.String,int)
meth public void setInherited(boolean)
meth public void setSmart(boolean)
supr org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement
hfds fieldName,inherited,smart,typeName

CLSS public final org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod
cons public init(org.netbeans.modules.parsing.spi.indexing.support.IndexResult,java.lang.String,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.groovy.editor.api.elements.common.MethodElement$MethodParameter>,java.lang.String,int)
intf org.netbeans.modules.groovy.editor.api.elements.common.MethodElement
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getReturnType()
meth public java.lang.String getSignature()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getParameterTypes()
meth public java.util.List<org.netbeans.modules.groovy.editor.api.elements.common.MethodElement$MethodParameter> getParameters()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
supr org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement
hfds parameters,returnType

CLSS public org.netbeans.modules.groovy.editor.api.lexer.Call
cons public init(java.lang.String,java.lang.String,boolean,boolean)
fld public final static org.netbeans.modules.groovy.editor.api.lexer.Call LOCAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.Call NONE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.Call UNKNOWN
meth public boolean isMethodExpected()
meth public boolean isSimpleIdentifier()
meth public boolean isStatic()
meth public java.lang.String getLhs()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public static org.netbeans.modules.groovy.editor.api.lexer.Call getCallType(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenHierarchy<javax.swing.text.Document>,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds isStatic,lhs,methodExpected,type

CLSS public final org.netbeans.modules.groovy.editor.api.lexer.GroovyLexer
cons public init(org.netbeans.spi.lexer.LexerRestartInfo<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>)
intf org.netbeans.spi.lexer.Lexer<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>
meth public java.lang.Object state()
meth public org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> nextToken()
meth public void release()
supr java.lang.Object
hfds LOG,index,lexerInput,myCharBuffer,parser,scanner,tokenFactory
hcls DelegateLexer,LexerInputReader,MyCharBuffer,MyCharQueue,State

CLSS public final !enum org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId
fld public final static java.lang.String GROOVY_MIME_TYPE = "text/x-groovy"
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ANNOTATION
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ANNOTATIONS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ANNOTATION_ARRAY_INIT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ANNOTATION_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ANNOTATION_FIELD_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ANNOTATION_MEMBER_VALUE_PAIR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ARRAY_DECLARATOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId AT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BAND
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BAND_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BIG_SUFFIX
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BLOCK
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BLOCK_COMMENT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BNOT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BOR_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BSR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BSR_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BXOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId BXOR_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId CASE_GROUP
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId CLASS_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId CLOSED_BLOCK
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId CLOSED_BLOCK_OP
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId CLOSURE_OP
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId COLON
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId COMMA
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId COMPARE_TO
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId CTOR_CALL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId CTOR_IDENT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DEC
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DIGIT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DIGITS_WITH_UNDERSCORE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DIGITS_WITH_UNDERSCORE_OPT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DIV
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DIV_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DOLLAR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DOLLAR_REGEXP_CTOR_END
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DOLLAR_REGEXP_LITERAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DOLLAR_REGEXP_SYMBOL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DOT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId DYNAMIC_MEMBER
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ELIST
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ELVIS_OPERATOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId EMPTY_STAT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ENUM_CONSTANT_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ENUM_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId EOF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId EOL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId EQUAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ERROR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ESC
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ESCAPED_DOLLAR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ESCAPED_SLASH
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId EXPONENT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId EXPR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId EXTENDS_CLAUSE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId FLOAT_SUFFIX
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId FOR_CONDITION
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId FOR_EACH_CLAUSE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId FOR_INIT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId FOR_IN_ITERABLE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId FOR_ITERATOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId GE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId GT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId HEX_DIGIT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId IDENTICAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId IDENTIFIER
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId IMPLEMENTS_CLAUSE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId IMPLICIT_PARAMETERS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId IMPORT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId INC
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId INDEX_OP
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId INSTANCE_INIT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId INTERFACE_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LABELED_ARG
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LABELED_STAT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LAND
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LBRACE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LBRACKET
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LETTER
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LINE_COMMENT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LIST_CONSTRUCTOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_abstract
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_as
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_assert
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_boolean
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_break
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_byte
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_case
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_catch
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_char
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_class
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_continue
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_def
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_default
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_double
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_else
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_enum
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_extends
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_false
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_final
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_finally
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_float
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_for
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_if
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_implements
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_import
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_in
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_instanceof
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_int
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_interface
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_long
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_native
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_new
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_null
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_package
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_private
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_protected
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_public
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_return
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_short
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_static
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_super
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_switch
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_synchronized
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_this
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_threadsafe
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_throw
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_throws
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_trait
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_transient
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_true
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_try
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_void
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_volatile
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LITERAL_while
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LNOT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LPAREN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId LT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MAP_CONSTRUCTOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MEMBER_POINTER
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId METHOD_CALL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId METHOD_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MINUS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MINUS_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MOD
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MODIFIERS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MOD_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MULTICATCH
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId MULTICATCH_TYPES
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NLS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NOT_EQUAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NOT_IDENTICAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NULL_TREE_LOOKAHEAD
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NUM_BIG_DECIMAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NUM_BIG_INT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NUM_DOUBLE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NUM_FLOAT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NUM_INT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId NUM_LONG
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId OBJBLOCK
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId ONE_NL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId OPTIONAL_DOT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId PACKAGE_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId PARAMETERS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId PARAMETER_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId PLUS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId PLUS_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId POST_DEC
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId POST_INC
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId QUESTION
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId RANGE_EXCLUSIVE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId RANGE_INCLUSIVE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId RBRACE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId RBRACKET
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId REGEXP_CTOR_END
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId REGEXP_LITERAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId REGEXP_SYMBOL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId REGEX_FIND
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId REGEX_MATCH
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId RPAREN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SELECT_SLOT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SEMI
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SH_COMMENT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SLIST
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SL_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SL_COMMENT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SPREAD_ARG
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SPREAD_DOT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SPREAD_MAP_ARG
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SR_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STAR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STAR_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STAR_STAR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STAR_STAR_ASSIGN
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STATIC_IMPORT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STATIC_INIT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STRICTFP
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STRING_CH
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STRING_CONSTRUCTOR
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STRING_CTOR_END
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STRING_CTOR_MIDDLE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STRING_CTOR_START
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STRING_LITERAL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId STRING_NL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId SUPER_CTOR_CALL
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TRAIT_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TRIPLE_DOT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TYPE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TYPECAST
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TYPE_ARGUMENT
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TYPE_ARGUMENTS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TYPE_LOWER_BOUNDS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TYPE_PARAMETER
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TYPE_PARAMETERS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId TYPE_UPPER_BOUNDS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId UNARY_MINUS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId UNARY_PLUS
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId UNUSED_CONST
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId UNUSED_DO
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId UNUSED_GOTO
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId VARIABLE_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId VARIABLE_PARAMETER_DEF
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId VOCAB
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId WHITESPACE
fld public final static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId WILDCARD_TYPE
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String fixedText()
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> language()
meth public static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>
hfds LANGUAGE,fixedText,primaryCategory
hcls GroovyHierarchy

CLSS public final org.netbeans.modules.groovy.editor.api.lexer.LexUtilities
meth public static boolean isBeginToken(org.netbeans.api.lexer.TokenId,org.netbeans.api.editor.document.LineDocument,int)
meth public static boolean isBeginToken(org.netbeans.api.lexer.TokenId,org.netbeans.api.editor.document.LineDocument,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>)
meth public static boolean isBeginToken(org.netbeans.api.lexer.TokenId,org.netbeans.editor.BaseDocument,int)
meth public static boolean isBeginToken(org.netbeans.api.lexer.TokenId,org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>)
meth public static boolean isCommentOnlyLine(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static boolean isEndmatchingDo(org.netbeans.editor.BaseDocument,int)
meth public static boolean isIndentToken(org.netbeans.api.lexer.TokenId)
meth public static char getTokenChar(org.netbeans.editor.BaseDocument,int)
meth public static int findSpaceBegin(org.netbeans.editor.BaseDocument,int)
meth public static int getBeginEndLineBalance(org.netbeans.editor.BaseDocument,int,boolean)
meth public static int getLineBalance(org.netbeans.editor.BaseDocument,int,org.netbeans.api.lexer.TokenId,org.netbeans.api.lexer.TokenId)
meth public static int getTokenBalance(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenId,org.netbeans.api.lexer.TokenId,int) throws javax.swing.text.BadLocationException
meth public static org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> findPreviousNonWsNonComment(org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>)
meth public static org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> getToken(org.netbeans.api.editor.document.LineDocument,int)
meth public static org.netbeans.api.lexer.Token<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> getToken(org.netbeans.editor.BaseDocument,int)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> getGroovyTokenSequence(javax.swing.text.Document,int)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> getGroovyTokenSequence(org.netbeans.api.lexer.TokenHierarchy<javax.swing.text.Document>,int)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> getPositionedSequence(org.netbeans.api.editor.document.LineDocument,int)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> getPositionedSequence(org.netbeans.api.editor.document.LineDocument,int,boolean)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> getPositionedSequence(org.netbeans.editor.BaseDocument,int)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId> getPositionedSequence(org.netbeans.editor.BaseDocument,int,boolean)
meth public static org.netbeans.editor.BaseDocument getDocument(org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.editor.BaseDocument getDocument(org.netbeans.modules.parsing.api.Source,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.editor.BaseDocument getDocument(org.openide.filesystems.FileObject,boolean)
meth public static org.netbeans.modules.csl.api.OffsetRange findBegin(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>)
meth public static org.netbeans.modules.csl.api.OffsetRange findBwd(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.TokenId,org.netbeans.api.lexer.TokenId)
meth public static org.netbeans.modules.csl.api.OffsetRange findEnd(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>)
meth public static org.netbeans.modules.csl.api.OffsetRange findFwd(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId>,org.netbeans.api.lexer.TokenId,org.netbeans.api.lexer.TokenId)
meth public static org.netbeans.modules.csl.api.OffsetRange getCommentBlock(org.netbeans.editor.BaseDocument,int)
meth public static org.netbeans.modules.csl.api.OffsetRange getLexerOffsets(org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult,org.netbeans.modules.csl.api.OffsetRange)
supr java.lang.Object
hfds END_PAIRS,INDENT_WORDS,WHITESPACES_AND_COMMENTS

CLSS public abstract interface !annotation org.netbeans.modules.groovy.editor.api.parser.ApplyGroovyTransformation
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, PACKAGE])
fld public final static java.lang.String APPLY_INDEX = "index"
fld public final static java.lang.String APPLY_PARSE = "parse"
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] disable()
meth public abstract !hasdefault java.lang.String[] enable()
meth public abstract !hasdefault java.lang.String[] mimeTypes()
meth public abstract !hasdefault java.lang.String[] value()

CLSS public org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage
cons public init()
fld public final static java.lang.String ACTIONS = "Loaders/text/x-groovy/Actions"
fld public final static java.lang.String GROOVY_MIME_TYPE = "text/x-groovy"
meth public boolean hasFormatter()
meth public boolean hasHintsProvider()
meth public boolean hasOccurrencesFinder()
meth public boolean hasStructureScanner()
meth public boolean isIdentifierChar(char)
meth public java.lang.String getDisplayName()
meth public java.lang.String getLineCommentPrefix()
meth public java.lang.String getPreferredExtension()
meth public java.util.Set<java.lang.String> getSourcePathIds()
meth public org.netbeans.api.lexer.Language getLexerLanguage()
meth public org.netbeans.modules.csl.api.CodeCompletionHandler getCompletionHandler()
meth public org.netbeans.modules.csl.api.DeclarationFinder getDeclarationFinder()
meth public org.netbeans.modules.csl.api.Formatter getFormatter()
meth public org.netbeans.modules.csl.api.HintsProvider getHintsProvider()
meth public org.netbeans.modules.csl.api.IndexSearcher getIndexSearcher()
meth public org.netbeans.modules.csl.api.InstantRenamer getInstantRenamer()
meth public org.netbeans.modules.csl.api.KeystrokeHandler getKeystrokeHandler()
meth public org.netbeans.modules.csl.api.OccurrencesFinder getOccurrencesFinder()
meth public org.netbeans.modules.csl.api.SemanticAnalyzer getSemanticAnalyzer()
meth public org.netbeans.modules.csl.api.StructureScanner getStructureScanner()
meth public org.netbeans.modules.parsing.spi.Parser getParser()
meth public org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory getIndexerFactory()
meth public static org.netbeans.core.spi.multiview.text.MultiViewEditorElement createEditor(org.openide.util.Lookup)
supr org.netbeans.modules.csl.spi.DefaultLanguageConfig
hfds GROOVY_FILE_ICON_16x16

CLSS public org.netbeans.modules.groovy.editor.api.parser.GroovyOccurrencesFinder
cons public init()
meth protected final boolean isCancelled()
meth protected final void resume()
meth public final java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> getSchedulerClass()
meth public final void cancel()
meth public int getPriority()
meth public java.util.Map<org.netbeans.modules.csl.api.OffsetRange,org.netbeans.modules.csl.api.ColoringAttributes> getOccurrences()
meth public void run(org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult,org.netbeans.modules.parsing.spi.SchedulerEvent)
meth public void setCaretPosition(int)
supr org.netbeans.modules.csl.api.OccurrencesFinder<org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult>
hfds LOG,cancelled,caretPosition,file,occurrences

CLSS public org.netbeans.modules.groovy.editor.api.parser.GroovyParser
cons public init()
innr public final static !enum Sanitize
innr public final static Context
meth protected org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult createParseResult(org.netbeans.modules.parsing.api.Snapshot,org.codehaus.groovy.ast.ModuleNode,org.codehaus.groovy.control.ErrorCollector)
meth public boolean isCancelled()
meth public org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void cancel()
meth public void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr org.netbeans.modules.parsing.spi.Parser
hfds LOG,PARSING_COUNT,PARSING_TIME,STATIC_ERRORS,cancelled,lastResult,maximumParsingTime,phaseCounters
hcls CU,ParseErrorHandler

CLSS public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Context
 outer org.netbeans.modules.groovy.editor.api.parser.GroovyParser
cons public init(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.spi.SourceModificationEvent)
meth public int getErrorOffset()
meth public java.lang.String getSanitizedSource()
meth public java.lang.String toString()
meth public org.netbeans.modules.csl.api.OffsetRange getSanitizedRange()
supr java.lang.Object
hfds caretOffset,compilerCustomizers,customizerCtx,document,errorHandler,errorOffset,event,parserTask,perfData,sanitized,sanitizedContents,sanitizedRange,sanitizedSource,snapshot,source

CLSS public final static !enum org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize
 outer org.netbeans.modules.groovy.editor.api.parser.GroovyParser
fld public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize EDITED_DOT
fld public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize EDITED_LINE
fld public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize ERROR_DOT
fld public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize ERROR_LINE
fld public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize MISSING_END
fld public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize NEVER
fld public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize NONE
fld public final static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize PRIOR_ERROR_LINE
meth public static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize valueOf(java.lang.String)
meth public static org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize[] values()
supr java.lang.Enum<org.netbeans.modules.groovy.editor.api.parser.GroovyParser$Sanitize>

CLSS public org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult
meth protected void invalidate()
meth public java.lang.String getSanitizedContents()
meth public java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
meth public org.codehaus.groovy.ast.ClassNode resolveClassName(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.codehaus.groovy.control.ErrorCollector getErrorCollector()
meth public org.netbeans.modules.csl.api.OffsetRange getSanitizedRange()
meth public org.netbeans.modules.groovy.editor.api.StructureAnalyzer$AnalysisResult getStructure()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.groovy.editor.api.elements.ast.ASTRoot getRootElement()
meth public void setErrors(java.util.Collection<? extends org.netbeans.modules.csl.api.Error>)
meth public void setStructure(org.netbeans.modules.groovy.editor.api.StructureAnalyzer$AnalysisResult)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.csl.spi.ParserResult
hfds analysisResult,errorCollector,errors,nbCollector,parser,rootElement,sanitized,sanitizedContents,sanitizedRange,unit

CLSS public org.netbeans.modules.groovy.editor.api.parser.GroovyVirtualSourceProvider
cons public init()
intf org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider
meth public boolean index()
meth public java.util.Set<java.lang.String> getSupportedExtensions()
meth public void translate(java.lang.Iterable<java.io.File>,java.io.File,org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider$Result)
supr java.lang.Object
hcls JavaStubGenerator,Task

CLSS public abstract interface org.netbeans.modules.groovy.editor.spi.completion.CompletionProvider
meth public abstract java.util.Map<org.netbeans.modules.groovy.editor.api.completion.FieldSignature,org.netbeans.modules.groovy.editor.api.completion.CompletionItem> getFields(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public abstract java.util.Map<org.netbeans.modules.groovy.editor.api.completion.FieldSignature,org.netbeans.modules.groovy.editor.api.completion.CompletionItem> getStaticFields(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public abstract java.util.Map<org.netbeans.modules.groovy.editor.api.completion.MethodSignature,org.netbeans.modules.groovy.editor.api.completion.CompletionItem> getMethods(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)
meth public abstract java.util.Map<org.netbeans.modules.groovy.editor.api.completion.MethodSignature,org.netbeans.modules.groovy.editor.api.completion.CompletionItem> getStaticMethods(org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext)

CLSS public abstract interface org.netbeans.modules.groovy.editor.spi.completion.DefaultImportsProvider
meth public abstract java.util.Set<java.lang.String> getDefaultImportClasses()
meth public abstract java.util.Set<java.lang.String> getDefaultImportPackages()

CLSS public abstract interface org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider
innr public abstract interface static Result
meth public abstract boolean index()
meth public abstract java.util.Set<java.lang.String> getSupportedExtensions()
meth public abstract void translate(java.lang.Iterable<java.io.File>,java.io.File,org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider$Result)

CLSS public abstract org.netbeans.modules.parsing.api.Task
cons public init()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.Parser
cons public init()
innr public abstract static Result
innr public final static !enum CancelReason
meth public abstract org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public void cancel()
 anno 0 java.lang.Deprecated()
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls MyAccessor

CLSS public abstract static org.netbeans.modules.parsing.spi.Parser$Result
 outer org.netbeans.modules.parsing.spi.Parser
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth protected abstract void invalidate()
meth protected boolean processingFinished()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds snapshot

CLSS public abstract org.netbeans.modules.parsing.spi.ParserResultTask<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract int getPriority()
meth public abstract void run({org.netbeans.modules.parsing.spi.ParserResultTask%0},org.netbeans.modules.parsing.spi.SchedulerEvent)
supr org.netbeans.modules.parsing.spi.SchedulerTask

CLSS public abstract org.netbeans.modules.parsing.spi.SchedulerTask
meth public abstract int getPriority()
meth public abstract java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> getSchedulerClass()
meth public abstract void cancel()
supr org.netbeans.modules.parsing.api.Task

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer
cons public init()
meth protected abstract void index(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.spi.Parser$Result,org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory
cons public init()
meth public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer createIndexer(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.api.Snapshot)
supr org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory
cons public init()
meth public abstract int getIndexVersion()
meth public abstract java.lang.String getIndexerName()
meth public abstract void filesDeleted(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public abstract void filesDirty(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
meth public int getPriority()
meth public void rootsRemoved(java.lang.Iterable<? extends java.net.URL>)
meth public void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.lexer.Lexer<%0 extends org.netbeans.api.lexer.TokenId>
meth public abstract java.lang.Object state()
meth public abstract org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.Lexer%0}> nextToken()
meth public abstract void release()

