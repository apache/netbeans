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

package org.netbeans.modules.java.freeform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Provides a FileBuiltQueryImplementation for the Java Freeform projects.
 * Currently, for each compilation unit, it looks to built-to element, finds the first
 * directory and supposes it is the target of compilation for this compilation unit.
 *
 * @author Jan Lahoda
 */
final class JavaFreeformFileBuiltQuery implements FileBuiltQueryImplementation, AntProjectListener {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(JavaFreeformFileBuiltQuery.class.getName());
    
    private Project project;
    private AntProjectHelper projectHelper;
    private PropertyEvaluator projectEvaluator;
    private AuxiliaryConfiguration aux;
    
    public JavaFreeformFileBuiltQuery(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        this.project = project;
        this.projectHelper = projectHelper;
        this.projectEvaluator = projectEvaluator;
        this.aux = aux;
        
        this.delegateTo = null;
        
        projectHelper.addAntProjectListener(this);
    }
    
    private FileBuiltQueryImplementation delegateTo;
    
    private FileBuiltQueryImplementation createDelegateTo() {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "JavaFreeformFileBuiltQuery.createDelegateTo start"); // NOI18N
        }
        
        Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
        List<String> from = new ArrayList<String>();
        List<String> to   = new ArrayList<String>();
        
        if (java != null) {
            List<Element> compilationUnits = XMLUtil.findSubElements(java);
            Iterator it = compilationUnits.iterator();
            while (it.hasNext()) {
                Element compilationUnitEl = (Element)it.next();
                assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
                List<String> rootNames = Classpaths.findPackageRootNames(compilationUnitEl);
                List<String> builtToNames = findBuiltToNames(compilationUnitEl);
                
                List<String> rootPatterns = new ArrayList<String>();
                String builtToPattern = null;
                
                for (String n : rootNames) {
                    rootPatterns.add(projectEvaluator.evaluate(n) + "/*.java"); // NOI18N
                }
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "Looking for a suitable built-to:"); // NOI18N
                }
                
                for (String n : builtToNames) {
                    String builtTo = projectEvaluator.evaluate(n);
                    if (builtTo == null) {
                        continue;
                    }
                    boolean isFolder = JavaProjectGenerator.isFolder(projectEvaluator, FileUtil.toFile(project.getProjectDirectory()), builtTo);
                    
                    if (isFolder && builtToPattern == null) {
                        builtToPattern = builtTo + "/*.class"; // NOI18N
                        break;
                    }
                }
                
                if (builtToPattern != null) {
                    if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                        ERR.log(ErrorManager.INFORMATIONAL, "Found built to pattern=" + builtToPattern + ", rootPatterns=" + rootPatterns); // NOI18N
                    }
                    for (String p : rootPatterns) {
                        from.add(p);
                        to.add(builtToPattern);
                    }
                } else {
                    if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                        ERR.log(ErrorManager.INFORMATIONAL, "No built to pattern found, rootPatterns=" + rootPatterns); // NOI18N
                    }
                }
            }
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "JavaFreeformFileBuiltQuery from=" + from + " to=" + to); // NOI18N
        }
        
        String[] fromStrings = from.toArray(new String[from.size()]);
        String[] toStrings = to.toArray(new String[to.size()]);
        
        FileBuiltQueryImplementation fbqi = projectHelper.createGlobFileBuiltQuery(projectEvaluator, fromStrings, toStrings);
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "JavaFreeformFileBuiltQuery.createDelegateTo end"); // NOI18N
        }
        
        return fbqi;
    }
    
    public void propertiesChanged(AntProjectEvent evt) {
        //ignore
    }
    
    public void configurationXmlChanged(AntProjectEvent evt) {
        synchronized (this) {
            delegateTo = null;
        }
    }
    
    public Status getStatus(final FileObject fo) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Status>() {
            public Status run() {
                synchronized (JavaFreeformFileBuiltQuery.this) {
                    if (delegateTo == null) {
                        delegateTo = createDelegateTo();
                    }
                    return delegateTo.getStatus(fo);
                }
            }
        });
    }
    
    static List<String> findBuiltToNames(Element compilationUnitEl) {
        List<String> names = new ArrayList<String>();
        for (Element e : XMLUtil.findSubElements(compilationUnitEl)) {
            if (!e.getLocalName().equals("built-to")) { // NOI18N
                continue;
            }
            String location = XMLUtil.findText(e);
            names.add(location);
        }
        return names;
    }
    
}
