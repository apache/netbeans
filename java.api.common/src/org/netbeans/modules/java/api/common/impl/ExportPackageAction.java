/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.impl;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.ant.PackageModifierImplementation;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Submenu which permits the user to export or unexport appropriate package.
 * Must be locate in NetBeans Module Project.
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.apisupport.project.ExportPackageAction")
@ActionRegistration(
        displayName = "#CTL_ExportPackageAction", lazy = false, asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Projects/package/Actions", position = 100),
})
@Messages({"CTL_UnexportPackageAction=Unexport Package","CTL_ExportPackageAction=Export Package"})
public final class ExportPackageAction extends AbstractAction implements ContextAwareAction{

    @Override
    public void actionPerformed(ActionEvent ev) {
        //well, since someone can assign a shortcut ti the action, the invokation is unvaiodable, make it noop        
        //assert false : "Action should never be called without a context";
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if(actionContext != null) {
            Collection<? extends FileObject> selectedPackagesLookup = actionContext.lookupAll(FileObject.class);
            Collection<FileObject> selectedPackages = new ArrayList<>();
            for(FileObject packageIter:selectedPackagesLookup) {
                selectedPackages.add(packageIter);
            }
            Iterator<FileObject> selectedPackagesIterator = selectedPackages.iterator();
            Project project = null;
            if(selectedPackagesIterator.hasNext()) {
                project = FileOwnerQuery.getOwner(selectedPackagesIterator.next());
                if(project != null) {
                    while(selectedPackagesIterator.hasNext()) {
                        Project tmpProject = FileOwnerQuery.getOwner(selectedPackagesIterator.next());
                        if(!project.equals(tmpProject)) {
                            return new ExportPackageAction.ContextAction();
                        }
                    }
                } else {
                    return new ExportPackageAction.ContextAction();
                }
            }
            if (project != null) {
                PackageModifierImplementation pmi = project.getLookup().lookup(PackageModifierImplementation.class);
                if(pmi != null) {
                    Collection<String> packagesToExport = new ArrayList<>();
                    boolean export = false;
                    for(FileObject selectedPkgIter : selectedPackages) {
                        Boolean isPublic = AccessibilityQuery.isPubliclyAccessible(selectedPkgIter);
                        if (isPublic != null) {
                            ClassPath cp = ClassPath.getClassPath(selectedPkgIter, ClassPath.SOURCE);
                            assert cp != null;
                            String packageName = cp.getResourceName(selectedPkgIter, '.', false);
                            if (!isPublic) {
                                export = true;
                            }
                            packagesToExport.add(packageName);
                        }
                        else {
                            return new ExportPackageAction.ContextAction();
                        }
                    }
                    return new ExportPackageAction.ContextAction(pmi, packagesToExport, export);
                }
            }
        }
        return new ExportPackageAction.ContextAction();
    }

    /**
     * The particular instance of this action for a given package(s).
     */
    private static final class ContextAction extends AbstractAction {
        private Collection<String> packagesToExport; 
        private boolean export;
        private PackageModifierImplementation pmi;
        
        public ContextAction() {
            this(true, false);
        }
        
        public ContextAction(PackageModifierImplementation pmi, Collection<String> packagesToExport, boolean export) {
            this(export, packagesToExport!=null && !packagesToExport.isEmpty());
            this.pmi = pmi;
            this.packagesToExport = packagesToExport;
            this.export = export;
        }
        
        private ContextAction(boolean export, boolean enable) {
            super(export?Bundle.CTL_ExportPackageAction():Bundle.CTL_UnexportPackageAction());
            this.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            this.setEnabled(enable);
        }
        
        @Override
        public void actionPerformed(ActionEvent evt) {
            pmi.exportPackageAction(packagesToExport, export); 
        }
        
    }
}