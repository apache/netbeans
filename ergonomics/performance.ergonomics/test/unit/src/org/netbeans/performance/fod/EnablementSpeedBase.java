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
package org.netbeans.performance.fod;

import org.netbeans.modules.ide.ergonomics.fod.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.Utilities;
import org.netbeans.modules.performance.utilities.CommonUtilities;

/**
 * Checks how quick it is to enable a feature.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class EnablementSpeedBase extends NbTestCase {

    public EnablementSpeedBase(String name) {
        super(name);
    }

    protected abstract String featureName();
    protected abstract long threshold();

    static Test suite(Class<? extends EnablementSpeedBase> clazz) {
        return NbModuleSuite.create(
                NbModuleSuite.emptyConfiguration().addTest(clazz).
                gui(false).
                clusters("ergonomics.*").
                clusters(".*").
                enableModules("ide[0-9]*", ".*").
                honorAutoloadEager(true)
        );
    }

    public void testEnableGivenCluster() {
        FeatureInfo enable = null;
        Pattern p = Pattern.compile(featureName());
        for (FeatureInfo fi : FeatureManager.features()) {
            Matcher m = p.matcher(fi.toString().split("\\[")[1].split("\\]")[0]);
            if (m.matches()) {
                enable = fi;
                break;
            }
        }
        assertNotNull("Can we find feature " + featureName() + "?", enable);

        long time = System.currentTimeMillis();
        assertTrue("Enabled", Utilities.featureDialog(enable, "notFoound", "featureName"));
        long now = System.currentTimeMillis();
        PerformanceData data = new PerformanceData();
        data.name = "enable" + featureName().toUpperCase();
        data.threshold = threshold();
        data.runOrder = PerformanceData.NO_ORDER;
        data.unit = "ms";
        data.value = (now - time);
        CommonUtilities.processUnitTestsResults(this.getClass().getCanonicalName(), data);

        System.err.println("enabled in " + ((now - time) / 1000) + "s");
    }
}
