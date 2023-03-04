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
package org.netbeans.modules.xml.catalog.spi;

import java.io.IOException;

/**
 * The interface is intended for NetBeans IDE platform integration.
 * <p>
 * IDE Lookup searches <tt>CatalogProvider</tt> instances registered in Lookup area.
 * It is used as Class -> instance bridge as Lookup does not support
 * direct Class registrations.
 *
 * @author  Petr Kuzel
 * @version NetBeans IDE platform integration 1.0
 */
public interface CatalogProvider {

    /**
     * @return A class with public no arg constructor loaded by a ClassLoader
     *         allowing to load the rest of catalog implementation
     *         (must implement {@link CatalogReader})
     */
    public Class provideClass() throws IOException, ClassNotFoundException;
}
