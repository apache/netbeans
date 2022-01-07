/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.disco;

import io.foojay.api.discoclient.pkg.Pkg;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PkgSelection {

    public static @NonNull PkgSelection of(@NonNull Pkg pkg) {
        return new PkgSelection() {
            @Override
            public Pkg get(@Nullable Client d) {
                return pkg;
            }

            @Override
            public String getJavaPlatformDisplayName() {
                return pkg.getDistribution().getUiString() + " " + pkg.getJavaVersion().toString();
            }

            @Override
            public String getFileName() {
                return pkg.getFileName();
            }
        };
    }

    public @Nullable Pkg get(@Nullable Client d);

    public @NonNull String getFileName();

    public @NonNull String getJavaPlatformDisplayName();

}
