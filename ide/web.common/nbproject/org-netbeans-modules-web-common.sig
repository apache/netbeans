#Signature file v4.1
#Version 1.122

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

CLSS public abstract interface java.util.EventListener

CLSS public final org.netbeans.modules.web.common.api.ByteStack
cons public init()
cons public init(int)
fld public final static byte DEFAULT_SIZE = 5
meth public boolean contains(int)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int peek()
meth public int pop()
meth public int size()
meth public java.lang.String toString()
meth public org.netbeans.modules.web.common.api.ByteStack copyOf()
meth public void clear()
meth public void copyFrom(org.netbeans.modules.web.common.api.ByteStack)
meth public void push(int)
supr java.lang.Object
hfds LOGGER,data,index

CLSS public org.netbeans.modules.web.common.api.Constants
cons public init()
fld public final static java.lang.String LANGUAGE_SNIPPET_SEPARATOR = "@@@"
supr java.lang.Object

CLSS public final org.netbeans.modules.web.common.api.CssPreprocessor
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.modules.web.common.api.CssPreprocessors
fld public final static java.lang.String PREPROCESSORS_PATH = "CSS/PreProcessors"
meth public org.netbeans.modules.web.common.api.CssPreprocessor getCssPreprocessor(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.web.common.api.CssPreprocessors getDefault()
meth public void addCssPreprocessorsListener(org.netbeans.modules.web.common.api.CssPreprocessorsListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void process(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void process(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public void process(org.netbeans.modules.web.common.api.CssPreprocessor,org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void process(org.netbeans.modules.web.common.api.CssPreprocessor,org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public void removeCssPreprocessorsListener(org.netbeans.modules.web.common.api.CssPreprocessorsListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds INSTANCE,PREPROCESSORS,RP,listenersSupport,preprocessorImplementationsListener,preprocessors
hcls PreprocessorImplementationsListener

CLSS public abstract interface org.netbeans.modules.web.common.api.CssPreprocessorsListener
innr public final static Support
intf java.util.EventListener
meth public abstract void customizerChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.common.api.CssPreprocessor)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void optionsChanged(org.netbeans.modules.web.common.api.CssPreprocessor)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void preprocessorsChanged()
meth public abstract void processingErrorOccured(org.netbeans.api.project.Project,org.netbeans.modules.web.common.api.CssPreprocessor,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.web.common.api.CssPreprocessorsListener$Support
 outer org.netbeans.modules.web.common.api.CssPreprocessorsListener
cons public init()
meth public boolean hasListeners()
meth public void addCssPreprocessorListener(org.netbeans.modules.web.common.api.CssPreprocessorsListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void fireCustomizerChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.common.api.CssPreprocessor)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void fireOptionsChanged(org.netbeans.modules.web.common.api.CssPreprocessor)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void firePreprocessorsChanged()
meth public void fireProcessingErrorOccured(org.netbeans.api.project.Project,org.netbeans.modules.web.common.api.CssPreprocessor,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void removeCssPreprocessorListener(org.netbeans.modules.web.common.api.CssPreprocessorsListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds listeners

CLSS public org.netbeans.modules.web.common.api.DependenciesGraph
cons public init(org.openide.filesystems.FileObject)
innr public Node
meth public java.util.Collection<org.openide.filesystems.FileObject> getAllReferedFiles()
meth public java.util.Collection<org.openide.filesystems.FileObject> getAllReferingFiles()
meth public java.util.Collection<org.openide.filesystems.FileObject> getAllRelatedFiles()
meth public java.util.Collection<org.openide.filesystems.FileObject> getFiles(org.netbeans.modules.web.common.api.DependencyType)
meth public org.netbeans.modules.web.common.api.DependenciesGraph$Node getNode(org.openide.filesystems.FileObject)
meth public org.netbeans.modules.web.common.api.DependenciesGraph$Node getSourceNode()
supr java.lang.Object
hfds LOGGER,file2node,sourceNode

CLSS public org.netbeans.modules.web.common.api.DependenciesGraph$Node
 outer org.netbeans.modules.web.common.api.DependenciesGraph
meth public boolean addReferedNode(org.netbeans.modules.web.common.api.DependenciesGraph$Node)
meth public boolean addReferingNode(org.netbeans.modules.web.common.api.DependenciesGraph$Node)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.web.common.api.DependenciesGraph$Node> getReferedNodes()
meth public java.util.Collection<org.netbeans.modules.web.common.api.DependenciesGraph$Node> getReferingNodes()
meth public org.netbeans.modules.web.common.api.DependenciesGraph getDependencyGraph()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds refered,refering,source

CLSS public final !enum org.netbeans.modules.web.common.api.DependencyType
fld public final static org.netbeans.modules.web.common.api.DependencyType REFERRED
fld public final static org.netbeans.modules.web.common.api.DependencyType REFERRING
fld public final static org.netbeans.modules.web.common.api.DependencyType REFERRING_AND_REFERRED
meth public static org.netbeans.modules.web.common.api.DependencyType valueOf(java.lang.String)
meth public static org.netbeans.modules.web.common.api.DependencyType[] values()
supr java.lang.Enum<org.netbeans.modules.web.common.api.DependencyType>

CLSS public org.netbeans.modules.web.common.api.DependentFileQuery
cons public init()
meth public static boolean isDependent(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
supr java.lang.Object
hfds lookup

CLSS public final org.netbeans.modules.web.common.api.FileReference
meth public java.lang.String linkPath()
meth public java.lang.String optimizedLinkPath()
meth public java.util.List<org.openide.filesystems.FileObject> sourcePathMembersToBase()
meth public java.util.List<org.openide.filesystems.FileObject> targetPathMembersToBase()
meth public org.netbeans.modules.web.common.api.FileReferenceModification createModification()
meth public org.netbeans.modules.web.common.api.FileReferenceType type()
meth public org.openide.filesystems.FileObject baseFolder()
meth public org.openide.filesystems.FileObject source()
meth public org.openide.filesystems.FileObject target()
supr java.lang.Object
hfds DESCENDING_PATH_ITEM,baseFolder,linkPath,source,target,type

CLSS public org.netbeans.modules.web.common.api.FileReferenceModification
meth public boolean rename(org.openide.filesystems.FileObject,java.lang.String)
meth public java.lang.String getModifiedReferencePath()
supr java.lang.Object
hfds absolutePathLink,items

CLSS public final !enum org.netbeans.modules.web.common.api.FileReferenceType
fld public final static org.netbeans.modules.web.common.api.FileReferenceType ABSOLUTE
fld public final static org.netbeans.modules.web.common.api.FileReferenceType RELATIVE
meth public static org.netbeans.modules.web.common.api.FileReferenceType valueOf(java.lang.String)
meth public static org.netbeans.modules.web.common.api.FileReferenceType[] values()
supr java.lang.Enum<org.netbeans.modules.web.common.api.FileReferenceType>

CLSS public org.netbeans.modules.web.common.api.LexerUtils
cons public init()
meth public !varargs static org.netbeans.api.lexer.Token followsToken(org.netbeans.api.lexer.TokenSequence,java.util.Collection<? extends org.netbeans.api.lexer.TokenId>,boolean,boolean,boolean,org.netbeans.api.lexer.TokenId[])
meth public !varargs static org.netbeans.api.lexer.Token followsToken(org.netbeans.api.lexer.TokenSequence,java.util.Collection<? extends org.netbeans.api.lexer.TokenId>,boolean,boolean,org.netbeans.api.lexer.TokenId[])
meth public !varargs static org.netbeans.api.lexer.Token followsToken(org.netbeans.api.lexer.TokenSequence,org.netbeans.api.lexer.TokenId,boolean,boolean,org.netbeans.api.lexer.TokenId[])
meth public static boolean endsWith(java.lang.CharSequence,java.lang.CharSequence,boolean,boolean)
meth public static boolean equals(java.lang.CharSequence,java.lang.CharSequence,boolean,boolean)
meth public static boolean startsWith(java.lang.CharSequence,java.lang.CharSequence,boolean,boolean)
meth public static int getLineBeginningOffset(java.lang.CharSequence,int) throws javax.swing.text.BadLocationException
meth public static int getLineOffset(java.lang.CharSequence,int) throws javax.swing.text.BadLocationException
meth public static int[] findLineBoundaries(java.lang.CharSequence,int)
meth public static java.lang.CharSequence trim(java.lang.CharSequence)
meth public static org.netbeans.api.lexer.TokenSequence getJoinedTokenSequence(javax.swing.text.Document,int,org.netbeans.api.lexer.Language)
meth public static org.netbeans.api.lexer.TokenSequence getTokenSequence(javax.swing.text.Document,int,org.netbeans.api.lexer.Language,boolean)
meth public static org.netbeans.api.lexer.TokenSequence getTokenSequence(org.netbeans.api.lexer.TokenHierarchy,int,org.netbeans.api.lexer.Language,boolean)
meth public static void rebuildTokenHierarchy(javax.swing.text.Document)
supr java.lang.Object

CLSS public org.netbeans.modules.web.common.api.Lines
cons public init(java.lang.CharSequence)
meth public int getLineIndex(int) throws javax.swing.text.BadLocationException
meth public int getLineOffset(int)
meth public int getLinesCount()
supr java.lang.Object
hfds lines,text

CLSS public final org.netbeans.modules.web.common.api.RemoteFileCache
cons public init()
meth public static java.net.URL isRemoteFile(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject getRemoteFile(java.net.URL) throws java.io.IOException
supr java.lang.Object

CLSS public final org.netbeans.modules.web.common.api.ServerURLMapping
fld public final static int CONTEXT_PROJECT_SOURCES = 1
fld public final static int CONTEXT_PROJECT_TESTS = 2
meth public static java.net.URL toServer(org.netbeans.api.project.Project,int,org.openide.filesystems.FileObject)
meth public static java.net.URL toServer(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject fromServer(org.netbeans.api.project.Project,int,java.net.URL)
meth public static org.openide.filesystems.FileObject fromServer(org.netbeans.api.project.Project,java.net.URL)
supr java.lang.Object

CLSS public final org.netbeans.modules.web.common.api.UsageLogger
innr public final static Builder
meth public !varargs void log(java.lang.Class<?>,java.lang.String,java.lang.Object[])
meth public !varargs void log(java.lang.Object[])
meth public boolean isLoggingEnabled()
meth public static org.netbeans.modules.web.common.api.UsageLogger jsTestRunUsageLogger(java.lang.String)
meth public static org.netbeans.modules.web.common.api.UsageLogger projectBrowserUsageLogger(java.lang.String)
meth public void reset()
supr java.lang.Object
hfds firstMessageOnly,logger,message,srcClass

CLSS public final static org.netbeans.modules.web.common.api.UsageLogger$Builder
 outer org.netbeans.modules.web.common.api.UsageLogger
cons public init(java.lang.String)
meth public org.netbeans.modules.web.common.api.UsageLogger create()
meth public org.netbeans.modules.web.common.api.UsageLogger$Builder firstMessageOnly(boolean)
meth public org.netbeans.modules.web.common.api.UsageLogger$Builder message(java.lang.Class<?>,java.lang.String)
supr java.lang.Object
hfds firstMessageOnly,loggerName,message,srcClass

CLSS public final org.netbeans.modules.web.common.api.ValidationResult
cons public init()
cons public init(org.netbeans.modules.web.common.api.ValidationResult)
innr public final static Message
meth public boolean hasErrors()
meth public boolean hasWarnings()
meth public boolean isFaultless()
meth public java.lang.String getFirstErrorMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getFirstWarningMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.List<org.netbeans.modules.web.common.api.ValidationResult$Message> getErrors()
meth public java.util.List<org.netbeans.modules.web.common.api.ValidationResult$Message> getWarnings()
meth public void addError(org.netbeans.modules.web.common.api.ValidationResult$Message)
meth public void addWarning(org.netbeans.modules.web.common.api.ValidationResult$Message)
meth public void merge(org.netbeans.modules.web.common.api.ValidationResult)
supr java.lang.Object
hfds errors,warnings

CLSS public final static org.netbeans.modules.web.common.api.ValidationResult$Message
 outer org.netbeans.modules.web.common.api.ValidationResult
cons public init(java.lang.Object,java.lang.String)
meth public java.lang.Object getSource()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
supr java.lang.Object
hfds message,source

CLSS public abstract interface org.netbeans.modules.web.common.api.ValueCompletion<%0 extends java.lang.Object>
meth public abstract java.util.List<{org.netbeans.modules.web.common.api.ValueCompletion%0}> getItems(org.openide.filesystems.FileObject,int,java.lang.String)

CLSS public final org.netbeans.modules.web.common.api.Version
meth public boolean equals(java.lang.Object)
meth public boolean isAboveOrEqual(org.netbeans.modules.web.common.api.Version)
meth public boolean isBelowOrEqual(org.netbeans.modules.web.common.api.Version)
meth public int hashCode()
meth public java.lang.Integer getMajor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.Integer getMicro()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.Integer getMinor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.Integer getUpdate()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getQualifier()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public static org.netbeans.modules.web.common.api.Version fromDottedNotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.common.api.Version fromJsr277NotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.common.api.Version fromJsr277OrDottedNotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DOTTED_PATTERN,JSR277_PATTERN,majorNumber,microNumber,minorNumber,qualifier,updateNumber,version

CLSS public final org.netbeans.modules.web.common.api.WebPageMetadata
cons public init(java.util.Map<java.lang.String,?>)
fld public final static java.lang.String MIMETYPE = "mimeType"
meth public java.lang.Object value(java.lang.String)
meth public java.util.Collection<java.lang.String> keys()
meth public static java.lang.String getContentMimeType(org.netbeans.modules.parsing.spi.Parser$Result,boolean)
meth public static org.netbeans.modules.web.common.api.WebPageMetadata getMetadata(org.openide.util.Lookup)
supr java.lang.Object
hfds metamap

CLSS public final org.netbeans.modules.web.common.api.WebServer
meth public int getPort()
meth public java.net.URL toServer(org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject fromServer(java.net.URL)
meth public static org.netbeans.modules.web.common.api.WebServer getWebserver()
meth public void start(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String)
meth public void stop(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOGGER,PORT,deployedApps,init,server,webServer
hcls Pair,Server

CLSS public org.netbeans.modules.web.common.api.WebUtils
cons public init()
meth public static boolean hasWebRoot(org.netbeans.api.project.Project)
meth public static boolean isValueQuoted(java.lang.CharSequence)
meth public static java.lang.String getRelativePath(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public static java.lang.String unquotedValue(java.lang.CharSequence)
meth public static java.lang.String urlToString(java.net.URL)
meth public static java.lang.String urlToString(java.net.URL,boolean)
meth public static java.net.InetAddress getLocalhostInetAddress()
meth public static java.net.URL stringToUrl(java.lang.String)
meth public static org.netbeans.modules.parsing.api.ResultIterator getResultIterator(org.netbeans.modules.parsing.api.ResultIterator,java.lang.String)
meth public static org.netbeans.modules.web.common.api.FileReference resolveToReference(org.openide.filesystems.FileObject,java.lang.String)
meth public static org.openide.filesystems.FileObject resolve(org.openide.filesystems.FileObject,java.lang.String)
supr java.lang.Object
hfds LOGGER,UNIT_TESTING,WEB_ROOT

CLSS public org.netbeans.modules.web.common.sourcemap.Mapping
cons public init()
meth public int getNameIndex()
meth public int getOriginalColumn()
meth public int getOriginalLine()
meth public int getSourceIndex()
meth public java.lang.String toString()
supr java.lang.Object
hfds NEW_LINE,column,nameIndex,originalColumn,originalLine,sourceIndex
hcls ColumnComparator

CLSS public org.netbeans.modules.web.common.sourcemap.SourceMap
meth public java.lang.String getFile()
meth public java.lang.String getName(int)
meth public java.lang.String getSourcePath(int)
meth public java.util.List<java.lang.String> getSources()
meth public org.netbeans.modules.web.common.sourcemap.Mapping findInverseMapping(java.lang.String,int)
meth public org.netbeans.modules.web.common.sourcemap.Mapping findInverseMapping(java.lang.String,int,int)
meth public org.netbeans.modules.web.common.sourcemap.Mapping findMapping(int)
meth public org.netbeans.modules.web.common.sourcemap.Mapping findMapping(int,int)
meth public static org.netbeans.modules.web.common.sourcemap.SourceMap parse(java.lang.String)
supr java.lang.Object
hfds INVALIDATING_STRING,SUPPORTED_VERSION,cache,inverseMappings,mappings,sourceMap,sourceRoot

CLSS public final org.netbeans.modules.web.common.sourcemap.SourceMapsScanner
meth public org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator scan(org.netbeans.api.project.Project)
meth public org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator scan(org.openide.filesystems.FileObject[])
meth public static org.netbeans.modules.web.common.sourcemap.SourceMapsScanner getInstance()
supr java.lang.Object
hfds INSTANCE,LOG,SRC_MAP_EXT,projectsMaps
hcls ProjectSourceMapsScanner

CLSS public abstract interface org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator
innr public final static Location
meth public abstract boolean registerTranslation(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract boolean registerTranslation(org.openide.filesystems.FileObject,org.netbeans.modules.web.common.sourcemap.SourceMap)
meth public abstract java.util.List<org.openide.filesystems.FileObject> getSourceFiles(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator$Location getCompiledLocation(org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator$Location)
meth public abstract org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator$Location getSourceLocation(org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator$Location)
meth public abstract org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator$Location getSourceLocation(org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator$Location,java.lang.String)
meth public abstract void unregisterTranslation(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator create()

CLSS public final static org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator$Location
 outer org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator
cons public init(org.openide.filesystems.FileObject,int,int)
meth public int getColumn()
meth public int getLine()
meth public java.lang.String getName()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds column,file,line,mapping,parentFolder,sourceMap

CLSS public abstract interface org.netbeans.modules.web.common.spi.CssPreprocessorImplementation
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addCssPreprocessorListener(org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void process(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void removeCssPreprocessorListener(org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()

CLSS public abstract interface org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener
innr public final static Support
intf java.util.EventListener
meth public abstract void customizerChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.common.spi.CssPreprocessorImplementation)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void optionsChanged(org.netbeans.modules.web.common.spi.CssPreprocessorImplementation)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void processingErrorOccured(org.netbeans.api.project.Project,org.netbeans.modules.web.common.spi.CssPreprocessorImplementation,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener$Support
 outer org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener
cons public init()
meth public boolean hasListeners()
meth public void addCssPreprocessorListener(org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void fireCustomizerChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.common.spi.CssPreprocessorImplementation)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void fireOptionsChanged(org.netbeans.modules.web.common.spi.CssPreprocessorImplementation)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void fireProcessingErrorOccured(org.netbeans.api.project.Project,org.netbeans.modules.web.common.spi.CssPreprocessorImplementation,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void removeCssPreprocessorListener(org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds listeners

CLSS public abstract interface org.netbeans.modules.web.common.spi.DependentFileQueryImplementation
innr public final static !enum Dependency
meth public abstract org.netbeans.modules.web.common.spi.DependentFileQueryImplementation$Dependency isDependent(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)

CLSS public final static !enum org.netbeans.modules.web.common.spi.DependentFileQueryImplementation$Dependency
 outer org.netbeans.modules.web.common.spi.DependentFileQueryImplementation
fld public final static org.netbeans.modules.web.common.spi.DependentFileQueryImplementation$Dependency NO
fld public final static org.netbeans.modules.web.common.spi.DependentFileQueryImplementation$Dependency UNKNOWN
fld public final static org.netbeans.modules.web.common.spi.DependentFileQueryImplementation$Dependency YES
meth public static org.netbeans.modules.web.common.spi.DependentFileQueryImplementation$Dependency valueOf(java.lang.String)
meth public static org.netbeans.modules.web.common.spi.DependentFileQueryImplementation$Dependency[] values()
supr java.lang.Enum<org.netbeans.modules.web.common.spi.DependentFileQueryImplementation$Dependency>

CLSS public abstract interface org.netbeans.modules.web.common.spi.ExternalExecutableUserWarning
meth public abstract void displayError(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.common.spi.ImportantFilesImplementation
innr public final static FileInfo
meth public abstract java.util.Collection<org.netbeans.modules.web.common.spi.ImportantFilesImplementation$FileInfo> getFiles()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public final static org.netbeans.modules.web.common.spi.ImportantFilesImplementation$FileInfo
 outer org.netbeans.modules.web.common.spi.ImportantFilesImplementation
cons public init(org.openide.filesystems.FileObject)
cons public init(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds description,displayName,file

CLSS public final org.netbeans.modules.web.common.spi.ImportantFilesSupport
innr public abstract interface static FileInfoCreator
meth public !varargs static org.netbeans.modules.web.common.spi.ImportantFilesSupport create(org.openide.filesystems.FileObject,java.lang.String[])
meth public java.util.Collection<org.netbeans.modules.web.common.spi.ImportantFilesImplementation$FileInfo> getFiles(org.netbeans.modules.web.common.spi.ImportantFilesSupport$FileInfoCreator)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds LOGGER,changeSupport,directory,fileChangeListener,fileNames
hcls FilesListener

CLSS public abstract interface static org.netbeans.modules.web.common.spi.ImportantFilesSupport$FileInfoCreator
 outer org.netbeans.modules.web.common.spi.ImportantFilesSupport
meth public abstract org.netbeans.modules.web.common.spi.ImportantFilesImplementation$FileInfo create(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.web.common.spi.ProjectWebRootProvider
meth public abstract java.util.Collection<org.openide.filesystems.FileObject> getWebRoots()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getWebRoot(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.web.common.spi.ProjectWebRootQuery
meth public static java.util.Collection<org.openide.filesystems.FileObject> getWebRoots(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.openide.filesystems.FileObject getWebRoot(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.common.spi.RemoteFSDecorator
intf org.openide.filesystems.StatusDecorator
meth public abstract void setDefaultDecorator(org.openide.filesystems.StatusDecorator)

CLSS public abstract interface org.netbeans.modules.web.common.spi.RemoteFileCacheImplementation
meth public abstract java.net.URL isRemoteFile(org.openide.filesystems.FileObject)
meth public abstract org.openide.filesystems.FileObject getRemoteFile(java.net.URL) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.web.common.spi.ServerURLMappingImplementation
meth public abstract java.net.URL toServer(int,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.openide.filesystems.FileObject fromServer(int,java.net.URL)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.web.common.spi.WebPageMetadataProvider
meth public abstract java.util.Map<java.lang.String,?> getMetadataMap(org.openide.util.Lookup)

CLSS public org.netbeans.modules.web.common.taginfo.AttrValueType
cons protected init(java.lang.String)
cons protected init(java.lang.String,java.lang.String[])
fld public static org.netbeans.modules.web.common.taginfo.AttrValueType BOOL
fld public static org.netbeans.modules.web.common.taginfo.AttrValueType NUMBER
fld public static org.netbeans.modules.web.common.taginfo.AttrValueType STRING
meth public java.lang.String getName()
meth public java.lang.String[] getPossibleValues()
meth public static org.netbeans.modules.web.common.taginfo.AttrValueType enumAttrValue(java.lang.String[])
supr java.lang.Object
hfds name,possibleValues

CLSS public org.netbeans.modules.web.common.taginfo.LibraryMetadata
cons public init(java.lang.String,java.util.Collection<org.netbeans.modules.web.common.taginfo.TagMetadata>)
meth public java.lang.String getId()
meth public org.netbeans.modules.web.common.taginfo.TagMetadata getTag(java.lang.String)
meth public static org.netbeans.modules.web.common.taginfo.LibraryMetadata readFromXML(java.io.InputStream) throws java.lang.Exception
supr java.lang.Object
hfds id,tagMap

CLSS public org.netbeans.modules.web.common.taginfo.TagAttrMetadata
cons public init(java.lang.String,boolean,java.util.Collection<org.netbeans.modules.web.common.taginfo.AttrValueType>,java.lang.String)
cons public init(java.lang.String,boolean,org.netbeans.modules.web.common.taginfo.AttrValueType)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.util.Collection<org.netbeans.modules.web.common.taginfo.AttrValueType>,java.lang.String)
cons public init(java.lang.String,org.netbeans.modules.web.common.taginfo.AttrValueType)
meth public boolean isOptional()
meth public java.lang.String getMimeType()
meth public java.lang.String getName()
meth public java.util.Collection<org.netbeans.modules.web.common.taginfo.AttrValueType> getValueTypes()
supr java.lang.Object
hfds mimeType,name,optional,valueTypes

CLSS public org.netbeans.modules.web.common.taginfo.TagMetadata
cons public init(java.lang.String,java.util.Collection<org.netbeans.modules.web.common.taginfo.TagAttrMetadata>)
meth public java.lang.String getName()
meth public java.util.Collection<org.netbeans.modules.web.common.taginfo.TagAttrMetadata> getAttributes()
meth public org.netbeans.modules.web.common.taginfo.TagAttrMetadata getAttribute(java.lang.String)
supr java.lang.Object
hfds attrMap,name

CLSS public abstract interface org.openide.filesystems.StatusDecorator
meth public abstract java.lang.String annotateName(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public abstract java.lang.String annotateNameHtml(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)

