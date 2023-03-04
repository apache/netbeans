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

package org.netbeans.modules.xml.api.model;

import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.openide.nodes.Node.Property;
import org.openide.util.Enumerations;

/**
 *
 * @author  Petr Kuzel <petr.kuzel@sun.com>
 */
public class SampleGrammarQueryManager extends GrammarQueryManager {

    /** Creates a new instance of Test */
    public SampleGrammarQueryManager() {
    }

    /** Can this manager provide a grammar for given context?
     * @param ctx Enumeration of DOM Nodes at Document level
     *        (never <code>null</code>). Method must not
     *        invoke <code>remove</remove> at the iterator.
     * @return <code>null</code> if a grammar cannot be provided for
     *         the context else return context items (subenum of
     *         passed one) that defines grammar enableness context.
     *
     */
    public Enumeration enabled(GrammarEnvironment ctx) {
        return Enumerations.empty();
    }
    
    /** @return detailed description.
     *
     */
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    /** Factory method providing a root grammar for given document.
     * @param input XML document input source that generated
     *        context passed to {@link #enable}.
     * @return GrammarQuery being able to work in the context
     *         or <code>null</null> if {@link #enabled} returns
     *         for the same context false.
     *
     */
    public GrammarQuery getGrammar(GrammarEnvironment input) {
        return new TestGrammar();
    }
    
    private static class TestGrammar implements GrammarQuery{
        
        /** @stereotype query
         * @output list of results that can be queried on name, and attributes
         * @time Performs fast up to 300 ms.
         * @param ctx represents virtual attribute <code>Node</code> to be replaced. Its parent is a element node.
         * @return enumeration of <code>GrammarResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
         *         Every list member represents one possibility.
         *
         */
        public Enumeration queryAttributes(HintContext ctx) {
            return Enumerations.empty();
        }
        
        /** @semantics Navigates through read-only Node tree to determine context and provide right results.
         * @postconditions Let ctx unchanged
         * @time Performs fast up to 300 ms.
         * @stereotype query
         * @param ctx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
         * @return enumeration of <code>GrammarResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes
         *         Every list member represents one possibility.
         *
         */
        public Enumeration queryElements(HintContext ctx) {
            return Enumerations.empty();
        }
        
        /** Allow to get names of <b>parsed general entities</b>.
         * @return enumeration of <code>GrammarResult</code>s (ENTITY_REFERENCE_NODEs)
         *
         */
        public Enumeration queryEntities(String prefix) {
            return Enumerations.empty();
        }
        
        /** Allow to get names of <b>declared notations</b>.
         * @return enumeration of <code>GrammarResult</code>s (NOTATION_NODEs)
         *
         */
        public Enumeration queryNotations(String prefix) {
            return Enumerations.empty();
        }
        
        /** Return options for value at given context.
         * It could be also used for completing of value parts such as Ant or XSLT property names (how to trigger it?).
         * @semantics Navigates through read-only Node tree to determine context and provide right results.
         * @postconditions Let ctx unchanged
         * @time Performs fast up to 300 ms.
         * @stereotype query
         * @input ctx represents virtual Node that has to be replaced (parent can be either Attr or Element), its own attributes does not name sense, it can be used just as the navigation start point.
         * @return enumeration of <code>GrammarResult</code>s (TEXT_NODEs) that can be queried on name, and attributes.
         *         Every list member represents one possibility.
         *
         */
        public Enumeration queryValues(HintContext ctx) {
            return Enumerations.empty();
        }

        public GrammarResult queryDefault(HintContext parentNodeCtx) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isAllowed(Enumeration en) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Component getCustomizer(HintContext nodeCtx) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean hasCustomizer(HintContext nodeCtx) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Property[] getProperties(HintContext nodeCtx) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
