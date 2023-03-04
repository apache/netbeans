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

package org.netbeans.core.windows.view.ui;

import org.openide.awt.StatusDisplayer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/** The status line component of the main window.
*
* @author Jaroslav Tulach, Jesse Glick
*/
final class StatusLine extends JLabel implements ChangeListener, Runnable {
    
    private StatusDisplayer d = StatusDisplayer.getDefault();
    
    /** Creates a new StatusLine */
    public StatusLine () {
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        run();
        d.addChangeListener(this);
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        d.removeChangeListener(this);
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        Font f = UIManager.getFont ("controlFont"); //NOI18N
        if (f == null) {
            f = UIManager.getFont ("Tree.font"); //NOI18N
        }
        if (f != null) {
            setFont(f);
        }
    }

    public void stateChanged(ChangeEvent e) {
        if(SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater (this);
        }
    }
    
    /** Called in event queue, should update the status text.
    */
    public void run () {
        String currentMsg = d.getStatusText ();
        setText (currentMsg);
    }
    
    /** #62967: Pref size so that status line is able to shrink as much as possible.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, super.getPreferredSize().height);
    }
    
    /** #62967: Minimum size so that status line is able to shrink as much as possible.
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, super.getMinimumSize().height);
    }

}
