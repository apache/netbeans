/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
