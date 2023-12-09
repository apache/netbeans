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

package org.netbeans.modules.maven.grammar;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar;
import org.netbeans.modules.maven.grammar.spi.GrammarExtensionProvider;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.PluginIndexManager;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * xml completion grammar based on xsd, additionally allowing more to be added.
 * @author Milos Kleint
 */
public class MavenProjectGrammar extends AbstractSchemaBasedGrammar {
    
    private static final Logger LOG = Logger.getLogger(MavenProjectGrammar.class.getName());
    private static final String[] SCOPES = new String[] {
        "compile", //NOI18N
        "test", //NOI18N
        "runtime", //NOI18N
        "provided", //NOI18N
        "system" //NOI18N
    };

    private final Project owner;


    MavenProjectGrammar(GrammarEnvironment env, Project owner) {
        super(env);
        this.owner = owner;
    }
    
    @Override
    protected InputStream getSchemaStream() {
        return getClass().getResourceAsStream("/org/netbeans/modules/maven/grammar/maven-4.0.0.xsd"); //NOI18N
    }
    
    private List<GrammarResult> hardwiredProperty(HintContext hintCtx, String name, String htmlDesc) {
        if (name.startsWith(hintCtx.getCurrentPrefix())) {
            MyElement el = new MyElement(name);
            el.setDescription(htmlDesc);
            return Collections.singletonList((GrammarResult)el);
        }        
        return Collections.emptyList();
    }
    
    @Override
    protected List<GrammarResult> getDynamicCompletion(String path, HintContext hintCtx, org.jdom2.Element parent) {
        List<GrammarResult> result = new ArrayList<>();
        if (path.endsWith("plugins/plugin/configuration") || //NOI18N
            path.endsWith("plugins/plugin/executions/execution/configuration")) { //NOI18N
            // assuming we have the configuration node as parent..
            // does not need to be true for complex stuff
            Node previous = path.indexOf("execution") > 0 //NOI18N
                ? hintCtx.getParentNode().getParentNode().getParentNode().getPreviousSibling()
                : hintCtx.getParentNode().getPreviousSibling();
            MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
            ArtifactInfoHolder info = findPluginInfo(previous, embedder, true);
            List<GrammarResult> res = collectPluginParams(info, hintCtx);
            if (res == null) { //let the local processing geta changce
                               //once the index failed.
                Document pluginDoc = loadDocument(info, embedder);
                if (pluginDoc != null) {
                    res = collectPluginParams(pluginDoc, hintCtx);
                }
            } 
            if (res != null) {
                result.addAll(res);
            }
        }
        
        if (path.endsWith("project/properties") || path.endsWith("profile/properties")) {
            result.addAll(hardwiredProperty(hintCtx, Constants.HINT_DISPLAY_NAME, "<html><h4>netbeans.hint.displayName</h4><p>A NetBeans specific property, only applicable within the IDE.</p><br/>Used by NetBeans to customize the display of the project in question.</html>"));
            result.addAll(hardwiredProperty(hintCtx, Constants.HINT_LICENSE, "<html><h4>netbeans.hint.license</h4><p>A NetBeans specific property, only applicable within the IDE.</p><br/>Used by NetBeans to select a license header template from the IDE's default set.</html>"));
            result.addAll(hardwiredProperty(hintCtx, Constants.HINT_LICENSE_PATH, "<html><h4>netbeans.hint.licensePath</h4><p>A NetBeans specific property, only applicable within the IDE.</p><br/>Used by NetBeans to find license header template from project's space in the filesystem. <br/>Value is an absolute or relative path to the template file.</html>"));
            result.addAll(hardwiredProperty(hintCtx, Constants.HINT_JDK_PLATFORM, "<html><h4>netbeans.hint.jdkPlatform</h4><p>A NetBeans specific property, only applicable within the IDE.</p><br/>Used by NetBeans to determine which JDK platform defined in Tools/Java Platforms should be used to run the Maven builds.</html>"));
            result.addAll(hardwiredProperty(hintCtx, Constants.HINT_COMPILE_ON_SAVE, "<html><h4>netbeans.compile.on.save</h4><p>A NetBeans specific property, only applicable within the IDE.</p><br/>Used by NetBeans to determine if Compile on Save feature should be enabled for the project or not.<br/>Allowed values are: true/false</html>"));
            result.addAll(hardwiredProperty(hintCtx, "netbeans.checkstyle.format", "<html><h4>netbeans.checkstyle.format</h4><p>A NetBeans specific property, only applicable within the IDE.</p><br/>Allowed values are: true/false</html>"));
        }

        GrammarExtensionProvider extProvider = Lookup.getDefault().lookup(GrammarExtensionProvider.class);
        if (extProvider != null) {
            result.addAll(extProvider.getDynamicCompletion(path, hintCtx, parent));
        }

        return result;
    }
    
