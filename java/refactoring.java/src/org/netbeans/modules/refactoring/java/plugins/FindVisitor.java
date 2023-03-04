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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class FindVisitor extends ErrorAwareTreePathScanner<Tree, Element> {

    private Collection<TreePath> usages = new ArrayList<TreePath>();
    
    protected CompilationController workingCopy;

    public FindVisitor(CompilationController workingCopy) {
        try {
            setWorkingCopy(workingCopy);
        } catch (ToPhaseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * 
     * @param workingCopy 
     * @throws org.netbeans.modules.refactoring.java.spi.ToPhaseException 
     */
    public void setWorkingCopy(CompilationController workingCopy) throws ToPhaseException {
        this.workingCopy = workingCopy;
        try {
            if (this.workingCopy.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                throw new ToPhaseException();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    public Collection<TreePath> getUsages() {
        return usages;
    }
    
    protected void addUsage(TreePath tp) {
        assert tp != null;
        usages.add(tp);
    }
}
