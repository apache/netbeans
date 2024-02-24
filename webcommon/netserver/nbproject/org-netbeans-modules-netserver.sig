#Signature file v4.1
#Version 1.36

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

CLSS public final org.netbeans.modules.netserver.api.ProtocolDraft
innr public final static !enum Draft
meth public boolean equals(java.lang.Object)
meth public boolean isRFC()
meth public int getVersion()
meth public int hashCode()
meth public org.netbeans.modules.netserver.api.ProtocolDraft$Draft getDraft()
meth public static org.netbeans.modules.netserver.api.ProtocolDraft getProtocol(int)
meth public static org.netbeans.modules.netserver.api.ProtocolDraft getRFC()
supr java.lang.Object
hfds draft,version

CLSS public final static !enum org.netbeans.modules.netserver.api.ProtocolDraft$Draft
 outer org.netbeans.modules.netserver.api.ProtocolDraft
fld public final static org.netbeans.modules.netserver.api.ProtocolDraft$Draft Draft75
fld public final static org.netbeans.modules.netserver.api.ProtocolDraft$Draft Draft76
meth public static org.netbeans.modules.netserver.api.ProtocolDraft$Draft valueOf(java.lang.String)
meth public static org.netbeans.modules.netserver.api.ProtocolDraft$Draft[] values()
supr java.lang.Enum<org.netbeans.modules.netserver.api.ProtocolDraft$Draft>

CLSS public final org.netbeans.modules.netserver.api.WebSocketClient
cons public init(java.net.URI,org.netbeans.modules.netserver.api.ProtocolDraft,org.netbeans.modules.netserver.api.WebSocketReadHandler) throws java.io.IOException
cons public init(java.net.URI,org.netbeans.modules.netserver.api.WebSocketReadHandler) throws java.io.IOException
meth public java.net.URI getURI()
meth public void sendMessage(java.lang.String)
meth public void start()
meth public void stop()
supr java.lang.Object
hfds RP,client

CLSS public abstract interface org.netbeans.modules.netserver.api.WebSocketReadHandler
meth public abstract void accepted(java.nio.channels.SelectionKey)
meth public abstract void closed(java.nio.channels.SelectionKey)
meth public abstract void read(java.nio.channels.SelectionKey,byte[],java.lang.Integer)

CLSS public final org.netbeans.modules.netserver.api.WebSocketServer
cons public init(java.net.SocketAddress,org.netbeans.modules.netserver.api.WebSocketReadHandler) throws java.io.IOException
meth public void sendMessage(java.nio.channels.SelectionKey,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void start()
meth public void stop()
supr java.lang.Object
hfds RP,server

