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
        int retVal;

        if(attachedWidget != null)
        {
            Rectangle bounds = attachedWidget.getBounds();
            retVal = getLocation(bounds);
        }
        else
        {
            Rectangle bounds;
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
