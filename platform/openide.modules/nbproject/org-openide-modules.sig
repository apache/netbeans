#Signature file v4.1
#Version 7.71

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public abstract interface !annotation org.openide.modules.ConstructorDelegate
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int[] delegateParams()

CLSS public final org.openide.modules.Dependency
fld public final static int COMPARE_ANY = 3
fld public final static int COMPARE_IMPL = 2
fld public final static int COMPARE_SPEC = 1
fld public final static int TYPE_IDE = 4
 anno 0 java.lang.Deprecated()
fld public final static int TYPE_JAVA = 3
fld public final static int TYPE_MODULE = 1
fld public final static int TYPE_NEEDS = 6
fld public final static int TYPE_PACKAGE = 2
fld public final static int TYPE_RECOMMENDS = 7
fld public final static int TYPE_REQUIRES = 5
fld public final static java.lang.String IDE_IMPL
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String IDE_NAME
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String JAVA_IMPL
fld public final static java.lang.String JAVA_NAME = "Java"
fld public final static java.lang.String VM_IMPL
fld public final static java.lang.String VM_NAME = "VM"
fld public final static org.openide.modules.SpecificationVersion IDE_SPEC
 anno 0 java.lang.Deprecated()
fld public final static org.openide.modules.SpecificationVersion JAVA_SPEC
fld public final static org.openide.modules.SpecificationVersion VM_SPEC
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public final int getComparison()
meth public final int getType()
meth public final java.lang.String getName()
meth public final java.lang.String getVersion()
meth public int hashCode()
meth public java.lang.String toString()
meth public static java.util.Set<org.openide.modules.Dependency> create(int,java.lang.String)
supr java.lang.Object
hfds FQN,comparison,name,serialVersionUID,type,version
hcls DependencyKey

CLSS public abstract org.openide.modules.InstalledFileLocator
cons protected init()
meth public abstract java.io.File locate(java.lang.String,java.lang.String,boolean)
meth public java.util.Set<java.io.File> locateAll(java.lang.String,java.lang.String,boolean)
meth public static org.openide.modules.InstalledFileLocator getDefault()
supr java.lang.Object
hfds DEFAULT,LOCK,instances,result

CLSS public abstract org.openide.modules.ModuleInfo
cons protected init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract boolean isEnabled()
meth public abstract boolean owns(java.lang.Class<?>)
meth public abstract int getCodeNameRelease()
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.Object getLocalizedAttribute(java.lang.String)
meth public abstract java.lang.String getCodeName()
meth public abstract java.lang.String getCodeNameBase()
meth public abstract java.util.Set<org.openide.modules.Dependency> getDependencies()
meth public abstract org.openide.modules.SpecificationVersion getSpecificationVersion()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.ClassLoader getClassLoader()
meth public java.lang.String getBuildVersion()
meth public java.lang.String getDisplayName()
meth public java.lang.String getImplementationVersion()
meth public java.lang.String[] getProvides()
supr java.lang.Object
hfds changeSupport

CLSS public org.openide.modules.ModuleInstall
cons public init()
meth protected boolean clearSharedData()
meth public boolean closing()
meth public void close()
meth public void installed()
 anno 0 java.lang.Deprecated()
meth public void restored()
meth public void uninstalled()
meth public void updated(int,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void validate()
supr org.openide.util.SharedClassObject
hfds serialVersionUID

CLSS public org.openide.modules.Modules
cons protected init()
meth public org.openide.modules.ModuleInfo findCodeNameBase(java.lang.String)
meth public org.openide.modules.ModuleInfo ownerOf(java.lang.Class<?>)
meth public static org.openide.modules.Modules getDefault()
supr java.lang.Object

CLSS public abstract interface !annotation org.openide.modules.OnStart
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.openide.modules.OnStop
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.openide.modules.PatchFor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
fld public final static java.lang.String MANIFEST_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host"
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

CLSS public abstract interface !annotation org.openide.modules.PatchedPublic
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR])
intf java.lang.annotation.Annotation

CLSS public abstract org.openide.modules.Places
cons protected init()
meth protected abstract java.io.File findCacheDirectory()
meth protected abstract java.io.File findUserDirectory()
meth public static java.io.File getCacheDirectory()
meth public static java.io.File getCacheSubdirectory(java.lang.String)
meth public static java.io.File getCacheSubfile(java.lang.String)
meth public static java.io.File getUserDirectory()
supr java.lang.Object
hfds LOG

CLSS public final org.openide.modules.SpecificationVersion
cons public init(java.lang.String)
intf java.lang.Comparable<org.openide.modules.SpecificationVersion>
meth public boolean equals(java.lang.Object)
meth public int compareTo(org.openide.modules.SpecificationVersion)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds digits,parseCache

CLSS public abstract org.openide.util.SharedClassObject
cons protected init()
intf java.io.Externalizable
meth protected boolean clearSharedData()
meth protected final java.lang.Object getLock()
meth protected final java.lang.Object getProperty(java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void finalize() throws java.lang.Throwable
meth protected java.lang.Object writeReplace()
meth protected void addNotify()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth protected void removeNotify()
meth protected void reset()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>,boolean)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds PROP_SUPPORT,addNotifySuper,alreadyWarnedAboutDupes,dataEntry,err,first,firstTrace,inReadExternal,initializeSuper,instancesBeingCreated,lock,prematureSystemOptionMutation,removeNotifySuper,serialVersionUID,systemOption,values,waitingOnSystemOption
hcls DataEntry,SetAccessibleAction,WriteReplace

CLSS public abstract interface !annotation org.openide.util.lookup.NamedServiceDefinition
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String position()
meth public abstract java.lang.Class<?>[] serviceType()
meth public abstract java.lang.String path()

