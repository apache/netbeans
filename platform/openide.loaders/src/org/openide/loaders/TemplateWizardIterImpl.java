/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.Set;
import javax.swing.event.*;
import org.netbeans.api.progress.ProgressHandle;

import org.openide.WizardDescriptor;
import org.openide.util.Mutex;

/** Implementation of template wizard's iterator that allows to
* delegate all functionality to another wizard.
*
* @author  Jaroslav Tulach
*/
class TemplateWizardIterImpl extends Object
implements WizardDescriptor.Iterator, ChangeListener, Runnable {

    /** iterator to delegate to */
    private TemplateWizard.Iterator iterator;

    /** bridge */
    private ChangeListener iteratorListener;
    
    /** is currently panel displayed? */
    private boolean showingPanel = false;
    private boolean newIteratorInstalled = false;

    /** Utility field used by event firing mechanism. */
    private javax.swing.event.EventListenerList listenerList = null;
    
    /** Instance of the template wizard that uses this Iterator.
     */
    private TemplateWizard wizardInstance;        
    
    /** */
    private boolean iteratorInitialized = false;

    /** Getter for the first panel.
     * @return the first and default panel
     */
    private WizardDescriptor.Panel<WizardDescriptor> firstPanel () {
        return wizardInstance.templateChooser();
    }

    /** Resets the iterator to first screen.
    */
    public void first () {
        showingPanel = true;
        fireStateChanged ();
    }

    /** Change the additional iterator.
    */
    public void setIterator (TemplateWizard.Iterator it, boolean notify) {
        if ((this.iterator != null) && (iteratorInitialized)) {
            this.iterator.removeChangeListener(iteratorListener);
            this.iterator.uninitialize(wizardInstance);
        }
        it.initialize(wizardInstance);
        iteratorListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                TemplateWizardIterImpl.this.stateChanged(e);
            }
        };
        it.addChangeListener(iteratorListener);
        iteratorInitialized = true;
        
        iterator = it;
        if (notify) {
            // bugfix #46589, don't call nextPanel() on new iterator
            if (showingPanel) {
                newIteratorInstalled = true;
            }
            showingPanel = false;
            fireStateChanged ();
        }
    }

    /** Getter for current iterator.
    */
    public TemplateWizard.Iterator getIterator () {
        if (iterator == null) {
            // first of all initialize the default iterator
            setIterator (wizardInstance.defaultIterator (), false);
        }
        if (!iteratorInitialized) {
            if (iterator != null) {
                iterator.initialize(wizardInstance);
                iteratorInitialized = true;
            }
        }
        return iterator;
    }

    /** Get the current panel.
     * @return the panel
     */
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return showingPanel ? firstPanel () : getIterator ().current ();
    }


    /** Get the name of the current panel.
     * @return the name
     */
    public String name() {
        return showingPanel ? "" : getIterator ().name (); // NOI18N
    }

    /** Test whether there is a next panel.
     * @return <code>true</code> if so
     */
    public boolean hasNext() {
        return showingPanel || getIterator ().hasNext();
    }

    /** Test whether there is a previous panel.
     * @return <code>true</code> if so
     */
    public boolean hasPrevious() {
        return !showingPanel && getIterator().hasPrevious();
    }

    /** Move to the next panel.
     * I.e. increment its index, need not actually change any GUI itself.
     * @exception NoSuchElementException if the panel does not exist
     */
    public void nextPanel() {
        if (showingPanel || newIteratorInstalled) {
            showingPanel = false;
            newIteratorInstalled = false;
        } else {
            getIterator ().nextPanel ();
        }
    }

    /** Move to the previous panel.
     * I.e. decrement its index, need not actually change any GUI itself.
     * @exception NoSuchElementException if the panel does not exist
     */
    public void previousPanel() {
        if (getIterator ().hasPrevious ()) {
            getIterator ().previousPanel ();
        } else {
            showingPanel = true;
            newIteratorInstalled = false;
        }
    }

    /** Refires the info to listeners */
    public void stateChanged(final javax.swing.event.ChangeEvent p1) {
        fireStateChanged ();
    }

    /** Registers ChangeListener to receive events.
     *@param listener The listener to register.
     */
    public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
        if (listenerList == null ) {
            listenerList = new javax.swing.event.EventListenerList();
        }
        listenerList.add (javax.swing.event.ChangeListener.class, listener);
    }
    /** Removes ChangeListener from the list of listeners.
     *@param listener The listener to remove.
     */
    public synchronized void removeChangeListener(
        javax.swing.event.ChangeListener listener
    ) {
        if (listenerList != null) {
            listenerList.remove (javax.swing.event.ChangeListener.class, listener);
        }        
    }
    
    public void initialize (WizardDescriptor wiz) {
        if (!(wiz instanceof TemplateWizard)) {
            throw new IllegalArgumentException ("WizardDescriptor must be instance of TemplateWizard, but is " + wiz); // NOI18N
        }
        this.wizardInstance = (TemplateWizard)wiz;
        if (wizardInstance.getTemplate () == null) {
            showingPanel = true;
        } else {
            newIteratorInstalled = false;
            showingPanel = false;
        }
        TemplateWizard.Iterator it = iterator;
	if ((it != null)&&(!iteratorInitialized)) {
	    it.initialize(wizardInstance);
            iteratorInitialized = true;
	}
    }
    
    public void uninitialize() {
	if (iterator != null && wizardInstance != null) { 
	    iterator.uninitialize(wizardInstance);            
            iteratorInitialized = false;
	}
        showingPanel = true;
    }
    
    public Set<DataObject> instantiate () throws IOException {
        assert wizardInstance != null : "wizardInstance cannot be null when instantiate() called."; // NOI18N
        return wizardInstance.instantiateNewObjects (null);
    }
    
    public Set<DataObject> instantiate (ProgressHandle handle) throws IOException {
        assert wizardInstance != null : "wizardInstance cannot be null when instantiate() called."; // NOI18N
        return wizardInstance.instantiateNewObjects (handle);
    }
    
    /** Notifies all registered listeners about the event.
     *
     *@param param1 Parameter #1 of the <CODE>ChangeEvent<CODE> constructor.
     */
    private void fireStateChanged() {
        Mutex.EVENT.writeAccess(this);
    }
    
    @Override
    public void run() {
        if (listenerList == null)
            return;
        
        javax.swing.event.ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList ();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==javax.swing.event.ChangeListener.class) {
                if (e == null)
                    e = new javax.swing.event.ChangeEvent (this);
                ((javax.swing.event.ChangeListener)listeners[i+1]).stateChanged (e);
            }
        }
    }

}

