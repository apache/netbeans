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
package org.netbeans.modules.php.apigen.annotations.parser;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine.ParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
class ThrowsLineParser implements AnnotationLineParser {

    static final String ANNOTATION_NAME = "throws"; //NOI18N

    @Override
    public AnnotationParsedLine parse(final String line) {
        AnnotationParsedLine result = null;
        String[] tokens = line.split("[ \t]+"); //NOI18N
        if (tokens.length > 0 && ANNOTATION_NAME.equals(tokens[0])) {
            result = handleAnnotation(line, tokens);
        }
        return result;
    }

    private AnnotationParsedLine handleAnnotation(final String line, String[] tokens) {
        String description = "";
        Map<OffsetRange, String> types = new HashMap<>();
        if (tokens.length > 1) {
            description = tokens[1];
            int start = ANNOTATION_NAME.length() + countSpacesToFirstNonWhitespace(line.substring(ANNOTATION_NAME.length()));
            int end = start + description.length();
            types.put(new OffsetRange(start, end), line.substring(start, end));
        }
        return new ParsedLine(ANNOTATION_NAME, types, description, true);
    }

    private static int countSpacesToFirstNonWhitespace(final String line) {
        int result = 0;
        for (int i = 0; i < line.length(); i++) {
            if (Character.isWhitespace(line.charAt(i))) {
                result++;
            } else {
                break;
            }
        }
        return result;
    }

}
