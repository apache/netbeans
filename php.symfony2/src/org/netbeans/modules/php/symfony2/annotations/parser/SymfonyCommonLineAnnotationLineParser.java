/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.annotations.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.annotation.util.AnnotationUtils;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SymfonyCommonLineAnnotationLineParser implements AnnotationLineParser {

    private static final AnnotationLineParser INSTANCE = new SymfonyCommonLineAnnotationLineParser();

    private static final Set<String> INLINE_ANNOTATIONS = new HashSet<>();
    static {
        INLINE_ANNOTATIONS.add("NotBlank"); //NOI18N
        INLINE_ANNOTATIONS.add("Blank"); //NOI18N
        INLINE_ANNOTATIONS.add("NotNull"); //NOI18N
        INLINE_ANNOTATIONS.add("Null"); //NOI18N
        INLINE_ANNOTATIONS.add("True"); //NOI18N
        INLINE_ANNOTATIONS.add("False"); //NOI18N
        INLINE_ANNOTATIONS.add("Email"); //NOI18N
        INLINE_ANNOTATIONS.add("MinLength"); //NOI18N
        INLINE_ANNOTATIONS.add("MaxLength"); //NOI18N
        INLINE_ANNOTATIONS.add("Url"); //NOI18N
        INLINE_ANNOTATIONS.add("Regex"); //NOI18N
        INLINE_ANNOTATIONS.add("Ip"); //NOI18N
        INLINE_ANNOTATIONS.add("Max"); //NOI18N
        INLINE_ANNOTATIONS.add("Min"); //NOI18N
        INLINE_ANNOTATIONS.add("Date"); //NOI18N
        INLINE_ANNOTATIONS.add("DateTime"); //NOI18N
        INLINE_ANNOTATIONS.add("Time"); //NOI18N
        INLINE_ANNOTATIONS.add("Choice"); //NOI18N
        INLINE_ANNOTATIONS.add("UniqueEntity"); //NOI18N
        INLINE_ANNOTATIONS.add("Language"); //NOI18N
        INLINE_ANNOTATIONS.add("Locale"); //NOI18N
        INLINE_ANNOTATIONS.add("Country"); //NOI18N
        INLINE_ANNOTATIONS.add("File"); //NOI18N
        INLINE_ANNOTATIONS.add("Image"); //NOI18N
        INLINE_ANNOTATIONS.add("Callback"); //NOI18N
        INLINE_ANNOTATIONS.add("Valid"); //NOI18N
    }

    private static final Set<String> TYPED_PARAMETERS = new HashSet<>();
    static {
        TYPED_PARAMETERS.add("type"); //NOI18N
    }

    // #258783 - register after Doctrine annotations (@Column)
    @AnnotationLineParser.Registration(position=1000)
    public static AnnotationLineParser getDefault() {
        return INSTANCE;
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        Map<OffsetRange, String> types = new HashMap<>();
        types.putAll(AnnotationUtils.extractInlineAnnotations(line, INLINE_ANNOTATIONS));
        types.putAll(AnnotationUtils.extractTypesFromParameters(line, TYPED_PARAMETERS));
        if (!types.isEmpty()) {
            result = new AnnotationParsedLine.ParsedLine("", types, line.trim());
        }
        return result;
    }

}
