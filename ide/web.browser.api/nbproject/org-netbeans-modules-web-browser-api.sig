#Signature file v4.1
#Version 1.68

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

CLSS public abstract javax.swing.AbstractListModel<%0 extends java.lang.Object>
cons public init()
fld protected javax.swing.event.EventListenerList listenerList
intf java.io.Serializable
intf javax.swing.ListModel<{javax.swing.AbstractListModel%0}>
meth protected void fireContentsChanged(java.lang.Object,int,int)
meth protected void fireIntervalAdded(java.lang.Object,int,int)
meth protected void fireIntervalRemoved(java.lang.Object,int,int)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public javax.swing.event.ListDataListener[] getListDataListeners()
meth public void addListDataListener(javax.swing.event.ListDataListener)
meth public void removeListDataListener(javax.swing.event.ListDataListener)
supr java.lang.Object

CLSS public abstract interface javax.swing.ComboBoxModel<%0 extends java.lang.Object>
intf javax.swing.ListModel<{javax.swing.ComboBoxModel%0}>
meth public abstract java.lang.Object getSelectedItem()
meth public abstract void setSelectedItem(java.lang.Object)

CLSS public abstract interface javax.swing.ListModel<%0 extends java.lang.Object>
meth public abstract int getSize()
meth public abstract void addListDataListener(javax.swing.event.ListDataListener)
meth public abstract void removeListDataListener(javax.swing.event.ListDataListener)
meth public abstract {javax.swing.ListModel%0} getElementAt(int)

CLSS public final !enum org.netbeans.modules.web.browser.api.BrowserFamilyId
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId ANDROID
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId CHROME
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId CHROMIUM
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId EDGE
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId FIREFOX
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId IE
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId IOS
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId JAVAFX_WEBVIEW
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId MOZILLA
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId OPERA
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId PHONEGAP
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId SAFARI
fld public final static org.netbeans.modules.web.browser.api.BrowserFamilyId UNKNOWN
meth public boolean isMobile()
meth public static org.netbeans.modules.web.browser.api.BrowserFamilyId valueOf(java.lang.String)
meth public static org.netbeans.modules.web.browser.api.BrowserFamilyId[] values()
supr java.lang.Enum<org.netbeans.modules.web.browser.api.BrowserFamilyId>

CLSS public final org.netbeans.modules.web.browser.api.BrowserPickerPopup
meth public org.netbeans.modules.web.browser.api.WebBrowser getSelectedBrowser()
meth public static org.netbeans.modules.web.browser.api.BrowserPickerPopup create(org.netbeans.modules.web.browser.spi.ProjectBrowserProvider)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void show(javax.swing.JComponent,int,int)
supr java.lang.Object
hfds menu

