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
package org.netbeans.modules.xml.axi.visitor;

import java.util.Stack;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.impl.ElementRef;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class DeepAXITreeVisitor extends DefaultVisitor {
        
    Stack<AXIComponent> pathToRoot = new Stack<AXIComponent>();
    
    /**
     * Creates a new instance of DeepAXITreeVisitor
     */
    public DeepAXITreeVisitor() {
        super();
    }
    
    public void visit(AXIDocument root) {
        visitChildren(root);
    }
    
    public void visit(Element element) {        
        visitChildren(element);
    }
    
    public void visit(AnyElement element) {
        visitChildren(element);
    }
    
    public void visit(Attribute attribute) {        
        visitChildren(attribute);
    }
        
    public void visit(AnyAttribute attribute) {
        visitChildren(attribute);
    }
    
    public void visit(Compositor compositor) {        
        visitChildren(compositor);
    }
            
    public void visit(ContentModel element) {
        visitChildren(element);
    }
        
    protected void visitChildren(AXIComponent component) {
        if( !canVisit(component) )
            return;
                
        pathToRoot.push(component.getOriginal());
        for(AXIComponent child: component.getChildren()) {
            child.accept(this);
        }
        pathToRoot.pop();
    }
        
    protected boolean canVisit(AXIComponent component) {        
        if(pathToRoot.contains(component))
            return false;
        
        if(component.getComponentType() == ComponentType.PROXY)
            return canVisit(component.getOriginal());

        if(component.getComponentType() == ComponentType.REFERENCE &&
           component instanceof ElementRef) {
            ElementRef ref = (ElementRef)component;
            Element e = ref.getReferent();
            if(pathToRoot.contains(e))
                return false;
        }
        
        return true;
    }
    
}
