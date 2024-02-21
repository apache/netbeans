#Signature file v4.1
#Version 1.57

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract interface java.lang.reflect.InvocationHandler
meth public abstract java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public abstract java.util.prefs.AbstractPreferences
cons protected init(java.util.prefs.AbstractPreferences,java.lang.String)
fld protected boolean newNode
fld protected final java.lang.Object lock
meth protected abstract java.lang.String getSpi(java.lang.String)
meth protected abstract java.lang.String[] childrenNamesSpi() throws java.util.prefs.BackingStoreException
meth protected abstract java.lang.String[] keysSpi() throws java.util.prefs.BackingStoreException
meth protected abstract java.util.prefs.AbstractPreferences childSpi(java.lang.String)
meth protected abstract void flushSpi() throws java.util.prefs.BackingStoreException
meth protected abstract void putSpi(java.lang.String,java.lang.String)
meth protected abstract void removeNodeSpi() throws java.util.prefs.BackingStoreException
meth protected abstract void removeSpi(java.lang.String)
meth protected abstract void syncSpi() throws java.util.prefs.BackingStoreException
meth protected boolean isRemoved()
meth protected final java.util.prefs.AbstractPreferences[] cachedChildren()
meth protected java.util.prefs.AbstractPreferences getChild(java.lang.String) throws java.util.prefs.BackingStoreException
meth public boolean getBoolean(java.lang.String,boolean)
meth public boolean isUserNode()
meth public boolean nodeExists(java.lang.String) throws java.util.prefs.BackingStoreException
meth public byte[] getByteArray(java.lang.String,byte[])
meth public double getDouble(java.lang.String,double)
meth public float getFloat(java.lang.String,float)
meth public int getInt(java.lang.String,int)
meth public java.lang.String absolutePath()
meth public java.lang.String get(java.lang.String,java.lang.String)
meth public java.lang.String name()
meth public java.lang.String toString()
meth public java.lang.String[] childrenNames() throws java.util.prefs.BackingStoreException
meth public java.lang.String[] keys() throws java.util.prefs.BackingStoreException
meth public java.util.prefs.Preferences node(java.lang.String)
meth public java.util.prefs.Preferences parent()
meth public long getLong(java.lang.String,long)
meth public void addNodeChangeListener(java.util.prefs.NodeChangeListener)
meth public void addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public void clear() throws java.util.prefs.BackingStoreException
meth public void exportNode(java.io.OutputStream) throws java.io.IOException,java.util.prefs.BackingStoreException
meth public void exportSubtree(java.io.OutputStream) throws java.io.IOException,java.util.prefs.BackingStoreException
meth public void flush() throws java.util.prefs.BackingStoreException
meth public void put(java.lang.String,java.lang.String)
meth public void putBoolean(java.lang.String,boolean)
meth public void putByteArray(java.lang.String,byte[])
meth public void putDouble(java.lang.String,double)
meth public void putFloat(java.lang.String,float)
meth public void putInt(java.lang.String,int)
meth public void putLong(java.lang.String,long)
meth public void remove(java.lang.String)
meth public void removeNode() throws java.util.prefs.BackingStoreException
meth public void removeNodeChangeListener(java.util.prefs.NodeChangeListener)
meth public void removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public void sync() throws java.util.prefs.BackingStoreException
supr java.util.prefs.Preferences

CLSS public abstract java.util.prefs.Preferences
cons protected init()
fld public final static int MAX_KEY_LENGTH = 80
fld public final static int MAX_NAME_LENGTH = 80
fld public final static int MAX_VALUE_LENGTH = 8192
meth public abstract boolean getBoolean(java.lang.String,boolean)
meth public abstract boolean isUserNode()
meth public abstract boolean nodeExists(java.lang.String) throws java.util.prefs.BackingStoreException
meth public abstract byte[] getByteArray(java.lang.String,byte[])
meth public abstract double getDouble(java.lang.String,double)
meth public abstract float getFloat(java.lang.String,float)
meth public abstract int getInt(java.lang.String,int)
meth public abstract java.lang.String absolutePath()
meth public abstract java.lang.String get(java.lang.String,java.lang.String)
meth public abstract java.lang.String name()
meth public abstract java.lang.String toString()
meth public abstract java.lang.String[] childrenNames() throws java.util.prefs.BackingStoreException
meth public abstract java.lang.String[] keys() throws java.util.prefs.BackingStoreException
meth public abstract java.util.prefs.Preferences node(java.lang.String)
meth public abstract java.util.prefs.Preferences parent()
meth public abstract long getLong(java.lang.String,long)
meth public abstract void addNodeChangeListener(java.util.prefs.NodeChangeListener)
meth public abstract void addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public abstract void clear() throws java.util.prefs.BackingStoreException
meth public abstract void exportNode(java.io.OutputStream) throws java.io.IOException,java.util.prefs.BackingStoreException
meth public abstract void exportSubtree(java.io.OutputStream) throws java.io.IOException,java.util.prefs.BackingStoreException
meth public abstract void flush() throws java.util.prefs.BackingStoreException
meth public abstract void put(java.lang.String,java.lang.String)
meth public abstract void putBoolean(java.lang.String,boolean)
meth public abstract void putByteArray(java.lang.String,byte[])
meth public abstract void putDouble(java.lang.String,double)
meth public abstract void putFloat(java.lang.String,float)
meth public abstract void putInt(java.lang.String,int)
meth public abstract void putLong(java.lang.String,long)
meth public abstract void remove(java.lang.String)
meth public abstract void removeNode() throws java.util.prefs.BackingStoreException
meth public abstract void removeNodeChangeListener(java.util.prefs.NodeChangeListener)
meth public abstract void removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public abstract void sync() throws java.util.prefs.BackingStoreException
meth public static java.util.prefs.Preferences systemNodeForPackage(java.lang.Class<?>)
meth public static java.util.prefs.Preferences systemRoot()
meth public static java.util.prefs.Preferences userNodeForPackage(java.lang.Class<?>)
meth public static java.util.prefs.Preferences userRoot()
meth public static void importPreferences(java.io.InputStream) throws java.io.IOException,java.util.prefs.InvalidPreferencesFormatException
supr java.lang.Object

