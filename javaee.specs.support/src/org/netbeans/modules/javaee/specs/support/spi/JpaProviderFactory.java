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
package org.netbeans.modules.javaee.specs.support.spi;

import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
public final class JpaProviderFactory {
    
    public static JpaProvider createJpaProvider(JpaProviderImplementation impl) {
        return Accessor.getDefault().createJpaProvider(impl);
    }
    
    public static JpaProvider createJpaProvider(final String className, final boolean isDefault,
            final boolean isJpa1Supported, final boolean isJpa2Supported, final boolean isJpa21Supported) {
        return Accessor.getDefault().createJpaProvider(new JpaProviderImplementation() {

            @Override
            public boolean isJpa1Supported() {
                return isJpa1Supported;
            }

            @Override
            public boolean isJpa2Supported() {
                return isJpa2Supported;
            }

            @Override
            public boolean isJpa21Supported() {
                return isJpa21Supported;
            }

            @Override
            public boolean isDefault() {
                return isDefault;
            }

            @Override
            public String getClassName() {
                return className;
            }
        });
    } 
    
    public static abstract class Accessor {

        private static volatile Accessor accessor;

        public static void setDefault(Accessor accessor) {
            if (Accessor.accessor != null) {
                throw new IllegalStateException("Already initialized accessor"); // NOI18N
            }
            Accessor.accessor = accessor;
        }

        public static Accessor getDefault() {
            if (accessor != null) {
                return accessor;
            }

            Class c = JpaProvider.class;
            try {
                Class.forName(c.getName(), true, Accessor.class.getClassLoader());
            } catch (ClassNotFoundException cnf) {
                Exceptions.printStackTrace(cnf);
            }

            return accessor;
        }
        
        public abstract JpaProvider createJpaProvider(JpaProviderImplementation impl);
    }
}
