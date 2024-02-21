#Signature file v4.1
#Version 2.44

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

CLSS public abstract org.netbeans.modules.profiler.attach.AttachWizard
cons public init()
meth public abstract boolean configured(org.netbeans.lib.profiler.common.AttachSettings)
meth public abstract org.netbeans.lib.profiler.common.AttachSettings configure(org.netbeans.lib.profiler.common.AttachSettings,boolean)
meth public static boolean isAvailable()
meth public static org.netbeans.modules.profiler.attach.AttachWizard getDefault()
supr java.lang.Object

CLSS public final org.netbeans.modules.profiler.attach.providers.RemotePackExporter
meth public boolean isAvailable()
meth public java.lang.String export(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public static org.netbeans.modules.profiler.attach.providers.RemotePackExporter getInstance()
meth public void export(java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds impl
hcls Singleton

CLSS public org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum
fld public final static org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum JDK110_BEYOND
fld public final static org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum JDK5
fld public final static org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum JDK6
fld public final static org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum JDK7
fld public final static org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum JDK8
fld public final static org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum JDK9
fld public final static org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum JDK_CVM
meth public boolean equals(java.lang.Object)
meth public java.lang.String toString()
meth public static java.util.Iterator iterator()
supr java.lang.Object
hfds jvmIndex,jvmNames

CLSS public abstract org.netbeans.modules.profiler.attach.spi.AbstractRemotePackExporter
cons public init()
meth protected final java.lang.String getJVMShort(java.lang.String)
meth protected final java.lang.String getPlatformShort(java.lang.String)
meth public abstract java.lang.String export(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String getRemotePackPath(java.lang.String,java.lang.String)
supr java.lang.Object
hfds jdkMapper,scriptMapper

CLSS public abstract org.netbeans.modules.profiler.attach.spi.AttachStepsProvider
cons public init()
meth public java.lang.String getSteps(org.netbeans.lib.profiler.common.AttachSettings)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void handleAction(java.lang.String,org.netbeans.lib.profiler.common.AttachSettings)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public org.netbeans.modules.profiler.attach.steps.BasicAttachStepsProvider
cons public init()
fld protected final static java.lang.String LINK_32ARCH = "file:/32arch"
fld protected final static java.lang.String LINK_64ARCH = "file:/64arch"
fld protected final static java.lang.String LINK_CLIPBOARD = "file:/clipboard"
fld protected final static java.lang.String LINK_JDK5 = "file:/jdk5"
fld protected final static java.lang.String LINK_JDK6UP = "file:/jdk6up"
fld protected final static java.lang.String LINK_REMOTEPACK = "file:/remotepack"
fld protected java.lang.String currentARCH
fld protected java.lang.String currentJDK
meth protected final void fireChange(javax.swing.event.ChangeEvent)
meth protected java.lang.String localDirectSteps(org.netbeans.lib.profiler.common.AttachSettings)
meth protected java.lang.String localDynamicSteps(org.netbeans.lib.profiler.common.AttachSettings)
meth protected java.lang.String parameters(org.netbeans.lib.profiler.common.AttachSettings)
meth protected java.lang.String remoteDirectSteps(org.netbeans.lib.profiler.common.AttachSettings)
meth protected static boolean isARMJVM(org.netbeans.lib.profiler.common.AttachSettings)
meth protected static boolean isCVMJVM(org.netbeans.lib.profiler.common.AttachSettings)
meth protected static java.lang.String exportRemotePack(java.lang.String,org.netbeans.lib.profiler.common.AttachSettings,java.lang.String) throws java.io.IOException
meth protected static java.lang.String getCorrectJavaMsg(java.lang.String,java.lang.String)
meth protected static java.lang.String getOS(org.netbeans.lib.profiler.common.AttachSettings,java.lang.String)
meth protected static java.lang.String getPlatform(org.netbeans.lib.profiler.common.AttachSettings,java.lang.String)
meth protected void copyParameters(org.netbeans.lib.profiler.common.AttachSettings)
meth protected void createRemotePack(org.netbeans.lib.profiler.common.AttachSettings)
meth protected void switchTo32ARCH()
meth protected void switchTo64ARCH()
meth protected void switchToJDK5()
meth protected void switchToJDK6Up()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public java.lang.String getSteps(org.netbeans.lib.profiler.common.AttachSettings)
meth public void handleAction(java.lang.String,org.netbeans.lib.profiler.common.AttachSettings)
supr org.netbeans.modules.profiler.attach.spi.AttachStepsProvider
hfds exportRunning,listeners

