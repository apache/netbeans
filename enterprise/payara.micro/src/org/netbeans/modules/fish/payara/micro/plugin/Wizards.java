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
package org.netbeans.modules.fish.payara.micro.plugin;

import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.util.Parameters;

/**
 * This class consists of static utility methods for manipulating wizards.
 *
 */
public final class Wizards {

    private static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; //NOI18N;

    private Wizards() {
    }

    /**
     * Merges the given <code>steps</code> with the given <code>panels</code>
     * into the given <code>wizard</code>. If the wizard has existing steps, the
     * new steps will be added after them.
     *
     * @param wizard the wizard to which to merge; must not be null.
     * @param panels the panels to merge; must not be null.
     * @param steps the steps to merge or null if the steps should be taken from
     * the {@link JComponent#getName names} of the panels components.
     */
    public static void mergeSteps(WizardDescriptor wizard, WizardDescriptor.Panel[] panels, String[] steps) {
        Parameters.notNull("wizard", wizard); //NOI18N
        Parameters.notNull("panels", panels); //NOI18N

        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        int offset;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            offset = beforeSteps.length;
            if (offset > 0 && ("...".equals(beforeSteps[offset - 1]))) {// NOI18N
                offset--;
            }
        } else {
            beforeSteps = null;
            offset = 0;
        }
        String[] resultSteps = new String[(offset) + panels.length];
        for (int i = 0; i < offset; i++) {
            resultSteps[i] = beforeSteps[i];
        }
        setSteps(panels, steps, resultSteps, offset);
    }

    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int n = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < n ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty(WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, i);
            component.getAccessibleContext().setAccessibleDescription(step);
            resultSteps[i + offset] = step;
        }
    }

}
