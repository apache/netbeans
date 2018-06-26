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
package org.netbeans.modules.javascript2.nodejs.editor.model;

import org.netbeans.modules.javascript2.model.spi.TypeNameConvertor;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.nodejs.editor.NodeJsUtils;

/**
 *
 * @author Petr Pisl
 */
@TypeNameConvertor.Registration(priority=200)
public class NodeJsTypeDisplayNameConvertor implements TypeNameConvertor {
    private static final String REQUIRE_MODULE_NAME = NodeJsUtils.REQUIRE_METHOD_NAME + "." + NodeJsUtils.FAKE_OBJECT_NAME_PREFIX;
    
    @Override
    public String getDisplayName(Type type) {
        String typeString = type.getType();
        String displayName = null;
        if (typeString != null 
                && (typeString.startsWith(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX)
                || typeString.startsWith(REQUIRE_MODULE_NAME))) {
            displayName = typeString.substring(typeString.indexOf(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX) + NodeJsUtils.FAKE_OBJECT_NAME_PREFIX.length());
            if (displayName.endsWith(NodeJsUtils.EXPORTS)) {
                int index = displayName.indexOf('.');
                if (index > 0) {
                    displayName = "Module " + displayName.substring(0, index); //NOI18N
                }
            } else {
                int index = displayName.lastIndexOf('.');
                if (index > 0) {
                    displayName = displayName.substring(index + 1);  
                } else {
                    displayName = "Module " + displayName; //NOI18N
                }
            }
        }
        return displayName;
    }
    
}
