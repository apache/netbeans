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
            return new ConfigurationPanel(attachTypeName, this, featureInfo, false);
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
