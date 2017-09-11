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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
