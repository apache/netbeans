/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.windows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import org.openide.awt.ActionID;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Description;
import org.openide.windows.TopComponent.Registration;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@ServiceProvider(service=Processor.class)
public final class TopComponentProcessor extends LayerGeneratingProcessor {
    public TopComponentProcessor() {
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hash = new HashSet<String>();
        hash.add(TopComponent.Registration.class.getCanonicalName());
        hash.add(TopComponent.OpenActionRegistration.class.getCanonicalName());
        hash.add(TopComponent.Description.class.getCanonicalName());
        return hash;
    }
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(TopComponent.Registration.class)) {
            TopComponent.Registration reg = e.getAnnotation(TopComponent.Registration.class);
            if (reg == null) {
                continue;
            }
            
            Description info = findInfo(e);
            if (info == null) {
                throw new LayerGenerationException("Cannot find TopComponent.Description for this element", e, processingEnv, reg);
            }
            String id = info.preferredID();
            checkValidId(id, e, processingEnv, info);

            String rootFolder;
            String[] roles = reg.roles();
            if (roles.length == 0) {
                rootFolder = "Windows2";
                generateSettingsAndWstcref(e, rootFolder, id, reg, info);
            } else {
                Set<String> uniqueRoles = new HashSet<String>();
                for (String role : roles) {
                    if (!uniqueRoles.add(role)) {
                        throw new LayerGenerationException("Duplicate role name found", e, processingEnv, reg);
                    }
                    if (role.isEmpty()) {
                        throw new LayerGenerationException("Unnamed role found", e, processingEnv, reg);
                    }
                    rootFolder = "Windows2/Roles/" + role;
                    generateSettingsAndWstcref(e, rootFolder, id, reg, info);
                }
            }
        }
        
        for (Element e : roundEnv.getElementsAnnotatedWith(TopComponent.OpenActionRegistration.class)) {
            TopComponent.OpenActionRegistration reg = e.getAnnotation(TopComponent.OpenActionRegistration.class);
            assert reg != null;
            Description info = findInfo(e);
            
            ActionID aid = e.getAnnotation(ActionID.class);
            if (aid != null) {
                File actionFile = layer(e).
                    file("Actions/" + aid.category() + "/" + aid.id().replace('.', '-') + ".instance").
                    methodvalue("instanceCreate", "org.openide.windows.TopComponent", "openAction");
                actionFile.instanceAttribute("component", TopComponent.class, reg, null);
                if (reg.preferredID().length() > 0) {
                    actionFile.stringvalue("preferredID", reg.preferredID());
                }
                actionFile.bundlevalue("displayName", reg.displayName(), reg, "displayName");
                if (info != null && info.iconBase().length() > 0) {
                    actionFile.stringvalue("iconBase", info.iconBase());
                }
                actionFile.write();
            }
        }
        return true;
    }

    private void generateSettingsAndWstcref(Element e, String rootFolder, String id, Registration reg, Description info) throws LayerGenerationException {
        File settingsFile = layer(e).
                file(rootFolder + "/Components/" + id + ".settings").
                contents(settingsFile(e));
        settingsFile.write();

        File modeFile = layer(e).
                file(rootFolder + "/Modes/" + reg.mode() + "/" + id + ".wstcref").
                position(reg.position()).
                contents(modeFile(info.preferredID(), reg.openAtStartup()));
        modeFile.write();
    }

    private Description findInfo(Element e) throws LayerGenerationException {
        Element type;
        switch (e.asType().getKind()) {
            case DECLARED: type = e; break;
            case EXECUTABLE: type = ((DeclaredType)((ExecutableType)e.asType()).getReturnType()).asElement(); break;
            default: throw new LayerGenerationException("" + e.asType().getKind(), e);    
        }
        TopComponent.Description info = type.getAnnotation(TopComponent.Description.class);
        return info;
    }

    private static String settingsFile(Element e) throws LayerGenerationException {
        String clazz, method;
        switch (e.getKind()) {
            case CLASS: clazz = e.toString(); method = null; break;
            case METHOD: clazz = e.getEnclosingElement().toString(); method = e.getSimpleName().toString(); break;
            default:
                throw new LayerGenerationException("Cannot work on given element", e);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE settings PUBLIC \"-//NetBeans//DTD Session settings 1.0//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\">\n");
        sb.append("<settings version=\"1.0\">\n");
        sb.append("  <instance class=\"").append(clazz).append("\"");
        if (method != null) {
            sb.append(" method=\"").append(method).append("\"");
        }
        sb.append("/>\n");
        sb.append("</settings>\n");
        return sb.toString();
    }
    
    private static String modeFile(String id, boolean openAtStart) 
    throws LayerGenerationException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE tc-ref PUBLIC \"-//NetBeans//DTD Top Component in Mode Properties 2.0//EN\" \"http://www.netbeans.org/dtds/tc-ref2_0.dtd\">\n");
        sb.append("<tc-ref version=\"2.0\">\n");
        sb.append("  <tc-id id=\"").append(id).append("\"/>\n");
        sb.append("  <state opened=\"").append(openAtStart).append("\"/>\n");
        sb.append("</tc-ref>\n");
        return sb.toString();
    }

    private static void checkValidId( String id, Element e, ProcessingEnvironment processingEnv, TopComponent.Description descr) throws LayerGenerationException {
        if( null == id )
            return;
        for( char c : id.toCharArray() ) {
            if( !(Character.isLetterOrDigit(c ) || c == '-' || c == '_')
                    || c > '\u007E' ) {
                throw new LayerGenerationException("The preferred id contains invalid character '" + c + "'", e, processingEnv, descr);
            }
        }
    }
}
