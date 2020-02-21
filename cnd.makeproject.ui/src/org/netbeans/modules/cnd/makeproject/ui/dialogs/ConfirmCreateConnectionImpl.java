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
package org.netbeans.modules.cnd.makeproject.ui.dialogs;

import javax.swing.JOptionPane;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ConfirmCreateConnectionFactory;
import org.openide.windows.WindowManager;

/**
 *
 */
public class ConfirmCreateConnectionImpl {
    @org.openide.util.lookup.ServiceProvider(service = ConfirmCreateConnectionFactory.class)
    public static final class ConfirmCreateConnectionFactoryImpl implements ConfirmCreateConnectionFactory {            

        @Override
        public ConfirmSupport.AutoConfirm createConnection(String dialogTitle, String message, String autoConfirmMessage) {
            int res = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), message, dialogTitle, JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                return new ConfirmSupport.AutoConfirm() {
                };
            }
            return null;
        }
        
    }    
}
