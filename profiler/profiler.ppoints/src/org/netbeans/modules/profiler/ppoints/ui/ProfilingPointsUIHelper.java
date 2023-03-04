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
package org.netbeans.modules.profiler.ppoints.ui;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilingPointsUIHelper {
    
    private static ProfilingPointsUIHelper INSTANCE;
    
    
    public abstract boolean displaySubprojectsOption();
    
    public abstract String getAllProjectsString();
    
    
    public static class Basic extends ProfilingPointsUIHelper {

        @Override
        public boolean displaySubprojectsOption() {
            return false;
        }

        @Override
        public String getAllProjectsString() {
            return Bundle.ProfilingPointsWindowUI_AllProjectsString();
        }
        
    }
    
    
    static synchronized ProfilingPointsUIHelper get() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(ProfilingPointsUIHelper.class);
            if (INSTANCE == null) INSTANCE = new Basic();
        }
        return INSTANCE;
    }
    
}
