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
package org.netbeans.modules.cloud.oracle;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Dusan Petrovic
 */
public class NotificationUtils {
    
    public static boolean confirmAction(String message) {
        NotifyDescriptor.Confirmation msg = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION);
        Object choice = DialogDisplayer.getDefault().notify(msg);
        return choice == NotifyDescriptor.YES_OPTION || choice == NotifyDescriptor.OK_OPTION;
    }
    
    public static void showErrorMessage(String message) {
        DialogDisplayer.getDefault()
                .notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }
    
    public static void showMessage(String message) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message));
    }
    
    public static void showWarningMessage(String message) {
        DialogDisplayer.getDefault()
                .notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE));
    }
}
