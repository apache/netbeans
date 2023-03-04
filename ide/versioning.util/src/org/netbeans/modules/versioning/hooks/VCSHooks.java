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

package org.netbeans.modules.versioning.hooks;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 *
 * @author Tomas Stupka
 */
public class VCSHooks {

    private static VCSHooks instance;
    private Result<? extends VCSHookFactory> hooksResult;

    private VCSHooks() {
    }

    public static VCSHooks getInstance() {
        if (instance == null) {
            instance = new VCSHooks();
        }
        return instance;
    }
    
    public <T extends VCSHook> Collection<T> getHooks(Class<T> clazz) {
        List<T> ret = new LinkedList<T>();
        Collection<? extends VCSHookFactory> c = getFactories();
        for (VCSHookFactory f : c) {
            if(f.getHookType() == clazz) {
                VCSHook hook = f.createHook();
                ret.add((T) hook);
                continue;
            }
        }
        return ret;
    }

    private Collection<? extends VCSHookFactory> getFactories() {
        if(hooksResult == null) {
            hooksResult = Lookup.getDefault().lookupResult(VCSHookFactory.class);
        }
        if(hooksResult == null) {
            return Collections.emptyList();
        }
        Collection<VCSHookFactory> c = (Collection<VCSHookFactory>) Lookup.getDefault().lookupAll(VCSHookFactory.class);
        return hooksResult.allInstances();
    }

}