CLSS public abstract interface java.util.prefs.PreferencesFactory
meth public abstract java.util.prefs.Preferences systemRoot()
meth public abstract java.util.prefs.Preferences userRoot()

CLSS public org.ini4j.BasicMultiMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(java.util.Map<{org.ini4j.BasicMultiMap%0},java.util.List<{org.ini4j.BasicMultiMap%1}>>)
intf java.io.Serializable
intf org.ini4j.MultiMap<{org.ini4j.BasicMultiMap%0},{org.ini4j.BasicMultiMap%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public int length(java.lang.Object)
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{org.ini4j.BasicMultiMap%1}> values()
meth public java.util.List<{org.ini4j.BasicMultiMap%1}> getAll(java.lang.Object)
meth public java.util.List<{org.ini4j.BasicMultiMap%1}> putAll({org.ini4j.BasicMultiMap%0},java.util.List<{org.ini4j.BasicMultiMap%1}>)
meth public java.util.Set<java.util.Map$Entry<{org.ini4j.BasicMultiMap%0},{org.ini4j.BasicMultiMap%1}>> entrySet()
meth public java.util.Set<{org.ini4j.BasicMultiMap%0}> keySet()
meth public void add({org.ini4j.BasicMultiMap%0},{org.ini4j.BasicMultiMap%1})
meth public void add({org.ini4j.BasicMultiMap%0},{org.ini4j.BasicMultiMap%1},int)
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.ini4j.BasicMultiMap%0},? extends {org.ini4j.BasicMultiMap%1}>)
meth public {org.ini4j.BasicMultiMap%1} get(java.lang.Object)
meth public {org.ini4j.BasicMultiMap%1} get(java.lang.Object,int)
meth public {org.ini4j.BasicMultiMap%1} put({org.ini4j.BasicMultiMap%0},{org.ini4j.BasicMultiMap%1})
meth public {org.ini4j.BasicMultiMap%1} put({org.ini4j.BasicMultiMap%0},{org.ini4j.BasicMultiMap%1},int)
meth public {org.ini4j.BasicMultiMap%1} remove(java.lang.Object)
meth public {org.ini4j.BasicMultiMap%1} remove(java.lang.Object,int)
supr java.lang.Object
hfds _impl,serialVersionUID
hcls ShadowEntry

CLSS public org.ini4j.BasicOptionMap
cons public init()
cons public init(boolean)
intf org.ini4j.OptionMap
meth public <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>,java.lang.String)
meth public <%0 extends java.lang.Object> {%%0} fetch(java.lang.Object,int,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} fetch(java.lang.Object,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} fetchAll(java.lang.Object,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} get(java.lang.Object,int,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} get(java.lang.Object,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getAll(java.lang.Object,java.lang.Class<{%%0}>)
meth public java.lang.String fetch(java.lang.Object)
meth public java.lang.String fetch(java.lang.Object,int)
meth public java.lang.String put(java.lang.String,java.lang.Object)
meth public java.lang.String put(java.lang.String,java.lang.Object,int)
meth public void add(java.lang.String,java.lang.Object)
meth public void add(java.lang.String,java.lang.Object,int)
meth public void from(java.lang.Object)
meth public void from(java.lang.Object,java.lang.String)
meth public void putAll(java.lang.String,java.lang.Object)
meth public void to(java.lang.Object)
meth public void to(java.lang.Object,java.lang.String)
supr org.ini4j.CommonMultiMap<java.lang.String,java.lang.String>
hfds ENVIRONMENT_PREFIX,ENVIRONMENT_PREFIX_LEN,EXPRESSION,G_INDEX,G_OPTION,SUBST_CHAR,SYSTEM_PROPERTY_PREFIX,SYSTEM_PROPERTY_PREFIX_LEN,_defaultBeanAccess,_propertyFirstUpper,serialVersionUID
hcls Access

CLSS public org.ini4j.BasicProfile
cons public init()
cons public init(boolean,boolean)
intf org.ini4j.Profile
meth public <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>,java.lang.String)
meth public <%0 extends java.lang.Object> {%%0} fetch(java.lang.Object,java.lang.Object,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} get(java.lang.Object,java.lang.Object,java.lang.Class<{%%0}>)
meth public java.lang.String fetch(java.lang.Object,java.lang.Object)
meth public java.lang.String get(java.lang.Object,java.lang.Object)
meth public java.lang.String getComment()
meth public java.lang.String put(java.lang.String,java.lang.String,java.lang.Object)
meth public java.lang.String remove(java.lang.Object,java.lang.Object)
meth public org.ini4j.Profile$Section add(java.lang.String)
meth public org.ini4j.Profile$Section remove(org.ini4j.Profile$Section)
meth public void add(java.lang.String,java.lang.String,java.lang.Object)
meth public void setComment(java.lang.String)
supr org.ini4j.CommonMultiMap<java.lang.String,org.ini4j.Profile$Section>
hfds EXPRESSION,G_OPTION,G_OPTION_IDX,G_SECTION,G_SECTION_IDX,SECTION_ENVIRONMENT,SECTION_SYSTEM_PROPERTIES,_comment,_propertyFirstUpper,_treeMode,serialVersionUID
hcls BeanInvocationHandler

CLSS public org.ini4j.BasicRegistry
cons public init()
intf org.ini4j.Registry
meth public java.lang.String getVersion()
meth public org.ini4j.Registry$Key add(java.lang.String)
meth public org.ini4j.Registry$Key get(java.lang.Object)
meth public org.ini4j.Registry$Key get(java.lang.Object,int)
meth public org.ini4j.Registry$Key put(java.lang.String,org.ini4j.Profile$Section)
meth public org.ini4j.Registry$Key put(java.lang.String,org.ini4j.Profile$Section,int)
meth public org.ini4j.Registry$Key remove(java.lang.Object)
meth public org.ini4j.Registry$Key remove(java.lang.Object,int)
meth public org.ini4j.Registry$Key remove(org.ini4j.Profile$Section)
meth public void setVersion(java.lang.String)
supr org.ini4j.BasicProfile
hfds _version,serialVersionUID

