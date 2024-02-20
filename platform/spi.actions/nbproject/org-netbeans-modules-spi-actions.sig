#Signature file v4.1
#Version 1.51

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

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

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public abstract org.netbeans.spi.actions.ContextAction<%0 extends java.lang.Object>
cons protected init(java.lang.Class<{org.netbeans.spi.actions.ContextAction%0}>)
cons protected init(java.lang.Class<{org.netbeans.spi.actions.ContextAction%0}>,java.lang.String,java.awt.Image)
meth protected abstract void actionPerformed(java.util.Collection<? extends {org.netbeans.spi.actions.ContextAction%0}>)
meth protected boolean checkQuantity(int)
meth protected boolean isEnabled(java.util.Collection<? extends {org.netbeans.spi.actions.ContextAction%0}>)
meth protected final org.netbeans.spi.actions.NbAction internalCreateContextAwareInstance(org.openide.util.Lookup)
meth protected final void refresh()
meth protected void change(java.util.Collection<? extends {org.netbeans.spi.actions.ContextAction%0}>,javax.swing.Action)
meth public boolean equals(java.lang.Object)
meth public final boolean isEnabled()
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public int hashCode()
meth public java.lang.String toString()
meth public static <%0 extends org.openide.util.Lookup$Provider, %1 extends java.lang.Object> org.netbeans.spi.actions.ContextAction<{%%0}> createIndirectAction(java.lang.Class<{%%0}>,org.netbeans.spi.actions.ContextAction<{%%1}>)
meth public static <%0 extends org.openide.util.Lookup$Provider, %1 extends java.lang.Object> org.netbeans.spi.actions.ContextAction<{%%0}> createIndirectAction(java.lang.Class<{%%0}>,org.netbeans.spi.actions.ContextAction<{%%1}>,boolean)
supr org.netbeans.spi.actions.NbAction
hfds stub,stubListener,type,unitTest
hcls StubListener

CLSS public abstract org.netbeans.spi.actions.LookupProviderAction<%0 extends org.openide.util.Lookup$Provider, %1 extends java.lang.Object>
cons protected init(java.lang.Class<{org.netbeans.spi.actions.LookupProviderAction%0}>,java.lang.Class<{org.netbeans.spi.actions.LookupProviderAction%1}>,boolean)
cons protected init(java.lang.Class<{org.netbeans.spi.actions.LookupProviderAction%0}>,java.lang.Class<{org.netbeans.spi.actions.LookupProviderAction%1}>,boolean,java.lang.String,java.awt.Image)
meth protected abstract void perform(java.util.Collection<? extends {org.netbeans.spi.actions.LookupProviderAction%1}>)
meth protected boolean checkQuantity(int)
meth protected boolean enabled(java.util.Collection<? extends {org.netbeans.spi.actions.LookupProviderAction%1}>)
meth protected final boolean isEnabled(java.util.Collection<? extends {org.netbeans.spi.actions.LookupProviderAction%0}>)
meth protected final void actionPerformed(java.util.Collection<? extends {org.netbeans.spi.actions.LookupProviderAction%0}>)
supr org.netbeans.spi.actions.ContextAction<{org.netbeans.spi.actions.LookupProviderAction%0}>
hfds all,delegateType,inDelegate,overDelegateType
hcls InternalDelegateAction

CLSS public abstract org.netbeans.spi.actions.NbAction
cons public init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
intf org.openide.util.ContextAwareAction
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected org.netbeans.spi.actions.NbAction internalCreateContextAwareInstance(org.openide.util.Lookup)
meth protected void addNotify()
meth protected void removeNotify()
meth public !varargs static org.netbeans.spi.actions.NbAction merge(boolean,org.netbeans.spi.actions.NbAction[])
meth public !varargs static org.netbeans.spi.actions.NbAction merge(org.netbeans.spi.actions.NbAction[])
meth public final javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void setEnabled(boolean)
meth public java.lang.Object getValue(java.lang.String)
meth public void putValue(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds STATE_LOCK,attached,pairs,supp
hcls ActionRunnable

CLSS public abstract org.netbeans.spi.actions.Single<%0 extends java.lang.Object>
cons protected init(java.lang.Class<{org.netbeans.spi.actions.Single%0}>)
cons protected init(java.lang.Class<{org.netbeans.spi.actions.Single%0}>,java.lang.String,java.awt.Image)
meth protected abstract void actionPerformed({org.netbeans.spi.actions.Single%0})
meth protected boolean isEnabled({org.netbeans.spi.actions.Single%0})
meth protected final boolean checkQuantity(int)
meth protected final boolean isEnabled(java.util.Collection<? extends {org.netbeans.spi.actions.Single%0}>)
meth protected final void actionPerformed(java.util.Collection<? extends {org.netbeans.spi.actions.Single%0}>)
supr org.netbeans.spi.actions.ContextAction<{org.netbeans.spi.actions.Single%0}>

CLSS public abstract org.netbeans.spi.actions.SurviveSelectionChange<%0 extends java.lang.Object>
cons protected init(java.lang.Class<{org.netbeans.spi.actions.SurviveSelectionChange%0}>)
cons protected init(java.lang.Class<{org.netbeans.spi.actions.SurviveSelectionChange%0}>,java.lang.String,java.awt.Image)
supr org.netbeans.spi.actions.ContextAction<{org.netbeans.spi.actions.SurviveSelectionChange%0}>

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

