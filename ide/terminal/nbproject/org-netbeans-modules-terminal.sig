#Signature file v4.1
#Version 1.50

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

CLSS public abstract org.netbeans.modules.terminal.api.IOConnect
cons public init()
fld public final static java.lang.String PROP_CONNECTED = "IOConnect.PROP_CONNECTED"
meth protected abstract boolean isConnected()
meth protected abstract void disconnectAll(java.lang.Runnable)
meth public static boolean isConnected(org.openide.windows.InputOutput)
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static void disconnectAll(org.openide.windows.InputOutput,java.lang.Runnable)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.terminal.api.IOEmulation
cons public init()
meth protected abstract boolean isDisciplined()
meth protected abstract java.lang.String getEmulation()
meth protected abstract void setDisciplined()
meth public static boolean isDisciplined(org.openide.windows.InputOutput)
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static java.lang.String getEmulation(org.openide.windows.InputOutput)
meth public static void setDisciplined(org.openide.windows.InputOutput)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.terminal.api.IONotifier
cons public init()
meth protected abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth protected abstract void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth protected abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth protected abstract void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static void addPropertyChangeListener(org.openide.windows.InputOutput,java.beans.PropertyChangeListener)
meth public static void addVetoableChangeListener(org.openide.windows.InputOutput,java.beans.VetoableChangeListener)
meth public static void removePropertyChangeListener(org.openide.windows.InputOutput,java.beans.PropertyChangeListener)
meth public static void removeVetoableChangeListener(org.openide.windows.InputOutput,java.beans.VetoableChangeListener)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.terminal.api.IOResizable
cons public init()
fld public final static java.lang.String PROP_SIZE = "IOResizable.PROP_SIZE"
innr public final static Size
meth public static boolean isSupported(org.openide.windows.InputOutput)
supr java.lang.Object

CLSS public final static org.netbeans.modules.terminal.api.IOResizable$Size
 outer org.netbeans.modules.terminal.api.IOResizable
cons public init(java.awt.Dimension,java.awt.Dimension)
fld public final java.awt.Dimension cells
fld public final java.awt.Dimension pixels
supr java.lang.Object

CLSS public abstract org.netbeans.modules.terminal.api.IOTerm
cons public init()
meth protected abstract void connect(java.io.OutputStream,java.io.InputStream,java.io.InputStream,java.lang.String,java.lang.Runnable)
meth protected abstract void disconnect(java.lang.Runnable)
meth protected abstract void requestFocus()
meth protected abstract void setReadOnly(boolean)
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static void connect(org.openide.windows.InputOutput,java.io.OutputStream,java.io.InputStream,java.io.InputStream)
meth public static void connect(org.openide.windows.InputOutput,java.io.OutputStream,java.io.InputStream,java.io.InputStream,java.lang.String)
meth public static void connect(org.openide.windows.InputOutput,java.io.OutputStream,java.io.InputStream,java.io.InputStream,java.lang.String,java.lang.Runnable)
meth public static void disconnect(org.openide.windows.InputOutput,java.lang.Runnable)
meth public static void requestFocus(org.openide.windows.InputOutput)
meth public static void setReadOnly(org.openide.windows.InputOutput,boolean)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.terminal.test.IOTest
cons public init()
meth protected abstract boolean isQuiescent()
meth protected abstract void performCloseAction()
meth public static boolean isQuiescent(org.openide.windows.InputOutput)
meth public static boolean isSupported(org.openide.windows.InputOutput)
meth public static void performCloseAction(org.openide.windows.InputOutput)
supr java.lang.Object

