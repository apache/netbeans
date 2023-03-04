/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Reads/writes project.xml.
 *
 * @author  Jesse Glick, David Konecny, Pavel Buzek
 */
public class FreeformProjectGenerator {

    /** Keep root elements in the order specified by project's XML schema. */
    private static final String[] rootElementsOrder = new String[]{"name", "properties", "folders", "ide-actions", "export", "view", "subprojects"}; // NOI18N
    private static final String[] viewElementsOrder = new String[]{"items", "context-menu"}; // NOI18N
    
    // this order is not required by schema, but follow it to minimize randomness a bit
    //private static final String[] folderElementsOrder = new String[]{"source-folder", "build-folder"}; // NOI18N
    private static final String[] viewItemElementsOrder = new String[]{"source-folder", "source-file"}; // NOI18N
    private static final String[] contextMenuElementsOrder = new String[]{"ide-action", "separator", "action"}; // NOI18N
    
    private FreeformProjectGenerator() {}

    public static AntProjectHelper createProject(File location, File dir, String name, File antScript) throws IOException {
        FileObject dirFO = createProjectDir(dir);
        FileObject locationFO = FileUtil.toFileObject(location);
        AntProjectHelper h = createProject(locationFO, dirFO, name, antScript);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }
    
    private static AntProjectHelper createProject(final FileObject locationFO, final FileObject dirFO, final String name, final File antScript) throws IOException {
        final AntProjectHelper[] h = new AntProjectHelper[1];
        final IOException[] ioe = new IOException[1];
        ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    Project p;
                    try {
                        h[0] = ProjectGenerator.createProject(dirFO, FreeformProjectType.TYPE);
                        p = ProjectManager.getDefault().findProject(dirFO);
                    } catch (IOException e) {
                        ioe[0] = e;
                        return;
                    }
                    AuxiliaryConfiguration aux = p.getLookup().lookup(AuxiliaryConfiguration.class);
                    assert aux != null;

                    Element data = Util.getPrimaryConfigurationData(h[0]);
                    Document doc = data.getOwnerDocument();

                    Node comment = doc.createComment(" " + NbBundle.getMessage(FreeformProjectGenerator.class, "LBL_Manual_Editing_Warning") + " ");
                    data.appendChild(comment);
                    
                    Element nm = doc.createElementNS(FreeformProjectType.NS_GENERAL, "name"); // NOI18N
                    nm.appendChild(doc.createTextNode(name)); // NOI18N
                    data.appendChild(nm);
                    Element props = doc.createElementNS(FreeformProjectType.NS_GENERAL, "properties"); // NOI18N
                    File locationF = FileUtil.toFile(locationFO);
                    File dirF = FileUtil.toFile(dirFO);
                    Map<String,String> properties = new HashMap<String,String>();
                    if (!locationFO.equals(dirFO)) {
                        Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); // NOI18N
                        property.setAttribute("name", ProjectConstants.PROP_PROJECT_LOCATION); // NOI18N
                        String path;
                        if (CollocationQuery.areCollocated(dirF, locationF)) {
                            path = PropertyUtils.relativizeFile(dirF, locationF); // NOI18N
                        } else {
                            path = locationF.getAbsolutePath();
                        }
                        property.appendChild(doc.createTextNode(path));
                        props.appendChild(property);
                        properties.put(ProjectConstants.PROP_PROJECT_LOCATION, path);
                    }
                    String antPath = "build.xml"; // NOI18N
                    if (antScript != null) {
                        Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); // NOI18N
                        property.setAttribute("name", ProjectConstants.PROP_ANT_SCRIPT); // NOI18N
                        antPath = Util.relativizeLocation(locationF, dirF, antScript);
                        property.appendChild(doc.createTextNode(antPath));
                        properties.put(ProjectConstants.PROP_ANT_SCRIPT, antPath);
                        antPath = "${"+ProjectConstants.PROP_ANT_SCRIPT+"}"; // NOI18N
                        props.appendChild(property);
                    }
                    //#56344:Always write a <properties> element to project.xml of a generated freeform
//                  if (props.getChildNodes().getLength() > 0) {
                    data.appendChild(props);
