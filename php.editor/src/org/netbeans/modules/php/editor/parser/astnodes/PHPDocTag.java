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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 * Represent a PHPDoc tag in the php documentation
 * @author Petr Pisl
 */
public class PHPDocTag extends ASTNode {

    public enum Type implements AnnotationParsedLine {
        GLOBAL("global"), //NOI18N
        METHOD("method"), //NOI18N
        PROPERTY("property"), PROPERTY_READ("property-read"), PROPERTY_WRITE("property-write"), //NOI18N
        PARAM("param"), //NOI18N
        RETURN("return"), //NOI18N
        VAR("var"), //NOI18N
        DEPRECATED("deprecated"); //NOI18N

        private final String name;

        private Type(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            //description of these annotations is parsed by an editor parser itself
            return "";
        }

        @Override
        public Map<OffsetRange, String> getTypes() {
            //types of these annotations are parsed by an editor parser itself
            return Collections.EMPTY_MAP;
        }

        @Override
        public boolean startsWithAnnotation() {
            //all default annotations start at the start of the parsed line
            return true;
        }

    }

    final private AnnotationParsedLine type;
    final private String value;

    public PHPDocTag(int start, int end, AnnotationParsedLine kind, String value) {
        super(start, end);
        this.type = kind;
        this.value = value;
    }

    public AnnotationParsedLine getKind() {
        return this.type;
    }

    /**
     *
     * @return this is the whole text of the tag
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @return the documentation for a tag
     */
    public String getDocumentation() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
            visitor.visit(this);
    }
}
