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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Simulates the platform selection logic in {@code build-impl.xml}.
 */
public class DestDirProvider extends ApisupportAntUtils.ComputedPropertyProvider {

    /**
     * @param eval {@code platform.properties} typically defining {@code nbplatform.active} but possibly directly defining {@code nbplatform.active.dir}
     */
    public DestDirProvider(PropertyEvaluator eval) {
        super(eval);
    }

    @Override protected Map<String, String> getProperties(Map<String, String> inputPropertyValues) {
        String platformS = inputPropertyValues.get("nbplatform.active.dir");
        if (platformS != null) {
            // This is really the recommended property, but for now a lot of code checks for NETBEANS_DEST_DIR, so copy it.
            return Collections.singletonMap(ModuleList.NETBEANS_DEST_DIR, platformS);
        }
        platformS = inputPropertyValues.get("nbplatform.active");
        if (platformS != null) {
            Map<String, String> m = new HashMap<String, String>();
            m.put(ModuleList.NETBEANS_DEST_DIR, "${nbplatform." + platformS + ".netbeans.dest.dir}");
            m.put("harness.dir", "${nbplatform." + platformS + ".harness.dir}");
            return m;
        }
        return Collections.emptyMap();
    }
    
    @Override protected Collection<String> inputProperties() {
        return Arrays.asList("nbplatform.active", "nbplatform.active.dir");
    }

}
