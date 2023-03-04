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
package org.netbeans.modules.web.el.spi;

import java.util.List;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Function {

    private String name;
    private String returnType;
    private List<String> parameters;
    private String description;

    public Function(String name, String returnType, List<String> parameters, String description) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.description = description;
    }

    /**
     * @return name of the function
     */
    public String getName() {
        return name;
    }

    /**
     * @return return type
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * @return {@code list} of method parameters
     */
    public List<String> getParameters() {
        return parameters;
    }

    /**
     * @return description of usage and functionality this function
     */
    public String getDescription() {
        return description;
    }
}
