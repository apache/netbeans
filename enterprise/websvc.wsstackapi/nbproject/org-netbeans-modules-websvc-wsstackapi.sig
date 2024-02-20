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

CLSS public final org.netbeans.modules.websvc.wsstack.api.WSStack<%0 extends java.lang.Object>
innr public abstract interface static Feature
innr public abstract interface static Tool
innr public final static !enum Source
meth public boolean isFeatureSupported(org.netbeans.modules.websvc.wsstack.api.WSStack$Feature)
meth public org.netbeans.modules.websvc.wsstack.api.WSStack$Source getSource()
meth public org.netbeans.modules.websvc.wsstack.api.WSStackVersion getVersion()
meth public org.netbeans.modules.websvc.wsstack.api.WSTool getWSTool(org.netbeans.modules.websvc.wsstack.api.WSStack$Tool)
meth public static <%0 extends java.lang.Object> org.netbeans.modules.websvc.wsstack.api.WSStack<{%%0}> findWSStack(org.openide.util.Lookup,java.lang.Class<{%%0}>)
meth public {org.netbeans.modules.websvc.wsstack.api.WSStack%0} get()
supr java.lang.Object
hfds impl,stackDescriptor,stackSource

CLSS public abstract interface static org.netbeans.modules.websvc.wsstack.api.WSStack$Feature
 outer org.netbeans.modules.websvc.wsstack.api.WSStack
meth public abstract java.lang.String getName()

CLSS public final static !enum org.netbeans.modules.websvc.wsstack.api.WSStack$Source
 outer org.netbeans.modules.websvc.wsstack.api.WSStack
fld public final static org.netbeans.modules.websvc.wsstack.api.WSStack$Source IDE
fld public final static org.netbeans.modules.websvc.wsstack.api.WSStack$Source JDK
fld public final static org.netbeans.modules.websvc.wsstack.api.WSStack$Source SERVER
meth public static org.netbeans.modules.websvc.wsstack.api.WSStack$Source valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.wsstack.api.WSStack$Source[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.wsstack.api.WSStack$Source>

CLSS public abstract interface static org.netbeans.modules.websvc.wsstack.api.WSStack$Tool
 outer org.netbeans.modules.websvc.wsstack.api.WSStack
meth public abstract java.lang.String getName()

CLSS public final org.netbeans.modules.websvc.wsstack.api.WSStackVersion
intf java.lang.Comparable<org.netbeans.modules.websvc.wsstack.api.WSStackVersion>
meth public boolean equals(java.lang.Object)
meth public int compareTo(org.netbeans.modules.websvc.wsstack.api.WSStackVersion)
meth public int getMajor()
meth public int getMicro()
meth public int getMinor()
meth public int getUpdate()
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.netbeans.modules.websvc.wsstack.api.WSStackVersion valueOf(int,int,int,int)
supr java.lang.Object
hfds major,micro,minor,update

CLSS public final org.netbeans.modules.websvc.wsstack.api.WSTool
meth public java.lang.String getName()
meth public java.net.URL[] getLibraries()
supr java.lang.Object
hfds spi

CLSS public final org.netbeans.modules.websvc.wsstack.spi.WSStackFactory
cons public init()
meth public static <%0 extends java.lang.Object> org.netbeans.modules.websvc.wsstack.api.WSStack<{%%0}> createWSStack(java.lang.Class<{%%0}>,org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation<{%%0}>,org.netbeans.modules.websvc.wsstack.api.WSStack$Source)
meth public static org.netbeans.modules.websvc.wsstack.api.WSStackVersion createWSStackVersion(java.lang.String)
meth public static org.netbeans.modules.websvc.wsstack.api.WSTool createWSTool(org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation<%0 extends java.lang.Object>
meth public abstract boolean isFeatureSupported(org.netbeans.modules.websvc.wsstack.api.WSStack$Feature)
meth public abstract org.netbeans.modules.websvc.wsstack.api.WSStackVersion getVersion()
meth public abstract org.netbeans.modules.websvc.wsstack.api.WSTool getWSTool(org.netbeans.modules.websvc.wsstack.api.WSStack$Tool)
meth public abstract {org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation%0} get()

CLSS public abstract interface org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation
meth public abstract java.lang.String getName()
meth public abstract java.net.URL[] getLibraries()

