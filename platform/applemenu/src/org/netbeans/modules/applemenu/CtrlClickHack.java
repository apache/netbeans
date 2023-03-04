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

package org.netbeans.modules.applemenu;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

/**
 * hack for issue #67799, on macosx with single button mouse,
 * make Ctrl-Click work as right click on multiselections
 *
 * Also handles issue #90371 - on Macintosh, JTextComponents
 * are never sent focus lost events, resulting in multiple
 * blinking carets.  Hack tracks last known JTextComponent
 * and sets its cursor to invisible if any other component 
 * gains focus (on Mac OS, getOppositeComponent() 
 * frequently returns null when coming from a JTextComponent)
 *
 * @author ttran, Tim Boudreau
 */
public class CtrlClickHack implements AWTEventListener {
    private Reference<JTextComponent> lastFocusedTextComponent = null;

    public void eventDispatched(AWTEvent e) {
        if (!(e instanceof MouseEvent) && !(e instanceof FocusEvent)) {
            return;
        }
        if (e instanceof FocusEvent) {
            FocusEvent fe = (FocusEvent) e;
            if (fe.getID() == FocusEvent.FOCUS_GAINED) {
                if (fe.getOppositeComponent() instanceof JTextComponent) {
                    JTextComponent jtc = (JTextComponent) fe.getOppositeComponent();
                    if (null != jtc) {
                        Caret caret = jtc.getCaret();
                        if (null != caret) {
                            caret.setVisible(false);
                        }
                    }
                } else {
                    JTextComponent jtc = lastFocusedTextComponent == null ? null :
                        lastFocusedTextComponent.get();
                    if (null != jtc) {
                        Caret caret = jtc.getCaret();
                        if (null != caret)
                            caret.setVisible(false);
                    }
                }
                if (fe.getComponent() instanceof JTextComponent) {
                    JTextComponent jtc = (JTextComponent) fe.getComponent();
                    lastFocusedTextComponent = new WeakReference<JTextComponent>(jtc);
                    if (null != jtc) {
                        Caret caret = jtc.getCaret();
                        if (null != caret) {
                            caret.setVisible(true);
                        }
                    }
                }
            }
            return;
        }
        MouseEvent evt = (MouseEvent) e;
        if (evt.getModifiers() != (InputEvent.BUTTON1_MASK | InputEvent.CTRL_MASK)) {
            return;
        }
        try {
            Field f1 = InputEvent.class.getDeclaredField("modifiers");
            Field f2 = MouseEvent.class.getDeclaredField("button");
            Method m = MouseEvent.class.getDeclaredMethod("setNewModifiers", new Class[] {});
            f1.setAccessible(true);
            f1.setInt(evt, InputEvent.BUTTON3_MASK);
            f2.setAccessible(true);
            f2.setInt(evt, MouseEvent.BUTTON3);
            m.setAccessible(true);
            m.invoke(evt, new Object[] {});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
