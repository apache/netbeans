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
package org.netbeans.modules.html.editor.spi;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * html.custom <-> html.editor internal communication only.
 * 
 * to be registered in global lookup.
 * 
 * @author marek
 */
public abstract class HintFixProvider {

    /**
     * The metadata map key for unknown attribute name.
     */
    public static final String UNKNOWN_ATTRIBUTE_FOUND = "unknown_attribute_found";

    /**
     * The metadata map key for unknown element name.
     */
    public static final String UNKNOWN_ELEMENT_FOUND = "unknown_element_found";
    
    /**
     * The metadata map key for the name of the parent of the unknown element.
     */
    public static final String UNKNOWN_ELEMENT_CONTEXT = "unknown_element_context";
    
    
    public abstract List<HintFix> getHintFixes(Context context);
    
    
    public static final class Context {
        
        private final Snapshot snapshot;
        private final HtmlParserResult result;
        private final Map<String, Object> metadata;

        public Context(Snapshot snapshot, HtmlParserResult result, Map<String, Object> metadata) {
            this.snapshot = snapshot;
            this.result = result;
            this.metadata = metadata;
        }

        public Snapshot getSnapshot() {
            return snapshot;
        }

        public HtmlParserResult getResult() {
            return result;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
        
    }
    
}
