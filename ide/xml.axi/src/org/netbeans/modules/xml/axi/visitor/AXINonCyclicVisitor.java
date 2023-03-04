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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.impl.SchemaGeneratorUtil;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * Deep non-cyclic visitor for AXI
 *
 * @author Ayub Khan
 */
public class AXINonCyclicVisitor extends DeepAXITreeVisitor {
    
    List<AXIComponent> path = null;
    
    private AXIModel am;
    
    private SchemaModel sm;
    
    /**
     * Creates a new instance of AXINonCyclicVisitor
     */
    public AXINonCyclicVisitor(AXIModel am) {
        this.am = am;
        this.sm = am.getSchemaModel();
        path = new ArrayList<AXIComponent>();
    }
    
    public void expand(AXIDocument root) {
        path.clear();
        if(root != null) {
            for(AXIComponent c : root.getChildren()) {//deep visit
                c.accept(this);
            }
        }
    }
    
    //Deep visit to populate AXI model, visit only the least referenced
    //global element, that references most global elements
    public void expand(List<Element> elements) {
        path.clear();
        for(Element e : elements) {//deep visit
            e.accept(this);
        }
    }
    
    public void visit(Element e) {
        if(!canVisit(e)) //skip recursion
            return;
        visitChildren(e);
    }
    
    public boolean canVisit(Element e) {
        Element orig = getOriginalElement(e);
        if(!SchemaGeneratorUtil.fromSameSchemaModel(orig, sm) ||
                path.size() > 0 && path.contains(orig)) //skip recursion
            return false;
        return true;
    }
    
    public void visitChildren(Element e) {
        Element orig = getOriginalElement(e);
        path.add(orig);
        try {
            super.visit(e);//now visit children
        } finally {
            path.remove(path.size()-1);
        }
    }
    
    private Element getOriginalElement(final Element e) {
        Element orig = e;
        if(orig.isReference()) {
            orig = SchemaGeneratorUtil.findOriginalElement(e);
        }
        return orig;
    }
}
