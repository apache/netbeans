#Signature file v4.1
#Version 6.39

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

CLSS public abstract interface java.util.Enumeration<%0 extends java.lang.Object>
meth public abstract boolean hasMoreElements()
meth public abstract {java.util.Enumeration%0} nextElement()

CLSS public abstract org.openide.util.enum.AlterEnumeration
cons public init(java.util.Enumeration)
intf java.util.Enumeration
meth protected abstract java.lang.Object alter(java.lang.Object)
meth public boolean hasMoreElements()
meth public java.lang.Object nextElement()
supr java.lang.Object
hfds en

CLSS public org.openide.util.enum.ArrayEnumeration
cons public init(java.lang.Object[])
intf java.util.Enumeration
meth public boolean hasMoreElements()
meth public java.lang.Object nextElement()
supr java.lang.Object
hfds array,index

CLSS public final org.openide.util.enum.EmptyEnumeration
cons public init()
fld public final static org.openide.util.enum.EmptyEnumeration EMPTY
intf java.util.Enumeration
meth public boolean hasMoreElements()
meth public java.lang.Object nextElement()
supr java.lang.Object

CLSS public org.openide.util.enum.FilterEnumeration
cons public init(java.util.Enumeration)
intf java.util.Enumeration
meth protected boolean accept(java.lang.Object)
meth public boolean hasMoreElements()
meth public java.lang.Object nextElement()
supr java.lang.Object
hfds EMPTY,en,next

CLSS public org.openide.util.enum.QueueEnumeration
cons public init()
intf java.util.Enumeration
meth protected void process(java.lang.Object)
meth public boolean hasMoreElements()
meth public java.lang.Object nextElement()
meth public void put(java.lang.Object)
meth public void put(java.lang.Object[])
supr java.lang.Object
hfds last,next
hcls ListItem

CLSS public org.openide.util.enum.RemoveDuplicatesEnumeration
cons public init(java.util.Enumeration)
meth protected boolean accept(java.lang.Object)
supr org.openide.util.enum.FilterEnumeration
hfds all

CLSS public org.openide.util.enum.SequenceEnumeration
cons public init(java.util.Enumeration)
cons public init(java.util.Enumeration,java.util.Enumeration)
intf java.util.Enumeration
meth public boolean hasMoreElements()
meth public java.lang.Object nextElement()
supr java.lang.Object
hfds checked,current,en

CLSS public org.openide.util.enum.SingletonEnumeration
cons public init(java.lang.Object)
intf java.util.Enumeration
meth public boolean hasMoreElements()
meth public java.lang.Object nextElement()
supr java.lang.Object
hfds object

