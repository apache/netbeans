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
package org.netbeans.spi.whitelist.support;

import org.netbeans.modules.whitelist.WhiteListQueryMerger;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.netbeans.spi.project.LookupMerger;

/**
 * Support for writing {@link WhiteListQueryImplementation}.
 * @author David Konecny
 * @author Tomas Zezula
 */
public class WhiteListQueryMergerSupport {

    /**
     * Placed in a lookup this class will merge all other WhiteListQueryImplementation
     * registered in the same lookup. All individual WhiteListQueryImplementation must
     * allow invocation and/or overriding of a method to get positive return.
     */
    public static LookupMerger<WhiteListQueryImplementation> createWhiteListQueryMerger() {
        return new WhiteListQueryMerger();
    }
    
}
