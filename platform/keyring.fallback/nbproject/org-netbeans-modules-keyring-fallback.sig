#Signature file v4.1
#Version 1.31

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

CLSS public abstract interface org.netbeans.modules.keyring.spi.EncryptionProvider
meth public abstract boolean decryptionFailed()
meth public abstract boolean enabled()
meth public abstract byte[] encrypt(char[]) throws java.lang.Exception
meth public abstract char[] decrypt(byte[]) throws java.lang.Exception
meth public abstract java.lang.String id()
meth public abstract void encryptionChanged()
meth public abstract void encryptionChangingCallback(java.util.concurrent.Callable<java.lang.Void>)
meth public abstract void freshKeyring(boolean)

CLSS public org.netbeans.modules.keyring.utils.Utils
meth public static byte[] chars2Bytes(char[])
meth public static char[] bytes2Chars(byte[])
meth public static void goMinusR(java.util.prefs.Preferences)
supr java.lang.Object
hfds LOG

