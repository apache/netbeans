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
package org.netbeans.modules.groovy.qaf;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.junit.NbModuleSuite;

/**
 * Basic tests for Groovy samples
 * -create sample
 * -build sample
 *
 * @author lukas
 */
public class GroovySampleTest extends GroovyTestCase {

    public GroovySampleTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return getName().substring(4);
    }

    @Override
    protected ProjectType getProjectType() {
        return ProjectType.SAMPLE;
    }

    @Override
    protected String getSamplesCategoryName() {
        //XXX - where are you hidden?
        return "Groovy"; //NOI18N
    }

    /**
     * Test for Groovy-Java Demo sample
     *
     */
    public void testGroovyJavaDemo() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.groovy.samples.Bundle", "Templates/Project/Samples/Groovy/GroovyJavaDemoProject.zip");
        createProject(sampleName);
        buildProject();
    }

    /**
     * Test for NB Project Generators sample
     */
    public void testNBProjectGenerators() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.groovy.samples.Bundle", "Templates/Project/Samples/Groovy/NBProjectGeneratorsProject.zip");
        createProject(sampleName);
        buildProject();
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GroovySampleTest.class)
                .enableModules(".*").clusters(".*"));
    }
}