CLSS public abstract interface org.ini4j.CommentedMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.util.Map<{org.ini4j.CommentedMap%0},{org.ini4j.CommentedMap%1}>
meth public abstract java.lang.String getComment(java.lang.Object)
meth public abstract java.lang.String putComment({org.ini4j.CommentedMap%0},java.lang.String)
meth public abstract java.lang.String removeComment(java.lang.Object)

CLSS public org.ini4j.CommonMultiMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
intf org.ini4j.CommentedMap<{org.ini4j.CommonMultiMap%0},{org.ini4j.CommonMultiMap%1}>
meth public java.lang.String getComment(java.lang.Object)
meth public java.lang.String putComment({org.ini4j.CommonMultiMap%0},java.lang.String)
meth public java.lang.String removeComment(java.lang.Object)
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.ini4j.CommonMultiMap%0},? extends {org.ini4j.CommonMultiMap%1}>)
meth public {org.ini4j.CommonMultiMap%1} remove(java.lang.Object)
meth public {org.ini4j.CommonMultiMap%1} remove(java.lang.Object,int)
supr org.ini4j.BasicMultiMap<{org.ini4j.CommonMultiMap%0},{org.ini4j.CommonMultiMap%1}>
hfds FIRST_CATEGORY,LAST_CATEGORY,META_COMMENT,SEPARATOR,_meta,serialVersionUID

CLSS public org.ini4j.Config
cons public init()
fld public final static boolean DEFAULT_EMPTY_OPTION = false
fld public final static boolean DEFAULT_EMPTY_SECTION = false
fld public final static boolean DEFAULT_ESCAPE = true
fld public final static boolean DEFAULT_GLOBAL_SECTION = false
fld public final static boolean DEFAULT_INCLUDE = false
fld public final static boolean DEFAULT_LOWER_CASE_OPTION = false
fld public final static boolean DEFAULT_LOWER_CASE_SECTION = false
fld public final static boolean DEFAULT_MULTI_OPTION = true
fld public final static boolean DEFAULT_MULTI_SECTION = false
fld public final static boolean DEFAULT_PROPERTY_FIRST_UPPER = false
fld public final static boolean DEFAULT_STRICT_OPERATOR = false
fld public final static boolean DEFAULT_TREE = true
fld public final static boolean DEFAULT_UNNAMED_SECTION = false
fld public final static char DEFAULT_PATH_SEPARATOR = '/'
fld public final static java.lang.String DEFAULT_GLOBAL_SECTION_NAME = "?"
fld public final static java.lang.String DEFAULT_LINE_SEPARATOR
fld public final static java.lang.String KEY_PREFIX = "org.ini4j.config."
fld public final static java.lang.String PROP_EMPTY_OPTION = "emptyOption"
fld public final static java.lang.String PROP_EMPTY_SECTION = "emptySection"
fld public final static java.lang.String PROP_ESCAPE = "escape"
fld public final static java.lang.String PROP_FILE_ENCODING = "fileEncoding"
fld public final static java.lang.String PROP_GLOBAL_SECTION = "globalSection"
fld public final static java.lang.String PROP_GLOBAL_SECTION_NAME = "globalSectionName"
fld public final static java.lang.String PROP_INCLUDE = "include"
fld public final static java.lang.String PROP_LINE_SEPARATOR = "lineSeparator"
fld public final static java.lang.String PROP_LOWER_CASE_OPTION = "lowerCaseOption"
fld public final static java.lang.String PROP_LOWER_CASE_SECTION = "lowerCaseSection"
fld public final static java.lang.String PROP_MULTI_OPTION = "multiOption"
fld public final static java.lang.String PROP_MULTI_SECTION = "multiSection"
fld public final static java.lang.String PROP_PATH_SEPARATOR = "pathSeparator"
fld public final static java.lang.String PROP_PROPERTY_FIRST_UPPER = "propertyFirstUpper"
fld public final static java.lang.String PROP_STRICT_OPERATOR = "strictOperator"
fld public final static java.lang.String PROP_TREE = "tree"
fld public final static java.lang.String PROP_UNNAMED_SECTION = "unnamedSection"
fld public final static java.nio.charset.Charset DEFAULT_FILE_ENCODING
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isEmptyOption()
meth public boolean isEmptySection()
meth public boolean isEscape()
meth public boolean isGlobalSection()
meth public boolean isInclude()
meth public boolean isLowerCaseOption()
meth public boolean isLowerCaseSection()
meth public boolean isMultiOption()
meth public boolean isMultiSection()
meth public boolean isPropertyFirstUpper()
meth public boolean isStrictOperator()
meth public boolean isTree()
meth public boolean isUnnamedSection()
meth public char getPathSeparator()
meth public final void reset()
meth public java.lang.String getGlobalSectionName()
meth public java.lang.String getLineSeparator()
meth public java.nio.charset.Charset getFileEncoding()
meth public org.ini4j.Config clone()
meth public static java.lang.String getEnvironment(java.lang.String)
meth public static java.lang.String getEnvironment(java.lang.String,java.lang.String)
meth public static java.lang.String getSystemProperty(java.lang.String)
meth public static java.lang.String getSystemProperty(java.lang.String,java.lang.String)
meth public static org.ini4j.Config getGlobal()
meth public void setEmptyOption(boolean)
meth public void setEmptySection(boolean)
meth public void setEscape(boolean)
meth public void setFileEncoding(java.nio.charset.Charset)
meth public void setGlobalSection(boolean)
meth public void setGlobalSectionName(java.lang.String)
meth public void setInclude(boolean)
meth public void setLineSeparator(java.lang.String)
meth public void setLowerCaseOption(boolean)
meth public void setLowerCaseSection(boolean)
meth public void setMultiOption(boolean)
meth public void setMultiSection(boolean)
meth public void setPathSeparator(char)
meth public void setPropertyFirstUpper(boolean)
meth public void setStrictOperator(boolean)
meth public void setTree(boolean)
meth public void setUnnamedSection(boolean)
supr java.lang.Object
hfds GLOBAL,_emptyOption,_emptySection,_escape,_fileEncoding,_globalSection,_globalSectionName,_include,_lineSeparator,_lowerCaseOption,_lowerCaseSection,_multiOption,_multiSection,_pathSeparator,_propertyFirstUpper,_strictOperator,_tree,_unnamedSection,serialVersionUID

