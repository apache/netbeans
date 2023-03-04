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
package org.netbeans.modules.php.editor.parser;

import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPVarCommentParserTest extends PHPTestBase {

    public PHPVarCommentParserTest(String testName) {
        super(testName);
    }

    public void testArray_01() throws Exception {
        String comment = " @var $b['y'] TestClass ";
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testArray_02() throws Exception {
        String comment = " @var $b[\"y\"] TestClass ";
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testIssue257709_01() throws Exception {
        String comment = " @var $b	Type "; // TAB
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testIssue257709_02() throws Exception {
        String comment = " @var 	$b	  Type "; // TAB + space
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testArrayForPHPDocPattern_01() throws Exception {
        String comment = "/** @var TestClass $b['y'] */";
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testArrayForPHPDocPattern_02() throws Exception {
        String comment = "/** @var TestClass $b[\"y\"] */";
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testPHPDocPattern_01() throws Exception {
        String comment = "/** @var Type	$b */"; // TAB
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testForPHPDocPattern_02() throws Exception {
        String comment = "/** @var 	Type	  $b */"; // TAB + space
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testForPHPDocPattern_03() throws Exception {
        String comment = "/** @var Type $b Description */";
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

    public void testForPHPDocPattern_04() throws Exception {
        String comment = "/** @var Type $b Long Description Something.*/";
        PHPVarCommentParser parser = new PHPVarCommentParser();
        PHPVarComment varComment = parser.parse(0, comment.length(), comment);
        assertEquals(Comment.Type.TYPE_VARTYPE, varComment.getCommentType());
        assertEquals("$b", varComment.getVariable().getVariable().getValue());
        assertEquals(1, varComment.getVariable().getTypes().size());
    }

}
