#Signature file v4.1
#Version 1.49

CLSS public java.beans.FeatureDescriptor
cons public init()
meth public boolean isExpert()
meth public boolean isHidden()
meth public boolean isPreferred()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public java.lang.String toString()
meth public java.util.Enumeration<java.lang.String> attributeNames()
meth public void setDisplayName(java.lang.String)
meth public void setExpert(boolean)
meth public void setHidden(boolean)
meth public void setName(java.lang.String)
meth public void setPreferred(boolean)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
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

CLSS public final org.netbeans.modules.websvc.jaxws.api.JAXWSSupport
meth public boolean isFromWSDL(java.lang.String)
meth public java.lang.String addService(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
meth public java.lang.String getServiceImpl(java.lang.String)
meth public java.lang.String getWsdlLocation(java.lang.String)
meth public java.net.URL getCatalog()
meth public java.util.List getServices()
meth public org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata> getWebservicesMetadataModel()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public org.openide.filesystems.FileObject getBindingsFolderForService(java.lang.String,boolean)
meth public org.openide.filesystems.FileObject getDeploymentDescriptorFolder()
meth public org.openide.filesystems.FileObject getLocalWsdlFolderForService(java.lang.String,boolean)
meth public org.openide.filesystems.FileObject getWsdlFolder(boolean) throws java.io.IOException
meth public static org.netbeans.modules.websvc.jaxws.api.JAXWSSupport getJAXWSSupport(org.openide.filesystems.FileObject)
meth public void addService(java.lang.String,java.lang.String,boolean)
meth public void removeNonJsr109Entries(java.lang.String) throws java.io.IOException
meth public void removeService(java.lang.String)
meth public void serviceFromJavaRemoved(java.lang.String)
supr java.lang.Object
hfds impl,implementations

CLSS public final org.netbeans.modules.websvc.jaxws.api.JAXWSView
meth public org.openide.nodes.Node createJAXWSView(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.websvc.jaxws.api.JAXWSView getJAXWSView()
supr java.lang.Object
hfds impl,implementations

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.api.JaxWsRefreshCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void refreshService(boolean)

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.api.JaxWsTesterCookie
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.String getTesterPageURL()

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.api.JaxWsWsdlCookie
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.String getWsdlURL()

CLSS public org.netbeans.modules.websvc.jaxws.api.WsdlWrapperGenerator
cons public init()
meth public static java.lang.String getWrapperName(java.net.URL)
meth public static org.netbeans.modules.websvc.jaxws.api.WsdlWrapperHandler parse(java.io.File) throws java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static org.netbeans.modules.websvc.jaxws.api.WsdlWrapperHandler parse(java.lang.String) throws java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void generateWrapperWSDLContent(java.io.File,javax.xml.transform.stream.StreamSource,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds TEMPLATE_BASE

CLSS public org.netbeans.modules.websvc.jaxws.api.WsdlWrapperHandler
cons public init()
fld public final static java.lang.String SOAP_BINDING_PREFIX = "http://schemas.xmlsoap.org/wsdl/soap"
fld public final static java.lang.String WSDL_SOAP_URI = "http://schemas.xmlsoap.org/wsdl/"
meth public boolean isServiceElement()
meth public java.lang.String getBindingTypeForPort(java.lang.String)
meth public java.lang.String getTargetNsPrefix()
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.DefaultHandler
hfds bindingInfo,bindings,insideBinding,insideService,isBinding,isPortType,isService,ports,prefixes,tns
hcls BindingInfo

CLSS public final org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportFactory
meth public static org.netbeans.modules.websvc.jaxws.api.JAXWSSupport createJAXWSSupport(org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportImpl
fld public final static java.lang.String CATALOG_FILE = "catalog.xml"
fld public final static java.lang.String SERVICES_LOCAL_FOLDER = "web-services"
fld public final static java.lang.String XML_RESOURCES_FOLDER = "xml-resources"
meth public abstract boolean isFromWSDL(java.lang.String)
meth public abstract java.lang.String addService(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
meth public abstract java.lang.String getServiceImpl(java.lang.String)
meth public abstract java.lang.String getWsdlLocation(java.lang.String)
meth public abstract java.net.URL getCatalog()
meth public abstract java.util.List getServices()
meth public abstract org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata> getWebservicesMetadataModel()
meth public abstract org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public abstract org.openide.filesystems.FileObject getBindingsFolderForService(java.lang.String,boolean)
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptorFolder()
meth public abstract org.openide.filesystems.FileObject getLocalWsdlFolderForService(java.lang.String,boolean)
meth public abstract org.openide.filesystems.FileObject getWsdlFolder(boolean) throws java.io.IOException
meth public abstract void addService(java.lang.String,java.lang.String,boolean)
meth public abstract void removeNonJsr109Entries(java.lang.String) throws java.io.IOException
meth public abstract void removeService(java.lang.String)
meth public abstract void serviceFromJavaRemoved(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider
meth public abstract org.netbeans.modules.websvc.jaxws.api.JAXWSSupport findJAXWSSupport(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.websvc.jaxws.spi.JAXWSViewFactory
meth public static org.netbeans.modules.websvc.jaxws.api.JAXWSView createJAXWSView(org.netbeans.modules.websvc.jaxws.spi.JAXWSViewImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.spi.JAXWSViewImpl
meth public abstract org.openide.nodes.Node createJAXWSView(org.netbeans.api.project.Project)

CLSS public abstract interface org.netbeans.modules.websvc.jaxws.spi.JAXWSViewProvider
meth public abstract org.netbeans.modules.websvc.jaxws.api.JAXWSView findJAXWSView()

CLSS public abstract org.netbeans.modules.websvc.jaxws.spi.ProjectJAXWSSupport
cons public init(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper)
fld protected final static java.lang.String JAKARTA_EE_VERSION_10 = "jakarta-ee-version-10"
fld protected final static java.lang.String JAKARTA_EE_VERSION_8 = "jakarta-ee-version-8"
fld protected final static java.lang.String JAKARTA_EE_VERSION_9 = "jakarta-ee-version-9"
fld protected final static java.lang.String JAKARTA_EE_VERSION_91 = "jakarta-ee-version-91"
fld protected final static java.lang.String JAVA_EE_VERSION_15 = "java-ee-version-15"
fld protected final static java.lang.String JAVA_EE_VERSION_16 = "java-ee-version-16"
fld protected final static java.lang.String JAVA_EE_VERSION_17 = "java-ee-version-17"
fld protected final static java.lang.String JAVA_EE_VERSION_18 = "java-ee-version-18"
fld protected final static java.lang.String JAVA_EE_VERSION_NONE = "java-ee-version-none"
intf org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportImpl
meth protected abstract void addJaxwsArtifacts(org.netbeans.api.project.Project,java.lang.String,java.lang.String) throws java.lang.Exception
meth protected java.lang.String getProjectJavaEEVersion()
meth protected org.openide.filesystems.FileObject getXmlArtifactsRoot()
meth protected void addServletElement(org.netbeans.api.project.Project,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String getWsdlLocation(java.lang.String)
meth public abstract org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata> getWebservicesMetadataModel()
meth public abstract org.openide.filesystems.FileObject getWsdlFolder(boolean) throws java.io.IOException
meth public boolean isFromWSDL(java.lang.String)
meth public java.lang.String addService(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
meth public java.lang.String getServiceImpl(java.lang.String)
meth public java.net.URL getCatalog()
meth public java.util.List<org.netbeans.modules.websvc.api.jaxws.project.config.Service> getServices()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public org.openide.filesystems.FileObject getBindingsFolderForService(java.lang.String,boolean)
meth public org.openide.filesystems.FileObject getLocalWsdlFolderForService(java.lang.String,boolean)
meth public void addService(java.lang.String,java.lang.String,boolean)
meth public void removeService(java.lang.String)
meth public void serviceFromJavaRemoved(java.lang.String)
supr java.lang.Object
hfds DEFAULT_WSIMPORT_OPTIONS,DEFAULT_WSIMPORT_VALUES,TARGET_OPTION,XENDORSED_OPTION,XNOCOMPILE_OPTION,antProjectHelper,project,serviceArtifactsFolder

CLSS public abstract org.openide.nodes.Node
cons protected init(org.openide.nodes.Children)
cons protected init(org.openide.nodes.Children,org.openide.util.Lookup)
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_ICON = "icon"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_OPENED_ICON = "openedIcon"
fld public final static java.lang.String PROP_PARENT_NODE = "parentNode"
fld public final static java.lang.String PROP_PROPERTY_SETS = "propertySets"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
fld public final static org.openide.nodes.Node EMPTY
innr public abstract interface static Cookie
innr public abstract interface static Handle
innr public abstract static IndexedProperty
innr public abstract static Property
innr public abstract static PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean hasPropertyChangeListener()
meth protected final void fireCookieChange()
meth protected final void fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void fireIconChange()
meth protected final void fireNameChange(java.lang.String,java.lang.String)
meth protected final void fireNodeDestroyed()
meth protected final void fireOpenedIconChange()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void firePropertySetsChange(org.openide.nodes.Node$PropertySet[],org.openide.nodes.Node$PropertySet[])
meth protected final void fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void setChildren(org.openide.nodes.Children)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean canCopy()
meth public abstract boolean canCut()
meth public abstract boolean canDestroy()
meth public abstract boolean canRename()
meth public abstract boolean hasCustomizer()
meth public abstract java.awt.Component getCustomizer()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.awt.Image getOpenedIcon(int)
meth public abstract java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public abstract org.openide.nodes.Node cloneNode()
meth public abstract org.openide.nodes.Node$Handle getHandle()
meth public abstract org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract org.openide.util.datatransfer.NewType[] getNewTypes()
meth public abstract org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public boolean equals(java.lang.Object)
meth public final boolean isLeaf()
meth public final javax.swing.JPopupMenu getContextMenu()
meth public final org.openide.nodes.Children getChildren()
meth public final org.openide.nodes.Node getParentNode()
meth public final org.openide.util.Lookup getLookup()
meth public final void addNodeListener(org.openide.nodes.NodeListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeNodeListener(org.openide.nodes.NodeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int hashCode()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String toString()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setHidden(boolean)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
hfds BLOCK_EVENTS,INIT_LOCK,LOCK,TEMPL_COOKIE,err,hierarchy,listeners,lookups,parent,warnedBadProperties
hcls LookupEventList,PropertyEditorRef

CLSS public abstract interface static org.openide.nodes.Node$Cookie
 outer org.openide.nodes.Node

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

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

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public org.xml.sax.helpers.DefaultHandler
cons public init()
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

