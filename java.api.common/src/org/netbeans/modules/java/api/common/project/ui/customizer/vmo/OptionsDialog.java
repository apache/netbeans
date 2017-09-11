/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Rastislav Komara
 */
public class OptionsDialog extends JDialog {

    private OptionsDialog() {
        this((Frame) null);
    }

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


    public static String showCustomizer(Window owner, String options) throws Exception {
        final String[] result = {options};
        final OptionsDialog od;
        if (owner instanceof Frame) {
            od = new OptionsDialog((Frame)owner);
        } else if (owner instanceof Dialog) {
            od = new OptionsDialog((Dialog)owner);
        } else if (owner instanceof Window) {
            od = new OptionsDialog((Window)owner);
        } else {
            od = new OptionsDialog();
        }
        final VMOptionsTableModel tableModel = new VMOptionsTableModel();
        tableModel.fill(options);
        
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

        od.setSize(480,360);        

        setScreenCenter(owner, od);

        od.setModal(true);
        od.setVisible(true);
        od.dispose();
        return result[0];
    }

    private static void setScreenCenter(Object owner, OptionsDialog od) {
        final Rectangle parentRect;
        if (owner instanceof Window) {
            Window window = (Window) owner;
            parentRect = new Rectangle(window.getLocationOnScreen(), window.getSize());
        } else if (owner instanceof Component) {
            Component component = (Component) owner;
            parentRect = new Rectangle(component.getLocationOnScreen(), component.getSize());
        } else {
            parentRect = new Rectangle(new Point(0,0), Toolkit.getDefaultToolkit().getScreenSize());
        }
        od.setLocation(new Point((int) parentRect.getCenterX() - (od.getWidth() >> 1), (int) parentRect.getCenterY() - (od.getHeight() >> 1) ));
    }

}
