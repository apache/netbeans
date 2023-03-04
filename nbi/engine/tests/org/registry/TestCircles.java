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