    private ArtifactInfoHolder findArtifactInfo(Node previous) {
        ArtifactInfoHolder holder = new ArtifactInfoHolder();
        while (previous != null) {
            if (previous instanceof org.w3c.dom.Element) {
                org.w3c.dom.Element el = (org.w3c.dom.Element)previous;
                NodeList lst = el.getChildNodes();
                if (lst.getLength() > 0) {
                    if ("artifactId".equals(el.getNodeName())) { //NOI18N
                        holder.setArtifactId(getNodeValue(lst.item(0).getNodeValue(), owner.getLookup().lookup(NbMavenProject.class)));
                    }
                    if ("groupId".equals(el.getNodeName())) { //NOI18N
                        holder.setGroupId(getNodeValue(lst.item(0).getNodeValue(), owner.getLookup().lookup(NbMavenProject.class)));
                    }
                    if ("version".equals(el.getNodeName())) { //NOI18N
                        holder.setVersion(getNodeValue(lst.item(0).getNodeValue(), owner.getLookup().lookup(NbMavenProject.class)));
                    }
                }
            }
            previous = previous.getPreviousSibling();
        }
        return holder;
    }
    
    static String getNodeValue(String value, NbMavenProject prj) {
        StringBuilder sb = new StringBuilder();
        Properties props = prj.getMavenProject().getProperties();
            
        int idxLeft = value.indexOf("${"); // NOI18N
        if(idxLeft < 0) {
            return value;
        } else if(idxLeft > 0) {
            sb.append(value.substring(0, idxLeft));
        } 
        int idxRight = 0;
        while(idxLeft > -1) {
            idxRight = value.indexOf("}", idxLeft + 2); // NOI18N
            if(idxRight > -1) {
                String propName = value.substring(idxLeft + 2, idxRight);
                String prop = props.getProperty(propName);
                if(prop != null) {
                    sb.append(prop);
                } else {
                    // something is wrong; return the original value and hope for the best
                    return value;
                }
            } else {
                // something is wrong; return the original value and hope for the best
                return value;
            }
            idxLeft = value.indexOf("${", idxRight); // NOI18N
            if(idxRight < value.length()) {
                if(idxLeft > idxRight) {                    
                    sb.append(value.substring(idxRight + 1, idxLeft));
                } else {
                    sb.append(value.substring(idxRight + 1));
                }
            }             
            idxRight++;
        }        
        return sb.toString();
    }
    
