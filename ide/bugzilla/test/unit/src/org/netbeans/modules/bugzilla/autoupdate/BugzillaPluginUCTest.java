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

package org.netbeans.modules.bugzilla.autoupdate;

import org.netbeans.modules.bugtracking.commons.AutoupdatePluginUCTestCase;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.bugtracking.commons.AutoupdateSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 */
public class BugzillaPluginUCTest extends AutoupdatePluginUCTestCase {

    String CATALOG_CONTENTS_FORMAT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.6//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_6.dtd\">" +
            "<module_updates timestamp=\"20/02/13/01/07/2009\">" +
            "<module autoload=\"false\" " +
                    "codenamebase=\"org.netbeans.libs.bugzilla\" " +
                    "distribution=\"modules/extra/org-netbeans-libs-bugzilla.nbm\" " +
                    "downloadsize=\"2546568\" " +
                    "eager=\"false\" " +
                    "homepage=\"http://www.netbeans.org/\" " +
                    "license=\"BE94B573\" " +
                    "moduleauthor=\"\" " +
                    "needsrestart=\"false\" " +
                    "releasedate=\"2009/05/27\">" +
                "<manifest AutoUpdate-Show-In-Client=\"true\" " +
                          "OpenIDE-Module=\"org.netbeans.libs.bugzilla\" " +
                          "OpenIDE-Module-Display-Category=\"Libraries\" " +
                          "OpenIDE-Module-Implementation-Version=\"090527\" " +
                          "OpenIDE-Module-Java-Dependencies=\"Java > 1.5\" " +
                          "OpenIDE-Module-Long-Description=\"This module bundles the Bugzilla connector implementation\" " +
                          "OpenIDE-Module-Module-Dependencies=\"org.jdesktop.layout/1 > 1.6, " +
                                                               "org.netbeans.libs.bugtracking > 1.0, " +
                                                               "org.netbeans.libs.commons_logging/1 > 1.7, " +
                                                               "org.openide.awt > 7.3, " +
                                                               "org.openide.dialogs > 7.8, " +
                                                               "org.openide.modules > 6.0, " +
                                                               "org.openide.nodes > 7.7, " +
                                                               "org.openide.util > 7.18, " +
                                                               "org.openide.windows > 6.24\" " +
                          "OpenIDE-Module-Name=\"Bugzilla Libraries\" " +
                          "OpenIDE-Module-Requires=\"org.openide.modules.ModuleFormat1\" " +
                          "OpenIDE-Module-Short-Description=\"Bundles Bugzilla Libraries\" " +
                          "OpenIDE-Module-Specification-Version=\"1.0.0\"/>" +
            "</module>" +
            "<module autoload=\"false\" " +
                    "codenamebase=\"{0}\" " +
                    "distribution=\"modules/extra/org-netbeans-modules-bugzilla.nbm\" " +
                    "downloadsize=\"192657\" " +
                    "eager=\"false\" " +
                    "homepage=\"http://www.netbeans.org/\" " +
                    "license=\"8B813426\" " +
                    "moduleauthor=\"\" " +
                    "needsrestart=\"true\" " +
                    "releasedate=\"2009/05/27\">" +
                "<manifest AutoUpdate-Show-In-Client=\"true\" " +
                          "OpenIDE-Module=\"{0}\" " +
                          "OpenIDE-Module-Implementation-Version=\"090527\" " +
                          "OpenIDE-Module-Java-Dependencies=\"Java > 1.5\" " +
                          "OpenIDE-Module-Long-Description=\"Support for Bugzilla issue tracker up to version {1} \" " +
                          "OpenIDE-Module-Module-Dependencies=\"org.jdesktop.layout/1 > 1.6, " +
                                                               "org.netbeans.api.progress/1 > 1.13, " +
                                                               "org.netbeans.libs.bugtracking > 1.0, " +
                                                               "org.netbeans.libs.commons_logging/1 > 1.7, " +
                                                               "org.netbeans.libs.bugzilla > 1.0, " +
                                                               "org.netbeans.modules.bugtracking > 1.0, " +
                                                               "org.netbeans.modules.kenai > 0.1, " +
                                                               "org.openide.awt > 7.3, " +
                                                               "org.openide.dialogs > 7.8, " +
                                                               "org.openide.filesystems > 7.21, " +
                                                               "org.openide.loaders > 7.5, " +
                                                               "org.openide.modules > 6.0, " +
                                                               "org.openide.nodes > 7.7, " +
                                                               "org.openide.util > 7.18, " +
                                                               "org.openide.windows > 6.24\" " +
                           "OpenIDE-Module-Name=\"Bugzilla\" " +
                           "OpenIDE-Module-Requires=\"org.openide.modules.ModuleFormat1\" " +
                           "OpenIDE-Module-Short-Description=\"Bugzilla\" " +
                           "OpenIDE-Module-Specification-Version=\"{2}\"/>" +
            "</module>" +
            "</module_updates>";

