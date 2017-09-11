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

package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.Element;

/**
 * Proxy compositor, acts on behalf of an original Compositor.
 * Delegates all calls to the original Compositor.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompositorProxy extends Compositor implements AXIComponentProxy {
            
    
    /**
     * Creates a new instance of CompositorProxy
     */
    public CompositorProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
            
    private Compositor getShared() {
        return (Compositor)getSharedComponent();
    }
    
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
    
    /**
     * Returns the type of this content model.
     */
    public CompositorType getType() {
        return getShared().getType();
    }
    
    /**
     * Returns the type of this content model.
     */
    public void setType(CompositorType value) {
        getShared().setType(value);
    }
    
    /**
     * Returns the min occurs.
     */
    public String getMinOccurs() {
        return getShared().getMinOccurs();            
    }
    
    public void setMinOccurs(String value) {
        getShared().setMinOccurs(value);
    }
    
    /**
     * Returns the max occurs.
     */
    public String getMaxOccurs() {
        return getShared().getMaxOccurs();
    }
    
    public void setMaxOccurs(String value) {
        getShared().setMaxOccurs(value);
    }
        
    /**
     * Adds a Compositor as its child.
     */
    public void addCompositor(CompositorProxy compositor) {
        getShared().addCompositor(compositor);
    }
    
    /**
     * Removes an Compositor.
     */
    public void removeCompositor(CompositorProxy compositor) {
        getShared().removeCompositor(compositor);
    }
    
    /**
     * Adds an Element as its child.
     */
    public void addElement(Element element) {
        getShared().addElement(element);
    }
        
    /**
     * Removes an Element.
     */
    public void removeElement(Element element) {
        getShared().removeElement(element);
    }
    
    public String toString() {        
        return getShared().toString();
    }
    
}
