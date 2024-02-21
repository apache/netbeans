#Signature file v4.1
#Version 1.60

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

CLSS public org.netbeans.core.options.keymap.api.KeyStrokeUtils
cons public init()
meth public static java.lang.String getKeyStrokeAsText(javax.swing.KeyStroke)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getKeyStrokesAsText(javax.swing.KeyStroke[],java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.List<javax.swing.KeyStroke[]> getKeyStrokesForAction(java.lang.String,javax.swing.KeyStroke)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static javax.swing.KeyStroke getKeyStroke(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static javax.swing.KeyStroke[] getKeyStrokes(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void refreshActionCache()
supr java.lang.Object
hfds EMACS_ALT,EMACS_CTRL,EMACS_META,EMACS_SHIFT,LOG,STRING_ALT,STRING_META

CLSS public abstract interface org.netbeans.core.options.keymap.api.ShortcutAction
meth public abstract java.lang.String getDelegatingActionId()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract org.netbeans.core.options.keymap.api.ShortcutAction getKeymapManagerInstance(java.lang.String)

CLSS public abstract interface org.netbeans.core.options.keymap.api.ShortcutsFinder
innr public abstract interface static Writer
meth public abstract java.lang.String showShortcutsDialog()
meth public abstract java.lang.String[] getShortcuts(org.netbeans.core.options.keymap.api.ShortcutAction)
meth public abstract org.netbeans.core.options.keymap.api.ShortcutAction findActionForId(java.lang.String)
meth public abstract org.netbeans.core.options.keymap.api.ShortcutAction findActionForShortcut(java.lang.String)
meth public abstract org.netbeans.core.options.keymap.api.ShortcutsFinder$Writer localCopy()
meth public abstract void apply()
 anno 0 java.lang.Deprecated()
meth public abstract void refreshActions()
meth public abstract void setShortcuts(org.netbeans.core.options.keymap.api.ShortcutAction,java.util.Set<java.lang.String>)
 anno 0 java.lang.Deprecated()

CLSS public abstract interface static org.netbeans.core.options.keymap.api.ShortcutsFinder$Writer
 outer org.netbeans.core.options.keymap.api.ShortcutsFinder
intf org.netbeans.core.options.keymap.api.ShortcutsFinder
meth public abstract void apply()
meth public abstract void setShortcuts(org.netbeans.core.options.keymap.api.ShortcutAction,java.util.Set<java.lang.String>)

CLSS public abstract org.netbeans.core.options.keymap.spi.KeymapManager
cons protected init(java.lang.String)
innr public abstract interface static WithRevert
meth public abstract boolean isCustomProfile(java.lang.String)
meth public abstract java.lang.String getCurrentProfile()
meth public abstract java.util.List<java.lang.String> getProfiles()
meth public abstract java.util.Map<java.lang.String,java.util.Set<org.netbeans.core.options.keymap.api.ShortcutAction>> getActions()
meth public abstract java.util.Map<org.netbeans.core.options.keymap.api.ShortcutAction,java.util.Set<java.lang.String>> getDefaultKeymap(java.lang.String)
meth public abstract java.util.Map<org.netbeans.core.options.keymap.api.ShortcutAction,java.util.Set<java.lang.String>> getKeymap(java.lang.String)
meth public abstract void deleteProfile(java.lang.String)
meth public abstract void refreshActions()
meth public abstract void saveKeymap(java.lang.String,java.util.Map<org.netbeans.core.options.keymap.api.ShortcutAction,java.util.Set<java.lang.String>>)
meth public abstract void setCurrentProfile(java.lang.String)
meth public final java.lang.String getName()
meth public java.lang.String getProfileDisplayName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public abstract interface static org.netbeans.core.options.keymap.spi.KeymapManager$WithRevert
 outer org.netbeans.core.options.keymap.spi.KeymapManager
meth public abstract void revertActions(java.lang.String,java.util.Collection<org.netbeans.core.options.keymap.api.ShortcutAction>) throws java.io.IOException
meth public abstract void revertProfile(java.lang.String) throws java.io.IOException

