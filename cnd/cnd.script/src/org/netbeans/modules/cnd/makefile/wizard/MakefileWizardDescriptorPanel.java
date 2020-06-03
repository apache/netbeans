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

package  org.netbeans.modules.cnd.makefile.wizard;

import java.awt.Component;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class MakefileWizardDescriptorPanel implements WizardDescriptor.Panel {

    /** Serial version number */
    static final long serialVersionUID = -7154324322016837684L;

    private Vector<ChangeListener> listvec;

    private MakefileWizardPanel panel = null;
    private String helpString = "Construct_make_create"; // NOI18N

    MakefileWizardDescriptorPanel(MakefileWizardPanel panel, String helpString) {
        this.panel = panel;
        this.helpString = helpString;
    }


    /** Get the component for this panel */
    public Component getComponent() {
        return panel;
    }


    /**
     *  Default help for those panels which do not currently have a help topic.
     */
    public HelpCtx getHelp() {
        return new HelpCtx(helpString);
    }


    /**
     *  The default validation method. Most panels don't do validation so don't
     *  need to override this.
     */
    public boolean isValid() {
        return panel.isPanelValid();
    }

    public void addChangeListener(ChangeListener listener) {
        if (listvec == null) {
            listvec = new Vector<ChangeListener>(1);
        }
        listvec.add(listener);
    }
  

    public void removeChangeListener(ChangeListener listener) {
        if (listvec != null) {
            listvec.remove(listener);
        }
    }


    public void readSettings(Object settings) {
    }


    public void storeSettings(Object settings) {
    }    

    public void putClientProperty(Object key, Object value) {
        panel.putClientProperty(key, value);
    }
}
