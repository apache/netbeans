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
package org.netbeans.modules.notifications.linux.jna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 *
 * @author Hector Espert
 */
public interface Libnotify extends Library {
    
    boolean notify_is_initted();
    
    boolean notify_init(String app_name);
    
    void notify_uninit();
    
    Pointer notify_notification_new(String summary, String body, String icon);
    
    boolean notify_notification_show(Pointer notification, Pointer error);
    
}
