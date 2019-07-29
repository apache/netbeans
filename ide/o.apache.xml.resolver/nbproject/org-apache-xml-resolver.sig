#Signature file v4.1
#Version 1.36.0

CLSS public abstract interface java.io.Serializable

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

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

