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
package org.netbeans.modules.spring.api.beans;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains Spring annotations related constants.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class SpringAnnotations {

    public static final Set<String> SPRING_COMPONENTS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
            "org.springframework.stereotype.Component", //NOI18N
            "org.springframework.stereotype.Controller", //NOI18N
            "org.springframework.stereotype.Repository", //NOI18N
            "org.springframework.stereotype.Service" //NOI18N
            )));
}
