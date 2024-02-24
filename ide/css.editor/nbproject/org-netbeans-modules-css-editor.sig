#Signature file v4.1
#Version 1.91

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

CLSS public abstract org.netbeans.modules.css.editor.module.spi.Browser
cons public init()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getRenderingEngineId()
meth public abstract java.lang.String getVendor()
meth public abstract java.lang.String getVendorSpecificPropertyId()
meth public abstract java.net.URL getActiveIcon()
meth public abstract java.net.URL getInactiveIcon()
meth public abstract org.netbeans.modules.css.lib.api.properties.PropertyCategory getPropertyCategory()
meth public final java.lang.String getVendorSpecificPropertyPrefix()
supr java.lang.Object

CLSS public org.netbeans.modules.css.editor.module.spi.CompletionContext
cons public init(org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.CssParserResult,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.css.lib.api.CssTokenId>,int,int,org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType,int,int,int,int,java.lang.String,java.lang.String)
meth public boolean isCssPreprocessorSource()
meth public int getActiveTokenDiff()
meth public int getAnchorOffset()
meth public int getEmbeddedAnchorOffset()
meth public int getEmbeddedCaretOffset()
meth public java.lang.String getPrefix()
meth public java.lang.String getSourceFileMimetype()
meth public org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.css.lib.api.CssTokenId> getTokenSequence()
meth public org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType getQueryType()
meth public org.netbeans.modules.css.lib.api.CssTokenId getActiveTokenId()
meth public org.netbeans.modules.css.lib.api.CssTokenId getNonWhiteTokenIdBackward()
meth public org.netbeans.modules.css.lib.api.Node getActiveNode()
meth public org.netbeans.modules.css.lib.api.Node getActiveTokenNode()
meth public org.netbeans.modules.css.lib.api.Node getNodeForNonWhiteTokenBackward()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void restoreTokenSequence()
supr org.netbeans.modules.css.editor.module.spi.EditorFeatureContext
hfds activeNode,activeTokenDiff,activeTokenNode,anchorOffset,embeddedAnchorOffset,embeddedCaretOffset,prefix,queryType,sourceFileMimetype,tokenSequence,tsIndex

