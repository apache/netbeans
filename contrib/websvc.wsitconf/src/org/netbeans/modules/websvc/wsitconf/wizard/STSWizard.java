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
package org.netbeans.modules.websvc.wsitconf.wizard;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitconf.util.Util;

import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.openide.WizardDescriptor;

import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 * Wizard to create a new STS Web service.
 * @author Martin Grebac
 */
public class STSWizard implements TemplateWizard.Iterator {

    private Project project;
    private static final Logger logger = Logger.getLogger(STSWizard.class.getName());
    private static final String SERVICENAME_TAG = "__SERVICENAME__"; //NOI18N
    private WsdlModeler wsdlModeler;
    private WsdlModel wsdlModel;
    private WsdlService service;
    private WsdlPort port;

    /** Create a new wizard iterator. */
    public STSWizard() {
    }

    public static STSWizard create() {
        return new STSWizard();
    }

    public Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException {
        File tempFolder = new File(System.getProperty("netbeans.user"));     //NOI18N
        DataObject folderDO = DataObject.find(FileUtil.toFileObject(tempFolder));

        final String fileName;
        if (ConfigVersion.CONFIG_1_0.equals(wiz.getProperty("cfgVersion"))) {
            fileName = "sts";
        } else {
            fileName = "sts13";
        }

        final String userDir = System.getProperty("netbeans.user") + File.separator + "config" +
                File.separator + "WebServices" + File.separator;
        final File userDirF = new File(userDir);
        
        logger.log(Level.FINE, "USERDIR: " + userDir);

        FileUtil.runAtomicAction(new Runnable() {
            public void run() {
                try {
                    FileUtil.createFolder(userDirF);
                } catch (IOException ex) { // do not care - this is thrown if the file already exists
                    // ignore
                }
            }
        });

        FileUtil.runAtomicAction(new Runnable() {
            public void run() {
                OutputStream schemaos = null;
                InputStream schemaIS = null;
                try {
                    FileObject wsdlFolder = FileUtil.toFileObject(userDirF);
                    schemaIS = this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/websvc/wsitconf/resources/templates/sts_schema.template"); //NOI18N
                    FileObject schema = null;
                    try {
                        schema = wsdlFolder.createData("sts_schema.xsd");
                    } catch (IOException ex) { // do not care - this is thrown if the file already exists
                        logger.log(Level.FINE, null, ex);
                        logger.log(Level.FINE, "schema: " + schema);
                        schema = wsdlFolder.getFileObject("sts_schema.xsd");
                    }
                    schemaos = schema.getOutputStream();
                    FileUtil.copy(schemaIS, schemaos);
                } catch (FileNotFoundException ex) {
                    logger.log(Level.INFO, null, ex);
                } catch (IOException ex) {
                    logger.log(Level.INFO, null, ex);
                } finally {
                    if (schemaos != null) {
                        try {
                            schemaos.close();
                        } catch (IOException ex) {
                            logger.log(Level.INFO, null, ex);
                        }
                    }
                    if (schemaIS != null) {
                        try {
                            schemaIS.close();
                        } catch (IOException ex) {
                            logger.log(Level.INFO, null, ex);
                        }
                    }
                }
            }
        });

        FileUtil.runAtomicAction(new Runnable() {
            public void run() {
                String serviceName = Templates.getTargetName(wiz);// + NbBundle.getMessage(STSWizard.class, "LBL_ServiceEnding"); //NOI18N

                FileObject wsdlFolder = FileUtil.toFileObject(userDirF);
                FileObject wsdlFO = null;
                try {
                    OutputStream wsdlos = null;
                    InputStream wsdlIS = null;
                    try {
                        wsdlIS = this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/websvc/wsitconf/resources/templates/" + fileName + ".template"); //NOI18N

                        try {
                            wsdlFO = wsdlFolder.createData(fileName, "wsdl");
                        } catch (IOException ex) { // do not care - this is thrown if the file already exists
                            logger.log(Level.FINE, null, ex);
                            logger.log(Level.FINE, "wsdl: " + wsdlFO);
                            wsdlFO = wsdlFolder.getFileObject(fileName, "wsdl");
                        }
                        wsdlos = wsdlFO.getOutputStream();
                        FileUtil.copy(wsdlIS, wsdlos);
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.INFO, null, ex);
                    } catch (IOException ex) {
                        logger.log(Level.INFO, null, ex);
                    } finally {
                        if (wsdlos != null) {
                            try {
                                wsdlos.close();
                            } catch (IOException ex) {
                                logger.log(Level.INFO, null, ex);
                            }
                        }
                        if (wsdlIS != null) {
                            try {
                                wsdlIS.close();
                            } catch (IOException ex) {
                                logger.log(Level.INFO, null, ex);
                            }
                        }
                    }

                    String newName = serviceName;
                    FileObject newFO = null;

                    InputStream fi = null;
                    OutputStream fo = null;

                    try {
                        File f = new File(FileUtil.toFile(wsdlFolder).getAbsolutePath(), newName + ".wsdl");
                        f.createNewFile();
                        fo = new FileOutputStream(f);
                        fi = wsdlFO.getInputStream();
                        FileUtil.copy(fi, fo);
                        newFO = FileUtil.toFileObject(f);
                    //newFO = FileUtil.copyFile(wsdlFO, wsdlFolder, newName);
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.INFO, null, ex);
                    } catch (IOException ex) {
                        logger.log(Level.INFO, null, ex);
                    } finally {
                        try {
                            if (fi != null) {
                                fi.close();
                            }
                            if (fo != null) {
                                fo.close();
                            }
                        } catch (IOException ex) {
                            logger.log(Level.INFO, null, ex);
                        }
                    }

                    File newFile = FileUtil.toFile(newFO);
                    final URL wsdlURL = newFile.toURI().toURL();

                    wiz.putProperty(WizardProperties.WSDL_FILE_PATH, newFile.getPath());

                    BufferedWriter writer = null;
                    try {
                        List<String> lines = wsdlFO.asLines();
                        writer = new BufferedWriter(new FileWriter(newFile));

                        for (String line : lines) {
                            if ((index = line.indexOf(SERVICENAME_TAG)) != -1) {
                                line = line.replaceAll(SERVICENAME_TAG, serviceName);
                            }
                            writer.write(line);
                            writer.newLine();
                        }
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.INFO, null, ex);
                    } catch (IOException ex) {
                        logger.log(Level.INFO, null, ex);
                    } finally {
                        try {
                            if (writer != null) {
                                writer.flush();
                                writer.close();
                            }
                        } catch (IOException ex) {
                            logger.log(Level.INFO, null, ex);
                        }
                    }

                    wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlURL);
                    wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                        public void modelCreated(WsdlModel model) {
                            wsdlModel = model;
                            if (wsdlModel == null) {
                                try {
                                    WsdlServiceHandler.parse(wsdlURL.toExternalForm());
                                } catch (ParserConfigurationException ex) {
                                    logger.log(Level.FINE, null, ex);
                                } catch (SAXException ex) {
                                    logger.log(Level.FINE, null, ex);
                                } catch (IOException ex) {
                                    logger.log(Level.FINE, null, ex);
                                }
                            } else {
                                List services = wsdlModel.getServices();
                                if (services != null && !services.isEmpty()) {
                                    service = (WsdlService) services.get(0);
                                    List ports = service.getPorts();
                                    if (ports != null && !ports.isEmpty()) {
                                        port = (WsdlPort) ports.get(0);
                                    }
                                }
                            }
                        }
                    });

                    int timeout = 10000;
                    while ((service == null) && (timeout > 0)) {
                        try {
                            Thread.sleep(200);
                            timeout -= 200;
                        } catch (InterruptedException ex) {
                            //                ex.printStackTrace();
                        }
                    }

                    if (service != null) {
                        wiz.putProperty(WizardProperties.WSDL_SERVICE, service);
                        wiz.putProperty(WizardProperties.WSDL_PORT, port);
                        wiz.putProperty(WizardProperties.WSDL_MODELER, wsdlModeler);
                        new STSWizardCreator(project, wiz).createSTS();
                    }
                } catch (MalformedURLException ex) {
                    logger.log(Level.INFO, null, ex);
                }
            }
        });

        return Collections.singleton(folderDO);
    }
    private transient int index;
    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private transient TemplateWizard wiz;

    private transient STSVersionPanel versionPanel = null;
    
    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;

        project = Templates.getProject(wiz);

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory()); // only web module supported for now
        boolean wizardEnabled = Util.isJavaEE5orHigher(project) && ServerUtils.isGlassfish(project) && (wm != null) && (JAXWSSupport.getJAXWSSupport(project.getProjectDirectory()) != null);

        versionPanel = new STSVersionPanel(wiz);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        WizardDescriptor.Panel firstPanel; //special case: use Java Chooser
        if (sourceGroups.length == 0) {
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, versionPanel), wizardEnabled);
        } else {
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, versionPanel), wizardEnabled);
        }
        JComponent comp = (JComponent) firstPanel.getComponent();
        Util.changeLabelInComponent(comp, NbBundle.getMessage(STSWizard.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"),
                NbBundle.getMessage(STSWizard.class, "LBL_Webservice_Name"));
        Util.hideLabelAndLabelFor(comp, NbBundle.getMessage(STSWizard.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));

        panels = new WizardDescriptor.Panel[]{
                    firstPanel,
                };

        // Creating steps.
        Object prop = this.wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);

        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    public void uninitialize(TemplateWizard wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardProperties.WEB_SERVICE_TYPE, null);
        }
        versionPanel = null;
        panels = null;
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }

    public String name() {
        return MessageFormat.format(NbBundle.getMessage(STSWizard.class, "LBL_WizardStepsCount"),
                new String[]{(Integer.valueOf(index + 1)).toString(), Integer.valueOf(panels.length).toString()}); //NOI18N
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }
}
