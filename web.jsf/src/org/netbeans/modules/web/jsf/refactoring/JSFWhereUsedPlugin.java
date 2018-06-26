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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.jsf.refactoring;


import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Pisl
 */
public class JSFWhereUsedPlugin extends JavaRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    private static final Logger LOGGER = Logger.getLogger(JSFWhereUsedPlugin.class.getName());
    
    private final WhereUsedQuery refactoring;
    private TreePathHandle treePathHandle = null;
    
    /** Creates a new instance of JSFWhereUsedPlugin */
    public JSFWhereUsedPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() == null) {
            semafor.set(new Object());
            //TODO: should be improved.
            Object element = refactoring.getRefactoringSource().lookup(Object.class);
            LOGGER.fine("Prepare refactoring: " + element);                 // NOI18N
            
            if (element instanceof TreePathHandle) {
                treePathHandle = (TreePathHandle)element;
                if (treePathHandle != null && TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())){
                    Project project = FileOwnerQuery.getOwner(treePathHandle.getFileObject());
                    if (project != null){
                        Element resElement = JSFRefactoringUtils.resolveElement(getClasspathInfo(refactoring), refactoring, treePathHandle);
                        TypeElement type = (TypeElement) resElement;
                        String fqnc = type.getQualifiedName().toString();
                        List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(project, fqnc,"");
                        for (Occurrences.OccurrenceItem item : items) {
                            refactoringElements.add(refactoring, new JSFWhereUsedElement(item));
                        }
                    }
                }
            }
            semafor.set(null);
        }
        return null;
    }
    
    public class JSFWhereUsedElement extends SimpleRefactoringElementImplementation  {
        
        private final Occurrences.OccurrenceItem item;
        
        public JSFWhereUsedElement(Occurrences.OccurrenceItem item){
            this.item = item;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public String getDisplayText() {
            return item.getWhereUsedMessage();
        }
        
        public void performChange() {
        }
        
        public Element getJavaElement() {
            return null;
        }
        
        public FileObject getParentFile() {
            return item.getFacesConfig();
        }
        
        public PositionBounds getPosition() {
            return item.getChangePosition();
        }
        
        public Lookup getLookup() {
            return Lookups.singleton(item.getFacesConfig());
        }
        
    }
}
