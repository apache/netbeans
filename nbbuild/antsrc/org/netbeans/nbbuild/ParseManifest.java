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

package org.netbeans.nbbuild;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task which parses given manifest file, searches for given attribute
 * and stores it in property.
 * 
 * @author Richard Michalsky
 */
public class ParseManifest extends Task {

    /**
     * Path to manifest.
     */
    private File manifest;
    public void setManifest(File manifest) {
        this.manifest = manifest;
    }
    /**
     * Task sets attribute name to given property.
     * If attribute is not found, property remains unset.
     */
    private String property;
    public void setProperty(String property) {
        this.property = property;
    }

    private String attribute;
    /**
     * Name of attribute to be read from given manifest.
     * @param attribute
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }


    @Override
    public void execute() throws BuildException {
        if (manifest == null) {
            throw new BuildException("Must specify parameter 'manifest'.");
        }
        if (property == null) {
            throw new BuildException("Must specify parameter 'property'.");
        }
        if (attribute == null) {
            throw new BuildException("Must specify parameter 'attribute'.");
        }
        try {
            try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(manifest))) {
                Manifest mf = new Manifest(is);
                String attr = mf.getMainAttributes().getValue(attribute);
                if (attr == null)
                    return;
                getProject().setProperty(property, attr);
            }
        } catch (Exception x) {
            throw new BuildException("Reading manifest " + manifest + ": " + x, x, getLocation());
        }
    }

}
