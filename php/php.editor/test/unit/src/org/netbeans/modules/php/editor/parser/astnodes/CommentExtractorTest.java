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
package org.netbeans.modules.php.editor.parser.astnodes;

import org.netbeans.modules.php.editor.PHPTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class CommentExtractorTest extends PHPTestBase {

    public CommentExtractorTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 5000;
    }

    public void testNoDescription() throws Exception {
        PHPDocNode methodName = new PHPDocNode(1, 2, "where");
        PHPDocMethodTag methodTag = new PHPDocMethodTag(1, 2, PHPDocTag.Type.METHOD, null, methodName, null, "@method DibiConnection where($cond)");
        String documentation = methodTag.getDocumentation();
        assertEquals("", documentation);
    }

    public void testExistingDescription() throws Exception {
        PHPDocNode methodName = new PHPDocNode(1, 2, "where");
        final String description = "My description.";
        PHPDocMethodTag methodTag = new PHPDocMethodTag(1, 2, PHPDocTag.Type.METHOD, null, methodName, null, "@method DibiConnection where($cond) " + description);
        String documentation = methodTag.getDocumentation();
        assertEquals(description, documentation);
    }

    public void testExistingDescriptionWithMoreDeclarations() throws Exception {
        PHPDocNode methodName = new PHPDocNode(1, 2, "where");
        final String description = "My description.";
        PHPDocMethodTag methodTag = new PHPDocMethodTag(1, 2, PHPDocTag.Type.METHOD, null, methodName, null, "@method DibiConnection where() where($cond) " + description);
        String documentation = methodTag.getDocumentation();
        assertEquals(description, documentation);
    }

    // NETBEANS-1861
    public void testStaticNoDescription() throws Exception {
        PHPDocNode methodName = new PHPDocNode(1, 2, "where");
        PHPDocMethodTag methodTag = new PHPDocMethodTag(1, 2, PHPDocTag.Type.METHOD, null, methodName, null, "@method static DibiConnection where($cond)");
        String documentation = methodTag.getDocumentation();
        assertEquals("", documentation);
    }

    public void testStaticExistingDescription() throws Exception {
        PHPDocNode methodName = new PHPDocNode(1, 2, "where");
        final String description = "My description.";
        PHPDocMethodTag methodTag = new PHPDocMethodTag(1, 2, PHPDocTag.Type.METHOD, null, methodName, null, "@method static DibiConnection where($cond) " + description);
        String documentation = methodTag.getDocumentation();
        assertEquals(description, documentation);
    }

    public void testStaticExistingDescriptionWithMoreDeclarations() throws Exception {
        PHPDocNode methodName = new PHPDocNode(1, 2, "where");
        final String description = "My description.";
        PHPDocMethodTag methodTag = new PHPDocMethodTag(1, 2, PHPDocTag.Type.METHOD, null, methodName, null, "@method static DibiConnection where() where($cond) " + description);
        String documentation = methodTag.getDocumentation();
        assertEquals(description, documentation);
    }

}