CLSS public org.ini4j.ConfigParser
cons public init()
cons public init(java.util.Map<java.lang.String,java.lang.String>)
innr public final static DuplicateSectionException
innr public final static InterpolationMissingOptionException
innr public final static NoOptionException
innr public final static NoSectionException
innr public final static ParsingException
innr public static ConfigParserException
innr public static InterpolationException
intf java.io.Serializable
meth protected org.ini4j.Ini getIni()
meth public !varargs void read(java.lang.String[]) throws java.io.IOException
meth public boolean getBoolean(java.lang.String,java.lang.String) throws org.ini4j.ConfigParser$InterpolationException,org.ini4j.ConfigParser$NoOptionException,org.ini4j.ConfigParser$NoSectionException
meth public boolean hasOption(java.lang.String,java.lang.String)
meth public boolean hasSection(java.lang.String)
meth public boolean removeOption(java.lang.String,java.lang.String) throws org.ini4j.ConfigParser$NoSectionException
meth public boolean removeSection(java.lang.String)
meth public double getDouble(java.lang.String,java.lang.String) throws org.ini4j.ConfigParser$InterpolationException,org.ini4j.ConfigParser$NoOptionException,org.ini4j.ConfigParser$NoSectionException
meth public float getFloat(java.lang.String,java.lang.String) throws org.ini4j.ConfigParser$InterpolationException,org.ini4j.ConfigParser$NoOptionException,org.ini4j.ConfigParser$NoSectionException
meth public int getInt(java.lang.String,java.lang.String) throws org.ini4j.ConfigParser$InterpolationException,org.ini4j.ConfigParser$NoOptionException,org.ini4j.ConfigParser$NoSectionException
meth public java.lang.String get(java.lang.String,java.lang.String) throws org.ini4j.ConfigParser$InterpolationException,org.ini4j.ConfigParser$NoOptionException,org.ini4j.ConfigParser$NoSectionException
meth public java.lang.String get(java.lang.String,java.lang.String,boolean) throws org.ini4j.ConfigParser$InterpolationException,org.ini4j.ConfigParser$NoOptionException,org.ini4j.ConfigParser$NoSectionException
meth public java.lang.String get(java.lang.String,java.lang.String,boolean,java.util.Map<java.lang.String,java.lang.String>) throws org.ini4j.ConfigParser$InterpolationException,org.ini4j.ConfigParser$NoOptionException,org.ini4j.ConfigParser$NoSectionException
meth public java.util.List<java.lang.String> options(java.lang.String) throws org.ini4j.ConfigParser$NoSectionException
meth public java.util.List<java.lang.String> sections()
meth public java.util.List<java.util.Map$Entry<java.lang.String,java.lang.String>> items(java.lang.String) throws org.ini4j.ConfigParser$InterpolationMissingOptionException,org.ini4j.ConfigParser$NoSectionException
meth public java.util.List<java.util.Map$Entry<java.lang.String,java.lang.String>> items(java.lang.String,boolean) throws org.ini4j.ConfigParser$InterpolationMissingOptionException,org.ini4j.ConfigParser$NoSectionException
meth public java.util.List<java.util.Map$Entry<java.lang.String,java.lang.String>> items(java.lang.String,boolean,java.util.Map<java.lang.String,java.lang.String>) throws org.ini4j.ConfigParser$InterpolationMissingOptionException,org.ini4j.ConfigParser$NoSectionException
meth public java.util.Map<java.lang.String,java.lang.String> defaults()
meth public long getLong(java.lang.String,java.lang.String) throws org.ini4j.ConfigParser$InterpolationException,org.ini4j.ConfigParser$NoOptionException,org.ini4j.ConfigParser$NoSectionException
meth public void addSection(java.lang.String) throws org.ini4j.ConfigParser$DuplicateSectionException
meth public void read(java.io.File) throws java.io.IOException
meth public void read(java.io.InputStream) throws java.io.IOException
meth public void read(java.io.Reader) throws java.io.IOException
meth public void read(java.net.URL) throws java.io.IOException
meth public void set(java.lang.String,java.lang.String,java.lang.Object) throws org.ini4j.ConfigParser$NoSectionException
meth public void write(java.io.File) throws java.io.IOException
meth public void write(java.io.OutputStream) throws java.io.IOException
meth public void write(java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds _ini,serialVersionUID
hcls PyIni

CLSS public static org.ini4j.ConfigParser$ConfigParserException
 outer org.ini4j.ConfigParser
cons public init(java.lang.String)
supr java.lang.Exception
hfds serialVersionUID

CLSS public final static org.ini4j.ConfigParser$DuplicateSectionException
 outer org.ini4j.ConfigParser
supr org.ini4j.ConfigParser$ConfigParserException
hfds serialVersionUID

CLSS public static org.ini4j.ConfigParser$InterpolationException
 outer org.ini4j.ConfigParser
cons protected init(java.lang.String)
supr org.ini4j.ConfigParser$ConfigParserException
hfds serialVersionUID

CLSS public final static org.ini4j.ConfigParser$InterpolationMissingOptionException
 outer org.ini4j.ConfigParser
supr org.ini4j.ConfigParser$InterpolationException
hfds serialVersionUID

CLSS public final static org.ini4j.ConfigParser$NoOptionException
 outer org.ini4j.ConfigParser
supr org.ini4j.ConfigParser$ConfigParserException
hfds serialVersionUID

CLSS public final static org.ini4j.ConfigParser$NoSectionException
 outer org.ini4j.ConfigParser
supr org.ini4j.ConfigParser$ConfigParserException
hfds serialVersionUID

CLSS public final static org.ini4j.ConfigParser$ParsingException
 outer org.ini4j.ConfigParser
supr java.io.IOException
hfds serialVersionUID

CLSS public abstract interface org.ini4j.Configurable
meth public abstract org.ini4j.Config getConfig()
meth public abstract void setConfig(org.ini4j.Config)

CLSS public org.ini4j.Ini
cons public init()
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.Reader) throws java.io.IOException
cons public init(java.net.URL) throws java.io.IOException
intf org.ini4j.Configurable
intf org.ini4j.Persistable
meth protected org.ini4j.spi.IniHandler newBuilder()
meth protected void store(org.ini4j.spi.IniHandler,org.ini4j.Profile$Section)
meth protected void store(org.ini4j.spi.IniHandler,org.ini4j.Profile$Section,java.lang.String,int)
meth public java.io.File getFile()
meth public org.ini4j.Config getConfig()
meth public void load() throws java.io.IOException
meth public void load(java.io.File) throws java.io.IOException
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void load(java.io.Reader) throws java.io.IOException
meth public void load(java.net.URL) throws java.io.IOException
meth public void setConfig(org.ini4j.Config)
meth public void setFile(java.io.File)
meth public void store() throws java.io.IOException
meth public void store(java.io.File) throws java.io.IOException
meth public void store(java.io.OutputStream) throws java.io.IOException
meth public void store(java.io.Writer) throws java.io.IOException
supr org.ini4j.BasicProfile
hfds _config,_file,serialVersionUID

