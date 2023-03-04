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
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
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
                        if (phpModifiers.isPrivate() && !meth.getType().isTrait()) {
                            // NETBEANS-6087 private methods of trait are used in classes
                            return checkAll(caretOccurence);
                        }
                    } else if (decl instanceof FieldElement) {
                        FieldElement fld = (FieldElement) decl;
                        PhpModifiers phpModifiers = fld.getPhpModifiers();
                        if (phpModifiers.isPrivate() && !fld.getType().isTrait()) {
                            // NETBEANS-6087 private field of trait are used in classes
                            return checkAll(caretOccurence);
                        }
                    } else if (decl instanceof TypeConstantElement) {
                        TypeConstantElement cnst = (TypeConstantElement) decl;
                        PhpModifiers phpModifiers = cnst.getPhpModifiers();
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
