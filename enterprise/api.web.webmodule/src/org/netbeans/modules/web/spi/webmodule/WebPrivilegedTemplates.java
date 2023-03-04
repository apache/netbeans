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
package org.netbeans.modules.web.spi.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * List of templates which should be in the initial "privileged" list
 * when creating a new file. An instance should be placed in the
 * <code>j2ee/webtier/templates</code> folder in a module layer.
 *
 * @author Petr Pisl
 */
public interface WebPrivilegedTemplates {

    /**
     * Returns the list of templates which should be added in the initial "privileged" list
     * when created a new file.
     *
     * @param  webModule the web module to return the templates for.
     *         For example, it can be used to find out whether the web module is extended
     *         by a framework and then appropriate templates for the framework can be offered
     *         in the list of privileged list of templates.
     *
     * @return full paths to privileged templates, e.g. <samp>Templates/Other/XmlFile.xml</samp>; never null.
     */
    public String[] getPrivilegedTemplates(WebModule webModule);
}
