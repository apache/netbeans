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
package org.netbeans.modules.xml.catalog.impl.sun;

import java.io.IOException;

import org.netbeans.modules.xml.catalog.spi.CatalogProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide class representing Catalog reader class.
 *
 * @author  Petr Kuzel
 * @version
 */
@ServiceProvider(service = CatalogProvider.class, position = 100)
public class SunCatalogProvider implements CatalogProvider {

    @Override
    public Class provideClass() throws IOException, ClassNotFoundException {
        return Catalog.class;
    }
}
