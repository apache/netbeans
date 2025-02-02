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
package org.netbeans.modules.java.hints;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.util.NbBundle;

/**
 * @author mbien
 */
public class OrganizeMembersTest extends NbTestCase {
    
    public OrganizeMembersTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HintTest.create(); // initializes lookup (stolen from bugs.TinyTest)
        MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE)
                  .lookup(Preferences.class)
                  .putBoolean(FmtOptions.sortMembersInGroups, true);
    }

    @Override
    protected boolean runInEQ() {
        // without it, hint markers are sometimes not found
        return true;
    }
    
    public void testSimpleMemberSort() throws Exception {
        HintTest.create()
            .sourceLevel(11)
            .input(
                """
                package test;
                public class Test {
                    public static int b;
                    public static int a;
                }
                """
            )
            .run(OrganizeMembers.class)
            .findWarning("2:4-2:24:verifier:" + NbBundle.getMessage(OrganizeMembers.class, "MSG_OragnizeMembers"))
            .applyFix()
            .assertOutput(
                """
                package test;
                public class Test {
                    public static int a;
                    public static int b;
                }
                """
            );
    }
    
    public void testRecordMemberSort() throws Exception {
        HintTest.create()
            .sourceLevel(17)
            .input(
                """
                package test;
                public record Test(int d, int c) {
                    public static int b;
                    public static int a;
                }
                """
            )
            .run(OrganizeMembers.class)
            .findWarning("2:4-2:24:verifier:" + NbBundle.getMessage(OrganizeMembers.class, "MSG_OragnizeMembers"))
            .applyFix()
            .assertOutput(
                """
                package test;
                public record Test(int d, int c) {
                    public static int a;
                    public static int b;
                }
                """
            );
    }
    
    public void testEnumMemberSort() throws Exception {
        HintTest.create()
            .sourceLevel(11)
            .input(
                """
                package test;
                public enum Test {
                    B, A;
                    public static int b;
                    public static int a;
                }
                """
            )
            .run(OrganizeMembers.class)
            .findWarning("3:4-3:24:verifier:" + NbBundle.getMessage(OrganizeMembers.class, "MSG_OragnizeMembers"))
            .applyFix()
            .assertOutput(
                """
                package test;
                public enum Test {
                    B, A;
                    public static int a;
                    public static int b;
                }
                """
            );
    }

}
