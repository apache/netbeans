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

package org.netbeans.modules.php.project.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectType;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.common.api.UsageLogger;

public final class UsageLogging {

    private static final Logger LOGGER = Logger.getLogger(UsageLogging.class.getName());

    private final UsageLogger testConfigUsageLogger = new UsageLogger.Builder(PhpProjectUtils.USAGE_LOGGER_NAME)
            .message(PhpProjectUtils.class, "USG_TEST_CONFIG_PHP") // NOI18N
            .firstMessageOnly(false)
            .create();
    private final UsageLogger phpTestRunUsageLogger = new UsageLogger.Builder(PhpProjectUtils.USAGE_LOGGER_NAME)
            .message(PhpProjectUtils.class, "USG_TEST_RUN_PHP") // NOI18N
            .create();
    private final UsageLogger jsTestRunUsageLogger = UsageLogger.jsTestRunUsageLogger(PhpProjectUtils.USAGE_LOGGER_NAME);


    //~ Helper methods

    public static void logTestConfig(PhpProject project, List<String> testingProviders) {
        assert project != null;
        project.getLookup().lookup(UsageLogging.class).logPhpTestConfig(testingProviders);
    }

    public static void logPhpTestRun(PhpProject project, List<PhpTestingProvider> testingProviders) {
        assert project != null;
        project.getLookup().lookup(UsageLogging.class).logPhpTestRun(testingProviders);
    }

    public static void logJsTestRun(PhpProject project, JsTestingProvider jsTestingProvider) {
        assert project != null;
        project.getLookup().lookup(UsageLogging.class).logJsTestRun(jsTestingProvider);
    }

    //~ Logging methods

    private void logPhpTestConfig(List<String> testingProviders) {
        assert testingProviders != null;
        LOGGER.finest("Usage logging for PHP test config");
        testConfigUsageLogger.log(StringUtils.implode(testingProviders, "|")); // NOI18N
    }

    private void logPhpTestRun(List<PhpTestingProvider> testingProviders) {
        assert testingProviders != null;
        LOGGER.finest("Usage logging for PHP test run");
        phpTestRunUsageLogger.log(getTestingProvidersForUsage(testingProviders));
    }

    private void logJsTestRun(JsTestingProvider jsTestingProvider) {
        assert jsTestingProvider != null;
        jsTestRunUsageLogger.log(PhpProjectType.TYPE, jsTestingProvider.getIdentifier());
    }

    private static String getTestingProvidersForUsage(Collection<PhpTestingProvider> testingProviders) {
        assert testingProviders != null;
        List<String> identifiers = new ArrayList<>(testingProviders.size());
        for (PhpTestingProvider provider : testingProviders) {
            identifiers.add(provider.getIdentifier());
        }
        return StringUtils.implode(identifiers, "|"); // NOI18N
    }

}
