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

import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;

/**
 *
 * @author marekfukala
 */
public interface HtmlParseResult extends ParseResult {

    public HtmlVersion version();

    public HtmlModel model();

    /**
     * @return collection of possible open tags in the context
     */
    public Collection<HtmlTag> getPossibleOpenTags(Element context);
    
    /**
     * @return map of possible html tag to existing matching open tag node 
     * or null if the end tag doesn't have to have an open tag
     */
    public Map<HtmlTag, OpenTag> getPossibleCloseTags(Element context);

}
