#Signature file v4.1
#Version 1.49

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

CLSS public final org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils
fld public final static java.lang.String DEFAULT_JAVAFX_PLATFORM = "Default_JavaFX_Platform"
fld public final static java.lang.String DEFAULT_PLATFORM = "default_platform"
fld public final static java.lang.String JAVAFX_CLASSPATH_EXTENSION = "javafx.classpath.extension"
fld public final static java.lang.String PLATFORM_ANT_NAME = "platform.ant.name"
meth public static boolean isJavaFXEnabled(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static boolean isThereAnyJavaFXPlatform()
meth public static java.lang.String getClassPathExtensionProperty()
meth public static java.lang.String getJavaFXRuntimeJar(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getPlatformAntName(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.java.platform.JavaPlatform findJavaFXPlatform()
meth public static org.netbeans.api.java.platform.JavaPlatform findJavaPlatform(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds JAVADOC_ONLINE_URL,JFXRT_JAR_NAME,JFXRT_RELATIVE_LOCATIONS,JRE_RELATIVE_LOCATIONS

CLSS public org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion
innr public final static !enum Support
meth public boolean isIncludedOnClassPath()
meth public boolean isSupported()
meth public java.util.List<java.lang.String> getExtensionArtifactPaths()
meth public static java.lang.String getPlatformHomeProperty(org.netbeans.api.java.platform.JavaPlatform)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Set<java.lang.String> getProjectClassPathExtension(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion forPlatform(org.netbeans.api.java.platform.JavaPlatform)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds PROP_JAVA_HOME,PROP_PLATFORM_ANT_NAME,SPEC_J2SE,artifacts,support

CLSS public final static !enum org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion$Support
 outer org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion
fld public final static org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion$Support INCLUDED
fld public final static org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion$Support MISSING
fld public final static org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion$Support PRESENT
meth public static org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion$Support valueOf(java.lang.String)
meth public static org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion$Support[] values()
supr java.lang.Enum<org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion$Support>