CLSS public final org.netbeans.modules.web.browser.api.BrowserSupport
meth public boolean canReload()
meth public boolean canReload(java.net.URL)
meth public boolean ignoreChange(org.openide.filesystems.FileObject)
meth public boolean isWebBrowserPaneOpen()
meth public boolean reload()
meth public boolean reload(java.net.URL)
meth public java.net.URL getBrowserURL(org.openide.filesystems.FileObject,boolean)
meth public static boolean ignoreChangeDefaultImpl(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.web.browser.api.BrowserSupport create()
meth public static org.netbeans.modules.web.browser.api.BrowserSupport create(org.netbeans.modules.web.browser.api.WebBrowser)
meth public static org.netbeans.modules.web.browser.api.BrowserSupport getDefault()
meth public static org.netbeans.modules.web.browser.api.BrowserSupport getDefaultEmbedded()
meth public static org.netbeans.modules.web.browser.api.BrowserSupport getGlobalSharedOne()
meth public void close(boolean)
meth public void load(java.net.URL,org.openide.filesystems.FileObject)
supr java.lang.Object
hfds INSTANCE,INSTANCE_EMBEDDED,browser,currentURL,file,listener,pane

CLSS public final org.netbeans.modules.web.browser.api.BrowserUISupport
innr public final static BrowserComboBoxModel
meth public static java.lang.String getLongDisplayName(org.netbeans.modules.web.browser.api.WebBrowser)
meth public static javax.swing.JComboBox createBrowserPickerComboBox(java.lang.String,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static javax.swing.JComboBox createBrowserPickerComboBox(java.lang.String,boolean,boolean,javax.swing.ComboBoxModel)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static javax.swing.ListCellRenderer<org.netbeans.modules.web.browser.api.WebBrowser> createBrowserRenderer()
meth public static org.netbeans.modules.web.browser.api.BrowserUISupport$BrowserComboBoxModel createBrowserModel(java.lang.String,boolean)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.web.browser.api.BrowserUISupport$BrowserComboBoxModel createBrowserModel(java.lang.String,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.web.browser.api.BrowserUISupport$BrowserComboBoxModel createBrowserModel(java.lang.String,java.util.List<org.netbeans.modules.web.browser.api.WebBrowser>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.web.browser.api.WebBrowser getBrowser(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.browser.api.WebBrowser getDefaultBrowserChoice(boolean)
supr java.lang.Object
hfds LOGGER
hcls BrowserRenderer

CLSS public final static org.netbeans.modules.web.browser.api.BrowserUISupport$BrowserComboBoxModel
 outer org.netbeans.modules.web.browser.api.BrowserUISupport
intf javax.swing.ComboBoxModel<org.netbeans.modules.web.browser.api.WebBrowser>
meth public int getSize()
meth public java.lang.String getSelectedBrowserId()
meth public org.netbeans.modules.web.browser.api.WebBrowser getElementAt(int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.web.browser.api.WebBrowser getSelectedBrowser()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.web.browser.api.WebBrowser getSelectedItem()
meth public void setSelectedItem(java.lang.Object)
supr javax.swing.AbstractListModel<org.netbeans.modules.web.browser.api.WebBrowser>
hfds browsers,selectedBrowser,serialVersionUID

CLSS public abstract org.netbeans.modules.web.browser.api.Page
cons public init()
fld public final static java.lang.String PROP_BROWSER_SELECTED_NODES = "browserSelectedNodes"
fld public final static java.lang.String PROP_DOCUMENT = "document"
fld public final static java.lang.String PROP_HIGHLIGHTED_NODES = "highlightedNodes"
fld public final static java.lang.String PROP_SELECTED_NODES = "selectedNodes"
meth public abstract java.lang.String getDocumentURL()
meth public abstract java.util.List<? extends org.openide.nodes.Node> getHighlightedNodes()
meth public abstract java.util.List<? extends org.openide.nodes.Node> getSelectedNodes()
meth public abstract org.openide.nodes.Node getDocumentNode()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setHighlightedNodes(java.util.List<? extends org.openide.nodes.Node>)
meth public abstract void setSelectedNodes(java.util.List<? extends org.openide.nodes.Node>)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.web.browser.api.PageInspector
cons public init()
fld public final static java.lang.String MESSAGE_DISPATCHER_FEATURE_ID = "inspect"
fld public final static java.lang.String PROP_MODEL = "model"
meth public abstract org.netbeans.modules.web.browser.api.Page getPage()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void inspectPage(org.openide.util.Lookup)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static org.netbeans.modules.web.browser.api.PageInspector getDefault()
supr java.lang.Object
hfds DEFAULT

CLSS public final org.netbeans.modules.web.browser.api.ResizeOption
fld public final static org.netbeans.modules.web.browser.api.ResizeOption SIZE_TO_FIT
innr public final static !enum Type
meth public boolean equals(java.lang.Object)
meth public boolean isDefault()
meth public boolean isShowInToolbar()
meth public int getHeight()
meth public int getWidth()
meth public int hashCode()
meth public java.lang.String getDisplayName()
meth public java.lang.String getToolTip()
meth public java.lang.String toString()
meth public org.netbeans.modules.web.browser.api.ResizeOption$Type getType()
meth public static org.netbeans.modules.web.browser.api.ResizeOption create(org.netbeans.modules.web.browser.api.ResizeOption$Type,java.lang.String,int,int,boolean,boolean)
supr java.lang.Object
hfds displayName,height,isDefault,showInToolbar,type,width

CLSS public final static !enum org.netbeans.modules.web.browser.api.ResizeOption$Type
 outer org.netbeans.modules.web.browser.api.ResizeOption
fld public final static org.netbeans.modules.web.browser.api.ResizeOption$Type CUSTOM
fld public final static org.netbeans.modules.web.browser.api.ResizeOption$Type DESKTOP
fld public final static org.netbeans.modules.web.browser.api.ResizeOption$Type NETBOOK
fld public final static org.netbeans.modules.web.browser.api.ResizeOption$Type SMARTPHONE_LANDSCAPE
fld public final static org.netbeans.modules.web.browser.api.ResizeOption$Type SMARTPHONE_PORTRAIT
fld public final static org.netbeans.modules.web.browser.api.ResizeOption$Type TABLET_LANDSCAPE
fld public final static org.netbeans.modules.web.browser.api.ResizeOption$Type TABLET_PORTRAIT
fld public final static org.netbeans.modules.web.browser.api.ResizeOption$Type WIDESCREEN
meth public static org.netbeans.modules.web.browser.api.ResizeOption$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.web.browser.api.ResizeOption$Type[] values()
supr java.lang.Enum<org.netbeans.modules.web.browser.api.ResizeOption$Type>

CLSS public final org.netbeans.modules.web.browser.api.ResizeOptions
meth public java.util.List<org.netbeans.modules.web.browser.api.ResizeOption> loadAll()
meth public static org.netbeans.modules.web.browser.api.ResizeOptions getDefault()
meth public void saveAll(java.util.List<org.netbeans.modules.web.browser.api.ResizeOption>)
supr java.lang.Object
hfds INSTANCE,LOGGER

CLSS public final org.netbeans.modules.web.browser.api.WebBrowser
meth public boolean hasNetBeansIntegration()
meth public boolean isEmbedded()
meth public java.awt.Image getIconImage(boolean)
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.net.URL fromBrowserURL(org.netbeans.api.project.Project,java.net.URL)
meth public java.net.URL toBrowserURL(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.net.URL)
meth public org.netbeans.modules.web.browser.api.BrowserFamilyId getBrowserFamily()
meth public org.netbeans.modules.web.browser.api.WebBrowserPane createNewBrowserPane()
meth public org.netbeans.modules.web.browser.api.WebBrowserPane createNewBrowserPane(boolean)
meth public org.netbeans.modules.web.browser.api.WebBrowserPane createNewBrowserPane(org.netbeans.modules.web.browser.api.WebBrowserFeatures)
meth public org.netbeans.modules.web.browser.api.WebBrowserPane createNewBrowserPane(org.netbeans.modules.web.browser.api.WebBrowserFeatures,boolean)
meth public org.openide.awt.HtmlBrowser$Factory getHtmlBrowserFactory()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds CHROME_LARGE,CHROME_SMALL,CHROMIUM_LARGE,CHROMIUM_SMALL,EDGE_LARGE,EDGE_SMALL,FIREFOX_LARGE,FIREFOX_SMALL,GENERIC_LARGE,GENERIC_SMALL,IE_LARGE,IE_SMALL,RP,SAFARI_LARGE,SAFARI_SMALL,browserMappings,changeSupport,factoryDesc,prefs

CLSS public final org.netbeans.modules.web.browser.api.WebBrowserFeatures
cons public init()
cons public init(boolean,boolean,boolean,boolean,boolean,boolean)
meth public boolean isConsoleLoggerEnabled()
meth public boolean isJsDebuggerEnabled()
meth public boolean isLiveHTMLEnabled()
meth public boolean isNetBeansIntegrationEnabled()
meth public boolean isNetworkMonitorEnabled()
meth public boolean isPageInspectorEnabled()
supr java.lang.Object
hfds consoleLoggerEnabled,jsDebuggerEnabled,liveHTMLEnabled,netBeansIntegrationEnabled,networkMonitorEnabled,pageInspectorEnabled

CLSS public final org.netbeans.modules.web.browser.api.WebBrowserPane
innr public abstract interface static WebBrowserPaneListener
innr public abstract static WebBrowserPaneEvent
innr public final static WebBrowserPaneURLChangedEvent
innr public final static WebBrowserPaneWasClosedEvent
meth public boolean ignoreChange(org.openide.filesystems.FileObject)
meth public boolean isEmbedded()
meth public java.awt.Component getBrowserComponent()
meth public org.openide.util.Lookup getLookup()
meth public void addListener(org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneListener)
meth public void close(boolean)
meth public void reload()
meth public void removeListener(org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneListener)
meth public void setProjectContext(org.openide.util.Lookup)
meth public void showURL(java.net.URL)
supr java.lang.Object
hfds createTopComponent,descriptor,features,impl,lastProjectContext,listener,listeners,open,topComponent,wrapEmbeddedBrowserInTopComponent

CLSS public abstract static org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneEvent
 outer org.netbeans.modules.web.browser.api.WebBrowserPane
meth public org.netbeans.modules.web.browser.api.WebBrowserPane getWebBrowserPane()
supr java.lang.Object
hfds pane

CLSS public abstract interface static org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneListener
 outer org.netbeans.modules.web.browser.api.WebBrowserPane
meth public abstract void browserEvent(org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneEvent)

CLSS public final static org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneURLChangedEvent
 outer org.netbeans.modules.web.browser.api.WebBrowserPane
supr org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneEvent

CLSS public final static org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneWasClosedEvent
 outer org.netbeans.modules.web.browser.api.WebBrowserPane
supr org.netbeans.modules.web.browser.api.WebBrowserPane$WebBrowserPaneEvent

CLSS public final org.netbeans.modules.web.browser.api.WebBrowsers
fld public final static java.lang.String PROP_BROWSERS = "browsers"
fld public final static java.lang.String PROP_DEFAULT_BROWSER = "browser"
meth public java.util.List<org.netbeans.modules.web.browser.api.WebBrowser> getAll(boolean,boolean,boolean)
meth public java.util.List<org.netbeans.modules.web.browser.api.WebBrowser> getAll(boolean,boolean,boolean,boolean)
meth public org.netbeans.modules.web.browser.api.WebBrowser getEmbedded()
meth public org.netbeans.modules.web.browser.api.WebBrowser getPreferred()
meth public static org.netbeans.modules.web.browser.api.WebBrowsers getInstance()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds BROWSERS2_FOLDER,BROWSERS_FOLDER,DEFAULT,INST,l,lis,lis2,sup
hcls BrowserWrapper

CLSS public abstract interface org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation
innr public final static BrowserURLMapper
meth public abstract org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation$BrowserURLMapper toBrowser(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.net.URL)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final static org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation$BrowserURLMapper
 outer org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getBrowserURLRoot()
meth public java.lang.String getServerURLRoot()
meth public void setBrowserURLRoot(java.lang.String)
meth public void setServerURLRoot(java.lang.String)
supr java.lang.Object
hfds browserURLRoot,serverURLRoot

CLSS public abstract interface org.netbeans.modules.web.browser.spi.BrowserURLMapperProvider
meth public abstract org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation getBrowserURLMapper()

CLSS public abstract interface org.netbeans.modules.web.browser.spi.EnhancedBrowser
meth public abstract boolean canReloadPage()
meth public abstract boolean ignoreChange(org.openide.filesystems.FileObject)
meth public abstract void close(boolean)
meth public abstract void initialize(org.netbeans.modules.web.browser.api.WebBrowserFeatures)
meth public abstract void setProjectContext(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory
meth public abstract boolean canCreateHtmlBrowserImpl()
meth public abstract boolean hasNetBeansIntegration()
meth public abstract java.awt.Image getIconImage(boolean)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract org.netbeans.modules.web.browser.api.BrowserFamilyId getBrowserFamilyId()

CLSS public final org.netbeans.modules.web.browser.spi.ExternalModificationsSupport
cons public init()
meth public static void handle(java.lang.String,java.lang.String,java.lang.String,java.net.URL)
supr java.lang.Object

CLSS public org.netbeans.modules.web.browser.spi.MessageDispatcher
cons public init()
innr public abstract interface static MessageListener
meth protected void dispatchMessage(java.lang.String,java.lang.String)
meth public void addMessageListener(org.netbeans.modules.web.browser.spi.MessageDispatcher$MessageListener)
meth public void removeMessageListener(org.netbeans.modules.web.browser.spi.MessageDispatcher$MessageListener)
supr java.lang.Object
hfds listeners

CLSS public abstract interface static org.netbeans.modules.web.browser.spi.MessageDispatcher$MessageListener
 outer org.netbeans.modules.web.browser.spi.MessageDispatcher
meth public abstract void messageReceived(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.PageInspectionHandle
meth public abstract void setSelectionMode(boolean)
meth public abstract void setSynchronizeSelection(boolean)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.PageInspectorCustomizer
fld public final static java.lang.String PROPERTY_HIGHLIGHT_SELECTION = "highlight.selection"
meth public abstract boolean isHighlightSelectionEnabled()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.ProjectBrowserProvider
fld public final static java.lang.String PROP_BROWSERS = "configurations"
fld public final static java.lang.String PROP_BROWSER_ACTIVE = "activeConfiguration"
meth public abstract boolean hasCustomizer()
meth public abstract java.util.Collection<org.netbeans.modules.web.browser.api.WebBrowser> getBrowsers()
meth public abstract org.netbeans.modules.web.browser.api.WebBrowser getActiveBrowser()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void customize()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setActiveBrowser(org.netbeans.modules.web.browser.api.WebBrowser) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.web.browser.spi.Resizable
meth public abstract void autofit()
meth public abstract void resize(int,int)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.ScriptExecutor
fld public final static java.lang.Object ERROR_RESULT
meth public abstract java.lang.Object execute(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.URLDisplayerImplementation
meth public abstract void showURL(java.net.URL,java.net.URL,org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.web.browser.spi.Zoomable
meth public abstract void zoom(double)

