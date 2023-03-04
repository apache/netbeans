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
package org.netbeans.spi.extexecution.open;

import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;

/**
 * Defines a handler for HTTP URL opening. May be used by default
 * {@link LineConvertor}s provided by {@link LineConvertors}.
 * <p>
 * Module implementing this interface should be marked as providing
 * <code>org.netbeans.spi.extexecution.open.HttpOpenHandler</code> token.
 *
 * @author Petr Hejl
 * @since 1.33
 */
public interface HttpOpenHandler {

    /**
     * Opens the URL.
     *
     * @param url URL to open
     */
    void open(@NonNull URL url);
}
