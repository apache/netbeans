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
package org.netbeans.modules.payara.tooling.data;

import java.util.List;
import org.netbeans.modules.payara.tooling.server.config.JavaEESet;
import org.netbeans.modules.payara.tooling.server.config.JavaSESet;
import org.netbeans.modules.payara.tooling.server.config.LibraryNode;

/**
 * Payara configuration reader API interface.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public interface PayaraConfig {

    /**
     * Get Payara libraries configuration.
     * <p/>
     * @return Payara libraries configuration.
     */
    public List<LibraryNode> getLibrary();

    /**
     * Get Payara Java EE configuration.
     * <p/>
     * @return Payara JavaEE configuration.
     */
    public JavaEESet getJavaEE();

    /**
     * Get Payara Java SE configuration.
     * <p/>
     * @return Payara JavaSE configuration.
     */
    public JavaSESet getJavaSE();

    /**
     * Get Payara tools configuration.
     * <p/>
     * @return Payara tools configuration.
     */
    public ToolsConfig getTools();
    
}
