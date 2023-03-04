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

package org.apache.tools.ant.module.bridge.impl;

import java.io.IOException;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.taskdefs.Antlib;

/**
 * Antlib subclass used to define custom tasks inside NetBeans.
 * Needs to be a subclass to access {@link Antlib#setClassLoader}
 * and {@link Antlib#setURI}.
 * @author Jesse Glick
 */
public final class NbAntlib extends Antlib {
    
    /**
     * Process antlib.xml definitions.
     * @param p a project to add definitions to
     * @param antlib location of antlib.xml to load
     * @param uri a URI to add definitions in, or null
     * @param l a class loader to load defined classes from
     */
    public static void process(Project p, URL antlib, String uri, ClassLoader l) throws IOException, BuildException {
        ComponentHelper helper = ComponentHelper.getComponentHelper(p);
        helper.enterAntLib(uri);
        Antlib al;
        try {
            UnknownElement antlibElement = new ProjectHelper2().parseUnknownElement(p, antlib);
            al = new NbAntlib(uri, l);
            al.setProject(p);
            al.setLocation(antlibElement.getLocation());
            al.init();
            antlibElement.configure(al);
        } finally {
            helper.exitAntLib();
        }
        al.execute();
    }
    
    private NbAntlib(String uri, ClassLoader l) {
        if (uri != null) {
            setURI(uri);
        }
        setClassLoader(l);
    }
    
}
