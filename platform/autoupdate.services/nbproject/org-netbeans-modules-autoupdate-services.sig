#Signature file v4.1
#Version 1.79

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

CLSS public final org.netbeans.api.autoupdate.InstallSupport
innr public final static Installer
innr public final static Validator
meth public boolean isContentModified(org.netbeans.api.autoupdate.InstallSupport$Installer,org.netbeans.api.autoupdate.UpdateElement)
meth public boolean isSigned(org.netbeans.api.autoupdate.InstallSupport$Installer,org.netbeans.api.autoupdate.UpdateElement)
meth public boolean isSignedUnverified(org.netbeans.api.autoupdate.InstallSupport$Installer,org.netbeans.api.autoupdate.UpdateElement)
meth public boolean isSignedVerified(org.netbeans.api.autoupdate.InstallSupport$Installer,org.netbeans.api.autoupdate.UpdateElement)
meth public boolean isTrusted(org.netbeans.api.autoupdate.InstallSupport$Installer,org.netbeans.api.autoupdate.UpdateElement)
meth public java.lang.String getCertificate(org.netbeans.api.autoupdate.InstallSupport$Installer,org.netbeans.api.autoupdate.UpdateElement)
meth public org.netbeans.api.autoupdate.InstallSupport$Installer doValidate(org.netbeans.api.autoupdate.InstallSupport$Validator,org.netbeans.api.progress.ProgressHandle) throws org.netbeans.api.autoupdate.OperationException
meth public org.netbeans.api.autoupdate.InstallSupport$Validator doDownload(org.netbeans.api.progress.ProgressHandle,boolean) throws org.netbeans.api.autoupdate.OperationException
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.autoupdate.InstallSupport$Validator doDownload(org.netbeans.api.progress.ProgressHandle,java.lang.Boolean,boolean) throws org.netbeans.api.autoupdate.OperationException
meth public org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.InstallSupport> getContainer()
meth public org.netbeans.api.autoupdate.OperationSupport$Restarter doInstall(org.netbeans.api.autoupdate.InstallSupport$Installer,org.netbeans.api.progress.ProgressHandle) throws org.netbeans.api.autoupdate.OperationException
meth public void doCancel() throws org.netbeans.api.autoupdate.OperationException
meth public void doRestart(org.netbeans.api.autoupdate.OperationSupport$Restarter,org.netbeans.api.progress.ProgressHandle) throws org.netbeans.api.autoupdate.OperationException
meth public void doRestartLater(org.netbeans.api.autoupdate.OperationSupport$Restarter)
supr java.lang.Object
hfds container,impl

CLSS public final static org.netbeans.api.autoupdate.InstallSupport$Installer
 outer org.netbeans.api.autoupdate.InstallSupport
supr java.lang.Object

CLSS public final static org.netbeans.api.autoupdate.InstallSupport$Validator
 outer org.netbeans.api.autoupdate.InstallSupport
supr java.lang.Object

CLSS public final org.netbeans.api.autoupdate.OperationContainer<%0 extends java.lang.Object>
innr public final static OperationInfo
meth public boolean canBeAdded(org.netbeans.api.autoupdate.UpdateUnit,org.netbeans.api.autoupdate.UpdateElement)
meth public boolean contains(org.netbeans.api.autoupdate.UpdateElement)
meth public boolean remove(org.netbeans.api.autoupdate.UpdateElement)
meth public final void setUnpack200(java.io.File)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.api.autoupdate.OperationContainer$OperationInfo<{org.netbeans.api.autoupdate.OperationContainer%0}>> listAll()
meth public java.util.List<org.netbeans.api.autoupdate.OperationContainer$OperationInfo<{org.netbeans.api.autoupdate.OperationContainer%0}>> listInvalid()
meth public org.netbeans.api.autoupdate.OperationContainer$OperationInfo<{org.netbeans.api.autoupdate.OperationContainer%0}> add(org.netbeans.api.autoupdate.UpdateElement)
meth public org.netbeans.api.autoupdate.OperationContainer$OperationInfo<{org.netbeans.api.autoupdate.OperationContainer%0}> add(org.netbeans.api.autoupdate.UpdateUnit,org.netbeans.api.autoupdate.UpdateElement)
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.InstallSupport> createForInstall()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.InstallSupport> createForInternalUpdate()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.InstallSupport> createForUpdate()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForCustomInstallComponent()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForCustomUninstallComponent()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForDirectDisable()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForDirectInstall()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForDirectUninstall()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForDirectUpdate()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForDisable()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForEnable()
meth public static org.netbeans.api.autoupdate.OperationContainer<org.netbeans.api.autoupdate.OperationSupport> createForUninstall()
meth public void add(java.util.Collection<org.netbeans.api.autoupdate.UpdateElement>)
meth public void add(java.util.Map<org.netbeans.api.autoupdate.UpdateUnit,org.netbeans.api.autoupdate.UpdateElement>)
meth public void remove(java.util.Collection<org.netbeans.api.autoupdate.UpdateElement>)
meth public void remove(org.netbeans.api.autoupdate.OperationContainer$OperationInfo<{org.netbeans.api.autoupdate.OperationContainer%0}>)
meth public void removeAll()
meth public {org.netbeans.api.autoupdate.OperationContainer%0} getSupport()
supr java.lang.Object
hfds impl,support,upToDate

