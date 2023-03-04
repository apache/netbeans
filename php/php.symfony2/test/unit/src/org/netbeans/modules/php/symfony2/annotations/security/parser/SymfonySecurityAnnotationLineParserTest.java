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
package org.netbeans.modules.php.symfony2.annotations.security.parser;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;


/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SymfonySecurityAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public SymfonySecurityAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = SymfonySecurityAnnotationLineParser.getDefault();
    }

    public void testSecureParser() {
        assertNotNull(parser.parse("Secure"));
    }

    public void testSecureParamParser() {
        assertNotNull(parser.parse("SecureParam"));
    }

    public void testSecureReturnParser() {
        assertNotNull(parser.parse("SecureReturn"));
    }

    public void testRunAsParser() {
        assertNotNull(parser.parse("RunAs"));
    }

    public void testSatisfiesParentSecurityPolicyParser() {
        assertNotNull(parser.parse("SatisfiesParentSecurityPolicy"));
    }

    public void testPreAuthorizeParser() {
        assertNotNull(parser.parse("PreAuthorize"));
    }

}
