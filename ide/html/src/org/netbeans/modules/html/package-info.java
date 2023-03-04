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
    @TemplateRegistration(folder="Other", position=100, displayName="#Templates/Other/html.html", content="templates/html.html", scriptEngine="freemarker", category="simple-files", description="TemplateHelp.html", requireProject=false),
    @TemplateRegistration(folder="Other", position=200, displayName="#Templates/Other/xhtml.xhtml", content="templates/xhtml.xhtml", scriptEngine="freemarker", category="simple-files", description="XhtmlTemplateHelp.html", requireProject=false)
})
package org.netbeans.modules.html;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
