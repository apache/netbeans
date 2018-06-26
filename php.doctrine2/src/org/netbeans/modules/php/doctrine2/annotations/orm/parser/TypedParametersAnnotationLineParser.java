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
package org.netbeans.modules.php.doctrine2.annotations.orm.parser;

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
public class TypedParametersAnnotationLineParser implements AnnotationLineParser {

    private static final Map<String, Set<String>> ANNOTATIONS = new HashMap<>();
    static {
        Set<String> entityRegexs = new HashSet<>();
        entityRegexs.add("repositoryClass"); //NOI18N
        ANNOTATIONS.put("Entity", entityRegexs); //NOI18N

        Set<String> discriminatorMapRegexs = new HashSet<>();
        discriminatorMapRegexs.add("\\\"\\s*\\w+\\s*\\\""); //NOI18N
        ANNOTATIONS.put("DiscriminatorMap", discriminatorMapRegexs); //NOI18N

        Set<String> manyToOneRegexs = new HashSet<>();
        manyToOneRegexs.add("targetEntity"); //NOI18N
        ANNOTATIONS.put("ManyToOne", manyToOneRegexs); //NOI18N

        Set<String> manyToManyRegexs = new HashSet<>();
        manyToManyRegexs.add("targetEntity"); //NOI18N
        ANNOTATIONS.put("ManyToMany", manyToManyRegexs); //NOI18N

        Set<String> oneToOneRegexs = new HashSet<>();
        oneToOneRegexs.add("targetEntity"); //NOI18N
        ANNOTATIONS.put("OneToOne", oneToOneRegexs); //NOI18N

        Set<String> oneToManyRegexs = new HashSet<>();
        oneToManyRegexs.add("targetEntity"); //NOI18N
        ANNOTATIONS.put("OneToMany", oneToManyRegexs); //NOI18N
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        String[] tokens = line.split("\\("); //NOI18N
        for (Map.Entry<String, Set<String>> entry : ANNOTATIONS.entrySet()) {
            if (tokens.length > 0 && AnnotationUtils.isTypeAnnotation(tokens[0], entry.getKey())) {
                String annotation = tokens[0].trim();
                String description = line.substring(annotation.length()).trim();
                Map<OffsetRange, String> types = new HashMap<>();
                types.put(new OffsetRange(0, annotation.length()), annotation);
                types.putAll(AnnotationUtils.extractTypesFromParameters(line, entry.getValue()));
                result = new AnnotationParsedLine.ParsedLine(entry.getKey(), types, description, true);
                break;
            }
        }
        return result;
    }

}
