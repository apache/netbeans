/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
