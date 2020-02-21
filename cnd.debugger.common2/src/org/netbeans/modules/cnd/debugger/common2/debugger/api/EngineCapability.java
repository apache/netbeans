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

package org.netbeans.modules.cnd.debugger.common2.debugger.api;

/**
 * different engine capabilities
 */
public enum EngineCapability {
    DERIVE_EXECUTABLE, // can derive executable from PID or Core file
    RTC_SUPPORT, // has RTC support
    RUN_AUTOSTART, // can do auto start after loading
    STACK_VERBOSE, // stack args display can be on/off
    STACK_MAXFRAME, // can set # of displayed frame
    DYNAMIC_TYPE, // can display class with dynamic type
    INHERITED_MEMBERS, // can display class with inherited members
    STATIC_MEMBERS, // can display class with static members
    MAX_OBJECT, // can set object size for display variables
    PRETTY_PRINT, // can use pretty print
}
