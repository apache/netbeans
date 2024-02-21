#Signature file v4.1
#Version 1.24

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

CLSS public final org.netbeans.spi.knockout.Bindings
meth public final org.netbeans.spi.knockout.Bindings booleanProperty(java.lang.String,boolean)
meth public final org.netbeans.spi.knockout.Bindings doubleProperty(java.lang.String,boolean)
meth public final org.netbeans.spi.knockout.Bindings function(java.lang.String)
meth public final org.netbeans.spi.knockout.Bindings intProperty(java.lang.String,boolean)
meth public final org.netbeans.spi.knockout.Bindings modelProperty(java.lang.String,org.netbeans.spi.knockout.Bindings,boolean)
meth public final org.netbeans.spi.knockout.Bindings stringProperty(java.lang.String,boolean)
meth public static java.lang.String findBindings(org.openide.filesystems.FileObject,int)
meth public static org.netbeans.spi.knockout.Bindings create(java.lang.String)
supr java.lang.Object
hfds name,props,subBindings

CLSS public abstract interface org.netbeans.spi.knockout.BindingsProvider
innr public final static Response
meth public abstract org.netbeans.spi.knockout.BindingsProvider$Response findBindings(org.openide.filesystems.FileObject)

CLSS public final static org.netbeans.spi.knockout.BindingsProvider$Response
 outer org.netbeans.spi.knockout.BindingsProvider
meth public org.netbeans.spi.knockout.BindingsProvider$Response create(org.netbeans.spi.knockout.Bindings,java.lang.String)
meth public static org.netbeans.spi.knockout.BindingsProvider$Response create(org.netbeans.spi.knockout.Bindings)
supr java.lang.Object
hfds bindings,targetId

