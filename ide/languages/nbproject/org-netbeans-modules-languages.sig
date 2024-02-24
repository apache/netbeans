#Signature file v4.1
#Version 1.144.0

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract org.netbeans.api.languages.ASTEvaluator
cons public init()
meth public abstract java.lang.String getFeatureName()
meth public abstract void afterEvaluation(org.netbeans.api.languages.ParserManager$State,org.netbeans.api.languages.ASTNode)
meth public abstract void beforeEvaluation(org.netbeans.api.languages.ParserManager$State,org.netbeans.api.languages.ASTNode)
meth public abstract void evaluate(org.netbeans.api.languages.ParserManager$State,java.util.List<org.netbeans.api.languages.ASTItem>,org.netbeans.modules.languages.Feature)
supr java.lang.Object

CLSS public org.netbeans.api.languages.ASTItem
meth public int getEndOffset()
meth public int getLength()
meth public int getOffset()
meth public java.lang.String getMimeType()
meth public java.util.List<org.netbeans.api.languages.ASTItem> getChildren()
meth public org.netbeans.api.languages.ASTPath findPath(int)
meth public org.netbeans.api.languages.Language getLanguage()
meth public void lock()
supr java.lang.Object
hfds children,childrenMap,language,length,offset

CLSS public org.netbeans.api.languages.ASTNode
meth public java.lang.String getAsText()
meth public java.lang.String getNT()
meth public java.lang.String getTokenTypeIdentifier(java.lang.String)
meth public java.lang.String print()
meth public java.lang.String toString()
meth public org.netbeans.api.languages.ASTNode findNode(java.lang.String,int)
meth public org.netbeans.api.languages.ASTNode getNode(java.lang.String)
meth public org.netbeans.api.languages.ASTToken getTokenType(java.lang.String)
meth public static org.netbeans.api.languages.ASTNode create(org.netbeans.api.languages.Language,java.lang.String,int)
meth public static org.netbeans.api.languages.ASTNode create(org.netbeans.api.languages.Language,java.lang.String,java.util.List<org.netbeans.api.languages.ASTItem>,int)
meth public static org.netbeans.api.languages.ASTNode createCompoundASTNode(org.netbeans.api.languages.Language,java.lang.String,java.util.List<org.netbeans.api.languages.ASTItem>,int)
meth public void addChildren(org.netbeans.api.languages.ASTItem)
meth public void removeChildren(org.netbeans.api.languages.ASTItem)
meth public void setChildren(int,org.netbeans.api.languages.ASTItem)
supr org.netbeans.api.languages.ASTItem
hfds nameToChild,nt
hcls CompoundNode

CLSS public abstract org.netbeans.api.languages.ASTPath
meth public abstract int size()
meth public abstract java.util.ListIterator<org.netbeans.api.languages.ASTItem> listIterator()
meth public abstract java.util.ListIterator<org.netbeans.api.languages.ASTItem> listIterator(int)
meth public abstract org.netbeans.api.languages.ASTItem get(int)
meth public abstract org.netbeans.api.languages.ASTItem getLeaf()
meth public abstract org.netbeans.api.languages.ASTItem getRoot()
meth public abstract org.netbeans.api.languages.ASTPath subPath(int)
meth public static org.netbeans.api.languages.ASTPath create(java.util.List<org.netbeans.api.languages.ASTItem>)
meth public static org.netbeans.api.languages.ASTPath create(org.netbeans.api.languages.ASTItem)
supr java.lang.Object
hcls Token2Path,TokenPath

CLSS public final org.netbeans.api.languages.ASTToken
meth public int getTypeID()
meth public java.lang.String getIdentifier()
meth public java.lang.String getTypeName()
meth public java.lang.String toString()
meth public static org.netbeans.api.languages.ASTToken create(org.netbeans.api.languages.Language,int,java.lang.String,int)
meth public static org.netbeans.api.languages.ASTToken create(org.netbeans.api.languages.Language,int,java.lang.String,int,int,java.util.List<? extends org.netbeans.api.languages.ASTItem>)
meth public static org.netbeans.api.languages.ASTToken create(org.netbeans.api.languages.Language,java.lang.String,java.lang.String,int,int,java.util.List<? extends org.netbeans.api.languages.ASTItem>)
supr org.netbeans.api.languages.ASTItem
hfds identifier,toString,type

