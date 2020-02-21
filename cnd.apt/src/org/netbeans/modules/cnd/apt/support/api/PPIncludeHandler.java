/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.support.api;

import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface PPIncludeHandler {
    // TODO: (get/set)State methods and interface could be hidded on impl layer,
    // because used only by PreprocHandler
    /*
     * save/restore state of handler
     */
    public State getState();
    public void setState(State state);
    
    /** immutable state object of include handler */    
    public interface State {
    };
    
    /**
     * returns the first file where include stack started
     */
    public StartEntry getStartEntry();
    
    public enum IncludeState {
        Success,
        Fail,
        Recursive
    }
    
    /*
     * 
     * notify about inclusion
     * @param path included file absolute path
     * @param line #include directive line
     * @param offset #include directive offset
     * @param resolvedDirIndex index of resolved directory in lists of include paths
     * @param inclDirIndex index in file
     * @return IncludeState.Recursive if inclusion is recursive and was prohibited
     */
    public IncludeState pushInclude(FileSystem fs, CharSequence path, int line, int offset, int resolvedDirIndex, int inclDirIndex);
    
    /*
     * notify about finished inclusion
     */
    public CharSequence popInclude();
    
    /**
     * include stack entry
     * - line where #include directive was
     * - resolved #include directive as absolute included path
     */
    public interface IncludeInfo {
        public FileSystem getFileSystem();
        public CharSequence getIncludedPath();
        public int getIncludeDirectiveLine();
        public int getIncludeDirectiveOffset();
        public int getResolvedDirectoryIndex();
        public int getIncludeDirectiveIndex();
    }     
}
