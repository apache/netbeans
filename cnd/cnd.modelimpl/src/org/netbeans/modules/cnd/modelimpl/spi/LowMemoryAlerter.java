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

package org.netbeans.modules.cnd.modelimpl.spi;

import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryEvent;

/**
 * The implementor of this SPI shoudl display a dialog to user
 *
 * @param event event that contains information concerning maximum memory and memory used
 *
 * @param fatal distingwishes two modes: 
 * if it is false, user should be just warned, so that he/she can switch off code model for the some or all projects;
 * if fatal is true, this mean that code model is already switched off for all projects.
 *
 */
public interface LowMemoryAlerter {
    void alert(LowMemoryEvent event, boolean fatal);
}
