/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser.api;

import java.io.FileReader;
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
