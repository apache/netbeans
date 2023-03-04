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
package org.netbeans.modules.xml.wsdl.model.extensions.http;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

public interface HTTPComponent extends ExtensibilityElement {

    public interface Visitor {
        void visit(HTTPBinding target);
        void visit(HTTPOperation target);
        void visit(HTTPUrlEncoded target);
        void visit(HTTPUrlReplacement target);
        void visit(HTTPAddress target);
    }
    
    void accept(Visitor visitor);
}