    private ArtifactInfoHolder findPluginInfo(Node previous, MavenEmbedder embedder, boolean checkLocalRepo) {
        ArtifactInfoHolder holder = findArtifactInfo(previous);
        if (holder.getGroupId() == null) {
            holder.setGroupId("org.apache.maven.plugins"); //NOI18N
        }
        if (holder.getVersion() != null && holder.getVersion().contains("${")) {
            //cannot do anything with unresolved value, clear and hope for the best
            holder.setVersion(null);
        }
        if (holder.getVersion() == null && holder.getGroupId() != null && holder.getArtifactId() != null) {
            NbMavenProject prj = owner.getLookup().lookup(NbMavenProject.class);
            if (prj != null) {
                for (Plugin a : prj.getMavenProject().getBuildPlugins()) {
                    if (holder.getGroupId().equals(a.getGroupId()) && holder.getArtifactId().equals(a.getArtifactId())) {
                        holder.setVersion(a.getVersion());
                        break;
                    } 
                }
                if (holder.getVersion() == null) {
                    PluginManagement man = prj.getMavenProject().getPluginManagement();
                    if (man != null) {
                        for (Plugin p : man.getPlugins()) {
                            if (holder.getGroupId().equals(p.getGroupId()) && holder.getArtifactId().equals(p.getArtifactId())) {
                                holder.setVersion(p.getVersion());
                                break;
                            } 
                        }
                    }
                }
            }
        }
        if (checkLocalRepo && (holder.getVersion() == null || "LATEST".equals(holder.getVersion()) || "RELEASE".equals(holder.getVersion()))  //NOI18N
                && holder.getArtifactId() != null && holder.getGroupId() != null) { //NOI18N
            File lev1 = new File(embedder.getLocalRepositoryFile(), holder.getGroupId().replace('.', File.separatorChar));
            File dir = new File(lev1, holder.getArtifactId());
            File fil = new File(dir, "maven-metadata-local.xml"); //NOI18N
            if (fil.exists()) {
                MetadataXpp3Reader reader = new MetadataXpp3Reader();
                try {
                    Metadata data = reader.read(new InputStreamReader(new FileInputStream(fil)));
                    if (data.getVersion() != null) {
                        holder.setVersion(data.getVersion());
                    } else {
                        Versioning vers = data.getVersioning();
                        if (vers != null) {
                            if ("LATEST".equals(holder.getVersion())) { //NOI18N
                                holder.setVersion(vers.getLatest());
                            }
                            if ("RELEASE".equals(holder.getVersion())) { //NOI18N
                                holder.setVersion(vers.getRelease());
                            }
                        }
                    }
                } catch (FileNotFoundException ex) {
                    LOG.log(Level.FINER, "", ex);
                } catch (XmlPullParserException ex) {
                    LOG.log(Level.FINER, "", ex);
                } catch (IOException ex) {
                    LOG.log(Level.FINER, "", ex);
                }
            }
        }
        if (holder.getVersion() == null) {
            holder.setVersion("RELEASE"); //NOI18N
        }
        
        return holder;
    }
    
    private List<GrammarResult> collectPluginParams(Document pluginDoc, HintContext hintCtx) {
        Iterator<Content> it = pluginDoc.getRootElement().getDescendants();
        List<GrammarResult> toReturn = new ArrayList<>();
        Collection<String> params = new HashSet<>();

        while (it.hasNext()) {
            Content c = it.next();
            if (!(c instanceof Element)) {
                continue;
            }
            Element el = (Element) c;
            if (!("parameter".equals(el.getName()) && //NOI18N
                  el.getParentElement() != null && "parameters".equals(el.getParentElement().getName()) && //NOI18N
                  el.getParentElement().getParentElement() != null && "mojo".equals(el.getParentElement().getParentElement().getName()))) { //NOI18N
                continue;
            }
            String editable = el.getChildText("editable"); //NOI18N
            if ("true".equalsIgnoreCase(editable)) { //NOI18N
                String name = el.getChildText("name"); //NOI18N
                if (name.startsWith(hintCtx.getCurrentPrefix()) && !params.contains(name)) {
                    params.add(name);
                    toReturn.add(new MyElement(name));
                }
            }
        }
        return toReturn;
    }