//                  }
                    Util.putPrimaryConfigurationData(h[0], data);
                    putBuildXMLSourceFile(h[0], antPath);
                }
            }
        );

        if (ioe[0] != null) {
            throw ioe[0];
        }
        return h[0];
    }

    private static FileObject createProjectDir(File dir) throws IOException {
        FileObject dirFO;
        if(!dir.exists()) {
            //Refresh before mkdir not to depend on window focus
            refreshFileSystem (dir);
            dir.mkdirs();
            refreshFileSystem (dir);
        }
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir; // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        return dirFO;                        
    }


    private static void refreshFileSystem (final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }

    /**
     * Read target mappings from project.
     * @param helper AntProjectHelper instance
     * @return list of TargetMapping instances
     */
    public static List<TargetMapping> getTargetMappings(AntProjectHelper helper) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<TargetMapping> list = new ArrayList<TargetMapping>();
        Element genldata = Util.getPrimaryConfigurationData(helper);
        Element actionsEl = XMLUtil.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            return list;
        }
        for (Element actionEl : XMLUtil.findSubElements(actionsEl)) {
            TargetMapping tm = new TargetMapping();
            tm.name = actionEl.getAttribute("name"); // NOI18N
            List<String> targetNames = new ArrayList<String>();
            EditableProperties props = new EditableProperties(false);
            for (Element subEl : XMLUtil.findSubElements(actionEl)) {
                if (subEl.getLocalName().equals("target")) { // NOI18N
                    targetNames.add(XMLUtil.findText(subEl));
                    continue;
                }
                if (subEl.getLocalName().equals("script")) { // NOI18N
                    tm.script = XMLUtil.findText(subEl);
                    continue;
                }
                if (subEl.getLocalName().equals("context")) { // NOI18N
                    TargetMapping.Context ctx = new TargetMapping.Context();
                    for (Element contextSubEl : XMLUtil.findSubElements(subEl)) {
                        if (contextSubEl.getLocalName().equals("property")) { // NOI18N
                            ctx.property = XMLUtil.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("format")) { // NOI18N
                            ctx.format = XMLUtil.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("folder")) { // NOI18N
                            ctx.folder = XMLUtil.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("pattern")) { // NOI18N
                            ctx.pattern = XMLUtil.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("arity")) { // NOI18N
                            Element sepFilesEl = XMLUtil.findElement(contextSubEl, "separated-files", FreeformProjectType.NS_GENERAL); // NOI18N
                            if (sepFilesEl != null) {
                                ctx.separator = XMLUtil.findText(sepFilesEl);
                            }
                            continue;
                        }
                    }
                    tm.context = ctx;
                }
                if (subEl.getLocalName().equals("property")) { // NOI18N
                    readProperty(subEl, props);
                    continue;
                }
            }
            tm.targets = targetNames;
            if (props.keySet().size() > 0) {
                tm.properties = props;
            }
            list.add(tm);
        }
        return list;
    }
    
    private static void readProperty(Element propertyElement, EditableProperties props) {
        String key = propertyElement.getAttribute("name"); // NOI18N
        String value = XMLUtil.findText(propertyElement);
        props.setProperty(key, value);
    }

    /**
     * Update target mappings of the project. Project is left modified and 
     * you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param mappings list of <TargetMapping> instances to store
     */
    public static void putTargetMappings(AntProjectHelper helper, List<TargetMapping> mappings) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element actions = XMLUtil.findElement(data, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actions != null) {
            data.removeChild(actions);
        }
        
        actions = doc.createElementNS(FreeformProjectType.NS_GENERAL, "ide-actions"); // NOI18N
        for (TargetMapping tm : mappings) {
            Element action = doc.createElementNS(FreeformProjectType.NS_GENERAL, "action"); //NOI18N
            assert tm.name != null && tm.name.length() > 0;
            action.setAttribute("name", tm.name); // NOI18N
            if (tm.script != null) {
                Element script = doc.createElementNS(FreeformProjectType.NS_GENERAL, "script"); //NOI18N
                script.appendChild(doc.createTextNode(tm.script));
                action.appendChild(script);
            }
            if (tm.targets != null) {
                for (String targetName : tm.targets) {
                    Element target = doc.createElementNS(FreeformProjectType.NS_GENERAL, "target"); //NOI18N
                    target.appendChild(doc.createTextNode(targetName));
                    action.appendChild(target);
                }
            }
            if (tm.properties != null) {
                writeProperties(tm.properties, doc, action);
            }
            if (tm.context != null) {
                Element context = doc.createElementNS(FreeformProjectType.NS_GENERAL, "context"); //NOI18N
                TargetMapping.Context ctx = tm.context;
                assert ctx.property != null;
                Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); //NOI18N
                property.appendChild(doc.createTextNode(ctx.property));
                context.appendChild(property);
                assert ctx.folder != null;
                Element folder = doc.createElementNS(FreeformProjectType.NS_GENERAL, "folder"); //NOI18N
                folder.appendChild(doc.createTextNode(ctx.folder));
                context.appendChild(folder);
                if (ctx.pattern != null) {
                    Element pattern = doc.createElementNS(FreeformProjectType.NS_GENERAL, "pattern"); //NOI18N
                    pattern.appendChild(doc.createTextNode(ctx.pattern));
                    context.appendChild(pattern);
                }
                assert ctx.format != null;
                Element format = doc.createElementNS(FreeformProjectType.NS_GENERAL, "format"); //NOI18N
                format.appendChild(doc.createTextNode(ctx.format));
                context.appendChild(format);
                Element arity = doc.createElementNS(FreeformProjectType.NS_GENERAL, "arity"); // NOI18N
                if (ctx.separator != null) {
                    Element sepFilesEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "separated-files"); // NOI18N
                    sepFilesEl.appendChild(doc.createTextNode(ctx.separator));
                    arity.appendChild(sepFilesEl);
                } else {
                    arity.appendChild(doc.createElementNS(FreeformProjectType.NS_GENERAL, "one-file-only")); // NOI18N
                }
                context.appendChild(arity);
                action.appendChild(context);
            }
            actions.appendChild(action);
        }
        XMLUtil.appendChildElement(data, actions, rootElementsOrder);
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    private static void writeProperties(EditableProperties props, Document doc, Element element) {
        for (Map.Entry<String,String> entry : props.entrySet()) {
            Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); //NOI18N
            property.setAttribute("name", entry.getKey()); // NOI18N
            property.appendChild(doc.createTextNode(entry.getValue()));
            element.appendChild(property);
        }
    }
    
    /**
     * Update context menu actions. Project is left modified and 
     * you must save it explicitely. This method stores all IDE actions
     * before the custom actions what means that user's customization by hand
     * (e.g. order of items) is lost.
     * @param helper AntProjectHelper instance
     * @param mappings list of <TargetMapping> instances for which the context
     *     menu actions will be created
     */
    public static void putContextMenuAction(AntProjectHelper helper, List<TargetMapping> mappings) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element viewEl = XMLUtil.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            XMLUtil.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element contextMenuEl = XMLUtil.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextMenuEl == null) {
            contextMenuEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "context-menu"); // NOI18N
            XMLUtil.appendChildElement(viewEl, contextMenuEl, viewElementsOrder);
        }
        for (Element ideActionEl : XMLUtil.findSubElements(contextMenuEl)) {
            if (!ideActionEl.getLocalName().equals("ide-action")) { // NOI18N
                continue;
            }
            contextMenuEl.removeChild(ideActionEl);
        }
        for (TargetMapping tm : sortMappings(mappings)) {
            if (tm.context != null) {
                // ignore context sensitive actions
                continue;
            }
            Element ideAction = doc.createElementNS(FreeformProjectType.NS_GENERAL, "ide-action"); //NOI18N
            ideAction.setAttribute("name", tm.name); // NOI18N
            XMLUtil.appendChildElement(contextMenuEl, ideAction, contextMenuElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    /**
     * Read custom context menu actions from project.
     * @param helper AntProjectHelper instance
     * @return list of CustomTarget instances
     */
    public static List<CustomTarget> getCustomContextMenuActions(AntProjectHelper helper) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<CustomTarget> list = new ArrayList<CustomTarget>();
        Element genldata = Util.getPrimaryConfigurationData(helper);
        Element viewEl = XMLUtil.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            return list;
        }
        Element contextMenuEl = XMLUtil.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextMenuEl == null) {
            return list;
        }
        for (Element actionEl : XMLUtil.findSubElements(contextMenuEl)) {
            if (!actionEl.getLocalName().equals("action")) { // NOI18N
                continue;
            }
            CustomTarget ct = new CustomTarget();
            List<String> targetNames = new ArrayList<String>();
            EditableProperties props = new EditableProperties(false);
            for (Element subEl : XMLUtil.findSubElements(actionEl)) {
                if (subEl.getLocalName().equals("target")) { // NOI18N
                    targetNames.add(XMLUtil.findText(subEl));
                    continue;
                }
                if (subEl.getLocalName().equals("script")) { // NOI18N
                    ct.script = XMLUtil.findText(subEl);
                    continue;
                }
                if (subEl.getLocalName().equals("label")) { // NOI18N
                    ct.label = XMLUtil.findText(subEl);
                    continue;
                }
                if (subEl.getLocalName().equals("property")) { // NOI18N
                    readProperty(subEl, props);
                    continue;
                }
            }
            ct.targets = targetNames;
            if (props.keySet().size() > 0) {
                ct.properties = props;
            }
            list.add(ct);
        }
        return list;
    }
    
    /**
     * Update custom context menu actions of the project. Project is left modified and 
     * you must save it explicitely. This method stores all custom actions 
     * after the IDE actions what means that user's customization by hand 
     * (e.g. order of items) is lost.
     * @param helper AntProjectHelper instance
     * @param list of <CustomTarget> instances to store
     */
    public static void putCustomContextMenuActions(AntProjectHelper helper, List<CustomTarget> customTargets) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element viewEl = XMLUtil.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            XMLUtil.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element contextMenuEl = XMLUtil.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextMenuEl == null) {
            contextMenuEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "context-menu"); // NOI18N
            XMLUtil.appendChildElement(viewEl, contextMenuEl, viewElementsOrder);
        }
        for (Element actionEl : XMLUtil.findSubElements(contextMenuEl)) {
            if (!actionEl.getLocalName().equals("action")) { // NOI18N
                continue;
            }
            contextMenuEl.removeChild(actionEl);
        }
        for (CustomTarget ct : customTargets) {
            Element action = doc.createElementNS(FreeformProjectType.NS_GENERAL, "action"); //NOI18N
            if (ct.script != null) {
                Element script = doc.createElementNS(FreeformProjectType.NS_GENERAL, "script"); //NOI18N
                script.appendChild(doc.createTextNode(ct.script)); // NOI18N
                action.appendChild(script);
            }
            Element label = doc.createElementNS(FreeformProjectType.NS_GENERAL, "label"); //NOI18N
            label.appendChild(doc.createTextNode(ct.label)); // NOI18N
            action.appendChild(label);
            if (ct.targets != null) {
                for (String targetName : ct.targets) {
                    Element target = doc.createElementNS(FreeformProjectType.NS_GENERAL, "target"); //NOI18N
                    target.appendChild(doc.createTextNode(targetName)); // NOI18N
                    action.appendChild(target);
                }
            }
            if (ct.properties != null) {
                writeProperties(ct.properties, doc, action);
            }
            XMLUtil.appendChildElement(contextMenuEl, action, contextMenuElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    /**
     * Structure describing custom target mapping.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class CustomTarget {
        public List<String> targets;
        public String label;
        public String script;
        public EditableProperties properties;
    }
    
    /**
     * Structure describing target mapping.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class TargetMapping {
        public String script;
        public List<String> targets;
        public String name;
        public EditableProperties properties;
        public Context context; // may be null
        
        public static final class Context {
            public String property;
            public String format;
            public String folder;
            public String pattern; // may be null
            public String separator; // may be null
        }
    }

    private static void putBuildXMLSourceFile(AntProjectHelper helper, String antPath) {
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element viewEl = XMLUtil.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            XMLUtil.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element itemsEl = XMLUtil.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
        if (itemsEl == null) {
            itemsEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "items"); // NOI18N
            XMLUtil.appendChildElement(viewEl, itemsEl, viewElementsOrder);
        }
        Element fileEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "source-file"); // NOI18N
        Element el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
        el.appendChild(doc.createTextNode(antPath)); // NOI18N
        fileEl.appendChild(el);
        XMLUtil.appendChildElement(itemsEl, fileEl, viewItemElementsOrder);
        Util.putPrimaryConfigurationData(helper, data);
    }

    /**
     * Returns Ant script of the freeform project
     * represented by the given AntProjectHelper.
     * @param helper AntProjectHelper of freeform project
     * @param ev evaluator of the freeform project
     * @return Ant script FileObject or null if it cannot be found
     */
    public static FileObject getAntScript(AntProjectHelper helper, PropertyEvaluator ev) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        String antScript = ev.getProperty(ProjectConstants.PROP_ANT_SCRIPT);
        if (antScript != null) {
            File f= helper.resolveFile(antScript);
            if (!f.exists()) {
                return null;
            }
            FileObject fo = FileUtil.toFileObject(f);
            return fo;
        } else {
            FileObject fo = helper.getProjectDirectory().getFileObject("build.xml"); // NOI18N
            return fo;
        }
    }
    
    /* Sort only well known project actions target mappings,
     * order of other actions is kept unchanged
     */
    private static List<TargetMapping> sortMappings(List<TargetMapping> toSort) {
        
        ArrayList<TargetMapping> list2Sort = new ArrayList<TargetMapping>(toSort);
        
        String sortedActions[] = new String[] {
            ActionProvider.COMMAND_BUILD,
            ActionProvider.COMMAND_REBUILD,
            ActionProvider.COMMAND_CLEAN,
            "javadoc", // NOI18N
            ActionProvider.COMMAND_RUN,
            "deploy", // NOI18N
            "redeploy", // NOI18N
            ActionProvider.COMMAND_TEST };
        
        ArrayList<TargetMapping> sortedList = new ArrayList<TargetMapping>(list2Sort.size());
        
        for (String actionName : sortedActions) {
            for (TargetMapping mapping : list2Sort) {
                if (actionName.equals(mapping.name)) {
                    sortedList.add(mapping);
                    list2Sort.remove(mapping);
                    break;
                }
            }
        }
        
        for (TargetMapping mapping : list2Sort) {
            sortedList.add(mapping);
        }
        
        return sortedList;
        
    }
    
}
