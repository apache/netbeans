/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.lib2;

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

    private LinkedList<KeyEvent> blockedEvents = new LinkedList<KeyEvent>();
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
            while(!blockedEvents.isEmpty()) {
                KeyEvent e = blockedEvents.removeFirst();
                e = new KeyEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), e.getKeyLocation());
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