    private List<GrammarResult> collectPluginParams(ArtifactInfoHolder info, HintContext hintCtx) {
        Set<PluginIndexManager.ParameterDetail> params;
        if (info.getGroupId() == null || info.getArtifactId() == null || info.getVersion() == null) {
            return null; //PluginIndexManager.getPluginParameters() asserts all coordinates are set
        }
        try {
            params = PluginIndexManager.getPluginParameters(info.getGroupId(), info.getArtifactId(), info.getVersion(), null);
            if (params == null) {
                return null;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        List<GrammarResult> toReturn = new ArrayList<>();

        for (PluginIndexManager.ParameterDetail plg : params) {
            if (plg.getName().startsWith(hintCtx.getCurrentPrefix())) {
                MyElement me = new MyElement(plg.getName());
                me.setDescription(plg.getHtmlDetails(true));
                toReturn.add(me);
            }
        }
        return toReturn;
    }


    @Override
    protected Enumeration<GrammarResult> getDynamicValueCompletion(String path, HintContext virtualTextCtx, Element el) {
        if (virtualTextCtx.getCurrentPrefix().length() > 0) {
            String prefix = virtualTextCtx.getCurrentPrefix();
            if (prefix.lastIndexOf("${") > prefix.lastIndexOf("}")) {
                int propStart = prefix.lastIndexOf("${") + 2;
                int delLen = -1;
                String propPrefix = prefix.substring(propStart);
                String val = virtualTextCtx.getNodeValue();
                int e = val.indexOf('}', propStart);
                if (e != -1) {
                    int s = val.indexOf("${", propStart);
                    if (s == -1 || e < s) {
                        delLen = e - (propStart + propPrefix.length()) + 1; // with the closing curly brace, caret will be placed after it
                    }
                }
                FileObject fo = getEnvironment().getFileObject();
                if (fo != null) {
                    List<String> set = new ArrayList<>();
                    set.add("basedir");
                    set.add("project.build.finalName");
                    set.add("project.version");
                    set.add("project.groupId");
                    Project p;
                    try {
                        p = ProjectManager.getDefault().findProject(fo.getParent());
                        if (p != null) {
                            NbMavenProject nbprj = p.getLookup().lookup(NbMavenProject.class);
                            if (nbprj != null) {
                                Properties props = nbprj.getMavenProject().getProperties();
                                if (props != null) {
                                    set.addAll(props.stringPropertyNames());
                                }
                            }
                        }
                    } catch (IOException ex) {
                        //Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        //Exceptions.printStackTrace(ex);
                    }
                    Collection<GrammarResult> elems = new ArrayList<>();
                    Collections.sort(set);
                    String suffix = virtualTextCtx.getNodeValue().substring(prefix.length());
                    int pplen = propPrefix.length();
                    for (String pr : set) {
                        if (pr.startsWith(propPrefix)) {
                            int l = delLen;
                            if (l == -1) {
                                // delete all following characters which match the property name
                                for (l = 0; (l < suffix.length()) && (l + pplen < pr.length()); l++) {
                                    if (suffix.charAt(l) != pr.charAt(pplen +l)) {
                                        break;
                                    }
                                }
                            }
                            elems.add(new ExpressionValueTextElement(pr, propPrefix, l));
                        }
                    }
                    return Collections.enumeration(elems);
                }
            }
        }
        if (path.endsWith("executions/execution/goals/goal")) { //NOI18N
            Node previous;
            // HACK.. if currentPrefix is zero length, the context is th element, otherwise it's the content inside
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                 previous = virtualTextCtx.getParentNode().getParentNode().getParentNode();
            } else {
                previous = virtualTextCtx.getParentNode().getParentNode().getParentNode().getParentNode();
            }
            previous = previous.getPreviousSibling();
            MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
            ArtifactInfoHolder info = findPluginInfo(previous, embedder, true);
            Enumeration<GrammarResult> res = collectGoals(info, virtualTextCtx);
            if (res == null) {
                Document pluginDoc = loadDocument(info, embedder);
                if (pluginDoc != null) {
                    return collectGoals(pluginDoc, virtualTextCtx);
                }
            } else {
                return res;
            }
        }
        if (path.endsWith("executions/execution/phase")) { //NOI18N
            return super.createTextValueList(Constants.DEFAULT_PHASES.toArray(new String[0]), virtualTextCtx);
        }
        if (path.endsWith("dependencies/dependency/version") || //NOI18N
            path.endsWith("plugins/plugin/version") || //NOI18N
            path.endsWith("extensions/extension/version") || //NOI18N
            path.endsWith("/project/parent/version")) { //NOI18N
            
            Node previous;
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                previous = virtualTextCtx.getPreviousSibling();
            } else {
                previous = virtualTextCtx.getParentNode().getPreviousSibling();
            }
            ArtifactInfoHolder hold = findPluginInfo(previous, null, false);
            if (hold.getGroupId() != null && hold.getArtifactId() != null) {
                Result<NBVersionInfo> result = RepositoryQueries.getVersionsResult(hold.getGroupId(), hold.getArtifactId(), RepositoryPreferences.getInstance().getRepositoryInfos());
                List<NBVersionInfo> verStrings = result.getResults();
                Collection<GrammarResult> elems = new ArrayList<>();
                Set<String> uniques = new HashSet<>();
                for (NBVersionInfo vers : verStrings) {
                    if (!uniques.contains(vers.getVersion()) && vers.getVersion().startsWith(virtualTextCtx.getCurrentPrefix())) {
                        uniques.add(vers.getVersion());
                        elems.add(new MyTextElement(vers.getVersion(), virtualTextCtx.getCurrentPrefix()));
                    }
                }
                if (result.isPartial()) {
                        elems.add(new PartialTextElement());
                    }
                return Collections.enumeration(elems);
            }
        }
        // version property completion
        String propXPath = "/project/properties/"; //NOI18N
        String profPropXPath = "/project/profiles/profile/properties/"; //NOI18N
        if (    (path.startsWith(propXPath) && path.indexOf("/", propXPath.length()) == -1)
            ||  (path.startsWith(profPropXPath) && path.indexOf("/", profPropXPath.length()) == -1)) { //NOI18N
          
            Node projectNode; // /project
            if (virtualTextCtx.getCurrentPrefix().isEmpty()) {
                projectNode = virtualTextCtx.getParentNode().getParentNode();
            } else {
                projectNode = virtualTextCtx.getParentNode().getParentNode().getParentNode();
            }
            String property;
            if (path.startsWith(profPropXPath)) {
                property = path.substring(profPropXPath.length());
                projectNode = projectNode.getParentNode().getParentNode();
            } else {
                property = path.substring(propXPath.length());
            }
            property = "${"+property+"}"; //NOI18N
            Set<ArtifactInfoHolder> usages = new HashSet<>();

            for (Node node : iterate(projectNode.getChildNodes())) {
                if ("dependencies".equals(node.getNodeName())) { //NOI18N
                    collectArtifacts("dependency", node, property, usages); //NOI18N
                } else if ("dependencyManagement".equals(node.getNodeName())) { //NOI18N
                    for (Node dmChild : iterate(node.getChildNodes())) {
                        if ("dependencies".equals(dmChild.getNodeName())) { //NOI18N
                            collectArtifacts("dependency", dmChild, property, usages); //NOI18N
                            break;
                        }
                    }
                } else if ("build".equals(node.getNodeName())) { //NOI18N
                    for (Node buildChild : iterate(node.getChildNodes())) {
                        if ("plugins".equals(buildChild.getNodeName())) { //NOI18N
                            collectArtifacts("plugin", buildChild, property, usages); //NOI18N
                        } else if ("pluginManagement".equals(buildChild.getNodeName())) { //NOI18N
                            for (Node pmChild : iterate(buildChild.getChildNodes())) {
                                if ("plugins".equals(pmChild.getNodeName())) { //NOI18N
                                    collectArtifacts("plugin", pmChild, property, usages); //NOI18N
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // intersection set; make sure all usages support the suggested versions
            Set<String> versions = new LinkedHashSet<>();
            boolean first = true;
            boolean partial = false;
            for (ArtifactInfoHolder artifact : usages) {
                Result<NBVersionInfo> versionInfo = RepositoryQueries.getVersionsResult(artifact.getGroupId(), artifact.getArtifactId(), null);
                partial |= versionInfo.isPartial();

                List<String> list = versionInfo.getResults().stream()
                                                            .map(NBVersionInfo::getVersion)
                                                            .collect(Collectors.toList());
                if (first) {
                    versions.addAll(list);
                    first = false;
                } else {
                    versions.retainAll(list);
                }
            }

            List<GrammarResult> completionItems = new ArrayList<>();
            for (String version : versions) {
                if (version.startsWith(virtualTextCtx.getCurrentPrefix())) {
                    completionItems.add(new MyTextElement(version, virtualTextCtx.getCurrentPrefix()));
                }
            }

            if (partial) {
                completionItems.add(new PartialTextElement());
            }

            return Collections.enumeration(completionItems);
        }
        
        if (path.endsWith("dependencies/dependency/groupId") || //NOI18N
            path.endsWith("extensions/extension/groupId")) {    //NOI18N
                Result<String> result = RepositoryQueries.getGroupsResult(RepositoryPreferences.getInstance().getRepositoryInfos());
                List<String> elems = result.getResults();
                ArrayList<GrammarResult> texts = new ArrayList<>();
                for (String elem : elems) {
                    if (elem.startsWith(virtualTextCtx.getCurrentPrefix())) {
                        texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
                    }
                }
                if (result.isPartial()) {
                        texts.add(new PartialTextElement());
                    }
                return Collections.enumeration(texts);
            
        }
        if (path.endsWith("plugins/plugin/groupId")) { //NOI18N
                Result<String> result = RepositoryQueries.filterPluginGroupIdsResult(virtualTextCtx.getCurrentPrefix(), RepositoryPreferences.getInstance().getRepositoryInfos());
//                elems.addAll(getRelevant(virtualTextCtx.getCurrentPrefix(), getCachedPluginGroupIds()));
                ArrayList<GrammarResult> texts = new ArrayList<>();
                for (String elem : result.getResults()) {
                    texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
                }
                if (result.isPartial()) {
                    texts.add(new PartialTextElement());
                }
                return Collections.enumeration(texts);
           
        }
        if (path.endsWith("dependencies/dependency/artifactId") || //NOI18N
            path.endsWith("extensions/extension/artifactId")) {    //NOI18N
            Node previous;
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                previous = virtualTextCtx.getPreviousSibling();
            } else {
                previous = virtualTextCtx.getParentNode().getPreviousSibling();
            }
            ArtifactInfoHolder hold = findArtifactInfo(previous);
            if (hold.getGroupId() != null) {
                    Result<String> result = RepositoryQueries.getArtifactsResult(hold.getGroupId(), RepositoryPreferences.getInstance().getRepositoryInfos());
                    List<String> elems = result.getResults();
                    ArrayList<GrammarResult> texts = new ArrayList<>();
                    String currprefix = virtualTextCtx.getCurrentPrefix();
                    for (String elem : elems) {
                        if (elem.startsWith(currprefix)) {
                            texts.add(new MyTextElement(elem, currprefix));
                        }
                    }
                    if (result.isPartial()) {
                            texts.add(new PartialTextElement());
                        }
                    return Collections.enumeration(texts);
               
            }
        }
        if (path.endsWith("dependencies/dependency/classifier")) { // #200852
            Node previous;
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                previous = virtualTextCtx.getPreviousSibling();
            } else {
                previous = virtualTextCtx.getParentNode().getPreviousSibling();
            }
            ArtifactInfoHolder hold = findArtifactInfo(previous);
            if (hold.getGroupId() != null && hold.getArtifactId() != null && hold.getVersion() != null) {
                Result<NBVersionInfo> result = RepositoryQueries.getRecordsResult(hold.getGroupId(), hold.getArtifactId(), hold.getVersion(), RepositoryPreferences.getInstance().getRepositoryInfos());

                List<NBVersionInfo> elems = result.getResults();
                List<GrammarResult> texts = new ArrayList<>();
                String currprefix = virtualTextCtx.getCurrentPrefix();
                Set<String> uniques = new HashSet<>();
                for (NBVersionInfo elem : elems) {
                    if (!uniques.contains(elem.getClassifier()) && elem.getClassifier() != null && elem.getClassifier().startsWith(currprefix)) {
                        texts.add(new MyTextElement(elem.getClassifier(), currprefix));
                        uniques.add(elem.getClassifier());
                    }
                }
                if (result.isPartial()) {
                        texts.add(new PartialTextElement());
                    }
                return Collections.enumeration(texts);
            }
        }
        if (path.endsWith("plugins/plugin/artifactId")) { //NOI18N
            //poor mans solution, just check local repository for possible versions..
            // in future would be nice to include remote repositories somehow..
            Node previous;
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                previous = virtualTextCtx.getPreviousSibling();
            } else {
                previous = virtualTextCtx.getParentNode().getPreviousSibling();
            }
            ArtifactInfoHolder hold = findArtifactInfo(previous);
            if (hold.getGroupId() != null) {
                Result<String> result = RepositoryQueries.filterPluginArtifactIdsResult(hold.getGroupId(), virtualTextCtx.getCurrentPrefix(), RepositoryPreferences.getInstance().getRepositoryInfos());
                ArrayList<GrammarResult> texts = new ArrayList<>();
                for (String elem : result.getResults()) {
                    texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
                }
                if (result.isPartial()) {
                    texts.add(new PartialTextElement());
                }
                return Collections.enumeration(texts);
            }
        }
        
        if (path.endsWith("dependencies/dependency/scope")) { //NOI18N
            if (path.contains("dependencyManagement")) {
                List<String> lst = new ArrayList<>(Arrays.asList(SCOPES));
                lst.add("import");
                Collections.sort(lst);
                return super.createTextValueList(lst.toArray(new String[0]), virtualTextCtx);
            }
            return super.createTextValueList(SCOPES, virtualTextCtx);
        }
        if (path.endsWith("repositories/repository/releases/updatePolicy") || //NOI18N
            path.endsWith("repositories/repository/snapshots/updatePolicy") || //NOI18N
            path.endsWith("pluginRepositories/pluginRepository/releases/updatePolicy") || //NOI18N
            path.endsWith("pluginRepositories/pluginRepository/snapshots/updatePolicy")) { //NOI18N
            return super.createTextValueList(MavenSettingsGrammar.UPDATE_POLICIES, virtualTextCtx);
        }
        if (path.endsWith("repository/releases/checksumPolicy") || //NOI18N
            path.endsWith("repository/snapshots/checksumPolicy") || //NOI18N
            path.endsWith("pluginRepository/releases/checksumPolicy") || //NOI18N
            path.endsWith("pluginRepository/snapshots/checksumPolicy")) { //NOI18N
            return super.createTextValueList(MavenSettingsGrammar.CHECKSUM_POLICIES, virtualTextCtx);
        }
        if (path.endsWith("repositories/repository/layout") || //NOI18N
            path.endsWith("pluginRepositories/pluginRepository/layout") || //NOI18N
            path.endsWith("distributionManagement/repository/layout")) { //NOI18N
            return super.createTextValueList(MavenSettingsGrammar.LAYOUTS, virtualTextCtx);
        }
        if (path.endsWith("repositories/repository/url") || //NOI18N
            path.endsWith("pluginRepositories/pluginRepository/url") || //NOI18N
            path.endsWith("distributionManagement/repository/url")) { //NOI18N
            
            List<String> repoIds = getRepoUrls();   
            return super.createTextValueList(repoIds.toArray(new String[0]),
                    virtualTextCtx);
        }
        
        if (path.endsWith("modules/module")) { //NOI18N
            FileObject fo = getEnvironment().getFileObject();
            if (fo != null) {
                File dir = FileUtil.toFile(fo).getParentFile();  
                String prefix = virtualTextCtx.getCurrentPrefix();
                boolean endingSlash = prefix.endsWith("/");
                String[] elms = StringUtils.split(prefix, "/");
                String lastElement = "";
                for (int i = 0; i < elms.length; i++) {
                    if ("..".equals(elms[i])) { //NOI18N
                        dir = dir != null ? dir.getParentFile() : null;
                    } else if (i < elms.length - (endingSlash ? 0 : 1)) {
                        dir = dir != null ? new File(dir, elms[i]) : null;
                    } else {
                        lastElement = elms[i];
                    }
                }
                prefix = lastElement != null ? lastElement : prefix;
                if (dir == null || !dir.exists() || !dir.isDirectory()) {
                    return null;
                }
                
                File[] modules = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                         return pathname.isDirectory() && new File(pathname, "pom.xml").exists(); //NOI18N
                    }
                });
                Collection<GrammarResult> elems = new ArrayList<>();
                for (int i = 0; i < modules.length; i++) {
                    if (modules[i].getName().startsWith(prefix)) {
                        elems.add(new MyTextElement(modules[i].getName(), prefix));
                    }
                }
                return Collections.enumeration(elems);
            }
        }
        return null;
    }

