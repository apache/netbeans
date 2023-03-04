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
package org.netbeans.modules.php.codeception.coverage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;

public class CoverageProvider {

    private static final Logger LOGGER = Logger.getLogger(CoverageProvider.class.getName());


    @CheckForNull
    public Coverage getCoverage() {
        CoverageImpl coverage = new CoverageImpl();
        try {
            CodeceptionCoverageLogParser.parse(
                    new BufferedReader(new InputStreamReader(new FileInputStream(Codecept.COVERAGE_LOG), StandardCharsets.UTF_8)), coverage);
        } catch (FileNotFoundException ex) {
            LOGGER.info(String.format("File %s not found. If there are no errors in Codeception output (verify in Output window), "
                    + "please report an issue (http://www.netbeans.org/issues/).", Codecept.COVERAGE_LOG));
            return null;
        }
        return coverage;
    }

}
