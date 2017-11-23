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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author lahvac
 */
public class JDKModules {

    public static Collection<Module> loadJDKModules(ModuleManager mgr, Events ev, ClassLoader loader) throws InvalidException, DuplicateException {
        List<Module> result = new ArrayList<>();
        Properties modules2Packages = new Properties();
        Properties modules2Jars = new Properties();
        Properties modules2Classes = new Properties();

        try (InputStream packagesIn = JDKModules.class.getResourceAsStream("jdk/jdk8-modules-packages");
             InputStream jarsIn = JDKModules.class.getResourceAsStream("jdk/jdk8-modules-files");
             InputStream classesIn = JDKModules.class.getResourceAsStream("jdk/jdk8-modules-classes")) {
            modules2Packages.load(packagesIn);
            modules2Jars.load(jarsIn);
            modules2Classes.load(classesIn);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        for (String key : modules2Packages.stringPropertyNames()) {
            if (ClassLoader.getSystemClassLoader().getResource(modules2Classes.getProperty(key).replace('.', '/') + ".class") != null) {
                String packages = modules2Packages.getProperty(key);
                if (key.equals("java.base")) {
                    packages += ",sun.reflect"; //so that reflection works....
                }
                result.add(mgr.createJDK(ev, null, key, packages, loader));
            }
        }

        return result;
    }

}
