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

package org.netbeans.editor.ext;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
*
* @author Dusan Balek
*/
public class KeyEventBlocker implements KeyListener {

    private LinkedList blockedEvents = new LinkedList();
    private JTextComponent component;
    private boolean discardKeyTyped = true;
    private static final boolean debugBlockEvent
    = Boolean.getBoolean("netbeans.debug.editor.blocker"); // NOI18N
    

    public KeyEventBlocker(JTextComponent component, boolean discardFirstKeyTypedEvent) {
        this.component = component;
        this.discardKeyTyped = discardFirstKeyTypedEvent;
        if (debugBlockEvent){
            System.out.println(""); //NOI18N
            System.out.println("attaching listener"+this.component.getClass()+" - "+this.component.hashCode()); //NOI18N
        }
        this.component.addKeyListener(this);
    }

    /** Has to be called from AWT event thread to be properly synchronized */
    public void stopBlocking(boolean dispatchBlockedEvents) {
        if (debugBlockEvent){
            System.out.println("removing listener from "+this.component.getClass()+" - "+this.component.hashCode()); //NOI18N
        }
        this.component.removeKeyListener(this);
        if (dispatchBlockedEvents){
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Component focusOwner = kfm.getFocusedWindow(); // or getFocusOwner()?
            while(!blockedEvents.isEmpty()) {
                KeyEvent e = (KeyEvent)blockedEvents.removeFirst();
                Component src = (focusOwner != null) ? focusOwner : (Component)e.getSource();
                e = new KeyEvent(src, e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), e.getKeyLocation());
                kfm.dispatchEvent(e);
            }
        }
    }
    
    public void stopBlocking() {
        stopBlocking(true);
    }

    public void keyPressed(KeyEvent e) {
        if (debugBlockEvent){
            System.out.println("consuming keyPressed event:"+KeyEvent.getKeyModifiersText(e.getModifiers())+" + "+KeyEvent.getKeyText(e.getKeyCode())); //NOI18N
        }
        e.consume();
        blockedEvents.add(e);
    }

    public void keyReleased(KeyEvent e) {
        if (debugBlockEvent){
            System.out.println("consuming keyReleased event:"+KeyEvent.getKeyModifiersText(e.getModifiers())+" + "+KeyEvent.getKeyText(e.getKeyCode())); //NOI18N
        }
        e.consume();
        blockedEvents.add(e);
    }

    public void keyTyped(KeyEvent e) {
        if (debugBlockEvent){
            System.out.println("consuming keyTyped event:"+KeyEvent.getKeyModifiersText(e.getModifiers())+" + "+KeyEvent.getKeyText(e.getKeyCode())); //NOI18N
        }
        e.consume();
        if (discardKeyTyped) {
            discardKeyTyped = false;
        } else {
            blockedEvents.add(e);
        }
    }
}
