/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.registry;
import junit.framework.TestCase;

/**
 *
 * @author Danila_Dugurov
 */

public class TestCircles extends TestCase {
    /*
    private List<Product> list;
    
    public void testThingInSelf() {
        try {
            list = new LinkedList<Product>();
            Product component = new Product();
            component.addRequirement(component);
            list.add(component);
            checkCircles();
            fail();
        } catch (UnresolvedDependencyException ex) {
        }
    }
    
    public void testMoreSofisticatedRequirements() {
        try {
            list = new LinkedList<Product>();
            Product component = new Product();
            Product depp = new Product();
            Product jony = new Product();
            list.add(component);
            list.add(depp);
            list.add(jony);
            component.addRequirement(depp);
            depp.addRequirement(jony);
            jony.addRequirement(component);
            checkCircles();
            fail();
        } catch (UnresolvedDependencyException ex) {
        }
    }
    
    public void testRequirementsAndConflicts() {
        try {
            list = new LinkedList<Product>();
            Product component = new Product();
            Product depp = new Product();
            Product jonny = new Product();
            list.add(component);
            list.add(depp);
            list.add(jonny);
            component.addRequirement(depp);
            depp.addRequirement(jonny);
            jonny.addConflict(component);
            checkCircles();
            fail();
        } catch (UnresolvedDependencyException ex) {
        }
    }
    
    public void testSofisticatedRequirementsAndConflicts() {
        try {
            list = new LinkedList<Product>();
            Product root = new Product();
            Product depp = new Product();
            Product jonny = new Product();
            Product independant = new Product();
            list.add(root);
            list.add(depp);
            list.add(jonny);
            list.add(independant);
            root.addRequirement(depp);
            root.addRequirement(jonny);
            jonny.addConflict(depp);
            jonny.addRequirement(independant);
            depp.addRequirement(independant);
            checkCircles();
            fail();
        } catch (UnresolvedDependencyException ex) {
        }
    }
    
    public void testOkConflicts() {
        try {
            list = new LinkedList<Product>();
            Product root = new Product();
            Product depp = new Product();
            Product jonny = new Product();          
            list.add(depp);
            list.add(jonny);
            list.add(root);
            root.addConflict(depp);
            root.addConflict(jonny);
            jonny.addRequirement(depp);
            checkCircles();
        } catch (UnresolvedDependencyException ex) {
            fail();
        }
    }
    
    private void checkCircles() throws UnresolvedDependencyException {
        for (Product component : list) {
            final Stack<Product> visited = new Stack<Product>();
            final Set<Product> conflictSet = new HashSet<Product>();
            final Set<Product> requirementSet = new HashSet<Product>();
            checkCircles(component, visited, conflictSet, requirementSet);
        }
    }
    
    private void checkCircles(Product component, Stack<Product> visited,
            Set<Product> conflictSet, Set<Product> requirementSet)
            throws UnresolvedDependencyException {
        if (visited.contains(component) || conflictSet.contains(component))
            throw new UnresolvedDependencyException("circles found");
        visited.push(component);
        requirementSet.add(component);
        if (!Collections.disjoint(requirementSet, component.getConflicts()))
            throw new UnresolvedDependencyException("circles found");
        conflictSet.addAll(component.getConflicts());
        for (Product comp : component.getRequirements())
            checkCircles(comp, visited, conflictSet, requirementSet);
        visited.pop();
    }*/
    public void testNone() {        
    }
}
