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
package org.netbeans.modules.xml.jaxb.util;

import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.xml.jaxb.api.model.JAXBWizModel;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListener;
import org.netbeans.modules.xml.jaxb.cfg.schema.Binding;
import org.netbeans.modules.xml.jaxb.cfg.schema.Bindings;
import org.netbeans.modules.xml.jaxb.cfg.schema.Catalog;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSource;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption;
import org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions;
import org.netbeans.modules.xml.jaxb.model.JAXBWizModelImpl;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.*;
import org.openide.util.RequestProcessor.Task;

/**
 * @author gmpatil
 * @author lgao
 */
public class ProjectHelper {

    public static final int PROJECT_TYPE_J2SE = 0;
    public static final int PROJECT_TYPE_EJB = 1;
    public static final int PROJECT_TYPE_WEB = 2;
    
    private static final String JAXB_ANT_XTN_NAME = "jaxb"; //NOI18n    
    private static final String JAXB_LIB_NAME = "jaxb"; //NOI18N
    private static final String PROP_BUILD_DIR = "build.dir"; //NOI18N
    private static final String PROP_SRC_DIR = "src.dir"; //NOI18N
    private static final String PROP_SRC_ROOT = "source.root"; //NOI18N
    private static final String NBPROJECT_DIR = "nbproject"; //NOI18N    
    private static final String XML_BINDING_CONFIG_FILE_NAME = "xml_binding_cfg.xml"; //NOI18N
    private static final String XML_BINDING_BUILD_FILE_NAME = "xml_binding_build.xml"; //NOI18N
    private static final String FILE_OBJECT_SEPARATOR = "/"; // NOI18N
    private static final String XSL_RESOURCE = "org/netbeans/modules/xml/jaxb/resources/JAXBBuild.xsl"; //NOI18N
    private static final String EJB_XSL_RESOURCE = "org/netbeans/modules/xml/jaxb/resources/JAXBBuild_ejb.xsl"; //NOI18N
    private static final String WEB_XSL_RESOURCE = "org/netbeans/modules/xml/jaxb/resources/JAXBBuild_web.xsl"; //NOI18N
    private static final String BUILD_GEN_JAXB_DIR = "build/generated-sources/jaxb"; //NOI18N
    private static final String NON_JAVA_SE_CONFIG_DIR = "conf/xml-resources/jaxb"; //NOI18N
    private static final String JAVA_SE_CONFIG_DIR = "xml-resources/jaxb"; //NOI18N
    private static final String PROP_XJC_DEF_CLASSPATH = "jaxbwiz.xjcdef.classpath" ;//NOI18N
    private static final String PROP_XJC_RUN_CLASSPATH = "jaxbwiz.xjcrun.classpath" ;//NOI18N
    private static final String PROP_JAXB_GEN_SRC_CLASSPATH = "jaxbwiz.gensrc.classpath";//NOI18N
    private static final String PROP_VAL_JAXB_LIB_CLASSPATH = "${libs.jaxb.classpath}";//NOI18N
    private static final String JAXB_CONTEXT_CLASS_RES_PATH = "javax/xml/bind/JAXBContext.class"; //NOI18N
    private static final String CLASSPATH_ENDORSED = "classpath/endorsed"; //NOI18N
    private static final String JAXB_ENDORSED="JAXB-ENDORSED"; //NOI18N

    public static final String IDE_MODULE_INSTALL_NAME = "modules/org-netbeans-modules-xml-wsdl-model.jar"; // NOI18N
    public static final String IDE_MODULE_INSTALL_CBN = "org.netbeans.modules.xml.wsdl.model"; // NOI18N

    /**
     * Property that disables CoS support in Java
     */
    public static final String JAXB_COMPILE_ON_SAVE = "compile.on.save.unsupported.jaxb"; // NOI18N
    
    // Make sure nobody instantiates this class.
    private ProjectHelper(){ }
    
    public static void refreshBuildScript(Project prj) {
        try {
            Source xmlSource = new StreamSource(getXMLBindingConfigFile(prj));
            Source xslSource = null;
            int projType = getProjectType(prj);
            if (projType == PROJECT_TYPE_EJB){
                xslSource = new StreamSource(
                    ProjectHelper.class.getClassLoader().getResourceAsStream(
                    EJB_XSL_RESOURCE));
            } else if (projType == PROJECT_TYPE_WEB){
                xslSource = new StreamSource(
                    ProjectHelper.class.getClassLoader().getResourceAsStream(
                    WEB_XSL_RESOURCE));
            } else {
                xslSource = new StreamSource(
                    ProjectHelper.class.getClassLoader().getResourceAsStream(
                    XSL_RESOURCE));
            }

            Result result = new StreamResult(getXMLBindingBuildFile(prj));
            TransformerFactory fact = TransformerFactory.newInstance();
            try {
                fact.setAttribute("indent-number", 4); //NOI18N
            } catch (Exception ex){
                //Ignore Xalan does not recognize "indent-number"
            }
            Transformer xformer = fact.newTransformer(xslSource);
            xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
            xformer.setOutputProperty(OutputKeys.METHOD, "xml"); //NOI18N
            xformer.transform(xmlSource, result);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static String getProperty(Project prj, String filePath, 
            String name) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        String str = null;
        String value = ep.getProperty(name);
        if (value != null) {
            PropertyEvaluator pe = aph.getStandardPropertyEvaluator();
            str = pe.evaluate(value);
        }
        return str;
    }

    private static void saveProperty(Project prj, String filePath, String name, 
            String value) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        if (value != null) {
            ep.put(name, value);
            aph.putProperties(filePath, ep);
        }
    }

