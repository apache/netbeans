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
package org.netbeans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author lahvac
 */
public class JDKModules {

    //XXX: transitive!
    public static Collection<Module> loadJDKModules(ModuleManager mgr, Events ev, ClassLoader loader) throws InvalidException, DuplicateException {
        List<Module> result = new ArrayList<>();

        try {
            Class<?> moduleClass = Class.forName("java.lang.Module");
            Method getDescriptor = moduleClass.getDeclaredMethod("getDescriptor");
            Method getName = moduleClass.getDeclaredMethod("getName");
            Class<?> moduleDescriptorClass = Class.forName("java.lang.module.ModuleDescriptor");
            Method exports = moduleDescriptorClass.getDeclaredMethod("exports");
            Class<?> exportsClass = Class.forName("java.lang.module.ModuleDescriptor$Exports");
            Method targets = exportsClass.getDeclaredMethod("targets");
            Method source = exportsClass.getDeclaredMethod("source");
            Class<?> moduleLayerClass = Class.forName("java.lang.ModuleLayer");
            Method boot = moduleLayerClass.getDeclaredMethod("boot");
            Object bootLayer = boot.invoke(null);
            Method modules = moduleLayerClass.getDeclaredMethod("modules");
            Set<?> jdkModules = (Set<?>) modules.invoke(bootLayer);

            for (Object jdkModule : jdkModules) {
                Iterable<Object> exportsSet = (Iterable<Object>) exports.invoke(getDescriptor.invoke(jdkModule));
                Set<String> packs = new HashSet<>();

                for (Object export : exportsSet) {
                    if (!((Collection<?>) targets.invoke(export)).isEmpty())
                        continue;

                    packs.add((String) source.invoke(export));
                }

                String packages = packs.stream().collect(Collectors.joining(","));
                String name = (String) getName.invoke(jdkModule);

                if (name.equals("java.base")) {
                    packages += ",jdk.internal.reflect"; //so that reflection works....
                }
                result.add(mgr.createJDK(ev, null, name, packages, loader));
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot read modules from the boot layer?", t);
        }

        return result;
    }

}
