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
package org.netbeans.modules.versioning.util.common;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/**
 * Mouse adapter for the commit message component.
 * 
 * @author Mario Schroeder
 */
public class CommitMessageMouseAdapter extends MouseAdapter {

    private CommitPopupBuilder popupBuilder;

    /**
     * Creates a new context popupMenu for a text component.
     * @param textComponent 
     */
    public CommitMessageMouseAdapter() {

        popupBuilder = new CommitPopupBuilder();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) { 
            show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * Shows the popup popupMenu if the invoker is a instance of JTextComponent.
     */
    private void show(Component invoker, int x, int y) {
        
        //to avoid class cast exception in action listener
        if (invoker instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent)invoker;
            JPopupMenu popupMenu = popupBuilder.getPopup(textComponent);
            popupMenu.setInvoker(invoker);
            popupMenu.show(invoker, x, y);
        }
    }

   

}
