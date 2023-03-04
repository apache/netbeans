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
package org.netbeans.modules.javascript2.doc.spi;

/**
 *
 * @author Petr Hejl
 */
public final class ParameterFormat {
    
    private final String pre;
    
    private final String param;
    
    private final String post;

    public ParameterFormat(String pre, String param, String post) {
        this.pre = pre;
        this.param = param;
        this.post = post;
    }

    public String getPre() {
        return pre;
    }

    public String getPost() {
        return post;
    }

    public String getParam() {
        return param;
    }
    
}
