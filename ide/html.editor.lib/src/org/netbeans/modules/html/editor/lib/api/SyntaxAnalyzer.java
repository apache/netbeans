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

import org.netbeans.modules.html.editor.lib.api.foreign.UndeclaredContentResolver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementsIterator;

/**
 * Plain HTML syntax analyzer
 *
 * @author mfukala@netbeans.org
 */
@Deprecated
public final class SyntaxAnalyzer extends ElementsIterator {

    public enum Behaviour {

        /**
         * set as SyntaxParserContext property if you do not want to check html
         * structure
         */
        DISABLE_STRUCTURE_CHECKS,
        /**
         * set as SyntaxParserContext property if you do not want to check html
         * attributes
         */
        DISABLE_ATTRIBUTES_CHECKS
    }
    
    private HtmlSource source;

    public static SyntaxAnalyzer create(HtmlSource source) {
        return new SyntaxAnalyzer(source);
    }

    private SyntaxAnalyzer(HtmlSource source) {
        super(source);
        this.source = source;
    }

    public SyntaxAnalyzerResult analyze() {
        return new SyntaxAnalyzerResult(source);
    }

    public SyntaxAnalyzerResult analyze(UndeclaredContentResolver resolver) {
        return new SyntaxAnalyzerResult(source, resolver);
    }

    public HtmlSource source() {
        return source;
    }

    public synchronized Iterator<Element> elementsIterator() {
        return this;
    }

    public synchronized SyntaxAnalyzerElements elements() {
        List<Element> result = new ArrayList<>();
        Iterator<Element> elementsIterator = elementsIterator();
        while (elementsIterator.hasNext()) {
            result.add(elementsIterator.next());
        }
        return new SyntaxAnalyzerElements(result);
    }

}
