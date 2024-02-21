#Signature file v4.1
#Version 7.49

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

CLSS public final org.netbeans.api.print.PrintManager
fld public final static java.lang.String PRINT_NAME = "print.name"
fld public final static java.lang.String PRINT_ORDER = "print.order"
fld public final static java.lang.String PRINT_PRINTABLE = "print.printable"
fld public final static java.lang.String PRINT_SIZE = "print.size"
meth public static javax.swing.Action printAction(javax.swing.JComponent)
meth public static javax.swing.Action printAction(org.netbeans.spi.print.PrintProvider[])
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.print.PrintPage
meth public abstract void print(java.awt.Graphics)

CLSS public abstract interface org.netbeans.spi.print.PrintProvider
meth public abstract java.lang.String getName()
meth public abstract java.util.Date lastModified()
meth public abstract org.netbeans.spi.print.PrintPage[][] getPages(int,int,double)

