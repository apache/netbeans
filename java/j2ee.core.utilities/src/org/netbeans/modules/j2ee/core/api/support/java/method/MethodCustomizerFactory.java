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
package org.netbeans.modules.j2ee.core.api.support.java.method;

import java.util.Collection;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;

/**
 * Provide a factory for obtaining MethodCustomizer instances
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class MethodCustomizerFactory {

    private MethodCustomizerFactory() {}
    
    public static MethodCustomizer businessMethod(String title, MethodModel method, ClasspathInfo cpInfo, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, boolean allowNoInterface, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                local,
                remote,
                selectLocal,
                selectRemote,
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                true,  // interfaces
                allowNoInterface,
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer homeMethod(String title, MethodModel method, ClasspathInfo cpInfo, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                local,
                remote,
                selectLocal,
                selectRemote,
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                true,  // interfaces
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer createMethod(String title, MethodModel method, ClasspathInfo cpInfo, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                local,
                remote,
                selectLocal,
                selectRemote,
                false,    // return type
                null,     // EJB QL
                false,    // finder cardinality
                true,     // exceptions
                true,     // interfaces
                "create", // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer finderMethod(String title, MethodModel method, ClasspathInfo cpInfo, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, String ejbql, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                local,
                remote,
                selectLocal,
                selectRemote,
                false,  // return type
                ejbql,  // EJB QL
                true,   // finder cardinality
                false,  // exceptions
                true,   // interfaces
                "find", // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer operationMethod(String title, MethodModel method, ClasspathInfo cpInfo, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                false, // interfaces
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer selectMethod(String title, MethodModel method, ClasspathInfo cpInfo, String ejbql, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                true,        // return type
                ejbql,       // EJB QL
                false,       // finder cardinality
                false,       // exceptions
                false,       // interfaces
                "ejbSelect", // prefix
                existingMethods
                );
    }
    
}
