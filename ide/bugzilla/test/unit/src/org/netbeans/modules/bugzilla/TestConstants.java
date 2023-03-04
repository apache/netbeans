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

package org.netbeans.modules.bugzilla;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 *
 * @author tomas
 */
public interface TestConstants {
    public static final String TEST_PROJECT = "unittest";
    public static final String TEST_PROJECT2 = "unittest2";
    public static final String REPO_PASSWD  = "unittest";
    public static final String REPO_HOST     = "bugtracking-test.cz.oracle.com";
    public static final String REPO_URL     = "http://" + REPO_HOST + "/bugzilla";
    public static final String REPO_USER    = "unittest@unit.test";
    public static final String REPO_USER_NAME    = "Mr. Unit Test Sr.";
    public static final String REPO_USER2    = "unittest2@unit.test";
    public static final String REPO_USER2_NAME    = "Mr. Unit Test Jr.";
    public static final String REPO_USER3    = "unittest3@unit.test";
    public static final String REPO_USER4    = "unittest4@unit.test";

    public static final String ISSUE_SEVERITY    = "bug";
    public static final String ISSUE_DESCRIPTION = "big bug";

    static NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();

}
