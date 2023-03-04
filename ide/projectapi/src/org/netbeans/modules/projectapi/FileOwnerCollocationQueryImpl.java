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

package org.netbeans.modules.projectapi;

import java.net.URI;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.util.lookup.ServiceProvider;

/**
 * A CollocationQueryImplementation implementation that collocates files based on
 * projects they are in.
 * @author Milos Kleint
 */
@ServiceProvider(service=CollocationQueryImplementation2.class, position=500)
public class FileOwnerCollocationQueryImpl implements CollocationQueryImplementation2 {

    @Override public URI findRoot(URI uri) {
        if (FileOwnerQuery.getOwner(uri) == null) {
            return null;
        }
        URI parent = uri;
        while (true) {
            uri = parent;
            parent = parent.resolve(parent.toString().endsWith("/") ? ".." : ".");
            if (FileOwnerQuery.getOwner(parent) == null) {
                break;
            }
            if (parent.getPath().equals("/")) {
                break;
            }
        }
        return uri;
        
    }

    @Override public boolean areCollocated(URI file1, URI file2) {
        URI root = findRoot(file1);
        boolean first = true;
        if (root == null) {
            root = findRoot(file2);
            first = false;
        }
        if (root != null) {
            String check = (first ? file2.toString() : file1.toString()) + '/';
            return check.startsWith(root.toString());
        }
        return false;
    }

}
