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
package org.jetbrains.kotlin.projectsextensions.maven.buildextender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.jetbrains.kotlin.log.KotlinLogger;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;

public class PomXmlModifier {

    private final Project project;
    private final String kotlinVersion = "1.0.4";
    private final String groupIdName = "org.jetbrains.kotlin";
    
    public PomXmlModifier(Project project) {
        this.project = project;
    }

    public void checkPom() {
        try {
            checkKotlinStdLibDependency();
            checkKotlinPlugin();
        } catch (DocumentException ex) {
            KotlinLogger.INSTANCE.logException("Cannot find pom.xml", ex);
        } catch (IOException ex) {
            KotlinLogger.INSTANCE.logException("", ex);
        }
    }
    
    private Element createDependenciesElement(Element root) {
        QName qname = new QName("dependencies", root.getQName().getNamespace());
        DefaultElement dependenciesElement = new DefaultElement(qname);
        
        root.content().add(dependenciesElement);
        
        return root.element("dependencies");
    }
    
    private Element createBuildElement(Element root) {
        QName qname = new QName("build", root.getQName().getNamespace());
        DefaultElement buildElement = new DefaultElement(qname);
        
        root.content().add(buildElement);
        
        return root.element("build");
    }
    
    private Element createPluginsElement(Element root) {
        QName qname = new QName("plugins", root.getQName().getNamespace());
        DefaultElement pluginsElement = new DefaultElement(qname);
        
        root.content().add(pluginsElement);
        
        return root.element("plugins");
    }
    
    private void createStdlibDependency(Element dependencies) {
        QName dependencyQname = new QName("dependency", dependencies.getQName().getNamespace());
        DefaultElement dependency = new DefaultElement(dependencyQname);
        
        QName groupIdQname = new QName("groupId", dependencies.getQName().getNamespace());
        DefaultElement groupId = new DefaultElement(groupIdQname);
        groupId.addText(groupIdName);
        
        QName artifactIdQname = new QName("artifactId", dependencies.getQName().getNamespace());
        DefaultElement artifactId = new DefaultElement(artifactIdQname);
        artifactId.addText("kotlin-stdlib");
        
        QName versionQname = new QName("version", dependencies.getQName().getNamespace());
        DefaultElement version = new DefaultElement(versionQname);
        version.addText(kotlinVersion);
        
        dependency.add(groupId);
        dependency.add(artifactId);
        dependency.add(version);
        
        dependencies.content().add(dependency);
    }
    
    private void addExecution(Element executions, String id, String phase, String goal) {
        QName executionQname1 = new QName("execution", executions.getQName().getNamespace());
        DefaultElement execution = new DefaultElement(executionQname1);
        
        QName idQname1 = new QName("id", executions.getQName().getNamespace());
        DefaultElement id1 = new DefaultElement(idQname1);
        id1.addText(id);
        QName phaseQname1 = new QName("phase", executions.getQName().getNamespace());
        DefaultElement phase1 = new DefaultElement(phaseQname1);
        phase1.addText(phase);
        QName goalsQname1 = new QName("goals", executions.getQName().getNamespace());
        DefaultElement goals1 = new DefaultElement(goalsQname1);
        QName goalQname1 = new QName("goal", executions.getQName().getNamespace());
        DefaultElement goal1 = new DefaultElement(goalQname1);
        goal1.addText(goal);
        
        goals1.add(goal1);
        
        execution.add(id1);
        execution.add(phase1);
        execution.add(goals1);
        
        executions.add(execution);
    }
    
    private void addExecutions(Element plugin) {
        QName executionsQname = new QName("executions", plugin.getQName().getNamespace());
        DefaultElement executions = new DefaultElement(executionsQname);
        
        addExecution(executions, "compile", "process-sources", "compile");
        addExecution(executions, "test-compile", "process-test-sources", "test-compile");
        
        plugin.add(executions);
    }
    
