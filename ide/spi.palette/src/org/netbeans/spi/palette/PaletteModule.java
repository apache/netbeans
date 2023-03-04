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

package org.netbeans.spi.palette;

import java.awt.EventQueue;
import org.openide.modules.ModuleInstall;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;

/** @deprecated Don't rely on presence of this class, its existence is
 * a mere internal implementation detail, it should have never been part
 * of the palette SPI.
 *
 * @author S. Aubrecht
 * @since 1.10
 */
@OnShowing
@Deprecated
public class PaletteModule extends ModuleInstall implements Runnable {
    
    /** Creates a new instance of ModuleInstall */
    public PaletteModule() {
    }
    
    @Override
    public void restored() {
        super.restored();
        WindowManager.getDefault().invokeWhenUIReady(this);
    }

    @Override
    public void run() {
        assert EventQueue.isDispatchThread();
        
        //start listening to activated TopComponents and Nodes 
        //to see if palette window should be displayed
        PaletteSwitch.getDefault().startListening();
    }
}