CLSS public abstract org.netbeans.api.languages.CharInput
cons public init()
meth public abstract boolean eof()
meth public abstract char next()
meth public abstract char read()
meth public abstract int getIndex()
meth public abstract java.lang.String getString(int,int)
meth public abstract void setIndex(int)
supr java.lang.Object

CLSS public org.netbeans.api.languages.CompletionItem
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.api.languages.CompletionItem$Type,int)
innr public final static !enum Type
meth public int getPriority()
meth public java.lang.String getDescription()
meth public java.lang.String getLibrary()
meth public java.lang.String getText()
meth public org.netbeans.api.languages.CompletionItem$Type getType()
meth public static org.netbeans.api.languages.CompletionItem create(java.lang.String)
meth public static org.netbeans.api.languages.CompletionItem create(java.lang.String,java.lang.String,java.lang.String,org.netbeans.api.languages.CompletionItem$Type,int)
supr java.lang.Object
hfds description,library,priority,text,type

CLSS public final static !enum org.netbeans.api.languages.CompletionItem$Type
 outer org.netbeans.api.languages.CompletionItem
fld public final static org.netbeans.api.languages.CompletionItem$Type CLASS
fld public final static org.netbeans.api.languages.CompletionItem$Type CONSTANT
fld public final static org.netbeans.api.languages.CompletionItem$Type CONSTRUCTOR
fld public final static org.netbeans.api.languages.CompletionItem$Type FIELD
fld public final static org.netbeans.api.languages.CompletionItem$Type INTERFACE
fld public final static org.netbeans.api.languages.CompletionItem$Type KEYWORD
fld public final static org.netbeans.api.languages.CompletionItem$Type LOCAL
fld public final static org.netbeans.api.languages.CompletionItem$Type METHOD
fld public final static org.netbeans.api.languages.CompletionItem$Type PARAMETER
meth public static org.netbeans.api.languages.CompletionItem$Type valueOf(java.lang.String)
meth public static org.netbeans.api.languages.CompletionItem$Type[] values()
supr java.lang.Enum<org.netbeans.api.languages.CompletionItem$Type>

CLSS public abstract org.netbeans.api.languages.Context
cons public init()
meth public abstract int getOffset()
meth public abstract javax.swing.text.Document getDocument()
meth public abstract javax.swing.text.JTextComponent getJTextComponent()
meth public static org.netbeans.api.languages.Context create(javax.swing.text.Document,int)
supr java.lang.Object
hcls CookieImpl

CLSS public org.netbeans.api.languages.Highlighting
innr public Highlight
meth protected void fire(int,int)
meth public javax.swing.text.AttributeSet get(int,int)
meth public org.netbeans.api.languages.Highlighting$Highlight highlight(int,int,javax.swing.text.AttributeSet)
meth public static org.netbeans.api.languages.Highlighting getHighlighting(javax.swing.text.Document)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds document,highlightings,items,listeners

CLSS public org.netbeans.api.languages.Highlighting$Highlight
 outer org.netbeans.api.languages.Highlighting
meth public void remove()
supr java.lang.Object
hfds attributeSet,end,start

CLSS public abstract org.netbeans.api.languages.Language
cons public init()
meth public abstract java.lang.String getMimeType()
meth public abstract org.netbeans.api.languages.ASTNode parse(java.io.InputStream) throws java.io.IOException,org.netbeans.api.languages.ParseException
supr java.lang.Object

CLSS public org.netbeans.api.languages.LanguageDefinitionNotFoundException
cons public init(java.lang.String)
supr org.netbeans.api.languages.ParseException

CLSS public abstract org.netbeans.api.languages.LanguagesManager
cons public init()
meth public abstract org.netbeans.api.languages.Language getLanguage(java.lang.String) throws org.netbeans.api.languages.LanguageDefinitionNotFoundException
meth public final static org.netbeans.api.languages.LanguagesManager get()
supr java.lang.Object

