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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 *
 * @author rico
 * Visitor that searches schema elements for a key or unique.
 * This is used by KeyRef to look for keys or unique that
 * it refers to in its "refer" attribute.
 */
public class FindReferredConstraintVisitor extends DeepSchemaVisitor{
    
    private Constraint constraint;
    private String name;
    private boolean found;
    
    /**
     * Creates a new instance of FindReferredConstraintVisitor
     */
    public FindReferredConstraintVisitor() {
    }
    
    /**
     * Recursively searches from parent for the Constraint (unique or key) that
     * has the same name as the name parameter.
     * @param parent Node where searching will start. 
     * @param name name of Constraint to look for
     */
    public Constraint findReferredConstraint(SchemaComponent parent, String name){
        this.name = name;
        found = false;
        parent.accept(this);
        return constraint;
    }
    
    public void visit(LocalElement le) {
        if(findConstraint(le.getConstraints())){
            return;
        }
        super.visit(le);
    }
    
    public void visit(GlobalElement ge) {
        if(findConstraint(ge.getConstraints())){
            return;
        }
        super.visit(ge);
    }
    
    protected void visitChildren(SchemaComponent sc) {
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
            if(found) return;
        }
    }
    
    private boolean findConstraint(Collection<Constraint> constraints){
        for(Constraint c : constraints){
            if(c.getName().equals(name)){
                constraint = c;
                found = true;
                return true;
            }
        }
        return false;
    }
}
