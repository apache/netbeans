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
package org.netbeans.modules.java.api.common.ant;

import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Provide API for manipulating with project packages. This API is associated with <code>org.netbeans.api.java.queries.AccessibilityQuery</code>.
 * Packages passed to exportPackageAction method were marked by AccessibilityQuery to be either public or private.
 * @author mkozeny
 * @since 1.49
 */
public interface PackageModifierImplementation {
    
    
    /**
     * Do the export or unexport of passed set of packages
     * @param packagesToExport set of packages to export or to unexport
     * @param export whether passed set of packages should be export or unexport
     */
    public void exportPackageAction(@NonNull Collection<String> packagesToExport, boolean export);
    
}
