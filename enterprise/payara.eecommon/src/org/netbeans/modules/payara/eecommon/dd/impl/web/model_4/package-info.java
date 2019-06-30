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

@Schema2Beans(
        schema = "../../../resources/payara-web-app_4.dtd",
        schemaType = DTD,
        mddFile = "../../../resources/payara-web-app_4.mdd",
        outputType = TRADITIONAL_BASEBEAN,
        docRoot = "payara-web-app",
        useInterfaces = true,
        attrProp = true,
        java5 = true
)
package org.netbeans.modules.payara.eecommon.dd.impl.web.model_4;

import org.netbeans.modules.schema2beans.Schema2Beans;
import static org.netbeans.modules.schema2beans.Schema2Beans.OutputType.TRADITIONAL_BASEBEAN;
import static org.netbeans.modules.schema2beans.Schema2Beans.SchemaType.DTD;
