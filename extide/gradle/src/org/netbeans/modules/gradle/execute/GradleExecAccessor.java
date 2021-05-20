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
package org.netbeans.modules.gradle.execute;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public abstract class GradleExecAccessor {
    
    private static final GradleExecConfiguration hookup = null;
    
    private static GradleExecAccessor INSTANCE;

    public static GradleExecAccessor instance() {
        return INSTANCE;
    }
    
    public abstract GradleExecConfiguration create(
            String id, String dispName, Map<String, String> projectProps, String cmdline);

    public abstract GradleExecConfiguration update(
            GradleExecConfiguration conf,
            String dispName, Map<String, String> projectProps, String cmdline);

    public GradleExecConfiguration copy(GradleExecConfiguration orig) {
        return create(orig.getId(), orig.getDisplayName(), orig.getProjectProperties(), orig.getCommandLineArgs());
    }
    
    protected GradleExecAccessor() {
    }
    
    public static void setInstance(GradleExecAccessor inst) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = inst;
    }
    
    @NbBundle.Messages({
        "CONFIG_DefaultConfigName=<default>"
    })
    public static GradleExecConfiguration createDefault() {
        return instance().create(GradleExecConfiguration.DEFAULT, Bundle.CONFIG_DefaultConfigName(), 
                Collections.emptyMap(), null);
    }
    
    static {
        try {
            Class.forName(GradleExecConfiguration.class.getName());
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
