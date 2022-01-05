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
package org.netbeans.modules.nativeimage.spi.debug.filters;

import org.netbeans.modules.nativeimage.api.debug.NIVariable;

/**
 * Displayer of variables. Modifies the way how variables are presented.
 *
 * @since 1.0
 */
public interface VariableDisplayer {

    /**
     * Translate a list of original native variables into a list of variables
     * presented to the debugger user.
     * @since 1.0
     */
    NIVariable[] displayed(NIVariable... variables);

}
