/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