  /*Return repo url's*/
    private List<String> getRepoUrls() {
        List<String> repos = new ArrayList<>();

        List<RepositoryInfo> ris = RepositoryPreferences.getInstance().getRepositoryInfos();
        for (RepositoryInfo ri : ris) {
            if(ri.getRepositoryUrl()!=null){
             repos.add(ri.getRepositoryUrl());
            }
        }

        return repos;

    }
    private Document loadDocument(ArtifactInfoHolder info, MavenEmbedder embedder) {
        if (info.getArtifactId() != null && info.getGroupId() != null && info.getVersion() != null) {
            Artifact art = embedder.createArtifact(info.getGroupId(), info.getArtifactId(), info.getVersion(), null, "jar"); //NOI18N
            String repopath = embedder.getLocalRepository().pathOf(art);
            File fil = new File(embedder.getLocalRepositoryFile(), repopath);
            if (fil.exists()) {
                try {
                    JarFile jf = new JarFile(fil);
                    JarEntry entry = jf.getJarEntry("META-INF/maven/plugin.xml"); //NOI18N
                    if (entry != null) {
                        InputStream str = jf.getInputStream(entry);
                        SAXBuilder builder = new SAXBuilder();
                        return builder.build(str);
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINER, "", ex);
                } catch (JDOMException ex) {
                    LOG.log(Level.FINER, "", ex);
                }
            }
            
        }
        return null;
    }
    
