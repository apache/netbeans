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

import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;

/**
 *
 * @author lkishalmi
 */
public class HCLTestBase extends CslTestBase {

    public HCLTestBase(String testName) {
        super(testName);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new HCLLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return HCLLanguage.MIME_TYPE;
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    @Override
    protected void checkStructure(String relFilePath) throws Exception {
        super.checkStructure(relFilePath, false, false, true);
    }

    
}
