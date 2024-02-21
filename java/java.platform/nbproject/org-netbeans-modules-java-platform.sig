#Signature file v4.1
#Version 1.65

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

CLSS public abstract org.netbeans.api.java.platform.JavaPlatform
cons protected init()
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_JAVADOC_FOLDER = "javadocFolders"
fld public final static java.lang.String PROP_SOURCE_FOLDER = "sourceFolders"
fld public final static java.lang.String PROP_SYSTEM_PROPERTIES = "systemProperties"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void setSystemProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getVendor()
meth public abstract java.util.Collection<org.openide.filesystems.FileObject> getInstallFolders()
meth public abstract java.util.List<java.net.URL> getJavadocFolders()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract org.netbeans.api.java.classpath.ClassPath getBootstrapLibraries()
meth public abstract org.netbeans.api.java.classpath.ClassPath getSourceFolders()
meth public abstract org.netbeans.api.java.classpath.ClassPath getStandardLibraries()
meth public abstract org.netbeans.api.java.platform.Specification getSpecification()
meth public abstract org.openide.filesystems.FileObject findTool(java.lang.String)
meth public boolean isValid()
meth public final java.util.Map<java.lang.String,java.lang.String> getSystemProperties()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static org.netbeans.api.java.platform.JavaPlatform getDefault()
supr java.lang.Object
hfds supp,sysproperties

CLSS public final org.netbeans.api.java.platform.JavaPlatformManager
cons public init()
fld public final static java.lang.String PROP_INSTALLED_PLATFORMS = "installedPlatforms"
meth public org.netbeans.api.java.platform.JavaPlatform getDefaultPlatform()
meth public org.netbeans.api.java.platform.JavaPlatform[] getInstalledPlatforms()
meth public org.netbeans.api.java.platform.JavaPlatform[] getPlatforms(java.lang.String,org.netbeans.api.java.platform.Specification)
meth public static org.netbeans.api.java.platform.JavaPlatformManager getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds cachedPlatforms,instance,lastProviders,pListener,pcs,providers,providersValid

CLSS public org.netbeans.api.java.platform.Profile
cons public init(java.lang.String,org.openide.modules.SpecificationVersion)
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getName()
meth public final org.openide.modules.SpecificationVersion getVersion()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds name,version

CLSS public final org.netbeans.api.java.platform.Specification
cons public init(java.lang.String,org.openide.modules.SpecificationVersion)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
cons public init(java.lang.String,org.openide.modules.SpecificationVersion,java.lang.String,org.netbeans.api.java.platform.Profile[])
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
cons public init(java.lang.String,org.openide.modules.SpecificationVersion,org.netbeans.api.java.platform.Profile[])
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getName()
meth public final org.netbeans.api.java.platform.Profile[] getProfiles()
meth public final org.openide.modules.SpecificationVersion getVersion()
meth public int hashCode()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
supr java.lang.Object
hfds displayName,name,profiles,version

CLSS public abstract interface org.netbeans.modules.java.platform.implspi.JavaPlatformProvider
fld public final static java.lang.String PROP_INSTALLED_PLATFORMS = "installedPlatforms"
meth public abstract org.netbeans.api.java.platform.JavaPlatform getDefaultPlatform()
meth public abstract org.netbeans.api.java.platform.JavaPlatform[] getInstalledPlatforms()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS abstract interface org.netbeans.modules.java.platform.implspi.package-info

CLSS public abstract interface org.netbeans.spi.java.platform.JavaPlatformFactory
innr public abstract interface static Provider
meth public abstract org.netbeans.api.java.platform.JavaPlatform create(org.openide.filesystems.FileObject,java.lang.String,boolean) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.spi.java.platform.JavaPlatformFactory$Provider
 outer org.netbeans.spi.java.platform.JavaPlatformFactory
meth public abstract org.netbeans.spi.java.platform.JavaPlatformFactory forType(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.spi.java.platform.support.ForwardingJavaPlatform
cons public init(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()
fld protected final org.netbeans.api.java.platform.JavaPlatform delegate
meth public java.lang.String getDisplayName()
meth public java.lang.String getVendor()
meth public java.util.Collection<org.openide.filesystems.FileObject> getInstallFolders()
meth public java.util.List<java.net.URL> getJavadocFolders()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public org.netbeans.api.java.classpath.ClassPath getBootstrapLibraries()
meth public org.netbeans.api.java.classpath.ClassPath getSourceFolders()
meth public org.netbeans.api.java.classpath.ClassPath getStandardLibraries()
meth public org.netbeans.api.java.platform.Specification getSpecification()
meth public org.openide.filesystems.FileObject findTool(java.lang.String)
supr org.netbeans.api.java.platform.JavaPlatform

