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

package org.netbeans.modules.maven.indexer.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class NBVersionInfoTest extends NbTestCase {

    public NBVersionInfoTest(String name) {
        super(name);
    }

    public void testCompareTo() throws Exception {
        List<NBVersionInfo> versions = Arrays.asList(
            new NBVersionInfo("nb", "api", "mod", "RELEASE691", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "RELEASE691", null, null, null, null, "stuff"), // looks like dupe since toString omits classifier
            new NBVersionInfo("nb", "api", "mod", "RELEASE70-BETA", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "RELEASE67", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "RELEASE671", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "1.2.1", null, null, null, null, null), // let's throw in some
            new NBVersionInfo("nb", "api", "mod", "1.3", null, null, null, null, null), // numeric versions in case repo has them
            new NBVersionInfo("nb", "api", "newmod", "2.0", null, null, null, null, null), // other projects too, for some queries
            new NBVersionInfo("nb", "modules", "impl", "3.0", null, null, null, null, null), // even other groups
            new NBVersionInfo("other", "modules", "impl", "3.1", null, null, null, null, null), // but ignore repo in comparisons
            new NBVersionInfo("nb", "api", "mod", "RELEASE68", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "RELEASE68", null, null, null, null, null), // keep duplicates?
            new NBVersionInfo("nb", "api", "mod", "RELEASE69-BETA", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "RELEASE69", null, null, null, null, null));
        Collections.sort(versions);
        assertEquals("[api:mod:1.3:nb, api:mod:1.2.1:nb, "
                    + "api:mod:RELEASE70-BETA:nb, api:mod:RELEASE691:nb, api:mod:RELEASE691:nb, api:mod:RELEASE69:nb, api:mod:RELEASE69-BETA:nb, "
                    + "api:mod:RELEASE68:nb, api:mod:RELEASE68:nb, api:mod:RELEASE671:nb, api:mod:RELEASE67:nb, "
                    + "api:newmod:2.0:nb, modules:impl:3.1:other, modules:impl:3.0:nb]",
            versions.toString());

        assertTrue(new NBVersionInfo("nb", "api", "mod", "RELEASE69", "nbm", null, null, null, null).compareTo(new NBVersionInfo("nb", "api", "mod", "RELEASE69", "jar", null, null, null, null)) > 0);
        assertTrue(new NBVersionInfo("nb", "api", "mod", "RELEASE691", null, null, null, null, null).equals(new NBVersionInfo("nb", "api", "mod", "RELEASE691", null, null, null, null, null)));
        assertEquals(0, new NBVersionInfo("nb", "api", "mod", "RELEASE691", null, null, null, null, null).compareTo(new NBVersionInfo("nb", "api", "mod", "RELEASE691", null, null, null, null, null)));
    }
    
    public void skiptestSorting226100() {
        List<NBVersionInfo> versions = Arrays.asList(
            new NBVersionInfo("nb", "api", "mod", "7.0.0pre3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "7.0.0pre2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "7.0.0pre1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "7.0.0.pre5", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "7.0.0.pre4", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1H.5-beta", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1H.4rc1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1H.4-beta", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1H.14.1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1H.14.1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1H.14", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1H.14", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.9", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.8", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.7", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.6rc1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.6rc0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.6", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.5rc0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.5", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.4rc1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.4rc0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.4", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.2rc5", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.2rc4", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.2rc2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.2rc1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.2rc0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.2pre1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.2pre0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.26RC0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.26RC0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.26", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.26", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.25", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.25", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.24", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.24", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.23", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.23", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.22", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.22", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.21", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.21", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.20", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.20", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.1rc1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.1rc0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.19", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.19", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.18", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.18", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.17", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.16", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.16", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.rc5", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.rc5", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.rc4", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.rc4", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.rc3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.rc3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.rc2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.rc2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.pre0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15.pre0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.15", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.14", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.14", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12rc1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12.rc5", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12.rc5", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12.rc4", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12.rc4", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12.rc3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12.rc3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12.rc2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.12", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.11", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.10", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0rc3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0rc2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0rc1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0rc0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0pre3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0pre2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0pre1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0pre0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.1.0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.0.2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.0.1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.0.0rc4", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.0.0rc3", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.0.0rc2", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.0.0rc1", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.0.0rc0", null, null, null, null, null),
            new NBVersionInfo("nb", "api", "mod", "6.0.0", null, null, null, null, null)
            );
        Collections.sort(versions);
    }

    
    public void skiptestSorting226100VersionOnly() {
        List<ComparableVersion> versions = Arrays.asList(
            new ComparableVersion( "7.0.0pre3"),
            new ComparableVersion("7.0.0pre2"),
            new ComparableVersion("7.0.0pre1"),
            new ComparableVersion("7.0.0.pre5"),
            new ComparableVersion("7.0.0.pre4"),
            new ComparableVersion("6.1H.5-beta"),
            new ComparableVersion("6.1H.4rc1"),
            new ComparableVersion("6.1H.4-beta"),
            new ComparableVersion("6.1H.14.1"),
            new ComparableVersion("6.1H.14.1"),
            new ComparableVersion("6.1H.14"),
            new ComparableVersion("6.1H.14"),
            new ComparableVersion("6.1.9"),
            new ComparableVersion("6.1.8"),
            new ComparableVersion("6.1.7"),
            new ComparableVersion("6.1.6rc1"),
            new ComparableVersion("6.1.6rc0"),
            new ComparableVersion("6.1.6"),
            new ComparableVersion("6.1.5rc0"),
            new ComparableVersion("6.1.5"),
            new ComparableVersion("6.1.4rc1"),
            new ComparableVersion("6.1.4rc0"),
            new ComparableVersion("6.1.4"),
            new ComparableVersion("6.1.3"),
            new ComparableVersion("6.1.2rc5"),
            new ComparableVersion("6.1.2rc4"),
            new ComparableVersion("6.1.2rc2"),
            new ComparableVersion("6.1.2rc1"),
            new ComparableVersion("6.1.2rc0"),
            new ComparableVersion("6.1.2pre1"),
            new ComparableVersion("6.1.2pre0"),
            new ComparableVersion("6.1.26RC0"),
            new ComparableVersion("6.1.26RC0"),
            new ComparableVersion("6.1.26"),
            new ComparableVersion("6.1.26"),
            new ComparableVersion("6.1.25"),
            new ComparableVersion("6.1.25"),
            new ComparableVersion("6.1.24"),
            new ComparableVersion("6.1.24"),
            new ComparableVersion("6.1.23"),
            new ComparableVersion("6.1.23"),
            new ComparableVersion("6.1.22"),
            new ComparableVersion("6.1.22"),
            new ComparableVersion("6.1.21"),
            new ComparableVersion("6.1.21"),
            new ComparableVersion("6.1.20"),
            new ComparableVersion("6.1.20"),
            new ComparableVersion("6.1.2"),
            new ComparableVersion("6.1.1rc1"),
            new ComparableVersion("6.1.1rc0"),
            new ComparableVersion("6.1.19"),
            new ComparableVersion("6.1.19"),
            new ComparableVersion("6.1.18"),
            new ComparableVersion("6.1.18"),
            new ComparableVersion("6.1.17"),
            new ComparableVersion("6.1.16"),
            new ComparableVersion("6.1.16"),
            new ComparableVersion("6.1.15.rc5"),
            new ComparableVersion("6.1.15.rc5"),
            new ComparableVersion("6.1.15.rc4"),
            new ComparableVersion("6.1.15.rc4"),
            new ComparableVersion("6.1.15.rc3"),
            new ComparableVersion("6.1.15.rc3"),
            new ComparableVersion("6.1.15.rc2"),
            new ComparableVersion("6.1.15.rc2"),
            new ComparableVersion("6.1.15.pre0"),
            new ComparableVersion("6.1.15.pre0"),
            new ComparableVersion("6.1.15"),
            new ComparableVersion("6.1.15"),
            new ComparableVersion("6.1.14"),
            new ComparableVersion("6.1.14"),
            new ComparableVersion("6.1.12rc1"),
            new ComparableVersion("6.1.12.rc5"),
            new ComparableVersion("6.1.12.rc5"),
            new ComparableVersion("6.1.12.rc4"),
            new ComparableVersion("6.1.12.rc4"),
            new ComparableVersion("6.1.12.rc3"),
            new ComparableVersion("6.1.12.rc3"),
            new ComparableVersion("6.1.12.rc2"),
            new ComparableVersion("6.1.12"),
            new ComparableVersion("6.1.12"),
            new ComparableVersion("6.1.11"),
            new ComparableVersion("6.1.10"),
            new ComparableVersion("6.1.1"),
            new ComparableVersion("6.1.0rc3"),
            new ComparableVersion("6.1.0rc2"),
            new ComparableVersion("6.1.0rc1"),
            new ComparableVersion("6.1.0rc0"),
            new ComparableVersion("6.1.0pre3"),
            new ComparableVersion("6.1.0pre2"),
            new ComparableVersion("6.1.0pre1"),
            new ComparableVersion("6.1.0pre0"),
            new ComparableVersion("6.1.0"),
            new ComparableVersion("6.0.2"),
            new ComparableVersion("6.0.1"),
            new ComparableVersion("6.0.0rc4"),
            new ComparableVersion("6.0.0rc3"),
            new ComparableVersion("6.0.0rc2"),
            new ComparableVersion("6.0.0rc1"),
            new ComparableVersion("6.0.0rc0"),
            new ComparableVersion("6.0.0")
            );
        Collections.sort(versions);
    }
}
