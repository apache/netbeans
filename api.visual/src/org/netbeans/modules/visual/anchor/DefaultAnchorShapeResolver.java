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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.anchor.AnchorShapeFactory.ConnectionEnd;
import org.netbeans.api.visual.anchor.AnchorShapeLocationResolver;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author treyspiva
 */
public class DefaultAnchorShapeResolver implements AnchorShapeLocationResolver
{
    private ConnectionWidget connection = null;
    private ConnectionEnd attachedEnd = ConnectionEnd.SOURCE;
    private Widget attachedWidget = null;
    
    public DefaultAnchorShapeResolver(ConnectionWidget connection,
                                      ConnectionEnd attachedTo)
    {
        this(connection, attachedTo, null);
    }
    
    public DefaultAnchorShapeResolver(ConnectionWidget connection,
                                      ConnectionEnd attachedTo,
                                      Widget attachedWidget)
    {
        this.connection = connection;
        this.attachedEnd = attachedTo;
        this.attachedWidget = attachedWidget;
    }
    
    public int getEndLocation()
    {
        int retVal = 0;
        
        if(attachedWidget != null)
        {
            Rectangle bounds = attachedWidget.getBounds();
            retVal = getLocation(bounds);
        }
        else
        {
//            Rectangle bounds = attachedWidget.getBounds();
            Rectangle bounds = new Rectangle();
            if(attachedEnd == ConnectionEnd.SOURCE)
            {
                Widget sourceWidget = getConnection().getSourceAnchor().getRelatedWidget();
                bounds = sourceWidget.getBounds();
            }
            else
            {
                Widget sourceWidget = getConnection().getTargetAnchor().getRelatedWidget();
                bounds = sourceWidget.getBounds();
            }
            retVal = getLocation(bounds);
        }
        
        return retVal;
    }

    public Widget getAttachedWidget()
    {
        return attachedWidget;
    }

    public void setAttachedWidget(Widget attachedWidget)
    {
        this.attachedWidget = attachedWidget;
    }
    
    public ConnectionWidget getConnection()
    {
        return connection;
    }

    public ConnectionEnd getAttachedEnd()
    {
        return attachedEnd;
    }
    
    protected int getLocation(Rectangle bounds)
    {
        int retVal = 0;
        
        Point endPt = null;
        Rectangle widgetBounds = null;
        
        if(attachedEnd == ConnectionEnd.SOURCE)
        {
            endPt = connection.getFirstControlPoint();
            widgetBounds = getSourceBounds();
        }
        else
        {
            endPt = connection.getLastControlPoint();
            widgetBounds = getTargetBounds();
        }
        
        if(widgetBounds != null)
        {
            if((endPt.x == widgetBounds.x) && (endPt.y == widgetBounds.y))
            {
                System.out.println("Top Left Corner");
                retVal = bounds.width;
            }
            else if((endPt.x == widgetBounds.x) && (endPt.y == (widgetBounds.y + widgetBounds.height)))
            {
                System.out.println("Bottom Left Corner");
                retVal = bounds.width;
            }
            if((endPt.x == (widgetBounds.x + widgetBounds.width)) && (endPt.y == widgetBounds.y))
            {
                System.out.println("Top Right Corner");
                retVal = bounds.width;
            }
            else if((endPt.x == (widgetBounds.x + widgetBounds.width)) && (endPt.y == (widgetBounds.y + widgetBounds.height)))
            {
                System.out.println("Bottom Right Corner");
                retVal = bounds.width;
            }
            else if(endPt.x <= widgetBounds.x)
            {
                retVal = bounds.width;
            }
            else if(endPt.x >= (widgetBounds.x + widgetBounds.width))
            {
                retVal = bounds.width;
            }
            else if(endPt.y <= widgetBounds.y)
            {
                retVal = bounds.height;
            }
            else if(endPt.y >= (widgetBounds.y + widgetBounds.height))
            {
                retVal = bounds.height;
            }
        }
        
        return retVal;
    }
    
    private Rectangle getSourceBounds()
    {
        Widget source = connection.getSourceAnchor().getRelatedWidget();
             
        if(source != null)
        {
            Point sourceLocation = source.getLocation();
            Rectangle clientArea = source.getClientArea();
            return new Rectangle(sourceLocation, clientArea.getSize());
        }
        
        return null;
    }

    private Rectangle getTargetBounds()
    {
        Widget target = connection.getTargetAnchor().getRelatedWidget();
                
        if(target != null)
        {
            Point targetLocation = target.getLocation();
            Rectangle targetArea = target.getClientArea();
            return new Rectangle(targetLocation, targetArea.getSize());
        }
        
        return null;
    }
}
