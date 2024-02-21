#Signature file v4.1
#Version 1.78.0

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

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.api.jumpto.type.TypeBrowser
cons public init()
innr public abstract interface static Filter
meth public !varargs static org.netbeans.spi.jumpto.type.TypeDescriptor browse(java.lang.String,java.lang.String,org.netbeans.api.jumpto.type.TypeBrowser$Filter,org.netbeans.spi.jumpto.type.TypeProvider[])
meth public !varargs static org.netbeans.spi.jumpto.type.TypeDescriptor browse(java.lang.String,org.netbeans.api.jumpto.type.TypeBrowser$Filter,org.netbeans.spi.jumpto.type.TypeProvider[])
supr java.lang.Object

CLSS public abstract interface static org.netbeans.api.jumpto.type.TypeBrowser$Filter
 outer org.netbeans.api.jumpto.type.TypeBrowser
meth public abstract boolean accept(org.netbeans.spi.jumpto.type.TypeDescriptor)

CLSS public abstract org.netbeans.spi.jumpto.file.FileDescriptor
cons public init()
meth protected final int getLineNumber()
meth public abstract java.lang.String getFileName()
meth public abstract java.lang.String getOwnerPath()
meth public abstract java.lang.String getProjectName()
meth public abstract javax.swing.Icon getIcon()
meth public abstract javax.swing.Icon getProjectIcon()
meth public abstract org.openide.filesystems.FileObject getFileObject()
meth public abstract void open()
meth public java.lang.String getFileDisplayPath()
supr org.netbeans.spi.jumpto.support.Descriptor
hfds lineNr,preferred

CLSS public abstract interface org.netbeans.spi.jumpto.file.FileProvider
innr public final static Context
innr public final static Result
meth public abstract boolean computeFiles(org.netbeans.spi.jumpto.file.FileProvider$Context,org.netbeans.spi.jumpto.file.FileProvider$Result)
meth public abstract void cancel()

CLSS public final static org.netbeans.spi.jumpto.file.FileProvider$Context
 outer org.netbeans.spi.jumpto.file.FileProvider
meth public int getLineNumber()
meth public java.lang.String getText()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.spi.jumpto.type.SearchType getSearchType()
meth public org.openide.filesystems.FileObject getRoot()
supr java.lang.Object
hfds currentProject,lineNr,project,sourceGroupRoot,text,type

CLSS public final static org.netbeans.spi.jumpto.file.FileProvider$Result
 outer org.netbeans.spi.jumpto.file.FileProvider
meth public void addFile(org.openide.filesystems.FileObject)
meth public void addFileDescriptor(org.netbeans.spi.jumpto.file.FileDescriptor)
meth public void pendingResult()
meth public void setMessage(java.lang.String)
supr java.lang.Object
hfds ctx,message,result,retry

CLSS public abstract interface org.netbeans.spi.jumpto.file.FileProviderFactory
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String name()
meth public abstract org.netbeans.spi.jumpto.file.FileProvider createFileProvider()