CLSS public abstract org.netbeans.api.languages.LibrarySupport
cons public init()
meth public abstract java.lang.String getProperty(java.lang.String,java.lang.String,java.lang.String)
meth public abstract java.util.List<java.lang.String> getItems(java.lang.String)
meth public abstract java.util.List<org.netbeans.api.languages.CompletionItem> getCompletionItems(java.lang.String)
meth public static org.netbeans.api.languages.LibrarySupport create(java.lang.String)
meth public static org.netbeans.api.languages.LibrarySupport create(java.util.List<java.lang.String>)
supr java.lang.Object
hcls DelegatingLibrarySupport,Handler,LibraryImpl

CLSS public org.netbeans.api.languages.ParseException
cons public init()
cons public init(java.lang.Exception)
cons public init(java.lang.Exception,org.netbeans.api.languages.ASTNode)
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.api.languages.ASTNode)
meth public org.netbeans.api.languages.ASTNode getASTNode()
supr java.lang.Exception
hfds node

CLSS public abstract org.netbeans.api.languages.ParserManager
cons public init()
innr public final static !enum State
meth public abstract boolean hasSyntaxErrors()
meth public abstract org.netbeans.api.languages.ASTNode getAST() throws org.netbeans.api.languages.ParseException
meth public abstract org.netbeans.api.languages.ParserManager$State getState()
meth public abstract void addASTEvaluator(org.netbeans.api.languages.ASTEvaluator)
meth public abstract void addListener(org.netbeans.api.languages.ParserManagerListener)
meth public abstract void removeASTEvaluator(org.netbeans.api.languages.ASTEvaluator)
meth public abstract void removeListener(org.netbeans.api.languages.ParserManagerListener)
meth public static org.netbeans.api.languages.ParserManager get(javax.swing.text.Document)
supr java.lang.Object
hfds managers

CLSS public final static !enum org.netbeans.api.languages.ParserManager$State
 outer org.netbeans.api.languages.ParserManager
fld public final static org.netbeans.api.languages.ParserManager$State ERROR
fld public final static org.netbeans.api.languages.ParserManager$State NOT_PARSED
fld public final static org.netbeans.api.languages.ParserManager$State OK
fld public final static org.netbeans.api.languages.ParserManager$State PARSING
meth public static org.netbeans.api.languages.ParserManager$State valueOf(java.lang.String)
meth public static org.netbeans.api.languages.ParserManager$State[] values()
supr java.lang.Enum<org.netbeans.api.languages.ParserManager$State>

CLSS public abstract interface org.netbeans.api.languages.ParserManagerListener
intf java.util.EventListener
meth public abstract void parsed(org.netbeans.api.languages.ParserManager$State,org.netbeans.api.languages.ASTNode)

CLSS public abstract org.netbeans.api.languages.SyntaxContext
cons public init()
meth public abstract org.netbeans.api.languages.ASTPath getASTPath()
meth public static org.netbeans.api.languages.SyntaxContext create(javax.swing.text.Document,org.netbeans.api.languages.ASTPath)
supr org.netbeans.api.languages.Context
hcls CookieImpl

CLSS public abstract org.netbeans.api.languages.TokenInput
cons public init()
meth public abstract boolean eof()
meth public abstract int getIndex()
meth public abstract int getOffset()
meth public abstract org.netbeans.api.languages.ASTToken next(int)
meth public abstract org.netbeans.api.languages.ASTToken read()
meth public abstract void setIndex(int)
supr java.lang.Object

