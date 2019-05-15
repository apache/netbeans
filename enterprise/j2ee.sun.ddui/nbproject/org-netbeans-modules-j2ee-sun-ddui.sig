#Signature file v4.1
#Version 1.40.1

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public abstract interface javax.enterprise.deploy.spi.DeploymentConfiguration
meth public abstract javax.enterprise.deploy.model.DeployableObject getDeployableObject()
meth public abstract javax.enterprise.deploy.spi.DConfigBeanRoot getDConfigBeanRoot(javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract javax.enterprise.deploy.spi.DConfigBeanRoot restoreDConfigBean(java.io.InputStream,javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void removeDConfigBean(javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.BeanNotFoundException
meth public abstract void restore(java.io.InputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void save(java.io.OutputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void saveDConfigBean(java.io.OutputStream,javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException

CLSS public abstract javax.swing.table.AbstractTableModel
cons public init()
fld protected javax.swing.event.EventListenerList listenerList
intf java.io.Serializable
intf javax.swing.table.TableModel
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public boolean isCellEditable(int,int)
meth public int findColumn(java.lang.String)
meth public java.lang.Class<?> getColumnClass(int)
meth public java.lang.String getColumnName(int)
meth public javax.swing.event.TableModelListener[] getTableModelListeners()
meth public void addTableModelListener(javax.swing.event.TableModelListener)
meth public void fireTableCellUpdated(int,int)
meth public void fireTableChanged(javax.swing.event.TableModelEvent)
meth public void fireTableDataChanged()
meth public void fireTableRowsDeleted(int,int)
meth public void fireTableRowsInserted(int,int)
meth public void fireTableRowsUpdated(int,int)
meth public void fireTableStructureChanged()
meth public void removeTableModelListener(javax.swing.event.TableModelListener)
meth public void setValueAt(java.lang.Object,int,int)
supr java.lang.Object

CLSS public abstract interface javax.swing.table.TableModel
meth public abstract boolean isCellEditable(int,int)
meth public abstract int getColumnCount()
meth public abstract int getRowCount()
meth public abstract java.lang.Class<?> getColumnClass(int)
meth public abstract java.lang.Object getValueAt(int,int)
meth public abstract java.lang.String getColumnName(int)
meth public abstract void addTableModelListener(javax.swing.event.TableModelListener)
meth public abstract void removeTableModelListener(javax.swing.event.TableModelListener)
meth public abstract void setValueAt(java.lang.Object,int,int)

CLSS public abstract org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration
cons protected init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons protected init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init()
 anno 0 java.lang.Deprecated()
fld protected final java.io.File primarySunDD
fld protected final java.io.File secondarySunDD
fld protected final org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper moduleHelper
fld protected final org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule module
fld protected org.netbeans.modules.glassfish.eecommon.api.config.DescriptorListener descriptorListener
fld protected org.netbeans.modules.glassfish.tooling.data.GlassFishVersion version
innr public final static !enum ChangeOperation
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration
meth protected <%0 extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean> {%%0} findNamedBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,java.lang.String,java.lang.String,java.lang.String)
meth protected org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider getProvider(java.io.File)
meth protected org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getInstalledAppServerVersion(java.io.File)
meth protected org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getTargetAppServerVersion()
meth protected org.netbeans.modules.j2ee.sun.dd.api.RootInterface getSunDDRoot(boolean) throws java.io.IOException
meth protected org.openide.filesystems.FileObject getSunDD(java.io.File,boolean) throws java.io.IOException
meth protected void createDefaultSunDD(java.io.File) throws java.io.IOException
meth protected void displayError(java.lang.Exception,java.lang.String)
meth protected void handleEventRelatedException(java.lang.Exception)
meth protected void handleEventRelatedIOException(java.io.IOException)
meth public <%0 extends java.lang.Object> org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<{%%0}> getMetadataModel(java.lang.Class<{%%0}>)
meth public abstract boolean supportsCreateDatasource()
meth public abstract boolean supportsCreateMessageDestination()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public final org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD()
meth public final org.netbeans.modules.j2ee.dd.api.webservices.Webservices getWebServicesRootDD()
meth public final static org.openide.util.Pair<java.io.File,java.lang.Boolean> getExistingResourceFile(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion)
meth public final static org.openide.util.Pair<java.io.File,java.lang.Boolean> getNewResourceFile(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion)
meth public java.lang.String findDatasourceJndiName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findDatasourceJndiNameForEjb(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findJndiNameForEjb(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String findMessageDestinationName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public java.lang.String getContextRoot() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public org.netbeans.modules.glassfish.eecommon.api.config.J2EEBaseVersion getJ2eeVersion()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
meth public org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getAppServerVersion()
meth public org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getMaxASVersion()
meth public org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getMinASVersion()
meth public org.netbeans.modules.j2ee.sun.dd.api.RootInterface getSunDDRoot(java.io.File,boolean) throws java.io.IOException
meth public static org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration getConfiguration(java.io.File)
meth public static void addConfiguration(java.io.File,org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration)
meth public static void removeConfiguration(java.io.File)
meth public void bindDatasourceReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindDatasourceReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindEjbReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindEjbReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMdbToMessageDestination(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMessageDestinationReference(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void bindMessageDestinationReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void dispose()
meth public void saveConfiguration(java.io.OutputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void setAppServerVersion(org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth public void setContextRoot(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
supr java.lang.Object
hfds LOGGER,RESOURCE_FILES,RESOURCE_FILES_SUFFIX,RP,appServerVersion,configurationMap,configurationMonitor,defaultcr,deferredAppServerChange,maxASVersion,minASVersion,sunServerIds

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration
meth public abstract java.lang.String getContextRoot() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void setContextRoot(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration
meth public abstract boolean supportsCreateDatasource()
meth public abstract java.lang.String findDatasourceJndiName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.lang.String findDatasourceJndiNameForEjb(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public abstract void bindDatasourceReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindDatasourceReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration
meth public abstract java.lang.String findJndiNameForEjb(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindEjbReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindEjbReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration
meth public abstract boolean supportsCreateMessageDestination()
meth public abstract java.lang.String findMessageDestinationName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMdbToMessageDestination(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMessageDestinationReference(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMessageDestinationReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public org.netbeans.modules.j2ee.sun.share.CharsetMapping
cons public init(java.nio.charset.Charset)
cons public init(java.nio.charset.Charset,boolean)
cons public init(java.nio.charset.Charset,java.lang.String)
fld public final static java.lang.Integer CHARSET_ALIAS_ASIDE
fld public final static java.lang.Integer CHARSET_ALIAS_SELECTION
fld public final static java.lang.Integer CHARSET_CANONICAL
fld public final static java.lang.String CHARSET_DISPLAY_TYPE = "CharsetDisplayType"
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getAlias()
meth public java.lang.String toString()
meth public java.nio.charset.Charset getCharset()
meth public static java.lang.Integer getDisplayOption()
meth public static java.util.SortedMap getSortedAvailableCharsetMappings()
meth public static org.netbeans.modules.j2ee.sun.share.CharsetMapping getCharsetMapping(java.lang.String)
meth public static org.netbeans.modules.j2ee.sun.share.CharsetMapping getCharsetMapping(java.nio.charset.Charset)
meth public static void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void setDisplayOption(java.lang.Integer)
meth public void updateDisplayText()
supr java.lang.Object
hfds bundle,charset,chosenAlias,displayOption,displayText,propSupport,showAliases,sortedAliasCharsetMappings,sortedCanonicalCharsetMappings,textOutOfDate,useAliases

CLSS public abstract interface org.netbeans.modules.j2ee.sun.share.Constants
fld public final static java.lang.String EMPTY_STRING = ""
fld public final static java.lang.String USER_DATA_CHANGED = "UserDataChanged"
fld public final static java.util.logging.Logger jsr88Logger

CLSS public final org.netbeans.modules.j2ee.sun.share.PrincipalNameMapping
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getClassName()
meth public java.lang.String getPrincipalName()
meth public java.lang.String toString()
supr java.lang.Object
hfds className,principalName

CLSS public org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel
fld public final static java.lang.String DUPLICATE_GROUP
fld public final static java.lang.String DUPLICATE_PRINCIPAL
fld public final static java.lang.String[] GROUP_COLUMN_NAMES
fld public final static java.lang.String[] PRINCIPAL_COLUMN_NAMES
meth public boolean contains(java.lang.Object)
meth public boolean removeElement(java.lang.Object)
meth public boolean replaceElement(java.lang.Object,java.lang.Object)
meth public int getColumnCount()
meth public int getRowCount()
meth public java.lang.Object getRow(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.lang.String getDuplicateErrorMessage(java.lang.String)
meth public static org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel getGroupMasterModel()
meth public static org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel getPrincipalMasterModel()
meth public void addElement(java.lang.Object)
meth public void removeElementAt(int)
meth public void removeElements(int[])
supr javax.swing.table.AbstractTableModel
hfds columnCount,columnNames,duplicateErrorPattern,groupMaster,masterList,principalMaster

CLSS public org.netbeans.modules.j2ee.sun.share.configbean.CmpListenerSupport
cons public init()
supr java.lang.Object
hcls CmpEntityVisitor,CmpFieldNameVisitor,CmpFieldVisitor,CmpNameVisitorFactory,EntityVisitor

CLSS public org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration
cons public init(javax.enterprise.deploy.model.DeployableObject)
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,java.lang.String,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface,org.netbeans.modules.glassfish.tooling.data.GlassFishVersion) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
intf javax.enterprise.deploy.spi.DeploymentConfiguration
meth public boolean supportsCreateDatasource()
meth public boolean supportsCreateMessageDestination()
meth public java.lang.String getDeploymentModuleName()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public javax.enterprise.deploy.model.DeployableObject getDeployableObject()
meth public javax.enterprise.deploy.spi.DConfigBeanRoot getDConfigBeanRoot(javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public javax.enterprise.deploy.spi.DConfigBeanRoot restoreDConfigBean(java.io.InputStream,javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void removeDConfigBean(javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.BeanNotFoundException
meth public void restore(java.io.InputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public void save(java.io.OutputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public void saveDConfigBean(java.io.OutputStream,javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public void setCMPResource(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public void setDeploymentModuleName(java.lang.String)
meth public void setMappingInfo(org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping[]) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
supr org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration
hfds deploymentModuleName,resourceProcessor,sdmi

CLSS public org.netbeans.modules.j2ee.sun.share.configbean.Utils
intf org.netbeans.modules.j2ee.sun.share.Constants
meth public static boolean booleanValueOf(java.lang.String)
meth public static boolean containsWhitespace(java.lang.String)
meth public static boolean hasTrailingSlash(java.lang.String)
meth public static boolean interpretCheckboxState(java.awt.event.ItemEvent)
meth public static boolean isJavaClass(java.lang.String)
meth public static boolean isJavaIdentifier(java.lang.String)
meth public static boolean isJavaPackage(java.lang.String)
meth public static boolean notEmpty(java.lang.String)
meth public static boolean strEmpty(java.lang.String)
meth public static boolean strEquals(java.lang.String,java.lang.String)
meth public static boolean strEquivalent(java.lang.String,java.lang.String)
meth public static int strCompareTo(java.lang.String,java.lang.String)
meth public static java.lang.String encodeUrlField(java.lang.String)
meth public static java.net.URL getResourceURL(java.lang.String,java.lang.Class)
meth public static void invokeHelp(java.lang.String)
meth public static void invokeHelp(org.openide.util.HelpCtx)
supr java.lang.Object
hfds booleanStrings

CLSS public org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan
cons public init() throws org.netbeans.modules.schema2beans.Schema2BeansException
cons public init(int)
cons public init(org.w3c.dom.Node,int) throws org.netbeans.modules.schema2beans.Schema2BeansException
fld public final static java.lang.String FILE_ENTRY = "FileEntry"
meth protected void initFromNode(org.w3c.dom.Node,int) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth protected void initOptions(int)
meth public int addFileEntry(org.netbeans.modules.j2ee.sun.share.plan.FileEntry) throws java.beans.PropertyVetoException
meth public int removeFileEntry(org.netbeans.modules.j2ee.sun.share.plan.FileEntry) throws java.beans.PropertyVetoException
meth public int sizeFileEntry()
meth public java.lang.String _getSchemaLocation()
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.share.plan.FileEntry getFileEntry(int)
meth public org.netbeans.modules.j2ee.sun.share.plan.FileEntry newFileEntry()
meth public org.netbeans.modules.j2ee.sun.share.plan.FileEntry[] getFileEntry()
meth public static org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan createGraph()
meth public static org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan createGraph(java.io.InputStream) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan createGraph(java.io.InputStream,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan createGraph(org.w3c.dom.Node) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void _setSchemaLocation(java.lang.String)
meth public void addVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void removeVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth public void setFileEntry(int,org.netbeans.modules.j2ee.sun.share.plan.FileEntry)
meth public void setFileEntry(org.netbeans.modules.j2ee.sun.share.plan.FileEntry[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.share.plan.FileEntry
cons public init()
cons public init(int)
fld public final static java.lang.String CONTENT = "Content"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String URI = "Uri"
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getContent()
meth public java.lang.String getName()
meth public java.lang.String getUri()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void addVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void removeVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
meth public void setContent(java.lang.String) throws java.beans.PropertyVetoException
meth public void setName(java.lang.String) throws java.beans.PropertyVetoException
meth public void setUri(java.lang.String) throws java.beans.PropertyVetoException
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.share.plan.Util
meth public static void convert(java.io.InputStream,java.util.jar.JarOutputStream) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.modules.schema2beans.BaseBean
cons public init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
fld protected org.netbeans.modules.schema2beans.DOMBinding binding
fld protected org.netbeans.modules.schema2beans.GraphManager graphManager
fld public final static int MERGE_COMPARE = 4
fld public final static int MERGE_INTERSECT = 1
fld public final static int MERGE_NONE = 0
fld public final static int MERGE_UNION = 2
fld public final static int MERGE_UPDATE = 3
innr public IterateChoiceProperties
intf java.lang.Cloneable
intf org.netbeans.modules.schema2beans.Bean
meth protected boolean hasDomNode()
meth protected int addValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected int removeValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected java.util.Iterator beanPropsIterator()
meth protected org.netbeans.modules.schema2beans.DOMBinding domBinding()
meth protected void addKnownValue(java.lang.String,java.lang.Object)
meth protected void buildPathName(java.lang.StringBuffer)
meth protected void copyProperty(org.netbeans.modules.schema2beans.BeanProp,org.netbeans.modules.schema2beans.BaseBean,int,java.lang.Object)
meth protected void init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
meth protected void initPropertyTables(int)
meth protected void removeValue(org.netbeans.modules.schema2beans.BeanProp,int)
meth protected void setDomBinding(org.netbeans.modules.schema2beans.DOMBinding)
meth protected void setGraphManager(org.netbeans.modules.schema2beans.GraphManager)
meth protected void setValue(org.netbeans.modules.schema2beans.BeanProp,int,java.lang.Object)
meth public abstract void dump(java.lang.StringBuffer,java.lang.String)
meth public boolean hasName(java.lang.String)
meth public boolean isChoiceProperty()
meth public boolean isChoiceProperty(java.lang.String)
meth public boolean isEqualTo(java.lang.Object)
meth public boolean isNull(java.lang.String)
meth public boolean isNull(java.lang.String,int)
meth public boolean isRoot()
meth public int addValue(java.lang.String,java.lang.Object)
meth public int idToIndex(java.lang.String,int)
meth public int indexOf(java.lang.String,java.lang.Object)
meth public int indexToId(java.lang.String,int)
meth public int removeValue(java.lang.String,java.lang.Object)
meth public int size(java.lang.String)
meth public java.lang.Object clone()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object getValue(java.lang.String,int)
meth public java.lang.Object getValueById(java.lang.String,int)
meth public java.lang.Object[] getValues(java.lang.String)
meth public java.lang.Object[] knownValues(java.lang.String)
meth public java.lang.String _getXPathExpr()
meth public java.lang.String _getXPathExpr(java.lang.Object)
meth public java.lang.String dtdName()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String dumpDomNode()
meth public java.lang.String dumpDomNode(int)
meth public java.lang.String dumpDomNode(java.lang.String,int)
meth public java.lang.String fullName()
meth public java.lang.String getAttributeValue(java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,int,java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getDefaultNamespace()
meth public java.lang.String name()
meth public java.lang.String nameChild(java.lang.Object)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean,boolean)
meth public java.lang.String nameSelf()
meth public java.lang.String toString()
meth public java.lang.String[] findAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String[] findPropertyValue(java.lang.String,java.lang.Object)
meth public java.lang.String[] findValue(java.lang.Object)
meth public java.lang.String[] getAttributeNames()
meth public java.lang.String[] getAttributeNames(java.lang.String)
meth public java.util.Iterator listChoiceProperties()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean newInstance(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean parent()
meth public org.netbeans.modules.schema2beans.BaseBean[] childBeans(boolean)
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listChoiceProperties(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public org.netbeans.modules.schema2beans.Bean _getParent()
meth public org.netbeans.modules.schema2beans.Bean _getRoot()
meth public org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp()
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public org.netbeans.modules.schema2beans.BeanProp[] beanProps()
meth public org.netbeans.modules.schema2beans.GraphManager graphManager()
meth public org.w3c.dom.Comment addComment(java.lang.String)
meth public org.w3c.dom.Comment[] comments()
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver,org.xml.sax.ErrorHandler) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void _setChanged(boolean)
meth public void addBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void changeDocType(java.lang.String,java.lang.String)
meth public void childBeans(boolean,java.util.List)
meth public void createAttribute(java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createAttribute(java.lang.String,java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createBean(org.w3c.dom.Node,org.netbeans.modules.schema2beans.GraphManager)
meth public void createProperty(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void createProperty(java.lang.String,java.lang.String,java.lang.Class)
meth public void createRoot(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void dumpAttributes(java.lang.String,int,java.lang.StringBuffer,java.lang.String)
meth public void dumpXml()
meth public void merge(org.netbeans.modules.schema2beans.BaseBean)
meth public void merge(org.netbeans.modules.schema2beans.BaseBean,int)
meth public void mergeUpdate(org.netbeans.modules.schema2beans.BaseBean)
meth public void reindent()
meth public void reindent(java.lang.String)
meth public void removeBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void removeComment(org.w3c.dom.Comment)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removeValue(java.lang.String,int)
meth public void setAttributeValue(java.lang.String,int,java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String,java.lang.String)
meth public void setDefaultNamespace(java.lang.String)
meth public void setValue(java.lang.String,int,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object[])
meth public void setValueById(java.lang.String,int,java.lang.Object)
meth public void write(java.io.File) throws java.io.IOException
meth public void write(java.io.OutputStream) throws java.io.IOException
meth public void write(java.io.OutputStream,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNoReindent(java.io.OutputStream) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNode(java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds attrCache,changeListeners,comparators,defaultNamespace,isRoot,propByName,propByOrder,propertyOrder

CLSS public abstract interface org.netbeans.modules.schema2beans.Bean
meth public abstract boolean hasName(java.lang.String)
meth public abstract boolean isRoot()
meth public abstract int addValue(java.lang.String,java.lang.Object)
meth public abstract int idToIndex(java.lang.String,int)
meth public abstract int indexToId(java.lang.String,int)
meth public abstract int removeValue(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract java.lang.Object getValue(java.lang.String,int)
meth public abstract java.lang.Object getValueById(java.lang.String,int)
meth public abstract java.lang.Object[] getValues(java.lang.String)
meth public abstract java.lang.String dtdName()
meth public abstract java.lang.String name()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public abstract org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public abstract org.netbeans.modules.schema2beans.Bean _getParent()
meth public abstract org.netbeans.modules.schema2beans.Bean _getRoot()
meth public abstract org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public abstract org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void childBeans(boolean,java.util.List)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setValue(java.lang.String,int,java.lang.Object)
meth public abstract void setValue(java.lang.String,java.lang.Object)
meth public abstract void setValueById(java.lang.String,int,java.lang.Object)

