/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.regex.Pattern;
import org.netbeans.modules.cnd.utils.MIMENames;

/**
 *
 */
public class CodeAssistanceConfiguration implements Cloneable {
    private MakeConfiguration makeConfiguration;
    private BooleanConfiguration buildAnalyzer;
    private BooleanConfiguration resolveSymbolicLinks;
    private VectorConfiguration<String> transientMacros;
    private VectorConfiguration<String> environmentVariables;
    private StringConfiguration tools;
    private BooleanConfiguration includeInCA;
    private StringConfiguration excludeInCA;
    public static final String DEFAULT_TOOLS = "gcc:c++:g++:clang:clang++:icc:icpc:ifort:gfortran:g77:g90:g95:cc:CC:ffortran:f77:f90:f95:ar:ld"; //NOI18N
    
    // Constructors
    public CodeAssistanceConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        buildAnalyzer = new BooleanConfiguration(true);
        resolveSymbolicLinks =  new BooleanConfiguration(false);
        tools = new StringConfiguration(null, DEFAULT_TOOLS);
        transientMacros = new VectorConfiguration<>(null);
        environmentVariables = new VectorConfiguration<>(null);
        includeInCA = new BooleanConfiguration(false);
        excludeInCA = new StringConfiguration(null, "");
    }

    public boolean getModified() {
        return getBuildAnalyzer().getModified() ||  getResolveSymbolicLinks().getModified() ||getTools().getModified() || 
                getEnvironmentVariables().getModified() || getTransientMacros().getModified() ||
                getIncludeInCA().getModified() || getExcludeInCA().getModified();
    }

    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    // Strip
    public void setBuildAnalyzer(BooleanConfiguration buildAnalyzer) {
        this.buildAnalyzer = buildAnalyzer;
    }

    public BooleanConfiguration getBuildAnalyzer() {
        return buildAnalyzer;
    }

    // Resolve symbolic links
    public void setResolveSymbolicLinks(BooleanConfiguration resolveSymbolicLinks) {
        this.resolveSymbolicLinks = resolveSymbolicLinks;
    }

    public BooleanConfiguration getResolveSymbolicLinks() {
        return resolveSymbolicLinks;
    }

    // Tool
    public void setTools(StringConfiguration tools) {
        this.tools = tools;
    }

    public StringConfiguration getTools() {
        return tools;
    }

    /**
     * @return the transientMacros
     */
    public VectorConfiguration<String> getTransientMacros() {
        return transientMacros;
    }

    /**
     * @param transientMacros the transientMacros to set
     */
    public void setTransientMacros(VectorConfiguration<String> transientMacros) {
        this.transientMacros = transientMacros;
    }

    /**
     * @return the environmentVariables
     */
    public VectorConfiguration<String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * @param environmentVariables the environmentVariables to set
     */
    public void setEnvironmentVariables(VectorConfiguration<String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public BooleanConfiguration getIncludeInCA() {
        return includeInCA;
    }

    public void setIncludeInCA(BooleanConfiguration includeInCA) {
        this.includeInCA = includeInCA;
    }
    
    public StringConfiguration getExcludeInCA() {
        return excludeInCA;
    }

    public void setExcludeInCA(StringConfiguration excludeInCA) {
        this.excludeInCA = excludeInCA;
    }
    
    // Clone and assign
    public void assign(CodeAssistanceConfiguration conf) {
        getBuildAnalyzer().assign(conf.getBuildAnalyzer());
        getResolveSymbolicLinks().assign(conf.getResolveSymbolicLinks());
        getTools().assign(conf.getTools());
        getTransientMacros().assign(conf.getTransientMacros());
        getEnvironmentVariables().assign(conf.getEnvironmentVariables());
        getIncludeInCA().assign(conf.getIncludeInCA());
        getExcludeInCA().assign(conf.getExcludeInCA());
    }

    @Override
    public CodeAssistanceConfiguration clone() {
        CodeAssistanceConfiguration clone = new CodeAssistanceConfiguration(getMakeConfiguration());
        clone.setBuildAnalyzer(getBuildAnalyzer().clone());
        clone.setResolveSymbolicLinks(getResolveSymbolicLinks().clone());
        clone.setTools(getTools().clone());
        clone.setTransientMacros(getTransientMacros().clone());
        clone.setEnvironmentVariables(getEnvironmentVariables().clone());
        clone.setIncludeInCA(getIncludeInCA().clone());
        clone.setExcludeInCA(getExcludeInCA().clone());
        return clone;
    }

    @Override
    public String toString() {
        return "{buildAnalyzer=" + buildAnalyzer + " tools=" + tools + '}'; // NOI18N
    }

    boolean includeInCA(Item item) {
        boolean add = getIncludeInCA().getValue();
        if (add) {
            if (MIMENames.isCppOrCOrFortran(item.getMIMEType())) {
                if (excludeInCA(item)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private Pattern lastIgnorePattern = null;
    boolean excludeInCA(Item item) {
        String ignore = getExcludeInCA().getValue();
        if (ignore.isEmpty()) {
            return false;
        }
        Pattern ignorePattern;
        synchronized (this) {
            if (lastIgnorePattern != null) {
                if (!lastIgnorePattern.pattern().equals(ignore)) {
                    lastIgnorePattern = null;
                }
            }
            if (lastIgnorePattern == null) {
                try {
                    lastIgnorePattern = Pattern.compile(ignore);
                } catch (Throwable ex) {
                    // do nothing
                }
            }
            ignorePattern = lastIgnorePattern;
        }
        if (ignorePattern != null) {
            return ignorePattern.matcher(item.getAbsolutePath()).find();
        }
        return false;
    }
    
}
