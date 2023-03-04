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

package org.netbeans.modules.hudson.ui.spi;

import org.junit.Assert;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider.Association;

public class ProjectHudsonProviderTest extends NbTestCase {

    public ProjectHudsonProviderTest(String n) {
        super(n);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testAssociation() throws Exception {
        assertEquals("http://nowhere.net/", new Association("http://nowhere.net/", null).toString());
        assertEquals("http://nowhere.net/job/foo%20bar/", new Association("http://nowhere.net/", "foo bar").toString());
        try {
            new Association("http://nowhere.net", null);
            fail();
        } catch (IllegalArgumentException x) {}
        try {
            new Association("http://nowhere.net/", "");
            fail();
        } catch (IllegalArgumentException x) {}
        try {
            new Association("http://nowhere.net/", " foo ");
            fail();
        } catch (IllegalArgumentException x) {}
        assertEquals("http://nowhere.net/", Association.fromString("http://nowhere.net/").getServerUrl());
        assertNull("http://nowhere.net/", Association.fromString("http://nowhere.net/").getJobName());
        assertEquals("http://nowhere.net/", Association.fromString("http://nowhere.net/job/foo%20bar/").getServerUrl());
        assertEquals("foo bar", Association.fromString("http://nowhere.net/job/foo%20bar/").getJobName());
        assertNull(Association.fromString("http://nowhere.net/hudson/view/someview/job/Some Job/site/")); // #189254
        assertNull(Association.fromString("http://nowhere.net/hudson/view/someview/job/Some Job/"));
        assertEquals("Some Job", Association.fromString("http://nowhere.net/hudson/view/someview/job/Some%20Job/").getJobName());
    }

    /**
     * Test for bug 200446.
     */
    public void testAssociationsWithViewName() {
        String urlViewJob = "http://nowhere.net/hudson/view/someview/job/Some%20Job/";
        Assert.assertArrayEquals(
                new String[]{"http://nowhere.net/hudson/", "Some Job"},
                Association.fromString(urlViewJob).getJobPath());
        Assert.assertEquals(
                "someview",
                Association.fromString(urlViewJob).getViewName());

        String urlViewOnly = "http://nowhere.net/hudson/view/Some%20View/";
        Assert.assertArrayEquals(
                new String[]{"http://nowhere.net/hudson/"},
                Association.fromString(urlViewOnly).getJobPath());
        Assert.assertEquals(
                "Some View",
                Association.fromString(urlViewOnly).getViewName());
    }

    public void testAssociationOfHudsonJobsInFolderHierarchy() {
        Association a = Association.fromString(
                "http://localhost:8080/app/hudson/job/folder%201/job/folder%201a/job/TestJob/");
        assertEquals("http://localhost:8080/app/hudson/", a.getServerUrl());
        assertEquals("folder 1/folder 1a/TestJob", a.getJobName());
        String[] expectedPath = {"http://localhost:8080/app/hudson/",
            "folder 1", "folder 1a", "TestJob"};
        assertEquals("Job path length is wrong",
                expectedPath.length, a.getJobPath().length);
        for (int i = 0; i < expectedPath.length; i++) {
            assertEquals(expectedPath[i], a.getJobPath()[i]);
        }
    }
}
