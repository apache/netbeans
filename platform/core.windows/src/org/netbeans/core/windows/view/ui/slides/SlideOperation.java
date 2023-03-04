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

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;
import org.netbeans.core.windows.Constants;
import org.openide.windows.TopComponent;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;

/*
 * Interface for slide in and slide out operations. Acts as command interface
 * for desktop part of winsys to be able to request slide operation.
 *
 * @author Dafe Simonek
 */
public interface SlideOperation {

    public static final int SLIDE_IN = 0;
    public static final int SLIDE_OUT = 1;
    public static final int SLIDE_INTO_EDGE = 2;
    public static final int SLIDE_INTO_DESKTOP = 3;
    public static final int SLIDE_RESIZE = 4;

    public Component getComponent ();
    
    public Rectangle getStartBounds ();
    
    public Rectangle getFinishBounds ();
    
    public String getSide ();

    public boolean requestsActivation ();
    
    public void run (JLayeredPane pane, Integer layer);
    
    public void setStartBounds (Rectangle bounds);
    
    public void setFinishBounds (Rectangle bounds);
    
    public int getType();

    public void prepareEffect();
    
}
