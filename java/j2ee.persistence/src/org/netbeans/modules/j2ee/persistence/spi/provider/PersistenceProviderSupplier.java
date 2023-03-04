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

package org.netbeans.modules.j2ee.persistence.spi.provider;

import java.util.List;
import org.netbeans.modules.j2ee.persistence.provider.Provider;

/**
 * This interface should typically be implemented by projects where it 
 * is possible to use the Java Persistence API support. It provides means
 * for getting the supported persistence providers and for querying
 * whether a default persistence provider is supported.
 * 
 * @author Erno Mononen
 */
public interface PersistenceProviderSupplier {

    /**
     * Gets the persistence providers that are supported in 
     * the project. The preferred provider should
     * be the first item in the returned list.
     * 
     * @return a list of the supported providers, or an empty list if no
     * providers were supported; never null.
     */ 
    List<Provider> getSupportedProviders();

    /**
     * Queries whether a default persistence provider supported 
     * in the project (a default persistence provider is a provider that
     * doesn't need to be specified in persistence.xml). 
     * @return true if the project supports a default
     * persistence provider. 
     */ 
    boolean supportsDefaultProvider();

}
