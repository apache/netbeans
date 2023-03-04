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

import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=AnnotationProcessingQueryImplementation.class,position=100)
public class ProjectAnnotationProcessingQueryImpl implements AnnotationProcessingQueryImplementation {

    public Result getAnnotationProcessingOptions(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project != null) {
            AnnotationProcessingQueryImplementation slq = project.getLookup().lookup(AnnotationProcessingQueryImplementation.class);
            if (slq != null) {
                return slq.getAnnotationProcessingOptions(file);
            }
        }
        return null;
    }

}
