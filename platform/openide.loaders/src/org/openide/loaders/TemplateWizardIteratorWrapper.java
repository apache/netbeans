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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;

/**
 *
 * @author Jiri Rechtacek
 */
class TemplateWizardIteratorWrapper implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor>, ChangeListener {

    protected TemplateWizardIterImpl iterImpl;
    
    TemplateWizardIteratorWrapper(TemplateWizardIterImpl iterImpl) {
        this.iterImpl = iterImpl;
    }
    
    public TemplateWizardIterImpl getOriginalIterImpl () {
        return iterImpl;
    }
    
    /** Resets the iterator to first screen.
    */
    public void first () {
        iterImpl.first ();
    }

    /** Change the additional iterator.
    */
    public void setIterator (TemplateWizard.Iterator it, boolean notify) {
        iterImpl.setIterator (it, notify);
    }

    /** Getter for current iterator.
    */
    public TemplateWizard.Iterator getIterator () {
        return iterImpl.getIterator ();
    }

    /** Get the current panel.
     * @return the panel
     */
    @Override public WizardDescriptor.Panel<WizardDescriptor> current() {
        return iterImpl.current ();
    }


    /** Get the name of the current panel.
     * @return the name
     */
    @Override public String name() {
        return iterImpl.name ();
    }

    /** Test whether there is a next panel.
     * @return <code>true</code> if so
     */
    @Override public boolean hasNext() {
        return iterImpl.hasNext ();
    }

    /** Test whether there is a previous panel.
     * @return <code>true</code> if so
     */
    @Override public boolean hasPrevious() {
        return iterImpl.hasPrevious ();
    }

    /** Move to the next panel.
     * I.e. increment its index, need not actually change any GUI itself.
     * @exception NoSuchElementException if the panel does not exist
     */
    @Override public void nextPanel() {
        iterImpl.nextPanel ();
    }

    /** Move to the previous panel.
     * I.e. decrement its index, need not actually change any GUI itself.
     * @exception NoSuchElementException if the panel does not exist
     */
    @Override public void previousPanel() {
        iterImpl.previousPanel ();
    }

    /** Refires the info to listeners */
    @Override public void stateChanged(final ChangeEvent p1) {
        iterImpl.stateChanged (p1);
    }

    /** Registers ChangeListener to receive events.
     *@param listener The listener to register.
     */
    @Override public void addChangeListener(ChangeListener listener) {
        iterImpl.addChangeListener (listener);
    }
    /** Removes ChangeListener from the list of listeners.
     *@param listener The listener to remove.
     */
    @Override public void removeChangeListener(ChangeListener listener) {
        iterImpl.removeChangeListener (listener);
    }
    
    @Override public void initialize(WizardDescriptor wiz) {
        iterImpl.initialize (wiz);
    }
    
    @Override public void uninitialize(WizardDescriptor wiz) {
        iterImpl.uninitialize();
    }
    
    @Override public Set<DataObject> instantiate () throws IOException {
        return iterImpl.instantiate ();
    }
    
    static class AsynchronousInstantiatingIterator extends TemplateWizardIteratorWrapper implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {
        AsynchronousInstantiatingIterator(TemplateWizardIterImpl it) {
            super (it);
        }
    }

    static class BackgroundInstantiatingIterator extends TemplateWizardIteratorWrapper implements WizardDescriptor.BackgroundInstantiatingIterator<WizardDescriptor> {
        BackgroundInstantiatingIterator(TemplateWizardIterImpl it) {
            super (it);
        }
    }

    static class ProgressInstantiatingIterator extends TemplateWizardIteratorWrapper implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
        ProgressInstantiatingIterator(TemplateWizardIterImpl it) {
            super (it);
        }
        @Override public Set<DataObject> instantiate (ProgressHandle handle) throws IOException {
            return iterImpl.instantiate (handle);
        }
    }
    
}
