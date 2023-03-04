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
package org.netbeans.modules.php.symfony2.annotations.extra.parser;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SymfonyExtraAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public SymfonyExtraAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = SymfonyExtraAnnotationLineParser.getDefault();
    }

    public void testMethodParser() {
        assertNotNull(parser.parse("Method"));
    }

    public void testRouteParser() {
        assertNotNull(parser.parse("Route"));
    }

    public void testParamConverterParser() {
        assertNotNull(parser.parse("ParamConverter"));
    }

    public void testTemplateParser() {
        assertNotNull(parser.parse("Template"));
    }

    public void testCacheParser() {
        assertNotNull(parser.parse("Cache"));
    }

}