CLSS public org.ini4j.IniPreferences
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.Reader) throws java.io.IOException
cons public init(java.net.URL) throws java.io.IOException
cons public init(org.ini4j.Ini)
innr protected SectionPreferences
meth protected java.lang.String getSpi(java.lang.String)
meth protected java.lang.String[] childrenNamesSpi() throws java.util.prefs.BackingStoreException
meth protected java.lang.String[] keysSpi() throws java.util.prefs.BackingStoreException
meth protected org.ini4j.Ini getIni()
meth protected org.ini4j.IniPreferences$SectionPreferences childSpi(java.lang.String)
meth protected void flushSpi() throws java.util.prefs.BackingStoreException
meth protected void putSpi(java.lang.String,java.lang.String)
meth protected void removeNodeSpi() throws java.util.prefs.BackingStoreException
meth protected void removeSpi(java.lang.String)
meth protected void syncSpi() throws java.util.prefs.BackingStoreException
supr java.util.prefs.AbstractPreferences
hfds EMPTY,_ini

CLSS protected org.ini4j.IniPreferences$SectionPreferences
 outer org.ini4j.IniPreferences
meth protected java.lang.String getSpi(java.lang.String)
meth protected java.lang.String[] childrenNamesSpi() throws java.util.prefs.BackingStoreException
meth protected java.lang.String[] keysSpi() throws java.util.prefs.BackingStoreException
meth protected org.ini4j.IniPreferences$SectionPreferences childSpi(java.lang.String)
meth protected void flushSpi() throws java.util.prefs.BackingStoreException
meth protected void putSpi(java.lang.String,java.lang.String)
meth protected void removeNodeSpi() throws java.util.prefs.BackingStoreException
meth protected void removeSpi(java.lang.String)
meth protected void syncSpi() throws java.util.prefs.BackingStoreException
meth public void flush() throws java.util.prefs.BackingStoreException
meth public void sync() throws java.util.prefs.BackingStoreException
supr java.util.prefs.AbstractPreferences
hfds _section

CLSS public org.ini4j.IniPreferencesFactory
cons public init()
fld public final static java.lang.String KEY_SYSTEM = "org.ini4j.prefs.system"
fld public final static java.lang.String KEY_USER = "org.ini4j.prefs.user"
fld public final static java.lang.String PROPERTIES = "ini4j.properties"
intf java.util.prefs.PreferencesFactory
meth public java.util.prefs.Preferences systemRoot()
meth public java.util.prefs.Preferences userRoot()
supr java.lang.Object
hfds _system,_user

