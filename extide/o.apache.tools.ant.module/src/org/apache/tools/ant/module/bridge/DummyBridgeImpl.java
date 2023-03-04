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

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.module.AntModule;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Enumerations;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Used when the real Ant class loader cannot be initialized for some reason.
 * @author Jesse Glick
 */
final class DummyBridgeImpl implements BridgeInterface, IntrospectionHelperProxy {
    
    private final Throwable problem;
    
    public DummyBridgeImpl(Throwable problem) {
        this.problem = problem;
        AntModule.err.notify(ErrorManager.INFORMATIONAL, problem);
    }
    
    public String getAntVersion() {
        return NbBundle.getMessage(DummyBridgeImpl.class, "ERR_ant_not_loadable", problem);
    }
    
    public boolean isAnt16() {
        return false;
    }
    
    public IntrospectionHelperProxy getIntrospectionHelper(Class<?> clazz) {
        return this;
    }
    
    public Class getAttributeType(String name) {
        throw new IllegalStateException();
    }
    
    public Enumeration<String> getAttributes() {
        return Enumerations.empty();
    }
    
    public Class getElementType(String name) {
        throw new IllegalStateException();
    }
    
    public Enumeration<String> getNestedElements() {
        return Enumerations.empty();
    }
    
    public boolean supportsCharacters() {
        return false;
    }
    
    public boolean toBoolean(String val) {
        return Boolean.valueOf(val).booleanValue();
    }
    
    public String[] getEnumeratedValues(Class<?> c) {
        return null;
    }

    @Override
    public boolean run(File buildFile, List<String> targets, InputStream in, OutputWriter out, OutputWriter err, Map<String,String> properties,
            Set<? extends String> concealedProperties, int verbosity, String displayName, Runnable interestingOutputCallback, ProgressHandle handle, InputOutput io) {
        err.println(NbBundle.getMessage(DummyBridgeImpl.class, "ERR_cannot_run_target"));
        problem.printStackTrace(err);
        return false;
    }
    
    public void stop(Thread process) {
        // do nothing
    }

}
