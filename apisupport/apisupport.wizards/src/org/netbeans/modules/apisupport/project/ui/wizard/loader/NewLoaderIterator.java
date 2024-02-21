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

package org.netbeans.modules.apisupport.project.ui.wizard.loader;

import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;

/**
 * Wizard for creating new DataLoaders
 *
 * @author Milos Kleint
 */
@TemplateRegistration(
    folder=UIUtil.TEMPLATE_FOLDER,
    position=500,
    displayName="#template_loader",
    iconBase="org/netbeans/modules/apisupport/project/ui/wizard/loader/newLoader.png",
    description="newLoader.html",
    category=UIUtil.TEMPLATE_CATEGORY
)
@Messages("template_loader=File Type")
public final class NewLoaderIterator extends BasicWizardIterator {
    
    private NewLoaderIterator.DataModel data;
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewLoaderIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new FileRecognitionPanel(wiz, data),
            new NameAndLocationPanel(wiz, data)
        };
    }
    
    public @Override void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        private String prefix;
        private File iconPath;
        private String mimeType;
        private boolean extensionBased = true;
        private String extension;
        private String namespace;
        private boolean useMultiview;
        
        private CreatedModifiedFiles files;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return files;
        }
        
        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.files = files;
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
        
        public File getIconPath() {
            return iconPath;
        }
        
        public void setIconPath(File iconPath) {
            this.iconPath = iconPath;
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
        
        public boolean isExtensionBased() {
            return extensionBased;
        }
        
        public void setExtensionBased(boolean extensionBased) {
            this.extensionBased = extensionBased;
        }

        public boolean canUseMultiview() {
            try {
                SpecificationVersion v = getModuleInfo().getDependencyVersion("org.netbeans.core.multiview"); // NOI18N
                if (v == null) {
                    return false;
                }
                SpecificationVersion l = getModuleInfo().getDependencyVersion("org.openide.loaders"); // NOI18N
                if (l == null) {
                    return false;
                }
                return v.compareTo(new SpecificationVersion("1.24")) >= 0 // NOI18N
                  && l.compareTo(new SpecificationVersion("7.26")) >= 0; // NOI18N
            } catch (IOException ex) {
                return false;
            }
        }
        public boolean isUseMultiview() {
            return useMultiview;
        }

        public void setUseMultiview(boolean useMultiview) {
            this.useMultiview = useMultiview;
        }
        
        public String getExtension() {
            return extension;
        }
        
        public void setExtension(String extension) {
            this.extension = extension;
        }
        
        public String getNamespace() {
            return namespace;
        }
        
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
        
    }
    
    public static void generateFileChanges(DataModel model) {
        CreatedModifiedFiles fileChanges = new CreatedModifiedFiles(model.getProject());

        boolean loaderlessObject;
        boolean lookupReadyObject;
        boolean annotationReadyObject;
        try {
            SpecificationVersion current = model.getModuleInfo().getDependencyVersion("org.openide.loaders"); // NOI18N
            annotationReadyObject = current == null || current.compareTo(new SpecificationVersion("7.36")) >= 0; // NOI18N
            loaderlessObject = current == null || current.compareTo(new SpecificationVersion("7.1")) >= 0; // NOI18N
            lookupReadyObject = current == null || current.compareTo(new SpecificationVersion("6.0")) >= 0; // NOI18N
        } catch (IOException ex) {
            Logger.getLogger(NewLoaderIterator.class.getName()).log(Level.INFO, null, ex);
            annotationReadyObject = false;
            loaderlessObject = false;
            lookupReadyObject = false;
        }
        
        String namePrefix = model.getPrefix();
        String packageName = model.getPackageName();
        final String mime = model.getMimeType();
        Map<String, String> replaceTokens = new HashMap<String, String>();
        replaceTokens.put("PREFIX", namePrefix);//NOI18N
        replaceTokens.put("PACKAGENAME", packageName);//NOI18N
        replaceTokens.put("MIMETYPE", mime);//NOI18N
        if (model.isExtensionBased()) {
            if (annotationReadyObject) {
                replaceTokens.put("EXTENSIONS", formatToList(model.getExtension())); // NOI18N
            } else {
                replaceTokens.put("EXTENSIONS", formatExtensions(model.isExtensionBased(), model.getExtension(), mime));//NOI18N
            }
        } else {
            if (annotationReadyObject) {
                replaceTokens.put("NAMESPACES", formatToList(model.getNamespace())); // NOI18N
            } else {
                replaceTokens.put("NAMESPACES", formatNameSpace(model.isExtensionBased(), model.getNamespace(), mime));//NOI18N
            }
        }
        
        // Copy action icon
        File origIconPath = model.getIconPath();
        FileObject origIcon = origIconPath != null ? FileUtil.toFileObject(origIconPath) : null;
        String relativeIconPath;
        if (origIcon != null) {
            relativeIconPath = model.addCreateIconOperation(fileChanges, origIcon);
            replaceTokens.put("IMAGESNIPPET", formatImageSnippet(relativeIconPath));//NOI18N
            replaceTokens.put("ICONPATH", relativeIconPath);//NOI18N
            replaceTokens.put("COMMENTICON", "");//NOI18N
        } else {
            replaceTokens.put("IMAGESNIPPET", formatImageSnippet(null)); //NOI18N
            replaceTokens.put("ICONPATH", "SET/PATH/TO/ICON/HERE"); //NOI18N
            replaceTokens.put("COMMENTICON", "//");//NOI18N
            relativeIconPath = null;
        }
        
        FileObject template;
        if (!loaderlessObject) {
            // 1. create dataloader file
            String loaderName = model.getDefaultPackagePath(namePrefix + "DataLoader.java", false); // NOI18N
            // XXX use nbresloc URL protocol rather than NewLoaderIterator.class.getResource(...):
            template = CreatedModifiedFiles.getTemplate("templateDataLoader.java");//NOI18N
            fileChanges.add(fileChanges.createFileWithSubstitutions(loaderName, template, replaceTokens));
            String loaderInfoName = model.getDefaultPackagePath(namePrefix + "DataLoaderBeanInfo.java", false); // NOI18N
            template = CreatedModifiedFiles.getTemplate("templateDataLoaderBeanInfo.java");//NOI18N
            fileChanges.add(fileChanges.createFileWithSubstitutions(loaderInfoName, template, replaceTokens));
        }
        
        // 2. dataobject file
        final boolean isEditable = Pattern.matches("(application/([a-zA-Z0-9_.-])*\\+xml|text/([a-zA-Z0-9_.+-])*)", //NOI18N
                mime);
        if (isEditable) {
            StringBuffer editorBuf = new StringBuffer();
            editorBuf.append("        CookieSet cookies = getCookieSet();\n");//NOI18N
            editorBuf.append("        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));"); // NOI18N
            replaceTokens.put("EDITOR_SUPPORT_SNIPPET", editorBuf.toString());//NOI18N
            replaceTokens.put("EDITOR_SUPPORT_IMPORT", "import org.openide.text.DataEditorSupport;");//NOI18N
        } else {
            // ignore the editor support snippet
            replaceTokens.put("EDITOR_SUPPORT_SNIPPET", "");//NOI18N
            replaceTokens.put("EDITOR_SUPPORT_IMPORT", "");//NOI18N
        }
        
        String doName = model.getDefaultPackagePath(namePrefix + "DataObject.java", false); // NOI18N
        template = null;
        if (annotationReadyObject) {
            template = CreatedModifiedFiles.getTemplate("templateDataObjectAnno.java");//NOI18N
            if (model.isUseMultiview()) {
                replaceTokens.put("MULTIVIEW", "true"); // NOI18N
            }
        } else if (loaderlessObject) {
            if (model.isUseMultiview()) {
                template = CreatedModifiedFiles.getTemplate("templateDataObjectMulti.java");//NOI18N
            } else {
                template = CreatedModifiedFiles.getTemplate("templateDataObjectInLayer.java");//NOI18N
            }
        } else if (lookupReadyObject) {
            template = CreatedModifiedFiles.getTemplate("templateDataObjectWithLookup.java");//NOI18N
        }
        if (template == null) {
            template = CreatedModifiedFiles.getTemplate("templateDataObject.java");//NOI18N
        }
        
        
        fileChanges.add(fileChanges.createFileWithSubstitutions(doName, template, replaceTokens));
        if (model.isUseMultiview()) {
            String formName = model.getDefaultPackagePath(namePrefix + "VisualElement.form", false); // NOI18N
            String javaName = model.getDefaultPackagePath(namePrefix + "VisualElement.java", false); // NOI18N
            FileObject java = CreatedModifiedFiles.getTemplate("templateDataObjectMultiForm.java");
            FileObject form = CreatedModifiedFiles.getTemplate("templateDataObjectMultiForm.form");
            fileChanges.add(fileChanges.createFile(formName, form));
            fileChanges.add(fileChanges.createFileWithSubstitutions(javaName, java, replaceTokens));
        }
        
        if (!loaderlessObject) {
            // 3. node file
            String nodeName = model.getDefaultPackagePath(namePrefix + "DataNode.java", false); // NOI18N
            template = CreatedModifiedFiles.getTemplate("templateDataNode.java");//NOI18N
            fileChanges.add(fileChanges.createFileWithSubstitutions(nodeName, template, replaceTokens));
        }
        
        if (!annotationReadyObject) {
            // 4. mimetyperesolver file
            template = CreatedModifiedFiles.getTemplate("templateresolver.xml");//NOI18N
            fileChanges.add(fileChanges.createLayerEntry("Services/MIMEResolver/" + namePrefix + "Resolver.xml", //NOI18N
                    template,
                    replaceTokens,
                    namePrefix + " Files",//NOI18N
                    null));
        }
        
        //5. update project.xml with dependencies
        fileChanges.add(fileChanges.addModuleDependency("org.netbeans.api.templates")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.filesystems")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.loaders")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.nodes")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.util")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.util.lookup")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.util.ui")); //NOI18N
        fileChanges.add(fileChanges.addModuleDependency("org.openide.windows")); //NOI18N
        if (isEditable) {
            // XXX unused at least for multiview case:
            fileChanges.add(fileChanges.addModuleDependency("org.openide.text")); //NOI18N
        }
        if (model.isUseMultiview()) {
            fileChanges.add(fileChanges.addModuleDependency("org.netbeans.core.multiview")); //NOI18N
            fileChanges.add(fileChanges.addModuleDependency("org.openide.awt")); //NOI18N
        }
        if (annotationReadyObject) {
            fileChanges.add(fileChanges.addModuleDependency("org.openide.awt")); //NOI18N
            fileChanges.add(fileChanges.addModuleDependency("org.openide.dialogs")); // NOI18N
        }

        if (!loaderlessObject) {
        // 6. update/create bundle file
        String bundlePath = model.getDefaultPackagePath("Bundle.properties", true); // NOI18N
        fileChanges.add(fileChanges.bundleKey(bundlePath, "LBL_" + namePrefix + "_loader_name",  // NOI18N
                namePrefix + " Files")); //NOI18N
        }
        if (annotationReadyObject) {
            // registration via processors
        } else if (loaderlessObject) {
            // 7. register in layer
            if (!annotationReadyObject) {
            String path = "Loaders/" + mime + "/Factories/" + namePrefix + "DataLoader.instance";
            Map<String,Object> attrs = new HashMap<String, Object>();
            attrs.put("instanceCreate", "methodvalue:org.openide.loaders.DataLoaderPool.factory"); //NOI18N
            attrs.put("dataObjectClass", packageName + "." + namePrefix + "DataObject"); //NOI18N
            attrs.put("mimeType", mime); //NOI18N
            if (relativeIconPath != null) {
                attrs.put("iconBase", relativeIconPath); //NOI18N
            }
            fileChanges.add(
                fileChanges.createLayerEntry(path, null, null, null, attrs)
            );
            }
        } else {
            // 7. register manifest entry
            boolean isXml = Pattern.matches("(application/([a-zA-Z0-9_.-])*\\+xml|text/([a-zA-Z0-9_.-])*\\+xml)", //NOI18N
                    mime);
            String installBefore = null;
            if (isXml) {
                installBefore = "org.openide.loaders.XMLDataObject, org.netbeans.modules.xml.XMLDataObject"; //NOI18N
            }

            fileChanges.add(fileChanges.addLoaderSection(packageName.replace('.', '/')  + "/" + namePrefix + "DataLoader", installBefore)); // NOI18N

            // 7a. create matching test registration for convenience (#73202)
            fileChanges.add(fileChanges.addLookupRegistration("org.openide.loaders.DataLoader", packageName + '.' + namePrefix + "DataLoader", true)); // NOI18N
        }
        
        //8. create layerfile actions subsection
        
        if (!annotationReadyObject) fileChanges.add(fileChanges.layerModifications(new CreatedModifiedFiles.LayerOperation() {
            @Override
            public void run(FileSystem layer) throws IOException {
                List<String> actions = new ArrayList<String>();
                if (isEditable) {
                    actions.add("System/org-openide-actions-OpenAction"); // NOI18N
                }
                actions.addAll(Arrays.asList(new String[] {
                    null,
                    "Edit/org-openide-actions-CutAction", // NOI18N
                    "Edit/org-openide-actions-CopyAction", // NOI18N
                    null,
                    "Edit/org-openide-actions-DeleteAction", // NOI18N
                    "System/org-openide-actions-RenameAction", // NOI18N
                    null,
                    "System/org-openide-actions-SaveAsTemplateAction", // NOI18N
                    null,
                    "System/org-openide-actions-FileSystemAction", // NOI18N
                    null,
                    "System/org-openide-actions-ToolsAction", // NOI18N
                    "System/org-openide-actions-PropertiesAction", // NOI18N
                }));
                FileObject folder = FileUtil.createFolder(layer.getRoot(), "Loaders/" + mime + "/Actions"); // NOI18N
                List<DataObject> kids = new ArrayList<DataObject>();
                Iterator it = actions.iterator();
                int i = 0;
                while (it.hasNext()) {
                    String name = (String) it.next();
                    FileObject kid;
                    if (name != null) {
                        kid = folder.createData(name.replaceAll("[^/]*/", "") + ".shadow"); // NOI18N
                        kid.setAttribute("originalFile", "Actions/" + name + ".instance"); // NOI18N
                    } else {
                        kid = folder.createData("sep-" + (++i) + ".instance"); // NOI18N
                        kid.setAttribute("instanceClass", "javax.swing.JSeparator"); // NOI18N
                    }
                    kids.add(DataObject.find(kid));
                }
                DataFolder.findFolder(folder).setOrder(kids.toArray(new DataObject[0]));
            }
        }, Collections.<String>emptySet()));
        
        //9. create sample template
        String suffix = null;
        if (model.isExtensionBased()) {
            suffix = "Template." + getFirstExtension(model.getExtension()); // NOI18N
            template = CreatedModifiedFiles.getTemplate("templateNew1");//NOI18N
        } else {
            template = CreatedModifiedFiles.getTemplate("templateNew2");//NOI18N
            suffix = "Template.xml"; // NOI18N
            try {
                replaceTokens.put("NAMESPACE", XMLUtil.toElementContent(model.getNamespace())); // NOI18N
            } catch (CharConversionException ex) {
                assert false: ex;
            }
        }
        if (annotationReadyObject) {
            Map<String, Object> templateAttrs = new LinkedHashMap<String, Object>();
            templateAttrs.put("folder", "Other");
            templateAttrs.put("content", namePrefix + suffix);
            fileChanges.add(fileChanges.packageInfo(packageName, Collections.singletonMap(TemplateRegistration.class.getCanonicalName(), templateAttrs)));
        } else {
            replaceTokens.put("TEMPLATE_NAME", suffix);
        }
        
        boolean useTR = false;
        NbModuleProvider nbmp = model.getModuleInfo();
        try {
            SpecificationVersion v = nbmp.getDependencyVersion("org.openide.loaders");
            if (v != null && v.compareTo(new SpecificationVersion("7.29")) >= 0) {
                useTR = true;
            }
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
        String displayName = "Empty " + namePrefix + " file";
        String templateName = namePrefix + suffix;
        if (useTR) {
            fileChanges.add(fileChanges.createFileWithSubstitutions(model.getDefaultPackagePath(templateName, true), template, replaceTokens));
            if (!annotationReadyObject) {
                Map<String,Map<String,?>> annos = new LinkedHashMap<String,Map<String,?>>();
                Map<String,Object> tr = new LinkedHashMap<String,Object>();
                tr.put("folder", "Other");
                tr.put("displayName", "#" + namePrefix + "template_displayName");
                tr.put("content", templateName);
                annos.put("org.netbeans.api.templates.TemplateRegistration", tr);
                annos.put("org.openide.util.NbBundle.Messages", Collections.singletonMap("value", namePrefix + "template_displayName=" + displayName));
                fileChanges.add(fileChanges.packageInfo(packageName, annos));
            }
        } else {
            fileChanges.add(fileChanges.createLayerEntry("Templates/Other/" + templateName, template, replaceTokens, displayName, Collections.singletonMap("template", true)));
        }
        model.setCreatedModifiedFiles(fileChanges);
    }
    
    private static String formatExtensions(boolean isExtensionBased, String ext, String mime) {
        if (!isExtensionBased) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        StringTokenizer tokens = new StringTokenizer(ext, " ,"); // NOI18N
        while (tokens.hasMoreTokens()) {
            String element = tokens.nextToken().trim();
            if (element.startsWith(".")) { // NOI18N
                element = element.substring(1);
            }
            buff.append("        <ext name=\"").append(element).append("\"/>\n"); //NOI18N
        }
        buff.append("        <resolver mime=\"").append(mime).append("\"/>"); //NOI18N
        return buff.toString();
    }

    private static String formatToList(String ext) {
        StringBuilder buff = new StringBuilder();
        buff.append("{ ");
        StringTokenizer tokens = new StringTokenizer(ext, " ,"); // NOI18N
        String sep = "";
        while (tokens.hasMoreTokens()) {
            String element = tokens.nextToken().trim();
            buff.append(sep).append("\"").append(element).append("\"");
            sep = ", ";
        }
        buff.append(" }");
        return buff.toString();
    }

    private static String getFirstExtension(String ext) {
        StringTokenizer tokens = new StringTokenizer(ext," ,"); // NOI18N
        String element = "someextension"; // NOI18N
        if (tokens.hasMoreTokens()) {
            element = tokens.nextToken().trim();
            if (element.startsWith(".")) { //NOI18N
                element = element.substring(1);
            }
        }
        return element;
    }
    
    private static String formatNameSpace(boolean isExtensionBased, String namespace, String mime) {
        if (isExtensionBased) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        buff.append("        <ext name=\"xml\"/>\n"); //NOI18N
        buff.append("        <resolver mime=\"").append(mime).append("\">\n"); //NOI18N
        buff.append("            <xml-rule>\n"); // NOI18N
        try {
            buff.append("                <element ns=\"").append(XMLUtil.toElementContent(namespace)).append("\"/>\n"); //NOI18N
        } catch (CharConversionException ex) {
            assert false : ex;
        }
        buff.append("            </xml-rule>\n"); //NOI18N
        buff.append("        </resolver>"); //NOI18N
        return buff.toString();
    }
    
    private static String formatImageSnippet(String path) {
        if (path == null) {
            return "return super.getIcon(type); // TODO add a custom icon here: ImageUtilities.loadImage(..., true)\n"; //NOI18N
        }
        StringBuffer buff = new StringBuffer();
        buff.append("        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {\n"); //NOI18N
        buff.append("            return ImageUtilities.loadImage(\""); //NOI18N
        buff.append(path).append("\");\n"); //NOI18N
        buff.append("        } else {\n"); //NOI18N
        buff.append("            return null;\n        }\n"); //NOI18N
        return buff.toString();
    }
    
}