CLSS public org.netbeans.api.languages.database.DatabaseContext
cons public init(org.netbeans.api.languages.database.DatabaseContext,java.lang.String,int,int)
meth protected void setParent(org.netbeans.api.languages.database.DatabaseContext)
meth public <%0 extends org.netbeans.api.languages.database.DatabaseDefinition> java.util.Collection<{%%0}> getDefinitions(java.lang.Class<{%%0}>)
meth public <%0 extends org.netbeans.api.languages.database.DatabaseDefinition> {%%0} getDefinitionInScopeByName(java.lang.Class<{%%0}>,java.lang.String)
meth public <%0 extends org.netbeans.api.languages.database.DatabaseDefinition> {%%0} getEnclosingDefinition(java.lang.Class<{%%0}>)
meth public <%0 extends org.netbeans.api.languages.database.DatabaseDefinition> {%%0} getEnclosingDefinition(java.lang.Class<{%%0}>,int)
meth public <%0 extends org.netbeans.api.languages.database.DatabaseDefinition> {%%0} getFirstDefinition(java.lang.Class<{%%0}>)
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.api.languages.database.DatabaseContext> getContexts()
meth public java.util.List<org.netbeans.api.languages.database.DatabaseDefinition> getAllVisibleDefinitions(int)
meth public java.util.List<org.netbeans.api.languages.database.DatabaseDefinition> getDefinitions()
meth public org.netbeans.api.languages.database.DatabaseContext getClosestContext(int)
meth public org.netbeans.api.languages.database.DatabaseContext getDatabaseContext(int)
meth public org.netbeans.api.languages.database.DatabaseContext getParent()
meth public org.netbeans.api.languages.database.DatabaseDefinition getDefinition(java.lang.String,int)
meth public org.netbeans.api.languages.database.DatabaseDefinition getEnclosingDefinition()
meth public org.netbeans.api.languages.database.DatabaseItem getDatabaseItem(int)
meth public void addContext(org.netbeans.api.languages.ASTItem,org.netbeans.api.languages.database.DatabaseContext)
meth public void addContext(org.netbeans.api.languages.database.DatabaseContext)
meth public void addDefinition(org.netbeans.api.languages.database.DatabaseDefinition)
meth public void addUsage(org.netbeans.api.languages.ASTToken,org.netbeans.api.languages.database.DatabaseDefinition)
meth public void addUsage(org.netbeans.api.languages.database.DatabaseUsage)
meth public void collectDefinitionsInScope(java.util.Collection<org.netbeans.api.languages.database.DatabaseDefinition>)
meth public void setEnclosingDefinition(org.netbeans.api.languages.database.DatabaseDefinition)
supr org.netbeans.api.languages.database.DatabaseItem
hfds contexts,contextsSorted,definitions,definitionsCache,definitionsSorted,enclosingDefinition,parent,type,usages,usagesSorted
hcls ItemsComparator

CLSS public org.netbeans.api.languages.database.DatabaseDefinition
cons public init(java.lang.String,java.lang.String,int,int)
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.net.URL getSourceFileUrl()
meth public java.util.List<org.netbeans.api.languages.database.DatabaseUsage> getUsages()
meth public static org.netbeans.api.languages.database.DatabaseDefinition load(java.io.DataInputStream) throws java.io.IOException
meth public void addUsage(org.netbeans.api.languages.database.DatabaseUsage)
meth public void save(java.io.DataOutputStream) throws java.io.IOException
meth public void setSourceFileUrl(java.net.URL)
supr org.netbeans.api.languages.database.DatabaseItem
hfds name,sourceFileUrl,type,usages

CLSS public org.netbeans.api.languages.database.DatabaseItem
cons public init(int,int)
meth public int getEndOffset()
meth public int getOffset()
supr java.lang.Object
hfds endOffset,offset

CLSS public org.netbeans.api.languages.database.DatabaseManager
cons public init()
meth public static org.netbeans.api.languages.database.DatabaseContext getRoot(org.netbeans.api.languages.ASTNode)
meth public static void setRoot(org.netbeans.api.languages.ASTNode,org.netbeans.api.languages.database.DatabaseContext)
supr java.lang.Object
hfds astNodeToDatabaseContext

CLSS public org.netbeans.api.languages.database.DatabaseUsage
cons public init(java.lang.String,int,int)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.api.languages.database.DatabaseDefinition getDefinition()
meth public void setDatabaseDefinition(org.netbeans.api.languages.database.DatabaseDefinition)
supr org.netbeans.api.languages.database.DatabaseItem
hfds definition,name

