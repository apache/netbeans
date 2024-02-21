#Signature file v4.1
#Version 3.55

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract java.io.Reader
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.lang.Readable
meth public abstract int read(char[],int,int) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[]) throws java.io.IOException
meth public int read(java.nio.CharBuffer) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.lang.Readable
meth public abstract int read(java.nio.CharBuffer) throws java.io.IOException

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

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public org.netbeans.modules.html.editor.lib.api.DefaultHelpItem
cons public init(java.net.URL,org.netbeans.modules.html.editor.lib.api.HelpResolver,java.lang.String)
cons public init(java.net.URL,org.netbeans.modules.html.editor.lib.api.HelpResolver,java.lang.String,java.lang.String)
intf org.netbeans.modules.html.editor.lib.api.HelpItem
meth public java.lang.String getHelpContent()
meth public java.lang.String getHelpHeader()
meth public java.net.URL getHelpURL()
meth public org.netbeans.modules.html.editor.lib.api.HelpResolver getHelpResolver()
supr java.lang.Object
hfds header,helpContent,helpResolver,helpUrl

CLSS public abstract org.netbeans.modules.html.editor.lib.api.DefaultHtmlParseResult
cons public init(org.netbeans.modules.html.editor.lib.api.HtmlSource,org.netbeans.modules.html.editor.lib.api.elements.Node,java.util.Collection<org.netbeans.modules.html.editor.lib.api.ProblemDescription>,org.netbeans.modules.html.editor.lib.api.HtmlVersion)
intf org.netbeans.modules.html.editor.lib.api.HtmlParseResult
meth public org.netbeans.modules.html.editor.lib.api.HtmlVersion version()
supr org.netbeans.modules.html.editor.lib.api.DefaultParseResult
hfds version

