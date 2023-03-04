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
package org.netbeans.modules.tomcat5;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;

/**
 *
 * @author Petr Hejl
 */
public class TomcatFactoryTest extends NbTestCase {

    public TomcatFactoryTest(String name) {
        super(name);
    }

    public void testOldCreate50MethodForAutoupdateModule() {
        assertNotNull(TomcatFactory.create50());
    }
    
    public void testTomEEtype() {

        TomcatManager.TomEEType type = TomcatManager.TomEEType.TOMEE_WEBPROFILE;
        String file = "activemq-protobuf-1.1.jar";
        
        if (TomcatFactory.TOMEE_PLUME_JAR_PATTERN.matcher(file).matches()) {
            type = TomcatManager.TomEEType.TOMEE_PLUME;
        } else if (TomcatFactory.TOMEE_PLUS_JAR_PATTERN.matcher(file).matches()) {
            type = TomcatManager.TomEEType.TOMEE_PLUS;
        } else if (TomcatFactory.TOMEE_MICROPROFILE_JAR_PATTERN.matcher(file).matches()) {
            type = TomcatManager.TomEEType.TOMEE_MICROPROFILE;
        } else if (TomcatFactory.TOMEE_WEBPROFILE_JAR_PATTERN.matcher(file).matches()) {
            type = TomcatManager.TomEEType.TOMEE_WEBPROFILE;
        } else if (TomcatFactory.TOMEE_JAXRS_JAR_PATTERN.matcher(file).matches()) {
            type = TomcatManager.TomEEType.TOMEE_JAXRS;
        }
        
        assertEquals(TomcatManager.TomEEType.TOMEE_PLUS, type);
        assertNotSame(TomcatManager.TomEEType.TOMEE_MICROPROFILE, type);
        
    }
    
}
