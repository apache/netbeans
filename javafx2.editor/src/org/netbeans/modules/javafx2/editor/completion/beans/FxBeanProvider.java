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
package org.netbeans.modules.javafx2.editor.completion.beans;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;

/**
 * Resolves name to a {@link FxBean} instance. The Provider is bound to a certain
 * {@link CompilationInfo}; when the controlling ParserTask ends, the Provider
 * becomes invalid, and may throw exceptions.
 * 
 * @author sdedic
 */
public interface FxBeanProvider {
    /**
     * Constructs and returns FxBean definition of the Javafx bean.
     * May return {@code null}, if the definition cannot be created, e.g.
     * when the requested class is missing. Also returns {@code null}, if 
     * the passed classname is {@code null}.
     * 
     * @param fullClassName fully qualified name of the class, whose FxBean definition
     * should be created. {@code null} is accepted, and results in {@code null} result.
     * 
     * @return FxBean instance that describes the 'fullClassName' class JavaFX features,
     * or {@code null}, if the information could not be obtained.
     */
    @CheckForNull
    public FxBean   getBeanInfo(@NullAllowed String fullClassName);
    @NonNull
    public CompilationInfo getCompilationInfo();
}