CLSS public final static org.netbeans.api.autoupdate.OperationContainer$OperationInfo<%0 extends java.lang.Object>
 outer org.netbeans.api.autoupdate.OperationContainer
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getBrokenDependencies()
meth public java.util.Set<java.lang.String> getMissingParts()
meth public java.util.Set<org.netbeans.api.autoupdate.UpdateElement> getRequiredElements()
meth public org.netbeans.api.autoupdate.UpdateElement getUpdateElement()
meth public org.netbeans.api.autoupdate.UpdateUnit getUpdateUnit()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.api.autoupdate.OperationException
cons public init(org.netbeans.api.autoupdate.OperationException$ERROR_TYPE)
cons public init(org.netbeans.api.autoupdate.OperationException$ERROR_TYPE,java.lang.Exception)
cons public init(org.netbeans.api.autoupdate.OperationException$ERROR_TYPE,java.lang.String)
innr public final static !enum ERROR_TYPE
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String toString()
meth public org.netbeans.api.autoupdate.OperationException$ERROR_TYPE getErrorType()
supr java.lang.Exception
hfds error,msg

CLSS public final static !enum org.netbeans.api.autoupdate.OperationException$ERROR_TYPE
 outer org.netbeans.api.autoupdate.OperationException
fld public final static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE ENABLE
fld public final static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE INSTALL
fld public final static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE INSTALLER
fld public final static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE MISSING_UNPACK200
fld public final static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE MODIFIED
fld public final static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE PROXY
fld public final static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE UNINSTALL
fld public final static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE WRITE_PERMISSION
meth public static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE valueOf(java.lang.String)
meth public static org.netbeans.api.autoupdate.OperationException$ERROR_TYPE[] values()
supr java.lang.Enum<org.netbeans.api.autoupdate.OperationException$ERROR_TYPE>

CLSS public final org.netbeans.api.autoupdate.OperationSupport
innr public final static Restarter
meth public org.netbeans.api.autoupdate.OperationSupport$Restarter doOperation(org.netbeans.api.progress.ProgressHandle) throws org.netbeans.api.autoupdate.OperationException
meth public void doCancel() throws org.netbeans.api.autoupdate.OperationException
meth public void doRestart(org.netbeans.api.autoupdate.OperationSupport$Restarter,org.netbeans.api.progress.ProgressHandle) throws org.netbeans.api.autoupdate.OperationException
meth public void doRestartLater(org.netbeans.api.autoupdate.OperationSupport$Restarter)
supr java.lang.Object
hfds container

CLSS public final static org.netbeans.api.autoupdate.OperationSupport$Restarter
 outer org.netbeans.api.autoupdate.OperationSupport
supr java.lang.Object

