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


package org.netbeans.modules.j2ee.deployment.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.modules.j2ee.deployment.impl.ui.RegistryNodeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;
import org.openide.util.Lookup;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.ConfigBean;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.NetbeansDeployment;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.WebContextRoot;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;


public class Server implements Node.Cookie {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static final String ATTR_NEEDS_FIND_SERVER_UI = "needsFindServerUI";

    private final NetbeansDeployment dep;
    private final Class factoryCls;
    private final String name;
    private final Lookup lkp;
    private final boolean needsFindServerUI;

    /** GuardedBy("this") */
    private DeploymentFactory factory = null;
    /** GuardedBy("this") */
    private DeploymentManager manager = null;
    /** GuardedBy("this") */
    private RegistryNodeProvider nodeProvider = null;


    Server(FileObject fo) throws IOException, ParserConfigurationException,
            SAXException, ClassNotFoundException {

        initDeploymentConfigurationFileList(fo);

        name = fo.getName();
        FileObject descriptor = fo.getFileObject("Descriptor");
        assert descriptor != null;
        needsFindServerUI = getBooleanValue(descriptor.getAttribute(ATTR_NEEDS_FIND_SERVER_UI), false);

        dep = NetbeansDeployment.createGraph(descriptor.getInputStream());

        lkp = Lookups.forPath(fo.getPath());
        factory = lkp.lookup(DeploymentFactory.class);
        if (factory != null) {
            factoryCls = factory.getClass();
        } else {
            FileObject factoryinstance = fo.getFileObject("Factory.instance");
            if (factoryinstance == null) {
                String msg = NbBundle.getMessage(Server.class, "MSG_NoFactoryInstanceClass", name);
                LOGGER.log(Level.SEVERE, msg);
                factoryCls = null;
                return;
            }
            DataObject dobj = DataObject.find(factoryinstance);
            InstanceCookie cookie = dobj.getCookie(InstanceCookie.class);
            if (cookie == null) {
                String msg = NbBundle.getMessage(Server.class, "MSG_FactoryFailed", name);
                LOGGER.log(Level.SEVERE, msg);
                factoryCls = null;
                return;
            }
            factoryCls = cookie.instanceClass();

            // speculative code depending on the DF implementation and if it registers
            // itself with DFM or not

            try {
                factory = (DeploymentFactory) cookie.instanceCreate();
            } catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    private synchronized DeploymentFactory getFactory() {
        if (factory == null) {

            DeploymentFactory[] factories = DeploymentFactoryManager.getInstance().getDeploymentFactories();
            for (int i = 0; i < factories.length; i++) {
                if (factoryCls.isInstance(factories[i])) {
                    factory = factories[i];
                    break;
                }
            }
        }
        if (factory == null) {
            throw new IllegalStateException("Can't acquire DeploymentFactory for " + name); //NOI18N
        }
        return factory;
    }

    public synchronized DeploymentManager getDisconnectedDeploymentManager()
            throws DeploymentManagerCreationException  {

        if(manager == null) {
            manager = getDisconnectedDeploymentManager(dep.getDisconnectedString());
        }
        return manager;
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        return getFactory().getDisconnectedDeploymentManager(uri);
    }

    public boolean handlesUri(String uri) {
        try {
            return getFactory().handlesURI(uri);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
            return false;
        }
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password)
            throws DeploymentManagerCreationException {
        return getFactory().getDeploymentManager(uri, username, password);
    }

    public String getDisplayName() {
        return getFactory().getDisplayName();
    }

    public String getShortName() {
        return name;
    }

    public String getIconBase() {
        return dep.getIcon();
    }

    public boolean canDeployEars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isEarDeploy();
    }

    public boolean canDeployWars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isWarDeploy();
    }

