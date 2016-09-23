/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.projectsextensions.j2se.buildextender;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinBuildExtender {
    
    private Project project = null;
    
    public KotlinBuildExtender(Project project) {
        if (!(project.getClass().getName().
                equals("org.netbeans.modules.java.j2seproject.J2SEProject"))) {
            return;
        }
        this.project = project;
    }

    public void addKotlinTasksToScript(Project project) {
        FileObject buildImpl = getBuildImplXml(project);
        if (buildImpl == null) {
            return;
        }
        try {
            addKotlinAnt(buildImpl);
            addKotlinLibProperty(buildImpl);
            insertWithKotlin(buildImpl);
        } catch (DocumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private FileObject getBuildImplXml(Project project) {
        FileObject projDir=project.getProjectDirectory();
        FileObject buildImpl = projDir.getFileObject("nbproject").getFileObject("build-impl.xml");
        
        return buildImpl;
    }
    
    private void addKotlinLibProperty(FileObject buildImpl) throws DocumentException, UnsupportedEncodingException, IOException{
        SAXReader reader = new SAXReader();
        Document document = reader.read(buildImpl.toURL());
        
        List<Element> elements = document.getRootElement().elements("property");
        
        for (Element el : elements) {
            if (el.attribute("name").getValue().equals("kotlin.lib")) {
                return;
            }
        }
        
        DefaultElement prop = new DefaultElement("property");
        prop.addAttribute("name", "kotlin.lib");
        prop.addAttribute("value", ProjectUtils.KT_HOME + "lib");
        
        List content = document.getRootElement().content();
        if (content != null ) {
            content.add(0, prop);
        }
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(buildImpl.getPath());
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(document);
        out.close();
    }
    
    private void addKotlinAnt(FileObject buildImpl) throws DocumentException, UnsupportedEncodingException, IOException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(buildImpl.toURL());
        
        List<Element> elements = document.getRootElement().elements("typedef");
        for (Element el : elements) {
            if (el.attribute("classpath").getValue().equals("${kotlin.lib}/kotlin-ant.jar")) {
                return;
            }
        }
        
        DefaultElement typedef = new DefaultElement("typedef");
        typedef.addAttribute("classpath", "${kotlin.lib}/kotlin-ant.jar");
        typedef.addAttribute("resource", "org/jetbrains/kotlin/ant/antlib.xml");
        
        List content = document.getRootElement().content();
        if (content != null ) {
            content.add(0, typedef);
        }
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(buildImpl.getPath());
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(document);
        out.close();
    }
    
    private void insertWithKotlin(FileObject buildImpl) throws DocumentException, IOException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(buildImpl.toURL());
        Element target = null;
        
        List<Element> elements = document.getRootElement().elements("target");
        for (Element el : elements) {
            if (el.attribute("name").getValue().equals("-init-macrodef-javac-with-processors")) {
                target = el;
            }
        }
        
        if (target == null) {
            return;
        }
        
        Element macrodef = target.element("macrodef");
        if (macrodef == null) {
            return;
        }
        
        Element sequential = macrodef.element("sequential");
        if (sequential == null) {
            return;
        }
        
        Element javac = sequential.element("javac");
        if (javac == null) {
            return;
        }
        
        if (javac.element("withKotlin") != null){
            return;
        }
        
        
        
        DefaultElement withKotlin = new DefaultElement("withKotlin");
        
        List content = javac.content();
        if (content != null ) {
            content.add(3, withKotlin);
        }
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(buildImpl.getPath());
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(document);
        out.close();      
    }
    
}
