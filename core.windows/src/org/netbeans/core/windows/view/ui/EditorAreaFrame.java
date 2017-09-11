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


package org.netbeans.core.windows.view.ui;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.netbeans.core.windows.view.Controller;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;


/**
 * Frame representing separate editor area.
 *
 * @author  Peter Zavadsky
 */
public class EditorAreaFrame extends JFrame {


    private Component desktop;
    private Controller controller;
    private long frametimestamp = 0;

    /** Creates a new instance of EditorAreaFrame */
    public EditorAreaFrame() {
        super(NbBundle.getMessage(EditorAreaFrame.class, "LBL_EditorAreaFrameTitle"));
        
        MainWindow.initFrameIcons(this);
    }
    
    public void setWindowActivationListener(Controller control) {
        controller = control;
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent evt) {
                if (frametimestamp != 0 && System.currentTimeMillis() > frametimestamp + 500) {
                    controller.userActivatedEditorWindow();
                }
            }
            public void windowOpened(WindowEvent event) {
                frametimestamp = System.currentTimeMillis();
            }
        });
    }
    
    public void toFront() {
        // ignore the window activation event, is not done by user.
        frametimestamp = System.currentTimeMillis();
        super.toFront();
    }
    
    public void setVisible(boolean visible) {
        frametimestamp = System.currentTimeMillis();
        super.setVisible(visible);
    }
    
    public void setDesktop(Component component) {
        if(desktop == component) {
            return;
        }
        
        if(desktop != null) {
            getContentPane().remove(desktop);
        }
        
        desktop = component;
        
        if(component != null) {
            getContentPane().add(component);
        }
    }

    private long timeStamp = 0; 
    
    public void setUserStamp(long stamp) {
        timeStamp = stamp;
    }
    
    public long getUserStamp() {
        return timeStamp;
    }
    
    private long mainWindowStamp = 0;
    
    public void setMainWindowStamp(long stamp) {
        mainWindowStamp = stamp;
    }
    
    public long getMainWindowStamp() {
        return mainWindowStamp;
    }
    
    
    
}
