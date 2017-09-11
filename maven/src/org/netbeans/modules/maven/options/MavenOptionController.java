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

package org.netbeans.modules.maven.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * controller for maven2 settings in the options dialog.
 * @author Milos Kleint
 */
@OptionsPanelController.SubRegistration(
    location=JavaOptions.JAVA,
    id=MavenOptionController.OPTIONS_SUBPATH,
    displayName="#TIT_Maven_Category",
    keywords="#KW_MavenOptions",
    keywordsCategory=JavaOptions.JAVA + "/Maven"
//    toolTip="#TIP_Maven_Category"
)
public class MavenOptionController extends OptionsPanelController {
    public static final String OPTIONS_SUBPATH = "Maven"; // NOI18N
    public static final String TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //NOI18N
            "<settings xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +//NOI18N
            "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">" +//NOI18N
            "</settings>";//NOI18N
    private SettingsPanel panel;
    private SettingsModel setts;
    private final List<PropertyChangeListener> listeners;
    /**
     * Creates a new instance of MavenOptionController
     */
    public MavenOptionController() {
        listeners = new ArrayList<PropertyChangeListener>();
    }

    @Override
    public void update() {
        getPanel().setValues();
    }
    
    @Override
    public void applyChanges() {
        getPanel().applyValues();
    }
    
    @Override
    public void cancel() {
        setts = null;
    }
    
    @Override
    public boolean isValid() {
        return getPanel().hasValidValues();
    }
    
    @Override
    public boolean isChanged() {
        return getPanel().hasChangedValues();
    }
    
    @Override
    public JComponent getComponent(Lookup lookup) {
        return getPanel();
    }

    void firePropChange(String property, Object oldVal, Object newVal) {
        ArrayList<PropertyChangeListener> lst;
        synchronized (listeners) {
            lst = new ArrayList<PropertyChangeListener>(listeners);
        }
        PropertyChangeEvent evnt = new PropertyChangeEvent(this, property, oldVal, newVal);
        for (PropertyChangeListener prop : lst) {
            if (prop == null) {
                //#180218
                continue;
            }
            prop.propertyChange(evnt);
        }
    }
    
    private SettingsPanel getPanel() {
        if (panel == null) {
            panel = new SettingsPanel(this);
        }
        return panel;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.maven.options.MavenAdvancedOption"); //NOI18N
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (listeners) {
            listeners.add(propertyChangeListener);
        }
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (listeners) {
            listeners.remove(propertyChangeListener);
        }
    }
    
}
