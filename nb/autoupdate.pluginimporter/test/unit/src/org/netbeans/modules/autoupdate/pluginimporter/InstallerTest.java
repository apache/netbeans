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
package org.netbeans.modules.autoupdate.pluginimporter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.NbBundle;

/**
 *
 */
public class InstallerTest {

    @Test
    public void testBranding() {
        // test that branding replacement contains 9.0 first Apache NetBeans incubating
        Comparator<String> versionComparator = (v1, v2) -> Float.compare(Float.parseFloat(v1), Float.parseFloat(v2));
        List<String> apacheversion = Arrays.asList(NbBundle.getMessage(Installer.class, "apachenetbeanspreviousversion").split(",")).stream().sorted(versionComparator.reversed()).collect(Collectors.toList());
        //Comparator<String> comp = (aName, bName) -> FloataName.compareTo(bName);
        Assert.assertTrue("apache version contains at last 9.0", apacheversion.contains("9.0"));
        Assert.assertTrue("last version reverse ordered should be 9.0", apacheversion.get(apacheversion.size() - 1).equals("9.0"));

    }

}
