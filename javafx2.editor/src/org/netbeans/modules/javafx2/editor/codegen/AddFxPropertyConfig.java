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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javafx2.editor.codegen;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class AddFxPropertyConfig {
    public static enum ACCESS {PRIVATE, PACKAGE, PROTECTED, PUBLIC};
    public static enum GENERATE {WRITABLE, READ_ONLY};
    
    private String name;
    private String initializer;
    private String propertyType;
    private String implementationType;
    private ACCESS access = AddFxPropertyConfig.ACCESS.PRIVATE;
    private GENERATE generate = AddFxPropertyConfig.GENERATE.WRITABLE;
    private boolean javadoc = true;

    public AddFxPropertyConfig(
            String name,
            String initializer,
            String propertyType,
            String implementationType,
            ACCESS access,
            GENERATE generate,
            boolean javadoc) {
        this.name = name;
        this.initializer = initializer;
        this.propertyType = propertyType;
        this.implementationType = implementationType;
        this.access = access;
        this.generate = generate;
        this.javadoc = javadoc;
    }

    public ACCESS getAccess() {
        return access;
    }

    public void setAccess(ACCESS access) {
        this.access = access;
    }

    public GENERATE getGenerate() {
        return generate;
    }

    public void setGenerate(GENERATE generate) {
        this.generate = generate;
    }

    public boolean isGenerateJavadoc() {
        return javadoc;
    }

    public void setGenerateJavadoc(boolean generateJavadoc) {
        this.javadoc = generateJavadoc;
    }

    public String getInitializer() {
        return initializer;
    }

    public void setInitializer(String initializer) {
        this.initializer = initializer;
    }       

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
    
    public String getImplementationType() {
        return implementationType;
    }
    
    public void setImplementationType(String implementationType) {
        this.implementationType = implementationType;
    }
}
