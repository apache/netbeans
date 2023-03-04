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

import org.netbeans.modules.xml.axi.impl.Preview;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Element;

/**
 *
 * @author Ayub Khan
 */
public class FindUsageVisitor extends AXINonCyclicVisitor {
    
    Preview p = null;
    
    Element usedBy = null;
    
    /** Creates a new instance of FindUsageVisitor */
    public FindUsageVisitor(AXIModel am) {
        super(am);
        p = new Preview();
    }
    
    public Preview findUsages(AXIDocument root) {
        if(root == null) return null;
        java.util.List<Element> axiges = root.getElements();
        for(Element e : axiges) {
            findUsages(e);
        }
        return p;
    }
    
    public Preview findUsages(Element e) {
        usedBy = e;
        p.addToUsage(e, usedBy);
        for(AXIComponent child: e.getChildren()) {
            child.accept(this);
        }
        return p;
    }
    
    public void visit(Element e) {
        if(!canVisit(e)) //skip recursion
            return;
        p.addToUsage(e, usedBy);
        visitChildren(e);
    }
}
