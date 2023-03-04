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
package org.netbeans.modules.nativeimage.api.debug;

/**
 * Representation of a native stack frame.
 *
 * @since 0.1
 */
public interface NIFrame {

    /**
     * Frame's thread ID.
     * @since 0.1
     */
    String getThreadId();

    /**
     * Frame's depth level. The top frame has level 0.
     * @since 0.1
     */
    int getLevel();

    /**
     * Frame's native address.
     * @since 0.1
     */
    String getAddress();

    /**
     * A short name of the file associated with the frame.
     * @since 0.1
     */
    String getShortFileName();

    /**
     * A full name of the file associated with the frame.
     * @since 0.1
     */
    String getFullFileName();

    /**
     * Name of the function associated with the frame.
     * @since 0.1
     */
    String getFunctionName();

    /**
     * 1-based line of the frame location.
     * @since 0.1
     */
    int getLine();
}
