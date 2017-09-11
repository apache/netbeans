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
package org.netbeans.modules.javafx2.editor.parser;

import org.netbeans.modules.javafx2.editor.completion.model.FxTreeUtilities;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBeanProvider;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.ErrorReporter;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;

/**
 * Environment used by model builder and attributing code.
 * 
 * @author sdedic
 */
public final class BuildEnvironment implements FxBeanProvider, ErrorReporter {
    /**
     * Javac compilation info
     */
    private CompilationInfo info;
    
    /**
     * Error reporting sink
     */
    private ErrorReporter   reporter;
    
    /**
     * Factory for BeanInfos, tied to the CompilationInfo
     */
    private FxBeanProvider beanProvider;
    
    /**
     * Accessor for model elements
     */
    private ModelAccessor accessor;
    
    /**
     * The underlying TokenHierarchy
     */
    private TokenHierarchy<XMLTokenId> hierarchy;
    
    /**
     * The model being built; top-level node of the model
     */
    private FxModel model;
    
    private FxTreeUtilities treeUtilities;
    
    public FxModel getModel() {
        return model;
    }

    void setModel(FxModel model) {
        this.model = model;
    }

    public TokenHierarchy<XMLTokenId> getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(TokenHierarchy<XMLTokenId> hierarchy) {
        this.hierarchy = hierarchy;
    }
    
    public ModelAccessor getAccessor() {
        return accessor;
    }
    
    void setAccessor(ModelAccessor accessor) {
        this.accessor = accessor;
    }
    
    void setCompilationInfo(CompilationInfo info) {
        this.info = info;
    }
    
    public CompilationInfo getCompilationInfo() {
        return info;
    }

    void setInfo(CompilationInfo info) {
        this.info = info;
    }
    
    @Override
    public void addError(ErrorMark error) {
        if (reporter != null) {
            reporter.addError(error);
        }
    }

    void setReporter(ErrorReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public FxBean getBeanInfo(String className) {
        if (beanProvider == null) {
            return null;
        } else {
            return beanProvider.getBeanInfo(className);
        }
    }

    void setBeanProvider(FxBeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    public FxTreeUtilities getTreeUtilities() {
        return treeUtilities;
    }

    void setTreeUtilities(FxTreeUtilities treeUtilities) {
        this.treeUtilities = treeUtilities;
    }
}
