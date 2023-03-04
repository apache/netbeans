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

package org.netbeans.core.ui.warmup;

import java.awt.GraphicsEnvironment;
import java.awt.dnd.DragSource;
import org.openide.util.lookup.ServiceProvider;

/** DnD pre-heat task. Initializes drag and drop by calling
 * DragSource.getDefaultDragSource();, which is expensive because of loading
 * of fonts.
 * May be executed by the core after startup to speed-up initialization
 * of explorer or other DnD sources and targets.
 *
 * @author  Dafe Simonek
 */
@ServiceProvider(service=Runnable.class, path="WarmUp")
public final class DnDWarmUpTask implements Runnable {

    /** Performs DnD pre-heat.
     */
    @Override
    public void run() {
        if (!GraphicsEnvironment.isHeadless()) {
            DragSource.getDefaultDragSource();
        }
    }

}
