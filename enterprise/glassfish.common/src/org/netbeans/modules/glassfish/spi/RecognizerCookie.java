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

package org.netbeans.modules.glassfish.spi;

import java.util.Collection;

/**
 * Addon modules should implement this interface to be able to process server
 * output in the log window.  Useful for turning stack traces into file links.
 *
 * @author Peter Williams
 */
public interface RecognizerCookie {

    /**
     * Called whenever the output window log readers are being configured.
     *
     * @return Collection of recognizer objects or empty collection if none
     *   available.
     */
    public Collection<? extends Recognizer> getRecognizers();
    
}
