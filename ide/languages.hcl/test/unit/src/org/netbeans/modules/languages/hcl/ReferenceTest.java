/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.languages.hcl;

import java.util.List;
import java.util.regex.Pattern;
import static junit.framework.TestCase.assertNotSame;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import org.netbeans.modules.languages.hcl.ast.HCLAttribute;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLElement;
import org.netbeans.modules.languages.hcl.ast.HCLExpression;
import org.netbeans.modules.languages.hcl.ast.HCLIdentifier;
import org.netbeans.modules.languages.hcl.ast.HCLResolveOperation;
import static org.junit.Assert.*;
import org.netbeans.modules.languages.hcl.ast.HCLBlockFactory;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;
import org.netbeans.modules.languages.hcl.grammar.HCLParser;

/**
 *
 * @author lkishalmi
 */
public class ReferenceTest {

    @Test
    public void testReferences1() throws Exception {
        assertTrue(elementAt("^\na{\nb=true\n}").isEmpty());
    }
    
    @Test
    public void testReferences2() throws Exception {
        var at = elementAt("^a{\nb=true\n}");
        assertTrue(at.isEmpty());
    }
    
    @Test
    public void testReferences3() throws Exception {
        var at = elementAt( """
                            a^{
                                b = true
                            }
                            """
                     );
        assertEquals(2, at.size());
        var it = at.iterator();
        assertTrue(it.next() instanceof HCLBlock);
        assertTrue(it.next() instanceof HCLIdentifier);
    }

    @Test
    public void testReferences4() throws Exception {
        var at = elementAt("a{^\nb=true\n}");
        assertEquals(1, at.size());
        var it = at.iterator();
        assertTrue(it.next() instanceof HCLBlock);
    }

    @Test
    public void testReferences5() throws Exception {
        var at = elementAt("a{\nb=^true\n}");
        assertEquals(2, at.size());
        var it = at.iterator();
        assertTrue(it.next() instanceof HCLBlock);
        assertTrue(it.next() instanceof HCLAttribute);
    }

    @Test
    public void testReferences6() throws Exception {
        var at = elementAt("a{\nb=t^rue\n}");
        assertEquals(3, at.size());
        var it = at.iterator();
        assertTrue(it.next() instanceof HCLBlock);
        assertTrue(it.next() instanceof HCLAttribute);
        assertTrue(it.next() instanceof HCLExpression);
    }

    @Test
    public void testReferences7() throws Exception {
        var at = elementAt("a{\nb = local.b.*^.c\n}");
        assertEquals(3, at.size());
        var it = at.iterator();
        assertTrue(it.next() instanceof HCLBlock);
        assertTrue(it.next() instanceof HCLAttribute);
        assertTrue(it.next() instanceof HCLResolveOperation.AttrSplat);
    }

    @Test
    public void testReferences8() throws Exception {
        var at = elementAt("a{\nb = local.b.*.^c\n}");
        assertEquals(3, at.size());
        var it = at.iterator();
        assertTrue(it.next() instanceof HCLBlock);
        assertTrue(it.next() instanceof HCLAttribute);
        assertTrue(it.next() instanceof HCLResolveOperation.Attribute);
    }

    @Test
    public void testReferences9() throws Exception {
        var at = elementAt("a{\nb = local.b.*.^\n}");
        assertEquals(3, at.size());
        var it = at.iterator();
        assertTrue(it.next() instanceof HCLBlock);
        assertTrue(it.next() instanceof HCLAttribute);
        assertTrue(it.next() instanceof HCLResolveOperation.Attribute);
    }

    private List<? extends HCLElement> elementAt(String code) {
        int pos = code.indexOf('^');

        assertNotSame(-1, pos);

        code = code.replaceAll(Pattern.quote("^"), "");
        
        return parse(code).elementsAt(pos - 1);
    }
    
    private SourceRef parse(String expr) {
        var lexer = new HCLLexer(CharStreams.fromString(expr));
        var parser = new HCLParser(new CommonTokenStream(lexer));
        var ret = new SourceRef(null);
        var bf = new HCLBlockFactory(ret::elementCreated);
        bf.process(parser.configFile());
        return ret;
   }    
}
