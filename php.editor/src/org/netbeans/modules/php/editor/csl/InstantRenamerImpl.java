/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.csl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.Occurence.Accuracy;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.parser.PHPParseResult;

/**
 *
 * @author Jan Lahoda, Radek Matous
 */
public class InstantRenamerImpl implements InstantRenamer {
    //TODO: instant rename isn't proper refactoring but cause it was released this way in 6.5
    // and because rename refactoring won't be implemented, so I've reverted it into
    //6.5 shape to supress the feeling there is a regression

    private static final boolean IS_RENAME_REFACTORING_ENABLED = true;
    private List<Occurence> allOccurences = Collections.emptyList();

    @Override
    public boolean isRenameAllowed(ParserResult info, int caretOffset, String[] explanationRetValue) {
        allOccurences.clear();
        PHPParseResult result = (PHPParseResult) info;
        final Model model = result.getModel();
        OccurencesSupport occurencesSupport = model.getOccurencesSupport(caretOffset);
        Occurence caretOccurence = occurencesSupport.getOccurence();
        if (caretOccurence != null) {
            final Accuracy accuracy = caretOccurence.degreeOfAccuracy();
            if (accuracy.equals(Occurence.Accuracy.EXACT) || accuracy.equals(Occurence.Accuracy.UNIQUE)) {
                if (IS_RENAME_REFACTORING_ENABLED) {
                    final Collection<? extends PhpElement> allDeclarations = caretOccurence.getAllDeclarations();
                    if (allDeclarations.size() != 1) {
                        return false;
                    }
                    PhpElement decl = allDeclarations.iterator().next();
                    if (decl instanceof VariableName) {
                        VariableName varName = (VariableName) decl;
                        if (!varName.isGloballyVisible() && !varName.representsThis()) {
                            return checkAll(caretOccurence);
                        }
                    } else if (decl instanceof MethodElement) {
                        MethodElement meth = (MethodElement) decl;
                        PhpModifiers phpModifiers = meth.getPhpModifiers();
                        if (phpModifiers.isPrivate()) {
                            return checkAll(caretOccurence);
                        }
                    } else if (decl instanceof FieldElement) {
                        FieldElement fld = (FieldElement) decl;
                        PhpModifiers phpModifiers = fld.getPhpModifiers();
                        if (phpModifiers.isPrivate()) {
                            return checkAll(caretOccurence);
                        }
                    }
                } else {
                    return checkAll(caretOccurence);
                }
            }
        }
        return false;
    }

    @Override
    public Set<OffsetRange> getRenameRegions(ParserResult info, int caretOffset) {
        Set<OffsetRange> retval = new HashSet<>();
        for (Occurence occurence : allOccurences) {
            retval.add(occurence.getOccurenceRange());
        }
        allOccurences.clear();
        return retval;
    }

    private boolean checkAll(Occurence caretOccurence) {
        List<Occurence> collected = new ArrayList<>();
        Collection<Occurence> all = caretOccurence.getAllOccurences();
        for (Occurence occurence : all) {
            if (IS_RENAME_REFACTORING_ENABLED) {
                if (occurence.getAllDeclarations().size() == 1) {
                    collected.add(occurence);
                } else {
                    allOccurences.clear();
                    return false;
                }
            } else {
                collected.add(occurence);
            }
        }
        allOccurences = collected;
        return true;
    }

}
