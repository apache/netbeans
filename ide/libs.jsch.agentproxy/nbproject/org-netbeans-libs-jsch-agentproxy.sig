#Signature file v4.1
#Version 1.7

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

CLSS public org.netbeans.libs.jsch.agentproxy.ConnectorFactory
innr public final static !enum ConnectorKind
meth public com.jcraft.jsch.AgentConnector createConnector(org.netbeans.libs.jsch.agentproxy.ConnectorFactory$ConnectorKind)
meth public static org.netbeans.libs.jsch.agentproxy.ConnectorFactory getInstance()
supr java.lang.Object
hfds LOG,instance

CLSS public final static !enum org.netbeans.libs.jsch.agentproxy.ConnectorFactory$ConnectorKind
 outer org.netbeans.libs.jsch.agentproxy.ConnectorFactory
fld public final static org.netbeans.libs.jsch.agentproxy.ConnectorFactory$ConnectorKind ANY
fld public final static org.netbeans.libs.jsch.agentproxy.ConnectorFactory$ConnectorKind PAGEANT
fld public final static org.netbeans.libs.jsch.agentproxy.ConnectorFactory$ConnectorKind SSH_AGENT
meth public static org.netbeans.libs.jsch.agentproxy.ConnectorFactory$ConnectorKind valueOf(java.lang.String)
meth public static org.netbeans.libs.jsch.agentproxy.ConnectorFactory$ConnectorKind[] values()
supr java.lang.Enum<org.netbeans.libs.jsch.agentproxy.ConnectorFactory$ConnectorKind>