CLSS public final org.netbeans.api.autoupdate.PluginInstaller
cons public init()
meth public !varargs java.lang.Object install(java.lang.String,java.lang.String,org.openide.util.Lookup,java.lang.Object[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public !varargs java.lang.Object install(java.util.Set<java.lang.String>,java.lang.String,org.openide.util.Lookup,java.lang.Object[]) throws org.netbeans.api.autoupdate.OperationException,org.openide.util.UserCancelException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.Object install(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.autoupdate.PluginInstaller getDefault()
supr java.lang.Object
hfds INSTANCE,LOG

CLSS public final org.netbeans.api.autoupdate.UpdateElement
meth public boolean equals(java.lang.Object)
meth public boolean isEnabled()
meth public int getDownloadSize()
meth public int hashCode()
meth public java.awt.Image getSourceIcon()
meth public java.lang.String getAuthor()
meth public java.lang.String getCategory()
meth public java.lang.String getCodeName()
meth public java.lang.String getDate()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getHomepage()
meth public java.lang.String getLicence()
meth public java.lang.String getLicenseId()
meth public java.lang.String getNotification()
meth public java.lang.String getSource()
meth public java.lang.String getSourceDescription()
meth public java.lang.String getSpecificationVersion()
meth public java.lang.String toString()
meth public org.netbeans.api.autoupdate.UpdateUnit getUpdateUnit()
meth public org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY getSourceCategory()
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.api.autoupdate.UpdateManager
innr public final static !enum TYPE
meth public !varargs java.util.List<org.netbeans.api.autoupdate.UpdateUnit> getUpdateUnits(org.netbeans.api.autoupdate.UpdateManager$TYPE[])
meth public final static org.netbeans.api.autoupdate.UpdateManager getDefault()
meth public java.util.List<org.netbeans.api.autoupdate.UpdateUnit> getUpdateUnits()
supr java.lang.Object
hfds mgr

CLSS public final static !enum org.netbeans.api.autoupdate.UpdateManager$TYPE
 outer org.netbeans.api.autoupdate.UpdateManager
fld public final static org.netbeans.api.autoupdate.UpdateManager$TYPE CUSTOM_HANDLED_COMPONENT
fld public final static org.netbeans.api.autoupdate.UpdateManager$TYPE FEATURE
fld public final static org.netbeans.api.autoupdate.UpdateManager$TYPE KIT_MODULE
fld public final static org.netbeans.api.autoupdate.UpdateManager$TYPE LOCALIZATION
fld public final static org.netbeans.api.autoupdate.UpdateManager$TYPE MODULE
fld public final static org.netbeans.api.autoupdate.UpdateManager$TYPE STANDALONE_MODULE
meth public static org.netbeans.api.autoupdate.UpdateManager$TYPE valueOf(java.lang.String)
meth public static org.netbeans.api.autoupdate.UpdateManager$TYPE[] values()
supr java.lang.Enum<org.netbeans.api.autoupdate.UpdateManager$TYPE>

CLSS public final org.netbeans.api.autoupdate.UpdateUnit
meth public boolean equals(java.lang.Object)
meth public boolean isPending()
meth public int hashCode()
meth public java.lang.String getCodeName()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.api.autoupdate.UpdateElement> getAvailableLocalizations()
meth public java.util.List<org.netbeans.api.autoupdate.UpdateElement> getAvailableUpdates()
meth public org.netbeans.api.autoupdate.UpdateElement getBackup()
meth public org.netbeans.api.autoupdate.UpdateElement getInstalled()
meth public org.netbeans.api.autoupdate.UpdateElement getInstalledLocalization()
meth public org.netbeans.api.autoupdate.UpdateManager$TYPE getType()
meth public org.netbeans.api.autoupdate.UpdateUnit getVisibleAncestor()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.api.autoupdate.UpdateUnitProvider
innr public final static !enum CATEGORY
meth public !varargs java.util.List<org.netbeans.api.autoupdate.UpdateUnit> getUpdateUnits(org.netbeans.api.autoupdate.UpdateManager$TYPE[])
meth public boolean isEnabled()
meth public boolean isTrusted()
meth public boolean refresh(org.netbeans.api.progress.ProgressHandle,boolean) throws java.io.IOException
meth public java.awt.Image getSourceIcon()
meth public java.lang.String getContentDescription()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getSourceDescription()
meth public java.lang.String toString()
meth public java.net.URL getProviderURL()
meth public java.util.List<org.netbeans.api.autoupdate.UpdateUnit> getUpdateUnits()
meth public org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY getCategory()
 anno 0 java.lang.Deprecated()
meth public void setDisplayName(java.lang.String)
meth public void setEnable(boolean)
meth public void setProviderURL(java.net.URL)
meth public void setTrusted(boolean)
supr java.lang.Object
hfds impl

CLSS public final static !enum org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY
 outer org.netbeans.api.autoupdate.UpdateUnitProvider
fld public final static org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY BETA
fld public final static org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY COMMUNITY
fld public final static org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY STANDARD
meth public static org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY valueOf(java.lang.String)
meth public static org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY[] values()
supr java.lang.Enum<org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY>

CLSS public final org.netbeans.api.autoupdate.UpdateUnitProviderFactory
meth public !varargs org.netbeans.api.autoupdate.UpdateUnitProvider create(java.lang.String,java.io.File[])
meth public java.util.List<org.netbeans.api.autoupdate.UpdateUnitProvider> getUpdateUnitProviders(boolean)
meth public org.netbeans.api.autoupdate.UpdateUnitProvider create(java.lang.String,java.lang.String,java.net.URL)
meth public org.netbeans.api.autoupdate.UpdateUnitProvider create(java.lang.String,java.lang.String,java.net.URL,java.lang.String,java.lang.String)
meth public org.netbeans.api.autoupdate.UpdateUnitProvider create(java.lang.String,java.lang.String,java.net.URL,org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY)
meth public static org.netbeans.api.autoupdate.UpdateUnitProviderFactory getDefault()
meth public void refreshProviders(org.netbeans.api.progress.ProgressHandle,boolean) throws java.io.IOException
meth public void remove(org.netbeans.api.autoupdate.UpdateUnitProvider)
supr java.lang.Object
hfds INSTANCE

CLSS public abstract org.netbeans.spi.autoupdate.AutoupdateClusterCreator
cons public init()
meth protected abstract java.io.File findCluster(java.lang.String)
meth protected abstract java.io.File[] registerCluster(java.lang.String,java.io.File) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.autoupdate.CustomInstaller
meth public abstract boolean install(java.lang.String,java.lang.String,org.netbeans.api.progress.ProgressHandle) throws org.netbeans.api.autoupdate.OperationException

CLSS public abstract interface org.netbeans.spi.autoupdate.CustomUninstaller
meth public abstract boolean uninstall(java.lang.String,java.lang.String,org.netbeans.api.progress.ProgressHandle) throws org.netbeans.api.autoupdate.OperationException

CLSS public abstract interface org.netbeans.spi.autoupdate.KeyStoreProvider
innr public final static !enum TrustLevel
meth public abstract java.security.KeyStore getKeyStore()
meth public org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel getTrustLevel()

CLSS public final static !enum org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel
 outer org.netbeans.spi.autoupdate.KeyStoreProvider
fld public final static org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel TRUST
fld public final static org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel TRUST_CA
fld public final static org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel VALIDATE
fld public final static org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel VALIDATE_CA
meth public static org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel valueOf(java.lang.String)
meth public static org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel[] values()
supr java.lang.Enum<org.netbeans.spi.autoupdate.KeyStoreProvider$TrustLevel>

CLSS public abstract interface org.netbeans.spi.autoupdate.PluginInstallerImplementation
meth public abstract !varargs java.lang.Object install(java.util.Set<java.lang.String>,java.lang.String,org.openide.util.Lookup,java.lang.Object[]) throws org.netbeans.api.autoupdate.OperationException,org.openide.util.UserCancelException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.spi.autoupdate.UpdateItem
meth public static org.netbeans.spi.autoupdate.UpdateItem createFeature(java.lang.String,java.lang.String,java.util.Set<java.lang.String>,java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.autoupdate.UpdateItem createInstalledNativeComponent(java.lang.String,java.lang.String,java.util.Set<java.lang.String>,java.lang.String,java.lang.String,org.netbeans.spi.autoupdate.CustomUninstaller)
meth public static org.netbeans.spi.autoupdate.UpdateItem createLocalization(java.lang.String,java.lang.String,java.lang.String,java.util.Locale,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL,java.lang.Boolean,java.lang.Boolean,java.lang.String,org.netbeans.spi.autoupdate.UpdateLicense)
meth public static org.netbeans.spi.autoupdate.UpdateItem createModule(java.lang.String,java.lang.String,java.net.URL,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.jar.Manifest,java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.String,org.netbeans.spi.autoupdate.UpdateLicense)
meth public static org.netbeans.spi.autoupdate.UpdateItem createModule(java.lang.String,java.lang.String,java.net.URL,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.jar.Manifest,java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.String,org.netbeans.spi.autoupdate.UpdateLicense)
meth public static org.netbeans.spi.autoupdate.UpdateItem createNativeComponent(java.lang.String,java.lang.String,java.lang.String,java.util.Set<java.lang.String>,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,java.lang.String,org.netbeans.spi.autoupdate.CustomInstaller,org.netbeans.spi.autoupdate.UpdateLicense)
supr java.lang.Object
hfds impl,original

CLSS public final org.netbeans.spi.autoupdate.UpdateLicense
meth public final static org.netbeans.spi.autoupdate.UpdateLicense createUpdateLicense(java.lang.String,java.lang.String)
supr java.lang.Object
hfds impl

CLSS public abstract interface org.netbeans.spi.autoupdate.UpdateProvider
meth public abstract boolean refresh(boolean) throws java.io.IOException
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getName()
meth public abstract java.util.Map<java.lang.String,org.netbeans.spi.autoupdate.UpdateItem> getUpdateItems() throws java.io.IOException
meth public abstract org.netbeans.api.autoupdate.UpdateUnitProvider$CATEGORY getCategory()

