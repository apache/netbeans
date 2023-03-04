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

package org.netbeans.modules.projectimport.eclipse.core;

import java.util.List;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;

/**
 * Represents classpath for an Eclipse project (.classpath file content)
 *
 * @author mkrauskopf
 */
public final class DotClassPath {

    private DotClassPathEntry output;
    
    private DotClassPathEntry jreContainer;
    
    private List<DotClassPathEntry> sourceRoots;
    private List<DotClassPathEntry> classpath;
    
    
    public DotClassPath(List<DotClassPathEntry> classpath, 
            List<DotClassPathEntry> sources, 
            DotClassPathEntry output,
            DotClassPathEntry jre) {
        this.sourceRoots = sources;
        this.classpath = classpath;
        this.output = output;
        this.jreContainer = jre;
        
    }
    
    public DotClassPathEntry getOutput() {
        return output;
    }
    
    List<DotClassPathEntry> getClassPathEntries() {
        return classpath;
    }
    
    /**
     * Just provides more convenient access to source entries.
     *
     * @see #getEntries()
     */
    List<DotClassPathEntry> getSourceRoots() {
        return sourceRoots;
    }

    void updateSourceRoots(List<DotClassPathEntry> sourceRoots) {
        this.sourceRoots = sourceRoots;
    }
    
    /**
     * Returns container classpath entry for JRE.
     *
     * @see #getEntries()
     */
    public DotClassPathEntry getJREContainer() {
        return jreContainer;
    }

    void updateClasspath(List<DotClassPathEntry> classpath) {
        this.classpath = classpath;
    }
    
    
}
