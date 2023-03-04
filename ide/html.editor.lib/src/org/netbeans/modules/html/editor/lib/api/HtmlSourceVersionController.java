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

package org.netbeans.modules.html.editor.lib.api;

import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;

/**
 * Usefull for providing an html source code version for templating languages typically
 * embedding pieces of html source code w/o html doctype declaration.
 *
 * An implementation of this class should be registered into the global lookup.
 * Register your instance with an appropriate layer position!!!
 *
 * Implementations wishing to do the resolution on per project basis can use
 * source.getSourceFileObject() and then FileObjectQuery to get the project.
 *
 * @author marekfukala
 */
public interface HtmlSourceVersionController {

    /**
     * Returns a version of the html source code.
     *
     * @param detectedVersion Detected version of the html code. If your controller doesn't
     * want to adjust the version of the given source just return null so other controllers
     * can be possibly queried. Any non-null value will stop the query and use the returned
     * value.
     *
     * The argument can be null if no version is detected.
     *
     * @param analyzerResult The html source code
     *
     */
    public HtmlVersion getSourceCodeVersion(SyntaxAnalyzerResult analyzerResult, HtmlVersion detectedVersion);

}
