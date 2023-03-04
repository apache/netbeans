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
        schema="../../resources/jboss-client_4_0_1.dtd",
        schemaType=SchemaType.DTD,
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        validate=true,
        attrProp=true,
        removeUnreferencedNodes=true,
        docRoot="jboss-client",
        java5=true
    ),
    @Schema2Beans(
        schema="../../resources/jboss-service_4_0_1.dtd",
        schemaType=SchemaType.DTD,
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        validate=true,
        attrProp=true,
        removeUnreferencedNodes=true,
        docRoot="server",
        java5=true
    ),
    @Schema2Beans(
        schema="../../resources/jboss_4_0_1.dtd",
        schemaType=SchemaType.DTD,
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        validate=true,
        attrProp=true,
        removeUnreferencedNodes=true,
        docRoot="jboss",
        java5=true
    ),
    @Schema2Beans(
        schema="../../resources/jboss-app_4_0_1.dtd",
        schemaType=SchemaType.DTD,
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        validate=true,
        attrProp=true,
        removeUnreferencedNodes=true,
        docRoot="jboss-app",
        java5=true
    ),
    @Schema2Beans(
        schema="../../resources/jboss-web_7_3_1.xsd",
        schemaType=SchemaType.XML_SCHEMA,
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        validate=true,
        attrProp=true,
        removeUnreferencedNodes=true,
        docRoot="jboss-web",
        java5=true
    ),
    @Schema2Beans(
        schema="../../resources/jboss-as-messaging-deployment_1_0_1.xsd",
        schemaType=SchemaType.XML_SCHEMA,
        outputType=OutputType.TRADITIONAL_BASEBEAN,
        validate=true,
        attrProp=true,
        removeUnreferencedNodes=true,
        docRoot="messaging-deployment",
        java5=true
    )
})
package org.netbeans.modules.javaee.wildfly.config.gen;

import org.netbeans.modules.schema2beans.Schema2Beans;
import org.netbeans.modules.schema2beans.Schema2Beans.Multiple;
import org.netbeans.modules.schema2beans.Schema2Beans.OutputType;
import org.netbeans.modules.schema2beans.Schema2Beans.SchemaType;
