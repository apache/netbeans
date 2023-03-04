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
package org.netbeans.modules.web.common.ui;

import java.awt.EventQueue;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.web.common.spi.ExternalExecutableUserWarning;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author martin
 */
public class ExternalExecutionUserWarningImpl implements ExternalExecutableUserWarning {

    @Override
    public void displayError(final String error, final String optionsPath) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(error, NotifyDescriptor.ERROR_MESSAGE));
                if (optionsPath != null) {
                    OptionsDisplayer.getDefault().open(optionsPath);
                }
            }
        });
    }
    
}
