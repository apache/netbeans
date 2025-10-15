#Signature file v4.1
#Version 0.6

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
innr public final static EnumDesc
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
intf java.lang.constant.Constable
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public final java.util.Optional<java.lang.Enum$EnumDesc<{java.lang.Enum%0}>> describeConstable()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface java.lang.constant.Constable
meth public abstract java.util.Optional<? extends java.lang.constant.ConstantDesc> describeConstable()

CLSS public final org.netbeans.api.dashboard.DashboardManager
cons public init()
meth public static org.netbeans.api.dashboard.DashboardManager getDefault()
meth public void show()
supr java.lang.Object
hfds INSTANCE,mainDisplayer,mainWidgets

CLSS public abstract interface org.netbeans.spi.dashboard.DashboardDisplayer
innr public abstract interface static Panel
innr public final static WidgetReference
intf org.openide.util.Lookup$Provider
meth public abstract void show(java.lang.String,java.util.List<org.netbeans.spi.dashboard.DashboardDisplayer$WidgetReference>)

CLSS public abstract interface static org.netbeans.spi.dashboard.DashboardDisplayer$Panel
 outer org.netbeans.spi.dashboard.DashboardDisplayer
intf org.openide.util.Lookup$Provider
meth public abstract java.lang.String id()
meth public abstract org.netbeans.spi.dashboard.DashboardDisplayer$WidgetReference widgetReference()
meth public abstract void refresh()

CLSS public final static org.netbeans.spi.dashboard.DashboardDisplayer$WidgetReference
 outer org.netbeans.spi.dashboard.DashboardDisplayer
cons public init(java.lang.String,org.netbeans.spi.dashboard.DashboardWidget)
cons public init(java.lang.String,org.netbeans.spi.dashboard.DashboardWidget,org.openide.filesystems.FileObject)
intf org.openide.util.Lookup$Provider
meth public java.lang.String id()
meth public org.netbeans.spi.dashboard.DashboardWidget widget()
meth public org.openide.util.Lookup getLookup()
supr java.lang.Object
hfds id,lookup,widget

CLSS public abstract interface org.netbeans.spi.dashboard.DashboardWidget
meth public abstract java.lang.String title(org.netbeans.spi.dashboard.DashboardDisplayer$Panel)
meth public abstract java.util.List<org.netbeans.spi.dashboard.WidgetElement> elements(org.netbeans.spi.dashboard.DashboardDisplayer$Panel)
meth public void attach(org.netbeans.spi.dashboard.DashboardDisplayer$Panel)
meth public void hidden(org.netbeans.spi.dashboard.DashboardDisplayer$Panel)
meth public void removed(java.lang.String)
meth public void showing(org.netbeans.spi.dashboard.DashboardDisplayer$Panel)

CLSS public abstract org.netbeans.spi.dashboard.WidgetElement
innr public final static ActionElement
innr public final static ComponentElement
innr public final static ImageElement
innr public final static LinkElement
innr public final static SeparatorElement
innr public final static TextElement
meth public static org.netbeans.spi.dashboard.WidgetElement$ActionElement action(javax.swing.Action)
meth public static org.netbeans.spi.dashboard.WidgetElement$ActionElement actionLink(javax.swing.Action)
meth public static org.netbeans.spi.dashboard.WidgetElement$ActionElement actionLinkNoIcon(javax.swing.Action)
meth public static org.netbeans.spi.dashboard.WidgetElement$ActionElement actionNoIcon(javax.swing.Action)
meth public static org.netbeans.spi.dashboard.WidgetElement$ComponentElement component(java.util.function.Supplier<javax.swing.JComponent>)
meth public static org.netbeans.spi.dashboard.WidgetElement$ImageElement image(java.lang.String)
meth public static org.netbeans.spi.dashboard.WidgetElement$LinkElement link(java.lang.String,java.net.URI)
meth public static org.netbeans.spi.dashboard.WidgetElement$LinkElement linkButton(java.lang.String,java.net.URI)
meth public static org.netbeans.spi.dashboard.WidgetElement$SeparatorElement separator()
meth public static org.netbeans.spi.dashboard.WidgetElement$TextElement aside(java.lang.String)
meth public static org.netbeans.spi.dashboard.WidgetElement$TextElement subheading(java.lang.String)
meth public static org.netbeans.spi.dashboard.WidgetElement$TextElement text(java.lang.String)
meth public static org.netbeans.spi.dashboard.WidgetElement$TextElement unavailable(java.lang.String)
supr java.lang.Object

CLSS public final static org.netbeans.spi.dashboard.WidgetElement$ActionElement
 outer org.netbeans.spi.dashboard.WidgetElement
meth public boolean asLink()
meth public boolean equals(java.lang.Object)
meth public boolean showIcon()
meth public int hashCode()
meth public java.lang.String toString()
meth public javax.swing.Action action()
supr org.netbeans.spi.dashboard.WidgetElement
hfds action,icon,link

CLSS public final static org.netbeans.spi.dashboard.WidgetElement$ComponentElement
 outer org.netbeans.spi.dashboard.WidgetElement
meth public java.lang.String toString()
meth public java.util.function.Supplier<javax.swing.JComponent> componentSupplier()
meth public javax.swing.JComponent component()
supr org.netbeans.spi.dashboard.WidgetElement
hfds componentSupplier

CLSS public final static org.netbeans.spi.dashboard.WidgetElement$ImageElement
 outer org.netbeans.spi.dashboard.WidgetElement
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String resourcePath()
meth public java.lang.String toString()
supr org.netbeans.spi.dashboard.WidgetElement
hfds resourcePath

CLSS public final static org.netbeans.spi.dashboard.WidgetElement$LinkElement
 outer org.netbeans.spi.dashboard.WidgetElement
meth public boolean asButton()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String text()
meth public java.lang.String toString()
meth public java.net.URI link()
supr org.netbeans.spi.dashboard.WidgetElement
hfds button,link,text

CLSS public final static org.netbeans.spi.dashboard.WidgetElement$SeparatorElement
 outer org.netbeans.spi.dashboard.WidgetElement
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr org.netbeans.spi.dashboard.WidgetElement

CLSS public final static org.netbeans.spi.dashboard.WidgetElement$TextElement
 outer org.netbeans.spi.dashboard.WidgetElement
innr public final static !enum Kind
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String text()
meth public java.lang.String toString()
meth public org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind kind()
supr org.netbeans.spi.dashboard.WidgetElement
hfds kind,text

CLSS public final static !enum org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind
 outer org.netbeans.spi.dashboard.WidgetElement$TextElement
fld public final static org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind ASIDE
fld public final static org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind NORMAL
fld public final static org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind SUBHEADING
fld public final static org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind UNAVAILABLE
meth public static org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind valueOf(java.lang.String)
meth public static org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind[] values()
supr java.lang.Enum<org.netbeans.spi.dashboard.WidgetElement$TextElement$Kind>

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

