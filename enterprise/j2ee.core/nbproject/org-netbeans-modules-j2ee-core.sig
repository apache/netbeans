#Signature file v4.1
#Version 1.48

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

CLSS public final !enum org.netbeans.api.j2ee.core.Profile
fld public final static java.util.Comparator<org.netbeans.api.j2ee.core.Profile> UI_COMPARATOR
fld public final static org.netbeans.api.j2ee.core.Profile J2EE_13
fld public final static org.netbeans.api.j2ee.core.Profile J2EE_14
fld public final static org.netbeans.api.j2ee.core.Profile JAKARTA_EE_10_FULL
fld public final static org.netbeans.api.j2ee.core.Profile JAKARTA_EE_10_WEB
fld public final static org.netbeans.api.j2ee.core.Profile JAKARTA_EE_8_FULL
fld public final static org.netbeans.api.j2ee.core.Profile JAKARTA_EE_8_WEB
fld public final static org.netbeans.api.j2ee.core.Profile JAKARTA_EE_9_1_FULL
fld public final static org.netbeans.api.j2ee.core.Profile JAKARTA_EE_9_1_WEB
fld public final static org.netbeans.api.j2ee.core.Profile JAKARTA_EE_9_FULL
fld public final static org.netbeans.api.j2ee.core.Profile JAKARTA_EE_9_WEB
fld public final static org.netbeans.api.j2ee.core.Profile JAVA_EE_5
fld public final static org.netbeans.api.j2ee.core.Profile JAVA_EE_6_FULL
fld public final static org.netbeans.api.j2ee.core.Profile JAVA_EE_6_WEB
fld public final static org.netbeans.api.j2ee.core.Profile JAVA_EE_7_FULL
fld public final static org.netbeans.api.j2ee.core.Profile JAVA_EE_7_WEB
fld public final static org.netbeans.api.j2ee.core.Profile JAVA_EE_8_FULL
fld public final static org.netbeans.api.j2ee.core.Profile JAVA_EE_8_WEB
meth public boolean isAtLeast(org.netbeans.api.j2ee.core.Profile)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toPropertiesString()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public static org.netbeans.api.j2ee.core.Profile fromPropertiesString(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.j2ee.core.Profile valueOf(java.lang.String)
meth public static org.netbeans.api.j2ee.core.Profile[] values()
supr java.lang.Enum<org.netbeans.api.j2ee.core.Profile>
hfds propertiesString

