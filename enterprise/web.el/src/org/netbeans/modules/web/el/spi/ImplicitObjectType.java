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

package org.netbeans.modules.web.el.spi;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author marekfukala
 */
public enum ImplicitObjectType {

        /** Object type implicit object - i.e. facesContext */
        OBJECT_TYPE,
        /** Map type implicit object - i.e. cookie */
        MAP_TYPE,
        /** Scope type implicit object - i.e. flowScope */
        SCOPE_TYPE,
        /** Raw implicit object - i.e. cc */
        RAW;

        /**
         * Returns all types.
         */
        public static final List<ImplicitObjectType> ALL_TYPES = Arrays.asList(ImplicitObjectType.MAP_TYPE,
            ImplicitObjectType.OBJECT_TYPE, ImplicitObjectType.RAW, ImplicitObjectType.SCOPE_TYPE);

}
