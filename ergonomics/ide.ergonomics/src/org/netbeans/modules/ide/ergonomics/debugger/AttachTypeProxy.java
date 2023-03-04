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

package org.netbeans.modules.ide.ergonomics.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Callable;
import javax.swing.JComponent;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.ide.ergonomics.fod.ConfigurationPanel;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FoDLayersProvider;
import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Pavel Flaska
 */
public class AttachTypeProxy extends AttachType implements Controller, Callable<JComponent> {
    private AttachType delegate;
    private boolean isVisible;
    private String attachTypeName;
    PropertyChangeSupport propertyChangeSupport;
    private FeatureInfo featureInfo;

    private AttachTypeProxy(String attachTypeName, FeatureInfo whichProvides) {
        this.delegate = null;
        this.isVisible = true;
        this.attachTypeName = attachTypeName;
        this.featureInfo = whichProvides;
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public static AttachType create(FileObject fob) {
        FeatureInfo whichProvides = FoDLayersProvider.getInstance().whichProvides(fob);
        String displayName = (String) fob.getAttribute("displayName");
        if (displayName == null) {
            throw new IllegalArgumentException("No displayName attribute: " + fob);
        }
        return new AttachTypeProxy(displayName, whichProvides);
    }
    
    @Override
    public String getTypeDisplayName() {
        if (!isVisible) {
            return null;
        } else {
            if (getAttachType() != null) {
                isVisible = false;
                return null;
            }
        }
        if (delegate != null) {
            return delegate.getTypeDisplayName();
        }
        return attachTypeName;
    }

    @Override
    public JComponent getCustomizer() {
        if (delegate == null) {
            return new ConfigurationPanel(attachTypeName, this, featureInfo);
        } else {
            return delegate.getCustomizer();
        }
    }

    @Override
    public Controller getController() {
        if (delegate == null) {
            return this;
        } else {
            return getRealController();
        }
    }

    private void invalidate() {
        isVisible = false;
        delegate = getAttachType();
        propertyChangeSupport.firePropertyChange(Controller.PROP_VALID, false, true);
    }

    AttachType getAttachType() {
        for (AttachType type : DebuggerManager.getDebuggerManager().lookup(null, AttachType.class)) {
            if (type instanceof AttachTypeProxy) {
                continue;
            } else if (type.getTypeDisplayName().equals(attachTypeName)) {
                return type;
            }
        }
        return null;
    }

    public boolean isValid() {
        return delegate == null ? false : getRealController().isValid();
    }

    public boolean ok() {
        return delegate != null ? getRealController().ok() : false;
    }

    public boolean cancel() {
        return delegate == null ? true : getRealController().cancel();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public JComponent call() throws Exception {
        invalidate();
        return getCustomizer();
    }

    private Controller getRealController() {
        Controller controller = null;
        if (delegate != null) {
            JComponent c = delegate.getCustomizer();
            controller = delegate.getController();
        }
        return controller;
    }

}
