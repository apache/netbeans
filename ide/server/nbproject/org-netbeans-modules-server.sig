#Signature file v4.1
#Version 1.56

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

CLSS public final org.netbeans.api.server.CommonServerUIs
meth public static org.netbeans.api.server.ServerInstance showAddServerInstanceWizard()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static void showCloudCustomizer(org.netbeans.api.server.ServerInstance)
meth public static void showCustomizer(org.netbeans.api.server.ServerInstance)
supr java.lang.Object

CLSS public final org.netbeans.api.server.ServerInstance
intf org.openide.util.Lookup$Provider
meth public boolean isRemovable()
meth public java.lang.String getDisplayName()
meth public java.lang.String getServerDisplayName()
meth public javax.swing.JComponent getCustomizer()
meth public org.openide.nodes.Node getBasicNode()
meth public org.openide.nodes.Node getFullNode()
meth public org.openide.util.Lookup getLookup()
meth public void remove()
supr java.lang.Object
hfds delegate

CLSS abstract interface org.netbeans.api.server.package-info

CLSS public abstract org.netbeans.api.server.properties.InstanceProperties
cons public init(java.lang.String)
meth public abstract boolean getBoolean(java.lang.String,boolean)
meth public abstract double getDouble(java.lang.String,double)
meth public abstract float getFloat(java.lang.String,float)
meth public abstract int getInt(java.lang.String,int)
meth public abstract java.lang.String getString(java.lang.String,java.lang.String)
meth public abstract long getLong(java.lang.String,long)
meth public abstract void putBoolean(java.lang.String,boolean)
meth public abstract void putDouble(java.lang.String,double)
meth public abstract void putFloat(java.lang.String,float)
meth public abstract void putInt(java.lang.String,int)
meth public abstract void putLong(java.lang.String,long)
meth public abstract void putString(java.lang.String,java.lang.String)
meth public abstract void remove()
meth public abstract void removeKey(java.lang.String)
meth public final java.lang.String getId()
supr java.lang.Object
hfds id

CLSS public final org.netbeans.api.server.properties.InstancePropertiesManager
meth public java.util.List<org.netbeans.api.server.properties.InstanceProperties> getProperties(java.lang.String)
meth public org.netbeans.api.server.properties.InstanceProperties createProperties(java.lang.String)
meth public static org.netbeans.api.server.properties.InstancePropertiesManager getInstance()
supr java.lang.Object
hfds LOGGER,cache,manager,random
hcls DefaultInstanceProperties

CLSS abstract interface org.netbeans.api.server.properties.package-info

CLSS public final org.netbeans.spi.server.ServerInstanceFactory
innr public abstract static Accessor
meth public static org.netbeans.api.server.ServerInstance createServerInstance(org.netbeans.spi.server.ServerInstanceImplementation)
supr java.lang.Object

CLSS public abstract static org.netbeans.spi.server.ServerInstanceFactory$Accessor
 outer org.netbeans.spi.server.ServerInstanceFactory
cons public init()
fld public static org.netbeans.spi.server.ServerInstanceFactory$Accessor DEFAULT
meth public abstract org.netbeans.api.server.ServerInstance createServerInstance(org.netbeans.spi.server.ServerInstanceImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.server.ServerInstanceImplementation
meth public abstract boolean isRemovable()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getServerDisplayName()
meth public abstract javax.swing.JComponent getCustomizer()
meth public abstract org.openide.nodes.Node getBasicNode()
meth public abstract org.openide.nodes.Node getFullNode()
meth public abstract void remove()

CLSS public abstract interface org.netbeans.spi.server.ServerInstanceProvider
meth public abstract java.util.List<org.netbeans.api.server.ServerInstance> getInstances()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.netbeans.spi.server.ServerWizardProvider
meth public abstract java.lang.String getDisplayName()
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator getInstantiatingIterator()

CLSS abstract interface org.netbeans.spi.server.package-info

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

