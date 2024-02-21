#Signature file v4.1
#Version 1.75

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

CLSS public final org.netbeans.modules.web.el.AstPath
cons public init(com.sun.el.parser.Node)
meth public com.sun.el.parser.Node getRoot()
meth public java.util.List<com.sun.el.parser.Node> leafToRoot()
meth public java.util.List<com.sun.el.parser.Node> rootToLeaf()
meth public java.util.List<com.sun.el.parser.Node> rootToNode(com.sun.el.parser.Node)
meth public java.util.List<com.sun.el.parser.Node> rootToNode(com.sun.el.parser.Node,boolean)
supr java.lang.Object
hfds nodes,root

CLSS public org.netbeans.modules.web.el.CompilationCache
cons public init()
innr public abstract interface static ValueProvider
innr public static Key
meth public !varargs static org.netbeans.modules.web.el.CompilationCache$Key createKey(java.lang.Object[])
meth public java.lang.Object getOrCache(org.netbeans.modules.web.el.CompilationCache$Key,org.netbeans.modules.web.el.CompilationCache$ValueProvider<?>)
supr java.lang.Object
hfds map

CLSS public static org.netbeans.modules.web.el.CompilationCache$Key
 outer org.netbeans.modules.web.el.CompilationCache
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object
hfds keys

CLSS public abstract interface static org.netbeans.modules.web.el.CompilationCache$ValueProvider<%0 extends java.lang.Object>
 outer org.netbeans.modules.web.el.CompilationCache
meth public abstract {org.netbeans.modules.web.el.CompilationCache$ValueProvider%0} get()

CLSS public org.netbeans.modules.web.el.CompilationContext
meth public org.netbeans.api.java.source.CompilationInfo info()
meth public org.netbeans.modules.web.el.CompilationCache cache()
meth public org.netbeans.modules.web.el.spi.ResolverContext context()
meth public org.openide.filesystems.FileObject file()
meth public static org.netbeans.modules.web.el.CompilationContext create(org.openide.filesystems.FileObject,org.netbeans.api.java.source.CompilationInfo)
supr java.lang.Object
hfds cache,context,file,info

CLSS public final org.netbeans.modules.web.el.ELElement
meth public boolean isValid()
meth public com.sun.el.parser.Node findNodeAt(int)
meth public com.sun.el.parser.Node getNode()
meth public java.lang.String toString()
meth public javax.el.ELException getError()
meth public org.netbeans.modules.csl.api.OffsetRange getEmbeddedOffset()
meth public org.netbeans.modules.csl.api.OffsetRange getOriginalOffset()
meth public org.netbeans.modules.csl.api.OffsetRange getOriginalOffset(com.sun.el.parser.Node)
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public org.netbeans.modules.web.el.ELElement makeValidCopy(com.sun.el.parser.Node,org.netbeans.modules.web.el.ELPreprocessor)
meth public org.netbeans.modules.web.el.ELPreprocessor getExpression()
supr java.lang.Object
hfds embeddedOffset,error,expression,node,originalOffset,snapshot

