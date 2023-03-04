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

package org.netbeans.editor;

import java.lang.ref.WeakReference;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;

/**
 * Action listener that has a weak reference
 * to the source action listener so it doesn't prevent
 * it to be garbage collected.
 * The calls to the <code>actionPerformed</code> are automatically
 * propagated to the source action listener.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class WeakTimerListener implements ActionListener {

    private WeakReference ref;

    private boolean stopTimer;

    /** Construct new listener with automatic timer stopping.
     */
    public WeakTimerListener(ActionListener source) {
        this(source, true);
    }

    /** Construct new listener.
     * @param source source action listener to which this listener delegates.
     * @param stopTimer whether the timer should be stopped automatically when
     *  the timer fires and the source listener was garbage collected.
     */
    public WeakTimerListener(ActionListener source, boolean stopTimer) {
        this.ref = new WeakReference(source);
        this.stopTimer = stopTimer;
    }

    public void actionPerformed(ActionEvent evt) {
        ActionListener src = (ActionListener)ref.get();
        if (src != null) {
            src.actionPerformed(evt);

        } else { // source listener was garbage collected
            if (evt.getSource() instanceof Timer) {
                Timer timer = (Timer)evt.getSource();
                timer.removeActionListener(this);

                if (stopTimer) {
                    timer.stop();
                }
            }
        }
    }

}
