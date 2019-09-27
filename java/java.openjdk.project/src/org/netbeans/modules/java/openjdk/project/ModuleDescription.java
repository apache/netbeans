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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author lahvac
 */
public class ModuleDescription {

    public final String name;
    public final List<Dependency> depend;
    public final Map<String, List<String>> exports;

    public ModuleDescription(String name, List<Dependency> depend, Map<String, List<String>> exports) {
        this.name = name;
        this.depend = depend;
        this.exports = exports;
    }

    @Override
    public String toString() {
        return "ModuleDescription{" + "name=" + name + ", depend=" + depend + ", exports=" + exports + '}';
    }

    private static final Map<URI, ModuleRepository> jdkRoot2Repository = new HashMap<>();

    public static ModuleRepository getModules(FileObject project) throws Exception {
        Pair<FileObject, Pair<Boolean, Boolean>> jdkRootAndType = findJDKRoot(project);

        if (jdkRootAndType == null)
            return null;

        FileObject jdkRoot = jdkRootAndType.first();

        ModuleRepository repository;
        
        synchronized (ModuleDescription.class) {
            repository = jdkRoot2Repository.get(jdkRoot.toURI());
        }

        if (repository != null)
            return repository;

        boolean hasModuleInfos;
        List<ModuleDescription> moduleDescriptions;
        FileObject modulesXML = BuildUtils.getFileObject(jdkRoot, "modules.xml");

        if (modulesXML != null) {
            moduleDescriptions = new ArrayList<>();
            readModulesXml(modulesXML, moduleDescriptions);
            readModulesXml(BuildUtils.getFileObject(jdkRoot, "closed/modules.xml"), moduleDescriptions);
            hasModuleInfos = false;
        } else {
            moduleDescriptions = readModuleInfos(jdkRoot);
            hasModuleInfos = true;
        }

        if (moduleDescriptions.isEmpty())
            return null;
        
        synchronized (ModuleDescription.class) {
            jdkRoot2Repository.put(jdkRoot.toURI(), repository = new ModuleRepository(jdkRoot, hasModuleInfos, jdkRootAndType.second().first(), jdkRootAndType.second().second(), moduleDescriptions));
        }

        return repository;
    }

    public static synchronized ModuleRepository getModuleRepository(URI forURI) {
        return jdkRoot2Repository.get(forURI);
    }

    private static Pair<FileObject, Pair<Boolean, Boolean>> findJDKRoot(FileObject projectDirectory) {
        if (BuildUtils.getFileObject(projectDirectory, "../../../open/src/java.base/share/classes/module-info.java") != null && 
            BuildUtils.getFileObject(projectDirectory, "../../../open/src/java.base/share/classes/module-info.java") != null &&
            BuildUtils.getFileObject(projectDirectory, "../../../open/src/java.compiler/share/classes/module-info.java") != null)
            return Pair.of(BuildUtils.getFileObject(projectDirectory, "../../.."), Pair.of(true, true));
        if (BuildUtils.getFileObject(projectDirectory, "../../src/java.base/share/classes/module-info.java") != null &&
            BuildUtils.getFileObject(projectDirectory, "../../src/java.compiler/share/classes/module-info.java") != null)
            return Pair.of(BuildUtils.getFileObject(projectDirectory, "../.."), Pair.of(true, false));
        if (BuildUtils.getFileObject(projectDirectory, "../../../modules.xml") != null ||
            (BuildUtils.getFileObject(projectDirectory, "../../../jdk/src/java.base/share/classes/module-info.java") != null && BuildUtils.getFileObject(projectDirectory, "../../../langtools/src/java.compiler/share/classes/module-info.java") != null))
            return Pair.of(BuildUtils.getFileObject(projectDirectory, "../../.."), Pair.of(false, false));
        if (BuildUtils.getFileObject(projectDirectory, "../../../../modules.xml") != null ||
            (BuildUtils.getFileObject(projectDirectory, "../../../../jdk/src/java.base/share/classes/module-info.java") != null && BuildUtils.getFileObject(projectDirectory, "../../../langtools/src/java.compiler/share/classes/module-info.java") != null))
            return Pair.of(BuildUtils.getFileObject(projectDirectory, "../../../.."), Pair.of(false, false));

        return null;
    }

    private static void readModulesXml(FileObject modulesXML, List<ModuleDescription> moduleDescriptions) throws SAXException, IOException {
        if (modulesXML == null)
            return ;

        try (InputStream in = modulesXML.getInputStream()) {
            Document doc = XMLUtil.parse(new InputSource(in), false, true, null, null);
            NodeList modules = doc.getDocumentElement().getElementsByTagName("module");

            for (int i = 0; i < modules.getLength(); i++) {
                moduleDescriptions.add(parseModule((Element) modules.item(i)));
            }
        }
    }