    public BugzillaPluginUCTest(String testName) {
        super(testName);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.create(BugzillaPluginUCTest.class, null, null);
    }
    
    public void testIsSupported() {
        assertTrue(BugzillaAutoupdate.getInstance().isSupportedVersion(BugzillaVersion.MIN_VERSION));
        assertTrue(BugzillaAutoupdate.getInstance().isSupportedVersion(BugzillaVersion.BUGZILLA_3_2));
        assertTrue(BugzillaAutoupdate.getInstance().isSupportedVersion(new BugzillaVersion("3.2.1")));
        assertTrue(BugzillaAutoupdate.getInstance().isSupportedVersion(getLower(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString())));
    }

    public void testIsNotSupported() {
        assertFalse(BugzillaAutoupdate.getInstance().isSupportedVersion(getHigherMicro(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString())));
        assertFalse(BugzillaAutoupdate.getInstance().isSupportedVersion(getHigherMinor(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString())));
        assertFalse(BugzillaAutoupdate.getInstance().isSupportedVersion(getHigherMajor(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString())));
    }

    public void testGetVersion() {
        assertEquals(new BugzillaVersion("1.1.1").toString(), BugzillaAutoupdate.getInstance().getVersion("test version 1.1.1 test").toString());
        assertEquals(new BugzillaVersion("1.1.1").toString(), BugzillaAutoupdate.getInstance().getVersion("test version 1.1.1 test").toString());
        assertEquals(new BugzillaVersion("1.1.1").toString(), BugzillaAutoupdate.getInstance().getVersion("test version 1.1.1").toString());
        assertEquals(new BugzillaVersion("1.1.1").toString(), BugzillaAutoupdate.getInstance().getVersion("version 1.1.1").toString());
        assertEquals(new BugzillaVersion("1.1").toString(), BugzillaAutoupdate.getInstance().getVersion("version 1.1").toString());
    }
    
    public void testGotVersion() {
        String desc = NbBundle.getBundle("org/netbeans/modules/bugzilla/Bundle").getString("OpenIDE-Module-Long-Description");
        BugzillaVersion version = BugzillaAutoupdate.getInstance().getVersion(desc);
        assertNotNull(version);
        assertEquals(BugzillaAutoupdate.SUPPORTED_BUGZILLA_VERSION.toString(), version.toString());
    }

    private BugzillaVersion getHigherMicro(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return new BugzillaVersion(new String("" + major + "." + minor + "." + ++micro));
    }

    private BugzillaVersion getHigherMinor(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return new BugzillaVersion(new String("" + major + "." + ++minor + "." + micro));
    }

    private BugzillaVersion getHigherMajor(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        return new BugzillaVersion(new String("" + ++major + "." + minor + "." + micro));
    }

    private BugzillaVersion getLower(String version) {
        String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        int major = segments.length > 0 ? toInt(segments[0]) : 0;
        int minor = segments.length > 1 ? toInt(segments[1]) : 0;
        int micro = segments.length > 2 ? toInt(segments[2]) : 0;
        if(micro > 0) {
            micro--;
        } else {
            if(minor > 0) {
                minor--;
            } else {
                major--;
            }
        }
        return new BugzillaVersion(new String("" + major + "." + minor + "." + ++micro));
    }

    private int toInt(String segment) {
        try {
            return segment.length() == 0 ? 0 : Integer.parseInt(getVersion(segment));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getVersion(String segment) {
        int n = segment.indexOf('-');
        return n == -1 ? segment : segment.substring(0, n);
    }

    @Override
    protected AutoupdateSupport getAutoupdateSupport() {
        return BugzillaAutoupdate.getInstance().getAutoupdateSupport();
    }

    @Override
    protected String getContentFormat() {
        return CATALOG_CONTENTS_FORMAT;
    }

    @Override
    protected String getCNB() {
        return BugzillaAutoupdate.BUGZILLA_MODULE_CODE_NAME;
    }

}
