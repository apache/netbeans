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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;

/** 
 *
 * @see SlidingButtonUI
 *
 * @author  Milos Kleint
 */
public class NimbusSlidingButtonUI extends SlidingButtonUI {
    // Has the shared instance defaults been initialized?
    private boolean defaults_initialized = false;   
    protected JToggleButton hiddenToggle;
    
    private static final NimbusSlidingButtonUI INSTANCE = new NimbusSlidingButtonUI();

    private NimbusSlidingButtonUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }    
    
    
    @Override
    public void installDefaults (AbstractButton b) {
        super.installDefaults(b);
	if(!defaults_initialized) {
            hiddenToggle = new JToggleButton();
            hiddenToggle.setText("");
            JToolBar bar = new JToolBar();
            bar.setRollover(true);
            bar.add(hiddenToggle);
	    defaults_initialized = true;
	}
    }
    
    @Override
    protected void uninstallDefaults(AbstractButton b) {
	super.uninstallDefaults(b);
	defaults_initialized = false;
    }   
    
    @Override
    public void paint(Graphics g, JComponent c) {
        ColorUtil.setupAntialiasing(g);
        
        AbstractButton button = (AbstractButton)c;
        hiddenToggle.setBorderPainted(true);
//        hiddenToggle.setBorderPainted(button.isBorderPainted());
        hiddenToggle.setRolloverEnabled(button.isRolloverEnabled());
        hiddenToggle.setFocusable(button.isFocusable());
        hiddenToggle.setFocusPainted(button.isFocusPainted());
        hiddenToggle.setMargin(button.getMargin());
//        hiddenToggle.setBorder(button.getBorder());
        hiddenToggle.getModel().setRollover(button.getModel().isRollover());
        hiddenToggle.getModel().setPressed(button.getModel().isPressed());
        hiddenToggle.getModel().setArmed(button.getModel().isArmed());
        hiddenToggle.getModel().setSelected(button.getModel().isSelected());
        
        hiddenToggle.setBounds(button.getBounds());
        super.paint(g, c);
    }
    
    @Override
    protected void paintBackground (Graphics2D g, AbstractButton button) {
        hiddenToggle.paint(g);
    }
    
    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {    
        hiddenToggle.paint(g);
    }
    
}