    // NodeList isn't iterable for some reason
    private static Iterable<Node> iterate(NodeList list) {
        return () -> new Iterator<Node>() {
            int current = 0;
            @Override public boolean hasNext() {
                return current < list.getLength();
            }
            @Override public Node next() {
                return list.item(current++);
            }
        };
    }

    private void collectArtifacts(String artifactTag, Node parent, String decoratedProperty, Set<ArtifactInfoHolder> usages) {
        for (Node child : iterate(parent.getChildNodes())) {
            if (artifactTag.equals(child.getNodeName())) {
                ArtifactInfoHolder artifact = new ArtifactInfoHolder();
                artifact.setGroupId("org.apache.maven.plugins"); //NOI18N
                for (Node attr : iterate(child.getChildNodes())) {
                    if (attr.getNodeName() != null) switch (attr.getNodeName()) {
                        case "groupId": artifact.setGroupId(attr.getFirstChild().getNodeValue()); break; //NOI18N
                        case "artifactId": artifact.setArtifactId(attr.getFirstChild().getNodeValue()); break; //NOI18N
                        case "version": artifact.setVersion(attr.getFirstChild().getNodeValue()); break; //NOI18N
                    }
                }
                if (artifact.getGroupId() != null && artifact.getArtifactId() != null && decoratedProperty.equals(artifact.getVersion())) {
                    usages.add(artifact);
                }
            }
        }
    }

