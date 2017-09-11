/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
