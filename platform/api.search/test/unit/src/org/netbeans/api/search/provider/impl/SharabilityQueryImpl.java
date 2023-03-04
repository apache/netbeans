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

package org.netbeans.api.search.provider.impl;

import java.io.File;
import java.net.URI;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Utilities;

/**
 * Primitive implementation of {@link SharabilityQuery}.
 *
 * @author  MarianPetras
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.SharabilityQueryImplementation2.class)
public class SharabilityQueryImpl implements SharabilityQueryImplementation2 {

    private static final String SHARABLE_SUFFIX = "_sharable";
    private static final String NON_SHARABLE_SUFFIX = "_unsharable";

    @Override
    public Sharability getSharability(URI uri) {
        final File file = Utilities.toFile(uri);
        if (file == null) {
            return Sharability.NOT_SHARABLE;
        }
        final String simpleName = getSimpleName(file);
        if (simpleName.endsWith(SHARABLE_SUFFIX)) {
            return Sharability.SHARABLE;
        } else if (simpleName.endsWith(NON_SHARABLE_SUFFIX)) {
            return Sharability.NOT_SHARABLE;
        } else {
            return file.isDirectory() ? Sharability.MIXED
                                      : Sharability.SHARABLE;
        }
    }

    private static String getSimpleName(File file) {
        String name = file.getName();
        if (file.isDirectory()) {
            return name;
        } else {
            int lastDotIndex = name.lastIndexOf('.');
            return (lastDotIndex != -1) ? name.substring(0, lastDotIndex)
                                        : name;
        }
    }

}
