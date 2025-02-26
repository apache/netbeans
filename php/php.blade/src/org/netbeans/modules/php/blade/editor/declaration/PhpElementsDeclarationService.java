/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.blade.editor.declaration;

import java.util.Collection;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.blade.csl.elements.ElementType;
import org.netbeans.modules.php.blade.csl.elements.NamedElement;
import org.netbeans.modules.php.blade.csl.elements.PhpFunctionElement;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexFunctionResult;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.editor.parser.BladePhpSnippetParser;
import static org.netbeans.modules.php.blade.editor.parser.BladePhpSnippetParser.PhpReference;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class PhpElementsDeclarationService {

    public PhpReference findReferenceAtCaret(ParserResult info, OffsetRange phpExprRange, int referenceOffset, FileObject file) {
        CharSequence phpExprSnippet = info.getSnapshot().getText().subSequence(phpExprRange.getStart(), phpExprRange.getEnd());
        BladePhpSnippetParser phpSnippetParser = new BladePhpSnippetParser(phpExprSnippet.toString(), file, referenceOffset);
        phpSnippetParser.parse();
        return phpSnippetParser.findIdentifierReference(referenceOffset);
    }

    public DeclarationLocation buildFunctionDeclLocation(PhpReference phpRef,
            Collection<PhpIndexFunctionResult> functionResults) {
        DeclarationLocation location = DeclarationLocation.NONE;
        for (PhpIndexFunctionResult indexResult : functionResults) {
            PhpFunctionElement resultHandle = new PhpFunctionElement(
                    phpRef.identifier,
                    indexResult.declarationFile,
                    ElementType.PHP_FUNCTION,
                    indexResult.getParams()
            );
            DeclarationLocation functionLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
            if (location.equals(DeclarationLocation.NONE)) {
                location = functionLocation;
            }
            location.addAlternative(new BladeDeclarationFinder.AlternativeLocationImpl(functionLocation));
        }

        return location;
    }

    public DeclarationLocation buildDeclLocation(String referenceIdentifier, ElementType type, Collection<PhpIndexResult> indexedResults) {
        DeclarationLocation location = DeclarationLocation.NONE;
        for (PhpIndexResult indexResult : indexedResults) {
            NamedElement resultHandle = new NamedElement(referenceIdentifier, indexResult.declarationFile, type);
            DeclarationLocation classLocation = new DeclarationFinder.DeclarationLocation(indexResult.declarationFile, indexResult.getStartOffset(), resultHandle);
            if (location.equals(DeclarationLocation.NONE)) {
                location = classLocation;
            }
            location.addAlternative(new BladeDeclarationFinder.AlternativeLocationImpl(classLocation));
        }
        return location;
    }
}
