/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
