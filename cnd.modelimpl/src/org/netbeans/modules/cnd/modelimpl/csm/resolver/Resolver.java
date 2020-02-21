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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.csm.resolver;

import org.netbeans.modules.cnd.api.model.*;
import java.util.*;

/**
 */
public interface Resolver {
    public static final boolean TRACE_RECURSION = false;
    public static final int INFINITE_RECURSION = 200;
    public static final int LIMITED_RECURSION = 5;

    public static final int NAMESPACE = 1 << 0;
    public static final int CLASSIFIER = 1 << 1;
    public static final int CLASS = 1 << 2;
    public static final int TEMPLATE_CLASS = 1 << 3;
    public static final int CLASS_FORWARD = 1 << 4;
    public static final int ALL = NAMESPACE | CLASSIFIER | CLASS | TEMPLATE_CLASS | CLASS_FORWARD;

    public Collection<CsmProject> getLibraries();

    public CsmFile getStartFile();

    /**
     * Resolves classifier (class/enum/typedef) or namespace name.
     * Why classifier or namespace? Because in the case org::vk::test
     * you don't know which is class and which is namespace name
     *
     * @param nameTokens tokenized name to resolve
     * (for example, for std::vector it is new CharSequence[] { "std", "vector" })
     *
     * @return object of the following class:
     *  CsmClassifier (CsmClass, CsmEnum, CsmTypedef)
     *  CsmNamespace
     */
    public CsmObject resolve(CharSequence[] nameTokens, int interestedKind);
    
    /**
     * Check infinite recursion in resolving
     */
    public boolean isRecursionOnResolving(int maxRecursion);

    public CsmClassifier getOriginalClassifier(CsmClassifier orig);

    public interface SafeTemplateBasedProvider {
        boolean isTemplateBased(Set<CsmType> visited);
    }    
}
