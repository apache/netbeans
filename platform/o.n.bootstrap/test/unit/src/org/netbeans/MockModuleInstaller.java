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

package org.netbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockModuleInstaller extends ModuleInstaller {

    // For examining results of what happened:
    public final List<String> actions = new ArrayList<String>();
    public final List<Object> args = new ArrayList<Object>();
    public final Map<String, String[]> provides = new HashMap<>();

    public void clear() {
        actions.clear();
        args.clear();
    }
    // For adding invalid modules:
    public final Set<Module> delinquents = new HashSet<Module>();
    // For adding modules that don't want to close:
    public final Set<Module> wontclose = new HashSet<Module>();

    @Override
    public String[] refineProvides(Module m) {
        return provides.get(m.getCodeNameBase());
    }

    public void prepare(Module m) throws InvalidException {
        if (delinquents.contains(m)) {
            throw new InvalidException(m, "not supposed to be installed");
        }
        actions.add("prepare");
        args.add(m);
    }

    public void dispose(Module m) {
        actions.add("dispose");
        args.add(m);
    }

    public void load(List<Module> modules) {
        actions.add("load");
        args.add(new ArrayList<Module>(modules));
    }

    public void unload(List<Module> modules) {
        actions.add("unload");
        args.add(new ArrayList<Module>(modules));
    }

    public boolean closing(List<Module> modules) {
        actions.add("closing");
        args.add(new ArrayList<Module>(modules));
        for (Module m : modules) {
            if (wontclose.contains(m)) {
                return false;
            }
        }
        return true;
    }

    public void close(List<Module> modules) {
        actions.add("close");
        args.add(new ArrayList<Module>(modules));
    }

}
