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
package org.netbeans.modules.csl.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;

/**
 * Implementations of this interface can be registered such that the formatter
 * helps indent or reformat source code, or even determine where the caret should
 * be placed on a newly created line.
 * 
 * @author Tor Norbye
 */
public interface Formatter {
    /**
     * Reformat the given portion of source code from startOffset to endOffset in the document.
     * You may use the provided parse tree information, if available, to guide formatting decisions.
     * The caret (if any) should be updated to the corresponding position that it was at before formatting.     * 
     */
    void reformat(@NonNull Context context, @NullAllowed ParserResult compilationInfo);

    /**
     * Reindent the source code. Adjusts indentation and strips trailing whitespace but
     * does not otherwise change the code. The caret (if any) should be updated to the corresponding
     * position that it was at before formatting.
     */
    void reindent(@NonNull Context context);

    /**
     * Return true if the reformat() task in this implementation utilizes the parse information.
     * If it doesn't, the infrastructure can skip producing a parse tree before calling reformat() which
     * has some performance benefits when the info isn't needed.
     */
    boolean needsParserResult();

    /**
     * Return the preferred size in characters of each indentation level for this language.
     * This is not necessarily going to mean spaces since the IDE may use tabs to perform
     * part of the indentation, but the number should reflect the number of spaces it would
     * visually correspond to. For example, the Sun JDK Java style guidelines would return
     * "4" here, and Ruby would return "2".
     *
     * @return The size in characters of each indentation level.
     */
    int indentSize();
    
    /**
     * Return the preferred "hanging indent" size, the amount of space to indent a continued
     * line such as the second line here:
     * <pre>
     *   foo = bar +
     *       baz
     * </pre>
     * The hanging indent is the indentation level difference between "baz" and "foo".
     */
    int hangingIndentSize();
}
