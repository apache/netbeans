#Signature file v4.1
#Version 1.61

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

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorDisplayer
fld public final static java.lang.String PROP_PANEL_SELECTION = "navigatorPanelSelection"
meth public abstract boolean allowAsyncUpdate()
meth public abstract org.netbeans.spi.navigator.NavigatorPanel getSelectedPanel()
meth public abstract org.openide.windows.TopComponent getTopComponent()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setDisplayName(java.lang.String)
meth public abstract void setPanels(java.util.List<? extends org.netbeans.spi.navigator.NavigatorPanel>,org.netbeans.spi.navigator.NavigatorPanel)
meth public abstract void setSelectedPanel(org.netbeans.spi.navigator.NavigatorPanel)

CLSS public final org.netbeans.spi.navigator.NavigatorHandler
meth public static void activateNavigator()
meth public static void activatePanel(org.netbeans.spi.navigator.NavigatorPanel)
supr java.lang.Object
hfds controller

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorLookupHint
meth public abstract java.lang.String getContentType()

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy
fld public final static int LOOKUP_HINTS_ONLY = 1
meth public abstract int getPanelsPolicy()

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorPanel
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
innr public abstract interface static DynamicRegistration
meth public abstract java.lang.String getDisplayHint()
meth public abstract java.lang.String getDisplayName()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void panelActivated(org.openide.util.Lookup)
meth public abstract void panelDeactivated()

CLSS public abstract interface static org.netbeans.spi.navigator.NavigatorPanel$DynamicRegistration
 outer org.netbeans.spi.navigator.NavigatorPanel
meth public abstract java.util.Collection<? extends org.netbeans.spi.navigator.NavigatorPanel> panelsFor(java.net.URI)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static !annotation org.netbeans.spi.navigator.NavigatorPanel$Registration
 outer org.netbeans.spi.navigator.NavigatorPanel
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String mimeType()

CLSS public abstract interface static !annotation org.netbeans.spi.navigator.NavigatorPanel$Registrations
 outer org.netbeans.spi.navigator.NavigatorPanel
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.spi.navigator.NavigatorPanel$Registration[] value()

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorPanelWithToolbar
intf org.netbeans.spi.navigator.NavigatorPanel
meth public abstract javax.swing.JComponent getToolbarComponent()

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorPanelWithUndo
intf org.netbeans.spi.navigator.NavigatorPanel
meth public abstract org.openide.awt.UndoRedo getUndoRedo()

