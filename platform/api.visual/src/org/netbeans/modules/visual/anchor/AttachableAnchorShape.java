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

import org.netbeans.api.visual.anchor.AnchorShapeLocationResolver;
import java.awt.Graphics2D;
import org.netbeans.api.visual.anchor.AnchorShape;

/**
 *
 * @author treyspiva
 */
public class AttachableAnchorShape implements AnchorShape
{
    private AnchorShape shape = null;
    private AnchorShapeLocationResolver locationResolver = null;
    
    public AttachableAnchorShape()
    {
        this(AnchorShape.NONE);
    }
    
    public AttachableAnchorShape(AnchorShapeLocationResolver resolver)
    {
        this(AnchorShape.NONE, resolver);
    }
    
    public AttachableAnchorShape(AnchorShape shape)
    {
        this(shape, null);
    }
    
    public AttachableAnchorShape(AnchorShape shape, 
                                 AnchorShapeLocationResolver resolver)
    {
        this.shape = shape;
        this.locationResolver = resolver;
    }

    public AnchorShape getShape()
    {
        return shape;
    }

    public void setShape(AnchorShape shape)
    {
        this.shape = shape;
    }

    public AnchorShapeLocationResolver getLocationResolver()
    {
        return locationResolver;
    }

    public void setLocationResolver(AnchorShapeLocationResolver locationResolver)
    {
        this.locationResolver = locationResolver;
    }
    
    public boolean isLineOriented()
    {
        return shape.isLineOriented();
    }

    public int getRadius()
    {
        return shape.getRadius();
    }

    public double getCutDistance()
    {
        
        return shape.getCutDistance() + getAdditionalDistance();
    }

    public void paint(Graphics2D graphics, boolean source)
    {
        int distance = getAdditionalDistance();
        graphics.translate(distance, 0);
        
        shape.paint(graphics, source);
        
        graphics.translate(-distance, 0);
    }

    protected int getAdditionalDistance()
    {
        int retVal = 0;

        if (locationResolver != null)
        {
            retVal = locationResolver.getEndLocation();
        }

        return retVal;
    }
}
