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

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/** Implementaion of WizardDescriptor.Panel that can be used in create from template.
 *
 * @author Jiri Rechtacek
 */
final class TemplateWizardPanel2 implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private TemplateWizard2 templateWizard2UI;
    /** listener to changes in the wizard */
    private ChangeListener listener;

    private WizardDescriptor settings;

    private TemplateWizard2 getPanelUI () {
        if (templateWizard2UI == null) {
            templateWizard2UI = new TemplateWizard2 ();
            templateWizard2UI.addChangeListener (listener);
        }
        return templateWizard2UI;
    }
    
    /** Add a listener to changes of the panel's validity.
    * @param l the listener to add
    * @see #isValid
    */
    public void addChangeListener (ChangeListener l) {
        if (listener != null) throw new IllegalStateException ();
        if (templateWizard2UI != null)
            templateWizard2UI.addChangeListener (l);
        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
    * @param l the listener to remove
    */
    public void removeChangeListener (ChangeListener l) {
        listener = null;
        if (templateWizard2UI != null)
            templateWizard2UI.removeChangeListener (l);
    }

    /** Get the component displayed in this panel.
     *
     * Note; method can be called from any thread, but not concurrently
     * with other methods of this interface.
     *
     * @return the UI component of this wizard panel
     *
     */
    public java.awt.Component getComponent() {
        return getPanelUI ();
    }
    
    /** Help for this panel.
     * When the panel is active, this is used as the help for the wizard dialog.
     * @return the help or <code>null</code> if no help is supplied
     *
     */
    public HelpCtx getHelp() {
        return new HelpCtx (TemplateWizard2.class);
    }
    
    /** Test whether the panel is finished and it is safe to proceed to the next one.
    * If the panel is valid, the "Next" (or "Finish") button will be enabled.
    * @return <code>true</code> if the user has entered satisfactory information
    */
    public boolean isValid() {
        if (templateWizard2UI == null) {
            return false;
        }
        
        String err = getPanelUI().implIsValid();
        // bugfix #34799, don't set errorMessage if the panel is not showed
        if (getPanelUI ().isShowing ()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, err); //NOI18N
        }
        return err == null;
    }
    
    /** Provides the wizard panel with the current data--either
     * the default data or already-modified settings, if the user used the previous and/or next buttons.
     * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
     * <p>The settings object is originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}.
     * In the case of a <code>TemplateWizard.Iterator</code> panel, the object is
     * in fact the <code>TemplateWizard</code>.
     * @param settings the object representing wizard panel state
     * @exception IllegalStateException if the the data provided
     * by the wizard are not valid.
     *
     */
    public void readSettings(WizardDescriptor settings) {
        this.settings = settings;
        getPanelUI ().implReadSettings (settings);
    }
    
    /** Provides the wizard panel with the opportunity to update the
     * settings with its current customized state.
     * Rather than updating its settings with every change in the GUI, it should collect them,
     * and then only save them when requested to by this method.
     * Also, the original settings passed to {@link #readSettings} should not be modified (mutated);
     * rather, the object passed in here should be mutated according to the collected changes,
     * in case it is a copy.
     * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
     * <p>The settings object is originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}.
     * In the case of a <code>TemplateWizard.Iterator</code> panel, the object is
     * in fact the <code>TemplateWizard</code>.
     * @param settings the object representing wizard panel state
     *
     */
    public void storeSettings(WizardDescriptor settings) {
        getPanelUI ().implStoreSettings (settings);
        this.settings = null;
    }

    public boolean isFinishPanel() {
        return true;
    }
    
}
