#Signature file v4.1
#Version 1.59

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

CLSS public final org.netbeans.modules.cordova.platforms.api.ClientProjectUtilities
cons public init()
meth public static boolean isUsingEmbeddedServer(org.netbeans.api.project.Project)
meth public static java.lang.String getWebContextRoot(org.netbeans.api.project.Project)
meth public static javax.swing.JPanel createMobilePlatformsSetupPanel()
meth public static org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation createMobileBrowserURLMapper()
meth public static org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation createMobileBrowser(org.netbeans.api.project.Project,org.netbeans.modules.web.browser.api.WebBrowser,org.netbeans.modules.web.browser.api.BrowserSupport,org.netbeans.spi.project.ActionProvider)
meth public static org.openide.filesystems.FileObject getSiteRoot(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject getStartFile(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public final org.netbeans.modules.cordova.platforms.api.PlatformManager
cons public init()
fld public final static java.lang.String ANDROID_TYPE = "android"
fld public final static java.lang.String IOS_TYPE = "ios"
meth public static java.util.Collection<? extends org.netbeans.modules.cordova.platforms.spi.MobilePlatform> getPlatforms()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.cordova.platforms.spi.MobilePlatform getPlatform(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.modules.cordova.platforms.api.ProcessUtilities
cons public init()
meth public !varargs static java.lang.String callProcess(java.lang.String,boolean,int,java.lang.String[]) throws java.io.IOException
supr java.lang.Object
hfds LOGGER,RP,io
hcls Redirector

CLSS public final org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport
cons public init()
meth public java.lang.String getUrl(org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public org.openide.filesystems.FileObject getFile(org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public static org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport getDefault()
meth public void reload()
meth public void startDebugging(org.netbeans.modules.cordova.platforms.spi.Device,org.netbeans.api.project.Project,org.openide.util.Lookup,boolean)
meth public void stopDebugging(boolean)
supr java.lang.Object
hfds RP,consoleLogger,debuggerSession,dispatcher,instance,networkMonitor,startDebuggingInProgress,transport,webKitDebugging
hcls MessageDispatcherImpl

CLSS public abstract interface org.netbeans.modules.cordova.platforms.spi.BuildPerformer
fld public final static java.lang.String BUILD_ANDROID = "build-android"
fld public final static java.lang.String BUILD_IOS = "build-ios"
fld public final static java.lang.String CLEAN_ANDROID = "clean-android"
fld public final static java.lang.String CLEAN_IOS = "clean-ios"
fld public final static java.lang.String REBUILD_ANDROID = "rebuild-android"
fld public final static java.lang.String REBUILD_IOS = "rebuild-ios"
fld public final static java.lang.String RUN_ANDROID = "sim-android"
fld public final static java.lang.String RUN_IOS = "sim-ios"
meth public abstract org.openide.execution.ExecutorTask perform(java.lang.String,org.netbeans.api.project.Project)

CLSS public abstract interface org.netbeans.modules.cordova.platforms.spi.Device
fld public final static java.lang.String BROWSER_PROP = "browser"
fld public final static java.lang.String DEVICE = "device"
fld public final static java.lang.String DEVICE_PROP = "device"
fld public final static java.lang.String EMULATOR = "emulator"
fld public final static java.lang.String TYPE_PROP = "type"
fld public final static java.lang.String VIRTUAL_DEVICE_PROP = "virtual.device"
meth public abstract boolean isEmulator()
meth public abstract boolean isWebViewDebugSupported()
meth public abstract org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport getDebugTransport()
meth public abstract org.netbeans.modules.cordova.platforms.spi.MobilePlatform getPlatform()
meth public abstract org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer getProjectConfigurationCustomizer(org.netbeans.api.project.Project,org.netbeans.modules.cordova.platforms.spi.PropertyProvider)
meth public abstract org.netbeans.spi.project.ActionProvider getActionProvider(org.netbeans.api.project.Project)
meth public abstract void addProperties(java.util.Properties)
meth public abstract void openUrl(java.lang.String)

CLSS public abstract org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport
cons public init()
fld protected org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback callBack
intf org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation
meth protected abstract void sendCommandImpl(org.netbeans.modules.web.webkit.debugging.spi.Command)
meth protected final java.lang.String getBundleIdentifier()
meth protected final java.lang.String translate(java.lang.String)
meth public final java.net.URL getConnectionURL()
meth public final void registerResponseCallback(org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback)
meth public final void sendCommand(org.netbeans.modules.web.webkit.debugging.spi.Command) throws org.netbeans.modules.web.webkit.debugging.api.TransportStateException
meth public final void setBaseUrl(java.lang.String)
meth public final void setBrowserURLMapper(org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation$BrowserURLMapper)
meth public final void setBundleIdentifier(java.lang.String)
meth public void flush()
supr java.lang.Object
hfds RP,bundleId,indexHtmlLocation,mapper

CLSS public abstract interface org.netbeans.modules.cordova.platforms.spi.MobilePlatform
meth public abstract boolean isReady()
meth public abstract boolean waitEmulatorReady(int)
meth public abstract java.lang.String getCodeSignIdentity()
meth public abstract java.lang.String getProvisioningProfilePath()
meth public abstract java.lang.String getSdkLocation()
meth public abstract java.lang.String getSimulatorPath()
meth public abstract java.lang.String getType()
meth public abstract java.util.Collection<? extends org.netbeans.modules.cordova.platforms.spi.Device> getConnectedDevices() throws java.io.IOException
meth public abstract java.util.Collection<? extends org.netbeans.modules.cordova.platforms.spi.Device> getVirtualDevices() throws java.io.IOException
meth public abstract java.util.Collection<? extends org.netbeans.modules.cordova.platforms.spi.ProvisioningProfile> getProvisioningProfiles()
meth public abstract java.util.Collection<? extends org.netbeans.modules.cordova.platforms.spi.SDK> getSDKs() throws java.io.IOException
meth public abstract org.netbeans.modules.cordova.platforms.spi.Device getDevice(java.lang.String,org.openide.util.EditableProperties)
meth public abstract org.netbeans.modules.cordova.platforms.spi.SDK getPrefferedTarget()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void manageDevices()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setCodeSignIdentity(java.lang.String)
meth public abstract void setProvisioningProfilePath(java.lang.String)
meth public abstract void setSdkLocation(java.lang.String)

CLSS public abstract interface org.netbeans.modules.cordova.platforms.spi.PropertyProvider
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.lang.String putProperty(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.cordova.platforms.spi.ProvisioningProfile
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getPath()

CLSS public abstract interface org.netbeans.modules.cordova.platforms.spi.SDK
meth public abstract java.lang.String getIdentifier()
meth public abstract java.lang.String getName()

CLSS public abstract interface org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation
fld public final static java.lang.String VERSION_1 = "version 1.0"
fld public final static java.lang.String VERSION_UNKNOWN_BEFORE_requestChildNodes = "version without requestChildNodes"
meth public abstract boolean attach()
meth public abstract boolean detach()
meth public abstract java.lang.String getConnectionName()
meth public abstract java.lang.String getVersion()
meth public abstract java.net.URL getConnectionURL()
meth public abstract void registerResponseCallback(org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback)
meth public abstract void sendCommand(org.netbeans.modules.web.webkit.debugging.spi.Command) throws org.netbeans.modules.web.webkit.debugging.api.TransportStateException

