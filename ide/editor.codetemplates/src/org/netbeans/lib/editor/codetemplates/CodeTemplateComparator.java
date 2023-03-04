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

package org.netbeans.lib.editor.codetemplates;

import java.util.Comparator;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;

/**
 * Comparator for code templates by their abbreviation name.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateComparator implements Comparator<CodeTemplate> {

    // public static final Comparator BY_ABBREVIATION = new CodeTemplateComparator(true, false);

    public static final Comparator<CodeTemplate> BY_ABBREVIATION_IGNORE_CASE = new CodeTemplateComparator(true, true);

    // public static final Comparator BY_PARAMETRIZED_TEXT = new CodeTemplateComparator(false, false);
    
    public static final Comparator<CodeTemplate> BY_PARAMETRIZED_TEXT_IGNORE_CASE = new CodeTemplateComparator(false, true);
    
    private final boolean byAbbreviation;
    
    private final boolean ignoreCase;
    
    private CodeTemplateComparator(boolean byAbbreviation, boolean ignoreCase) {
        this.byAbbreviation = byAbbreviation;
        this.ignoreCase = ignoreCase;
    }
    
    public int compare(CodeTemplate t1, CodeTemplate t2) {
        String n1 = byAbbreviation ? t1.getAbbreviation() : t1.getParametrizedText();
        String n2 = byAbbreviation ? t2.getAbbreviation() : t2.getParametrizedText();
        return ignoreCase ? n1.compareToIgnoreCase(n2) : n1.compareTo(n2);
    }
    
}
