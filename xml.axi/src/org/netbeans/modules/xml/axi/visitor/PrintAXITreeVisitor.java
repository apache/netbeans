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
