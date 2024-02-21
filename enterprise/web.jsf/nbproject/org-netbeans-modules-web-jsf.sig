#Signature file v4.1
#Version 2.3.0

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

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

CLSS public abstract interface java.util.EventListener

CLSS public org.netbeans.modules.web.jsf.api.ConfigurationUtils
cons public init()
meth public static java.lang.String getFacesServletMapping(org.netbeans.modules.web.api.webmodule.WebModule)
meth public static java.lang.String translateURI(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.j2ee.dd.api.web.Servlet getFacesServlet(org.netbeans.modules.web.api.webmodule.WebModule)
meth public static org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel getConfigModel(org.openide.filesystems.FileObject,boolean)
meth public static org.openide.filesystems.FileObject findFacesConfigForManagedBean(org.netbeans.modules.web.api.webmodule.WebModule,java.lang.String)
meth public static org.openide.filesystems.FileObject[] getFacesConfigFiles(org.netbeans.modules.web.api.webmodule.WebModule)
supr java.lang.Object
hfds configModelsEditable,configModelsNonEditable

CLSS public org.netbeans.modules.web.jsf.api.JsfComponentUtils
meth public static boolean isMavenBased(org.netbeans.modules.web.api.webmodule.WebModule)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.project.libraries.Library createMavenDependencyLibrary(java.lang.String,java.lang.String[],java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.project.libraries.Library enhanceLibraryWithPomContent(org.netbeans.api.project.libraries.Library,java.util.List<java.net.URI>) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static void enhanceFileBody(org.openide.loaders.DataObject,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static void reformat(org.openide.loaders.DataObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public org.netbeans.modules.web.jsf.api.editor.JSFBeanCache
cons public init()
innr public abstract interface static JsfBeansProvider
meth public static java.util.List<org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean> getBeans(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOG

CLSS public abstract interface static org.netbeans.modules.web.jsf.api.editor.JSFBeanCache$JsfBeansProvider
 outer org.netbeans.modules.web.jsf.api.editor.JSFBeanCache
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean> getBeans(org.netbeans.api.project.Project)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext
meth public abstract org.openide.awt.UndoRedo getUndoRedo()
meth public abstract org.openide.filesystems.FileObject getFacesConfigFile()
meth public abstract void setMultiViewTopComponent(org.openide.windows.TopComponent)

CLSS public org.netbeans.modules.web.jsf.api.editor.JSFEditorUtilities
cons public init()
fld public final static int XML_ELEMENT = 4
fld public final static int XML_TEXT = 1
fld public final static java.lang.String END_LINE
meth public static int writeBean(org.netbeans.editor.BaseDocument,org.netbeans.modules.schema2beans.BaseBean,java.lang.String) throws java.io.IOException
meth public static int[] getConverterDefinition(org.netbeans.editor.BaseDocument,java.lang.String,java.lang.String)
meth public static int[] getManagedBeanDefinition(org.netbeans.editor.BaseDocument,java.lang.String,java.lang.String)
meth public static int[] getNavigationRuleDefinition(org.netbeans.editor.BaseDocument,java.lang.String)
meth public static java.lang.String getNavigationRule(org.netbeans.editor.BaseDocument,int)
meth public static org.netbeans.editor.BaseDocument getBaseDocument(org.openide.loaders.DataObject)
meth public static org.openide.text.CloneableEditorSupport findCloneableEditorSupport(org.openide.loaders.DataObject)
supr java.lang.Object
hcls CreateXMLPane

CLSS public org.netbeans.modules.web.jsf.api.editor.JSFResourceBundlesProvider
cons public init()
meth public static java.util.List<org.netbeans.modules.web.el.spi.ResourceBundle> getResourceBundles(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOGGER

CLSS public org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider
innr public final static FacesComponentLibrary
innr public final static FacesLibraryComponent
meth public static java.util.Collection<? extends org.netbeans.modules.web.jsfapi.api.Library> getLibraries(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOGGER
hcls FacesComponentTag

CLSS public final static org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider$FacesComponentLibrary
 outer org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider
cons public init(java.lang.String)
intf org.netbeans.modules.web.jsfapi.api.Library
meth public java.lang.String getDefaultNamespace()
meth public java.lang.String getDefaultPrefix()
meth public java.lang.String getDisplayName()
meth public java.lang.String getNamespace()
meth public java.util.Collection<? extends org.netbeans.modules.web.jsfapi.api.LibraryComponent> getComponents()
meth public java.util.Set<java.lang.String> getValidNamespaces()
meth public org.netbeans.modules.web.jsfapi.api.LibraryComponent getComponent(java.lang.String)
meth public org.netbeans.modules.web.jsfapi.api.LibraryType getType()
meth public void addComponent(org.netbeans.modules.web.jsfapi.api.LibraryComponent)
supr java.lang.Object
hfds FACES_COMPONENT,components,namespace

CLSS public final static org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider$FacesLibraryComponent
 outer org.netbeans.modules.web.jsf.api.editor.JsfFacesComponentsProvider
cons public init(org.netbeans.modules.web.jsfapi.api.Library,org.netbeans.modules.web.jsf.impl.metamodel.ComponentImpl)
intf org.netbeans.modules.web.jsfapi.api.LibraryComponent
meth public java.lang.String getName()
meth public java.lang.String[][] getDescription()
meth public org.netbeans.modules.web.jsfapi.api.Library getLibrary()
meth public org.netbeans.modules.web.jsfapi.api.Tag getTag()
meth public org.openide.filesystems.FileObject getComponentFile(org.netbeans.api.project.Project)
supr java.lang.Object
hfds handle,library,name,tag

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering
fld public final static java.lang.String NAME
fld public final static java.lang.String OTHERS
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrderingElement> getElements()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Name> getNames()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Others> getOthers()
meth public abstract void addElement(int,org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrderingElement)
meth public abstract void addName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public abstract void addOther(org.netbeans.modules.web.jsf.api.facesmodel.Others)
meth public abstract void removeName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public abstract void removeOthers(org.netbeans.modules.web.jsf.api.facesmodel.Others)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrderingElement
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ActionListener
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.After
intf org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Application
fld public final static java.lang.String ACTION_LISTENER
fld public final static java.lang.String APPLICATION_ELEMENT = "application-element"
fld public final static java.lang.String APPLICATION_EXTENSION
fld public final static java.lang.String DEFAULT_RENDER_KIT_ID
fld public final static java.lang.String DEFAULT_VALIDATORS
fld public final static java.lang.String EL_RESOLVER
fld public final static java.lang.String LOCALE_CONFIG
fld public final static java.lang.String MESSAGE_BUNDLE
fld public final static java.lang.String NAVIGATION_HANDLER
fld public final static java.lang.String PARTIAL_TRAVERSAL
fld public final static java.lang.String PROPERTY_RESOLVER
fld public final static java.lang.String RESOURCE_BUNDLE
fld public final static java.lang.String RESOURCE_HANDLER
fld public final static java.lang.String RESOURCE_LIBRARY_CONTRACTS
fld public final static java.lang.String STATE_MANAGER
fld public final static java.lang.String SYSTEM_EVENT_LISTENER
fld public final static java.lang.String VARIABLE_RESOLVER
fld public final static java.lang.String VIEW_HANDLER
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ActionListener> getActionListeners()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement> getApplicationElements()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ApplicationExtension> getApplicationExtensions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId> getDefaultRenderKitIds()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators> getDefaultValidators()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ElResolver> getElResolvers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig> getLocaleConfig()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle> getMessageBundles()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler> getNavigationHandler()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal> getPartialTraversals()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver> getPropertyResolvers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle> getResourceBundles()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler> getResourceHandlers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts> getResourceLibraryContracts()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.StateManager> getStateManagers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver> getVariableResolvers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler> getViewHandlers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener> getSystemEventListeners()
meth public abstract void addActionListener(org.netbeans.modules.web.jsf.api.facesmodel.ActionListener)
meth public abstract void addApplicationElement(int,org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement)
meth public abstract void addApplicationExtension(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationExtension)
meth public abstract void addDefaultRenderKitId(org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId)
meth public abstract void addDefaultValidators(org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators)
meth public abstract void addElResolver(org.netbeans.modules.web.jsf.api.facesmodel.ElResolver)
meth public abstract void addLocaleConfig(org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig)
meth public abstract void addMessageBundle(org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle)
meth public abstract void addNavigationHandler(org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler)
meth public abstract void addPartialTraversal(org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal)
meth public abstract void addPropertyResolver(org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver)
meth public abstract void addResourceBundle(org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle)
meth public abstract void addResourceHandler(org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler)
meth public abstract void addResourceLibraryContract(org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts)
meth public abstract void addStateMenager(org.netbeans.modules.web.jsf.api.facesmodel.StateManager)
meth public abstract void addSystemEventListener(org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener)
meth public abstract void addVariableResolver(org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver)
meth public abstract void addViewHandler(org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler)
meth public abstract void removeActionListener(org.netbeans.modules.web.jsf.api.facesmodel.ActionListener)
meth public abstract void removeApplicationExtension(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationExtension)
meth public abstract void removeDefaultRenderKitId(org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId)
meth public abstract void removeDefaultValidators(org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators)
meth public abstract void removeElResolver(org.netbeans.modules.web.jsf.api.facesmodel.ElResolver)
meth public abstract void removeLocaleConfig(org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig)
meth public abstract void removeMessageBundle(org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle)
meth public abstract void removeNavigationHandler(org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler)
meth public abstract void removePartialTraversal(org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal)
meth public abstract void removePropertyResolver(org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver)
meth public abstract void removeResourceBundle(org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle)
meth public abstract void removeResourceHandler(org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler)
meth public abstract void removeResourceLibraryContract(org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts)
meth public abstract void removeStateMenager(org.netbeans.modules.web.jsf.api.facesmodel.StateManager)
meth public abstract void removeSystemEventListener(org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener)
meth public abstract void removeVariableResolver(org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver)
meth public abstract void removeViewHandler(org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ApplicationExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer
fld public final static java.lang.String ATTRIBUTE
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute> getAttributes()
meth public abstract void addAttribute(int,org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute)
meth public abstract void addAttribute(org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute)
meth public abstract void removeAttribute(org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Before
intf org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Clazz
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute
fld public final static java.lang.String ATTRIBUTE_CLASS
fld public final static java.lang.String ATTRIBUTE_NAME
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getAttributeClass()
meth public abstract java.lang.String getAttributeName()
meth public abstract void setAttributeClass(java.lang.String)
meth public abstract void setAttributeName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping
fld public final static java.lang.String CONTRACTS
fld public final static java.lang.String URL_PATTERN
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getContracts()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern getUrlPattern()
meth public abstract void setContracts(java.lang.String)
meth public abstract void setUrlPattern(org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Converter
fld public final static java.lang.String CONVERTER_CLASS
fld public final static java.lang.String CONVERTER_EXTENSION
fld public final static java.lang.String CONVERTER_FOR_CLASS
fld public final static java.lang.String CONVERTER_ID
intf org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer
intf org.netbeans.modules.web.jsf.api.metamodel.FacesConverter
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension> getConverterExtensions()
meth public abstract void addConverterExtension(int,org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
meth public abstract void addConverterExtension(org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
meth public abstract void removeConverterExtension(org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
meth public abstract void setConverterClass(java.lang.String)
meth public abstract void setConverterForClass(java.lang.String)
meth public abstract void setConverterId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.lang.String getLocale()
meth public abstract void setLocale(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators
fld public final static java.lang.String VALIDATOR_ID
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.metamodel.ValidatorId> getValidatorIds()
meth public abstract void addValidatorId(int,org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId)
meth public abstract void addValidatorId(org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId)
meth public abstract void removeValidatorId(org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Description
intf org.netbeans.modules.web.jsf.api.facesmodel.LangAttribute
meth public abstract java.lang.String getValue()
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
fld public final static java.lang.String DESCRIPTION
fld public final static java.lang.String DISPLAY_NAME
fld public final static java.lang.String ICON
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Description> getDescriptions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.DisplayName> getDisplayNames()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Icon> getIcons()
meth public abstract void addDescription(int,org.netbeans.modules.web.jsf.api.facesmodel.Description)
meth public abstract void addDescription(org.netbeans.modules.web.jsf.api.facesmodel.Description)
meth public abstract void addDisplayName(int,org.netbeans.modules.web.jsf.api.facesmodel.DisplayName)
meth public abstract void addDisplayName(org.netbeans.modules.web.jsf.api.facesmodel.DisplayName)
meth public abstract void addIcon(int,org.netbeans.modules.web.jsf.api.facesmodel.Icon)
meth public abstract void addIcon(org.netbeans.modules.web.jsf.api.facesmodel.Icon)
meth public abstract void removeDescription(org.netbeans.modules.web.jsf.api.facesmodel.Description)
meth public abstract void removeDisplayName(org.netbeans.modules.web.jsf.api.facesmodel.DisplayName)
meth public abstract void removeIcon(org.netbeans.modules.web.jsf.api.facesmodel.Icon)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.DisplayName
intf org.netbeans.modules.web.jsf.api.facesmodel.LangAttribute
meth public abstract java.lang.String getValue()
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ElResolver
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FaceletCacheFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior
fld public final static java.lang.String BEHAVIOR_EXTENSION
intf org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer
intf org.netbeans.modules.web.jsf.api.metamodel.Behavior
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension> getBehaviorExtensions()
meth public abstract void addBehaviorExtension(int,org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension)
meth public abstract void addBehaviorExtension(org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension)
meth public abstract void removeBehaviorExtension(org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension)
meth public abstract void setBehaviorClass(java.lang.String)
meth public abstract void setBehaviorId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
intf org.netbeans.modules.web.jsf.api.metamodel.ClientBehaviorRenderer
meth public abstract void setClientBehaviorRendererClass(java.lang.String)
meth public abstract void setClientBehaviorRendererType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent
fld public final static java.lang.String COMPONENT_EXTENSION
fld public final static java.lang.String FACET
intf org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer
intf org.netbeans.modules.web.jsf.api.metamodel.Component
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension> getComponentExtensions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Facet> getFacets()
meth public abstract void addComponentExtension(int,org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension)
meth public abstract void addComponentExtension(org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension)
meth public abstract void addFacet(int,org.netbeans.modules.web.jsf.api.facesmodel.Facet)
meth public abstract void addFacet(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
meth public abstract void removeComponentExtension(org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension)
meth public abstract void removeFacet(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
meth public abstract void setComponentClass(java.lang.String)
meth public abstract void setComponentType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig
fld public final static java.lang.String ABSOLUTE_ORDERING
fld public final static java.lang.String APPLICATION
fld public final static java.lang.String BEHAVIOR
fld public final static java.lang.String COMPONENT
fld public final static java.lang.String CONVERTER
fld public final static java.lang.String FACES_CONFIG_EXTENSION
fld public final static java.lang.String FACTORY
fld public final static java.lang.String FLOW_DEFINITION
fld public final static java.lang.String LIFECYCLE
fld public final static java.lang.String MANAGED_BEAN
fld public final static java.lang.String METADATA_COMPLETE = "metadata-complete"
fld public final static java.lang.String NAME
fld public final static java.lang.String NAVIGATION_RULE
fld public final static java.lang.String ORDERING
fld public final static java.lang.String PROTECTED_VIEWS
fld public final static java.lang.String REFERENCED_BEAN
fld public final static java.lang.String RENDER_KIT
fld public final static java.lang.String VALIDATOR
fld public final static java.lang.String VERSION = "version"
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.lang.Boolean isMetaDataComplete()
meth public abstract java.lang.String getVersion()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering> getAbsoluteOrderings()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Application> getApplications()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Converter> getConverters()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior> getBehaviors()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement> getFacesConfigElements()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigExtension> getFacesConfigExtensions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator> getValidators()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Factory> getFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition> getFlowDefinitions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle> getLifecycles()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean> getManagedBeans()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Name> getNames()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule> getNavigationRules()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Ordering> getOrderings()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews> getProtectedViews()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean> getReferencedBeans()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.RenderKit> getRenderKits()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.metamodel.Component> getComponents()
meth public abstract void addAbsoluteOrdering(org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering)
meth public abstract void addApplication(org.netbeans.modules.web.jsf.api.facesmodel.Application)
meth public abstract void addBehavior(org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior)
meth public abstract void addComponent(org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent)
meth public abstract void addConverter(org.netbeans.modules.web.jsf.api.facesmodel.Converter)
meth public abstract void addFacesConfigElement(int,org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement)
meth public abstract void addFacesConfigExtension(org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigExtension)
meth public abstract void addFactories(org.netbeans.modules.web.jsf.api.facesmodel.Factory)
meth public abstract void addFlowDefinition(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition)
meth public abstract void addLifecycle(org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle)
meth public abstract void addManagedBean(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean)
meth public abstract void addName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public abstract void addNavigationRule(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule)
meth public abstract void addOrdering(org.netbeans.modules.web.jsf.api.facesmodel.Ordering)
meth public abstract void addProtectedView(org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews)
meth public abstract void addReferencedBean(org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean)
meth public abstract void addRenderKit(org.netbeans.modules.web.jsf.api.facesmodel.RenderKit)
meth public abstract void addValidator(org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator)
meth public abstract void removeAbsoluteOrdering(org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering)
meth public abstract void removeApplication(org.netbeans.modules.web.jsf.api.facesmodel.Application)
meth public abstract void removeBehavior(org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior)
meth public abstract void removeComponent(org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent)
meth public abstract void removeConverter(org.netbeans.modules.web.jsf.api.facesmodel.Converter)
meth public abstract void removeFacesConfigExtension(org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigExtension)
meth public abstract void removeFactory(org.netbeans.modules.web.jsf.api.facesmodel.Factory)
meth public abstract void removeFlowDefinition(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition)
meth public abstract void removeLifecycle(org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle)
meth public abstract void removeManagedBean(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean)
meth public abstract void removeName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public abstract void removeNavigationRule(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule)
meth public abstract void removeOrdering(org.netbeans.modules.web.jsf.api.facesmodel.Ordering)
meth public abstract void removeProtectedView(org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews)
meth public abstract void removeReferencedBean(org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean)
meth public abstract void removeRenderKit(org.netbeans.modules.web.jsf.api.facesmodel.RenderKit)
meth public abstract void removeValidator(org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator)
meth public abstract void setMetaDataComplete(java.lang.Boolean)
meth public abstract void setVersion(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
intf org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps
intf org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty
meth public abstract void setPropertyClass(java.lang.String)
meth public abstract void setPropertyName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer
fld public final static java.lang.String COMPONENT_FAMILY
fld public final static java.lang.String FACET
intf org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.metamodel.Renderer
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Facet> getFacets()
meth public abstract void addFacet(int,org.netbeans.modules.web.jsf.api.facesmodel.Facet)
meth public abstract void addFacet(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
meth public abstract void removeFacet(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
meth public abstract void setComponentFamily(java.lang.String)
meth public abstract void setRendererClass(java.lang.String)
meth public abstract void setRendererType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener
meth public abstract void setSourceClass(java.lang.String)
meth public abstract void setSystemEventClass(java.lang.String)
meth public abstract void setSystemEventListenerClass(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator
fld public final static java.lang.String VALIDATOR_EXTENSION
intf org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer
intf org.netbeans.modules.web.jsf.api.metamodel.Validator
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension> getValidatorExtensions()
meth public abstract void addValidatorExtension(int,org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension)
meth public abstract void addValidatorExtension(org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension)
meth public abstract void removeValidatorExtension(org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension)
meth public abstract void setValidatorClass(java.lang.String)
meth public abstract void setValidatorId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent
intf org.netbeans.modules.web.jsf.api.metamodel.ValidatorId

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Facet
fld public final static java.lang.String FACET_NAME
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getFacetName()
meth public abstract void setFacetName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Factory
fld public final static java.lang.String APPLICATION_FACTORY
fld public final static java.lang.String EXCEPTION_HANDLER_FACTORY
fld public final static java.lang.String EXTERNAL_CONTEXT_FACTORY
fld public final static java.lang.String FACELET_CACHE_FACTORY
fld public final static java.lang.String FACES_CONTEXT_FACTORY
fld public final static java.lang.String FACTORY_EXTENSION
fld public final static java.lang.String FLASH_FACTORY
fld public final static java.lang.String FLOW_HANDLER_FACTORY
fld public final static java.lang.String LIFECYCLE_FACTORY
fld public final static java.lang.String PARTIAL_VIEW_CONTEXT_FACTORY
fld public final static java.lang.String RENDER_KIT_FACTORY
fld public final static java.lang.String TAG_HANDLER_DELEGATE_FACTORY
fld public final static java.lang.String VIEW_DECLARATION_LANGUAGE_FACTORY
fld public final static java.lang.String VISIT_CONTEXT_FACTORY
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory> getApplicationFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory> getExceptionHandlerFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory> getExternalContextFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FaceletCacheFactory> getFaceletCacheFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory> getFacesContextFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement> getElements()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FactoryExtension> getFactoryExtensions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory> getFlashFactory()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowHandlerFactory> getFlowHandlerFactory()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory> getLifecycleFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory> getPartialViewContextFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory> getRenderKitFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory> getTagHandlerDelegateFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory> getViewDeclarationLanguageFactories()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory> getVisitContextFactories()
meth public abstract void addApplicationFactory(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory)
meth public abstract void addElement(int,org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement)
meth public abstract void addExceptionHandlerFactory(org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory)
meth public abstract void addExternalContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory)
meth public abstract void addFaceletCacheFactory(org.netbeans.modules.web.jsf.api.facesmodel.FaceletCacheFactory)
meth public abstract void addFacesContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory)
meth public abstract void addFactoryExtension(org.netbeans.modules.web.jsf.api.facesmodel.FactoryExtension)
meth public abstract void addFlashFactory(org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory)
meth public abstract void addFlowHandlerFactory(org.netbeans.modules.web.jsf.api.facesmodel.FlowHandlerFactory)
meth public abstract void addLifecycleFactory(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory)
meth public abstract void addPartialViewContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory)
meth public abstract void addRenderKitFactory(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory)
meth public abstract void addTagHandlerDelegateFactory(org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory)
meth public abstract void addViewDeclarationLanguageFactory(org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory)
meth public abstract void addVisitContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory)
meth public abstract void removeApplicationFactory(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory)
meth public abstract void removeExceptionHandlerFactory(org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory)
meth public abstract void removeExternalContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory)
meth public abstract void removeFaceletCacheFactory(org.netbeans.modules.web.jsf.api.facesmodel.FaceletCacheFactory)
meth public abstract void removeFacesContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory)
meth public abstract void removeFactoryExtension(org.netbeans.modules.web.jsf.api.facesmodel.FactoryExtension)
meth public abstract void removeFlashFactory(org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory)
meth public abstract void removeFlowHandlerFactory(org.netbeans.modules.web.jsf.api.facesmodel.FlowHandlerFactory)
meth public abstract void removeLifecycleFactory(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory)
meth public abstract void removePartialViewContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory)
meth public abstract void removeRenderKitFactory(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory)
meth public abstract void removeTagHandlerDelegateFactory(org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory)
meth public abstract void removeViewDeclarationLanguageFactory(org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory)
meth public abstract void removeVisitContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FactoryExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowCall
fld public final static java.lang.String FLOW_REFERENCE
fld public final static java.lang.String OUTBOUND_PARAMETER
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference> getFacesFlowReferences()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowCallOutboundParameter> getOutboundParameters()
meth public abstract void addFacesFlowReference(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference)
meth public abstract void addOutboundParameter(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallOutboundParameter)
meth public abstract void removeFacesFlowReference(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference)
meth public abstract void removeOutboundParameter(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallOutboundParameter)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference
fld public final static java.lang.String FLOW_DOCUMENT_ID
fld public final static java.lang.String FLOW_ID
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId> getFlowDocumentIds()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowId> getFlowIds()
meth public abstract void addFlowDocumentId(org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId)
meth public abstract void addFlowId(org.netbeans.modules.web.jsf.api.facesmodel.FlowId)
meth public abstract void removeFlowDocumentId(org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId)
meth public abstract void removeFlowId(org.netbeans.modules.web.jsf.api.facesmodel.FlowId)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInOutParameter
fld public final static java.lang.String NAME
intf org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameterValue
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Name> getNames()
meth public abstract void addName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public abstract void removeName(org.netbeans.modules.web.jsf.api.facesmodel.Name)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInboundParameter
intf org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInOutParameter

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowCallOutboundParameter
intf org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInOutParameter

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter
fld public final static java.lang.String CLASS
intf org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameterValue
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Clazz> getClasses()
meth public abstract void addClass(org.netbeans.modules.web.jsf.api.facesmodel.Clazz)
meth public abstract void removeClass(org.netbeans.modules.web.jsf.api.facesmodel.Clazz)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameterValue
fld public final static java.lang.String VALUE
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Value> getValues()
meth public abstract void addValue(org.netbeans.modules.web.jsf.api.facesmodel.Value)
meth public abstract void removeValue(org.netbeans.modules.web.jsf.api.facesmodel.Value)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition
fld public final static java.lang.String FINALIZER
fld public final static java.lang.String FLOW_CALL
fld public final static java.lang.String FLOW_RETURN
fld public final static java.lang.String INBOUND_PARAMETER
fld public final static java.lang.String INITIALIZER
fld public final static java.lang.String METHOD_CALL
fld public final static java.lang.String NAVIGATION_RULE
fld public final static java.lang.String START_NODE
fld public final static java.lang.String SWITCH
fld public final static java.lang.String VIEW
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowCall> getFlowCalls()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInboundParameter> getInboundParameters()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer> getFinalizers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer> getInitializers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall> getMethodCalls()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn> getFlowReturns()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode> getStartNodes()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch> getSwitches()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowView> getViews()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule> getNavigationRules()
meth public abstract void addFinalizer(org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer)
meth public abstract void addFlowCall(org.netbeans.modules.web.jsf.api.facesmodel.FlowCall)
meth public abstract void addFlowReturn(org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn)
meth public abstract void addInboundParameter(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInboundParameter)
meth public abstract void addInitializer(org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer)
meth public abstract void addMethodCall(org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall)
meth public abstract void addNavigationRule(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule)
meth public abstract void addStartNode(org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode)
meth public abstract void addSwitch(org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch)
meth public abstract void addView(org.netbeans.modules.web.jsf.api.facesmodel.FlowView)
meth public abstract void removeFinalizer(org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer)
meth public abstract void removeFlowCall(org.netbeans.modules.web.jsf.api.facesmodel.FlowCall)
meth public abstract void removeFlowReturn(org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn)
meth public abstract void removeInboundParameter(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInboundParameter)
meth public abstract void removeInitializer(org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer)
meth public abstract void removeMethodCall(org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall)
meth public abstract void removeNavigationRule(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule)
meth public abstract void removeStartNode(org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode)
meth public abstract void removeSwitch(org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch)
meth public abstract void removeView(org.netbeans.modules.web.jsf.api.facesmodel.FlowView)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowHandlerFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowId
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall
fld public final static java.lang.String DEFAULT_OUTCOME
fld public final static java.lang.String METHOD
fld public final static java.lang.String PARAMETER
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter> getParameters()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome> getDefaultOutcomes()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Method> getMethods()
meth public abstract void addDefaultOutcome(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome)
meth public abstract void addMethod(org.netbeans.modules.web.jsf.api.facesmodel.Method)
meth public abstract void addParameter(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter)
meth public abstract void removeDefaultOutcome(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome)
meth public abstract void removeMethod(org.netbeans.modules.web.jsf.api.facesmodel.Method)
meth public abstract void removeParameter(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn
fld public final static java.lang.String FROM_OUTCOME
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome> getFromOutcomes()
meth public abstract void addFromOutcome(org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome)
meth public abstract void removeFromOutcome(org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch
fld public final static java.lang.String DEFAULT_OUTCOME
fld public final static java.lang.String NAVIGATION_CASE
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome> getDefaultOutcomes()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase> getNavigationCases()
meth public abstract void addDefaultOutcome(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome)
meth public abstract void addNavigationCase(org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase)
meth public abstract void removeDefaultOutcome(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome)
meth public abstract void removeNavigationCase(org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FlowView
fld public final static java.lang.String VDL_DOCUMENT
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getVdlDocument()
meth public abstract void setVdlDocument(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.lang.String getFullyQualifiedClassType()
meth public abstract void setFullyQualifiedClassType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Icon
fld public final static java.lang.String LARGE_ICON
fld public final static java.lang.String SMALL_ICON
intf org.netbeans.modules.web.jsf.api.facesmodel.LangAttribute
meth public abstract java.lang.String getLargeIcon()
meth public abstract java.lang.String getSmallIcon()
meth public abstract void setLargeIcon(java.lang.String)
meth public abstract void setSmallIcon(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
fld public final static java.lang.String ID = "id"
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.lang.String getId()
meth public abstract void setId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.If
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent>
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel getModel()
meth public abstract void accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering createAbsoluteOrdering()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ActionListener createActionListener()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.After createAfter()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Application createApplication()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory createApplicationFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Before createBefore()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Clazz createClass()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute createAttribute()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping createContractMapping()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Converter createConverter()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale createDefatultLocale()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId createDefaultRenderKitId()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators createDefaultValidators()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Description createDescription()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.DisplayName createDisplayName()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ElResolver createElResolver()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory createExceptionHandlerFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory createExternalContextFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FaceletCacheFactory createFaceletCacheFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior createBehavior()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer createClientBehaviorRenderer()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent createComponent()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig createFacesConfig()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory createFacesContextFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty createManagedProperty()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer createRenderer()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener createSystemEventListener()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator createValidator()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId createValidatorId()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Facet createFacet()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Factory createFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory createFlashFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowCall createFlowDefinitionFlowCall()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference createFlowDefinitionFlowCallFacesFlowReference()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInboundParameter createFlowDefinitionFlowCallInboundParameter()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowCallOutboundParameter createFlowDefinitionFlowCallOutboundParameter()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter createFlowDefinitionFlowCallParameter()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome createFlowDefinitionDefaultOutcome()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition createFlowDefinition()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId createFlowDocumentId()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer createFinalizer()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowHandlerFactory createFlowHandlerFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowId createFlowId()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer createInitializer()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall createFlowDefinitionFacesMethodCall()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn createFlowDefinitionFlowReturn()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode createStartNode()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch createFlowDefinitionSwitch()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FlowView createFlowDefinitionView()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome createFromOutcome()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Icon createIcon()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.If createIf()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent create(org.w3c.dom.Element,org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent)
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle createLifecycle()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory createLifecycleFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ListEntries createListEntries()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig createLocaleConfig()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean createManagedBean()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.MapEntries createMapEntries()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle createMessageBundle()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Method createMethod()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Name createName()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase createNavigationCase()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler createNavigationHandler()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule createNavigationRule()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Ordering createOrdering()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Others createOthers()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal createPartialTraversal()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory createPartialViewContextFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener createPhaseListener()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Property createProperty()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver createPropertyResolver()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews createProtectedView()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Redirect createRedirect()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean createReferencedBean()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.RenderKit createRenderKit()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory createRenderKitFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle createResourceBundle()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler createResourceHandler()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts createResourceLibraryContracts()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.StateManager createStateManager()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale createSupportedLocale()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory createTagHandlerDelegateFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern createUrlPattern()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Value createValue()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver createVariableResolver()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory createViewDeclarationLanguageFactory()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler createViewHandler()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ViewParam createViewParam()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory createVisitContextFactory()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel
intf org.netbeans.modules.xml.xam.dom.DocumentModel<org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent>
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig getRootComponent()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory getFactory()
meth public abstract org.netbeans.modules.web.jsfapi.api.JsfVersion getVersion()

CLSS public org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModelFactory
meth protected org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel createModel(org.netbeans.modules.xml.xam.ModelSource)
meth public org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public static org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModelFactory getInstance()
supr org.netbeans.modules.xml.xam.AbstractModelFactory<org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel>
hfds INSTANCE

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor
innr public static Deep
innr public static Default
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ActionListener)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.After)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Application)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Before)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Clazz)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Converter)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Description)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.DisplayName)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ElResolver)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FaceletCacheFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Factory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowCall)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInOutParameter)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowHandlerFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowId)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowView)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Icon)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.If)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ListEntries)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.MapEntries)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Method)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Ordering)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Others)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Property)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Redirect)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.RenderKit)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.StateManager)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.Value)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.ViewParam)
meth public abstract void visit(org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory)

