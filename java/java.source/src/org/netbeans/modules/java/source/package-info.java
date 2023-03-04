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

@TemplateRegistrations({
    @TemplateRegistration(folder = "Classes/Code", position = 100, content = "resources/GeneratedMethodBody.template", scriptEngine = "freemarker", displayName = "#GeneratedMethodBody", category = "java-code-snippet"),
    @TemplateRegistration(folder = "Classes/Code", position = 200, content = "resources/OverriddenMethodBody.template", scriptEngine = "freemarker", displayName = "#OverriddenMethodBody", category = "java-code-snippet"),
    /*see layer.xml for non-public templates*/
})
@NbBundle.Messages({
    "GeneratedMethodBody=Generated Method Body", "OverriddenMethodBody=Overridden Method Body" //NOI18N
})
package org.netbeans.modules.java.source;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.util.NbBundle;

