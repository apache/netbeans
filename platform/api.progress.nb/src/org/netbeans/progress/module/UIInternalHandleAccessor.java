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
package org.netbeans.progress.module;

import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.UIInternalHandle;

/**
 *
 * @author sdedic
 */
public abstract class UIInternalHandleAccessor {
    private static UIInternalHandleAccessor INSTANCE;
    
    public static void setInstance(UIInternalHandleAccessor acc) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = acc;
    }
    
    public static UIInternalHandleAccessor instance() {
        return INSTANCE;
    }
    
    public abstract void setController(InternalHandle h, Controller c);
    
    public abstract void markCustomPlaced(InternalHandle h);
    
    static {
        // force creation -> accessor is registered.
        new UIInternalHandle("", null, false, null);
    }
}
