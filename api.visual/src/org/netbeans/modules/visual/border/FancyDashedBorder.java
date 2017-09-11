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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
