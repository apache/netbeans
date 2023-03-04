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

import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public interface HtmlParser {

    /**
     * Returns a name of the parser. 
     * 
     * @return An internal identifier of the parser. Doesn't need to be localized, not presented to user.
     */
    @Deprecated
    public String getName();

    /**
     * Decides if the parser can parse parse html source of the given version.
     * 
     * @return true if the parser can parse given html version
     */
    public boolean canParse(HtmlVersion version);

    /**
     * Parses the given source.
     * 
     * @param source html source
     * @param preferedVersion represents a preferred html version if the version cannot be determined from the source
     * @param lookup contains some additional information necessary to the parser
     * @return instance of {@link HtmlParseResult}
     * @throws ParseException 
     */
    public HtmlParseResult parse(HtmlSource source, HtmlVersion preferedVersion, Lookup lookup) throws ParseException;

    /**
     * @deprecated Register an instance of {@link HtmlModelProvider} instead.
     */
    @Deprecated
    public HtmlModel getModel(HtmlVersion version);

}
