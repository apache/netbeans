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

package org.netbeans.modules.php.atoum.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.atoum.commands.Atoum;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;

public final class CoverageProvider {

    private static final Logger LOGGER = Logger.getLogger(CoverageProvider.class.getName());

    private final PhpModule phpModule;


    public CoverageProvider(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @CheckForNull
    public Coverage getCoverage(TestRunInfo runInfo) {
        assert runInfo.isCoverageEnabled();
        Atoum atoum = Atoum.getForPhpModule(phpModule, false);
        if (atoum == null) {
            return null;
        }
        File coverageLog = atoum.getCoverageLog();
        if (coverageLog == null) {
            // likely some error
            return null;
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(coverageLog), StandardCharsets.UTF_8))) {
            return new CoverageImpl(CloverLogParser.parse(reader));
        } catch (IOException exc) {
            LOGGER.log(Level.WARNING, null, exc);
        }
        return null;
    }

}
