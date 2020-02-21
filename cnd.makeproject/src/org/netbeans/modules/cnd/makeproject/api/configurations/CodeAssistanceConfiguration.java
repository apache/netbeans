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
