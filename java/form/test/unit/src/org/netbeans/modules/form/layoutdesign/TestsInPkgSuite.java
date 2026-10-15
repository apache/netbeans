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

package org.netbeans.modules.form.layoutdesign;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.openide.util.Exceptions;

/**
 * Test suite class that collects all test classes from package of given
 * annotated class. Does not work if the classes are in a JAR.
 */
public class TestsInPkgSuite extends Suite {

    public TestsInPkgSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(builder, klass, getPkgTestClasses(klass));
    }

    private static Class<?>[] getPkgTestClasses(Class<?> klass) throws InitializationError {
        ClassLoader classLoader = klass.getClassLoader();
        String pkgName = klass.getPackage().getName();
        URL url = classLoader.getResource(pkgName.replace('.', '/'));
        File pkgFolder;
        try {
            pkgFolder = new File(url.toURI());
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            throw new InitializationError(ex.getMessage());
        }

        List<Class<?>> classes = new LinkedList<Class<?>>();
        for (File f : pkgFolder.listFiles()) {
            String fileName = f.getName();
            if (fileName.endsWith("Test.class")) {
                try {
                    String simpleClsName = fileName.substring(0, fileName.length()-".class".length());
                    classes.add(Class.forName(pkgName + "." + simpleClsName, true, classLoader));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    throw new InitializationError(ex.getMessage());
                } catch (LinkageError ex) {
                    Exceptions.printStackTrace(ex);
                    throw new InitializationError(ex.getMessage());
                }
            }
        }

        return (Class<?>[]) classes.toArray(new Class<?>[0]);
    }
}
