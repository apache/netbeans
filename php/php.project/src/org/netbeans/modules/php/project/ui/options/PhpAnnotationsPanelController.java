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
package org.netbeans.modules.php.project.ui.options;

import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.annotations.UserAnnotations;
import org.netbeans.modules.php.project.ui.PhpAnnotationsPanel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
    displayName="#LBL_AnnotationsOptions", // NOI18N
//    toolTip="#LBL_AnnotationsOptionsTooltip",
    id=PhpAnnotationsPanelController.ID,
    location=UiUtils.OPTIONS_PATH,
    position=155
)
public class PhpAnnotationsPanelController extends BaseOptionsPanelController {

    private static final Logger LOGGER = Logger.getLogger(PhpAnnotationsPanelController.class.getName());
    public static final String ID = "Annotations"; // NOI18N

    // @GuardedBy(EDT)
    private PhpAnnotationsPanel panel = null;


    @Override
    protected boolean validateComponent() {
        assert EventQueue.isDispatchThread();
        // noop
        return true;
    }

    @Override
    protected void updateInternal() {
        assert EventQueue.isDispatchThread();
        getPanel();
        panel.setAnnotations(UserAnnotations.getGlobal().getAnnotations());
        panel.setResolveDeprecatedElements(PhpOptions.getInstance().isAnnotationsResolveDeprecatedElements());
        panel.setUnknownAsType(PhpOptions.getInstance().isAnnotationsUnknownAnnotationsAsTypeAnnotations());
    }

    @Override
    protected void applyChangesInternal() {
        assert EventQueue.isDispatchThread();
        getPanel();
        UserAnnotations.getGlobal().setAnnotations(panel.getAnnotations());
        boolean resolveDeprecatedElements = panel.isResolveDeprecatedElements();
        LOGGER.log(Level.INFO, "Resolving of deprecated PHP elements: {0}", resolveDeprecatedElements);
        PhpOptions.getInstance().setAnnotationsResolveDeprecatedElements(resolveDeprecatedElements);
        PhpOptions.getInstance().setAnnotationsUnknownAnnotationsAsTypeAnnotations(panel.isUnknownAsType());
    }

    @Override
    protected boolean areOptionsChanged() {
        getPanel();
        return PhpOptions.getInstance().isAnnotationsResolveDeprecatedElements() != panel.isResolveDeprecatedElements()
                || PhpOptions.getInstance().isAnnotationsUnknownAnnotationsAsTypeAnnotations() != panel.isUnknownAsType()
                || !UserAnnotations.getGlobal().getAnnotations().equals(panel.getAnnotations());
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.options.PhpAnnotationsPanelController"); // NOI18N
    }

    private PhpAnnotationsPanel getPanel() {
        assert EventQueue.isDispatchThread();
        if (panel == null) {
            panel = PhpAnnotationsPanel.forOptions();
            panel.addChangeListener(this);
        }
        return panel;
    }

}
