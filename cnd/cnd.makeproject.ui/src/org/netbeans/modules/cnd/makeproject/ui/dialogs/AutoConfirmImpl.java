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

import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.AutoConfirm;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.AutoConfirmFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 */
public class AutoConfirmImpl implements AutoConfirm {
    @org.openide.util.lookup.ServiceProvider(service=AutoConfirmFactory.class)
    public static final class AutoConfirmFactoryImpl implements AutoConfirmFactory {

        @Override
        public AutoConfirm create(String dialogTitle, String message, String autoConfirmMessage) {
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(message,
                    dialogTitle,
                    NotifyDescriptor.YES_NO_OPTION);
            Object notify = DialogDisplayer.getDefault().notify(d);
            if (notify == NotifyDescriptor.YES_OPTION) {
                return new AutoConfirmImpl();
            }
            return null;
        }
    }
    
}
