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
package org.netbeans.modules.java.module.graph;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class DependencyCalculatorTest extends NbTestCase {

    public DependencyCalculatorTest(String name) {
        super(name);
    }
    
    public void testGetTransitives_A2B2C_A2C_AndSoOn() throws IOException {
        // A -> B -> C -> ... -> N        
        // => 
        // A -> C
        // ...        
        getTransitivesTest(0);
    }

    public void testGetTransitives_NotPublicDependingOnPublic() throws IOException {
        // A -> B -> C -> ... 
        // where: 
        // A -> B not public
        // B -> C public        
        // => 
        // A -> C
        // ...
        getTransitivesTest(1);
        // A -> B -> C -> D -> ... 
        // where: 
        // A -> B not public
        // B -> C not public
        // C -> D public
        // D -> E public 
        // ...
        // => 
        // B -> D 
        // ...
        getTransitivesTest(2);
    }
    
    public void testGetTransitives_Tree() throws IOException {
        DependencyCalculator dc = new DependencyCalculator(FileUtil.toFileObject(getWorkDir())); // just some FO so that we can create an DepCalculator instance
        
        // A -> B         public
        // B -> {C, D, E} public
        // => 
        // A -> {C, D, E}
        // ...        
        List<DependencyEdge> deps = new LinkedList<>();
        deps.add(createDep("A", "B", true));
        deps.add(createDep("B", "C", true));
        deps.add(createDep("B", "D", true));        
        deps.add(createDep("B", "E", true));        
        
        Collection<DependencyEdge> trans = dc.collectTransitiveDependencies(deps);
        assertEquals(3, trans.size());
        
        // A -> B         not public
        // B -> {C, D, E} public
        // => 
        // A -> {C, D, E}
        // ...        
        deps = new LinkedList<>();
        deps.add(createDep("A", "B", false));
        deps.add(createDep("B", "C", true));
        deps.add(createDep("B", "D", true));        
        deps.add(createDep("B", "E", true));        
        
        trans = dc.collectTransitiveDependencies(deps);
        assertEquals(3, trans.size());
    }
    
    public void testGetTransitives_TreeWithDupes() throws IOException {
        DependencyCalculator dc = new DependencyCalculator(FileUtil.toFileObject(getWorkDir())); // just some FO so that we can create an DepCalculator instance
        
        // A -> {B, C}
        // B -> {D} 
        // C -> {D} 
        // => 
        // A -> {D}
        // ...        
        List<DependencyEdge> deps = new LinkedList<>();
        deps.add(createDep("A", "B", true));
        deps.add(createDep("A", "C", true));
        deps.add(createDep("B", "D", true));
        deps.add(createDep("C", "D", true));        
        
        Collection<DependencyEdge>  trans = dc.collectTransitiveDependencies(deps);
        assertEquals(1, trans.size());
        
        // A -> {B, C} 
        // B -> {D} 
        // C -> {D} 
        // D -> {E} 
        // => 
        // A -> {D, E}
        // ...        
        deps = new LinkedList<>();
        deps.add(createDep("A", "B", true));
        deps.add(createDep("A", "C", true));
        deps.add(createDep("B", "D", true));
        deps.add(createDep("C", "D", true));        
        deps.add(createDep("D", "E", true));        
        
        trans = dc.collectTransitiveDependencies(deps);
        assertEquals(4, trans.size());
    }
    
    /**
     * check transitive deps for a chain of public deps
     * A -> B -> ... -> N
     * N = 3 ... 10
     */
    private void getTransitivesTest(int notPublicAtStart) throws IOException {
        DependencyCalculator dc = new DependencyCalculator(FileUtil.toFileObject(getWorkDir())); // just some FO so that we can create an DepCalculator instance
        
        for (int tuples = 2; tuples < 10; tuples++) {
            List<DependencyEdge> deps = new LinkedList<>();
            char c = 'a';
            print("");
            print(" ==== creating:");
            for (int pair = 1; pair <= tuples; pair++) {
                String source = Character.toString(c);
                String target = Character.toString(++c);
                print(" " + source + " -> " + target + ", public " + (pair > notPublicAtStart));
                deps.add(createDep(source, target, pair > notPublicAtStart));
            }
            
            Collection<DependencyEdge> trans = dc.collectTransitiveDependencies(deps);
            print(" ==== transitive are:");
            for (DependencyEdge td : trans) {
                print(" " + td.getSource().getName() + " -> " + td.getTarget().getName()) ;
                assertTrue(td.isTrasitive());
                assertFalse(td.isPublic());            
            }
            assertEquals(" tuples: " + tuples, getTransitiveCount(tuples + 1 - (notPublicAtStart > 0 ? notPublicAtStart - 1 : 0)), trans.size());
            
            print(" ==== testing existence: ");
            char s = 'a';
            s += (notPublicAtStart > 0 ? notPublicAtStart - 1 : 0);
            while(s <= c) {
                char t = s;
                t += 2;
                while(t <= c) {                
                    String source = Character.toString(s);
                    String target = Character.toString(t);
                    print(" " + source + " -> " + target);
                    boolean found = false;
                    for (DependencyEdge tran : trans) {
                        if(tran.getSource().getName().equals(source) && tran.getTarget().getName().equals(target)) {
                            found = true;
                            trans.remove(tran);
                            break;
                        }
                    }
                    assertTrue(" missing " + source + " -> " + target + " !!!", found);                
                    t++;
                }
                s++;
            }
            
            if(!trans.isEmpty()) {
                print(" not expected leftovers: "); // could this even happen?
                for (DependencyEdge tran : trans) {
                    print(" " + tran.getSource().getName() + " -> " + tran.getTarget().getName());
                }
            }
            assertTrue(trans.isEmpty());
        }
    }

    private void print(String msg) {
        System.out.println(msg);
    }
    
    private DependencyEdge createDep(String source, String target, boolean isPub) {
        return new DependencyEdge(new ModuleNode(source, false, false, null), new ModuleNode(target, false, false, null), isPub, false);
    }

    /**
     * 
     * @param n amount of nodes
     * @return 
     */
    private Object getTransitiveCount(int n) {
        // combinations of node pairs
        // n! / ( 2! * ( n - 2 )! ) => ( (n - 1) * n ) / 2
        int ret = (((n - 1) * n) / 2);
        // minus the amount of direct deps
        ret -= n - 1; 
        return ret;
    }
    
}
