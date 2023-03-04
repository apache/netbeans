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

package org.netbeans.modules.java.source.save;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.save.CasualDiff.Diff;
import org.netbeans.modules.java.source.save.CasualDiff.DiffTypes;

/**
 *
 * @author lahvac
 */
public class DiffFacilityTest extends NbTestCase {

    public DiffFacilityTest(String name) {
        super(name);
    }

    public void testTokenListMatch() {
        Collection<Diff> diffs = new LinkedHashSet<Diff>();
        new DiffFacility(diffs).makeTokenListMatch("", "a", 0);

        assertEquals(1, diffs.size());

        Diff d = diffs.iterator().next();

        assertEquals(DiffTypes.INSERT, d.type);
        assertEquals(0, d.getPos());
        assertEquals("a", d.getText());
    }
    
    public void testMultilineWhitespace208270() {
        Collection<Diff> diffs = new LinkedHashSet<Diff>();
        new DiffFacility(diffs).makeListMatch("    public void method() {\n" +
                                              "        Runnable r = new Runnable() {\n" +
                                              "\n" +
                                              "            @Override\n" +
                                              "            public void run() {\n" +
                                              "                throw new UnsupportedOperationException();\n" +
                                              "            }\n" +
                                              "        };\n" +
                                              "    }",
                                              "    public void method() {\n" +
                                              "        Runnable r;\n" +
                                              "        r = new Runnable() {\n" +
                                              "            \n" +
                                              "            @Override\n" +
                                              "            public void run() {\n" +
                                              "                throw new UnsupportedOperationException();\n" +
                                              "            }\n" +
                                              "        };\n" +
                                              "    }",
                                              39);

        assertEquals(3, diffs.size());
        
        Iterator<Diff> diffIterator = diffs.iterator();
        Diff d1 = diffIterator.next();

        assertEquals(DiffTypes.INSERT, d1.type);
        assertEquals(84, d1.getPos());
        assertEquals(";\n        r", d1.getText());
        
        Diff d2 = diffIterator.next();

        assertEquals(DiffTypes.DELETE, d2.type);
        assertEquals(103, d2.getPos());
        assertEquals(104, d2.getEnd());
        
        Diff d3 = diffIterator.next();

        assertEquals(DiffTypes.INSERT, d3.type);
        assertEquals(105, d3.getPos());
        assertEquals("            \n", d3.getText());
    }
    
}