CLSS public abstract interface org.netbeans.spi.jumpto.support.AsyncDescriptor<%0 extends java.lang.Object>
meth public abstract boolean hasCorrectCase()
meth public abstract void addDescriptorChangeListener(org.netbeans.spi.jumpto.support.DescriptorChangeListener<{org.netbeans.spi.jumpto.support.AsyncDescriptor%0}>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeDescriptorChangeListener(org.netbeans.spi.jumpto.support.DescriptorChangeListener<{org.netbeans.spi.jumpto.support.AsyncDescriptor%0}>)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.spi.jumpto.support.Descriptor
cons public init()
supr java.lang.Object
hfds attrs
hcls DescriptorAccessorImpl

CLSS public final org.netbeans.spi.jumpto.support.DescriptorChangeEvent<%0 extends java.lang.Object>
cons public init({org.netbeans.spi.jumpto.support.DescriptorChangeEvent%0},java.util.Collection<? extends {org.netbeans.spi.jumpto.support.DescriptorChangeEvent%0}>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Collection<? extends {org.netbeans.spi.jumpto.support.DescriptorChangeEvent%0}> getReplacement()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.util.EventObject
hfds replacement

CLSS public abstract interface org.netbeans.spi.jumpto.support.DescriptorChangeListener<%0 extends java.lang.Object>
intf java.util.EventListener
meth public abstract void descriptorChanged(org.netbeans.spi.jumpto.support.DescriptorChangeEvent<{org.netbeans.spi.jumpto.support.DescriptorChangeListener%0}>)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.jumpto.support.NameMatcher
 anno 0 java.lang.FunctionalInterface()
fld public final static org.netbeans.spi.jumpto.support.NameMatcher ALL
fld public final static org.netbeans.spi.jumpto.support.NameMatcher NONE
meth public abstract boolean accept(java.lang.String)

CLSS public final org.netbeans.spi.jumpto.support.NameMatcherFactory
meth public static java.lang.String wildcardsToRegexp(java.lang.String,boolean)
meth public static org.netbeans.spi.jumpto.support.NameMatcher createNameMatcher(java.lang.String,org.netbeans.spi.jumpto.type.SearchType)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.jumpto.support.NameMatcher createNameMatcher(java.lang.String,org.netbeans.spi.jumpto.type.SearchType,java.util.Map<java.lang.String,java.lang.Object>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds RE_SPECIALS
hcls BaseNameMatcher,CamelCaseNameMatcher,CaseInsensitiveExactNameMatcher,CaseInsensitivePrefixNameMatcher,ExactNameMatcher,PrefixNameMatcher,RegExpNameMatcher

CLSS public abstract org.netbeans.spi.jumpto.symbol.SymbolDescriptor
cons public init()
meth public abstract int getOffset()
meth public abstract java.lang.String getOwnerName()
meth public abstract java.lang.String getProjectName()
meth public abstract java.lang.String getSymbolName()
meth public abstract javax.swing.Icon getIcon()
meth public abstract javax.swing.Icon getProjectIcon()
meth public abstract org.openide.filesystems.FileObject getFileObject()
meth public abstract void open()
meth public java.lang.String getFileDisplayPath()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getSimpleName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr org.netbeans.spi.jumpto.support.Descriptor
hfds matchedSymbolName,provider

CLSS public abstract interface org.netbeans.spi.jumpto.symbol.SymbolProvider
innr public final static Context
innr public final static Result
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String name()
meth public abstract void cancel()
meth public abstract void cleanup()
meth public abstract void computeSymbolNames(org.netbeans.spi.jumpto.symbol.SymbolProvider$Context,org.netbeans.spi.jumpto.symbol.SymbolProvider$Result)

CLSS public final static org.netbeans.spi.jumpto.symbol.SymbolProvider$Context
 outer org.netbeans.spi.jumpto.symbol.SymbolProvider
meth public java.lang.String getText()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.spi.jumpto.type.SearchType getSearchType()
supr java.lang.Object
hfds project,text,type

CLSS public final static org.netbeans.spi.jumpto.symbol.SymbolProvider$Result
 outer org.netbeans.spi.jumpto.symbol.SymbolProvider
meth public void addResult(java.util.List<? extends org.netbeans.spi.jumpto.symbol.SymbolDescriptor>)
meth public void addResult(org.netbeans.spi.jumpto.symbol.SymbolDescriptor)
meth public void pendingResult()
meth public void setHighlightText(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setMessage(java.lang.String)
supr java.lang.Object
hfds dirty,highlightText,highlightTextAlreadySet,message,provider,result,retry

CLSS public final !enum org.netbeans.spi.jumpto.type.SearchType
fld public final static org.netbeans.spi.jumpto.type.SearchType CAMEL_CASE
fld public final static org.netbeans.spi.jumpto.type.SearchType CASE_INSENSITIVE_CAMEL_CASE
fld public final static org.netbeans.spi.jumpto.type.SearchType CASE_INSENSITIVE_EXACT_NAME
fld public final static org.netbeans.spi.jumpto.type.SearchType CASE_INSENSITIVE_PREFIX
fld public final static org.netbeans.spi.jumpto.type.SearchType CASE_INSENSITIVE_REGEXP
fld public final static org.netbeans.spi.jumpto.type.SearchType EXACT_NAME
fld public final static org.netbeans.spi.jumpto.type.SearchType PREFIX
fld public final static org.netbeans.spi.jumpto.type.SearchType REGEXP
meth public static org.netbeans.spi.jumpto.type.SearchType valueOf(java.lang.String)
meth public static org.netbeans.spi.jumpto.type.SearchType[] values()
supr java.lang.Enum<org.netbeans.spi.jumpto.type.SearchType>

CLSS public abstract org.netbeans.spi.jumpto.type.TypeDescriptor
cons public init()
meth public abstract int getOffset()
meth public abstract java.lang.String getContextName()
meth public abstract java.lang.String getOuterName()
meth public abstract java.lang.String getProjectName()
meth public abstract java.lang.String getSimpleName()
meth public abstract java.lang.String getTypeName()
meth public abstract javax.swing.Icon getIcon()
meth public abstract javax.swing.Icon getProjectIcon()
meth public abstract org.openide.filesystems.FileObject getFileObject()
meth public abstract void open()
meth public java.lang.String getFileDisplayPath()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.spi.jumpto.support.Descriptor
hfds highlightText

CLSS public abstract interface org.netbeans.spi.jumpto.type.TypeProvider
innr public final static Context
innr public final static Result
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String name()
meth public abstract void cancel()
meth public abstract void cleanup()
meth public abstract void computeTypeNames(org.netbeans.spi.jumpto.type.TypeProvider$Context,org.netbeans.spi.jumpto.type.TypeProvider$Result)

CLSS public final static org.netbeans.spi.jumpto.type.TypeProvider$Context
 outer org.netbeans.spi.jumpto.type.TypeProvider
meth public java.lang.String getText()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.spi.jumpto.type.SearchType getSearchType()
supr java.lang.Object
hfds project,text,type

CLSS public final static org.netbeans.spi.jumpto.type.TypeProvider$Result
 outer org.netbeans.spi.jumpto.type.TypeProvider
meth public void addResult(java.util.List<? extends org.netbeans.spi.jumpto.type.TypeDescriptor>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addResult(org.netbeans.spi.jumpto.type.TypeDescriptor)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void pendingResult()
meth public void setHighlightText(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setMessage(java.lang.String)
supr java.lang.Object
hfds highlightText,highlightTextAlreadySet,message,modified,result,retry

