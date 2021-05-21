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

package org.netbeans.modules.php.editor.parser.api;

import java.io.StringReader;
import java_cup.runtime.Symbol;
import junit.framework.TestCase;
import org.netbeans.modules.php.editor.parser.*;
import org.netbeans.modules.php.editor.parser.astnodes.*;

/**
 *
 * @author Petr Pisl
 */
public class UtilsTest extends TestCase {
    
    public UtilsTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetCommentForNode() throws Exception {
        String text = "<?php\n    /**\n    * @link http://ahoj\n    */    class Test  {}\n   $name=\"Hello\"?>";
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(text));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Symbol root = parser.parse();
        assertNotNull(root);
        Program program = (Program)root.value;
        assertEquals(2, program.getStatements().size());
        
        ASTNode astNode = program.getStatements().get(0);
        Comment comment = Utils.getCommentForNode(program, astNode);
        assertNotNull(comment);
        
        astNode = program.getStatements().get(1);
        comment = Utils.getCommentForNode(program, astNode);
        assertNull(comment);
        
    }
    
    public void testGetNodeAtOffset() throws Exception {
        String text = "<?php\n    /**\n    * @link http://ahoj\n    */    class Test  {} \n    $name = \"hello\"\n?>";
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(text));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Symbol root = parser.parse();
        assertNotNull(root);
        Program program = (Program)root.value;
        ASTNode node = Utils.getNodeAtOffset(program, 79);
        assertEquals(Scalar.class, node.getClass());
        assertEquals(76, node.getStartOffset());
        assertEquals(83, node.getEndOffset());
    }

}
