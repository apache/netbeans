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
package org.netbeans.jellytools;

import junit.framework.Test;

/**
 *
 * @author Vojtech Sigler
 */
public class JavaBundleKeysTest extends TestBundleKeys {

    public static String propertiesName = "org/netbeans/jellytools/JavaBundleKeysTest.properties";

    public JavaBundleKeysTest(String isBundleName) {
        super(isBundleName);
    }

    @Override
    protected ClassLoader getDescendantClassLoader() {
        return JavaBundleKeysTest.class.getClassLoader();
    }

    /*
     * Overriden for the use in the non-static part of this class.
     */
    @Override
    public String getPropertiesName() {
        return propertiesName;
    }

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return prepareSuite(JavaBundleKeysTest.class, propertiesName);
    }
}
