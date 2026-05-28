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
package org.netbeans.nbexec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

/**
 *
 * @author Jaroslav Tulach
 */
public class MainCallback {
    static final String PROPERTY_ENABLE_TEST_NBEXEC_OPTIONS = "test.nbexec.options";
    static final String PROPERTY_PREFIX_TEST_NBEXEC_OPTIONS = PROPERTY_ENABLE_TEST_NBEXEC_OPTIONS + ".";

    public static void main(String[] args) throws IOException {
        File userDir = new File(System.getProperty("netbeans.user"));
        storeArgs(userDir, args);
        if (Boolean.parseBoolean(System.getProperty(PROPERTY_ENABLE_TEST_NBEXEC_OPTIONS))) {
            storeTestPropertyOptions(userDir);
        }
    }

    private static void storeArgs(File userDir, String[] args) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(userDir, "args")));
        oos.writeObject(args);
        oos.close();
    }

    private static void storeTestPropertyOptions(File userDir) throws IOException {
        Properties testProperties = new Properties();
        System.getProperties().forEach((k, v) -> {
            if (k instanceof String key && key.startsWith(PROPERTY_PREFIX_TEST_NBEXEC_OPTIONS))
                testProperties.put(k, v);
        });
        try (FileOutputStream fos = new FileOutputStream(new File(userDir, "options.properties"))) {
            testProperties.store(fos, "JVM flags starting with test.nbexec.");
        }
    }

    public static String[] getArgs(File userDir) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(userDir, "args")));
        return (String[])ois.readObject();
    }

    public static Properties getTestPropertyOptions(File userDir) throws IOException {
        Properties testProperties = new Properties();
        try (FileInputStream fis = new FileInputStream(new File(userDir, "options.properties"))) {
            testProperties.load(fis);
        }
        return testProperties;
    }
}
