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
package org.netbeans.modules.visual.border;

import java.awt.*;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author alex_grk
 */
public final class FancyDashedBorder extends DashedBorder {

    private static int focusField=5;
    private static int rectSize=5;
    private static int halfRectSize=rectSize/2;
    private static int rect15Size=rectSize+halfRectSize;

    public FancyDashedBorder(Color color, int width, int height) {
        super (color, width, height);
    }
    
    public FancyDashedBorder (String property, Widget associated, int width, int height) {
        super (property, associated, width, height);
    }
    
    public FancyDashedBorder (String property, ResourceTable table, int width, int height) {
        super (property, table, width, height);
    }
    
    public void paint(Graphics2D g, Rectangle bounds) {
        int x=bounds.x,y=bounds.y,width=bounds.width,height=bounds.height;
        //x=x+halfRectSize;y=y+halfRectSize;width=width-rectSize;height=height-rectSize;
        bounds.x=bounds.x+focusField/2;bounds.y=bounds.y+focusField/2;bounds.width=bounds.width-focusField;bounds.height=bounds.height-focusField;
        super.paint(g,bounds);
        g.drawRect(x,y,rectSize,rectSize);
        g.drawRect(x+width/2-halfRectSize,y,rectSize,rectSize);
        g.drawRect(x+width-rect15Size,y,rectSize,rectSize);
        g.drawRect(x+width-rect15Size,y-halfRectSize+height/2,rectSize,rectSize);
        g.drawRect(x+width-rect15Size,y-rect15Size+height,rectSize,rectSize);
        g.drawRect(x+width/2-halfRectSize,y-rect15Size+height,rectSize,rectSize);
        g.drawRect(x,y-rect15Size+height,rectSize,rectSize);
        g.drawRect(x,y-rectSize+height/2,rectSize,rectSize);
    }
    
}