CLSS public org.ini4j.InvalidFileFormatException
cons public init(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public abstract interface org.ini4j.MultiMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.util.Map<{org.ini4j.MultiMap%0},{org.ini4j.MultiMap%1}>
meth public abstract int length(java.lang.Object)
meth public abstract java.util.List<{org.ini4j.MultiMap%1}> getAll(java.lang.Object)
meth public abstract java.util.List<{org.ini4j.MultiMap%1}> putAll({org.ini4j.MultiMap%0},java.util.List<{org.ini4j.MultiMap%1}>)
meth public abstract void add({org.ini4j.MultiMap%0},{org.ini4j.MultiMap%1})
meth public abstract void add({org.ini4j.MultiMap%0},{org.ini4j.MultiMap%1},int)
meth public abstract {org.ini4j.MultiMap%1} get(java.lang.Object,int)
meth public abstract {org.ini4j.MultiMap%1} put({org.ini4j.MultiMap%0},{org.ini4j.MultiMap%1},int)
meth public abstract {org.ini4j.MultiMap%1} remove(java.lang.Object,int)

CLSS public abstract interface org.ini4j.OptionMap
intf org.ini4j.CommentedMap<java.lang.String,java.lang.String>
intf org.ini4j.MultiMap<java.lang.String,java.lang.String>
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>,java.lang.String)
meth public abstract <%0 extends java.lang.Object> {%%0} fetch(java.lang.Object,int,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} fetch(java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} fetchAll(java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} get(java.lang.Object,int,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} get(java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getAll(java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract java.lang.String fetch(java.lang.Object)
meth public abstract java.lang.String fetch(java.lang.Object,int)
meth public abstract java.lang.String put(java.lang.String,java.lang.Object)
meth public abstract java.lang.String put(java.lang.String,java.lang.Object,int)
meth public abstract void add(java.lang.String,java.lang.Object)
meth public abstract void add(java.lang.String,java.lang.Object,int)
meth public abstract void from(java.lang.Object)
meth public abstract void from(java.lang.Object,java.lang.String)
meth public abstract void putAll(java.lang.String,java.lang.Object)
meth public abstract void to(java.lang.Object)
meth public abstract void to(java.lang.Object,java.lang.String)

CLSS public org.ini4j.Options
cons public init()
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.Reader) throws java.io.IOException
cons public init(java.net.URL) throws java.io.IOException
intf org.ini4j.Configurable
intf org.ini4j.Persistable
meth protected org.ini4j.spi.OptionsHandler newBuilder()
meth protected void store(org.ini4j.spi.OptionsHandler) throws java.io.IOException
meth public java.io.File getFile()
meth public java.lang.String getComment()
meth public org.ini4j.Config getConfig()
meth public void load() throws java.io.IOException
meth public void load(java.io.File) throws java.io.IOException
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void load(java.io.Reader) throws java.io.IOException
meth public void load(java.net.URL) throws java.io.IOException
meth public void setComment(java.lang.String)
meth public void setConfig(org.ini4j.Config)
meth public void setFile(java.io.File)
meth public void store() throws java.io.IOException
meth public void store(java.io.File) throws java.io.IOException
meth public void store(java.io.OutputStream) throws java.io.IOException
meth public void store(java.io.Writer) throws java.io.IOException
supr org.ini4j.BasicOptionMap
hfds _comment,_config,_file,serialVersionUID

CLSS public abstract interface org.ini4j.Persistable
meth public abstract java.io.File getFile()
meth public abstract void load() throws java.io.IOException
meth public abstract void load(java.io.File) throws java.io.IOException
meth public abstract void load(java.io.InputStream) throws java.io.IOException
meth public abstract void load(java.io.Reader) throws java.io.IOException
meth public abstract void load(java.net.URL) throws java.io.IOException
meth public abstract void setFile(java.io.File)
meth public abstract void store() throws java.io.IOException
meth public abstract void store(java.io.File) throws java.io.IOException
meth public abstract void store(java.io.OutputStream) throws java.io.IOException
meth public abstract void store(java.io.Writer) throws java.io.IOException

CLSS public abstract interface org.ini4j.Profile
fld public final static char PATH_SEPARATOR = '/'
innr public abstract interface static Section
intf org.ini4j.CommentedMap<java.lang.String,org.ini4j.Profile$Section>
intf org.ini4j.MultiMap<java.lang.String,org.ini4j.Profile$Section>
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>,java.lang.String)
meth public abstract <%0 extends java.lang.Object> {%%0} fetch(java.lang.Object,java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} get(java.lang.Object,java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract java.lang.String fetch(java.lang.Object,java.lang.Object)
meth public abstract java.lang.String get(java.lang.Object,java.lang.Object)
meth public abstract java.lang.String getComment()
meth public abstract java.lang.String put(java.lang.String,java.lang.String,java.lang.Object)
meth public abstract java.lang.String remove(java.lang.Object,java.lang.Object)
meth public abstract org.ini4j.Profile$Section add(java.lang.String)
meth public abstract org.ini4j.Profile$Section remove(org.ini4j.Profile$Section)
meth public abstract void add(java.lang.String,java.lang.String,java.lang.Object)
meth public abstract void setComment(java.lang.String)

CLSS public abstract interface static org.ini4j.Profile$Section
 outer org.ini4j.Profile
intf org.ini4j.OptionMap
meth public abstract !varargs org.ini4j.Profile$Section lookup(java.lang.String[])
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getSimpleName()
meth public abstract java.lang.String[] childrenNames()
meth public abstract org.ini4j.Profile$Section addChild(java.lang.String)
meth public abstract org.ini4j.Profile$Section getChild(java.lang.String)
meth public abstract org.ini4j.Profile$Section getParent()
meth public abstract void removeChild(java.lang.String)

CLSS public org.ini4j.Reg
cons public init()
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.Reader) throws java.io.IOException
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.net.URL) throws java.io.IOException
fld protected final static java.lang.String DEFAULT_SUFFIX = ".reg"
fld protected final static java.lang.String TMP_PREFIX = "reg-"
intf org.ini4j.Configurable
intf org.ini4j.Persistable
intf org.ini4j.Registry
meth protected org.ini4j.spi.IniHandler newBuilder()
meth public java.io.File getFile()
meth public org.ini4j.Config getConfig()
meth public static boolean isWindows()
meth public void load() throws java.io.IOException
meth public void load(java.io.File) throws java.io.IOException
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void load(java.io.Reader) throws java.io.IOException
meth public void load(java.net.URL) throws java.io.IOException
meth public void read(java.lang.String) throws java.io.IOException
meth public void setConfig(org.ini4j.Config)
meth public void setFile(java.io.File)
meth public void store() throws java.io.IOException
meth public void store(java.io.File) throws java.io.IOException
meth public void store(java.io.OutputStream) throws java.io.IOException
meth public void store(java.io.Writer) throws java.io.IOException
meth public void write() throws java.io.IOException
supr org.ini4j.BasicRegistry
hfds CR,LF,PROP_OS_NAME,STDERR_BUFF_SIZE,WINDOWS,_config,_file,serialVersionUID

CLSS public abstract interface org.ini4j.Registry
fld public final static char ESCAPE_CHAR = '\u005c'
fld public final static char KEY_SEPARATOR = '\u005c'
fld public final static char TYPE_SEPARATOR = ':'
fld public final static java.lang.String LINE_SEPARATOR = "\r\n"
fld public final static java.lang.String VERSION = "Windows Registry Editor Version 5.00"
fld public final static java.nio.charset.Charset FILE_ENCODING
innr public abstract interface static Key
innr public final static !enum Hive
innr public final static !enum Type
intf org.ini4j.Profile
meth public abstract java.lang.String getVersion()
meth public abstract org.ini4j.Registry$Key get(java.lang.Object)
meth public abstract org.ini4j.Registry$Key get(java.lang.Object,int)
meth public abstract org.ini4j.Registry$Key put(java.lang.String,org.ini4j.Profile$Section)
meth public abstract org.ini4j.Registry$Key put(java.lang.String,org.ini4j.Profile$Section,int)
meth public abstract org.ini4j.Registry$Key remove(java.lang.Object)
meth public abstract org.ini4j.Registry$Key remove(java.lang.Object,int)
meth public abstract void setVersion(java.lang.String)

CLSS public final static !enum org.ini4j.Registry$Hive
 outer org.ini4j.Registry
fld public final static org.ini4j.Registry$Hive HKEY_CLASSES_ROOT
fld public final static org.ini4j.Registry$Hive HKEY_CURRENT_CONFIG
fld public final static org.ini4j.Registry$Hive HKEY_CURRENT_USER
fld public final static org.ini4j.Registry$Hive HKEY_LOCAL_MACHINE
fld public final static org.ini4j.Registry$Hive HKEY_USERS
meth public static org.ini4j.Registry$Hive valueOf(java.lang.String)
meth public static org.ini4j.Registry$Hive[] values()
supr java.lang.Enum<org.ini4j.Registry$Hive>

CLSS public abstract interface static org.ini4j.Registry$Key
 outer org.ini4j.Registry
