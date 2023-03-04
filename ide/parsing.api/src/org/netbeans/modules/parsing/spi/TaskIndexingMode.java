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
package org.netbeans.modules.parsing.spi;

/**
 * Determines how to {@link ParserResultTask} behaves
 * during scan.
 * @author Tomas Zezula
 * @since 1.51
 */
public enum TaskIndexingMode {
    /**
     * The {@link ParserResultTask} is executed during the scan.
     */
    ALLOWED_DURING_SCAN,
    /**
     * The {@link ParserResultTask} is not executed during the scan,
     * it's deferred when the scan finished.
     */
    DISALLOWED_DURING_SCAN
}
