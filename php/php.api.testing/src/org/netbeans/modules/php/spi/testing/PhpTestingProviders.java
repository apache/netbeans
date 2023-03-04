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
package org.netbeans.modules.php.spi.testing;

import java.util.List;

/**
 * Class which should be found in PHP module's lookup
 * that uses {@link PhpTestingProvider PhpTestingProviders}.
 * @since 0.17
 */
public interface PhpTestingProviders {

    /**
     * Gets list of testing providers that are enabled in this PHP module.
     * @return list of testing providers that are enabled in this PHP module; can be empty but never {@code null}
     */
    List<PhpTestingProvider> getEnabledTestingProviders();

}
