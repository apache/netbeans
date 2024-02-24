#Signature file v4.1
#Version 1.46

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.api.whitelist.WhiteListQuery
innr public final static !enum Operation
innr public final static Result
innr public final static RuleDescription
innr public final static WhiteList
meth public static boolean isWhiteListEnabledInProject(org.netbeans.api.project.Project,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.whitelist.WhiteListQuery$WhiteList getWhiteList(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void enableWhiteListInProject(org.netbeans.api.project.Project,java.lang.String,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds mergedGlobalWhiteLists

CLSS public final static !enum org.netbeans.api.whitelist.WhiteListQuery$Operation
 outer org.netbeans.api.whitelist.WhiteListQuery
fld public final static org.netbeans.api.whitelist.WhiteListQuery$Operation USAGE
meth public static org.netbeans.api.whitelist.WhiteListQuery$Operation valueOf(java.lang.String)
meth public static org.netbeans.api.whitelist.WhiteListQuery$Operation[] values()
supr java.lang.Enum<org.netbeans.api.whitelist.WhiteListQuery$Operation>

CLSS public final static org.netbeans.api.whitelist.WhiteListQuery$Result
 outer org.netbeans.api.whitelist.WhiteListQuery
cons public init()
cons public init(java.util.List<? extends org.netbeans.api.whitelist.WhiteListQuery$RuleDescription>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean isAllowed()
meth public java.util.List<? extends org.netbeans.api.whitelist.WhiteListQuery$RuleDescription> getViolatedRules()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds allowed,violatedRules

CLSS public final static org.netbeans.api.whitelist.WhiteListQuery$RuleDescription
 outer org.netbeans.api.whitelist.WhiteListQuery
cons public init(java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String getRuleDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getRuleName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getWhiteListID()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds ruleDescription,ruleName,whiteListID

CLSS public final static org.netbeans.api.whitelist.WhiteListQuery$WhiteList
 outer org.netbeans.api.whitelist.WhiteListQuery
meth public final org.netbeans.api.whitelist.WhiteListQuery$Result check(org.netbeans.api.java.source.ElementHandle<?>,org.netbeans.api.whitelist.WhiteListQuery$Operation)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.api.whitelist.index.WhiteListIndex
innr public final static Problem
meth public !varargs java.util.Collection<? extends org.netbeans.api.whitelist.index.WhiteListIndex$Problem> getWhiteListViolations(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.whitelist.index.WhiteListIndex getDefault()
meth public void addWhiteListIndexListener(org.netbeans.api.whitelist.index.WhiteListIndexListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removeWhiteListIndexListener(org.netbeans.api.whitelist.index.WhiteListIndexListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds instance,listeners
hcls WhiteListIndexAccessorImpl

CLSS public final static org.netbeans.api.whitelist.index.WhiteListIndex$Problem
 outer org.netbeans.api.whitelist.index.WhiteListIndex
meth public int getLine()
meth public org.netbeans.api.whitelist.WhiteListQuery$Result getResult()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds line,relPath,result,root

CLSS public org.netbeans.api.whitelist.index.WhiteListIndexEvent
meth public java.net.URL getRoot()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.util.EventObject
hfds root

CLSS public abstract interface org.netbeans.api.whitelist.index.WhiteListIndexListener
intf java.util.EventListener
meth public abstract void indexChanged(org.netbeans.api.whitelist.index.WhiteListIndexEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.api.whitelist.support.WhiteListSupport
meth public static java.util.Map<? extends com.sun.source.tree.Tree,? extends org.netbeans.api.whitelist.WhiteListQuery$Result> getWhiteListViolations(com.sun.source.tree.CompilationUnitTree,org.netbeans.api.whitelist.WhiteListQuery$WhiteList,com.sun.source.util.Trees,java.util.concurrent.Callable<java.lang.Boolean>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds LOG
hcls WhiteListScanner

CLSS public abstract interface org.netbeans.spi.whitelist.WhiteListQueryImplementation
innr public abstract interface static UserSelectable
innr public abstract interface static WhiteListImplementation
meth public abstract org.netbeans.spi.whitelist.WhiteListQueryImplementation$WhiteListImplementation getWhiteList(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.spi.whitelist.WhiteListQueryImplementation$UserSelectable
 outer org.netbeans.spi.whitelist.WhiteListQueryImplementation
intf org.netbeans.spi.whitelist.WhiteListQueryImplementation
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()

CLSS public abstract interface static org.netbeans.spi.whitelist.WhiteListQueryImplementation$WhiteListImplementation
 outer org.netbeans.spi.whitelist.WhiteListQueryImplementation
meth public abstract org.netbeans.api.whitelist.WhiteListQuery$Result check(org.netbeans.api.java.source.ElementHandle<?>,org.netbeans.api.whitelist.WhiteListQuery$Operation)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.spi.whitelist.support.WhiteListQueryMergerSupport
cons public init()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.whitelist.WhiteListQueryImplementation> createWhiteListQueryMerger()
supr java.lang.Object