CLSS public static org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor$Deep
 outer org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor
cons public init()
meth protected void visitChild(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent)
supr org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor$Default

CLSS public static org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor$Default
 outer org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor
cons public init()
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor
meth protected void visitChild()
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ActionListener)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.After)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Application)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Before)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Clazz)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Converter)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Description)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.DisplayName)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ElResolver)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FaceletCacheFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesValidator)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FacesValidatorId)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Factory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowCall)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInOutParameter)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowHandlerFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowId)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FlowView)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Icon)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.If)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ListEntries)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.MapEntries)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Method)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Ordering)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Others)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Property)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Redirect)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.RenderKit)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.StateManager)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.Value)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.ViewParam)
meth public void visit(org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory)
supr java.lang.Object

CLSS public final org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion forClasspath(java.util.Collection<java.io.File>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion forClasspath(java.util.List<java.net.URL>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion forProject(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion forServerLibrary(org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion forWebModule(org.netbeans.modules.web.api.webmodule.WebModule)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion get(org.netbeans.modules.web.api.webmodule.WebModule,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOG,SPECIFIC_CLASS_NAMES,projectListenerCache,projectVersionCache

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.LangAttribute
fld public final static java.lang.String LANG_ATTRIBUTE
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.lang.String getLang()
meth public abstract void setLang(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle
fld public final static java.lang.String LIFECYCLE_EXTENSION
fld public final static java.lang.String PHASE_LISTENER
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension> getLifecycleExtensions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener> getPhaseListeners()
meth public abstract void addLifecycleExtension(int,org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension)
meth public abstract void addLifecycleExtension(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension)
meth public abstract void addPhaseListener(int,org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener)
meth public abstract void addPhaseListener(org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener)
meth public abstract void removeLifecycleExtension(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension)
meth public abstract void removePhaseListener(org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.LifecycleExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ListEntries
fld public final static java.lang.String VALUE_CLASS
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps
meth public abstract java.lang.String getValueClass()
meth public abstract void setValueClass(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig
fld public final static java.lang.String DEFAULT_LOCALE
fld public final static java.lang.String SUPPORTED_LOCALE
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale> getSupportedLocales()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale getDefaultLocale()
meth public abstract void addSupportedLocales(int,org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale)
meth public abstract void addSupportedLocales(org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale)
meth public abstract void removeSupportedLocale(org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale)
meth public abstract void setDefaultLocale(org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean
fld public final static java.lang.String LIST_ENTRIES
fld public final static java.lang.String MANAGED_BEAN_CLASS
fld public final static java.lang.String MANAGED_BEAN_EXTENSION
fld public final static java.lang.String MANAGED_BEAN_SCOPE
fld public final static java.lang.String MANAGED_PROPERTY
fld public final static java.lang.String MAP_ENTRIES
innr public final static !enum Scope
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension> getManagedBeanExtensions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps> getManagedProps()
meth public abstract void addManagedBeanExtension(int,org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension)
meth public abstract void addManagedBeanExtension(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension)
meth public abstract void addManagedBeanProps(int,org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps)
meth public abstract void addManagedBeanProps(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps)
meth public abstract void removeManagedBeanExtension(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension)
meth public abstract void removeManagedBeanProps(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps)
meth public abstract void setEager(java.lang.Boolean)
meth public abstract void setManagedBeanClass(java.lang.String)
meth public abstract void setManagedBeanName(java.lang.String)
meth public abstract void setManagedBeanScope(java.lang.String)
meth public abstract void setManagedBeanScope(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope)

CLSS public final static !enum org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope
 outer org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean
fld public final static org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope APPLICATION
fld public final static org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope NONE
fld public final static org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope REQUEST
fld public final static org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope SESSION
fld public final static org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope VIEW
meth public java.lang.String toString()
meth public static org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope valueOf(java.lang.String)
meth public static org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope[] values()
supr java.lang.Enum<org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope>
hfds scope

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.MapEntries
fld public final static java.lang.String KEY_CLASS
fld public final static java.lang.String VALUE_CLASS
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps
meth public abstract java.lang.String getKeyClass()
meth public abstract java.lang.String getValueClass()
meth public abstract void setKeyClass(java.lang.String)
meth public abstract void setValueClass(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Method
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Name
intf org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrderingElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase
fld public final static java.lang.String FROM_ACTION
fld public final static java.lang.String FROM_OUTCOME
fld public final static java.lang.String IF
fld public final static java.lang.String REDIRECT
fld public final static java.lang.String TO_VIEW_ID
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract boolean isRedirected()
meth public abstract java.lang.String getFromAction()
meth public abstract java.lang.String getFromOutcome()
meth public abstract java.lang.String getToViewId()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.If getIf()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Redirect getRedirect()
meth public abstract void setFromAction(java.lang.String)
meth public abstract void setFromOutcome(java.lang.String)
meth public abstract void setIf(org.netbeans.modules.web.jsf.api.facesmodel.If)
meth public abstract void setRedirect(org.netbeans.modules.web.jsf.api.facesmodel.Redirect)
meth public abstract void setRedirected(boolean)
meth public abstract void setToViewId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule
fld public final static java.lang.String FROM_VIEW_ID
fld public final static java.lang.String NAVIGATION_CASE
fld public final static java.lang.String NAVIGATION_RULE_EXTENSION
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getFromViewId()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase> getNavigationCases()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension> getNavigationRuleExtensions()
meth public abstract void addNavigationCase(int,org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase)
meth public abstract void addNavigationCase(org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase)
meth public abstract void addNavigationRuleExtension(int,org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension)
meth public abstract void addNavigationRuleExtension(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension)
meth public abstract void removeNavigationCase(org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase)
meth public abstract void removeNavigationRuleExtension(org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension)
meth public abstract void setFromViewId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.NavigationRuleExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Ordering
fld public final static java.lang.String AFTER
fld public final static java.lang.String BEFORE
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.After getAfter()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Before getBefore()
meth public abstract void setAfter(org.netbeans.modules.web.jsf.api.facesmodel.After)
meth public abstract void setBefore(org.netbeans.modules.web.jsf.api.facesmodel.Before)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.OrderingElement
fld public final static java.lang.String NAME
fld public final static java.lang.String OTHERS
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Name> getNames()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.Others getOthers()
meth public abstract void addName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public abstract void removeName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
meth public abstract void setOther(org.netbeans.modules.web.jsf.api.facesmodel.Others)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Others
intf org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrderingElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.PhaseListener
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Property
fld public final static java.lang.String PROPERTY_CLASS
fld public final static java.lang.String PROPERTY_NAME
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getPropertyClass()
meth public abstract java.lang.String getPropertyName()
meth public abstract void setPropertyClass(java.lang.String)
meth public abstract void setPropertyName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer
fld public final static java.lang.String PROPERTY
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.Property> getProperties()
meth public abstract void addProperty(int,org.netbeans.modules.web.jsf.api.facesmodel.Property)
meth public abstract void addProperty(org.netbeans.modules.web.jsf.api.facesmodel.Property)
meth public abstract void removePropety(org.netbeans.modules.web.jsf.api.facesmodel.Property)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews
fld public final static java.lang.String PROTECTED_VIEWS
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern> getUrlPatterns()
meth public abstract void addUrlPatterns(org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern)
meth public abstract void removeUrlPatterns(org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Redirect
fld public final static java.lang.String INCLUDE_VIEW_PARAMS
fld public final static java.lang.String VIEW_PARAM
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.Boolean getIncludeViewParams()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ViewParam> getViewParams()
meth public abstract void addViewParam(int,org.netbeans.modules.web.jsf.api.facesmodel.ViewParam)
meth public abstract void addViewParam(org.netbeans.modules.web.jsf.api.facesmodel.ViewParam)
meth public abstract void removeViewParam(org.netbeans.modules.web.jsf.api.facesmodel.ViewParam)
meth public abstract void setIncludeViewParams(java.lang.Boolean)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ReferencebleJSFConfigComponent
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
intf org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean
fld public final static java.lang.String REFERENCED_BEAN_CLASS
fld public final static java.lang.String REFERENCED_BEAN_NAME
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getReferencedBeanClass()
meth public abstract java.lang.String getReferencedBeanName()
meth public abstract void setReferencedBeanClass(java.lang.String)
meth public abstract void setReferencedBeanName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.RenderKit
fld public final static java.lang.String CLIENT_BEHAVIOR_RENDERER
fld public final static java.lang.String RENDERER
fld public final static java.lang.String RENDER_KIT_CLASS
fld public final static java.lang.String RENDER_KIT_EXTENSION
fld public final static java.lang.String RENDER_KIT_ID
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getRenderKitClass()
meth public abstract java.lang.String getRenderKitId()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension> getRenderKitExtensions()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.metamodel.ClientBehaviorRenderer> getClientBehaviorRenderers()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.metamodel.Renderer> getRenderers()
meth public abstract void addClientBehaviorRenderer(int,org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer)
meth public abstract void addClientBehaviorRenderer(org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer)
meth public abstract void addRenderKitExtension(int,org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension)
meth public abstract void addRenderKitExtension(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension)
meth public abstract void addRenderer(int,org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer)
meth public abstract void addRenderer(org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer)
meth public abstract void removeRenderKitExtension(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension)
meth public abstract void removeRenderer(org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer)
meth public abstract void removeaddClientBehaviorRenderer(org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer)
meth public abstract void setRenderKitClass(java.lang.String)
meth public abstract void setRenderKitId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.RenderKitExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle
fld public final static java.lang.String BASE_NAME
fld public final static java.lang.String VAR
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getBaseName()
meth public abstract java.lang.String getVar()
meth public abstract void setBaseName(java.lang.String)
meth public abstract void setVar(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts
fld public final static java.lang.String CONTRACT_MAPPING
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.DescriptionGroup
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping> getContractMappings()
meth public abstract void addContractMapping(org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping)
meth public abstract void removeContractMapping(org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.StateManager
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.lang.String getLocale()
meth public abstract void setLocale(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent
fld public final static java.lang.String TEXT = "text"
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent
meth public abstract java.lang.String getText()
meth public abstract void setText(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern
fld public final static java.lang.String URL_PATTERN
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ValidatorExtension
intf org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ValidatorId
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.Value
intf org.netbeans.modules.web.jsf.api.facesmodel.TextJsfComponent

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler
fld public final static java.lang.String VIEW_HANDLER
intf org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.ViewParam
fld public final static java.lang.String NAME
fld public final static java.lang.String VALUE
intf org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory
intf org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement
intf org.netbeans.modules.web.jsf.api.facesmodel.FullyQualifiedClassType

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.Behavior
fld public final static java.lang.String BEHAVIOR_CLASS
fld public final static java.lang.String BEHAVIOR_ID
intf org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement
meth public abstract java.lang.String getBehaviorClass()
meth public abstract java.lang.String getBehaviorId()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.ClientBehaviorRenderer
fld public final static java.lang.String CLIENT_BEHAVIOR_RENDERER_CLASS
fld public final static java.lang.String CLIENT_BEHAVIOR_RENDERER_TYPE
meth public abstract java.lang.String getClientBehaviorRendererClass()
meth public abstract java.lang.String getClientBehaviorRendererType()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.Component
fld public final static java.lang.String COMPONENT_CLASS
fld public final static java.lang.String COMPONENT_TYPE
intf org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement
meth public abstract java.lang.String getComponentClass()
meth public abstract java.lang.String getComponentType()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.FacesConverter
intf org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement
meth public abstract java.lang.String getConverterClass()
meth public abstract java.lang.String getConverterForClass()
meth public abstract java.lang.String getConverterId()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean
fld public final static java.lang.String EAGER = "eager"
fld public final static java.lang.String MANAGED_BEAN_NAME
intf org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement
meth public abstract java.lang.Boolean getEager()
meth public abstract java.lang.String getManagedBeanClass()
meth public abstract java.lang.String getManagedBeanName()
meth public abstract java.lang.String getManagedBeanScopeString()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty> getManagedProperties()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean$Scope getManagedBeanScope()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.JsfModel
meth public abstract <%0 extends org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement> java.util.List<{%%0}> getElements(java.lang.Class<{%%0}>)
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig> getFacesConfigs()
meth public abstract java.util.List<org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel> getModels()
meth public abstract org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig getMainConfig()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.JsfModelProvider
meth public abstract org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.web.jsf.api.metamodel.JsfModel> getModel()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty
fld public final static java.lang.String PROPERT_CLASS
fld public final static java.lang.String PROPERT_NAME
meth public abstract java.lang.String getPropertyClass()
meth public abstract java.lang.String getPropertyName()

CLSS public org.netbeans.modules.web.jsf.api.metamodel.ModelUnit
fld public final java.lang.String PROP_CONFIG_FILES = "configFiles"
intf java.beans.PropertyChangeListener
intf org.openide.filesystems.FileChangeListener
meth public java.util.List<org.openide.filesystems.FileObject> getApplicationConfigurationResources()
meth public org.netbeans.api.java.classpath.ClassPath getBootPath()
meth public org.netbeans.api.java.classpath.ClassPath getCompilePath()
meth public org.netbeans.api.java.classpath.ClassPath getSourcePath()
meth public org.openide.filesystems.FileObject getApplicationFacesConfig()
meth public static org.netbeans.modules.web.jsf.api.metamodel.ModelUnit create(org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.project.Project)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void fileChanged(org.openide.filesystems.FileEvent)
meth public void fileDataCreated(org.openide.filesystems.FileEvent)
meth public void fileDeleted(org.openide.filesystems.FileEvent)
meth public void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void fileRenamed(org.openide.filesystems.FileRenameEvent)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds DEFAULT_FACES_CONFIG_PATH,FACES_CONFIG,FACES_CONFIG_SUFFIX,LOGGER,META_INF,RP,bootPath,changeSupport,compilePath,configFiles,configRoots,projectRef,sourcePath

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.Renderer
fld public final static java.lang.String RENDERER_CLASS
fld public final static java.lang.String RENDERER_TYPE
meth public abstract java.lang.String getComponentFamily()
meth public abstract java.lang.String getRendererClass()
meth public abstract java.lang.String getRendererType()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener
fld public final static java.lang.String SOURCE_CLASS
fld public final static java.lang.String SYSTEM_EVENT_CLASS
fld public final static java.lang.String SYSTEM_EVENT_LISTENER_CLASS
meth public abstract java.lang.String getSourceClass()
meth public abstract java.lang.String getSystemEventClass()
meth public abstract java.lang.String getSystemEventListenerClass()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.Validator
fld public final static java.lang.String VALIDATOR_CLASS
fld public final static java.lang.String VALIDATOR_ID
intf org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement
meth public abstract java.lang.String getValidatorClass()
meth public abstract java.lang.String getValidatorId()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.metamodel.ValidatorId
meth public abstract java.lang.String getText()

CLSS public abstract interface org.netbeans.modules.web.jsf.api.palette.PaletteItem
meth public abstract java.lang.String getDisplayName()
meth public abstract void insert(javax.swing.text.JTextComponent)

CLSS public final org.netbeans.modules.web.jsf.api.palette.PaletteItemsProvider
cons public init()
meth public final static java.util.Collection<org.netbeans.modules.web.jsf.api.palette.PaletteItem> getPaletteItems()
supr java.lang.Object
hfds ITEMS

CLSS public abstract interface org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer
intf org.openide.util.HelpCtx$Provider
meth public abstract boolean isValid()
meth public abstract java.lang.String getErrorMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getWarningMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract javax.swing.JComponent getComponent()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void saveConfiguration()

CLSS public abstract interface org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation
meth public abstract boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.web.jsfapi.api.JsfVersion> getJsfVersion()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.openide.filesystems.FileObject> extend(org.netbeans.modules.web.api.webmodule.WebModule,org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer createJsfComponentCustomizer(org.netbeans.modules.web.api.webmodule.WebModule)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void remove(org.netbeans.modules.web.api.webmodule.WebModule)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.web.jsf.spi.components.JsfComponentProvider
fld public final static java.lang.String COMPONENTS_PATH = "j2ee/jsf/components"
meth public abstract java.util.Collection<org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation> getJsfComponents()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.Library
intf org.netbeans.modules.web.jsfapi.api.LibraryInfo
meth public abstract java.lang.String getDefaultNamespace()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Collection<? extends org.netbeans.modules.web.jsfapi.api.LibraryComponent> getComponents()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.jsfapi.api.LibraryComponent getComponent(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.web.jsfapi.api.LibraryType getType()
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.LibraryComponent
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String[][] getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.jsfapi.api.Library getLibrary()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.jsfapi.api.Tag getTag()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.LibraryInfo
meth public abstract java.lang.String getDefaultPrefix()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getNamespace()
meth public abstract java.util.Set<java.lang.String> getValidNamespaces()

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModelFactory<%0 extends org.netbeans.modules.xml.xam.Model>
cons public init()
fld public final static int DELAY_DIRTY = 1000
fld public final static int DELAY_SYNCER = 2000
fld public final static java.lang.String MODEL_LOADED_PROPERTY = "modelLoaded"
meth protected abstract {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createModel(org.netbeans.modules.xml.xam.ModelSource)
meth protected java.lang.Object getKey(org.netbeans.modules.xml.xam.ModelSource)
meth protected {org.netbeans.modules.xml.xam.AbstractModelFactory%0} getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractModelFactory%0}> getModels()
meth public static org.netbeans.modules.xml.xam.spi.ModelAccessProvider getAccessProvider()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createFreshModel(org.netbeans.modules.xml.xam.ModelSource)
supr java.lang.Object
hfds LOG,SYNCER,cachedModels,factories,propSupport

CLSS public abstract interface org.netbeans.modules.xml.xam.Component<%0 extends org.netbeans.modules.xml.xam.Component>
meth public abstract <%0 extends {org.netbeans.modules.xml.xam.Component%0}> java.util.List<{%%0}> getChildren(java.lang.Class<{%%0}>)
meth public abstract boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren()
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren(java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.Component%0}>>)
meth public abstract org.netbeans.modules.xml.xam.Component copy({org.netbeans.modules.xml.xam.Component%0})
meth public abstract org.netbeans.modules.xml.xam.Model getModel()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract {org.netbeans.modules.xml.xam.Component%0} getParent()

CLSS public abstract interface org.netbeans.modules.xml.xam.Model<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Model%0}>>
fld public final static java.lang.String STATE_PROPERTY = "state"
innr public final static !enum State
intf org.netbeans.modules.xml.xam.Referenceable
meth public abstract boolean inSync()
meth public abstract boolean isIntransaction()
meth public abstract boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public abstract org.netbeans.modules.xml.xam.Model$State getState()
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public abstract void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public abstract void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void endTransaction()
meth public abstract void removeChildComponent(org.netbeans.modules.xml.xam.Component)
meth public abstract void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void sync() throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentComponent<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent>
fld public final static java.lang.String TEXT_CONTENT_PROPERTY = "textContent"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.dom.DocumentComponent%0}>
meth public abstract boolean isInDocumentModel()
meth public abstract boolean referencesSameNode(org.w3c.dom.Node)
meth public abstract int findAttributePosition(java.lang.String)
meth public abstract int findPosition()
meth public abstract java.lang.String getAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
meth public abstract org.w3c.dom.Element getPeer()
meth public abstract void setAttribute(java.lang.String,org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentComponent%0} findChildComponent(org.w3c.dom.Element)

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>>
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public abstract org.w3c.dom.Document getDocument()
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} createComponent({org.netbeans.modules.xml.xam.dom.DocumentModel%0},org.w3c.dom.Element)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} getRootComponent()

CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileRenamed(org.openide.filesystems.FileRenameEvent)

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

