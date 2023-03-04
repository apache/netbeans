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
