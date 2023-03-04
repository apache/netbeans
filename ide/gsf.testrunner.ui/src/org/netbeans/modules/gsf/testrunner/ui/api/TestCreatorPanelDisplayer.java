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
package org.netbeans.modules.gsf.testrunner.ui.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.ui.CommonTestsCfgOfCreate;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 * Displays a dialog for creating new test class
 * @author Theofanis Oikonomou
 */
public final class TestCreatorPanelDisplayer {

    private static final TestCreatorPanelDisplayer INSTANCE = new TestCreatorPanelDisplayer();
    private static final RequestProcessor RP = new RequestProcessor(TestCreatorPanelDisplayer.class);

    private TestCreatorPanelDisplayer() {}
    /**
     * Get the default <code>TestCreatorPanelDisplayer</code>
     * @return the default instance
     */
    public static TestCreatorPanelDisplayer getDefault() {
        return INSTANCE;
    }

    /**
     * Displays a dialog for creating new test class
     * @param activatedFOs objects for which the test will be created
     * @param location the location where the new test will be created, can be {@code null}
     * @param testingFramework the framework to be used when the new test will be created, e.g. {@code "JUnit"} or {@code "TestNG"}, can be {@code null}
     */
    public void displayPanel(FileObject[] activatedFOs, Object location, String testingFramework) {
//        TODO - replace this with new parsing.api from tzezula...
//	final DataObject[] modified = DataObject.getRegistry().getModified();
        CommonTestsCfgOfCreate cfg = new CommonTestsCfgOfCreate(activatedFOs);
        boolean isJ2MEProject = isJ2MEProject(activatedFOs);
	cfg.createCfgPanel(false, isJ2MEProject);

	ArrayList<String> testingFrameworks = new ArrayList<String>();
	Collection<? extends Lookup.Item<TestCreatorProvider>> providers = Lookup.getDefault().lookupResult(TestCreatorProvider.class).allItems();
        if (!isJ2MEProject) {
            for (Lookup.Item<TestCreatorProvider> provider : providers) {
                if(provider.getInstance().enable(activatedFOs)) {
                    testingFrameworks.add(provider.getDisplayName());
                }
            }
        }
        if(testingFrameworks.isEmpty()) { //no testing frameworks available
            return;
        }
	cfg.addTestingFrameworks(testingFrameworks);
	cfg.setPreselectedLocation(location);
	cfg.setPreselectedFramework(testingFramework);
	if (!cfg.configure()) {
	    return;
	}
//	saveAll(modified); // #149048
	String selected = cfg.getSelectedTestingFramework();

	for (final Lookup.Item<TestCreatorProvider> provider : providers) {
	    if (provider.getDisplayName().equals(selected)) {
		final TestCreatorProvider.Context context = new TestCreatorProvider.Context(activatedFOs);
		context.setSingleClass(cfg.isSingleClass());
		context.setTargetFolder(cfg.getTargetFolder());
		context.setTestClassName(cfg.getTestClassName());
                context.setIntegrationTests(cfg.isIntegrationTests());
                context.setConfigurationPanelProperties(cfg.getConfigurationPanelProperties());
                final Collection<? extends SourceGroup> createdSourceRoots = cfg.getCreatedSourceRoots();
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        //Todo: display some progress
                        for (SourceGroup sg : createdSourceRoots) {
                            IndexingManager.getDefault().refreshIndexAndWait(sg.getRootFolder().toURL(), null);
                        }
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run() {
                                provider.getInstance().createTests(context);
                            }
                        });
                    }
                });
		cfg = null;
		break;
	    }
	}
    }

    private boolean isJ2MEProject(FileObject[] activatedFOs) {
        FileObject fileObject = activatedFOs[0];
        if (fileObject != null) {
            Project p = FileOwnerQuery.getOwner(fileObject);
            if (p != null) {
                return p.getLookup().lookup(TestCreatorPanelDisplayerProjectServiceProvider.class) != null;
            }
        }
        return false;
    }

    @ProjectServiceProvider(service = TestCreatorPanelDisplayerProjectServiceProvider.class, projectType = "org-netbeans-modules-mobility-project")
    public static class TestCreatorPanelDisplayerProjectServiceProvider {

        public TestCreatorPanelDisplayerProjectServiceProvider(Project p) {
        }
    }

    private void saveAll(Node[] activatedNodes) {
        for(Node node : activatedNodes) {
            SaveCookie saveCookie = node.getLookup().lookup(SaveCookie.class);
            if (saveCookie != null) {
                try {
                    saveCookie.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
