/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.ui.wizard;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.remote.ui.networkneighbour.NetworkRegistry;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/*
 * package
 */ final class CreateHostWizardPanel1 implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>, WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private CreateHostVisualPanel1 component;
    private final CreateHostData data;
    private WizardDescriptor settings;
    private final AtomicBoolean knownError = new AtomicBoolean(false);

    public CreateHostWizardPanel1(CreateHostData data) {
        this.data = data;
    }

    @Override
    public CreateHostVisualPanel1 getComponent() {
        if (component == null) {
            component = new CreateHostVisualPanel1(data, this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewRemoteDevelopmentHostWizardP1");
    }

    @Override
    public boolean isValid() {
        String hostname = getComponent().getHostname();
        if (hostname == null || hostname.isEmpty()) {
            setError(NbBundle.getMessage(CreateHostWizardPanel1.class,
                    "CreateHostWizardPanel1.error.noHost")); // NOI18N
            return false;
        }

        Integer port = getComponent().getPort();
        if (port < 22) {
            setError(NbBundle.getMessage(CreateHostWizardPanel1.class,
                    "CreateHostWizardPanel1.error.invalidPort")); // NOI18N
            return false;
        }

        if (data.isManagingUser()) {
            String user = getComponent().getUser();
            if (user == null || user.isEmpty()) {
                setError(NbBundle.getMessage(CreateHostWizardPanel1.class,
                        "CreateHostWizardPanel1.error.noUser")); // NOI18N
                return false;
            }
        }

        if (!getComponent().isProxyValid()) {
            setError(NbBundle.getMessage(CreateHostWizardPanel1.class,
                    "CreateHostWizardPanel1.error.invalidProxy")); // NOI18N
            return false;
        }

        // After delayed validation fails, isValid is invoked instantly...
        // If set message to null here, user will not see an error about
        // unreacheable host...

        if (!knownError.compareAndSet(true, false)) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }

        return true;
    }
    ////////////////////////////////////////////////////////////////////////////
    // change support
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
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
        if (data.isManagingUser()) {
            data.setUserName(getComponent().getUser());
        }
        data.setHostName(getComponent().getHostname());
        data.setPort(getComponent().getPort());
    }

    @Override
    public void prepareValidation() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void validate() throws WizardValidationException {

        component.applyProxyChangesIfNeed();

        String hostname = getComponent().getHostname();
        Integer port = getComponent().getPort();

        setInfo(NbBundle.getMessage(CreateHostWizardPanel1.class,
                "CreateHostWizardPanel1.info.connecting", hostname, port)); // NOI18N

        if (!NetworkRegistry.getInstance().isHostAccessible(hostname, port)) {
            String error = NbBundle.getMessage(CreateHostWizardPanel1.class,
                    "CreateHostWizardPanel1.error.pingFailed", hostname, port); // NOI18N
            setError(error);
            knownError.set(true);
            throw new WizardValidationException(getComponent(),
                    "pingFailed", error); // NOI18N
        }

        setInfo(null);
    }

    private void setError(final String error) {
        if (SwingUtilities.isEventDispatchThread()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
                }
            });
        }
    }

    private void setInfo(final String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
                }
            });
        }
    }
}