fld public final static java.lang.String DEFAULT_NAME = "@"
intf org.ini4j.Profile$Section
meth public abstract !varargs org.ini4j.Registry$Key lookup(java.lang.String[])
meth public abstract org.ini4j.Registry$Key addChild(java.lang.String)
meth public abstract org.ini4j.Registry$Key getChild(java.lang.String)
meth public abstract org.ini4j.Registry$Key getParent()
meth public abstract org.ini4j.Registry$Type getType(java.lang.Object)
meth public abstract org.ini4j.Registry$Type getType(java.lang.Object,org.ini4j.Registry$Type)
meth public abstract org.ini4j.Registry$Type putType(java.lang.String,org.ini4j.Registry$Type)
meth public abstract org.ini4j.Registry$Type removeType(java.lang.Object)

CLSS public final static !enum org.ini4j.Registry$Type
 outer org.ini4j.Registry
fld public final static char REMOVE_CHAR = '-'
fld public final static char SEPARATOR_CHAR = ':'
fld public final static java.lang.String REMOVE
fld public final static java.lang.String SEPARATOR
fld public final static org.ini4j.Registry$Type REG_BINARY
fld public final static org.ini4j.Registry$Type REG_DWORD
fld public final static org.ini4j.Registry$Type REG_DWORD_BIG_ENDIAN
fld public final static org.ini4j.Registry$Type REG_EXPAND_SZ
fld public final static org.ini4j.Registry$Type REG_FULL_RESOURCE_DESCRIPTOR
fld public final static org.ini4j.Registry$Type REG_LINK
fld public final static org.ini4j.Registry$Type REG_MULTI_SZ
fld public final static org.ini4j.Registry$Type REG_NONE
fld public final static org.ini4j.Registry$Type REG_QWORD
fld public final static org.ini4j.Registry$Type REG_RESOURCE_LIST
fld public final static org.ini4j.Registry$Type REG_RESOURCE_REQUIREMENTS_LIST
fld public final static org.ini4j.Registry$Type REG_SZ
meth public java.lang.String toString()
meth public static org.ini4j.Registry$Type fromString(java.lang.String)
meth public static org.ini4j.Registry$Type valueOf(java.lang.String)
meth public static org.ini4j.Registry$Type[] values()
supr java.lang.Enum<org.ini4j.Registry$Type>
hfds MAPPING,_prefix

CLSS public org.ini4j.Wini
cons public init()
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.Reader) throws java.io.IOException
cons public init(java.net.URL) throws java.io.IOException
fld public final static char PATH_SEPARATOR = '\u005c'
meth public java.lang.String escape(java.lang.String)
meth public java.lang.String unescape(java.lang.String)
supr org.ini4j.Ini
hfds serialVersionUID

CLSS public abstract org.ini4j.spi.AbstractBeanInvocationHandler
cons public init()
intf java.lang.reflect.InvocationHandler
meth protected abstract boolean hasPropertySpi(java.lang.String)
meth protected abstract java.lang.Object getPropertySpi(java.lang.String,java.lang.Class<?>)
meth protected abstract void setPropertySpi(java.lang.String,java.lang.Object,java.lang.Class<?>)
meth protected boolean hasProperty(java.lang.String)
meth protected java.lang.Object getProperty(java.lang.String,java.lang.Class<?>)
meth protected java.lang.Object getProxy()
meth protected java.lang.Object parse(java.lang.String,java.lang.Class<?>)
meth protected java.lang.Object zero(java.lang.Class<?>)
meth protected void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth protected void addVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth protected void removeVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth protected void setProperty(java.lang.String,java.lang.Object,java.lang.Class<?>) throws java.beans.PropertyVetoException
meth public java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.beans.PropertyVetoException
supr java.lang.Object
hfds ADD_PREFIX,HAS_PREFIX,PROPERTY_CHANGE_LISTENER,READ_BOOLEAN_PREFIX,READ_PREFIX,REMOVE_PREFIX,VETOABLE_CHANGE_LISTENER,WRITE_PREFIX,_pcSupport,_proxy,_vcSupport
hcls Prefix

CLSS public abstract interface org.ini4j.spi.BeanAccess
meth public abstract int propLength(java.lang.String)
meth public abstract java.lang.String propDel(java.lang.String)
meth public abstract java.lang.String propGet(java.lang.String)
meth public abstract java.lang.String propGet(java.lang.String,int)
meth public abstract java.lang.String propSet(java.lang.String,java.lang.String)
meth public abstract java.lang.String propSet(java.lang.String,java.lang.String,int)
meth public abstract void propAdd(java.lang.String,java.lang.String)

CLSS public org.ini4j.spi.BeanTool
cons public init()
meth protected java.lang.Object parseSpecialValue(java.lang.String,java.lang.Class)
meth public <%0 extends java.lang.Object> {%%0} parse(java.lang.String,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} proxy(java.lang.Class<{%%0}>,org.ini4j.spi.BeanAccess)
meth public <%0 extends java.lang.Object> {%%0} zero(java.lang.Class<{%%0}>)
meth public final static org.ini4j.spi.BeanTool getInstance()
meth public void inject(java.lang.Object,org.ini4j.spi.BeanAccess)
meth public void inject(org.ini4j.spi.BeanAccess,java.lang.Object)
supr java.lang.Object
hfds INSTANCE,PARSE_METHOD
hcls BeanInvocationHandler

CLSS public org.ini4j.spi.EscapeTool
cons public init()
meth public java.lang.String escape(java.lang.String)
meth public java.lang.String quote(java.lang.String)
meth public java.lang.String unescape(java.lang.String)
meth public java.lang.String unquote(java.lang.String)
meth public static org.ini4j.spi.EscapeTool getInstance()
supr java.lang.Object
hfds ASCII_MAX,ASCII_MIN,DOUBLE_QUOTE,ESCAPEABLE_CHARS,ESCAPE_CHAR,ESCAPE_LETTERS,HEX,HEX_DIGIT_1_OFFSET,HEX_DIGIT_2_OFFSET,HEX_DIGIT_3_OFFSET,HEX_DIGIT_MASK,HEX_RADIX,INSTANCE,UNICODE_HEX_DIGITS

