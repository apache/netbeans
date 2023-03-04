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
package org.netbeans.modules.profiler.heapwalk.details.netbeans;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public class VCSDetailsProvider extends DetailsProvider.Basic {

    private static final String GIT_STATUS = "org.netbeans.libs.git.GitStatus"; // NOI18N
    
    public VCSDetailsProvider() {
        super(GIT_STATUS);
    }

    @Override
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (GIT_STATUS.equals(className))  {
            String relpath = DetailsUtils.getInstanceFieldString(instance, "relativePath", heap); // NOI18N
            String status = DetailsUtils.getInstanceFieldString(instance, "statusHeadWC", heap); // NOI18N
            if (status != null && relpath != null) {
                return status + " " + relpath;      // NOI18N 
            }
        }
        return null;
    }
}
