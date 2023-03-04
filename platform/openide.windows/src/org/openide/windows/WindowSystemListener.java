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
package org.openide.windows;

import java.util.EventListener;

/**
 * Window system listener to receive notifications when the window system loads
 * or saves its state to disk.
 * 
 * @see WindowManager#addWindowSystemListener(WindowSystemListener)
 * 
 * @author S. Aubrecht
 * @since 6.43
 */
public interface WindowSystemListener extends EventListener {
    
    /**
     * Invoked from EDT before the window system loads.
     * @param event 
     */
    void beforeLoad( WindowSystemEvent event );
    
    /**
     * Invoked from EDT after the window system finished loading and is about
     * to show the main window and its content.
     * @param event 
     */
    void afterLoad( WindowSystemEvent event );
    
    /**
     * Invoked from EDT before the window system starts saving its state to disk.
     * @param event 
     */
    void beforeSave( WindowSystemEvent event );
    
    /**
     * Invoked from EDT when the window system finished saving its state to disk.
     * @param event 
     */
    void afterSave( WindowSystemEvent event );
}
