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

package org.netbeans.modules.maven.spi.actions;

import org.openide.util.Lookup;

/**
 * Register an {@code ActionConverter} in a Maven Project Lookup to convert
 * generic action names to project/plugin specific action names if needed.
 *
 * @author mkleint
 */
public interface ActionConvertor {

    /**
     * Shall return an action name converted from the given action the action
     * context is provided in the Lookup. If no conversion shall be applied
     * on the action, this method shall return {@code null}.
     *
     * @param action the action name to be converted
     * @param lookup the action context
     * @return the converted action name or {@code null} if this convertor
     *         does not support the given action.
     */
    String convert(String action, Lookup lookup);

}