CLSS public final org.netbeans.modules.web.el.ELIndex
meth public java.util.Collection<? extends org.netbeans.modules.parsing.spi.indexing.support.IndexResult> findIdentifierReferences(java.lang.String)
meth public java.util.Collection<? extends org.netbeans.modules.parsing.spi.indexing.support.IndexResult> findMethodReferences(java.lang.String)
meth public java.util.Collection<? extends org.netbeans.modules.parsing.spi.indexing.support.IndexResult> findPropertyReferences(java.lang.String)
meth public static org.netbeans.modules.web.el.ELIndex get(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds querySupport

CLSS public final org.netbeans.modules.web.el.ELIndexer
cons public init()
innr public final static Factory
innr public final static Fields
meth protected void index(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.spi.Parser$Result,org.netbeans.modules.parsing.spi.indexing.Context)
supr org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer
hfds LOGGER
hcls Analyzer

CLSS public final static org.netbeans.modules.web.el.ELIndexer$Factory
 outer org.netbeans.modules.web.el.ELIndexer
cons public init()
meth public int getIndexVersion()
meth public java.lang.String getIndexerName()
meth public org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer createIndexer(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.api.Snapshot)
meth public void filesDeleted(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public void filesDirty(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
supr org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory
hfds INDEXABLE_MIMETYPES,NAME,VERSION

CLSS public final static org.netbeans.modules.web.el.ELIndexer$Fields
 outer org.netbeans.modules.web.el.ELIndexer
cons public init()
fld public final static java.lang.String EXPRESSION = "expression"
fld public final static java.lang.String IDENTIFIER = "identifier"
fld public final static java.lang.String IDENTIFIER_FULL_EXPRESSION = "identifier_full_expression"
fld public final static java.lang.String METHOD = "method"
fld public final static java.lang.String METHOD_FULL_EXPRESSION = "method_full_expression"
fld public final static java.lang.String PROPERTY = "property"
fld public final static java.lang.String PROPERTY_FULL_EXPRESSION = "property_full_expression"
fld public final static java.lang.String PROPERTY_OWNER = "property_owner"
fld public final static java.lang.String SEPARATOR = "|"
meth public static java.lang.String[] split(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.web.el.ELLanguage
cons public init()
fld public final static java.lang.String MIME_TYPE = "text/x-el"
meth public boolean hasHintsProvider()
meth public boolean hasOccurrencesFinder()
meth public java.lang.String getDisplayName()
meth public org.netbeans.api.lexer.Language getLexerLanguage()
meth public org.netbeans.modules.csl.api.CodeCompletionHandler getCompletionHandler()
meth public org.netbeans.modules.csl.api.DeclarationFinder getDeclarationFinder()
meth public org.netbeans.modules.csl.api.HintsProvider getHintsProvider()
meth public org.netbeans.modules.csl.api.OccurrencesFinder getOccurrencesFinder()
meth public org.netbeans.modules.csl.spi.CommentHandler getCommentHandler()
meth public org.netbeans.modules.parsing.spi.Parser getParser()
meth public org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory getIndexerFactory()
supr org.netbeans.modules.csl.spi.DefaultLanguageConfig

CLSS public final org.netbeans.modules.web.el.ELParser
cons public init()
meth public org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public static com.sun.el.parser.Node parse(org.netbeans.modules.web.el.ELPreprocessor)
meth public static org.netbeans.modules.web.el.ELParser create(javax.swing.text.Document)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
meth public void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr org.netbeans.modules.parsing.spi.Parser
hfds ATTRIBUTE_EL_MARKER,LOGGER,document,result

CLSS public final org.netbeans.modules.web.el.ELParserResult
cons public init(org.netbeans.modules.parsing.api.Snapshot)
cons public init(org.openide.filesystems.FileObject)
meth protected void invalidate()
meth public boolean hasElements()
meth public boolean isValid()
meth public java.lang.String toString()
meth public java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
meth public java.util.List<org.netbeans.modules.web.el.ELElement> getElements()
meth public java.util.List<org.netbeans.modules.web.el.ELElement> getElementsTo(int)
meth public org.netbeans.modules.web.el.ELElement addErrorElement(javax.el.ELException,org.netbeans.modules.web.el.ELPreprocessor,org.netbeans.modules.csl.api.OffsetRange)
meth public org.netbeans.modules.web.el.ELElement addValidElement(com.sun.el.parser.Node,org.netbeans.modules.web.el.ELPreprocessor,org.netbeans.modules.csl.api.OffsetRange)
meth public org.netbeans.modules.web.el.ELElement getElementAt(int)
meth public org.openide.filesystems.FileObject getFileObject()
supr org.netbeans.modules.csl.spi.ParserResult
hfds elements,file
hcls ELError

CLSS public org.netbeans.modules.web.el.ELPreprocessor
cons public !varargs init(java.lang.String,java.lang.String[][][])
fld public final static java.lang.String[][] ESCAPED_CHARACTERS
fld public final static java.lang.String[][] XML_ENTITY_REFS_CONVERSION_TABLE
meth public int getOriginalOffset(int)
meth public int getPreprocessedOffset(int)
meth public java.lang.String getOriginalExpression()
meth public java.lang.String getPreprocessedExpression()
meth public java.lang.String toString()
supr java.lang.Object
hfds LOG,conversionTables,diffs,originalExpression,preprocessedExpression,stringLiterals

CLSS public final org.netbeans.modules.web.el.ELTypeUtilities
meth public static boolean isAccessIntoCollection(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node)
meth public static boolean isImplicitObjectReference(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node,java.util.List<org.netbeans.modules.web.el.spi.ImplicitObjectType>,boolean)
meth public static boolean isIterableElement(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.Element)
meth public static boolean isMapElement(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.Element)
meth public static boolean isRawObjectReference(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node,boolean)
meth public static boolean isResourceBundleVar(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node)
meth public static boolean isSameMethod(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node,javax.lang.model.element.ExecutableElement)
meth public static boolean isSameMethod(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node,javax.lang.model.element.ExecutableElement,boolean)
meth public static boolean isScopeObject(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node)
meth public static boolean isStaticIterableElement(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node)
meth public static boolean isSubtypeOf(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.type.TypeMirror,java.lang.CharSequence)
meth public static java.lang.String getBracketMethodName(com.sun.el.parser.Node)
meth public static java.lang.String getParametersAsString(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.ExecutableElement)
meth public static java.lang.String getRawObjectName(com.sun.el.parser.Node)
meth public static java.lang.String getTypeNameFor(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.Element)
meth public static java.util.Collection<org.netbeans.modules.web.el.spi.Function> getELFunctions(org.netbeans.modules.web.el.CompilationContext)
meth public static java.util.Collection<org.netbeans.modules.web.el.spi.ImplicitObject> getImplicitObjects(org.netbeans.modules.web.el.CompilationContext)
meth public static java.util.List<java.lang.String> getParameterNames(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.ExecutableElement)
meth public static java.util.List<javax.lang.model.element.Element> getSuperTypesFor(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.Element)
meth public static java.util.List<javax.lang.model.element.Element> getSuperTypesFor(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.Element,org.netbeans.modules.web.el.ELElement,java.util.List<com.sun.el.parser.Node>)
meth public static javax.lang.model.element.Element getReferredType(org.netbeans.modules.web.el.CompilationContext,com.sun.el.parser.Node,org.openide.filesystems.FileObject)
meth public static javax.lang.model.element.Element getReferredType(org.netbeans.modules.web.el.CompilationContext,org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo,org.openide.filesystems.FileObject)
meth public static javax.lang.model.element.Element getTypeFor(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.Element)
meth public static javax.lang.model.element.Element resolveElement(org.netbeans.modules.web.el.CompilationContext,org.netbeans.modules.web.el.ELElement,com.sun.el.parser.Node)
meth public static javax.lang.model.element.Element resolveElement(org.netbeans.modules.web.el.CompilationContext,org.netbeans.modules.web.el.ELElement,com.sun.el.parser.Node,java.util.Map<com.sun.el.parser.AstIdentifier,com.sun.el.parser.Node>,java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo>)
meth public static javax.lang.model.element.TypeElement getElementForType(org.netbeans.modules.web.el.CompilationContext,java.lang.String)
meth public static javax.lang.model.type.TypeMirror getReturnType(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.ExecutableElement)
meth public static javax.lang.model.type.TypeMirror getReturnType(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.ExecutableElement,org.netbeans.modules.web.el.ELElement,java.util.List<com.sun.el.parser.Node>)
meth public static javax.lang.model.type.TypeMirror getReturnTypeForGenericClass(org.netbeans.modules.web.el.CompilationContext,javax.lang.model.element.ExecutableElement,org.netbeans.modules.web.el.ELElement,java.util.List<com.sun.el.parser.Node>)
meth public static org.netbeans.api.java.source.ClasspathInfo getElimplExtendedCPI(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds EL_IMPL_JAR_FO,FACES_CONTEXT_CLASS,LOG,STREAM_CLASS,TYPES,UI_COMPONENT_CLASS
hcls TypeResolverVisitor

CLSS public final org.netbeans.modules.web.el.ELVariableResolvers
meth public static java.lang.String findBeanClass(org.netbeans.modules.web.el.CompilationContext,java.lang.String,org.openide.filesystems.FileObject)
meth public static java.lang.String findBeanName(org.netbeans.modules.web.el.CompilationContext,java.lang.String,org.openide.filesystems.FileObject)
meth public static java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo> getBeansInScope(org.netbeans.modules.web.el.CompilationContext,java.lang.String,org.netbeans.modules.parsing.api.Snapshot)
meth public static java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo> getManagedBeans(org.netbeans.modules.web.el.CompilationContext,org.openide.filesystems.FileObject)
meth public static java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo> getRawObjectProperties(org.netbeans.modules.web.el.CompilationContext,java.lang.String,org.netbeans.modules.parsing.api.Snapshot)
meth public static java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo> getVariables(org.netbeans.modules.web.el.CompilationContext,org.netbeans.modules.parsing.api.Snapshot,int)
supr java.lang.Object

CLSS public org.netbeans.modules.web.el.NodeUtil
cons public init()
meth public static boolean isMethodCall(com.sun.el.parser.Node)
meth public static com.sun.el.parser.Node getSiblingBefore(com.sun.el.parser.Node)
meth public static java.lang.String dump(com.sun.el.parser.Node)
meth public static java.util.List<com.sun.el.parser.Node> getRootToNode(org.netbeans.modules.web.el.ELElement,com.sun.el.parser.Node)
supr java.lang.Object
hfds INDENT

CLSS public final org.netbeans.modules.web.el.ResourceBundles
fld protected final static java.util.Map<org.openide.filesystems.FileObject,org.netbeans.modules.web.el.ResourceBundles> CACHE
innr public static Location
meth public boolean canHaveBundles()
meth public boolean isResourceBundleIdentifier(java.lang.String,org.netbeans.modules.web.el.spi.ResolverContext)
meth public boolean isValidKey(java.lang.String,java.lang.String)
meth public java.lang.String findResourceBundleIdentifier(org.netbeans.modules.web.el.AstPath)
meth public java.lang.String getValue(java.lang.String,java.lang.String)
meth public java.util.List<org.netbeans.modules.web.el.ResourceBundles$Location> getLocationsForBundleIdent(java.lang.String)
meth public java.util.List<org.netbeans.modules.web.el.ResourceBundles$Location> getLocationsForBundleKey(java.lang.String,java.lang.String)
meth public java.util.List<org.netbeans.modules.web.el.spi.ResourceBundle> getBundles(org.netbeans.modules.web.el.spi.ResolverContext)
meth public java.util.List<org.openide.util.Pair<com.sun.el.parser.AstIdentifier,com.sun.el.parser.Node>> collectKeys(com.sun.el.parser.Node)
meth public java.util.List<org.openide.util.Pair<com.sun.el.parser.AstIdentifier,com.sun.el.parser.Node>> collectKeys(com.sun.el.parser.Node,org.netbeans.modules.web.el.spi.ResolverContext)
meth public java.util.Map<java.lang.String,java.lang.String> getEntries(java.lang.String)
meth public static org.netbeans.modules.web.el.ResourceBundles create(org.netbeans.modules.web.api.webmodule.WebModule,org.netbeans.api.project.Project)
meth public static org.netbeans.modules.web.el.ResourceBundles get(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds FILE_CHANGE_LISTENER,LOGGER,bundlesMap,currentBundlesHashCode,project,webModule
hcls ResourceBundleInfo

CLSS public static org.netbeans.modules.web.el.ResourceBundles$Location
 outer org.netbeans.modules.web.el.ResourceBundles
cons public init(int,org.openide.filesystems.FileObject)
meth public int getOffset()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds file,offset

CLSS public abstract org.netbeans.modules.web.el.spi.ELPlugin
cons public init()
innr public static Query
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<java.lang.String> getMimeTypes()
meth public abstract java.util.Collection<org.netbeans.modules.web.el.spi.ImplicitObject> getImplicitObjects(org.openide.filesystems.FileObject)
meth public abstract java.util.List<org.netbeans.modules.web.el.spi.Function> getFunctions(org.openide.filesystems.FileObject)
meth public abstract java.util.List<org.netbeans.modules.web.el.spi.ResourceBundle> getResourceBundles(org.openide.filesystems.FileObject,org.netbeans.modules.web.el.spi.ResolverContext)
meth public boolean isValidProperty(javax.lang.model.element.ExecutableElement,org.netbeans.modules.parsing.api.Source,org.netbeans.modules.csl.api.CodeCompletionContext,org.netbeans.modules.web.el.CompilationContext)
supr java.lang.Object

CLSS public static org.netbeans.modules.web.el.spi.ELPlugin$Query
 outer org.netbeans.modules.web.el.spi.ELPlugin
cons public init()
meth public static boolean isValidProperty(javax.lang.model.element.ExecutableElement,org.netbeans.modules.parsing.api.Source,org.netbeans.modules.web.el.CompilationContext,org.netbeans.modules.csl.api.CodeCompletionContext)
meth public static java.util.Collection<? extends org.netbeans.modules.web.el.spi.ELPlugin> getELPlugins()
meth public static java.util.Collection<org.netbeans.modules.web.el.spi.ImplicitObject> getImplicitObjects(org.openide.filesystems.FileObject)
meth public static java.util.List<org.netbeans.modules.web.el.spi.Function> getFunctions(org.openide.filesystems.FileObject)
meth public static java.util.List<org.netbeans.modules.web.el.spi.ResourceBundle> getResourceBundles(org.openide.filesystems.FileObject,org.netbeans.modules.web.el.spi.ResolverContext)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.el.spi.ELVariableResolver
innr public final static FieldInfo
innr public final static VariableInfo
meth public abstract java.lang.String getBeanName(java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.web.el.spi.ResolverContext)
meth public abstract java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo> getBeansInScope(java.lang.String,org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.web.el.spi.ResolverContext)
meth public abstract java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo> getManagedBeans(org.openide.filesystems.FileObject,org.netbeans.modules.web.el.spi.ResolverContext)
meth public abstract java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo> getRawObjectProperties(java.lang.String,org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.web.el.spi.ResolverContext)
meth public abstract java.util.List<org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo> getVariables(org.netbeans.modules.parsing.api.Snapshot,int,org.netbeans.modules.web.el.spi.ResolverContext)
meth public abstract org.netbeans.modules.web.el.spi.ELVariableResolver$FieldInfo getInjectableField(java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.web.el.spi.ResolverContext)

CLSS public final static org.netbeans.modules.web.el.spi.ELVariableResolver$FieldInfo
 outer org.netbeans.modules.web.el.spi.ELVariableResolver
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getEnclosingClass()
meth public java.lang.String getType()
supr java.lang.Object
hfds enclosingClass,type

CLSS public final static org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo
 outer org.netbeans.modules.web.el.spi.ELVariableResolver
fld public final java.lang.String clazz
fld public final java.lang.String expression
fld public final java.lang.String name
meth public static org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo createResolvedVariable(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo createUnresolvedVariable(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.web.el.spi.ELVariableResolver$VariableInfo createVariable(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.web.el.spi.Function
cons public init(java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.lang.String)
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String getReturnType()
meth public java.util.List<java.lang.String> getParameters()
supr java.lang.Object
hfds description,name,parameters,returnType

CLSS public abstract interface org.netbeans.modules.web.el.spi.ImplicitObject
meth public abstract java.lang.String getClazz()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.web.el.spi.ImplicitObjectType getType()

CLSS public final !enum org.netbeans.modules.web.el.spi.ImplicitObjectType
fld public final static java.util.List<org.netbeans.modules.web.el.spi.ImplicitObjectType> ALL_TYPES
fld public final static org.netbeans.modules.web.el.spi.ImplicitObjectType MAP_TYPE
fld public final static org.netbeans.modules.web.el.spi.ImplicitObjectType OBJECT_TYPE
fld public final static org.netbeans.modules.web.el.spi.ImplicitObjectType RAW
fld public final static org.netbeans.modules.web.el.spi.ImplicitObjectType SCOPE_TYPE
meth public static org.netbeans.modules.web.el.spi.ImplicitObjectType valueOf(java.lang.String)
meth public static org.netbeans.modules.web.el.spi.ImplicitObjectType[] values()
supr java.lang.Enum<org.netbeans.modules.web.el.spi.ImplicitObjectType>

CLSS public final org.netbeans.modules.web.el.spi.ResolverContext
cons public init()
meth public java.lang.Object getContent(java.lang.String)
meth public void setContent(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds context

CLSS public final org.netbeans.modules.web.el.spi.ResourceBundle
cons public init(java.lang.String,java.lang.String,java.util.List<org.openide.filesystems.FileObject>)
meth public java.lang.String getBaseName()
meth public java.lang.String getVar()
meth public java.util.List<org.openide.filesystems.FileObject> getFiles()
supr java.lang.Object
hfds baseName,files,var

