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
package org.netbeans.modules.javascript.v8debug.ui;

import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin
 */
@ServiceProvider(service = V8Debugger.ErrorMessageHandler.class)
public class ErrorMessageHandlerImpl implements V8Debugger.ErrorMessageHandler {

    @Override
    public void errorResponse(String error) {
        StatusDisplayer.getDefault().setStatusText(error);
    }

    @Override
    public void errorEvent(String error) {
        NotifyDescriptor nde = new NotifyDescriptor.Message(error, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nde);
    }
    
}
