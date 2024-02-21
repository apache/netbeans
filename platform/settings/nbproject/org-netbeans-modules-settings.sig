#Signature file v4.1
#Version 1.72

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

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface !annotation org.netbeans.api.settings.ConvertAsJavaBean
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean subclasses()

CLSS public abstract interface !annotation org.netbeans.api.settings.ConvertAsProperties
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
fld public final static java.lang.String IGNORE_ALL_CHANGES = "all"
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean autostore()
meth public abstract !hasdefault java.lang.String[] ignoreChanges()
meth public abstract java.lang.String dtd()

CLSS public abstract interface !annotation org.netbeans.api.settings.FactoryMethod
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS abstract interface org.netbeans.api.settings.package-info

CLSS public abstract org.netbeans.spi.settings.Convertor
cons public init()
meth protected static org.openide.util.Lookup findContext(java.io.Reader)
meth protected static org.openide.util.Lookup findContext(java.io.Writer)
meth public abstract java.lang.Object read(java.io.Reader) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void registerSaver(java.lang.Object,org.netbeans.spi.settings.Saver)
meth public abstract void unregisterSaver(java.lang.Object,org.netbeans.spi.settings.Saver)
meth public abstract void write(java.io.Writer,java.lang.Object) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.spi.settings.DOMConvertor
cons protected init(java.lang.String,java.lang.String,java.lang.String)
meth protected abstract java.lang.Object readElement(org.w3c.dom.Element) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected abstract void writeElement(org.w3c.dom.Document,org.w3c.dom.Element,java.lang.Object) throws java.io.IOException
meth protected final static java.lang.Object delegateRead(org.w3c.dom.Element) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected final static org.w3c.dom.Element delegateWrite(org.w3c.dom.Document,java.lang.Object) throws java.io.IOException
meth protected static org.openide.util.Lookup findContext(org.w3c.dom.Document)
meth public final java.lang.Object read(java.io.Reader) throws java.io.IOException,java.lang.ClassNotFoundException
meth public final void write(java.io.Writer,java.lang.Object) throws java.io.IOException
supr org.netbeans.spi.settings.Convertor
hfds ATTR_ID,ATTR_IDREF,ATTR_PUBLIC_ID,ELM_DELEGATE,ctxCache,publicID,refsCache,rootElement,systemID
hcls CacheRec

CLSS public abstract interface org.netbeans.spi.settings.Saver
meth public abstract void markDirty()
meth public abstract void requestSave() throws java.io.IOException

