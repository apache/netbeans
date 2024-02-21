#Signature file v4.1
#Version 3.27.0

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2
intf org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase
meth public abstract java.lang.String getIconResource(int)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogDescriptorBase
fld public final static java.lang.String PROP_CATALOG_DESC = "ca-desc"
fld public final static java.lang.String PROP_CATALOG_ICON = "ca-icon"
fld public final static java.lang.String PROP_CATALOG_NAME = "ca-name"
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getShortDescription()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogListener
intf java.util.EventListener
meth public abstract void notifyInvalidate()
meth public abstract void notifyNew(java.lang.String)
meth public abstract void notifyRemoved(java.lang.String)
meth public abstract void notifyUpdate(java.lang.String)

CLSS public org.netbeans.modules.xml.catalog.spi.CatalogListenerAdapter
cons public init()
intf org.netbeans.modules.xml.catalog.spi.CatalogListener
meth public void notifyInvalidate()
meth public void notifyNew(java.lang.String)
meth public void notifyRemoved(java.lang.String)
meth public void notifyUpdate(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogProvider
meth public abstract java.lang.Class provideClass() throws java.io.IOException,java.lang.ClassNotFoundException

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogReader
meth public abstract java.lang.String getSystemID(java.lang.String)
meth public abstract java.lang.String resolvePublic(java.lang.String)
meth public abstract java.lang.String resolveURI(java.lang.String)
meth public abstract java.util.Iterator getPublicIDs()
meth public abstract void addCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)
meth public abstract void refresh()
meth public abstract void removeCatalogListener(org.netbeans.modules.xml.catalog.spi.CatalogListener)

CLSS public abstract interface org.netbeans.modules.xml.catalog.spi.CatalogWriter
meth public abstract void registerCatalogEntry(java.lang.String,java.lang.String)

CLSS public final org.netbeans.modules.xml.catalog.spi.ProvidersRegistry
cons public init()
meth public final static java.util.Iterator getProviderClasses(java.lang.Class[])
supr java.lang.Object

