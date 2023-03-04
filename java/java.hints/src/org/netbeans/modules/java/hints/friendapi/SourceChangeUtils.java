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
package org.netbeans.modules.java.hints.friendapi;

import java.util.Set;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.OrganizeImports;

/**
 * Provides access to selected functions for friend modules. Not an official API - 
 * may be changed without further notice.
 * @author sdedic
 * @since 1.82
 */
public class SourceChangeUtils {
    /**
     * Organizes imports. Adds imports according to parameters, reorganizes imports
     * to use stars accoridng to coding style. If 'cleanImports' is true, 
     * 
     * @param copy
     * @param addImports imports to add
     * @param cleanImports if true, attempts to cleanup star imports
     * @throws IllegalStateException 
     */
    public static void doOrganizeImports(WorkingCopy copy, Set<Element> addImports, boolean cleanImports) throws IllegalStateException {
        OrganizeImports.doOrganizeImports(copy, addImports, cleanImports);
    }
}
