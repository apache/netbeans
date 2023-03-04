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
package org.netbeans.core.startup;

import java.awt.GraphicsEnvironment;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.netbeans.junit.NbTestCase;

public class SplashTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(SplashTest.class);
    }

    public SplashTest(String name) {
        super(name);
    }

    public void testIncrementStepsInSplash() {
        Splash splash = Splash.getInstance();
        splash.addToMaxSteps(10);
        Assert.assertEquals("Ten steps now", 10, splash.getMaxSteps());
        splash.increment(6);
        Assert.assertEquals("Progress is 6", 6, splash.getProgress());
        splash.addToMaxSteps(10);
        Assert.assertEquals("Twenty steps now", 20, splash.getMaxSteps());
        Assert.assertEquals("Progress adjusted to twelve", 12, splash.getProgress());
    }
}
