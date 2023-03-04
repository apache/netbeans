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

package org.netbeans.spi.debugger;

import java.util.List;


/**
 * Abstract ancestor of classes providing lookup.
 *
 * @author Jan Jancura
 */
public interface ContextProvider {

    /**
     * Returns list of services of given type from given folder.
     *
     * @param folder a folder name or null
     * @param service a type of service to look for
     * @return list of services of given type
     */
    <T> List<? extends T> lookup(String folder, Class<T> service);

    /**
     * Returns one service of given type from given folder.
     *
     * @param folder a folder name or null
     * @param service a type of service to look for
     * @return ne service of given type
     */
    <T> T lookupFirst(String folder, Class<T> service);
}

