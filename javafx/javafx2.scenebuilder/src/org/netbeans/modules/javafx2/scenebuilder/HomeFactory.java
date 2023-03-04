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
package org.netbeans.modules.javafx2.scenebuilder;

/**
 *
 * @author Jaroslav Bachorik
 */
public interface HomeFactory {
    /**
     * Try to locate the default SB installation path
     * @return Returns the default SB installation path or null
     */
    Home defaultHome();
    /**
     * Loads a custom SB installation path
     * @param customPath The custom SB installation path
     * @return Returns a valid home or null
     */
    Home loadHome(String customPath);
}