    private static ModuleDescription parseModule(Element moduleEl) {
        NodeList children = moduleEl.getChildNodes();
        String name = null;
        List<Dependency> depend = new ArrayList<>();
        Map<String, List<String>> exports = new HashMap<>();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element childEl = (Element) child;

            switch (childEl.getLocalName()) {
                case "name":
                    name = childEl.getTextContent();
                    break;
                case "depend":
                    depend.add(new Dependency(childEl.getTextContent(), "true".equals(childEl.getAttribute("re-exports")), false));
                    break;
                case "export":
                    String exported = null;
                    List<String> exportedTo = null;
                    NodeList exportChildren = childEl.getChildNodes();

                    for (int j = 0; j < exportChildren.getLength(); j++) {
                        Node exportChild = exportChildren.item(j);

                        if (exportChild.getNodeType() != Node.ELEMENT_NODE) continue;
                        
                        switch (exportChild.getLocalName()) {
                            case "name":
                                exported = exportChild.getTextContent();
                                break;
                            case "to":
                                if (exportedTo == null) exportedTo = new ArrayList<>();
                                exportedTo.add(exportChild.getTextContent());
                                break;
                        }
                    }

                    exports.put(exported, exportedTo != null ? Collections.unmodifiableList(exportedTo) : null);
                    break;
            }
        }

