#Signature file v4.1
#Version 1.63

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

CLSS public final org.netbeans.modules.groovy.support.api.GroovyExtender
meth public static boolean activate(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean deactivate(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isActive(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.modules.groovy.support.api.GroovySettings
fld public final static java.lang.String GROOVY_DOC_PROPERTY = "groovy.doc"
fld public final static java.lang.String GROOVY_OPTIONS_CATEGORY = "Advanced/org-netbeans-modules-groovy-support-api-GroovySettings"
meth public boolean isHonourAccessModifiers()
meth public java.lang.String getDisplayName()
meth public java.lang.String getGroovyDoc()
meth public java.lang.String getTooltip()
meth public org.netbeans.spi.options.OptionsPanelController create()
meth public static org.netbeans.modules.groovy.support.api.GroovySettings getInstance()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setGroovyDoc(java.lang.String)
meth public void setHonourAccessModifiers(boolean)
supr org.netbeans.spi.options.AdvancedOption
hfds ACCESS_MODIFIERS,DEFAULT_ACCESS_MODIFIERS,GROOVY_DOC,instance,propertyChangeSupport

CLSS public org.netbeans.modules.groovy.support.api.GroovySources
cons public init()
fld public final static java.lang.String GROOVY_FILE_ICON_16x16 = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png"
fld public final static java.lang.String SOURCES_TYPE_GRAILS = "grails"
fld public final static java.lang.String SOURCES_TYPE_GRAILS_UNKNOWN = "grails_unknown"
fld public final static java.lang.String SOURCES_TYPE_GROOVY = "groovy"
meth public static java.util.List<org.netbeans.api.project.SourceGroup> getGroovySourceGroups(org.netbeans.api.project.Sources)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.groovy.support.spi.GroovyExtenderImplementation
meth public abstract boolean activate()
meth public abstract boolean deactivate()
meth public abstract boolean isActive()

CLSS public abstract interface org.netbeans.modules.groovy.support.spi.GroovyOptionsSubpanel
meth public abstract boolean changed()
meth public abstract boolean valid()
meth public abstract java.awt.Component getComponent()
meth public abstract void load()
meth public abstract void store()

CLSS public abstract org.netbeans.spi.options.AdvancedOption
cons protected init()
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getTooltip()
meth public abstract org.netbeans.spi.options.OptionsPanelController create()
supr java.lang.Object
hfds CONTROLLER,DISPLAYNAME,KEYWORDS,KEYWORDS_CATEGORY,TOOLTIP

