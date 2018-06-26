/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser.annotation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.project.api.PhpAnnotations;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class CustomTypeAnnnotationLineParser implements AnnotationLineParser {
    private static final AnnotationLineParser INSTANCE = new CustomTypeAnnnotationLineParser();
    private volatile boolean treatUnknownAnnotationsAsTypeAnnotations;

    @AnnotationLineParser.Registration(position = 10000)
    public static AnnotationLineParser getDefault() {
        return INSTANCE;
    }

    private CustomTypeAnnnotationLineParser() {
        treatUnknownAnnotationsAsTypeAnnotations = PhpAnnotations.getDefault().isUnknownAnnotationsAsTypeAnnotations();
        PhpAnnotations.getDefault().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PhpAnnotations.PROP_UNKNOWN_ANNOTATIONS_AS_TYPE_ANNOTATIONS.equals(evt.getPropertyName())) {
                    treatUnknownAnnotationsAsTypeAnnotations = (boolean) evt.getNewValue();
                }
            }
        });
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        if (treatUnknownAnnotationsAsTypeAnnotations) {
            result = parseLine(line, result);
        }
        return result;
    }

    private AnnotationParsedLine parseLine(String line, AnnotationParsedLine result) {
        String[] wsSeparatedLine = line.split("[\t ]", 2); //NOI18N
        if (wsSeparatedLine.length > 0) {
            String annotationPart = wsSeparatedLine[0];
            String[] tokens = annotationPart.split("\\(", 2); //NOI18N
            if (tokens.length > 0) {
                String annotation = tokens[0].trim();
                String description = line.substring(annotation.length()).trim();
                Map<OffsetRange, String> types = new HashMap<>();
                types.put(new OffsetRange(0, annotation.length()), annotation);
                result = new AnnotationParsedLine.ParsedLine(annotation, types, description, true);
            }
        }
        return result;
    }

}
