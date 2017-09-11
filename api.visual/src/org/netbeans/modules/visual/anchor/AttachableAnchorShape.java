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
