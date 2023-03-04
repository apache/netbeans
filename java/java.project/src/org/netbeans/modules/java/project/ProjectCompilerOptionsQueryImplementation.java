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

import java.util.Optional;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = 100)
public final class ProjectCompilerOptionsQueryImplementation implements CompilerOptionsQueryImplementation {

    @Override
    public Result getOptions(FileObject file) {
        return Optional.ofNullable(FileOwnerQuery.getOwner(file))
                .map((p) -> p.getLookup().lookup(CompilerOptionsQueryImplementation.class))
                .map((q) -> q.getOptions(file))
                .orElse(null);
    }
}
