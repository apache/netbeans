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
package org.netbeans.modules.progress.spi;

import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;

/**
 * SPI that allows the base classes to create Handles and Controllers specialized
 * for the target environment. The SPI is to be implemented by the platform where
 * Progress API is deployed.
 * 
 * @author sdedic
 * @since 1.40
 */
public interface ProgressEnvironment {
    /**
     * Creates a ProgressHandle with the specified parameters. The creation includes
     * also creation and initialization of the InternalHandle.
     * 
     * @param displayname the display name
     * @param c callback that can cancel the operation, optional - can be {@code null}
     * @param userInit true, if initiated by the user, false if initiated automatically
     * @return ProgressHandle instance
     */
    public ProgressHandle createHandle(String displayname, Cancellable c, boolean userInit);
    
    /**
     * Creates a Controller specific for the target environment. The method is called
     * to obtain the <b>default</b> controller instance.
     * @return initialized Controller
     */
    public Controller     getController();
}
