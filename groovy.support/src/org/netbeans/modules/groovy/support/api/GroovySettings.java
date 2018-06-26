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

package org.netbeans.modules.groovy.support.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.netbeans.modules.groovy.support.options.SupportOptionsPanelController;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import static org.netbeans.modules.groovy.support.api.Bundle.*;

/**
 * Groovy settings
 *
 * @author Martin Adamek
 */
// FIXME separate classes ?
public final class GroovySettings extends AdvancedOption {

    public static final String GROOVY_OPTIONS_CATEGORY = "Advanced/org-netbeans-modules-groovy-support-api-GroovySettings"; // NOI18N
    public static final String GROOVY_DOC_PROPERTY  = "groovy.doc"; // NOI18N
    
    private static final String GROOVY_DOC  = "groovyDoc"; // NOI18N
    private static GroovySettings instance;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    

    private GroovySettings() {
    }

    public static synchronized GroovySettings getInstance() {
        if (instance == null) {
            instance = new GroovySettings();
        }
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public String getGroovyDoc() {
        synchronized (this) {
            return getPreferences().get(GROOVY_DOC, null); // NOI18N
        }
    }

    public void setGroovyDoc(String groovyDoc) {
        assert groovyDoc != null;

        String oldValue;
        synchronized (this) {
            oldValue = getGroovyDoc();
            getPreferences().put(GROOVY_DOC, groovyDoc);
        }
        propertyChangeSupport.firePropertyChange(GROOVY_DOC_PROPERTY, oldValue, groovyDoc);
    }

    @Override
    @NbBundle.Messages("AdvancedOption_DisplayName_Support=Groovy")
    public String getDisplayName() {
        return AdvancedOption_DisplayName_Support();
    }

    @Override
    @NbBundle.Messages("AdvancedOption_Tooltip_Support=Groovy configuration")
    public String getTooltip() {
        return AdvancedOption_Tooltip_Support();
    }

    @Override
    public OptionsPanelController create() {
        return new SupportOptionsPanelController();
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(GroovySettings.class);
    }

}
