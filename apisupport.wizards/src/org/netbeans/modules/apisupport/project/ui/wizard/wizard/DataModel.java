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

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;

/**
 * Data model used across the <em>New Wizard Wizard</em>.
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {
    
    private CreatedModifiedFiles cmf;
    
    // first panel data (Wizard Type)
    private boolean branching;
    private TemplateType fileTemplateType;
    private int nOfSteps;
    
    // second panel data (Name, Icon and Location)
    private String prefix;
    private String displayName;
    private String category;
    private File origIconPath;
    
    DataModel(final WizardDescriptor wiz) {
        super(wiz);
        fileTemplateType = isHTMLUIPossible() ? TemplateType.HTML4J : TemplateType.FILE;
    }
    
    final boolean isHTMLUIPossible() {
        try {
            SpecificationVersion templates = getModuleInfo().getDependencyVersion("org.netbeans.api.templates");
            return templates != null;
        } catch (IOException ex) {
            return false;
        }
    }
    
    CreatedModifiedFiles getCreatedModifiedFiles() {
        if (cmf == null) {
            switch (getFileTemplateType()) {
                case CUSTOM:
                case FILE:
                    regenerate();
                    break;
                case HTML:
                case HTML4J:
                    regenerateHTML();
                    break;
            }
        }
        return cmf;
    }
    
    private void regenerate() {
        cmf = new CreatedModifiedFiles(getProject());
        
        Map<String, String> basicTokens = new HashMap<String, String>();
        basicTokens.put("PACKAGE_NAME", getPackageName()); // NOI18N
        basicTokens.put("WIZARD_PREFIX", prefix); // NOI18N

        List<String> panelClassNames = new ArrayList<String>();
        
        // Create wizard and visual panels
        for (int stepNumber = 1; stepNumber < (nOfSteps + 1); stepNumber++) {
            String visualPanelClass = prefix + "VisualPanel" + stepNumber; // NOI18N
            String wizardPanelClass = prefix + "WizardPanel" + stepNumber; // NOI18N
            
            Map<String, String> replaceTokens = new HashMap<String, String>(basicTokens);
            replaceTokens.put("VISUAL_PANEL_CLASS", visualPanelClass); // NOI18N
            replaceTokens.put("WIZARD_PANEL_CLASS", wizardPanelClass); // NOI18N
            replaceTokens.put("STEP_NAME", "Step #" + stepNumber); // NOI18N
            
            // generate .java file for visual panel
            String path = getDefaultPackagePath(visualPanelClass + ".java", false); // NOI18N
            FileObject template = CreatedModifiedFiles.getTemplate("visualPanel.java"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
            
            // generate .form file for visual panel
            path = getDefaultPackagePath(visualPanelClass + ".form", false); // NOI18N
            template = CreatedModifiedFiles.getTemplate("visualPanel.form"); // NOI18N
            cmf.add(cmf.createFile(path, template));
            
            // generate .java file for wizard panel
            path = getDefaultPackagePath(wizardPanelClass + ".java", false); // NOI18N
            template = CreatedModifiedFiles.getTemplate("wizardPanel.java"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));

            panelClassNames.add(wizardPanelClass);
        }

        cmf.add(cmf.addModuleDependency("org.netbeans.api.templates")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.util")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.util.ui")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.dialogs")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.awt")); // NOI18N
        
        Map<String,Object> replaceTokens = new HashMap<String,Object>(basicTokens);
        replaceTokens.put("panelClassNames", panelClassNames); // NOI18N

        // generate .java for wizard iterator
        if (fileTemplateType == TemplateType.FILE || branching) {
            String iteratorClass = prefix + "WizardIterator"; // NOI18N
            replaceTokens.put("ITERATOR_CLASS", iteratorClass); // NOI18N
            
            if (fileTemplateType == TemplateType.FILE) {
                // generate .html description for the template
                String lowerCasedPrefix = prefix.substring(0, 1).toLowerCase(Locale.ENGLISH) + prefix.substring(1);
                cmf.add(cmf.createFileWithSubstitutions(getDefaultPackagePath(lowerCasedPrefix, true) + ".html", CreatedModifiedFiles.getTemplate("wizardDescription.html"), Collections.<String,String>emptyMap())); // NOI18N
                boolean useTR = false;
                NbModuleProvider nbmp = getModuleInfo();
                if (nbmp != null) {
                    try {
                        SpecificationVersion v = nbmp.getDependencyVersion("org.openide.loaders");
                        if (v != null && v.compareTo(new SpecificationVersion("7.29")) >= 0) {
                            useTR = true;
                        }
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
                String instanceFullPath = category + '/' + lowerCasedPrefix;
                if (useTR) {
                    cmf.add(cmf.addModuleDependency("org.openide.loaders"));
                    replaceTokens.put("TR_folder", category.replaceFirst("^Templates/", ""));
                    replaceTokens.put("TR_displayName", displayName);
                    replaceTokens.put("TR_description", lowerCasedPrefix + ".html");
                } else {
                // add layer entry about a new file wizard
                cmf.add(cmf.createLayerEntry(instanceFullPath, null, null, displayName, null));
                cmf.add(cmf.createLayerAttribute(instanceFullPath, "template", Boolean.TRUE)); // NOI18N
                String fqIteratorClass = getPackageName() + '.' + iteratorClass;
                cmf.add(cmf.createLayerAttribute(instanceFullPath, "instantiatingIterator", // NOI18N
                        "newvalue:" + fqIteratorClass)); // NOI18N
                try {
                    URL url = new URL("nbresloc:/" + getPackageName().replace('.','/') + '/' // NOI18N
                            + lowerCasedPrefix + ".html"); // NOI18N
                    cmf.add(cmf.createLayerAttribute(instanceFullPath, "templateWizardURL", url)); // NOI18N
                } catch (MalformedURLException ex) {
                    Util.err.notify(ex);
                }
                }
                
                // Copy wizard icon
                FileObject origIcon = origIconPath != null ? FileUtil.toFileObject(origIconPath) : null;
                if (origIcon != null) {
                    String relToSrcDir = addCreateIconOperation(cmf, origIcon);
                    if (useTR) {
                        replaceTokens.put("TR_iconBase", relToSrcDir);
                    } else {
                    cmf.add(cmf.createLayerAttribute(instanceFullPath, "iconBase", relToSrcDir)); // NOI18N
                    }
                }
            }
            FileObject template = CreatedModifiedFiles.getTemplate(
                fileTemplateType == TemplateType.FILE ? 
                "instantiatingIterator.java" : "wizardIterator.java" // NOI18N
            );
            String path = getDefaultPackagePath(iteratorClass + ".java", false); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
        } else {
            String path = getDefaultPackagePath(prefix + "WizardAction.java", false); // NOI18N
            FileObject template = CreatedModifiedFiles.getTemplate("sampleAction.java"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
        }
    }
    
    private void regenerateHTML() {
        boolean isJava = fileTemplateType == TemplateType.HTML4J;
        cmf = new CreatedModifiedFiles(getProject());
        
        Map<String, Object> basicTokens = new HashMap<String, Object>();
        basicTokens.put("PACKAGE_NAME", getPackageName()); // NOI18N
        basicTokens.put("WIZARD_PREFIX", prefix); // NOI18N

        cmf.add(cmf.addModuleDependency("org.netbeans.api.templates")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.util")); // NOI18N
        if (isJava) {
            cmf.add(cmf.addModuleDependency("net.java.html")); // NOI18N
            cmf.add(cmf.addModuleDependency("net.java.html.json")); // NOI18N
        }
        cmf.add(cmf.addManifestToken(ManifestManager.OPENIDE_MODULE_NEEDS, "org.netbeans.api.templates.wizard")); // NOI18N
        cmf.add(cmf.addManifestToken(ManifestManager.OPENIDE_MODULE_NEEDS, "javax.script.ScriptEngine.freemarker")); // NOI18N
        
        basicTokens.put("HTML4J", isJava);
        
        // generate .html description for the template
        String lowerCasedPrefix = prefix.substring(0, 1).toLowerCase(Locale.ENGLISH) + prefix.substring(1);
        cmf.add(cmf.createFileWithSubstitutions(
            getDefaultPackagePath(lowerCasedPrefix, true) + "Descr.html", // NOI18N
            CreatedModifiedFiles.getTemplate("wizardDescription.html"), // NOI18N
            Collections.<String,String>emptyMap())); 
        
        cmf.add(cmf.createFileWithSubstitutions(
            getDefaultPackagePath(lowerCasedPrefix, true) + ".html", // NOI18N
            CreatedModifiedFiles.getTemplate("wizardHTML.html"), // NOI18N
            basicTokens
        )); 
        cmf.add(cmf.createFileWithSubstitutions(
            getDefaultPackagePath(lowerCasedPrefix, true) + ".fmk", // NOI18N
            CreatedModifiedFiles.getTemplate("wizardHTML.fmk"), // NOI18N
            basicTokens
        )); 
        String instanceFullPath = category + '/' + lowerCasedPrefix;

        basicTokens.put("TR_folder", category.replaceFirst("^Templates/", ""));
        basicTokens.put("TR_displayName", displayName);
        basicTokens.put("TR_description", lowerCasedPrefix + "Descr.html");
        basicTokens.put("TR_page", lowerCasedPrefix + ".html");
        basicTokens.put("TR_content", lowerCasedPrefix + ".fmk");

        // Copy wizard icon
        FileObject origIcon = origIconPath != null ? FileUtil.toFileObject(origIconPath) : null;
        if (origIcon != null) {
            String relToSrcDir = addCreateIconOperation(cmf, origIcon);
            basicTokens.put("TR_iconBase", relToSrcDir);
        }
        FileObject template = CreatedModifiedFiles.getTemplate("wizardHTML.java"); // NOI18N
        String path = getDefaultPackagePath(prefix + ".java", false); // NOI18N
        cmf.add(cmf.createFileWithSubstitutions(path, template, basicTokens));
    }
    
    private void reset() {
        cmf = null;
    }
    
    void setBranching(boolean branching) {
        this.branching = branching;
    }
    
    boolean isBranching() {
        return branching;
    }
    
    void setFileTemplateType(TemplateType fileTemplateType) {
        this.fileTemplateType = fileTemplateType;
    }
    
    TemplateType getFileTemplateType() {
        return fileTemplateType;
    }
    
    void setNumberOfSteps(int nOfSteps) {
        this.nOfSteps = nOfSteps;
    }
    
    void setClassNamePrefix(String prefix) {
        reset();
        this.prefix = prefix;
    }
    
    void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    void setCategory(String category) {
        this.category = category;
    }
    
    void setIcon(File origIconPath) {
        reset();
        this.origIconPath = origIconPath;
    }
    
    public @Override void setPackageName(String packageName) {
        super.setPackageName(packageName);
        reset();
    }

    enum TemplateType {
        HTML4J, HTML, CUSTOM, FILE
    }
}
