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
    public abstract static class Panel
            implements WizardDescriptor.Panel<I18nWizardDescriptor.Settings> {

        /** Reference to panel. */
        private Component component;

        /** Keeps only one listener. It's fine since WizardDescriptor registers always the same listener. */
        private ChangeListener changeListener;


        /** initialized in read settings **/  
        private I18nWizardDescriptor.Settings settings = null;

        /** Gets component to display. Implements <code>WizardDescriptor.Panel</code> interface method. 
         * @return this instance */
        public final synchronized Component getComponent() {
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


