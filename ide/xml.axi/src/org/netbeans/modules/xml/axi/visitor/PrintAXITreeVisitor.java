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

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.Compositor;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class PrintAXITreeVisitor extends DeepAXITreeVisitor {
            
    /**
     * Creates a new instance of PrintAXITreeVisitor
     */
    public PrintAXITreeVisitor() {
        super();
    }
    
    protected void visitChildren(AXIComponent component) {
        if(PRINT_TO_CONSOLE)
            printModel(component);
        
        depth++;
        super.visitChildren(component);
        depth--;
    }
    
    private void printModel(AXIComponent component) {
        StringBuffer buffer = new StringBuffer();
        if(component instanceof Compositor) {
            Compositor compositor = (Compositor)component;
            buffer.append((getTab() == null) ? compositor : getTab() + compositor);
            buffer.append("<min=" + compositor.getMinOccurs() + ":max=" + compositor.getMaxOccurs() + ">");
        }
        if(component instanceof Element) {
            Element element = (Element)component;
            buffer.append((getTab() == null) ? element.getName() : getTab() + element.getName());
            if(element.getAttributes().size() != 0) {
                buffer.append("<" + getAttributes(element) + ">");
            }
            buffer.append("<min=" + element.getMinOccurs() + ":max=" + element.getMaxOccurs() + ">");
        }
        if(component instanceof AnyElement) {
            AnyElement element = (AnyElement)component;
            buffer.append((getTab() == null) ? element : getTab() + element);
        }
        
        System.out.println(buffer.toString());
    }
        
    
    private String getAttributes(Element element) {
        StringBuffer attrs = new StringBuffer();
        for(AbstractAttribute attr : element.getAttributes()) {
            attrs.append(attr+":");
        }
        if(attrs.length() > 0)
            return attrs.toString().substring(0, attrs.length()-1);
        else
            return attrs.toString();
    }
    
    private String getTab() {
        String tabStr = "++++";
        
        if(depth == 0) {
            return null;
        }
        
        StringBuffer tab = new StringBuffer();
        for(int i=0; i<depth ; i++) {
            tab.append(tabStr);
        }
        return tab.toString();
    }
    
    private int depth = 0;
    private static boolean PRINT_TO_CONSOLE= false;
}
