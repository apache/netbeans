/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.j2ee.core.api.support.java.method;

import java.util.Collection;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;

/**
 * Provide a factory for obtaining MethodCustomizer instances
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class MethodCustomizerFactory {

    private MethodCustomizerFactory() {}
    
    public static MethodCustomizer businessMethod(String title, MethodModel method, ClasspathInfo cpInfo, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, boolean allowNoInterface, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                local,
                remote,
                selectLocal,
                selectRemote,
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                true,  // interfaces
                allowNoInterface,
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer homeMethod(String title, MethodModel method, ClasspathInfo cpInfo, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                local,
                remote,
                selectLocal,
                selectRemote,
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                true,  // interfaces
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer createMethod(String title, MethodModel method, ClasspathInfo cpInfo, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                local,
                remote,
                selectLocal,
                selectRemote,
                false,    // return type
                null,     // EJB QL
                false,    // finder cardinality
                true,     // exceptions
                true,     // interfaces
                "create", // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer finderMethod(String title, MethodModel method, ClasspathInfo cpInfo, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, String ejbql, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                local,
                remote,
                selectLocal,
                selectRemote,
                false,  // return type
                ejbql,  // EJB QL
                true,   // finder cardinality
                false,  // exceptions
                true,   // interfaces
                "find", // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer operationMethod(String title, MethodModel method, ClasspathInfo cpInfo, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                false, // interfaces
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer selectMethod(String title, MethodModel method, ClasspathInfo cpInfo, String ejbql, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                cpInfo,
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                true,        // return type
                ejbql,       // EJB QL
                false,       // finder cardinality
                false,       // exceptions
                false,       // interfaces
                "ejbSelect", // prefix
                existingMethods
                );
    }
    
}