    private void createPluginElement(Element plugins) {
        QName pluginQname = new QName("plugin", plugins.getQName().getNamespace());
        DefaultElement plugin = new DefaultElement(pluginQname);
        
        QName groupIdQname = new QName("groupId", plugins.getQName().getNamespace());
        DefaultElement groupId = new DefaultElement(groupIdQname);
        groupId.addText(groupIdName);
        
        QName artifactIdQname = new QName("artifactId", plugins.getQName().getNamespace());
        DefaultElement artifactId = new DefaultElement(artifactIdQname);
        artifactId.addText("kotlin-maven-plugin");
        
        QName versionQname = new QName("version", plugins.getQName().getNamespace());
        DefaultElement version = new DefaultElement(versionQname);
        version.addText(kotlinVersion);
        
        plugin.add(groupId);
        plugin.add(artifactId);
        plugin.add(version);
        
        addExecutions(plugin);
        
        plugins.add(plugin);
    }
    
    private File getPOMFile(Project proj) {
        Class clazz = proj.getClass();
        try {
            Method method = clazz.getMethod("getPOMFile");
            return (File) method.invoke(proj);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }
    
    private void checkKotlinStdLibDependency() throws DocumentException, IOException {
        File pom = getPOMFile(project);
        if (pom == null) {
            return;
        }
        SAXReader reader = new SAXReader();
        Document pomDocument = reader.read(pom);
        
        Element root = pomDocument.getRootElement();
        Element dependencies = root.element("dependencies");
        if (dependencies == null) {
            dependencies = createDependenciesElement(root);
        }
        
        Element stdlibDependency = null;
        for (Object el : dependencies.elements("dependency")) {
            Element dep = (Element) el;
            Element groupId = dep.element("groupId");
            Element artifactId = dep.element("artifactId");
            if (groupId == null || artifactId == null) {
                continue;
            }
            if (groupId.getText().equals(groupIdName) && 
                    artifactId.getText().equals("kotlin-stdlib")) {
                stdlibDependency = dep;
                break;
            }
        }
        
        if (stdlibDependency == null) {
            createStdlibDependency(dependencies);
        }
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(pom);
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(pomDocument);
        out.close();
    }
    
    public boolean hasKotlinPluginInPom() throws DocumentException {
        File pom = getPOMFile(project);
        if (pom == null) {
            return false;
        }
        SAXReader reader = new SAXReader();
        Document pomDocument = reader.read(pom);
        
        Element root = pomDocument.getRootElement();
        Element build = root.element("build");
        if (build == null) {
            return false;
        }
        
        Element plugins = build.element("plugins");
        if (plugins == null) {
            return false;
        }
        
        Element plugin = null;
        for (Object el : plugins.elements("plugin")) {
            Element plug = (Element) el;
            Element groupId = plug.element("groupId");
            Element artifactId = plug.element("artifactId");
            if (groupId == null || artifactId == null) {
                continue;
            }
            if (groupId.getText().equals(groupIdName) && 
                    artifactId.getText().equals("kotlin-maven-plugin")) {
                plugin = plug;
                break;
            }
        }
        
        return plugin != null;
    }

    private void checkKotlinPlugin() throws DocumentException, IOException {
        File pom = getPOMFile(project);
        if (pom == null) {
            return;
        }
        SAXReader reader = new SAXReader();
        Document pomDocument = reader.read(pom);
        
        Element root = pomDocument.getRootElement();
        Element build = root.element("build");
        if (build == null) {
            build = createBuildElement(root);
        }
        
        Element plugins = build.element("plugins");
        if (plugins == null) {
            plugins = createPluginsElement(build);
        }
        
        Element plugin = null;
        for (Object el : plugins.elements("plugin")) {
            Element plug = (Element) el;
            Element groupId = plug.element("groupId");
            Element artifactId = plug.element("artifactId");
            if (groupId == null || artifactId == null) {
                continue;
            }
            if (groupId.getText().equals(groupIdName) && 
                    artifactId.getText().equals("kotlin-maven-plugin")) {
                plugin = plug;
                break;
            }
        }
        
        if (plugin != null) {
            return;
        }
        
        createPluginElement(plugins);
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        FileWriter out = new FileWriter(pom);
        XMLWriter writer;
        writer = new XMLWriter(out, format);
        writer.write(pomDocument);
        out.close();
    }
    
}