CLSS public org.ini4j.spi.IniBuilder
cons public init()
intf org.ini4j.spi.IniHandler
meth public static org.ini4j.spi.IniBuilder newInstance(org.ini4j.Ini)
meth public void endIni()
meth public void endSection()
meth public void handleComment(java.lang.String)
meth public void handleOption(java.lang.String,java.lang.String)
meth public void setIni(org.ini4j.Ini)
meth public void startIni()
meth public void startSection(java.lang.String)
supr java.lang.Object
hfds _ini

CLSS public org.ini4j.spi.IniFormatter
cons public init()
intf org.ini4j.spi.IniHandler
meth protected java.io.PrintWriter getOutput()
meth protected org.ini4j.Config getConfig()
meth protected void setConfig(org.ini4j.Config)
meth protected void setOutput(java.io.PrintWriter)
meth public static org.ini4j.spi.IniFormatter newInstance(java.io.Writer,org.ini4j.Config)
meth public void endIni()
meth public void endSection()
meth public void handleComment(java.lang.String)
meth public void handleOption(java.lang.String,java.lang.String)
meth public void startIni()
meth public void startSection(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.ini4j.spi.IniHandler
meth public abstract void endIni()
meth public abstract void endSection()
meth public abstract void handleComment(java.lang.String)
meth public abstract void handleOption(java.lang.String,java.lang.String)
meth public abstract void startIni()
meth public abstract void startSection(java.lang.String)

CLSS public org.ini4j.spi.IniParser
cons public init()
meth protected org.ini4j.Config getConfig()
meth protected void parseError(java.lang.String,int) throws org.ini4j.InvalidFileFormatException
meth protected void setConfig(org.ini4j.Config)
meth public static org.ini4j.spi.IniParser newInstance()
meth public static org.ini4j.spi.IniParser newInstance(org.ini4j.Config)
meth public void parse(java.io.InputStream,org.ini4j.spi.IniHandler) throws java.io.IOException
meth public void parse(java.io.Reader,org.ini4j.spi.IniHandler) throws java.io.IOException
meth public void parse(java.net.URL,org.ini4j.spi.IniHandler) throws java.io.IOException
supr java.lang.Object
hfds COMMENTS,OPERATORS,SECTION_BEGIN,SECTION_END

CLSS public org.ini4j.spi.OptionsBuilder
cons public init()
intf org.ini4j.spi.OptionsHandler
meth protected static org.ini4j.spi.OptionsBuilder newInstance()
meth public static org.ini4j.spi.OptionsBuilder newInstance(org.ini4j.Options)
meth public void endOptions()
meth public void handleComment(java.lang.String)
meth public void handleOption(java.lang.String,java.lang.String)
meth public void setOptions(org.ini4j.Options)
meth public void startOptions()
supr java.lang.Object
hfds _header,_lastComment,_options

CLSS public org.ini4j.spi.OptionsFormatter
cons public init()
intf org.ini4j.spi.OptionsHandler
meth protected java.io.PrintWriter getOutput()
meth protected org.ini4j.Config getConfig()
meth protected void setConfig(org.ini4j.Config)
meth protected void setOutput(java.io.PrintWriter)
meth public static org.ini4j.spi.OptionsFormatter newInstance(java.io.Writer,org.ini4j.Config)
meth public void endOptions()
meth public void handleComment(java.lang.String)
meth public void handleOption(java.lang.String,java.lang.String)
meth public void startOptions()
supr java.lang.Object

CLSS public abstract interface org.ini4j.spi.OptionsHandler
meth public abstract void endOptions()
meth public abstract void handleComment(java.lang.String)
meth public abstract void handleOption(java.lang.String,java.lang.String)
meth public abstract void startOptions()

CLSS public org.ini4j.spi.OptionsParser
cons public init()
meth protected org.ini4j.Config getConfig()
meth protected void parseError(java.lang.String,int) throws org.ini4j.InvalidFileFormatException
meth protected void setConfig(org.ini4j.Config)
meth public static org.ini4j.spi.OptionsParser newInstance()
meth public static org.ini4j.spi.OptionsParser newInstance(org.ini4j.Config)
meth public void parse(java.io.InputStream,org.ini4j.spi.OptionsHandler) throws java.io.IOException
meth public void parse(java.io.Reader,org.ini4j.spi.OptionsHandler) throws java.io.IOException
meth public void parse(java.net.URL,org.ini4j.spi.OptionsHandler) throws java.io.IOException
supr java.lang.Object
hfds COMMENTS,OPERATORS

CLSS public org.ini4j.spi.RegBuilder
cons public init()
intf org.ini4j.spi.IniHandler
meth public static org.ini4j.spi.RegBuilder newInstance(org.ini4j.Reg)
meth public void endIni()
meth public void endSection()
meth public void handleComment(java.lang.String)
meth public void handleOption(java.lang.String,java.lang.String)
meth public void setReg(org.ini4j.Reg)
meth public void startIni()
meth public void startSection(java.lang.String)
supr java.lang.Object
hfds _reg

CLSS public org.ini4j.spi.RegEscapeTool
cons public init()
meth public final static org.ini4j.spi.RegEscapeTool getInstance()
meth public java.lang.String encode(org.ini4j.spi.TypeValuesPair)
meth public org.ini4j.spi.TypeValuesPair decode(java.lang.String)
supr org.ini4j.spi.EscapeTool
hfds DIGIT_SIZE,HEX_CHARSET,INSTANCE,LOWER_DIGIT,UPPER_DIGIT

CLSS public org.ini4j.spi.TypeValuesPair
cons public init(org.ini4j.Registry$Type,java.lang.String[])
meth public java.lang.String[] getValues()
meth public org.ini4j.Registry$Type getType()
supr java.lang.Object
hfds _type,_values

CLSS public final org.ini4j.spi.Warnings
fld public final static java.lang.String UNCHECKED = "unchecked"
supr java.lang.Object

CLSS public org.ini4j.spi.WinEscapeTool
cons public init()
meth public static org.ini4j.spi.WinEscapeTool getInstance()
supr org.ini4j.spi.EscapeTool
hfds ANSI_HEX_DIGITS,ANSI_OCTAL_DIGITS,INSTANCE,OCTAL_RADIX

