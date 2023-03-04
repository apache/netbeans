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

package org.netbeans.modules.queries;

import java.net.URI;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.util.lookup.ServiceProvider;

/**
 * Tests whether files are in parent-child relationship. Such files are
 * considered to be collocated.
 *
 * @author David Konecny
 */
@ServiceProvider(service=CollocationQueryImplementation2.class, position=100)
public class ParentChildCollocationQuery implements CollocationQueryImplementation2 {

    @Override public boolean areCollocated(URI file1, URI file2) {
        if (file1.equals(file2)) {
            return true;
        }
        String f1 = file1.toString();
        if (!f1.endsWith("/")) {
            f1 += "/";
        }
        String f2 = file2.toString();
        if (!f2.endsWith("/")) {
            f2 += "/";
        }
        return f1.startsWith(f2) || f2.startsWith(f1);
    }
    
    @Override public URI findRoot(URI file) {
        return null;
    }
    
}
