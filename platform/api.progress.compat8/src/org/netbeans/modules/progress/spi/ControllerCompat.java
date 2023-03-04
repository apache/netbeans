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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.openide.modules.PatchFor;

/**
 * This is a compatibility class for Controller SPI.
 * Timer field was removed, and the getVisualComponent method as well.
 * The timer is initially initialized using ordinary Swing Timer, and it triggers
 * runEvents method as in the SwingController class. If a client instantiates Controller
 * directly (not the SwingController subclass), it will get a non-null timer that fires events.
 * <p/>
 * SwingController contains a hack which will reflectively call the {@link #compatPostInit}
 * to provide the same timer instance as actually used for scheduling for better compatibility.
 * @author sdedic
 */
@PatchFor(Controller.class)
public class ControllerCompat {
    /**
     * This field is added for compatibility
     */
    protected Timer     timer;
    
    public Component getVisualComponent() {
        Object component = ((Controller)(Object)this).getProgressUIWorker();
        if (component instanceof Component) {
            return (Component)component;
        }
        return null;
    }
    
    public ControllerCompat() {
       timer = new Timer(400, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Controller)(Object)this).runEvents();
            }
        });
        timer.setRepeats(false);
    }
    
    protected void compatPostInit(Timer timer) {
        this.timer = timer;
    }
}
