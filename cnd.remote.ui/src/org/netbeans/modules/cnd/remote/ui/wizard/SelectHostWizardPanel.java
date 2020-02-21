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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.ui.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.remote.ui.setup.CreateHostWizardIterator;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class SelectHostWizardPanel implements
        WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>,
        ChangeListener {
    
    private final ChangeListener changeListener;
    private final boolean allowLocal;
    private SelectHostVisualPanel component;
    private final CreateHostData createHostData;
    private final ToolsCacheManager cacheManager;
    private final CreateHostWizardPanel1 delegate;
    private final AtomicBoolean setupNewHost;
    private WizardDescriptor wizardDescriptor;
    private volatile boolean needsValidation;
    private final boolean allowToCreateNewHostDirectly;

    public SelectHostWizardPanel(boolean allowLocal, boolean allowToCreateNewHostDirectly, ChangeListener changeListener) {
        this.allowLocal = allowLocal;
        this.changeListener = changeListener;
        cacheManager = ToolsCacheManager.createInstance(true);
        this.allowToCreateNewHostDirectly = allowToCreateNewHostDirectly;
        createHostData = new CreateHostData(cacheManager, allowToCreateNewHostDirectly);
        delegate = new CreateHostWizardPanel1(createHostData);
        delegate.addChangeListener(this);
        setupNewHost = new AtomicBoolean();
        if (allowLocal) {
            setupNewHost.set(ServerList.getRecords().isEmpty());
        } else {
            setupNewHost.set(ServerList.getRecords().size() <= 1);
        }
        needsValidation = false;
    }

    @Override
    public SelectHostVisualPanel getComponent() {
        synchronized (this) {
            if (component == null) {                
                component = new SelectHostVisualPanel(this, allowLocal, delegate.getComponent(), setupNewHost, allowToCreateNewHostDirectly);
            }
        }
        return component;
    }

    public List<WizardDescriptor.Panel<WizardDescriptor>> getAdditionalPanels() {
        List<WizardDescriptor.Panel<WizardDescriptor>> list = new ArrayList<>(2);
        list.add(new CreateHostWizardPanel2(createHostData));
        list.add(new CreateHostWizardPanel3(createHostData));
        return list;
    }

    public boolean isNewHost() {
        return setupNewHost.get();
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void prepareValidation() {
        if (needsValidation) {
            getComponent().enableControls(false);
        }
    }

    @Override
    public void validate() throws WizardValidationException {
        if (needsValidation) {
            ExecutionEnvironment execEnv = getComponent().getSelectedHost();
            try {
                if (execEnv != null) {
                    ConnectionManager.getInstance().connectTo(execEnv);
                    RemoteUtil.checkSetupAfterConnection(execEnv);
                }
            } catch (IOException ex) {
                String message = NbBundle.getMessage(getClass(), "CannotConnectMessage");
                throw new WizardValidationException(getComponent(), message, message);
            } catch (CancellationException ex) {
                String message = NbBundle.getMessage(getClass(), "ConnectCancelledMessage");
                throw new WizardValidationException(getComponent(), message, message);
            } finally {
                getComponent().enableControls(true);
            }
        }
    }

    @Override
    public boolean isValid() {
        if (setupNewHost.get()) {
            return delegate.isValid();
        } else {
            return getComponent().getSelectedHost() != null;
        }
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

    @Override
    public void readSettings(WizardDescriptor settings) {
        delegate.readSettings(settings);
        this.wizardDescriptor = settings;
        getComponent().onReadSettings();
        needsValidation = false;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        delegate.storeSettings(settings);
        ExecutionEnvironment env = getComponent().getSelectedHost();
        WizardConstants.PROPERTY_HOST_UID.put(settings, (env == null) ? null : ExecutionEnvironmentFactory.toUniqueID(env)); // NOI18N
        WizardConstants.PROPERTY_TOOLS_CACHE_MANAGER.put(settings, createHostData.getCacheManager()); // NOI18N
        getComponent().onStoreSettings();
        needsValidation = true;
    }

    ExecutionEnvironment getSelectedHost() {
        return getComponent().getSelectedHost();
    }

    void apply() {
        if (isNewHost()) {
            CreateHostWizardIterator.applyHostSetup(cacheManager, createHostData);
        }
    }
}
