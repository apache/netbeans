/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
