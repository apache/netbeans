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

@Multiple({
    @Schema2Beans(
        schema="../../resources/tomcat5_context.dtd",
        schemaType=SchemaType.DTD,
        mddFile="../../resources/tomcat5_context.mdd",
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        attrProp=true,
        java5=true
    ),
    @Schema2Beans(
        schema="../../resources/tomcat5_server.dtd",
        schemaType=SchemaType.DTD,
        mddFile="../../resources/tomcat5_server.mdd",
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        attrProp=true,
        java5=true
    ),
    @Schema2Beans(
        schema="../../resources/tomee_resources.dtd",
        schemaType=SchemaType.DTD,
        mddFile="../../resources/tomee_resources.mdd",
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        attrProp=true,
        java5=true
    )
})
package org.netbeans.modules.tomcat5.config.gen;

import org.netbeans.modules.schema2beans.Schema2Beans;
import org.netbeans.modules.schema2beans.Schema2Beans.Multiple;
import org.netbeans.modules.schema2beans.Schema2Beans.OutputType;
import org.netbeans.modules.schema2beans.Schema2Beans.SchemaType;
