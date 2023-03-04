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

/*
 * DefaultVisitor.java
 *
 * Created on August 18, 2005, 6:04 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.visitor;


/**
 *
 * @author ChrisWebster
 */
public class DefaultVisitor implements XMLNodeVisitor {
    public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute attr) {
        visitNode(attr);
    }
    
    public void visit(org.netbeans.modules.xml.xdm.nodes.Document doc) {
        visitNode(doc);
    }
    
    public void visit(org.netbeans.modules.xml.xdm.nodes.Element e) {
        visitNode(e);
    }
    
    public void visit(org.netbeans.modules.xml.xdm.nodes.Text txt) {
        visitNode(txt);
    }
    
    protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node node) {
    }
    
}
