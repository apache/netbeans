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

package org.netbeans.modules.web.jspparser;

import java.io.IOException;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;

/**
 *
 * @author pj97932
 */
public class ParserPresentTest extends NbTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ParserPresentTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        TestUtil.setup(this);
    }
    
    public void testParserPresent() throws IOException {
        JspParserAPI api = JspParserFactory.getJspParser();
        assertNotNull(api);
    }
    
    public void testSameInstance() throws IOException {
        JspParserAPI api1 = JspParserFactory.getJspParser();
        JspParserAPI api2 = JspParserFactory.getJspParser();
        log(api1.toString());
        log(api2.toString());
        assertSame("JSP parser instance should be the same all the time", api1, api2);
    }
    
}