        return new ModuleDescription(name, Collections.unmodifiableList(depend), Collections.unmodifiableMap(exports));
    }

    private static List<ModuleDescription> readModuleInfos(FileObject jdkRoot) throws Exception {
        List<ModuleDescription> result = new ArrayList<>();
        List<FileObject> todo = new LinkedList<>();

        todo.add(jdkRoot);

        while (!todo.isEmpty()) {
            FileObject current = todo.remove(0);

            if (".hg".equals(current.getNameExt()))
                continue; //ignore mercurial repository data

            if ("build".equals(current.getNameExt()) && jdkRoot.equals(current.getParent()))
                continue; //ignore build dir

            FileObject moduleInfo = getModuleInfo(current);

            if (moduleInfo != null) {
                ModuleDescription module = parseModuleInfo(moduleInfo);

                if (module != null) {
                    result.add(module);
                }

                FileObject srcDir = current.getParent();
                if (srcDir != null && srcDir.getNameExt().equals("src")) {
                    //do not look inside recognized modules:
                    continue;
                }
            }

            if (BuildUtils.getFileObject(current, "TEST.ROOT") != null) {
                continue; //do not look inside test folders
            }

            todo.addAll(Arrays.asList(current.getChildren()));
        }

        return result;
    }

    private static FileObject getModuleInfo(FileObject project) {
        for (FileObject c : project.getChildren()) {
            FileObject moduleInfo = BuildUtils.getFileObject(c, "classes/module-info.java");

            if (moduleInfo != null)
                return moduleInfo;
        }
        return null;
    }

    private static final Pattern MODULE = Pattern.compile("module\\s+(?<modulename>([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+)");
    private static final Pattern REQUIRES = Pattern.compile("requires\\s+(?<flags>(transitive\\s+|public\\s+|static\\s+)*)(?<dependency>([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+)\\s*;");
    private static final Pattern EXPORTS = Pattern.compile("exports\\s+([^;]*?\\\\s+)?(?<package>([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+)(\\s+to\\s+(?<to>([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+(\\s*,\\s*([a-zA-Z0-9]+\\.)*[a-zA-Z0-9]+)*))?\\s*;");
    private static ModuleDescription parseModuleInfo(FileObject f) throws IOException {
        try (Reader r = new InputStreamReader(f.getInputStream())) {
            ModuleDescription desc = parseModuleInfo(r);

            if (desc == null || !desc.name.equals(BuildUtils.getFileObject(f, "../../..").getNameExt()))
                return null;

            return desc;
        }
    }

    static ModuleDescription parseModuleInfo(Reader r) throws IOException {
        TokenHierarchy<Reader> th = TokenHierarchy.create(r,
                                                          JavaTokenId.language(),
                                                          EnumSet.of(JavaTokenId.BLOCK_COMMENT, JavaTokenId.ERROR,
                                                                     JavaTokenId.INVALID_COMMENT_END, JavaTokenId.JAVADOC_COMMENT,
                                                                     JavaTokenId.LINE_COMMENT, JavaTokenId.STRING_LITERAL),
                                                          new InputAttributes());
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());

        ts.moveStart();

        StringBuilder content = new StringBuilder();

        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                content.append(' ');
            } else {
                content.append(ts.token().text());
            }
        }

        Matcher moduleMatcher = MODULE.matcher(content);

        if (!moduleMatcher.find())
            return null;

        String moduleName = moduleMatcher.group("modulename");

        List<Dependency> depends = new ArrayList<>();
        boolean hasJavaBaseDependency = false;
        Matcher requiresMatcher = REQUIRES.matcher(content);

        while (requiresMatcher.find()) {
            String depName = requiresMatcher.group("dependency");
            boolean isPublic = false;
            boolean isStatic = false;
            String flags = requiresMatcher.group("flags");

            if (flags != null) {
                isPublic = flags.contains("transitive") || flags.contains("public");
                isStatic = flags.contains("static");
            }

            depends.add(new Dependency(depName, isPublic, isStatic));

            hasJavaBaseDependency |= depName.equals("java.base");
        }

        if (!hasJavaBaseDependency && !"java.base".equals(moduleName))
            depends.listIterator().add(new Dependency("java.base", false, false));

        Map<String, List<String>> exports = new LinkedHashMap<>();
        Matcher exportsMatcher = EXPORTS.matcher(content);

        while (exportsMatcher.find()) {
            String pack = exportsMatcher.group("package");
            String to   = exportsMatcher.group("to");

            List<String> toModule = to != null ? Arrays.asList(to.split("\\s*,\\s*")) : null;

            exports.put(pack, toModule);
        }

        return new ModuleDescription(moduleName, depends, exports);
    }

    public static class ModuleRepository {
        private final Set<Project> openProjects = new HashSet<>();
        private final FileObject root;
        private final boolean hasModuleInfos;
        private final boolean consolidatedRepository;
        private final boolean explicitOpen;
        public final List<ModuleDescription> modules;

        private ModuleRepository(FileObject root, boolean hasModuleInfos, boolean consolidatedRepository, boolean explicitOpen, List<ModuleDescription> modules) {
            this.root = root;
            this.hasModuleInfos = hasModuleInfos;
            this.consolidatedRepository = consolidatedRepository;
            this.explicitOpen = explicitOpen;
            this.modules = modules;
        }

        public FileObject getJDKRoot() {
            return root;
        }

        public ModuleDescription findModule(String moduleName) {
            for (ModuleDescription md : modules) {
                if (md.name.equals(moduleName))
                    return md;
            }

            return null;
        }

        public FileObject findModuleRoot(String moduleName) {
            if (consolidatedRepository) {
                FileObject module;

                if (explicitOpen) {
                    module = BuildUtils.getFileObject(root, "open/src/" + moduleName);
                    if (module == null) {
                        module = BuildUtils.getFileObject(root, "closed/src/" + moduleName);
                    }
                } else {
                    module = BuildUtils.getFileObject(root, "src/" + moduleName);
                }

                if (module != null && module.isFolder())
                    return module;
            } else {
                for (FileObject repo : root.getChildren()) {
                    FileObject module = BuildUtils.getFileObject(repo, "src/" + moduleName);

                    if (module == null)
                        module = BuildUtils.getFileObject(repo, "src/closed/" + moduleName);

                    if (module != null && module.isFolder() && validate(repo, module))
                        return module;
                }
            }
            
            return null;
        }

        private boolean validate(FileObject repo, FileObject project) {
            if (hasModuleInfos)
                return getModuleInfo(project) != null;
            switch (project.getNameExt()) {
                case "java.base":
                    return repo.getName().equals("jdk");
                case "java.corba":
                    return repo.getName().equals("corba");
                case "jdk.compiler":
                    return repo.getName().equals("langtools");
                case "jdk.dev":
                    return repo.getName().equals("langtools");
            }
            return true;
        }

        public String moduleTests(String moduleName) {
            String open = explicitOpen ? "open/" : "";
            //TODO? for now, tests are assigned to java.base, java.compiler and java.xml, depending on the location of the tests:
            switch (moduleName) {
                case "java.base":
                    return consolidatedRepository ? "${jdkRoot}/" + open + "test/jdk/" : "${jdkRoot}/jdk/test/";
                case "java.compiler":
                    return consolidatedRepository ? "${jdkRoot}/test/" + open + "langtools/" : "${jdkRoot}/langtools/test/";
                case "java.xml":
                    return consolidatedRepository ? "${jdkRoot}/test/" + open + "jaxp/" : "${jdkRoot}/jaxp/test/";
                case "jdk.scripting.nashorn":
                    return consolidatedRepository ? "${jdkRoot}/test/" + open + "nashorn/" : "${jdkRoot}/nashorn/test/";
            }
            return null;
        }

        public Collection<String> allDependencies(ModuleDescription module) {
            Set<String> result = new LinkedHashSet<>();

            allDependencies(module, result, false);

            return result;
        }

        private void allDependencies(ModuleDescription module, Set<String> result, boolean transitiveOnly) {
            for (Dependency dep : module.depend) {
                if (transitiveOnly && !dep.requiresPublic)
                    continue;

                ModuleDescription md = findModule(dep.moduleName);

                if (md == null) {
                    //XXX
                } else {
                    allDependencies(md, result, true);
                }

                result.add(dep.moduleName);
            }
        }

        public boolean isConsolidatedRepo() {
            return consolidatedRepository;
        }

        public synchronized void projectOpened(Project opened) {
            this.openProjects.add(opened);
        }

        public synchronized void projectClosed(Project closed) {
            this.openProjects.remove(closed);
        }

        public synchronized boolean isAnyProjectOpened() {
            return !this.openProjects.isEmpty();
        }
    }

    public static final class Dependency {
        public final String moduleName;
        public final boolean requiresPublic;
        public final boolean requiresStatic;

        public Dependency(String moduleName, boolean requiresPublic, boolean requiresStatic) {
            this.moduleName = moduleName;
            this.requiresPublic = requiresPublic;
            this.requiresStatic = requiresStatic;
        }

    }
}