CLSS public org.netbeans.modules.html.editor.lib.api.DefaultParseResult
cons public init(org.netbeans.modules.html.editor.lib.api.HtmlSource,org.netbeans.modules.html.editor.lib.api.elements.Node,java.util.Collection<org.netbeans.modules.html.editor.lib.api.ProblemDescription>)
intf org.netbeans.modules.html.editor.lib.api.ParseResult
meth public java.util.Collection<org.netbeans.modules.html.editor.lib.api.ProblemDescription> getProblems()
meth public org.netbeans.modules.html.editor.lib.api.HtmlSource source()
meth public org.netbeans.modules.html.editor.lib.api.elements.Node root()
supr java.lang.Object
hfds problems,root,source

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.HelpItem
meth public abstract java.lang.String getHelpContent()
meth public abstract java.lang.String getHelpHeader()
meth public abstract java.net.URL getHelpURL()
meth public abstract org.netbeans.modules.html.editor.lib.api.HelpResolver getHelpResolver()

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.HelpResolver
meth public abstract java.lang.String getHelpContent(java.net.URL)
meth public abstract java.net.URL resolveLink(java.net.URL,java.lang.String)

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.HtmlParseResult
intf org.netbeans.modules.html.editor.lib.api.ParseResult
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.model.HtmlTag> getPossibleOpenTags(org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public abstract java.util.Map<org.netbeans.modules.html.editor.lib.api.model.HtmlTag,org.netbeans.modules.html.editor.lib.api.elements.OpenTag> getPossibleCloseTags(org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public abstract org.netbeans.modules.html.editor.lib.api.HtmlVersion version()
meth public abstract org.netbeans.modules.html.editor.lib.api.model.HtmlModel model()

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.HtmlParser
meth public abstract boolean canParse(org.netbeans.modules.html.editor.lib.api.HtmlVersion)
meth public abstract java.lang.String getName()
 anno 0 java.lang.Deprecated()
meth public abstract org.netbeans.modules.html.editor.lib.api.HtmlParseResult parse(org.netbeans.modules.html.editor.lib.api.HtmlSource,org.netbeans.modules.html.editor.lib.api.HtmlVersion,org.openide.util.Lookup) throws org.netbeans.modules.html.editor.lib.api.ParseException
meth public abstract org.netbeans.modules.html.editor.lib.api.model.HtmlModel getModel(org.netbeans.modules.html.editor.lib.api.HtmlVersion)
 anno 0 java.lang.Deprecated()

CLSS public final org.netbeans.modules.html.editor.lib.api.HtmlParserFactory
cons public init()
meth public static org.netbeans.modules.html.editor.lib.api.HtmlParser findParser(org.netbeans.modules.html.editor.lib.api.HtmlVersion)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.HtmlParsingResult
meth public abstract java.util.Map<java.lang.String,java.lang.String> getNamespaces()
meth public abstract java.util.Map<java.lang.String,org.netbeans.modules.html.editor.lib.api.elements.Node> roots()
meth public abstract org.netbeans.modules.html.editor.lib.api.HtmlVersion getDetectedHtmlVersion()
meth public abstract org.netbeans.modules.html.editor.lib.api.HtmlVersion getHtmlVersion()
meth public abstract org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult getSyntaxAnalyzerResult()
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node root()
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node root(java.lang.String)
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node rootOfUndeclaredTagsParseTree()

CLSS public final org.netbeans.modules.html.editor.lib.api.HtmlSource
cons public init(java.lang.CharSequence)
cons public init(java.lang.CharSequence,org.netbeans.modules.parsing.api.Snapshot,org.openide.filesystems.FileObject)
cons public init(org.netbeans.modules.parsing.api.Snapshot)
cons public init(org.openide.filesystems.FileObject)
meth public java.lang.CharSequence getSourceCode()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public org.openide.filesystems.FileObject getSourceFileObject()
supr java.lang.Object
hfds snapshot,sourceCode,sourceFileObject

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.HtmlSourceVersionController
meth public abstract org.netbeans.modules.html.editor.lib.api.HtmlVersion getSourceCodeVersion(org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult,org.netbeans.modules.html.editor.lib.api.HtmlVersion)

CLSS public final !enum org.netbeans.modules.html.editor.lib.api.HtmlVersion
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion HTML32
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion HTML40_FRAMESET
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion HTML40_STRICT
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion HTML40_TRANSATIONAL
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion HTML41_FRAMESET
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion HTML41_STRICT
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion HTML41_TRANSATIONAL
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion HTML5
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion XHTML10_FRAMESET
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion XHTML10_STICT
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion XHTML10_TRANSATIONAL
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion XHTML11
fld public final static org.netbeans.modules.html.editor.lib.api.HtmlVersion XHTML5
fld public static org.netbeans.modules.html.editor.lib.api.HtmlVersion DEFAULT_VERSION_UNIT_TESTS_OVERRIDE
meth public boolean isXhtml()
meth public java.lang.String getDefaultNamespace()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDoctypeDeclaration()
meth public java.lang.String getPublicID()
meth public java.lang.String getSystemId()
meth public org.netbeans.modules.html.editor.lib.dtd.DTD getDTD()
meth public static org.netbeans.modules.html.editor.lib.api.HtmlVersion find(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.html.editor.lib.api.HtmlVersion getDefaultVersion()
meth public static org.netbeans.modules.html.editor.lib.api.HtmlVersion getDefaultXhtmlVersion()
meth public static org.netbeans.modules.html.editor.lib.api.HtmlVersion valueOf(java.lang.String)
meth public static org.netbeans.modules.html.editor.lib.api.HtmlVersion[] values()
supr java.lang.Enum<org.netbeans.modules.html.editor.lib.api.HtmlVersion>
hfds DEFAULT_HTML_VERSION,DEFAULT_XHTML_VERSION,DOCTYPE_PREFIX,HTML5_DOCTYPE,XHTML5_DOCTYPE,defaultNamespace,displayName,fallbackPublicID,isXhtml,publicID,systemID

CLSS public org.netbeans.modules.html.editor.lib.api.MaskedAreas
cons public init(int[],int[])
meth public int[] lens()
meth public int[] positions()
supr java.lang.Object
hfds lens,positions

CLSS public org.netbeans.modules.html.editor.lib.api.ParseException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.ParseResult
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.ProblemDescription> getProblems()
meth public abstract org.netbeans.modules.html.editor.lib.api.HtmlSource source()
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node root()

CLSS public final org.netbeans.modules.html.editor.lib.api.ProblemDescription
fld public final static int ERROR = 2
fld public final static int FATAL = 3
fld public final static int INFORMATION = 0
fld public final static int INTERNAL_ERROR = 4
fld public final static int WARNING = 1
meth public boolean equals(java.lang.Object)
meth public int getFrom()
meth public int getTo()
meth public int getType()
meth public int hashCode()
meth public java.lang.String dump(java.lang.String)
meth public java.lang.String getKey()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public static org.netbeans.modules.html.editor.lib.api.ProblemDescription create(java.lang.String,java.lang.String,int,int,int)
supr java.lang.Object
hfds from,key,text,to,type

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.SourceElementHandle
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node resolve(org.netbeans.modules.parsing.spi.Parser$Result)
meth public abstract org.openide.filesystems.FileObject getFileObject()

CLSS public final org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer
 anno 0 java.lang.Deprecated()
innr public final static !enum Behaviour
meth public java.util.Iterator<org.netbeans.modules.html.editor.lib.api.elements.Element> elementsIterator()
meth public org.netbeans.modules.html.editor.lib.api.HtmlSource source()
meth public org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerElements elements()
meth public org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult analyze()
meth public org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult analyze(org.netbeans.modules.html.editor.lib.api.foreign.UndeclaredContentResolver)
meth public static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer create(org.netbeans.modules.html.editor.lib.api.HtmlSource)
supr org.netbeans.modules.html.editor.lib.api.elements.ElementsIterator
hfds source

CLSS public final static !enum org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer$Behaviour
 outer org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer
fld public final static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer$Behaviour DISABLE_ATTRIBUTES_CHECKS
fld public final static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer$Behaviour DISABLE_STRUCTURE_CHECKS
meth public static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer$Behaviour valueOf(java.lang.String)
meth public static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer$Behaviour[] values()
supr java.lang.Enum<org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer$Behaviour>

CLSS public final org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerElements
cons public init(java.util.Collection<org.netbeans.modules.html.editor.lib.api.elements.Element>)
meth public java.util.Collection<org.netbeans.modules.html.editor.lib.api.elements.Element> items()
supr java.lang.Object
hfds elements

CLSS public org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult
fld public final static java.lang.String FILTERED_CODE_NAMESPACE = "filtered_code"
innr public final static !enum FilteredContent
meth public !varargs org.netbeans.modules.html.editor.lib.api.MaskedAreas getMaskedAreas(org.netbeans.modules.html.editor.lib.api.HtmlSource,org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult$FilteredContent[])
meth public boolean mayBeXhtml()
meth public java.lang.String getHtmlTagDefaultNamespace()
meth public java.lang.String getPublicID()
meth public java.util.Collection<org.netbeans.modules.html.editor.lib.api.ParseResult> getAllParseResults() throws org.netbeans.modules.html.editor.lib.api.ParseException
meth public java.util.Iterator<org.netbeans.modules.html.editor.lib.api.elements.Element> getElementsIterator()
meth public java.util.Map<java.lang.String,java.lang.String> getDeclaredNamespaces()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,java.util.Collection<java.lang.String>> getAllDeclaredNamespaces()
meth public java.util.Set<java.lang.String> getAllDeclaredPrefixes()
meth public org.netbeans.modules.html.editor.lib.api.HtmlParseResult parseHtml() throws org.netbeans.modules.html.editor.lib.api.ParseException
meth public org.netbeans.modules.html.editor.lib.api.HtmlSource getSource()
meth public org.netbeans.modules.html.editor.lib.api.HtmlVersion getDetectedHtmlVersion()
meth public org.netbeans.modules.html.editor.lib.api.HtmlVersion getHtmlVersion()
meth public org.netbeans.modules.html.editor.lib.api.ParseResult parseEmbeddedCode(java.lang.String) throws org.netbeans.modules.html.editor.lib.api.ParseException
meth public org.netbeans.modules.html.editor.lib.api.ParseResult parsePlain()
meth public org.netbeans.modules.html.editor.lib.api.ParseResult parseUndeclaredEmbeddedCode() throws org.netbeans.modules.html.editor.lib.api.ParseException
meth public org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerElements getElements()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.html.editor.lib.api.elements.Declaration getDoctypeDeclaration()
meth public org.netbeans.modules.html.editor.lib.api.model.HtmlModel getHtmlModel()
supr java.lang.Object
hfds EMPTY_RESOLVER,LOG,allPrefixes,declaration,detectedHtmlVersion,elementsParserCache,embeddedCodeParseResults,htmlParseResult,maskedAreas,namespaces,resolver,source,undeclaredEmbeddedCodeParseResult
hcls ElementContentFilter,EmptyUndeclaredContentResolver,FilteredIterator,MaskedArea

CLSS public final static !enum org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult$FilteredContent
 outer org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult
fld public final static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult$FilteredContent CUSTOM_TAGS
fld public final static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult$FilteredContent DECLARED_FOREIGN_XHTML_CONTENT
meth public static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult$FilteredContent valueOf(java.lang.String)
meth public static org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult$FilteredContent[] values()
supr java.lang.Enum<org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult$FilteredContent>

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.dtd.ReaderProvider
meth public abstract boolean isXMLContent(java.lang.String)
meth public abstract java.io.Reader getReaderForIdentifier(java.lang.String,java.lang.String)
meth public abstract java.util.Collection<java.lang.String> getIdentifiers()
meth public abstract org.openide.filesystems.FileObject getSystemId(java.lang.String)

CLSS public abstract org.netbeans.modules.html.editor.lib.api.dtd.ReaderProviderFactory
cons public init()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.dtd.ReaderProvider> getProviders()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.Attribute
intf org.netbeans.modules.html.editor.lib.api.elements.Named
meth public abstract boolean isValueQuoted()
meth public abstract int nameOffset()
meth public abstract int valueOffset()
meth public abstract java.lang.CharSequence unquotedValue()
meth public abstract java.lang.CharSequence value()

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter
meth public abstract boolean accepts(org.netbeans.modules.html.editor.lib.api.elements.Attribute)

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.CloseTag
intf org.netbeans.modules.html.editor.lib.api.elements.Named
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.OpenTag matchingOpenTag()

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.Declaration
intf org.netbeans.modules.html.editor.lib.api.elements.Element
meth public abstract java.lang.CharSequence declarationName()
meth public abstract java.lang.CharSequence publicId()
meth public abstract java.lang.CharSequence rootElementName()
meth public abstract java.lang.CharSequence systemId()

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.Element
meth public abstract int from()
meth public abstract int to()
meth public abstract java.lang.CharSequence id()
meth public abstract java.lang.CharSequence image()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.ProblemDescription> problems()
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.ElementType type()
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node parent()

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.ElementFilter
meth public abstract boolean accepts(org.netbeans.modules.html.editor.lib.api.elements.Element)

CLSS public final !enum org.netbeans.modules.html.editor.lib.api.elements.ElementType
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType ATTRIBUTE
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType CLOSE_TAG
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType COMMENT
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType DECLARATION
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType ENTITY_REFERENCE
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType ERROR
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType OPEN_TAG
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType ROOT
fld public final static org.netbeans.modules.html.editor.lib.api.elements.ElementType TEXT
meth public static org.netbeans.modules.html.editor.lib.api.elements.ElementType valueOf(java.lang.String)
meth public static org.netbeans.modules.html.editor.lib.api.elements.ElementType[] values()
supr java.lang.Enum<org.netbeans.modules.html.editor.lib.api.elements.ElementType>

CLSS public org.netbeans.modules.html.editor.lib.api.elements.ElementUtils
meth public static boolean isDescendant(org.netbeans.modules.html.editor.lib.api.elements.Node,org.netbeans.modules.html.editor.lib.api.elements.Node)
meth public static boolean isVirtualNode(org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public static java.lang.String dumpTree(org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public static java.lang.String dumpTree(org.netbeans.modules.html.editor.lib.api.elements.Element,java.lang.CharSequence)
meth public static java.lang.String encodeToString(org.netbeans.modules.html.editor.lib.api.elements.TreePath)
meth public static java.lang.String getNamespace(org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public static java.util.Collection<org.netbeans.modules.html.editor.lib.api.model.HtmlTag> getPossibleOpenTags(org.netbeans.modules.html.editor.lib.api.model.HtmlModel,org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public static java.util.List<org.netbeans.modules.html.editor.lib.api.elements.Element> getChildrenRecursivelly(org.netbeans.modules.html.editor.lib.api.elements.Element,org.netbeans.modules.html.editor.lib.api.elements.ElementFilter,boolean)
meth public static java.util.List<org.netbeans.modules.html.editor.lib.api.elements.Node> getAncestors(org.netbeans.modules.html.editor.lib.api.elements.Node,org.netbeans.modules.html.editor.lib.api.elements.ElementFilter)
meth public static java.util.Map<org.netbeans.modules.html.editor.lib.api.model.HtmlTag,org.netbeans.modules.html.editor.lib.api.elements.OpenTag> getPossibleCloseTags(org.netbeans.modules.html.editor.lib.api.model.HtmlModel,org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public static org.netbeans.modules.html.editor.lib.api.elements.Element findByPhysicalRange(org.netbeans.modules.html.editor.lib.api.elements.Node,int,boolean)
meth public static org.netbeans.modules.html.editor.lib.api.elements.FeaturedNode getRoot(org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public static org.netbeans.modules.html.editor.lib.api.elements.Node findBySemanticRange(org.netbeans.modules.html.editor.lib.api.elements.Node,int,boolean)
meth public static org.netbeans.modules.html.editor.lib.api.elements.OpenTag query(org.netbeans.modules.html.editor.lib.api.elements.Node,java.lang.String)
meth public static org.netbeans.modules.html.editor.lib.api.elements.OpenTag query(org.netbeans.modules.html.editor.lib.api.elements.Node,java.lang.String,boolean)
meth public static void dumpTree(org.netbeans.modules.html.editor.lib.api.elements.Element,java.lang.StringBuffer)
meth public static void dumpTree(org.netbeans.modules.html.editor.lib.api.elements.Element,java.lang.StringBuffer,java.lang.CharSequence)
meth public static void visitAncestors(org.netbeans.modules.html.editor.lib.api.elements.Element,org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor)
meth public static void visitChildren(org.netbeans.modules.html.editor.lib.api.elements.Element,org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor,org.netbeans.modules.html.editor.lib.api.elements.ElementType)
meth public static void visitChildren(org.netbeans.modules.html.editor.lib.api.elements.Node,org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor)
supr java.lang.Object
hfds ELEMENT_PATH_ELEMENTS_DELIMITER,ELEMENT_PATH_INDEX_DELIMITER,INDENT

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor
meth public abstract void visit(org.netbeans.modules.html.editor.lib.api.elements.Element)

CLSS public org.netbeans.modules.html.editor.lib.api.elements.ElementsIterator
cons public init(org.netbeans.modules.html.editor.lib.api.HtmlSource)
cons public init(org.netbeans.modules.parsing.api.Snapshot)
intf java.util.Iterator<org.netbeans.modules.html.editor.lib.api.elements.Element>
meth public boolean hasNext()
meth public org.netbeans.modules.html.editor.lib.api.elements.Element next()
meth public void remove()
supr java.lang.Object
hfds wrapped

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.FeaturedNode
intf org.netbeans.modules.html.editor.lib.api.elements.Node
meth public abstract java.lang.Object getProperty(java.lang.String)

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.Named
intf org.netbeans.modules.html.editor.lib.api.elements.Element
meth public abstract java.lang.CharSequence name()
meth public abstract java.lang.CharSequence namespacePrefix()
meth public abstract java.lang.CharSequence unqualifiedName()

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.Node
intf org.netbeans.modules.html.editor.lib.api.elements.Element
meth public abstract <%0 extends org.netbeans.modules.html.editor.lib.api.elements.Element> java.util.Collection<{%%0}> children(java.lang.Class<{%%0}>)
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.elements.Element> children()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.elements.Element> children(org.netbeans.modules.html.editor.lib.api.elements.ElementFilter)
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.elements.Element> children(org.netbeans.modules.html.editor.lib.api.elements.ElementType)

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.elements.OpenTag
intf org.netbeans.modules.html.editor.lib.api.elements.Named
intf org.netbeans.modules.html.editor.lib.api.elements.Node
meth public abstract boolean isEmpty()
meth public abstract int semanticEnd()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.elements.Attribute> attributes()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.elements.Attribute> attributes(org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter)
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Attribute getAttribute(java.lang.String)
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.CloseTag matchingCloseTag()

CLSS public org.netbeans.modules.html.editor.lib.api.elements.TreePath
cons public init(org.netbeans.modules.html.editor.lib.api.elements.Element)
cons public init(org.netbeans.modules.html.editor.lib.api.elements.Element,org.netbeans.modules.html.editor.lib.api.elements.Element)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.html.editor.lib.api.elements.Element> path()
meth public org.netbeans.modules.html.editor.lib.api.elements.Element first()
meth public org.netbeans.modules.html.editor.lib.api.elements.Element last()
supr java.lang.Object
hfds first,last

CLSS public org.netbeans.modules.html.editor.lib.api.foreign.CharSequenceReader
cons public init(java.lang.CharSequence)
fld protected int length
fld protected int next
fld protected java.lang.CharSequence source
meth protected char processReadChar(char) throws java.io.IOException
meth protected void inputReset()
meth protected void markedAt(int)
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close()
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.io.Reader
hfds mark

CLSS public org.netbeans.modules.html.editor.lib.api.foreign.MaskingChSReader
cons public init(java.lang.CharSequence,int[],int[])
meth public int read() throws java.io.IOException
supr org.netbeans.modules.html.editor.lib.api.foreign.SimpleMaskingChSReader
hfds lens,positionIndex,positions

CLSS public org.netbeans.modules.html.editor.lib.api.foreign.SimpleMaskingChSReader
cons public init(java.lang.CharSequence)
fld protected char MASK_CHAR
meth protected char processReadChar(char) throws java.io.IOException
meth protected void inputReset()
meth protected void markedAt(int)
supr org.netbeans.modules.html.editor.lib.api.foreign.CharSequenceReader
hfds PATTERN_CHAR,markMaskPos,maskPos

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.foreign.UndeclaredContentResolver
meth public abstract boolean isCustomAttribute(org.netbeans.modules.html.editor.lib.api.elements.Attribute,org.netbeans.modules.html.editor.lib.api.HtmlSource)
meth public abstract boolean isCustomTag(org.netbeans.modules.html.editor.lib.api.elements.Named,org.netbeans.modules.html.editor.lib.api.HtmlSource)
meth public abstract java.util.Map<java.lang.String,java.util.List<java.lang.String>> getUndeclaredNamespaces(org.netbeans.modules.html.editor.lib.api.HtmlSource)

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.model.HtmlModel
meth public abstract java.lang.String getModelId()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Collection<? extends org.netbeans.modules.html.editor.lib.api.model.NamedCharRef> getNamedCharacterReferences()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.model.HtmlTag> getAllTags()
meth public abstract org.netbeans.modules.html.editor.lib.api.model.HtmlTag getTag(java.lang.String)

CLSS public final org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory
cons public init()
meth public static org.netbeans.modules.html.editor.lib.api.model.HtmlModel getModel(org.netbeans.modules.html.editor.lib.api.HtmlVersion)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.model.HtmlModelProvider
meth public abstract org.netbeans.modules.html.editor.lib.api.model.HtmlModel getModel(org.netbeans.modules.html.editor.lib.api.HtmlVersion)

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.model.HtmlTag
meth public abstract boolean hasOptionalEndTag()
meth public abstract boolean hasOptionalOpenTag()
meth public abstract boolean isEmpty()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.model.HtmlTag> getChildren()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute> getAttributes()
meth public abstract org.netbeans.modules.html.editor.lib.api.HelpItem getHelp()
meth public abstract org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute getAttribute(java.lang.String)
meth public abstract org.netbeans.modules.html.editor.lib.api.model.HtmlTagType getTagClass()

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute
meth public abstract boolean isRequired()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<java.lang.String> getPossibleValues()
meth public abstract org.netbeans.modules.html.editor.lib.api.HelpItem getHelp()
meth public abstract org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType getType()

CLSS public final !enum org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType BOOLEAN
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType CSS_CLASS
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType CSS_ID
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType GENERIC
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType SET
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType URL
meth public static org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType valueOf(java.lang.String)
meth public static org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType[] values()
supr java.lang.Enum<org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType>

CLSS public final !enum org.netbeans.modules.html.editor.lib.api.model.HtmlTagType
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagType ARIA
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagType HTML
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagType MATHML
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagType SVG
fld public final static org.netbeans.modules.html.editor.lib.api.model.HtmlTagType UNKNOWN
meth public static org.netbeans.modules.html.editor.lib.api.model.HtmlTagType valueOf(java.lang.String)
meth public static org.netbeans.modules.html.editor.lib.api.model.HtmlTagType[] values()
supr java.lang.Enum<org.netbeans.modules.html.editor.lib.api.model.HtmlTagType>

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.model.NamedCharRef
meth public abstract char getValue()
meth public abstract java.lang.String getName()

CLSS public final org.netbeans.modules.html.editor.lib.api.validation.ValidationContext
cons public init(java.io.Reader,org.netbeans.modules.html.editor.lib.api.HtmlVersion,org.openide.filesystems.FileObject,org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult)
meth public boolean isFeatureEnabled(java.lang.String)
meth public java.io.Reader getSourceReader()
meth public org.netbeans.modules.html.editor.lib.api.HtmlVersion getVersion()
meth public org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult getSyntaxAnalyzerResult()
meth public org.openide.filesystems.FileObject getFile()
meth public void enableFeature(java.lang.String,boolean)
supr java.lang.Object
hfds features,file,result,source,version

CLSS public org.netbeans.modules.html.editor.lib.api.validation.ValidationException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public final org.netbeans.modules.html.editor.lib.api.validation.ValidationResult
cons public init(org.netbeans.modules.html.editor.lib.api.validation.Validator,org.netbeans.modules.html.editor.lib.api.validation.ValidationContext,java.util.Collection<org.netbeans.modules.html.editor.lib.api.ProblemDescription>,boolean)
meth public boolean isSuccess()
meth public java.lang.String getValidatorName()
meth public java.util.Collection<org.netbeans.modules.html.editor.lib.api.ProblemDescription> getProblems()
meth public org.netbeans.modules.html.editor.lib.api.validation.ValidationContext getContext()
supr java.lang.Object
hfds context,problems,success,validator

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.validation.Validator
meth public abstract boolean canValidate(org.netbeans.modules.html.editor.lib.api.HtmlVersion)
meth public abstract java.lang.String getValidatorName()
meth public abstract org.netbeans.modules.html.editor.lib.api.validation.ValidationResult validate(org.netbeans.modules.html.editor.lib.api.validation.ValidationContext) throws org.netbeans.modules.html.editor.lib.api.validation.ValidationException

CLSS public org.netbeans.modules.html.editor.lib.api.validation.ValidatorService
cons public init()
meth public static org.netbeans.modules.html.editor.lib.api.validation.Validator getValidator(org.netbeans.modules.html.editor.lib.api.HtmlVersion)
supr java.lang.Object

