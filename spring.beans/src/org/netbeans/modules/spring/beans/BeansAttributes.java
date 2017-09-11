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

package org.netbeans.modules.spring.beans;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class BeansAttributes {

    private BeansAttributes() {
    }
    
    public static final String DEPENDS_ON = "depends-on"; // NOI18N
    public static final String PARENT = "parent"; // NOI18N
    public static final String FACTORY_BEAN = "factory-bean"; // NOI18N
    public static final String NAME = "name"; // NOI18N
    public static final String DEFAULT_LAZY_INIT = "default-lazy-init"; // NOI18N
    public static final String AUTOWIRE = "autowire"; // NOI18N
    public static final String DEFAULT_MERGE = "default-merge"; // NOI18N
    public static final String DEFAULT_DEPENDENCY_CHECK = "default-dependency-check"; // NOI18N
    public static final String DEFAULT_AUTOWIRE = "default-autowire"; // NOI18N
    public static final String DEPENDENCY_CHECK = "dependency-check"; // NOI18N
    public static final String LAZY_INIT = "lazy-init"; // NOI18N
    public static final String ABSTRACT = "abstract"; // NOI18N
    public static final String AUTOWIRE_CANDIDATE = "autowire-candidate"; // NOI18N
    public static final String MERGE = "merge"; // NOI18N
    public static final String RESOURCE = "resource"; // NOI18N
    public static final String INIT_METHOD = "init-method"; // NOI18N
    public static final String DESTROY_METHOD = "destroy-method"; // NOI18N
    public static final String CLASS = "class"; // NOI18N
    public static final String VALUE_TYPE = "value-type"; // NOI18N
    public static final String KEY_TYPE = "key-type"; // NOI18N
    public static final String TYPE = "type"; // NOI18N
    public static final String REF = "ref"; // NOI18N
    public static final String BEAN = "bean"; // NOI18N
    public static final String LOCAL = "local"; // NOI18N
    public static final String KEY_REF = "key-ref"; // NOI18N
    public static final String VALUE_REF = "value-ref"; // NOI18N
    public static final String REPLACER = "replacer";  // NOI18N
    public static final String FACTORY_METHOD = "factory-method"; // NOI18N
    public static final String ID = "id"; // NOI18N
    public static final String PRIMARY = "primary"; // NOI18N
    public static final String ALIAS = "alias"; // NOI18N
    public static final String BASE_PACKAGE = "base-package"; // NOI18N
}
