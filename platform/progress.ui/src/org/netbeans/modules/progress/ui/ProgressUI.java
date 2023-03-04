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
package org.netbeans.modules.progress.ui;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.ProgressEnvironment;
import org.netbeans.modules.progress.spi.SwingController;
import org.openide.util.Cancellable;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = ProgressEnvironment.class)
public class ProgressUI implements ProgressEnvironment {

    @Override
    public ProgressHandle createHandle(String displayname, Cancellable c, boolean userInit) {
        if (userInit) {
            return ProgressHandleFactory.createUIHandle(displayname, c, null);
        } else {
            return ProgressHandleFactory.createSystemUIHandle(displayname, c, null);
        }
    }

    @Override
    public Controller getController() {
        return SwingController.getDefault();
    }
    
}
