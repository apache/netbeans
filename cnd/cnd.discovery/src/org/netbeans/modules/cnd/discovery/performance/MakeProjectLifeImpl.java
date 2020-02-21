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
package org.netbeans.modules.cnd.discovery.performance;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectLife;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=MakeProjectLife.class)
public class MakeProjectLifeImpl implements MakeProjectLife {
    private PerformanceIssueDetector detector;

    @Override
    public void start(Project project) {
        synchronized(this) {
             if (detector == null) {
                 detector = new PerformanceIssueDetector();
                 PerformanceLogger.getLogger().addPerformanceListener(detector);
             }
             detector.start(project);
        }
    }

    @Override
    public void stop(Project project) {
        synchronized(this) {
             if (detector != null) {
                 detector.stop(project);
             }
        }
    }
}
