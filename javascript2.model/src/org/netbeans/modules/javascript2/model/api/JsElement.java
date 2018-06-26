/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.model.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Petr Pisl
 */
public interface JsElement extends ElementHandle {

    public enum Kind {

        FUNCTION(1),
        METHOD(2),
        CONSTRUCTOR(3),
        OBJECT(4),
        PROPERTY(5),
        VARIABLE(6),
        FIELD(7),
        FILE(8),
        PARAMETER(9),
        ANONYMOUS_OBJECT(10),
        PROPERTY_GETTER(11),
        PROPERTY_SETTER(12),
        OBJECT_LITERAL(13),
        CATCH_BLOCK(14),
        WITH_OBJECT(15),
        CALLBACK(16),
        CLASS(17),
        GENERATOR(18),
        CONSTANT(19), 
        BLOCK(20);
        
        private final int id;
        private static final Map<Integer, Kind> LOOKUP = new HashMap<Integer, Kind>();
        
        static {
            for (Kind kind : EnumSet.allOf(Kind.class)) {
                LOOKUP.put(kind.getId(), kind);
            }
        }
        
        private Kind(int id) {
            this.id = id;
        }
        
        public int getId() {
            return this.id;
        }
        
        public static  Kind fromId(int id) {
            return LOOKUP.get(id);
        }
        
        public boolean isFunction() {
            return this == FUNCTION || this == METHOD || this == CONSTRUCTOR
                    || this == PROPERTY_GETTER || this == PROPERTY_SETTER 
                    || this == CALLBACK || this == GENERATOR;
        }
        
        public boolean isPropertyGetterSetter() {
            return this == PROPERTY_GETTER || this == PROPERTY_SETTER;
        }
        
    }

    int getOffset();

    OffsetRange getOffsetRange();

    Kind getJSKind();
    
    boolean isDeclared();

    boolean isPlatform();

    String getSourceLabel();
}
