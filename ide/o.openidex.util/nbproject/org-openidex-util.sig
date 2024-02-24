#Signature file v4.1
#Version 3.70

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
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

CLSS public abstract org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static Registry
innr public final static Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String displayName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public final org.openide.ServiceType createClone()
 anno 0 java.lang.Deprecated()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds err,name,serialVersionUID,supp

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public org.openidex.search.DataObjectSearchGroup
cons public init()
meth protected void add(org.openidex.search.SearchType)
meth public org.openide.nodes.Node getNodeForFoundObject(java.lang.Object)
meth public void doSearch()
supr org.openidex.search.SearchGroup
hfds MB,REQUIRED_PER_FULL_GC,REQUIRED_PER_ITERATION,lowMemoryWarning,lowMemoryWarningCount

CLSS public abstract interface org.openidex.search.FileObjectFilter
fld public final static int DO_NOT_TRAVERSE = 0
fld public final static int TRAVERSE = 1
fld public final static int TRAVERSE_ALL_SUBFOLDERS = 2
meth public abstract boolean searchFile(org.openide.filesystems.FileObject)
meth public abstract int traverseFolder(org.openide.filesystems.FileObject)

CLSS public org.openidex.search.FileObjectSearchGroup
cons public init()
meth protected void add(org.openidex.search.SearchType)
meth public org.openide.nodes.Node getNodeForFoundObject(java.lang.Object)
meth public void doSearch()
supr org.openidex.search.SearchGroup

CLSS public abstract org.openidex.search.SearchGroup
cons public init()
fld protected final java.util.Set<java.lang.Object> resultObjects
fld protected final java.util.Set<org.openide.nodes.Node> searchRoots
fld protected org.openidex.search.SearchType[] searchTypes
fld protected volatile boolean stopped
fld public final static java.lang.String PROP_FOUND = "org.openidex.search.found"
fld public final static java.lang.String PROP_RESULT = "org.openidex.search.result"
innr public abstract interface static Factory
innr public final static Registry
meth protected abstract void doSearch()
meth protected void add(org.openidex.search.SearchType)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void onStopSearch()
meth protected void prepareSearch()
meth protected void processSearchObject(java.lang.Object)
meth public abstract org.openide.nodes.Node getNodeForFoundObject(java.lang.Object)
meth public final void stopSearch()
meth public java.util.Set<java.lang.Object> getResultObjects()
meth public org.openide.nodes.Node[] getSearchRoots()
meth public org.openidex.search.SearchType[] getSearchTypes()
meth public static org.openidex.search.SearchGroup[] createSearchGroups(org.openidex.search.SearchType[])
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void search()
meth public void setSearchRootNodes(org.openide.nodes.Node[])
supr java.lang.Object
hfds propChangeSupport,propListener

CLSS public abstract interface static org.openidex.search.SearchGroup$Factory
 outer org.openidex.search.SearchGroup
meth public abstract org.openidex.search.SearchGroup createSearchGroup()

CLSS public final static org.openidex.search.SearchGroup$Registry
 outer org.openidex.search.SearchGroup
meth public static boolean hasFactory(java.lang.Class)
meth public static boolean registerSearchGroupFactory(java.lang.Class,org.openidex.search.SearchGroup$Factory)
meth public static org.openidex.search.SearchGroup createSearchGroup(java.lang.Class)
supr java.lang.Object
hfds registry

CLSS public final org.openidex.search.SearchHistory
fld public final static java.lang.String ADD_TO_HISTORY = "add-to-history"
fld public final static java.lang.String LAST_SELECTED = "last-selected"
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.openidex.search.SearchPattern> getSearchPatterns()
meth public org.openidex.search.SearchPattern getLastSelected()
 anno 0 java.lang.Deprecated()
meth public static org.openidex.search.SearchHistory getDefault()
meth public void add(org.openidex.search.SearchPattern)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setLastSelected(org.openidex.search.SearchPattern)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds INSTANCE,MAX_SEARCH_PATTERNS_ITEMS,PREFS_NODE,PROP_SEARCH_PATTERN_PREFIX,pcs,prefs,searchPatternsList

CLSS public abstract interface org.openidex.search.SearchInfo
innr public abstract interface static Files
meth public abstract boolean canSearch()
meth public abstract java.util.Iterator<org.openide.loaders.DataObject> objectsToSearch()

CLSS public abstract interface static org.openidex.search.SearchInfo$Files
 outer org.openidex.search.SearchInfo
intf org.openidex.search.SearchInfo
meth public abstract java.util.Iterator<org.openide.filesystems.FileObject> filesToSearch()

CLSS public final org.openidex.search.SearchInfoFactory
fld public final static org.openidex.search.FileObjectFilter SHARABILITY_FILTER
fld public final static org.openidex.search.FileObjectFilter VISIBILITY_FILTER
meth public !varargs static org.openidex.search.SearchInfo createCompoundSearchInfo(org.openidex.search.SearchInfo[])
meth public static org.openidex.search.SearchInfo createSearchInfo(org.openide.filesystems.FileObject,boolean,org.openidex.search.FileObjectFilter[])
meth public static org.openidex.search.SearchInfo createSearchInfo(org.openide.filesystems.FileObject[],boolean,org.openidex.search.FileObjectFilter[])
meth public static org.openidex.search.SearchInfo createSearchInfoBySubnodes(org.openide.nodes.Node)
supr java.lang.Object

CLSS public final org.openidex.search.SearchPattern
meth public boolean equals(java.lang.Object)
meth public boolean isMatchCase()
meth public boolean isRegExp()
meth public boolean isWholeWords()
meth public int hashCode()
meth public java.lang.String getSearchExpression()
meth public static org.openidex.search.SearchPattern create(java.lang.String,boolean,boolean,boolean)
supr java.lang.Object
hfds matchCase,regExp,searchExpression,wholeWords

CLSS public abstract org.openidex.search.SearchType
cons public init()
fld protected final static java.lang.String PROP_OBJECT_CHANGED = "org.openidex.search.objectChanged"
fld public final static java.lang.String PROP_VALID = "valid"
intf java.lang.Cloneable
meth protected abstract boolean testObject(java.lang.Object)
meth protected abstract java.lang.Class[] createSearchTypeClasses()
meth protected boolean acceptSearchObject(java.lang.Object)
meth protected org.openide.nodes.Node[] acceptSearchRootNodes(org.openide.nodes.Node[])
meth protected void prepareSearchObject(java.lang.Object)
meth public abstract boolean enabled(org.openide.nodes.Node[])
meth public final boolean isValid()
meth public final java.lang.Class[] getSearchTypeClasses()
meth public final void setValid(boolean)
meth public java.lang.Object clone()
meth public org.openide.nodes.Node[] getDetails(java.lang.Object)
meth public org.openide.nodes.Node[] getDetails(org.openide.nodes.Node)
meth public static java.util.Enumeration<? extends org.openidex.search.SearchType> enumerateSearchTypes()
 anno 0 java.lang.Deprecated()
supr org.openide.ServiceType
hfds searchTypeClasses,serialVersionUID,valid

CLSS public final org.openidex.search.Utils
meth public static java.util.Iterator<org.openide.filesystems.FileObject> getFileObjectsIterator(org.openidex.search.SearchInfo)
supr java.lang.Object

