/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.bugzilla.query;

import java.io.UnsupportedEncodingException;
import java.util.logging.LogRecord;
import org.netbeans.modules.bugzilla.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class ControllerTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME = "Beautiful";
    private static String QUERY_NAME = "Hilarious";

    public ControllerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure conector        
        
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
    }

    public void testParameters() throws MalformedURLException, CoreException, InterruptedException, UnsupportedEncodingException {
        LogHandler h = new LogHandler("Finnished populate query controller");
        Bugzilla.LOG.addHandler(h);
        String parametersUrl = getParametersUrl();
        BugzillaQuery q = new BugzillaQuery(QUERY_NAME, QueryTestUtil.getRepository(), parametersUrl, true, false, true);
        QueryController c = q.getController();
        assertParameters(h, parametersUrl, c);
        
        // lets make sure that what was returned (not encoded) will work next time as well
        h.done = false;
        parametersUrl = c.getUrlParameters(false);
        q = new BugzillaQuery(QUERY_NAME, QueryTestUtil.getRepository(), parametersUrl, true, false, true);
        c = q.getController();
        assertParameters(h, parametersUrl, c);
    }

    private void assertParameters(LogHandler h, String parametersUrl, QueryController c) throws UnsupportedEncodingException, InterruptedException, IllegalStateException {
        // wait while populate
        int timeout = 60000;
        long ts = System.currentTimeMillis();
        while(!h.done) {
            Thread.sleep(500);
            if(ts + timeout < System.currentTimeMillis()) throw new IllegalStateException("timeout");
        }

        // get the paramters in an encoded form
        String[] parametersGiven = parametersUrl.split("&");
        String params = c.getUrlParameters(true);
        assertTrue(params.startsWith("&"));
        params = params.substring(1);
        String[] parametersReturned = params.split("&");
//        assertEquals(parametersGiven.length, parametersReturned.length);

        Set<String> returnedSet = new HashSet<String>(parametersReturned.length);
        for (String string : parametersReturned) {
            returnedSet.add(string);
        }
        for (int i = 1; i < parametersGiven.length; i++) { // skip the first elemenent - its = ""
            String p = parametersGiven[i];
            p = handleEncoding(p); // encode provided parameter
            if(!returnedSet.contains(p)) { // compare originaly provided with returned
                fail("missing given parameter [" + p + "] between returned at index " + i);
            }
        }

        returnedSet.clear();
        for (String p : parametersGiven) {
            p = handleEncoding(p);
            returnedSet.add(p);
        }
        for (int i = 1; i < parametersReturned.length; i++) { // skip the first elemenent - its = ""
            String p = parametersReturned[i];
            if(!returnedSet.contains(p)) {
                fail("missing returned parameter [" + p + "] between given");
            }
        }
    }

    private String handleEncoding(String p) throws UnsupportedEncodingException {
        int idx = p.indexOf("=");
        if(p.contains("Bug+creation")) {
            // got some special handling - see also QueryParameter.java
            p = p.substring(0, idx + 1) + URLEncoder.encode("[", "UTF-8") + "Bug+creation" + URLEncoder.encode("]", "UTF-8");
        } else {
            p = p.substring(0, idx + 1) + URLEncoder.encode(p.substring(idx + 1), "UTF-8");
        }
        return p;
    }

    private String getParametersUrl() {
        return  "&short_desc_type=allwordssubstr" +
                "&status_whiteboard_type=allwordssubstr" +
                "&status_whiteboard=xxx" +
                "&short_desc=xxx" +
                "&product=some+product" +
                "&component=some+component" +
                "&version=unspecified" +
                "&long_desc_type=substring" +
                "&long_desc=xxx" +
                "&bug_severity=blocker" +
                "&keywords_type=allwords" +
                "&keywords=xxx" +
                "&bug_status=NEW" +
                "&resolution=FIXED" +
                "&priority=P1" +
                "&target_milestone=" +
                "&emailassigned_to1=1&emailreporter1=1&emailcc1=1&emaillongdesc1=1&emailtype1=substring&email1=xxx" +
                "&chfieldfrom=2009-01-01&chfieldto=Now" +
                    "&chfield=[Bug+creation]" +
                    "&chfield=alias" +
                    "&chfield=assigned_to" +
                    "&chfield=cclist_accessible" +
                    "&chfield=component" +
                    "&chfield=deadline" +
                    "&chfield=everconfirmed" +
                    "&chfield=rep_platform" +
                    "&chfield=remaining_time" +
                    "&chfield=work_time" +
                    "&chfield=keywords" +
                    "&chfield=estimated_time" +
                    "&chfield=op_sys" +
                    "&chfield=priority" +
                    "&chfield=product" +
                    "&chfield=qa_contact" +
                    "&chfield=reporter_accessible" +
                    "&chfield=resolution" +
                    "&chfield=bug_severity" +
                    "&chfield=bug_status" +
                    "&chfield=short_desc" +
                    "&chfield=target_milestone" +
                    "&chfield=bug_file_loc" +
                    "&chfield=version" +
                    "&chfield=votes" +
                    "&chfield=status_whiteboard" +
                    "&chfieldvalue=xxx";
    }

    private class LogHandler extends Handler {
        final String msg;
        boolean done = false;
        public LogHandler(String msg) {
            this.msg = msg;
        }

        @Override
        public void publish(LogRecord record) {
            if(!done) {
                done = record.getMessage().startsWith(msg);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }
}
