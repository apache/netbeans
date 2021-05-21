/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.openide.windows;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import org.openide.awt.ActionID;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Description;
import org.openide.windows.TopComponent.Registration;

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
