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
package org.netbeans.modules.php.atoum.run;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.atoum.commands.Atoum;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;

public final class TestRunner {

    private final PhpModule phpModule;


    public TestRunner(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    public void runTests(TestRunInfo runInfo, TestSession testSession) throws TestRunException {
        Atoum atoum = Atoum.getForPhpModule(phpModule, true);
        if (atoum == null) {
            return;
        }
        Integer result = atoum.runTests(phpModule, runInfo, testSession);
        // 255 - some error
        // 1 - some test failed
        if (result != null
                && result == 255) {
            throw new TestRunException();
        }
    }

}
