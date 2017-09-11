/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
