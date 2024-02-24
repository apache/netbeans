#Signature file v4.1
#Version 1.68

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

CLSS public abstract org.netbeans.lib.uihandler.BugTrackingAccessor
cons public init()
meth public abstract char[] getPassword()
meth public abstract java.lang.String getUsername()
meth public abstract void openIssue(java.lang.String)
meth public abstract void savePassword(char[])
meth public abstract void saveUsername(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.uihandler.Decorable
meth public abstract void setDisplayName(java.lang.String)
meth public abstract void setIconBaseWithExtension(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setShortDescription(java.lang.String)

CLSS public final !enum org.netbeans.lib.uihandler.InputGesture
fld public final static org.netbeans.lib.uihandler.InputGesture KEYBOARD
fld public final static org.netbeans.lib.uihandler.InputGesture MENU
fld public final static org.netbeans.lib.uihandler.InputGesture TOOLBAR
meth public static org.netbeans.lib.uihandler.InputGesture valueOf(java.lang.String)
meth public static org.netbeans.lib.uihandler.InputGesture valueOf(java.util.logging.LogRecord)
meth public static org.netbeans.lib.uihandler.InputGesture[] values()
supr java.lang.Enum<org.netbeans.lib.uihandler.InputGesture>
hfds F

CLSS public final org.netbeans.lib.uihandler.LogRecords
meth public static void decorate(java.util.logging.LogRecord,org.netbeans.lib.uihandler.Decorable)
meth public static void scan(java.io.File,java.util.logging.Handler) throws java.io.IOException
meth public static void scan(java.io.InputStream,java.util.logging.Handler) throws java.io.IOException
meth public static void write(java.io.OutputStream,java.util.logging.LogRecord) throws java.io.IOException
supr java.lang.Object
hfds FORMATTER,LOG,RECORD_ELM_END,RECORD_ELM_START
hcls FakeBundle,FakeException,HandlerDelegate,Parser

CLSS public org.netbeans.lib.uihandler.PasswdEncryption
cons public init()
fld public final static int MAX_ENCRYPTION_LENGHT = 100
meth public static byte[] decrypt(byte[],java.security.PrivateKey) throws java.io.IOException,java.security.GeneralSecurityException
meth public static byte[] encrypt(byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static byte[] encrypt(byte[],java.security.PublicKey) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.String decrypt(java.lang.String,java.security.PrivateKey) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.String encrypt(java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.String encrypt(java.lang.String,java.security.PublicKey) throws java.io.IOException,java.security.GeneralSecurityException
supr java.lang.Object
hfds delimiter

