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
package org.netbeans.modules.cnd.remote.ui.wizard;

import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static org.netbeans.modules.cnd.remote.server.RemoteServerList.TRACE_SETUP;
import static org.netbeans.modules.cnd.remote.server.RemoteServerList.TRACE_SETUP_PREFIX;
import org.netbeans.modules.cnd.remote.server.StopWatch;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/*package*/ final class CreateHostWizardPanel2 implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>, WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private CreateHostVisualPanel2 component;
    private ExecutionEnvironment lastValidatedHost;
    private final CreateHostData data;

    /** NB: is written only in validate(), is read from other places */
    private volatile Future<Boolean> validationTask;

    private WizardDescriptor settings;

    public CreateHostWizardPanel2(CreateHostData data) {
        this.data = data;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public CreateHostVisualPanel2 getComponent() {
        if (component == null) {
            component = new CreateHostVisualPanel2(data, this);
        }
        return component;
    }

    @Override
    public void prepareValidation() {
        component.enableControls(false);
    }

    @Override
    public void validate() throws WizardValidationException {
        ExecutionEnvironment host = component.getHost();
        
        if (host == null || !host.equals(lastValidatedHost)) {
            StopWatch sw = StopWatch.createAndStart(TRACE_SETUP, TRACE_SETUP_PREFIX, host, "host validation"); //NOI18N
            validationTask = component.validateHost();

            try {
                validationTask.get();
                sw.stop();
            } catch (InterruptedException ex) {
                validationTask.cancel(true);
            } catch (Exception ex) {
                // just skip it 
                // component.getHost() == null will indicate that validation
                // failed
            } finally {
                validationTask = null;
            }
        }

        component.enableControls(true);

        if (component.getHost() == null) {
            String errMsg = NbBundle.getMessage(getClass(), "MSG_Failure");
            throw new WizardValidationException(component, errMsg, errMsg);
        }

        lastValidatedHost = host;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewRemoteDevelopmentHostWizardP2");
    }

    @Override
    public boolean isValid() {
        if (!component.canValidateHost()) {
            String message = NbBundle.getMessage(getClass(), "MSG_HostAlreadyAdded"); // NOI18N
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }

        if (component.hasConfigProblems()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, component.getConfigProblem());
            return false;
        }

        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N

        return true;
    }
    ////////////////////////////////////////////////////////////////////////////
    // change support
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    // 
    // This method (removeChangeListener) is called when we go away from
    // the panel.
    // If it happens when we are in the host validation phase and to step back,
    // then both buttons (prev, next) will be disabled (validationRuns == true)
    // See WizardDescriptor:893
    //         boolean valid = p.isValid () && !validationRuns;
    //         nextButton.setEnabled (next && valid);
    //
    // So here we should interrupt the validation process.
    //
    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
        Future<Boolean> task = validationTask;
        if (task != null && !task.isDone()) {
            task.cancel(true);
            // no need to set validationTask to null:
            // it is not null only if validate() is running =>
            // it's validate() who will set it to null 
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    ////////////////////////////////////////////////////////////////////////////
    // settings
    @Override
    public void readSettings(WizardDescriptor settings) {
        this.settings = settings;
        getComponent().init();
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        component.storeConfiguration();
        data.setExecutionEnvironment(getComponent().getHost());
        data.setRunOnFinish(getComponent().getRunOnFinish());
    }
}
