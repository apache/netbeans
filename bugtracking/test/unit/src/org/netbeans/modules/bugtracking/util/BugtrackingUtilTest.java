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

package org.netbeans.modules.bugtracking.util;

import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.bugtracking.TestIssue;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.TestQuery;
import org.netbeans.modules.bugtracking.TestRepository;
import org.netbeans.modules.bugtracking.spi.*;

/**
 *
 * @author Tomas Stupka
 */
public class BugtrackingUtilTest {

    private final static String[] VALID_HOSTS = new String [] {
        "netbeans.org/bugzilla",
        "123.netbeans.org/bugzilla",
        "netbeans123.org/bugzilla",
        "123netbeans123.org/bugzilla",
        "somenetbeans123.org/bugzilla",
        "some.netbeans123.org/bugzilla",
        "netbeans.org/123bugzilla",
        "netbeans.org/eventhis",
    };

    private final static String[] INVALID_HOSTS = new String [] {
        "n123etbeans.org/bugzilla",
        "netbeans.aorg/bugzilla",
        "netbeans.123org/bugzilla",
        "a",
        "a.b",
        "a.b.c",
    };

    public BugtrackingUtilTest() {
    }

    @Test
    public void testIsNbRepositoryStringUrl() {        
        assertFalse(NBBugzillaUtils.isNbRepository("enembenem"));
        urlTest("http://", VALID_HOSTS, false);
        urlTest("https://", VALID_HOSTS, false);
        urlTest("http://", INVALID_HOSTS, true);
        urlTest("https://", INVALID_HOSTS, true);
    }

    @Test
    public void testWrongProtocol() {
        urlTest("", VALID_HOSTS, true);
        urlTest("ht", VALID_HOSTS, true);
        urlTest("http:", VALID_HOSTS, true);
        urlTest("http:/", VALID_HOSTS, true);
        urlTest("htttp://", VALID_HOSTS, true);
    }
    
    @Test
    public void testWrongHost() {
        urlTest("http://", INVALID_HOSTS, true);
    }

    private void urlTest(String protocol, String[] hosts, boolean fail) {
        for (String url : hosts) {
            if(!fail && !NBBugzillaUtils.isNbRepository(protocol + url)) {
                fail(protocol + url + " is expected to be a netbeans bugzilla url");
            } else if(fail && NBBugzillaUtils.isNbRepository(protocol + url)) {
                fail(protocol + url + " isn't expected to be a netbeans bugzilla url");
            }
        }
    }

    @Test(expected=NullPointerException.class)
    public void testIsNbRepositoryNullString() {
        String url = null;
        NBBugzillaUtils.isNbRepository(url);
    }

    @Test
    public void testIsNbRepositoryRepo() {
        assertFalse(NBBugzillaUtils.isNbRepository(TestKit.getRepository(new BUTestRepository("enembenem")).getUrl()));
        assertTrue(NBBugzillaUtils.isNbRepository("https://netbeans.org/bugzilla"));        
    }

    @Test(expected=NullPointerException.class)
    public void testIsNbRepositoryNullRepo() {
        BUTestRepository repo = null;
        assertFalse(NBBugzillaUtils.isNbRepository(TestKit.getRepository(repo).getUrl()));
    }

    @Test(expected=NullPointerException.class)
    public void testIsNbRepositoryRepoNullUrl() {
        assertFalse(NBBugzillaUtils.isNbRepository(TestKit.getRepository(new BUTestRepository(null)).getUrl()));
    }

    private class BUTestRepository extends TestRepository {
        private final String url;
        private RepositoryInfo info;
        public BUTestRepository(String url) {
            this.url = url;
            this.info = new RepositoryInfo(null, null, url, null,null,null,null,null,null);
        }
        @Override
        public RepositoryInfo getInfo() {
            return info;
        }
    }

}
