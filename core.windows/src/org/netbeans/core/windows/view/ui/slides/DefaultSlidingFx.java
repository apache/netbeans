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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeListener;

/**
 * default impl of SlidingFX.
 * Acts as command interface
 * for desktop part of winsys to be able to request slide operation.
 *
 * @author Milos Kleint
 */
public class DefaultSlidingFx  implements SlidingFx {


    public void showEffect(JLayeredPane pane, Integer layer, SlideOperation operation) {
//        Component comp = operation.getComponent();
//        Graphics2D gr2d = (Graphics2D)pane.getGraphics();
//        Color original = gr2d.getColor();
//        gr2d.setColor(Color.BLUE.darker().darker());
//        Rectangle start = operation.getStartBounds();
//        Rectangle finish = operation.getFinishBounds();
//        Rectangle current = start;
//        for (int i = 0; i < 6 /** magic constant **/; i++) {
//            Rectangle newRect;
//            if (i > 0) {
//                // wipe out old
//            } 
//            newRect = new Rectangle();
//            newRect.x = Math.abs((current.x + finish.x) / 2);
//            newRect.y = Math.abs((current.y + finish.y) / 2);
//            newRect.height = Math.abs((current.height + finish.height) / 2);
//            newRect.width = Math.abs((current.width + finish.width) / 2);
//            gr2d.drawRect(newRect.x, newRect.y, newRect.width, newRect.height);
//            gr2d.setColor(gr2d.getColor().brighter());
//            current = newRect;
//        }
//        gr2d.setColor(original);
////        try {
////            Thread.sleep(5000);
////        } catch (Throwable th) {
////            
////        }
        
    }
     
    public void prepareEffect(SlideOperation operation) {
        // no preparation needed
    }    
    
    public void setFinishListener(ChangeListener finishL) {
        // no noperation, operation don't need to wait
    }
    
    public boolean shouldOperationWait() {
        return false;
    }
    
}
