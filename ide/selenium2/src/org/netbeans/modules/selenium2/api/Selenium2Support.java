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
package org.netbeans.modules.selenium2.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.selenium2.spi.Selenium2SupportImpl;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class Selenium2Support {

    public static final String SELENIUM_FOLDER_NAME = "test";          //NOI18N
    public static final String SELENIUM_LIBRARY_NAME = "Selenium2";         //NOI18N
    public static final String DEFAULT_SERVER_PORT = "80"; // NOI18N
    public static final String DEFAULT_SELENIUM_SERVER_PORT = "4444"; //NOI18N
    public static final String SELENIUM_TESTCLASS_NAME_SUFFIX = TestCreatorProvider.INTEGRATION_TEST_CLASS_SUFFIX;
    
    private static Lookup.Result<Selenium2SupportImpl> implementations;
    /** Cache of all available Selenium2SupportImpl instances. */
    private static List<Selenium2SupportImpl> cache;
    
    private Selenium2Support() {
    }

    /**
     * Look for instance of Selenium2SupportImpl supporting given project
     * in the default lookup
     *
     * @param p project to query for Selenium 2 support
     * @return Selenium2SupportImpl instance for given project; null if there's not any
     */
    public static final Selenium2SupportImpl findSelenium2Support(Project p) {
        if(p == null) {
            return null;
        }
        for (Selenium2SupportImpl s: getImplementations()) {
            if (s.isSupportActive(p)) {
                return s;
            }
        }
        return null;
    }
    
    /**
     * Loads a test template.
     * If the template loading fails, displays an error message.
     *
     * @param  templateID  bundle key identifying the template type
     * @return  loaded template, or <code>null</code> if the template
     *          could not be loaded
     */
    public static DataObject loadTestTemplate(String templateID) {
        // get the Test class template
        String path = templateID;
        try {
            FileObject fo = FileUtil.getConfigFile(path);
            if (fo == null) {
                noTemplateMessage(path);
                return null;
            }
            return DataObject.find(fo);
        }
        catch (DataObjectNotFoundException e) {
            noTemplateMessage(path);
            return null;
        }
    }
    
    /**
     * Check whether any implementation supports given FileObjects.
     *
     * @param activatedFOs FileObjects to check
     * @return {@code true} if any instance supports given FileObjects, {@code false} otherwise
     */
    public static boolean isSupportEnabled(FileObject[] activatedFOs) {
        if (activatedFOs == null || activatedFOs.length == 0) {
            return false;
        }
        if (activatedFOs[0] != null && activatedFOs[0].isValid()) {
            Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
            Selenium2SupportImpl selenium2Support = findSelenium2Support(p);
            if(selenium2Support != null) {
                return selenium2Support.isSupportEnabled(activatedFOs);
            }
        }
        return false;
    }
    
    public static void runTests(FileObject[] activatedFOs, boolean isSelenium) {
        if (activatedFOs == null || activatedFOs.length == 0) {
            return;
        }
        if (activatedFOs[0] != null && activatedFOs[0].isValid()) {
            Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
            Selenium2SupportImpl selenium2Support = findSelenium2Support(p);
            if(selenium2Support != null) {
                selenium2Support.runTests(activatedFOs, isSelenium);
            }
        }
    }
    
    public static ArrayList<FileObject> createTests(TestCreatorProvider.Context context) {
        FileObject[] activatedFOs = context.getActivatedFOs();
        ArrayList<FileObject> createdFiles = new ArrayList<>();
        if (activatedFOs[0] != null && activatedFOs.length != 0 && activatedFOs[0].isValid()) {
            Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
            Selenium2SupportImpl selenium2Support = Selenium2Support.findSelenium2Support(p);
            if (selenium2Support != null) {
                FileObject dir = context.getTargetFolder();
                boolean singleClass = context.isSingleClass();
                if (singleClass) {
                    FileObject seleniumTestFile = createSeleniumTestFile(selenium2Support, dir, context.getTestClassName());
                    if (seleniumTestFile != null) {
                        createdFiles.add(seleniumTestFile);
                    }
                } else {
                    ArrayList<FileObject> activatedFOs2 = new ArrayList<>();
                    for (FileObject fo : activatedFOs) {
                        if (fo.isData()) {
                            if (!activatedFOs2.contains(fo)) {
                                activatedFOs2.add(fo);
                            }
                        } else if (fo.isFolder()) {
                            Enumeration<? extends FileObject> children = fo.getChildren(true);
                            while (children.hasMoreElements()) {
                                FileObject child = children.nextElement();
                                if (child.isData() && !activatedFOs2.contains(child)) {
                                    activatedFOs2.add(child);
                                }
                            }
                        }
                    }
                    for (FileObject fo : activatedFOs2) {
                        FileObject seleniumTestFile = createSeleniumTestFile(selenium2Support, dir, selenium2Support.getSourceAndTestClassNames(fo, false, true)[1]);
                        if (seleniumTestFile != null) {
                            createdFiles.add(seleniumTestFile);
                        }
                    }
                }
                if(createdFiles.size() == 1) {
                    EditorCookie ed = createdFiles.get(0).getLookup().lookup(EditorCookie.class);
                    if(ed != null) {
                        ed.open();
                    }
                }
            }
        }
        return createdFiles;
    }
    
    private static FileObject createSeleniumTestFile(Selenium2SupportImpl selenium2Support, FileObject targetFolder, String targetName) {
        try {
            FileObject dir = targetFolder;
            String name = targetName.replace('.', '/');
            int index = name.lastIndexOf('/');
            String pkg = index > -1 ? name.substring(0, index) : "";
            String clazz = index > -1 ? name.substring(index + 1) : name;
            
            // create package if it does not exist
            if (pkg.length() > 0) {
                try {
                    dir = FileUtil.createFolder(targetFolder, pkg);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            DataFolder df = DataFolder.findFolder(dir);
            DataObject dTemplate = Selenium2Support.loadTestTemplate(selenium2Support.getTemplateID());
            Object serverPort = null;
            if (serverPort == null) {
                serverPort = DEFAULT_SERVER_PORT;
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("server_port", serverPort);   //NOI18N
            params.put("selenium_server_port", DEFAULT_SELENIUM_SERVER_PORT);   //NOI18N
            
            DataObject dobj = dTemplate.createFromTemplate(df, clazz, params);
            return dobj.getPrimaryFile();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    @Messages({"# {0} - template file","MSG_template_not_found=Template file {0} was not found. Check the Selenium 2.0 templates in the Template manager."})
    private static void noTemplateMessage(String temp) {
        String msg = Bundle.MSG_template_not_found(temp);     //NOI18N
        NotifyDescriptor descr = new NotifyDescriptor.Message(
                msg,
                NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(descr);
    }

    private static synchronized List<Selenium2SupportImpl> getImplementations() {
        if (implementations == null) {
            implementations = Lookup.getDefault().lookupResult(Selenium2SupportImpl.class);
            implementations.addLookupListener(new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    synchronized (Selenium2Support.class) {
                        cache = null;
                    }
                }
            });
        }
        if (cache == null) {
            cache = new ArrayList<Selenium2SupportImpl>(implementations.allInstances());
        }
        return Collections.unmodifiableList(cache);
    }
    
}
