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
package org.netbeans.modules.java.lsp.server;

import com.google.gson.GsonBuilder;

/**
 * Configure the GSON serializer that serializes messages to / from the
 * LSP server. Allows to export NetBeans complex objects, such as FileObject,
 * block unwanted fields from objects, or recreate complex objects by 
 * finding them based on the received wire data (i.e. recreate a Project
 * if a string directory path is given).
 * <p/>
 * An instance should be registered in the Lookup, using {@link ServiceProvider}
 * annotation.
 * 
 * @author sdedic
 */
public interface LspGsonSetup {
    /**
     * Configures GSon builder and returns the configured instance
     * @param b the builder, partially configured
     * @return the configured instance
     */
    public void configureBuilder(GsonBuilder b);
}