    public boolean canDeployEjbJars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isEjbjarDeploy();
    }

    // PENDING should be cached?
    public String getHelpId(String beanClass) {
        ConfigBean[] beans = dep.getConfigBean();
        for(int i = 0; i < beans.length; i++) {
            if(beans[i].getClassName().equals(beanClass)) {
                return beans[i].getHelpid();
            }
        }
        return null;
    }

    public synchronized RegistryNodeProvider getNodeProvider() {
        if (nodeProvider != null) {
            return nodeProvider;
        }

        RegistryNodeFactory nodeFact = lkp.lookup(RegistryNodeFactory.class);
        if (nodeFact == null) {
            String msg = NbBundle.getMessage(Server.class, "MSG_NoInstance", name, RegistryNodeFactory.class);
            LOGGER.log(Level.INFO, msg);
        }
        nodeProvider = new RegistryNodeProvider(nodeFact); //null is acceptable
        return nodeProvider;
    }

    public RegistryNodeFactory getRegistryNodeFactory() {
        return lkp.lookup(RegistryNodeFactory.class);
    }

    /** returns OptionalDeploymentManagerFactory or null it is not provided by the plugin */
    public OptionalDeploymentManagerFactory getOptionalFactory () {
        OptionalDeploymentManagerFactory o = lkp.lookup(OptionalDeploymentManagerFactory.class);
        return o;
    }

    /** returns J2eePlatformFactory or null if it is not provided by the plugin */
    public J2eePlatformFactory getJ2eePlatformFactory () {
        J2eePlatformFactory o = lkp.lookup(J2eePlatformFactory.class);
        return o;
    }

    public ModuleConfigurationFactory getModuleConfigurationFactory() {
        return lkp.lookup(ModuleConfigurationFactory.class);
    }

    public VerifierSupport getVerifierSupport() {
        VerifierSupport vs = lkp.lookup(VerifierSupport.class);
        return vs;
    }

    public boolean canVerify(J2eeModule.Type moduleType) {
        VerifierSupport vs = getVerifierSupport();
        return  vs != null && vs.supportsModuleType(J2eeModuleAccessor.getDefault().getJsrModuleType(moduleType));
    }

    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        getVerifierSupport().verify(target, logger);
    }

    public ServerInstance[] getInstances() {
        Collection ret = new ArrayList();
        for (Iterator i=ServerRegistry.getInstance().getInstances().iterator(); i.hasNext();) {
            ServerInstance inst = (ServerInstance) i.next();
            if (name.equals(inst.getServer().getShortName())) {
                ret.add(inst);
            }
        }
        return (ServerInstance[]) ret.toArray(new ServerInstance[0]);
    }

    public WebContextRoot getWebContextRoot() {
        return dep.getWebContextRoot();
    }

    public DeploymentFactory getDeploymentFactory() {
        return getFactory();
    }

    private static boolean getBooleanValue(Object v, boolean dvalue) {
        if (v instanceof Boolean)
            return ((Boolean)v);
        if (v instanceof String)
            return Boolean.valueOf((String) v);
        return dvalue;
    }

    public boolean needsFindServerUI() {
        return needsFindServerUI;
    }

    @Override
    public String toString() {
        return getShortName ();
    }

    public boolean supportsModuleType(J2eeModule.Type type) {
        if (J2eeModule.Type.WAR.equals(type)) {
            return this.canDeployWars();
        } else if (J2eeModule.Type.EJB.equals(type)) {
            return this.canDeployEjbJars();
        } else if (J2eeModule.Type.EAR.equals(type)) {
            return this.canDeployEars();
        } else {
            // PENDING, precise answer for other module types, for now assume true
            return true;
        }
    }

    private static final String LAYER_DEPLOYMENT_FILE_NAMES = "DeploymentFileNames"; //NOI18N
    private Map<String, String[]> deployConfigDescriptorMap;

    private final void initDeploymentConfigurationFileList(FileObject fo) {
        deployConfigDescriptorMap = new HashMap<>();
        FileObject deplFNames = fo.getFileObject(LAYER_DEPLOYMENT_FILE_NAMES);
        if (deplFNames != null) {
            FileObject mTypes [] = deplFNames.getChildren();
            for (int j = 0; j < mTypes.length; j++) {
                String mTypeName = mTypes[j].getName().toUpperCase();
                FileObject allNames[] = mTypes[j].getChildren();
                if (allNames == null || allNames.length == 0) {
                    continue;
                }
                List<String> filepaths = new ArrayList<String>();
                for (int i = 0; i < allNames.length; i++) {
                    if (allNames[i] == null) {
                        continue;
                    }
                    String fname = allNames[i].getNameExt();
                    filepaths.add(fname.replace('\\', '/')); //just in case..
                }
                deployConfigDescriptorMap.put(mTypeName, filepaths.toArray(new String[0]));
            }
        }
    }

    public String[] getDeploymentPlanFiles(J2eeModule.Type type) {
        Object jsrModuleType = J2eeModuleAccessor.getDefault().getJsrModuleType(type);
        return deployConfigDescriptorMap.get(jsrModuleType.toString().toUpperCase());
    }
}
