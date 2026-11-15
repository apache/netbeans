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
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import org.netbeans.api.java.platform.JavaPlatform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Rastislav Komara
 */
public class OptionsDialog extends JDialog {

    private OptionsDialog(Frame owner) {
        super(owner);
        if (owner != null) {
            setTitle(owner.getTitle());
        }
    }

    private OptionsDialog(Dialog owner) {
        super(owner);
        if (owner != null) {
            setTitle(owner.getTitle());
        }
    }

    private OptionsDialog(Window owner) {
        super(owner);
        if (owner != null) {
            setTitle("VM Options");
        }
    }

    void createLayout(final VMOptionEditorPanel.Callback callback, VMOptionsTableModel model) {
        setContentPane(new VMOptionEditorPanel(callback, model));
    }

    /**
     * Shows the customizer with the options supported by the given
     * platform's {@code java} launcher; when the platform or its options
     * are unknown the built-in option list is used.
     *
     * @param owner the dialog owner
     * @param options the origin options to be preselected in the customizer
     * @param platform the platform whose options are offered, may be {@code null}
     * @return the options selected in the customizer dialog
     * @throws Exception if it isn't possible to parse the vm options.
     */
    public static String showCustomizer(Window owner, String options, JavaPlatform platform) throws Exception {
        final String[] result = {options};
        OptionsDialog od = switch (owner) {
            case Frame frame -> new OptionsDialog(frame);
            case Dialog dialog -> new OptionsDialog(dialog);
            case Window window -> new OptionsDialog(window);
        };
        final VMOptionsTableModel tableModel = new VMOptionsTableModel();
        tableModel.fill(options, JdkOptions.loadOptions(platform));
        
        od.createLayout(new VMOptionEditorPanel.Callback() {
            @Override
            public void okButtonActionPerformed(ActionEvent evt) {
                final List<JavaVMOption<?>> vmOptionList = tableModel.getValidOptions();
                StringBuilder sb = new StringBuilder(" ");
                for (JavaVMOption<?> option : vmOptionList) {
                    option.print(sb);
                    sb.append(" ");
                }
                result[0] = sb.toString().trim();
                od.setVisible(false);
            }

            @Override
            public void cancelButtonActionPerformed(ActionEvent evt) {
                od.setVisible(false);
            }
        }, tableModel);

        od.setSize(400, 600);        

        setScreenCenter(owner, od);

        od.setModal(true);
        od.setVisible(true);
        od.dispose();
        return result[0];
    }

    private static void setScreenCenter(Object owner, OptionsDialog od) {
        Rectangle parentRect = switch (owner) {
            case Window window -> new Rectangle(window.getLocationOnScreen(), window.getSize());
            case Component component -> new Rectangle(component.getLocationOnScreen(), component.getSize());
            default -> new Rectangle(new Point(0,0), Toolkit.getDefaultToolkit().getScreenSize());
        };
        od.setLocation(new Point((int) parentRect.getCenterX() - (od.getWidth() >> 1), (int) parentRect.getCenterY() - (od.getHeight() >> 1) ));
    }

}
