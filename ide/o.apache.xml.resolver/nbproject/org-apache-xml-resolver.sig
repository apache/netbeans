#Signature file v4.1
#Version 1.54.0

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface javax.xml.transform.URIResolver
meth public abstract javax.xml.transform.Source resolve(java.lang.String,java.lang.String) throws javax.xml.transform.TransformerException

CLSS public org.apache.xml.resolver.Catalog
cons public init()
cons public init(org.apache.xml.resolver.CatalogManager)
fld protected boolean default_override
fld protected java.net.URL base
fld protected java.net.URL catalogCwd
fld protected java.util.Hashtable readerMap
fld protected java.util.Vector catalogEntries
fld protected java.util.Vector catalogFiles
fld protected java.util.Vector catalogs
fld protected java.util.Vector localCatalogFiles
fld protected java.util.Vector localDelegate
fld protected java.util.Vector readerArr
fld protected org.apache.xml.resolver.CatalogManager catalogManager
fld public final static int BASE
fld public final static int CATALOG
fld public final static int DELEGATE_PUBLIC
fld public final static int DELEGATE_SYSTEM
fld public final static int DELEGATE_URI
fld public final static int DOCTYPE
fld public final static int DOCUMENT
fld public final static int DTDDECL
fld public final static int ENTITY
fld public final static int LINKTYPE
fld public final static int NOTATION
fld public final static int OVERRIDE
fld public final static int PUBLIC
fld public final static int REWRITE_SYSTEM
fld public final static int REWRITE_URI
fld public final static int SGMLDECL
fld public final static int SYSTEM
fld public final static int SYSTEM_SUFFIX
fld public final static int URI
fld public final static int URI_SUFFIX
meth protected java.lang.String encodedByte(int)
meth protected java.lang.String fixSlashes(java.lang.String)
meth protected java.lang.String makeAbsolute(java.lang.String)
meth protected java.lang.String normalizeURI(java.lang.String)
meth protected java.lang.String resolveLocalPublic(int,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth protected java.lang.String resolveLocalSystem(java.lang.String) throws java.io.IOException
meth protected java.lang.String resolveLocalURI(java.lang.String) throws java.io.IOException
meth protected java.lang.String resolveSubordinateCatalogs(int,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.apache.xml.resolver.Catalog newCatalog()
meth protected void addDelegate(org.apache.xml.resolver.CatalogEntry)
meth protected void copyReaders(org.apache.xml.resolver.Catalog)
meth protected void parseCatalogFile(java.lang.String) throws java.io.IOException,org.apache.xml.resolver.CatalogException
meth protected void parsePendingCatalogs() throws java.io.IOException
meth public java.lang.String getCurrentBase()
meth public java.lang.String getDefaultOverride()
meth public java.lang.String resolveDoctype(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String resolveDocument() throws java.io.IOException
meth public java.lang.String resolveEntity(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String resolveNotation(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String resolvePublic(java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String resolveSystem(java.lang.String) throws java.io.IOException
meth public java.lang.String resolveURI(java.lang.String) throws java.io.IOException
meth public java.util.Iterator getPublicIDs()
meth public org.apache.xml.resolver.CatalogManager getCatalogManager()
meth public void addEntry(org.apache.xml.resolver.CatalogEntry)
meth public void addReader(java.lang.String,org.apache.xml.resolver.readers.CatalogReader)
meth public void loadSystemCatalogs() throws java.io.IOException
meth public void parseAllCatalogs() throws java.io.IOException
meth public void parseCatalog(java.lang.String) throws java.io.IOException
meth public void parseCatalog(java.lang.String,java.io.InputStream) throws java.io.IOException,org.apache.xml.resolver.CatalogException
meth public void parseCatalog(java.net.URL) throws java.io.IOException
meth public void setCatalogManager(org.apache.xml.resolver.CatalogManager)
meth public void setupReaders()
meth public void unknownEntry(java.util.Vector)
supr java.lang.Object

CLSS public org.apache.xml.resolver.CatalogEntry
cons public init()
cons public init(int,java.util.Vector) throws org.apache.xml.resolver.CatalogException
cons public init(java.lang.String,java.util.Vector) throws org.apache.xml.resolver.CatalogException
fld protected int entryType
fld protected java.util.Vector args
fld protected static int nextEntry
fld protected static java.util.Hashtable entryTypes
fld protected static java.util.Vector entryArgs
meth public int getEntryType()
meth public java.lang.String getEntryArg(int)
meth public static int addEntryType(java.lang.String,int)
meth public static int getEntryArgCount(int) throws org.apache.xml.resolver.CatalogException
meth public static int getEntryArgCount(java.lang.String) throws org.apache.xml.resolver.CatalogException
meth public static int getEntryType(java.lang.String) throws org.apache.xml.resolver.CatalogException
meth public void setEntryArg(int,java.lang.String)
supr java.lang.Object

CLSS public org.apache.xml.resolver.CatalogException
cons public init(int)
cons public init(int,java.lang.String)
cons public init(java.lang.Exception)
cons public init(java.lang.String,java.lang.Exception)
fld public final static int INVALID_ENTRY = 2
fld public final static int INVALID_ENTRY_TYPE = 3
fld public final static int NO_XML_PARSER = 4
fld public final static int PARSE_FAILED = 7
fld public final static int UNENDED_COMMENT = 8
fld public final static int UNKNOWN_FORMAT = 5
fld public final static int UNPARSEABLE = 6
fld public final static int WRAPPER = 1
meth public int getExceptionType()
meth public java.lang.Exception getException()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
supr java.lang.Exception
hfds exception,exceptionType

CLSS public org.apache.xml.resolver.CatalogManager
cons public init()
cons public init(java.lang.String)
fld public org.apache.xml.resolver.helpers.Debug debug
meth public boolean allowOasisXMLCatalogPI()
meth public boolean getAllowOasisXMLCatalogPI()
meth public boolean getIgnoreMissingProperties()
meth public boolean getPreferPublic()
meth public boolean getRelativeCatalogs()
meth public boolean getUseStaticCatalog()
meth public boolean preferPublic()
meth public boolean queryAllowOasisXMLCatalogPI()
meth public boolean relativeCatalogs()
meth public boolean staticCatalog()
meth public int getVerbosity()
meth public int verbosity()
meth public java.lang.String catalogClassName()
meth public java.lang.String getCatalogClassName()
meth public java.lang.String queryCatalogClassName()
meth public java.util.Vector catalogFiles()
meth public java.util.Vector getCatalogFiles()
meth public org.apache.xml.resolver.Catalog getCatalog()
meth public org.apache.xml.resolver.Catalog getPrivateCatalog()
meth public org.apache.xml.resolver.helpers.BootstrapResolver getBootstrapResolver()
meth public static org.apache.xml.resolver.CatalogManager getStaticManager()
meth public void ignoreMissingProperties(boolean)
meth public void setAllowOasisXMLCatalogPI(boolean)
meth public void setBootstrapResolver(org.apache.xml.resolver.helpers.BootstrapResolver)
meth public void setCatalogClassName(java.lang.String)
meth public void setCatalogFiles(java.lang.String)
meth public void setIgnoreMissingProperties(boolean)
meth public void setPreferPublic(boolean)
meth public void setRelativeCatalogs(boolean)
meth public void setUseStaticCatalog(boolean)
meth public void setVerbosity(int)
supr java.lang.Object
hfds bResolver,catalogClassName,catalogFiles,defaultCatalogFiles,defaultOasisXMLCatalogPI,defaultPreferPublic,defaultRelativeCatalogs,defaultUseStaticCatalog,defaultVerbosity,fromPropertiesFile,ignoreMissingProperties,oasisXMLCatalogPI,pAllowPI,pClassname,pFiles,pIgnoreMissing,pPrefer,pStatic,pVerbosity,preferPublic,propertyFile,propertyFileURI,relativeCatalogs,resources,staticCatalog,staticManager,useStaticCatalog,verbosity

CLSS public org.apache.xml.resolver.NbCatalogManager
cons public init()
cons public init(java.lang.String)
fld public org.apache.xml.resolver.helpers.Debug debug
meth public boolean allowOasisXMLCatalogPI()
meth public boolean getAllowOasisXMLCatalogPI()
meth public boolean getIgnoreMissingProperties()
meth public boolean getPreferPublic()
meth public boolean getRelativeCatalogs()
meth public boolean getUseStaticCatalog()
meth public boolean preferPublic()
meth public boolean queryAllowOasisXMLCatalogPI()
meth public boolean relativeCatalogs()
meth public boolean staticCatalog()
meth public int getVerbosity()
meth public int verbosity()
meth public java.lang.String catalogClassName()
meth public java.lang.String getCatalogClassName()
meth public java.lang.String queryCatalogClassName()
meth public java.util.Vector catalogFiles()
meth public java.util.Vector getCatalogFiles()
meth public org.apache.xml.resolver.Catalog getCatalog()
meth public org.apache.xml.resolver.Catalog getPrivateCatalog()
meth public org.apache.xml.resolver.helpers.BootstrapResolver getBootstrapResolver()
meth public static org.apache.xml.resolver.CatalogManager getStaticManager()
meth public void ignoreMissingProperties(boolean)
meth public void setAllowOasisXMLCatalogPI(boolean)
meth public void setBootstrapResolver(org.apache.xml.resolver.helpers.BootstrapResolver)
meth public void setCatalogClassName(java.lang.String)
meth public void setCatalogFiles(java.lang.String)
meth public void setIgnoreMissingProperties(boolean)
meth public void setPreferPublic(boolean)
meth public void setRelativeCatalogs(boolean)
meth public void setUseStaticCatalog(boolean)
meth public void setVerbosity(int)
supr org.apache.xml.resolver.CatalogManager
hfds bResolver,catalogClassName,catalogFiles,defaultCatalogFiles,defaultOasisXMLCatalogPI,defaultPreferPublic,defaultRelativeCatalogs,defaultUseStaticCatalog,defaultVerbosity,fromPropertiesFile,ignoreMissingProperties,oasisXMLCatalogPI,pAllowPI,pClassname,pFiles,pIgnoreMissing,pPrefer,pStatic,pVerbosity,preferPublic,propertyFile,propertyFileURI,relativeCatalogs,resources,staticCatalog,staticManager,useStaticCatalog,verbosity

CLSS public org.apache.xml.resolver.Resolver
cons public init()
fld public final static int RESOLVER
fld public final static int SYSTEMREVERSE
fld public final static int SYSTEMSUFFIX
fld public final static int URISUFFIX
meth protected java.lang.String resolveExternalPublic(java.lang.String,java.lang.String) throws java.io.IOException
meth protected java.lang.String resolveExternalSystem(java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.apache.xml.resolver.Resolver queryResolver(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String resolvePublic(java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String resolveSystem(java.lang.String) throws java.io.IOException
meth public java.lang.String resolveSystemReverse(java.lang.String) throws java.io.IOException
meth public java.lang.String resolveURI(java.lang.String) throws java.io.IOException
meth public java.util.Vector resolveAllSystem(java.lang.String) throws java.io.IOException
meth public java.util.Vector resolveAllSystemReverse(java.lang.String) throws java.io.IOException
meth public void addEntry(org.apache.xml.resolver.CatalogEntry)
meth public void setupReaders()
supr org.apache.xml.resolver.Catalog

CLSS public org.apache.xml.resolver.Version
cons public init()
meth public static java.lang.String getProduct()
meth public static java.lang.String getVersion()
meth public static java.lang.String getVersionNum()
meth public static void main(java.lang.String[])
supr java.lang.Object

CLSS public org.apache.xml.resolver.apps.XParseError
cons public init(boolean,boolean)
intf org.xml.sax.ErrorHandler
meth public int getErrorCount()
meth public int getFatalCount()
meth public int getMaxMessages()
meth public int getWarningCount()
meth public void error(org.xml.sax.SAXParseException)
meth public void fatalError(org.xml.sax.SAXParseException)
meth public void setMaxMessages(int)
meth public void warning(org.xml.sax.SAXParseException)
supr java.lang.Object
hfds baseURI,errorCount,fatalCount,maxMessages,showErrors,showWarnings,warningCount

CLSS public org.apache.xml.resolver.apps.resolver
cons public init()
meth public static void main(java.lang.String[]) throws java.io.IOException
meth public static void usage()
supr java.lang.Object
hfds debug

CLSS public org.apache.xml.resolver.apps.xparse
cons public init()
meth public static void main(java.lang.String[]) throws java.io.IOException
supr java.lang.Object
hfds debug

CLSS public org.apache.xml.resolver.apps.xread
cons public init()
meth public static void main(java.lang.String[]) throws java.io.IOException
supr java.lang.Object
hfds debug

CLSS public org.apache.xml.resolver.helpers.BootstrapResolver
cons public init()
fld public final static java.lang.String xCatalogPubId = "-//DTD XCatalog//EN"
fld public final static java.lang.String xmlCatalogPubId = "-//OASIS//DTD XML Catalogs V1.0//EN"
fld public final static java.lang.String xmlCatalogRNG = "http://www.oasis-open.org/committees/entity/release/1.0/catalog.rng"
fld public final static java.lang.String xmlCatalogSysId = "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd"
fld public final static java.lang.String xmlCatalogXSD = "http://www.oasis-open.org/committees/entity/release/1.0/catalog.xsd"
intf javax.xml.transform.URIResolver
intf org.xml.sax.EntityResolver
meth public javax.xml.transform.Source resolve(java.lang.String,java.lang.String) throws javax.xml.transform.TransformerException
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String)
supr java.lang.Object
hfds publicMap,systemMap,uriMap

CLSS public org.apache.xml.resolver.helpers.Debug
cons public init()
fld protected int debug
meth public int getDebug()
meth public void message(int,java.lang.String)
meth public void message(int,java.lang.String,java.lang.String)
meth public void message(int,java.lang.String,java.lang.String,java.lang.String)
meth public void setDebug(int)
supr java.lang.Object

CLSS public abstract org.apache.xml.resolver.helpers.FileURL
cons protected init()
meth public static java.net.URL makeURL(java.lang.String) throws java.net.MalformedURLException
supr java.lang.Object

CLSS public org.apache.xml.resolver.helpers.Namespaces
cons public init()
meth public static java.lang.String getLocalName(org.w3c.dom.Element)
meth public static java.lang.String getNamespaceURI(org.w3c.dom.Element)
meth public static java.lang.String getNamespaceURI(org.w3c.dom.Node,java.lang.String)
meth public static java.lang.String getPrefix(org.w3c.dom.Element)
supr java.lang.Object

CLSS public abstract org.apache.xml.resolver.helpers.PublicId
cons protected init()
meth public static java.lang.String decodeURN(java.lang.String)
meth public static java.lang.String encodeURN(java.lang.String)
meth public static java.lang.String normalize(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.apache.xml.resolver.readers.CatalogReader
meth public abstract void readCatalog(org.apache.xml.resolver.Catalog,java.io.InputStream) throws java.io.IOException,org.apache.xml.resolver.CatalogException
meth public abstract void readCatalog(org.apache.xml.resolver.Catalog,java.lang.String) throws java.io.IOException,org.apache.xml.resolver.CatalogException

CLSS public abstract interface org.apache.xml.resolver.readers.DOMCatalogParser
meth public abstract void parseCatalogEntry(org.apache.xml.resolver.Catalog,org.w3c.dom.Node)

CLSS public org.apache.xml.resolver.readers.DOMCatalogReader
cons public init()
fld protected java.util.Hashtable namespaceMap
intf org.apache.xml.resolver.readers.CatalogReader
meth public java.lang.String getCatalogParser(java.lang.String,java.lang.String)
meth public void readCatalog(org.apache.xml.resolver.Catalog,java.io.InputStream) throws java.io.IOException,org.apache.xml.resolver.CatalogException
meth public void readCatalog(org.apache.xml.resolver.Catalog,java.lang.String) throws java.io.IOException,org.apache.xml.resolver.CatalogException
meth public void setCatalogParser(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public org.apache.xml.resolver.readers.ExtendedXMLCatalogReader
cons public init()
fld public final static java.lang.String extendedNamespaceName = "http://nwalsh.com/xcatalog/1.0"
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.apache.xml.resolver.readers.OASISXMLCatalogReader

CLSS public org.apache.xml.resolver.readers.OASISXMLCatalogReader
cons public init()
cons public init(javax.xml.parsers.SAXParserFactory,org.apache.xml.resolver.Catalog)
fld protected java.util.Stack baseURIStack
fld protected java.util.Stack namespaceStack
fld protected java.util.Stack overrideStack
fld protected org.apache.xml.resolver.Catalog catalog
fld public final static java.lang.String namespaceName = "urn:oasis:names:tc:entity:xmlns:xml:catalog"
fld public final static java.lang.String tr9401NamespaceName = "urn:oasis:names:tc:entity:xmlns:tr9401:catalog"
intf org.apache.xml.resolver.readers.SAXCatalogParser
meth protected boolean inExtensionNamespace()
meth public boolean checkAttributes(org.xml.sax.Attributes,java.lang.String)
meth public boolean checkAttributes(org.xml.sax.Attributes,java.lang.String,java.lang.String)
meth public org.apache.xml.resolver.Catalog getCatalog()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setCatalog(org.apache.xml.resolver.Catalog)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.apache.xml.resolver.readers.SAXCatalogReader

CLSS public abstract interface org.apache.xml.resolver.readers.SAXCatalogParser
intf org.xml.sax.ContentHandler
intf org.xml.sax.DocumentHandler
meth public abstract void setCatalog(org.apache.xml.resolver.Catalog)

CLSS public org.apache.xml.resolver.readers.SAXCatalogReader
cons public init()
cons public init(java.lang.String)
cons public init(javax.xml.parsers.SAXParserFactory)
fld protected java.lang.String parserClass
fld protected java.util.Hashtable namespaceMap
fld protected javax.xml.parsers.SAXParserFactory parserFactory
fld protected org.apache.xml.resolver.helpers.Debug debug
intf org.apache.xml.resolver.readers.CatalogReader
intf org.xml.sax.ContentHandler
intf org.xml.sax.DocumentHandler
meth public java.lang.String getCatalogParser(java.lang.String,java.lang.String)
meth public java.lang.String getParserClass()
meth public javax.xml.parsers.SAXParserFactory getParserFactory()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String) throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void readCatalog(org.apache.xml.resolver.Catalog,java.io.InputStream) throws java.io.IOException,org.apache.xml.resolver.CatalogException
meth public void readCatalog(org.apache.xml.resolver.Catalog,java.lang.String) throws java.io.IOException,org.apache.xml.resolver.CatalogException
meth public void setCatalogParser(java.lang.String,java.lang.String,java.lang.String)
meth public void setClassLoader(java.lang.ClassLoader)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void setParserClass(java.lang.String)
meth public void setParserFactory(javax.xml.parsers.SAXParserFactory)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,org.xml.sax.AttributeList) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr java.lang.Object
hfds abandonHope,catalog,loader,saxParser

CLSS public org.apache.xml.resolver.readers.SAXParserHandler
cons public init()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.DefaultHandler
hfds ch,er

CLSS public org.apache.xml.resolver.readers.TR9401CatalogReader
cons public init()
meth public void readCatalog(org.apache.xml.resolver.Catalog,java.io.InputStream) throws java.io.IOException
supr org.apache.xml.resolver.readers.TextCatalogReader

CLSS public org.apache.xml.resolver.readers.TextCatalogReader
cons public init()
fld protected boolean caseSensitive
fld protected int top
fld protected int[] stack
fld protected java.io.InputStream catfile
fld protected java.util.Stack tokenStack
intf org.apache.xml.resolver.readers.CatalogReader
meth protected int nextChar() throws java.io.IOException
meth protected java.lang.String nextToken() throws java.io.IOException,org.apache.xml.resolver.CatalogException
meth protected void finalize()
meth public boolean getCaseSensitive()
meth public void readCatalog(org.apache.xml.resolver.Catalog,java.io.InputStream) throws java.io.IOException
meth public void readCatalog(org.apache.xml.resolver.Catalog,java.lang.String) throws java.io.IOException
meth public void setCaseSensitive(boolean)
supr java.lang.Object

CLSS public org.apache.xml.resolver.readers.XCatalogReader
cons public init()
cons public init(javax.xml.parsers.SAXParserFactory,org.apache.xml.resolver.Catalog)
fld protected org.apache.xml.resolver.Catalog catalog
intf org.apache.xml.resolver.readers.SAXCatalogParser
meth public org.apache.xml.resolver.Catalog getCatalog()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setCatalog(org.apache.xml.resolver.Catalog)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.apache.xml.resolver.readers.SAXCatalogReader

CLSS public org.apache.xml.resolver.tools.CatalogResolver
cons public init()
cons public init(boolean)
cons public init(org.apache.xml.resolver.CatalogManager)
fld public boolean namespaceAware
fld public boolean validating
intf javax.xml.transform.URIResolver
intf org.xml.sax.EntityResolver
meth public java.lang.String getResolvedEntity(java.lang.String,java.lang.String)
meth public javax.xml.transform.Source resolve(java.lang.String,java.lang.String) throws javax.xml.transform.TransformerException
meth public org.apache.xml.resolver.Catalog getCatalog()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String)
supr java.lang.Object
hfds catalog,catalogManager

CLSS public org.apache.xml.resolver.tools.NbCatalogResolver
cons public init()
cons public init(boolean)
cons public init(org.apache.xml.resolver.NbCatalogManager)
fld public boolean namespaceAware
fld public boolean validating
intf javax.xml.transform.URIResolver
intf org.xml.sax.EntityResolver
meth public java.lang.String getResolvedEntity(java.lang.String,java.lang.String)
meth public javax.xml.transform.Source resolve(java.lang.String,java.lang.String) throws javax.xml.transform.TransformerException
meth public org.apache.xml.resolver.Catalog getCatalog()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String)
supr java.lang.Object
hfds catalog,catalogManager

CLSS public org.apache.xml.resolver.tools.ResolvingParser
cons public init()
cons public init(org.apache.xml.resolver.CatalogManager)
fld public static boolean namespaceAware
fld public static boolean suppressExplanation
fld public static boolean validating
intf org.xml.sax.DTDHandler
intf org.xml.sax.DocumentHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.Parser
meth public org.apache.xml.resolver.Catalog getCatalog()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String)
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDTDHandler(org.xml.sax.DTDHandler)
meth public void setDocumentHandler(org.xml.sax.DocumentHandler)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setLocale(java.util.Locale) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,org.xml.sax.AttributeList) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr java.lang.Object
hfds allowXMLCatalogPI,baseURL,catalogManager,catalogResolver,documentHandler,dtdHandler,oasisXMLCatalogPI,parser,piCatalogResolver,saxParser

CLSS public org.apache.xml.resolver.tools.ResolvingXMLFilter
cons public init()
cons public init(org.apache.xml.resolver.CatalogManager)
cons public init(org.xml.sax.XMLReader)
cons public init(org.xml.sax.XMLReader,org.apache.xml.resolver.CatalogManager)
fld public static boolean suppressExplanation
meth public org.apache.xml.resolver.Catalog getCatalog()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String)
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds allowXMLCatalogPI,baseURL,catalogManager,catalogResolver,oasisXMLCatalogPI,piCatalogResolver

CLSS public org.apache.xml.resolver.tools.ResolvingXMLReader
cons public init()
cons public init(org.apache.xml.resolver.CatalogManager)
fld public static boolean namespaceAware
fld public static boolean validating
supr org.apache.xml.resolver.tools.ResolvingXMLFilter

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DocumentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,org.xml.sax.AttributeList) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.Parser
meth public abstract void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void setDTDHandler(org.xml.sax.DTDHandler)
meth public abstract void setDocumentHandler(org.xml.sax.DocumentHandler)
meth public abstract void setEntityResolver(org.xml.sax.EntityResolver)
meth public abstract void setErrorHandler(org.xml.sax.ErrorHandler)
meth public abstract void setLocale(java.util.Locale) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.XMLFilter
intf org.xml.sax.XMLReader
meth public abstract org.xml.sax.XMLReader getParent()
meth public abstract void setParent(org.xml.sax.XMLReader)

CLSS public abstract interface org.xml.sax.XMLReader
meth public abstract boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract org.xml.sax.ContentHandler getContentHandler()
meth public abstract org.xml.sax.DTDHandler getDTDHandler()
meth public abstract org.xml.sax.EntityResolver getEntityResolver()
meth public abstract org.xml.sax.ErrorHandler getErrorHandler()
meth public abstract void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void setContentHandler(org.xml.sax.ContentHandler)
meth public abstract void setDTDHandler(org.xml.sax.DTDHandler)
meth public abstract void setEntityResolver(org.xml.sax.EntityResolver)
meth public abstract void setErrorHandler(org.xml.sax.ErrorHandler)
meth public abstract void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException

CLSS public org.xml.sax.helpers.DefaultHandler
cons public init()
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

CLSS public org.xml.sax.helpers.XMLFilterImpl
cons public init()
cons public init(org.xml.sax.XMLReader)
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
intf org.xml.sax.XMLFilter
meth public boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public org.xml.sax.ContentHandler getContentHandler()
meth public org.xml.sax.DTDHandler getDTDHandler()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public org.xml.sax.XMLReader getParent()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void setDTDHandler(org.xml.sax.DTDHandler)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void setParent(org.xml.sax.XMLReader)
meth public void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

