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
package org.netbeans.modules.notifications.tool;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.netbeans.modules.notifications.filter.FilterEditor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Hector Espert
 */
public class DevelopmentToolMenuButtonActionListener implements ActionListener {
   
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        NotificationGeneratorPanel notificationGeneratorPanel = new NotificationGeneratorPanel();
        
        JButton launchButton = new JButton(NbBundle.getMessage(FilterEditor.class, "btnGenerate")); //NOI18N
        Object[] options = new Object[]{launchButton};
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(notificationGeneratorPanel, NbBundle.getMessage(FilterEditor.class, "LBL_NotificationGenerator"), true, //NOI18N
                options, launchButton, DialogDescriptor.DEFAULT_ALIGN, HelpCtx.DEFAULT_HELP, null);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        
        if (launchButton.equals(dialogDescriptor.getValue())) {
            Image image = notificationGeneratorPanel.getNotificationImage();
            NotificationDisplayer.getDefault().notify(notificationGeneratorPanel.getNotificationTitle(), 
                    ImageUtilities.image2Icon(image), 
                    notificationGeneratorPanel.getNotificationDetails(), 
                    notificationGeneratorPanel.getNotificationActionListener(), 
                    notificationGeneratorPanel.getNotificationPriority(), 
                    notificationGeneratorPanel.getNotificationCategory());
        }
    }
    
    public static DevelopmentToolMenuButtonActionListener getInstance() {
        return new DevelopmentToolMenuButtonActionListener();
    }
    
}
