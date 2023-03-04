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
package org.netbeans.modules.hudson.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.RemoteFileSystem;

/**
 *
 * @author jhavlin
 */
public class HudsonInstanceImplTest {

    /**
     * Test fix for bug 224857.
     */
    @Test
    public void testHudsonInstanceImplPersistenceIsNotNull() {
        String url = "http://test224857/";
        HudsonInstanceImpl impl = HudsonInstanceImpl.createHudsonInstance(
                "Test224857", url, "1");
        if (impl == null) {
            // Instance wasn't created, should be already present.
            impl = HudsonManagerImpl.getDefault().getInstance(url);
        }
        try {
            if (impl != null) {
                assertNotNull(impl.getPersistence());
            } else {
                System.out.println("A problem occured when getting instance.");
            }
        } finally {
            HudsonManagerImpl.getDefault().removeInstance(impl);
        }
    }

    /**
     * Test for bug #230406 - NullPointerException: putProperty: job_color.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testJobWithoutSomePropertiesCanBeCreated()
            throws InterruptedException {

        BuilderConnector bc = new BuilderConnector() {

            @Override
            public BuilderConnector.InstanceData getInstanceData(
                    boolean authentication) {
                JobData jd = new JobData();
                jd.setJobName("test");
                jd.setJobUrl("http://x230406/job/test/");
                ViewData wd = new ViewData("Main", "http://x230406/main/",
                        true);
                return new InstanceData(Collections.singleton(jd),
                        Collections.singleton(wd),
                        Collections.<FolderData>emptySet());
            }

            @Override
            public Collection<BuilderConnector.BuildData> getJobBuildsData(
                    HudsonJob job) {
                return Collections.emptySet();
            }

            @Override
            public void getJobBuildResult(HudsonJobBuild build,
                    AtomicBoolean building,
                    AtomicReference<HudsonJobBuild.Result> result) {
                result.set(Result.NOT_BUILT);
            }

            @Override
            public RemoteFileSystem getArtifacts(HudsonJobBuild build) {
                return null;
            }

            @Override
            public RemoteFileSystem getArtifacts(HudsonMavenModuleBuild build) {
                return null;
            }

            @Override
            public RemoteFileSystem getWorkspace(HudsonJob job) {
                return null;
            }

            @Override
            public boolean isConnected() {
                return false;
            }

            @Override
            public boolean isForbidden() {
                return false;
            }

            @Override
            public HudsonVersion getHudsonVersion(boolean authentication) {
                return new HudsonVersion("9.9");
            }

            @Override
            public void startJob(HudsonJob job) {
            }

            @Override
            public BuilderConnector.ConsoleDataProvider getConsoleDataProvider() {
                return null;
            }

            @Override
            public BuilderConnector.FailureDataProvider getFailureDataProvider() {
                return null;
            }

            @Override
            public Collection<? extends HudsonJobChangeItem> getJobBuildChanges(
                    HudsonJobBuild build) {
                return Collections.emptySet();
            }
        };
        HudsonInstance hi = HudsonManager.addInstance("x230406",
                "http://x230406/", 1, bc);
        try {
            for (int i = 0; i < 600; i++) {
                if (hi.getJobs().isEmpty()) {
                    Thread.sleep(100);
                } else {
                    break;
                }
            }
            assertFalse(hi.getJobs().isEmpty());
            HudsonJob hj = hi.getJobs().iterator().next();
            assertEquals(HudsonJob.Color.grey, hj.getColor());
            assertEquals("test", hj.getName());
            assertEquals("test", hj.getDisplayName());
        } finally {
            HudsonManager.removeInstance(hi);
        }
    }

    @Test
    public void testIgnoreNullListener() {
        String url = "http://testIgnoreNullListener/";
        HudsonInstanceImpl i = HudsonInstanceImpl.createHudsonInstance(
                "testIgnoreNullListener", url, "1");
        if (i == null) {
            i = HudsonManagerImpl.getDefault().getInstance(url);
        }
        if (i == null) {
            System.out.println("Cannot get instance, skipping this test.");
            return;
        }
        try {
            i.addHudsonChangeListener(null);
            i.addHudsonChangeListener(null);
            i.terminate(); // shouldn't throw any exception
        } finally {
            HudsonManager.removeInstance(i);
        }
    }
}