    private static String getProjectProperty(Project prj, String prop) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }

    private static String getPrivateProperty(Project prj, String prop) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }

    private static void savePrivateProperty(Project prj, String prop, 
            String value) {
        saveProperty(prj, AntProjectHelper.PRIVATE_PROPERTIES_PATH, prop, 
                value);
    }

    private static void saveProjectProperty(Project prj, String prop, 
            String value) {
        saveProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop, 
                value);
    }

    public static String getProjectSourceDir(Project prj) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH,
                PROP_SRC_DIR); 
    }

    public static File getSourceDirectoryFile(Project prj) {
        return new File(getProjectSourceDir(prj));
    }

    public static String getProjectBuildDir(Project prj) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH,
                PROP_BUILD_DIR); 
    }

    public static String getProjectSourceRoot(Project prj) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH,
                PROP_SRC_ROOT); 
    }

    public static File getProjectDirectory(Project prj) {
        return FileUtil.toFile(prj.getProjectDirectory());
    }

    public static String getProjectRelativePath(Project prj, String absPath) {
        String relPath = null;
        if (absPath != null) {
            String projectDirectory = 
                    getProjectDirectory(prj).getAbsolutePath();

            if (absPath.toLowerCase().indexOf(projectDirectory.toLowerCase()) 
                    != -1) {
                relPath = absPath.substring(projectDirectory.length() + 1);
            } else {
                relPath = absPath;
            }
        }

        return relPath;
    }
    
    public static void disableCoS(Project p, final boolean enable) {
        final AntProjectHelper helper = getAntProjectHelper(p);
        if (helper == null) {
            if (enable) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(ProjectHelper.class, "WARN_couldNotEnableCoS"),
                        StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            }
            return;
        }
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    if (enable) {
                        ep.setProperty(JAXB_COMPILE_ON_SAVE, Boolean.TRUE.toString());
                    } else {
                        ep.remove(JAXB_COMPILE_ON_SAVE);
                    }
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    
                    return null;
                }
            });
        } catch (MutexException ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }

    private static void addJAXBLibrary(Project prj) {
        SourceGroup[] sgs = ProjectUtils.getSources(prj).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath compileClassPath = ClassPath.getClassPath(sgs[0]
                .getRootFolder(), ClassPath.COMPILE);
        ClassPath bootClassPath = ClassPath.getClassPath(sgs[0]
                .getRootFolder(), ClassPath.BOOT);
        ClassPath classPath = ClassPathSupport.createProxyClassPath(
                new ClassPath[]{compileClassPath, bootClassPath});
        FileObject jaxbClass = classPath.findResource(
                JAXB_CONTEXT_CLASS_RES_PATH); 
        if (jaxbClass == null) {
            // Add JAXB jars if not in the classpath
            Library jaxbLib = LibraryManager.getDefault().getLibrary(
                    JAXB_LIB_NAME);
            Sources srcs = ProjectUtils.getSources(prj);
            if (srcs != null) {
                SourceGroup[] srg = srcs.getSourceGroups(
                        JavaProjectConstants.SOURCES_TYPE_JAVA);
                if ((srg != null) && (srg.length > 0)) {
                    try {
                        ProjectClassPathModifier.addLibraries(
                                new Library[]{jaxbLib}, srg[0].getRootFolder(), 
                                ClassPath.COMPILE);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    public static int getProjectType(Project prj) {
        String prjClzName = prj.getClass().getName();
        int prjType = PROJECT_TYPE_J2SE;
        if (prjClzName.indexOf("EjbJarProject") != -1) {//NOI18N
            prjType = PROJECT_TYPE_EJB;
        } else if (prjClzName.indexOf("WebProject") != -1) {//NOI18N
            prjType = PROJECT_TYPE_WEB;
        }

        return prjType;
    }

    public static Schemas getXMLBindingSchemas(Project prj) {
        Schemas scs = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            String projName = fo.getName();
            File projDir = FileUtil.toFile(fo);
            File configFile = null;
            try {
                configFile = new File(projDir, NBPROJECT_DIR + File.separator 
                        + XML_BINDING_CONFIG_FILE_NAME);
                if (configFile.exists()) {
                    scs = Schemas.read(configFile);
                } else {
                    scs = new Schemas();
                    Lookup lookup = prj.getLookup();
                    if (lookup != null) {
                        ProjectInformation pi = lookup.lookup(
                                ProjectInformation.class);
                        if (pi != null) {
                            projName = pi.getName();
                        }
                    }
                    scs.setProjectName(projName);
                    scs.setDestdir(BUILD_GEN_JAXB_DIR);
                    scs.setVersion(JAXBWizModuleConstants.LATEST_CFG_VERSION);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return scs;
    }

    private static File getXMLBindingBuildFile(Project prj) {
        File buildFile = null;

        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            File projDir = FileUtil.toFile(fo);

            try {
                buildFile = new File(projDir, NBPROJECT_DIR + File.separator 
                        + XML_BINDING_BUILD_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return buildFile;
    }

    private static File getXMLBindingConfigFile(Project prj) {
        File configFile = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            File projDir = FileUtil.toFile(fo);

            try {
                configFile = new File(projDir, NBPROJECT_DIR + File.separator 
                        + XML_BINDING_CONFIG_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return configFile;
    }

    private static void saveXMLBindingSchemas(Project prj, Schemas scs) {
        try {
            File configFile = getXMLBindingConfigFile(prj);
            if (configFile != null) {
                scs.write(configFile);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static XjcOptions populateXjcOptions(WizardDescriptor wiz) {
        XjcOptions xjcOpts = new XjcOptions();
        Map<String, Boolean> options = (Map<String, Boolean>) wiz.getProperty(
                JAXBWizModuleConstants.XJC_OPTIONS);
        if (options != null) {
            Set<String> keys = options.keySet();
            Iterator<String> itr = keys.iterator();
            String key = null;
            Boolean value;
            XjcOption xjcOption = null;

            while (itr.hasNext()) {
                key = itr.next();
                value = options.get(key);
                xjcOption = new XjcOption();
                xjcOption.setName(key);
                xjcOption.setValue(value.toString());
                xjcOpts.addXjcOption(xjcOption);
            }
        }
        return xjcOpts;
    }

    public static FileObject retrieveResource(FileObject targetFolder, 
            URI source){
        Retriever retriever = Retriever.getDefault();
        FileObject result = null;
        try {
            result = retriever.retrieveResource(targetFolder, source);
        } catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (result == null) {
            // XXX TODO Handle or log exception.
            // Map map = retriever.getRetrievedResourceExceptionMap();
        }
        return result;
    }

    private static String replace(String str, String replace,
            String replaceWith){
        return str.replaceFirst(replace, replaceWith);
    }
    
    private static Map<String, String> getOrigNewLocationMap(Schema oSchema,
            String newName, String schemasRootFolder){
        Map<String, String> ret = new HashMap<String, String>();
        String replace = schemasRootFolder + "/" + oSchema.getName(); //NOI18N
        String replaceWith = schemasRootFolder + "/" + newName; //NOI18N
        
        SchemaSources ss = oSchema.getSchemaSources();
        SchemaSource[] ssArray = ss.getSchemaSource();
        for (SchemaSource s : ssArray){
            ret.put(s.getOrigLocation(), 
                    replace(s.getLocation(), replace, replaceWith));
        }
        
        Bindings bindings = oSchema.getBindings();
        if (bindings != null){
            Binding[] bs = bindings.getBinding();
            for (Binding b : bs){
                ret.put(b.getOrigLocation(), 
                    replace(b.getLocation(), replace, replaceWith));
            }
        }
        
        Catalog c = oSchema.getCatalog();
        if ((c != null) && (c.getOrigLocation() != null)){
            ret.put(c.getOrigLocation(), 
                    replace(c.getLocation(), replace, replaceWith));
        }
        return ret;
    }
     

    private static void deleteStaleResources(FileObject projRootFo, 
            Map<String, String> map, List<String> files2skip) {

        Set<String> keys = map.keySet();
        FileObject fo = null;
        String relPath = null;
        for (String key: keys){
            relPath = map.get(key);
            if (!files2skip.contains(relPath)){
                fo = projRootFo.getFileObject(relPath);
                if (fo != null){
                    try {
                        fo.delete();
                    } catch (Exception ex){
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    public static Schema importResources(Project project, WizardDescriptor wiz,
            Schema oSchema) throws IOException {
        // If Schema name is changed rename the directory to 
        // new name. We should not over write again Schema, 
        // Binding and catalog files.
        
        Map<String, String> res2Skip = null;
        Map<String, String> tobeRemoved = null;
        List<String> newProjFiles = new ArrayList<String>();
        String oSchemaName = null;

        FileObject projFO = project.getProjectDirectory();
        FileObject projectSchemaDir = getFOProjectSchemaDir(project);        
        File projSchemasDir = FileUtil.toFile(projectSchemaDir);       
        File projDir = FileUtil.toFile(projFO);       
        
        Schema schema = new Schema();
        SchemaSources sss = new SchemaSources();
        SchemaSource ss = null;        
        Bindings bindings = new Bindings(); 
        Binding binding = null;
        Catalog catalog = new Catalog();

        schema.setName((String) wiz.getProperty(
                JAXBWizModuleConstants.SCHEMA_NAME));
        schema.setPackage((String) wiz.getProperty(
                JAXBWizModuleConstants.PACKAGE_NAME));
        schema.setCatalog(catalog);
        schema.setBindings(bindings);
        schema.setSchemaSources(sss);
        schema.setType((String) wiz.getProperty(
                JAXBWizModuleConstants.SCHEMA_TYPE));
        schema.setXjcOptions(populateXjcOptions(wiz));
                        
        // If schema name is changed rename the old directory.
        if (oSchema != null) {
            //FileUtil.getRelativePath(projFO, newFileFO)
            res2Skip = getOrigNewLocationMap(oSchema, schema.getName(),
                    FileUtil.getRelativePath(projFO, projectSchemaDir));
            tobeRemoved = new HashMap<String, String>();
            tobeRemoved.putAll(res2Skip) ;

            oSchemaName = oSchema.getName();

            if (! oSchemaName.equals(schema.getName())){
                File oSchemaDir = new File(projSchemasDir, oSchemaName);
                // Do we need this?
                if (!oSchemaDir.exists()) {
                    // Do not use File.mkdirs();
                    // see  http://www.netbeans.org/servlets/BrowseList?list=nbdev&by=thread&from=827178
                    //oSchemaDir.mkdirs();
                    FileUtil.createFolder(oSchemaDir);
                }

                FileObject oSchemaDirFO = FileUtil.toFileObject(oSchemaDir);
                FileLock fl = null;
                try {
                    fl = oSchemaDirFO.lock();
                    oSchemaDirFO.rename(fl, schema.getName(), null);
                } catch (Exception ex){
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (fl != null){
                        fl.releaseLock();
                    }
                }
            }
        } else {
            res2Skip = new HashMap<String, String>();
            tobeRemoved = new HashMap<String, String>();
        }

        
        File schemaDir = new File(projSchemasDir, schema.getName());
        if (!schemaDir.exists()) {
            //schemaDir.mkdirs();
            FileUtil.createFolder(schemaDir);
        }
        
        FileObject schemaDirFO = FileUtil.toFileObject(schemaDir);
        File srcFile = null;
        File targetFile = null;
        FileObject newFileFO = null;
        String url = null;
        URL remoteSchema = null;

        List<String> xsdFileList = (List<String>) wiz.getProperty(
                JAXBWizModuleConstants.XSD_FILE_LIST );

        List<String> bindingFileList = (List<String>) wiz.getProperty(
                JAXBWizModuleConstants.JAXB_BINDING_FILES);

        String catlogFile = (String) wiz.getProperty(
                JAXBWizModuleConstants.CATALOG_FILE);
        
        boolean srcLocTypeUrl = JAXBWizModuleConstants.SRC_LOC_TYPE_URL.equals(
                (String) wiz.getProperty(
                JAXBWizModuleConstants.SOURCE_LOCATION_TYPE));
        if (xsdFileList != null){
            // Schema files 
            for (int i = 0; i < xsdFileList.size(); i++) {
                if (srcLocTypeUrl) {
                    // URL
                    url = xsdFileList.get(i);
                    ss = new SchemaSource();
                    ss.setOrigLocation(url);
                    ss.setOrigLocationType(
                            JAXBWizModuleConstants.SRC_LOC_TYPE_URL);
                    sss.addSchemaSource(ss);                        
                    tobeRemoved.remove(url);
                    if (res2Skip.get(url) == null){
                        remoteSchema = new URL(url);
                        try {
                            newFileFO = retrieveResource(schemaDirFO, 
                                    remoteSchema.toURI());
                        } catch (URISyntaxException ex) {
                            throw new IOException(ex.getMessage());
                        }
                        ss.setLocation(FileUtil.getRelativePath(projFO,
                                newFileFO));
                        newProjFiles.add(FileUtil.getRelativePath(projFO,
                                newFileFO));
                    } else {
                        ss.setLocation(res2Skip.get(url));
                        newProjFiles.add(res2Skip.get(url));
                    }
                } else {
                    // Local file
                    ss = new SchemaSource();
                    ss.setOrigLocation(xsdFileList.get(i));
                    sss.addSchemaSource(ss);                        
                    tobeRemoved.remove(xsdFileList.get(i));
                    if (res2Skip.get(xsdFileList.get(i)) == null){
                        //srcFile = new File(xsdFileList.get(i));
                        srcFile = FileSysUtil.Relative2AbsolutePath(projDir, 
                                xsdFileList.get(i));                        
                        targetFile = new File(schemaDir, srcFile.getName());
                        if (targetFile.exists()) {
                            targetFile.delete();
                        }
                        newFileFO = retrieveResource(schemaDirFO,
                                srcFile.toURI());
                        ss.setLocation(FileUtil.getRelativePath(projFO, 
                                newFileFO));
                        newProjFiles.add(FileUtil.getRelativePath(projFO, 
                                newFileFO));
                    } else {
                        ss.setLocation(res2Skip.get(xsdFileList.get(i)));
                        newProjFiles.add(res2Skip.get(xsdFileList.get(i)));
                    }
                }
            }            
        }

        if (bindingFileList != null){        
            // Binding files
            for (int i = 0; i < bindingFileList.size(); i++) {
                // All binding files are from local sources
                // Assumes there is not name conflict between other binding and 
                // Schema files.
                binding = new Binding();
                binding.setOrigLocation(bindingFileList.get(i));
                bindings.addBinding(binding);                    
                tobeRemoved.remove(bindingFileList.get(i));

                if (res2Skip.get(bindingFileList.get(i)) == null){
                    //srcFile = new File(bindingFileList.get(i));
                    srcFile = FileSysUtil.Relative2AbsolutePath(projDir,
                            bindingFileList.get(i));
                    targetFile = new File(schemaDir, srcFile.getName());
                    if (targetFile.exists()) {
                        targetFile.delete();
                    }

                    newFileFO = retrieveResource(schemaDirFO, srcFile.toURI());

                    binding.setLocation(FileUtil.getRelativePath(projFO, 
                            newFileFO));
                    newProjFiles.add(FileUtil.getRelativePath(projFO, 
                            newFileFO));
                } else {
                    binding.setLocation(res2Skip.get(bindingFileList.get(i)));
                    newProjFiles.add(res2Skip.get(bindingFileList.get(i)));
                }
            }
        }
        
        //Catalog file
        if (catlogFile != null){
            catalog.setOrigLocation(catlogFile);
            tobeRemoved.remove(catlogFile);

            if (res2Skip.get(catlogFile) == null){
                //srcFile = new File(catlogFile);
                srcFile = FileSysUtil.Relative2AbsolutePath(projDir, 
                        catlogFile);
                targetFile = new File(schemaDir, srcFile.getName());
                if (targetFile.exists()) {
                    targetFile.delete();
                }

                newFileFO = retrieveResource(schemaDirFO, srcFile.toURI());            
                catalog.setLocation(FileUtil.getRelativePath(projFO,
                    newFileFO));
                newProjFiles.add(FileUtil.getRelativePath(projFO,
                    newFileFO));
            } else {
                catalog.setLocation(res2Skip.get(catlogFile));
                newProjFiles.add(res2Skip.get(catlogFile));
            }
        }
        
        deleteStaleResources(projFO, tobeRemoved, newProjFiles);
        return schema;
    }

    public static AntProjectHelper getAntProjectHelper(Project project) {
        try {
            Method getAntProjectHelperMethod = project.getClass().getMethod(
                    "getAntProjectHelper"); //NOI18N
            if (getAntProjectHelperMethod != null) {
                AntProjectHelper helper = (AntProjectHelper) 
                        getAntProjectHelperMethod.invoke(project);

                return helper;
            }
        } catch (NoSuchMethodException nme) {
            // somewhat expected for non-ant projects
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public static FileObject getFOForProjectBuildFile(Project prj) {
        FileObject buildFileFo = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            buildFileFo = fo.getFileObject("build.xml"); //NOI18N
        }
        return buildFileFo;
    }

    public static FileObject getFOForNBProjectDir(Project prj) {
        return prj.getProjectDirectory().getFileObject(NBPROJECT_DIR);
    }

    public static FileObject getFOForBindingConfigFile(Project prj) {
        FileObject configFile = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();

            try {
                fo.getFileObject(NBPROJECT_DIR + FILE_OBJECT_SEPARATOR).refresh();
                configFile = fo.getFileObject(NBPROJECT_DIR 
                        + FILE_OBJECT_SEPARATOR + XML_BINDING_CONFIG_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return configFile;
    }

    public static FileObject getFOForBindingBuildFile(Project prj) {
        FileObject buildFileFo = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            try {
                fo.getFileObject(NBPROJECT_DIR + FILE_OBJECT_SEPARATOR).refresh();                
                buildFileFo = fo.getFileObject(NBPROJECT_DIR 
                        + FILE_OBJECT_SEPARATOR + XML_BINDING_BUILD_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return buildFileFo;
    }

    private static void createDirs(FileObject rootDir, String relDir)  {
        File fileRootDir = FileUtil.toFile(rootDir);
        File fileRelDirs = new File(fileRootDir, relDir);
        if (!fileRelDirs.exists()) {
            // Do not use fileRelDirs.mkdirs();
            // see  http://www.netbeans.org/servlets/BrowseList?list=nbdev&by=thread&from=827178
            try {
                FileUtil.createFolder(rootDir, relDir);
            } catch (IOException ex){
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static FileObject getFOProjectSchemaDir(Project project) {
        String srcDirStr = null;
        FileObject foSchemaDir = null;
        FileObject foProjDir = project.getProjectDirectory();
        if ((getProjectType(project) == PROJECT_TYPE_EJB) 
                || (getProjectType(project) == PROJECT_TYPE_WEB)) {
            srcDirStr = getProjectSourceRoot(project);
            FileObject srcDir = foProjDir.getFileObject(srcDirStr);
            createDirs(srcDir, NON_JAVA_SE_CONFIG_DIR);
            foSchemaDir = srcDir.getFileObject(NON_JAVA_SE_CONFIG_DIR);//NOI18N
        } else {
            FileObject srcDir = foProjDir;
            createDirs(srcDir, JAVA_SE_CONFIG_DIR);
            foSchemaDir = srcDir.getFileObject(JAVA_SE_CONFIG_DIR); //NOI18N
        }
        return foSchemaDir;
    }
    
    private static void addClasspathProperties(Project prj){
        String xjcClasspath = getProjectProperty(prj, PROP_XJC_DEF_CLASSPATH);
        boolean modified = false;
        if ((xjcClasspath == null) || ("".equals(xjcClasspath))){
            saveProjectProperty(prj, PROP_XJC_DEF_CLASSPATH, 
                    PROP_VAL_JAXB_LIB_CLASSPATH);
            modified = true;
        }

        String xjcRunClasspath = getProjectProperty(prj, PROP_XJC_RUN_CLASSPATH);
        if ((xjcRunClasspath == null) || ("".equals(xjcRunClasspath))){
            saveProjectProperty(prj, PROP_XJC_RUN_CLASSPATH, 
                    PROP_VAL_JAXB_LIB_CLASSPATH);
            modified = true;
        }
        
        String jaxbGenSrcClasspath = getProjectProperty(prj, 
                PROP_JAXB_GEN_SRC_CLASSPATH);
        if ((jaxbGenSrcClasspath == null) || ("".equals(jaxbGenSrcClasspath))){
            saveProjectProperty(prj, PROP_JAXB_GEN_SRC_CLASSPATH, 
                    PROP_VAL_JAXB_LIB_CLASSPATH);
            modified = true;
        }
        
        if (modified){
            try{
                ProjectManager.getDefault().saveProject(prj);
            }catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    public static Schema addSchema(Project project, Schemas scs, Schema schema){
        try {
            scs.addSchema(schema);
            saveXMLBindingSchemas(project, scs);
            refreshBuildScript(project);
            addClasspathProperties(project);

            // Register our build XML file, if not already.
            // http://wiki.netbeans.org/wiki/view/BuildScriptExtensibility
            // http://www.netbeans.org/issues/show_bug.cgi?id=93509
            AntBuildExtender ext = project.getLookup().lookup(
                    AntBuildExtender.class);
            if (ext != null && ext.getExtension(JAXB_ANT_XTN_NAME) == null) {
                FileObject jaxbBuildXml = getFOForBindingBuildFile(project);
                AntBuildExtender.Extension jaxbBuild = ext.addExtension( 
                        JAXB_ANT_XTN_NAME, jaxbBuildXml); 
                jaxbBuild.addDependency(
                        JAXBWizModuleConstants.JAXB_COMPILE_TARGET_DEPENDS,
                        JAXBWizModuleConstants.JAXB_COMPILE_TARGET);
                ProjectManager.getDefault().saveProject(project);
                String buildDir = getProjectBuildDir(project);
                if (project.getProjectDirectory() != null){
                    FileObject buildFo = project.getProjectDirectory().getFileObject(buildDir);
                    if (buildFo != null){
                        buildFo.refresh();
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return schema;
    }

    /**
     * Returns list of String Schema/Binding names used in the project.
     **/
    public static List<String> getSchemaNames(Project project) {
        List<String> ret = new ArrayList<String>();
        Schemas scs = getXMLBindingSchemas(project);
        if (scs != null) {
            Schema[] schemas = scs.getSchema();
            for (Schema schema : schemas) {
                ret.add(schema.getName());
            }
        }
        return ret;
    }

    public static void removeSchema(Project project, Schemas scs, 
            Schema schema){
        try {
            scs.removeSchema(schema);
            saveXMLBindingSchemas(project, scs);
            refreshBuildScript(project);
            
            if (scs.sizeSchema() == 0) {
                disableCoS(project, false);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void compileXSDs(final Project project) {
        compileXSDs(project, false);
    }

    public static void compileXSDs(final Project project, 
            final boolean addLibs){
        executeAntTarget(project, addLibs, 
                JAXBWizModuleConstants.JAXB_COMPILE_TARGET);
    }

    public static void cleanCompileXSDs(final Project project, 
            final boolean addLibs)
    {
        cleanCompileXSDs(project, addLibs, null);
    }
    
    public static void cleanCompileXSDs(final Project project, 
            final boolean addLibs, TaskListener listener ){
        executeAntTarget(project, addLibs, 
                JAXBWizModuleConstants.JAXB_CLEAN_COMPILE_TARGET, listener );
    }
    
    private static void executeAntTarget(final Project project,
            final boolean addLibs,
            final String antTarget)
    {
        executeAntTarget(project, addLibs, antTarget, null );
    }

    private static void executeAntTarget(final Project project,
            final boolean addLibs,
            final String antTarget, TaskListener listener )
    {
        final ProgressHandle progressHandle = ProgressHandleFactory
                .createHandle(NbBundle.getMessage(ProjectHelper.class, 
                "MSG_JAXB_PROGRESS")); //NOI18N;
        progressHandle.start();

        Runnable run = new Runnable() {
            public void run() {
                try {
                    if (addLibs) {
                        addJAXBLibrary(project);
                    }

                    FileObject buildXml = getFOForProjectBuildFile(project);
                    String[] args = new String[]{antTarget};
                                        
                    if (buildXml != null) {
                        ExecutorTask task = ActionUtils.runTarget(buildXml,
                                args, null);
                        task.waitFinished();
                        if (task.result() != 0) {
                            String mes = NbBundle.getMessage(
                                    ProjectHelper.class, "MSG_ERROR_COMPILING"); //NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor
                                    .Message(mes, 
                                    NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        }
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    progressHandle.finish();
                }
            }
        };

        Task task = RequestProcessor.getDefault().create(run);
        if ( listener != null ){
            task.addTaskListener(listener);
        }
        task.schedule(0);
    }
    
//    private static boolean isJDK6(final Project prj) {
//        boolean ret = false;
//        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
//        if (jpm != null) {
//            String platForm = getProjectProperty(prj, PLATFORM_ACTIVE);
//            if (DEFAULT_PLATFORM.equals(platForm)) {
//
//                JavaPlatform dflt = jpm.getDefaultPlatform();
//                if (dflt != null) {
//                    if (JDK_1_6.compareTo(dflt.getSpecification().getVersion()) <= 0) {
//                        ret = true;
//                    }
//                }
//            } else {
//                JavaPlatform[] jp = jpm.getInstalledPlatforms();
//                if (jp != null) {
//                    for (JavaPlatform jpi : jp) {
//                        if (jpi.getProperties().get("platform.ant.name").equals( //NOI18N
//                                platForm)) {
//                            SpecificationVersion sv = jpi.getSpecification()
//                                    .getVersion();
//                            if (JDK_1_6.compareTo(sv) <= 0) {
//                                ret = true;
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        return ret;
//    }
    
    public static void addCfgFileChangeListener(Project prj, 
            FileChangeListener l){
        
        FileObject fo = getFOForBindingConfigFile(prj);
        FileChangeListener fcl = null;
        if (fo != null) {
            fcl = FileUtil.weakFileChangeListener(l, fo);
            fo.addFileChangeListener(fcl);
        } else {
            fo = getFOForNBProjectDir(prj);
            if (fo != null) {
                fcl = FileUtil.weakFileChangeListener(l, fo);                
                fo.addFileChangeListener(fcl);
            }
        }
    }

    public static void removeModelListner(Project prj, FileChangeListener l){
        // WeakChangeListener will return true for its proxy.
        FileObject fo = getFOForBindingConfigFile(prj);
        if (fo != null) {
            fo.removeFileChangeListener(l);
        }

        fo = getFOForNBProjectDir(prj);
        if (fo != null) {
            fo.removeFileChangeListener(l);
        }
    }
 
    public static void addModelListener(Project prj, JAXBWizEventListener l){
        if (prj != null){
            JAXBWizModel model = prj.getLookup().lookup(
                    JAXBWizModel.class);
            if (model != null){
                JAXBWizEventListener weak = WeakListeners.create(
                        JAXBWizEventListener.class, l, model);
                model.addJAXBWizEventListener(weak);
            } 
        }        
    }
    
    public static void removeModelListener(Project prj, JAXBWizEventListener l){
        if (prj != null){
            JAXBWizModel model = prj.getLookup().lookup(
                    JAXBWizModel.class);
            if (model != null){
                // WeakChangeListener will return true for its proxy.                
                model.removeJAXBWizEventListener(l);
            }
        }        
    }

    public static void addSchema2Model(Project prj, Schema schema){
        if (prj != null){
            JAXBWizModelImpl model = (JAXBWizModelImpl) prj.getLookup().lookup(
                    JAXBWizModel.class);
            if (model != null){
                model.addSchema(schema);
            }
        }                
    }
    
    public static void changeSchemaInModel(Project prj, Schema os, Schema ns){
        if (prj != null){
            JAXBWizModelImpl model = (JAXBWizModelImpl) prj.getLookup().lookup(
                    JAXBWizModel.class);
            if (model != null){
                model.changeSchema(os, ns);
            }
        }                
    }    

    public static void deleteSchemaFromModel(Project prj, Schema os){
        if (prj != null){
            JAXBWizModelImpl model = (JAXBWizModelImpl) prj.getLookup().lookup(
                    JAXBWizModel.class);
            if (model != null){
                model.deleteSchema(os);
            }
        }                
    }        

    public static void checkAndDeregisterScript(Project prj){
        if (prj != null){
            Schemas ss = getXMLBindingSchemas(prj);
            if ((ss == null) || (ss.sizeSchema() == 0)){
                AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
                if (ext != null && ext.getExtension(JAXB_ANT_XTN_NAME) != null) {
                    ext.removeExtension(JAXB_ANT_XTN_NAME);
                    try {
                        ProjectManager.getDefault().saveProject(prj);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    public static void cleanupLocalSchemaDir(Project project, Schema schema){
        FileObject projectSchemasDir = getFOProjectSchemaDir(project);        
        FileObject schemaDir = projectSchemasDir.getFileObject(
                schema.getName());
        if (schemaDir != null){
            try {
                schemaDir.delete();
            } catch (Exception ex){
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static void migrateProjectFromPreDot5Version(Project prj){
        // String oldJaxbLibName = "jaxb21" ; //NOI18N
        // Remove Old library. Can't for now, need an API for removing non existent lib
        //Add new library
        addJAXBLibrary(prj);
        
        //Update JAXB Wiz properties.
        saveProjectProperty(prj, PROP_XJC_DEF_CLASSPATH, 
                PROP_VAL_JAXB_LIB_CLASSPATH);
        saveProjectProperty(prj, PROP_XJC_RUN_CLASSPATH, 
                PROP_VAL_JAXB_LIB_CLASSPATH);
        saveProjectProperty(prj, PROP_JAXB_GEN_SRC_CLASSPATH, 
                PROP_VAL_JAXB_LIB_CLASSPATH);        
        
        try {
            ProjectManager.getDefault().saveProject(prj);
            Schemas scs = ProjectHelper.getXMLBindingSchemas(prj);
            ProjectHelper.refreshBuildScript(prj);
            scs.setVersion(JAXBWizModuleConstants.LATEST_CFG_VERSION);
            ProjectHelper.saveXMLBindingSchemas(prj, scs);
            disableCoS(prj, scs.sizeSchema() != 0);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void addJaxbApiEndorsed(Project prj) throws IOException {
        FileObject srcRoot = getSourceRoot(prj);
        if (srcRoot != null) {
            addJaxbApiEndorsed(srcRoot);
        }
    }

    private static void addJaxbApiEndorsed(FileObject srcRoot) throws IOException {
        String sample = "javax/xml/bind/JAXBElement.class"; // NOI18N
        ClassPath classPath = ClassPath.getClassPath(srcRoot, CLASSPATH_ENDORSED);
        if (classPath == null || classPath.findResource(sample) == null) {
            Library jaxbApiLib = LibraryManager.getDefault().getLibrary(JAXB_ENDORSED);
            assert jaxbApiLib != null : "JAXB endorsed library must be present";
            try {
                if (jaxbApiLib == null) {
                    throw  new UnsupportedOperationException("No JAXB endorsed library found in Libraries.");
                }
                ProjectClassPathModifier.addLibraries(new Library[] {jaxbApiLib}, srcRoot, CLASSPATH_ENDORSED);
            } catch (UnsupportedOperationException x) {
                classPath = ClassPath.getClassPath(srcRoot, ClassPath.BOOT);
                if (classPath == null || classPath.findResource(sample) == null) {
                    Logger.getLogger(ProjectHelper.class.getName()).log(Level.WARNING, "Could not add JAXB at all: {0}", x.getMessage());
                } else {
                    Logger.getLogger(ProjectHelper.class.getName()).log(Level.WARNING, "Could not add JAXB override: {0}", x.getMessage());
                }
            }
        }
    }

    private static FileObject getSourceRoot(Project prj) {
        Sources sources = ProjectUtils.getSources(prj);
        if (sources != null) {
            SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (srcGroups != null && srcGroups.length>0) {
                return srcGroups[0].getRootFolder();
            }
        }
        return null;
    }

    private static Library createJaxbApiLibrary() throws IOException {
        List<URL> apiJars = getJaxbApiJars();
        if (apiJars.size() > 0) {
            Map<String, List<URL>> map = Collections.<String, List<URL>>singletonMap("classpath", apiJars); //NOI18N
            return LibraryManager.getDefault().createLibrary("j2se", JAXB_ENDORSED, map); //NOI18N
        }
        return null;
    }

    private static List<URL> getJaxbApiJars() throws IOException {
        List<URL> urls = new ArrayList<URL>();
        File apiJar = InstalledFileLocator.getDefault().locate("modules/ext/jaxb/api/jaxb-api.jar", null, false); // NOI18N
        if (apiJar != null) {
            URL url = apiJar.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                urls.add(FileUtil.getArchiveRoot(url));
            }
        }
        return urls;
    }
}
