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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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


package org.netbeans.modules.i18n.wizard;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import java.util.Map;
import org.openide.loaders.DataObject;


/**
 * Wizard descriptor of i18n wizard and i18n test wizard.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see org.openide.WizardDescriptor
 */
final class I18nWizardDescriptor extends WizardDescriptor {

    /** Preferred size for panels in i18n wizard. */
    public static final Dimension PREFERRED_DIMENSION = new Dimension(500, 300);
    
    /** Hack. In super it's private. */
    private final WizardDescriptor.Iterator<Settings> panels;

    /** Hack. In super it's private. */
    private final Settings settings;
    
    /** Creates new I18nWizardDescriptor */
    private I18nWizardDescriptor(WizardDescriptor.Iterator<Settings> panels, Settings settings) {
        super(panels, settings);
        
        this.panels = panels;
        this.settings = settings;
    }

    /** Creates I18N wizard descriptor.
     * @return <code>I18nWizardDescriptor</code> instance. */
    static WizardDescriptor createI18nWizardDescriptor(WizardDescriptor.Iterator<Settings> panels, Settings settings) {
        return new I18nWizardDescriptor(panels, settings);
    }
    
    /**
     * Kind of abstract "adapter" implementing <code>WizardDescriptor.Panel</code>
     * interface. Used by i18n wizard.
     *
     * @see org.openide.WizardDescriptor.Panel
     */
    public static abstract class Panel
            implements WizardDescriptor.Panel<I18nWizardDescriptor.Settings> {

        /** Reference to panel. */
        private Component component;

        /** Keeps only one listener. It's fine since WizardDescriptor registers always the same listener. */
        private ChangeListener changeListener;


        /** initialized in read settings **/  
        private I18nWizardDescriptor.Settings settings = null;

        /** Gets component to display. Implements <code>WizardDescriptor.Panel</code> interface method. 
         * @return this instance */
        public synchronized final Component getComponent() {
            if (component == null) {
                component = createComponent();
            }

            return component;
        }

        /** Creates component. */
        protected abstract Component createComponent();

        /** Indicates if panel is valid. Implements <code>WizardDescriptor.Panel</code> interface method. 
         * @return true */
        public boolean isValid() {
            return true;
        }

        /** Reads settings at the start when the panel comes to play. Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void readSettings(I18nWizardDescriptor.Settings settings) {
	  this.settings = settings;
        }

        /** Stores settings at the end of panel show. Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void storeSettings(I18nWizardDescriptor.Settings settings) {
        }

        /** Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void addChangeListener(ChangeListener listener) {
            changeListener = listener;
        }

        /** Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void removeChangeListener(ChangeListener listener) {
            if ((changeListener != null) && (changeListener == listener)) {
                changeListener = null;
            }
        }

        /** Fires state changed event. Helper method. */
        public final void fireStateChanged() {
            if (changeListener != null) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }

        public Project getProject() {
	  return settings.project;
	}

        public Map<DataObject,SourceData> getMap() {
	  return settings.map;
	}
 
	                	        
    } // End of nested class Panel.

  public static class Settings {
    public Settings(Map<DataObject,SourceData> map, Project project) {
      this.map = map;
      this.project = project;
    }
    public Map<DataObject,SourceData> map;
    public Project project;
  }
    
}


