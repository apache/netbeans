/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.windows;

import org.openide.awt.StatusDisplayer;
import org.openide.util.*;

import java.beans.*;


/** Simple support for an openable objects.
* Can be used either as an {@link org.openide.cookies.OpenCookie},
* {@link org.openide.cookies.ViewCookie}, or {@link org.openide.cookies.CloseCookie},
* depending on which cookies the subclass implements.
*
* @author Jaroslav Tulach
*/
public abstract class CloneableOpenSupport extends Object {
    private static java.awt.Container container;

    /** the environment that provides connection to outside world */
    protected Env env;

    /** All opened editors on this file.
     * <em>Warning:</em> Treat this field like <code>final</code>.
     * Internally the instance is used as <code>WeakListener</code>
     * on <code>Env</code> validity changes.
     * Changing the instance in subclasses would lead to breaking
     * of that listening, thus to errorneous behaviour. */
    protected CloneableTopComponent.Ref allEditors;

    /** New support for a given environment.
    * @param env environment to take all date from/to
    */
    public CloneableOpenSupport(Env env) {
        this.env = env;

        Listener l = new Listener(env, this);
        this.allEditors = l;

        // attach property change listener to be informed about loosing validity
        env.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(l, env));

        // attach vetoable change listener to be cancel loosing validity when modified
        env.addVetoableChangeListener(org.openide.util.WeakListeners.vetoableChange(l, env));
    }

    /** Opens and focuses or just focuses already opened
     * <code>CloneableTopComponent</code>.
     * <p><b>Note: The actual processing of this method is scheduled into AWT thread
     * in case it is called from other than the AWT thread.</b></p>
     * @see org.openide.cookies.OpenCookie#open
     * @see #openCloneableTopComponent
     */
    public void open() {
        CloneableOpenSupport redirect = CloneableOpenSupportRedirector.findRedirect(this);
        if (redirect != null) {
            redirect.open();
            this.afterRedirectImpl(redirect);
            return;
        }
        //Bugfix #10688 open() is now run in AWT thread
        Mutex.EVENT.writeAccess(
            new Runnable() {
                public void run() {
                    CloneableTopComponent editor = openCloneableTopComponent();
                    editor.requestActive();
                }
            }
        );
    }

    /** Focuses existing component to view, or if none exists creates new.
    * The default implementation simply calls {@link #open}.
    * @see org.openide.cookies.ViewCookie#view
    */
    public void view() {
        open();
    }

    /** Focuses existing component to view, or if none exists creates new.
    * The default implementation simply calls {@link #open}.
    * @see org.openide.cookies.EditCookie#edit
    */
    public void edit() {
        open();
    }

    /** Closes all components.
    * @return <code>true</code> if every component is successfully closed or <code>false</code> if the user cancelled the request
    * @see org.openide.cookies.CloseCookie#close
    */
    public boolean close() {
        return close(true);
    }

    /** Closes all opened windows.
    * @param ask true if we should ask user
    * @return true if sucesfully closed
    */
    protected boolean close(final boolean ask) {
        if (allEditors.isEmpty()) {
            return true;
        }

        //Bugfix #10688 close() is now run in AWT thread
        //also bugfix of 10714 - whole close (boolean) is run in AWT thread
        Boolean ret = Mutex.EVENT.writeAccess(
                new Mutex.Action<Boolean>() {
                    public Boolean run() {
                        //synchronized (allEditors) {
                        synchronized (getLock()) {
                            // user canceled the action
                            if (ask && !canClose()) {
                                return Boolean.FALSE;
                            }

                            java.util.Enumeration en = allEditors.getComponents();

                            while (en.hasMoreElements()) {
                                TopComponent c = (TopComponent) en.nextElement();

                                if (!c.close()) {
                                    return Boolean.FALSE;
                                }
                            }
                        }

                        return Boolean.TRUE;
                    }
                }
            );

        return ret.booleanValue();
    }

    /** Should test whether all data is saved, and if not, prompt the user
    * to save.
    * The default implementation returns <code>true</code>.
    *
    * @return <code>true</code> if everything can be closed
    */
    protected boolean canClose() {
        return true;
    }

    /** Simply open for an editor. */
    protected final CloneableTopComponent openCloneableTopComponent() {
        //synchronized (allEditors) {
        synchronized (getLock()) {
            CloneableTopComponent ret = allEditors.getArbitraryComponent();

            if (ret != null) {
                ret.open();

                return ret;
            } else {
                // no opened editor
                String msgOpening = messageOpening();

                if (msgOpening != null) {
                    StatusDisplayer.getDefault().setStatusText(msgOpening);
                }

                CloneableTopComponent editor = createCloneableTopComponent();
                editor.setReference(allEditors);
                editor.open();

                String msgOpened = messageOpened();

                if (msgOpened == null && msgOpening != null) {
                    // only reset when opening message was displayed
                    msgOpened = ""; // NOI18N
                }

                if (msgOpened != null) {
                    StatusDisplayer.getDefault().setStatusText(msgOpened);
                }

                return editor;
            }
        }
    }

    /** Creates lock object used in close and openCloneableTopComponent. */
    private Object getLock() {
        if (container == null) {
            container = new java.awt.Container();
        }

        return container.getTreeLock();
    }

    /** A method to create a new component. Must be overridden in subclasses.
    * @return the cloneable top component for this support
    */
    protected abstract CloneableTopComponent createCloneableTopComponent();

    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    protected abstract String messageOpening();

    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    protected abstract String messageOpened();

    private void afterRedirectImpl(CloneableOpenSupport redirectedTo) {
        // there is a common patern in user code:
        // CloneableEditorSupport ces = ...;
        // ces.edit();
        // JEditorPane[] panes = ces.getOpenedPanes();
        // if (panes != null) panes[0].setPosition(offset);
        // in case when redirection has happened during edit() call
        // 'ces' instance returns null for getOpenedPanes
        // but we want panes to be available for 'ces' instance after redirection;
        // remember editors from redirected instance to have correct opened panes
        // for this instance as well
        this.allEditors = redirectedTo.allEditors;
//        this.env = redirect.env;
        afterRedirect(redirectedTo);
    }
    
    /**
     * Called to notify that another redirected CloneableOpenSupport was opened instead of this one.
     * @param redirectedTo redirected instance which was opened instead of this one
     * @since 6.70
     */
    protected void afterRedirect(CloneableOpenSupport redirectedTo) {
    }

    /** Abstract interface that is used by CloneableOpenSupport to
    * talk to outside world.
    */
    public static interface Env extends java.io.Serializable {
        /** that is fired when the objects wants to mark itself as
        * invalid, so all components should be closed.
        */
        public static final String PROP_VALID = "valid"; // NOI18N

        /** that is fired when the objects wants to mark itself modified
        * or not modified.
        */
        public static final String PROP_MODIFIED = "modified"; // NOI18N

        /** Adds property listener.
        */
        public void addPropertyChangeListener(PropertyChangeListener l);

        /** Removes property listener.
        */
        public void removePropertyChangeListener(PropertyChangeListener l);

        /** Adds veto listener.
        */
        public void addVetoableChangeListener(VetoableChangeListener l);

        /** Removes veto listener.
        */
        public void removeVetoableChangeListener(VetoableChangeListener l);

        /** Test whether the support is in valid state or not.
        * It could be invalid after deserialization when the object it
        * referenced to does not exist anymore.
        *
        * @return true or false depending on its state
        */
        public boolean isValid();

        /** Test whether the object is modified or not.
        * @return true if the object is modified
        */
        public boolean isModified();

        /** Support for marking the environement modified.
        * @exception java.io.IOException if the environment cannot be marked modified
        *    (for example when the file is readonly), when such exception
        *    is the support should discard all previous changes
        */
        public void markModified() throws java.io.IOException;

        /** Reverse method that can be called to make the environment
        * unmodified.
        */
        public void unmarkModified();

        /** Method that allows environment to find its
        * cloneable open support.
        */
        public CloneableOpenSupport findCloneableOpenSupport();
    }

    /** Property change &amp; veto listener. To react to dispose/delete of
    * the data object.
    */
    private static final class Listener extends CloneableTopComponent.Ref implements PropertyChangeListener,
        VetoableChangeListener, Runnable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -1934890789745432531L;

        /** environment to use as connection to outside world */
        private final Env env;
        // rerefence to prevent GC of COS created in readResolve() by call to support()
        private final transient CloneableOpenSupport refCOS;

        /** Constructor.
        */
        public Listener(Env env, CloneableOpenSupport cos) {
            this.env = env;
            this.refCOS = cos;
        }

        /** Getter for the associated CloneableOpenSupport
        * @return the support or null if none was found
        */
        private CloneableOpenSupport support() {
            return env.findCloneableOpenSupport();
        }

        public void propertyChange(PropertyChangeEvent ev) {
            if (Env.PROP_VALID.equals(ev.getPropertyName())) {
                // do not check it if old value is not true
                if (Boolean.FALSE.equals(ev.getOldValue())) {
                    return;
                }

                Mutex.EVENT.readAccess(this);
            }
        }

        /** Closes the support in AWT thread.
         */
        public void run() {
            // loosing validity
            CloneableOpenSupport os = support();

            if (os != null) {
                // mark the object as not being modified, so nobody
                // will ask for save
                env.unmarkModified();

                os.close(false);
            }
        }

        /** Forbids setValid (false) on data object when there is an
        * opened editor.
        *
        * @param ev PropertyChangeEvent
        */
        public void vetoableChange(PropertyChangeEvent ev)
        throws PropertyVetoException {
            if (Env.PROP_VALID.equals(ev.getPropertyName())) {
                // do not check it if old value is not true
                if (Boolean.FALSE.equals(ev.getOldValue())) {
                    return;
                }

                if (env.isModified()) {
                    // if the object is modified 
                    CloneableOpenSupport os = support();

                    if ((os != null) && !os.canClose()) {
                        // is modified and has not been successfully closed
                        throw new PropertyVetoException(
                        // [PENDING] this is not a very good detail message!
                        "", ev // NOI18N
                        );
                    }
                }
            }
        }

        /** Resolvable to connect to the right data object. This
        * method is used for connectiong CloneableTopComponents via
        * their CloneableTopComponent.Ref
        */
        public Object readResolve() {
            CloneableOpenSupport os = support();

            if (os == null) {
                // problem! no replace!?
                return this;
            }

            // use the editor support's CloneableTopComponent.Ref
            return os.allEditors;
        }
    }
}
