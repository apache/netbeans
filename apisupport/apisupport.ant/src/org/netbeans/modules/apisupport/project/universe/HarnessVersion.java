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

package org.netbeans.modules.apisupport.project.universe;

import org.openide.modules.SpecificationVersion;
import static org.netbeans.modules.apisupport.project.universe.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * Release of the build harness.
 * Proceeds in chronological order so we can do compatibility tests with {@link #compareTo}.
 */
public enum HarnessVersion {

    /** Unknown version - platform might be invalid, or just predate any 5.0 release version. */
    UNKNOWN("0"),
    V50("1.6"),
    /** Harness version found in 5.0 update 1 and 5.5. */
    V50u1("1.7"),
    /** Harness version found in 5.5 update 1. */
    V55u1("1.9"),
    V60("1.10"),
    V61("1.11"),
    V65("1.12"),
    V67("1.14"),
    V68("1.18"),
    V69("1.20"),
    V70("1.23"),
    V71("1.27"),
    V72("1.30"),
    V73("1.32"),
    V74("1.35"),
    V80("1.37"),
    V81("1.40"),
    V82("1.42"),
    V90("1.43");

    /** spec version of org-netbeans-modules-apisupport-harness.jar */
    private final String minimumSpecVersion;

    private HarnessVersion(String minimumSpecVersion) {
        this.minimumSpecVersion = minimumSpecVersion;
    }

    /** Gets a quick display name. */
    @Messages({
        "LBL_harness_version_5.0=5.0",
        "LBL_harness_version_5.0u1=5.0 update 1 / 5.5",
        "LBL_harness_version_5.5u1=5.5 update 1",
        "LBL_harness_version_6.0=6.0",
        "LBL_harness_version_6.1=6.1",
        "LBL_harness_version_6.5=6.5",
        "LBL_harness_version_6.7=6.7",
        "LBL_harness_version_6.8=6.8",
        "LBL_harness_version_6.9=6.9",
        "LBL_harness_version_7.0=7.0",
        "LBL_harness_version_7.1=7.1",
        "LBL_harness_version_7.2=7.2",
        "LBL_harness_version_7.3=7.3",
        "LBL_harness_version_7.4=7.4",
        "LBL_harness_version_8.0=8.0",
        "LBL_harness_version_8.1=8.1",
        "LBL_harness_version_8.2=8.2",
        "LBL_harness_version_9.0=9.0",
        "LBL_harness_version_unknown=unknown"
    })
    public String getDisplayName() {
        switch (this) {
            case V50:
                return LBL_harness_version_5_0();
            case V50u1:
                return LBL_harness_version_5_0u1();
            case V55u1:
                return LBL_harness_version_5_5u1();
            case V60:
                return LBL_harness_version_6_0();
            case V61:
                return LBL_harness_version_6_1();
            case V65:
                return LBL_harness_version_6_5();
            case V67:
                return LBL_harness_version_6_7();
            case V68:
                return LBL_harness_version_6_8();
            case V69:
                return LBL_harness_version_6_9();
            case V70:
                return LBL_harness_version_7_0();
            case V71:
                return LBL_harness_version_7_1();
            case V72:
                return LBL_harness_version_7_2();
            case V73:
                return LBL_harness_version_7_3();
            case V74:
                return LBL_harness_version_7_4();
            case V80:
                return LBL_harness_version_8_0();
            case V81:
                return LBL_harness_version_8_1();
            case V82:
                return LBL_harness_version_8_2();
            case V90:
                return LBL_harness_version_9_0();
            default:
                assert this == UNKNOWN;
        }
        return LBL_harness_version_unknown();
    }

    static HarnessVersion forHarnessModuleVersion(SpecificationVersion v) {
        HarnessVersion[] versions = HarnessVersion.values();
        for (int i = versions.length - 1; i >= 0; i--) {
            if (v.compareTo(new SpecificationVersion(versions[i].minimumSpecVersion)) >= 0) {
                return versions[i];
            }
        }
        assert false : "UNKNOWN's 0 should have matched";
        return UNKNOWN;
    }

}
