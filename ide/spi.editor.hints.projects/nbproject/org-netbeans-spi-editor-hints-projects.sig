#Signature file v4.1
#Version 1.34.0

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

CLSS public org.netbeans.spi.editor.hints.projects.PerProjectHintsPanel
innr public abstract interface static MimeType2Preferences
meth public javax.swing.JComponent getPanel()
meth public static org.netbeans.spi.editor.hints.projects.PerProjectHintsPanel create(org.openide.filesystems.FileObject)
meth public void applyChanges()
meth public void setGlobalSettings()
meth public void setPerProjectSettings(org.netbeans.modules.editor.tools.storage.api.ToolPreferences)
meth public void setPerProjectSettings(org.netbeans.spi.editor.hints.projects.PerProjectHintsPanel$MimeType2Preferences)
supr java.lang.Object
hfds panel

CLSS public abstract interface static org.netbeans.spi.editor.hints.projects.PerProjectHintsPanel$MimeType2Preferences
 outer org.netbeans.spi.editor.hints.projects.PerProjectHintsPanel
meth public abstract java.util.prefs.Preferences getPreferences(java.lang.String)

CLSS public abstract interface org.netbeans.spi.editor.hints.projects.ProjectSettings
fld public final static java.lang.String HINTS_TOOL_ID = "hints"
meth public abstract boolean getUseProjectSettings()
meth public abstract java.util.prefs.Preferences getProjectSettings(java.lang.String)

CLSS public org.netbeans.spi.editor.hints.projects.support.StandardProjectSettings
cons public init()
fld public final static java.lang.String ATTR_CUSTOMIZERS_FOLDER = "customizersFolder"
fld public final static java.lang.String ATTR_DEFAULT_HINT_FILE = "defaultHintFileKey"
fld public final static java.lang.String ATTR_ENABLE_KEY = "enableKey"
fld public final static java.lang.String ATTR_HINT_FILE_KEY = "hintFileKey"
meth public static org.netbeans.spi.project.LookupProvider createPreferencesBasedSettings()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.LookupProvider createSettings(java.lang.String,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.spi.project.LookupProvider createSettings(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider createCustomizerProvider(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider createCustomizerProvider(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DEF_PROJECT_SETTINGS,DEF_USE_PROJECT,KEY_PROJECT_SETTINGS,KEY_USE_PROJECT
hcls Standard

