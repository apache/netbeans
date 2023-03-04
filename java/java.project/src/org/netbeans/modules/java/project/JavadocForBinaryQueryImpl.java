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

package org.netbeans.modules.java.project;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;

/**
 * Delegates {@link JavadocForBinaryQueryImplementation} to the project which
 * owns the binary file.
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation.class, position=100)
public class JavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {

    private static final Logger LOG = Logger.getLogger(JavadocForBinaryQueryImpl.class.getName());
    
    /** Default constructor for lookup. */
    public JavadocForBinaryQueryImpl() {
    }
    
    public JavadocForBinaryQuery.Result findJavadoc(URL binary) {
        boolean log = LOG.isLoggable(Level.FINE);
        Project project = FileOwnerQuery.getOwner(URI.create(binary.toString()));
        if (project != null) {
            JavadocForBinaryQueryImplementation jfbqi = project.getLookup().lookup(JavadocForBinaryQueryImplementation.class);
            if (jfbqi != null) {
                JavadocForBinaryQuery.Result result = jfbqi.findJavadoc(binary);
                if (log) LOG.fine("Project " + project + " reported for " + binary + ": " + (result != null ? Arrays.asList(result.getRoots()) : null));
                return result;
            } else {
                if (log) LOG.fine("Project " + project + " did not have any JavadocForBinaryQueryImplementation");
            }
        } else {
            if (log) LOG.fine("No project found for " + binary + "; cannot find Javadoc");
        }
        return null;
    }
    
}