    private Enumeration<GrammarResult> collectGoals(Document pluginDoc, HintContext virtualTextCtx) {
        Iterator<Content> it = pluginDoc.getRootElement().getDescendants();
        Collection<GrammarResult> toReturn = new ArrayList<>();
        while (it.hasNext()) {
            Content c = it.next();
            if (!(c instanceof Element)) {
                continue;
            }
            Element el = (Element) c;
            if (!("goal".equals(el.getName()) && //NOI18N
                  el.getParentElement() != null && "mojo".equals(el.getParentElement().getName()))) { //NOI18N
                continue;
            }
            String name = el.getText();
            if (name.startsWith(virtualTextCtx.getCurrentPrefix())) {
               toReturn.add(new MyTextElement(name, virtualTextCtx.getCurrentPrefix()));
            }
        }
        return Collections.enumeration(toReturn);
    }

    private Enumeration<GrammarResult> collectGoals(ArtifactInfoHolder info, HintContext virtualTextCtx) {

        if (info.getGroupId() == null || info.getArtifactId() == null || info.getVersion() == null) {
            //#159317
            return null;
        }
        @SuppressWarnings("unchecked")
        Set<String> goals;
        try {
            goals = PluginIndexManager.getPluginGoals(info.getGroupId(), info.getArtifactId(), info.getVersion());
            if (goals == null) {
                // let the document/local repository based collectGoals() get a chance.
                return null;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        Collection<GrammarResult> toReturn = new ArrayList<>();
        for (String name : goals) {
            if (name.startsWith(virtualTextCtx.getCurrentPrefix())) {
               toReturn.add(new MyTextElement(name, virtualTextCtx.getCurrentPrefix()));
            }
        }
        return Collections.enumeration(toReturn);
    }

    
    private static class ArtifactInfoHolder  {
        private String artifactId;
        private String groupId;
        private String version;
        
        public String getArtifactId() {
            return artifactId;
        }
        
        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }
        
        public String getGroupId() {
            return groupId;
        }
        
        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.artifactId);
            hash = 67 * hash + Objects.hashCode(this.groupId);
            hash = 67 * hash + Objects.hashCode(this.version);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ArtifactInfoHolder other = (ArtifactInfoHolder) obj;
            if (!Objects.equals(this.artifactId, other.artifactId)) {
                return false;
            }
            if (!Objects.equals(this.groupId, other.groupId)) {
                return false;
            }
            return Objects.equals(this.version, other.version);
        }

        @Override
        public String toString() {
            return "ArtifactInfoHolder{" + "artifactId=" + artifactId + ", groupId=" + groupId + ", version=" + version + '}'; //NOI18N
        }

    }
}