CLSS public abstract org.netbeans.modules.css.editor.module.spi.CssCompletionItem
cons protected init(org.netbeans.modules.csl.api.ElementHandle,java.lang.String,int,boolean)
fld protected boolean addSemicolon
fld protected final static int SORT_PRIORITY = 300
meth public boolean isSmart()
meth public int getAnchorOffset()
meth public int getSortPrioOverride()
meth public java.lang.String getCustomInsertTemplate()
meth public java.lang.String getInsertPrefix()
meth public java.lang.String getLhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getSortText()
meth public java.lang.String toString()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
meth public static org.netbeans.modules.csl.api.CompletionProposal createColorChooserCompletionItem(int,java.lang.String,boolean)
meth public static org.netbeans.modules.csl.api.CompletionProposal createUnitCompletionItem(org.netbeans.modules.css.lib.api.properties.UnitGrammarElement)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createColorValueCompletionItem(org.netbeans.modules.css.editor.csl.CssValueElement,org.netbeans.modules.css.lib.api.properties.GrammarElement,int,boolean,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createFileCompletionItem(org.netbeans.modules.css.editor.csl.CssElement,java.lang.String,int,java.awt.Color,javax.swing.ImageIcon,boolean,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createHashColorCompletionItem(org.netbeans.modules.css.editor.csl.CssElement,java.lang.String,java.lang.String,int,boolean,boolean,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createPropertyCompletionItem(org.netbeans.modules.css.editor.csl.CssElement,org.netbeans.modules.css.lib.api.properties.PropertyDefinition,java.lang.String,int,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createRAWCompletionItem(org.netbeans.modules.css.editor.csl.CssElement,java.lang.String,org.netbeans.modules.csl.api.ElementKind,int,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createSelectorCompletionItem(org.netbeans.modules.css.editor.csl.CssElement,java.lang.String,int,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createSelectorCompletionItem(org.netbeans.modules.css.editor.csl.CssElement,java.lang.String,int,boolean,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createValueCompletionItem(org.netbeans.modules.css.editor.csl.CssValueElement,java.lang.String,java.lang.String,int,boolean,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createValueCompletionItem(org.netbeans.modules.css.editor.csl.CssValueElement,org.netbeans.modules.css.lib.api.properties.GrammarElement,int,boolean,boolean)
meth public static org.netbeans.modules.css.editor.module.spi.CssCompletionItem createValueCompletionItem(org.netbeans.modules.css.editor.csl.CssValueElement,org.netbeans.modules.css.lib.api.properties.ValueGrammarElement,java.lang.String,int,boolean,boolean)
supr org.netbeans.modules.csl.spi.DefaultCompletionProposal
hfds anchorOffset,element,value
hcls ColorChooserItem,ColorCompletionItem,FileCompletionItem,HashColorCompletionItem,PropertyCompletionItem,RAWCompletionItem,SelectorCompletionItem,UnitItem,ValueCompletionItem

CLSS public abstract org.netbeans.modules.css.editor.module.spi.CssEditorModule
cons public init()
meth public <%0 extends java.util.List<org.netbeans.modules.csl.api.StructureItem>> org.netbeans.modules.css.lib.api.NodeVisitor<{%%0}> getStructureItemsNodeVisitor(org.netbeans.modules.css.editor.module.spi.FeatureContext,{%%0})
meth public <%0 extends java.util.Map<java.lang.String,java.util.List<org.netbeans.modules.csl.api.OffsetRange>>> org.netbeans.modules.css.lib.api.NodeVisitor<{%%0}> getFoldsNodeVisitor(org.netbeans.modules.css.editor.module.spi.FeatureContext,{%%0})
meth public <%0 extends java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.util.Set<org.netbeans.modules.csl.api.ColoringAttributes>>> org.netbeans.modules.css.lib.api.NodeVisitor<{%%0}> getSemanticHighlightingNodeVisitor(org.netbeans.modules.css.editor.module.spi.FeatureContext,{%%0})
meth public <%0 extends java.util.Set<org.netbeans.modules.csl.api.OffsetRange>> org.netbeans.modules.css.lib.api.NodeVisitor<{%%0}> getInstantRenamerVisitor(org.netbeans.modules.css.editor.module.spi.EditorFeatureContext,{%%0})
meth public <%0 extends java.util.Set<org.netbeans.modules.csl.api.OffsetRange>> org.netbeans.modules.css.lib.api.NodeVisitor<{%%0}> getMarkOccurrencesNodeVisitor(org.netbeans.modules.css.editor.module.spi.EditorFeatureContext,{%%0})
meth public boolean isInstantRenameAllowed(org.netbeans.modules.css.editor.module.spi.EditorFeatureContext)
meth public java.util.Collection<java.lang.String> getPropertyNames(org.openide.filesystems.FileObject)
meth public java.util.Collection<java.lang.String> getPseudoClasses(org.netbeans.modules.css.editor.module.spi.EditorFeatureContext)
meth public java.util.Collection<java.lang.String> getPseudoElements(org.netbeans.modules.css.editor.module.spi.EditorFeatureContext)
meth public java.util.Collection<org.netbeans.modules.css.editor.module.spi.Browser> getExtraBrowsers(org.openide.filesystems.FileObject)
meth public java.util.Collection<org.netbeans.modules.css.editor.module.spi.HelpResolver> getHelpResolvers(org.openide.filesystems.FileObject)
meth public java.util.List<org.netbeans.modules.csl.api.CompletionProposal> getCompletionProposals(org.netbeans.modules.css.editor.module.spi.CompletionContext)
meth public org.netbeans.modules.css.editor.module.spi.PropertySupportResolver$Factory getPropertySupportResolverFactory()
meth public org.netbeans.modules.css.editor.module.spi.SemanticAnalyzer getSemanticAnalyzer()
meth public org.netbeans.modules.css.lib.api.properties.PropertyDefinition getPropertyDefinition(java.lang.String)
meth public org.openide.util.Pair<org.netbeans.modules.csl.api.OffsetRange,org.netbeans.modules.css.editor.module.spi.FutureParamTask<org.netbeans.modules.csl.api.DeclarationFinder$DeclarationLocation,org.netbeans.modules.css.editor.module.spi.EditorFeatureContext>> getDeclaration(javax.swing.text.Document,int)
supr java.lang.Object

CLSS public org.netbeans.modules.css.editor.module.spi.EditorFeatureContext
cons public init(org.netbeans.modules.css.lib.api.CssParserResult,int)
meth public int getCaretOffset()
meth public javax.swing.text.Document getDocument()
supr org.netbeans.modules.css.editor.module.spi.FeatureContext
hfds caretOffset

CLSS public org.netbeans.modules.css.editor.module.spi.FeatureCancel
cons public init()
meth public boolean isCancelled()
meth public void attachCancelAction(java.lang.Runnable)
meth public void cancel()
supr java.lang.Object
hfds cancelActions,isCancelled

CLSS public org.netbeans.modules.css.editor.module.spi.FeatureContext
cons public init(org.netbeans.modules.css.lib.api.CssParserResult)
meth public org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.css.lib.api.CssTokenId> getTokenSequence()
meth public org.netbeans.modules.css.lib.api.CssParserResult getParserResult()
meth public org.netbeans.modules.css.lib.api.Node getParseTreeRoot()
meth public org.netbeans.modules.css.model.api.Model getSourceModel()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public org.netbeans.modules.parsing.api.Source getSource()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds result

CLSS public abstract interface org.netbeans.modules.css.editor.module.spi.FutureParamTask<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {org.netbeans.modules.css.editor.module.spi.FutureParamTask%0} run({org.netbeans.modules.css.editor.module.spi.FutureParamTask%1})

CLSS public abstract org.netbeans.modules.css.editor.module.spi.HelpResolver
cons public init()
meth public abstract int getPriority()
meth public abstract java.lang.String getHelp(org.openide.filesystems.FileObject,org.netbeans.modules.css.lib.api.properties.PropertyDefinition)
meth public abstract java.net.URL resolveLink(org.openide.filesystems.FileObject,org.netbeans.modules.css.lib.api.properties.PropertyDefinition,java.lang.String)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.css.editor.module.spi.PropertySupportResolver
cons public init()
innr public abstract interface static Factory
meth public abstract boolean isPropertySupported(java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.css.editor.module.spi.PropertySupportResolver$Factory
 outer org.netbeans.modules.css.editor.module.spi.PropertySupportResolver
meth public abstract org.netbeans.modules.css.editor.module.spi.PropertySupportResolver createPropertySupportResolver(org.netbeans.modules.css.editor.module.spi.Browser)

CLSS public abstract org.netbeans.modules.css.editor.module.spi.SemanticAnalyzer
cons public init()
meth public org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult analyzeDeclaration(org.netbeans.modules.css.lib.api.Node)
supr java.lang.Object

CLSS public final org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult
fld public final static org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult UNKNOWN
fld public final static org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult VALID
innr public final static !enum Type
meth public java.util.Collection<org.netbeans.modules.css.lib.api.ProblemDescription> getProblems()
meth public org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult$Type getType()
meth public static org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult createErroneousResult(java.util.Collection<org.netbeans.modules.css.lib.api.ProblemDescription>)
supr java.lang.Object
hfds problems,type

CLSS public final static !enum org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult$Type
 outer org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult
fld public final static org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult$Type ERRONEOUS
fld public final static org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult$Type UNKNOWN
fld public final static org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult$Type VALID
meth public static org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult$Type[] values()
supr java.lang.Enum<org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult$Type>

CLSS public org.netbeans.modules.css.editor.module.spi.Utilities
fld public final static java.lang.String CATEGORY_META_PROPERTY_NAME = "$category"
meth public !varargs static <%0 extends java.util.Set<org.netbeans.modules.csl.api.OffsetRange>> org.netbeans.modules.css.lib.api.NodeVisitor<{%%0}> createMarkOccurrencesNodeVisitor(org.netbeans.modules.css.editor.module.spi.EditorFeatureContext,{%%0},org.netbeans.modules.css.lib.api.NodeType[])
meth public static boolean isVendorSpecificPropertyValueToken(org.openide.filesystems.FileObject,java.lang.CharSequence)
meth public static java.util.List<org.netbeans.modules.csl.api.CompletionProposal> createRAWCompletionProposals(java.util.Collection<java.lang.String>,org.netbeans.modules.csl.api.ElementKind,int)
meth public static java.util.List<org.netbeans.modules.csl.api.CompletionProposal> createRAWCompletionProposals(java.util.Collection<java.lang.String>,org.netbeans.modules.csl.api.ElementKind,int,java.lang.String)
meth public static java.util.List<org.netbeans.modules.csl.api.CompletionProposal> filterCompletionProposals(java.util.List<org.netbeans.modules.csl.api.CompletionProposal>,java.lang.CharSequence,boolean)
meth public static java.util.List<org.netbeans.modules.csl.api.CompletionProposal> wrapProperties(java.util.Collection<org.netbeans.modules.css.lib.api.properties.PropertyDefinition>,int)
meth public static java.util.List<org.netbeans.modules.csl.api.CompletionProposal> wrapProperties(java.util.Collection<org.netbeans.modules.css.lib.api.properties.PropertyDefinition>,int,int)
meth public static java.util.Map<java.lang.String,org.netbeans.modules.css.lib.api.properties.PropertyDefinition> parsePropertyDefinitionFile(java.lang.String,org.netbeans.modules.css.lib.api.CssModule)
supr java.lang.Object

CLSS public org.netbeans.modules.css.indexing.api.CssIndex
innr public static AllDependenciesMaps
meth public <%0 extends org.netbeans.modules.css.indexing.api.CssIndexModel> java.util.Map<org.openide.filesystems.FileObject,{%%0}> getIndexModels(java.lang.Class<org.netbeans.modules.css.indexing.api.CssIndexModelFactory<{%%0}>>) throws java.io.IOException
meth public <%0 extends org.netbeans.modules.css.indexing.api.CssIndexModel> {%%0} getIndexModel(java.lang.Class,org.openide.filesystems.FileObject) throws java.io.IOException
meth public java.util.Collection<org.openide.filesystems.FileObject> find(org.netbeans.modules.css.refactoring.api.RefactoringElementType,java.lang.String)
meth public java.util.Collection<org.openide.filesystems.FileObject> findClassDeclarations(java.lang.String)
meth public java.util.Collection<org.openide.filesystems.FileObject> findClasses(java.lang.String)
meth public java.util.Collection<org.openide.filesystems.FileObject> findColor(java.lang.String)
meth public java.util.Collection<org.openide.filesystems.FileObject> findHtmlElement(java.lang.String)
meth public java.util.Collection<org.openide.filesystems.FileObject> findIdDeclarations(java.lang.String)
meth public java.util.Collection<org.openide.filesystems.FileObject> findIds(java.lang.String)
meth public java.util.Collection<org.openide.filesystems.FileObject> getAllIndexedFiles()
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>> findAll(org.netbeans.modules.css.refactoring.api.RefactoringElementType)
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>> findAllClassDeclarations()
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>> findAllIdDeclarations()
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>> findByPrefix(org.netbeans.modules.css.refactoring.api.RefactoringElementType,java.lang.String)
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>> findClassesByPrefix(java.lang.String)
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>> findColorsByPrefix(java.lang.String)
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>> findIdsByPrefix(java.lang.String)
meth public org.netbeans.modules.css.indexing.api.CssIndex$AllDependenciesMaps getAllDependencies() throws java.io.IOException
meth public org.netbeans.modules.web.common.api.DependenciesGraph getDependencies(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.css.indexing.api.CssIndex create(org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.netbeans.modules.css.indexing.api.CssIndex get(org.netbeans.api.project.Project) throws java.io.IOException
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void notifyChange()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds INDEXES,LOGGER,REGEXP_CHARS_TO_ENCODE,SASS_EXT,SCSS_EXT,VIRTUAL_ELEMENT_MARKER_STR,allDepsCache,allDepsCache_hashCode,changeSupport,querySupport,sourceRoots

CLSS public static org.netbeans.modules.css.indexing.api.CssIndex$AllDependenciesMaps
 outer org.netbeans.modules.css.indexing.api.CssIndex
cons public init(java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>>,java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>>)
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>> getDest2source()
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>> getSource2dest()
supr java.lang.Object
hfds dest2source,source2dest

CLSS public abstract org.netbeans.modules.css.indexing.api.CssIndexModel
cons public init()
meth public abstract void storeToIndex(org.netbeans.modules.parsing.spi.indexing.support.IndexDocument)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.css.indexing.api.CssIndexModelFactory<%0 extends org.netbeans.modules.css.indexing.api.CssIndexModel>
cons public init()
meth public abstract java.util.Collection<java.lang.String> getIndexKeys()
meth public abstract {org.netbeans.modules.css.indexing.api.CssIndexModelFactory%0} getModel(org.netbeans.modules.css.lib.api.CssParserResult)
meth public abstract {org.netbeans.modules.css.indexing.api.CssIndexModelFactory%0} loadFromIndex(org.netbeans.modules.parsing.spi.indexing.support.IndexResult)
supr java.lang.Object

CLSS public org.netbeans.modules.css.refactoring.api.CssRefactoring
meth public static java.util.Collection<org.netbeans.modules.css.refactoring.api.Entry> getAllSelectors(org.openide.filesystems.FileObject,org.netbeans.modules.css.refactoring.api.RefactoringElementType)
meth public static java.util.Collection<org.openide.filesystems.FileObject> findAllStyleSheets(org.openide.filesystems.FileObject)
meth public static java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.css.refactoring.api.EntryHandle>> findAllOccurances(java.lang.String,org.netbeans.modules.css.refactoring.api.RefactoringElementType,org.openide.filesystems.FileObject,boolean)
supr java.lang.Object

CLSS public org.netbeans.modules.css.refactoring.api.CssRefactoringExtraInfo
cons public init()
meth public boolean isRefactorAll()
meth public void setRefactorAll(boolean)
supr java.lang.Object
hfds refactorAll

CLSS public org.netbeans.modules.css.refactoring.api.CssRefactoringInfo
cons public init(org.openide.filesystems.FileObject,java.lang.String,org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type)
innr public final static !enum Type
meth public java.lang.String getElementName()
meth public org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type getType()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds fileObject,name,type

CLSS public final static !enum org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type
 outer org.netbeans.modules.css.refactoring.api.CssRefactoringInfo
fld public final static org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type CLASS
fld public final static org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type ELEMENT
fld public final static org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type HEX_COLOR
fld public final static org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type ID
fld public final static org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type RESOURCE_IDENTIFIER
fld public final static org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type URI
meth public static org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type[] values()
supr java.lang.Enum<org.netbeans.modules.css.refactoring.api.CssRefactoringInfo$Type>

CLSS public abstract interface org.netbeans.modules.css.refactoring.api.Entry
meth public abstract boolean isValidInSourceDocument()
meth public abstract boolean isVirtual()
meth public abstract int getLineOffset()
meth public abstract java.lang.CharSequence getLineText()
meth public abstract java.lang.CharSequence getText()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getBodyRange()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getDocumentBodyRange()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getDocumentRange()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getRange()

CLSS public final org.netbeans.modules.css.refactoring.api.EntryHandle
meth public boolean isRelatedEntry()
meth public java.lang.String toString()
meth public org.netbeans.modules.css.refactoring.api.Entry entry()
meth public static org.netbeans.modules.css.refactoring.api.EntryHandle createEntryHandle(org.netbeans.modules.css.refactoring.api.Entry,boolean)
supr java.lang.Object
hfds entry,isRelatedEntry

CLSS public final !enum org.netbeans.modules.css.refactoring.api.RefactoringElementType
fld public final static org.netbeans.modules.css.refactoring.api.RefactoringElementType CLASS
fld public final static org.netbeans.modules.css.refactoring.api.RefactoringElementType COLOR
fld public final static org.netbeans.modules.css.refactoring.api.RefactoringElementType ELEMENT
fld public final static org.netbeans.modules.css.refactoring.api.RefactoringElementType ID
fld public final static org.netbeans.modules.css.refactoring.api.RefactoringElementType IMPORT
meth public java.lang.String getIndexKey()
meth public static org.netbeans.modules.css.refactoring.api.RefactoringElementType valueOf(java.lang.String)
meth public static org.netbeans.modules.css.refactoring.api.RefactoringElementType[] values()
supr java.lang.Enum<org.netbeans.modules.css.refactoring.api.RefactoringElementType>
hfds indexKey

