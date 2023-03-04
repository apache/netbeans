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
package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.wscompile.BadCommandLineException;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.wsdl.parser.MetadataFinder;
import com.sun.tools.ws.wsdl.parser.WSDLInternalizationLogic;
import com.sun.xml.ws.util.JAXWSUtils;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xml.resolver.NbCatalogManager;
import org.apache.xml.resolver.tools.NbCatalogResolver;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 *
 * @author mkuchtiak
 */
public class WsdlModeler {

    private WsdlModel wsdlModel;
    private WSDLModeler ideWSDLModeler;
    private URL wsdlUrl;
    private URL[] bindings;
    private URL catalog;
    private EntityResolver entityResolver;
    private Set<String> bindingFiles;
    private String packageName;
    private final List<WsdlModelListener> modelListeners;
    private List<WsdlChangeListener> wsdlChangeListeners;
    RequestProcessor.Task task;
    int listenersSize;
    protected Properties properties;
    private Throwable creationException;

    /** Creates a new instance of WsdlModeler */
    WsdlModeler(URL wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
        modelListeners = Collections.synchronizedList(new ArrayList<WsdlModelListener>());
        wsdlChangeListeners = new ArrayList<WsdlChangeListener>();
        task = new RequestProcessor("WsdlModeler-request-processor").create(new Runnable() { //NOI18N

            public void run() {
                generateWsdlModel();
                fireModelCreated(wsdlModel);
            }
        }, true);

    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setJAXBBindings(URL[] bindings) {
        this.bindings = bindings;
    }

    public URL[] getJAXBBindings() {
        return bindings;
    }

    public void setCatalog(URL catalog) {
        this.catalog = catalog;
    }

    public URL getCatalog() {
        return catalog;
    }

    void setWsdlUrl(URL url) {
        wsdlUrl = url;
    }

    public URL getWsdlUrl() {
        return wsdlUrl;
    }

    public Throwable getCreationException() {
        return creationException;
    }

    public WsdlModel getWsdlModel() {
        return wsdlModel;
    }

    public WsdlModel getAndWaitForWsdlModel() {
        return getAndWaitForWsdlModel( false );
    }
    
    public WsdlModel getAndWaitForWsdlModel(boolean forceReload ) {
        if (forceReload || getWsdlModel() == null) {
            generateWsdlModel();
        }
        return wsdlModel;
    }

    public void generateWsdlModel(WsdlModelListener listener, final WsdlErrorHandler errorHandler) {
        RequestProcessor.Task task1 = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                generateWsdlModel(errorHandler);
                fireModelCreated(wsdlModel);
            }
        }, true);
        addWsdlModelListener(listener);
        task1.run();
    }

    public void generateWsdlModel(WsdlModelListener listener) {
        generateWsdlModel(listener, false);
    }

    public void generateWsdlModel(WsdlModelListener listener, boolean forceReload) {

        if (forceReload) {
            try {
                task.waitFinished(10000);
            } catch (InterruptedException ex) {
            }
            addWsdlModelListener(listener);
            task.schedule(100);
        } else {
            addWsdlModelListener(listener);
            task.schedule(100);
        }
    }

    private void generateWsdlModel() {
        generateWsdlModel(new CatchFirstErrorHandler());
    }

    private void generateWsdlModel(WsdlErrorHandler errorHandler) {
        WsimportOptions options = new WsimportOptions();
        properties = new Properties();
        bindingFiles = new HashSet<String>();
        if (bindings != null) {
            for (int i = 0; i < bindings.length; i++) {
                try {
                    options.addBindings(JAXWSUtils.absolutize(bindings[i].toExternalForm()));
                } catch (BadCommandLineException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINE, "WsdlModeler.generateWsdlModel", ex); //NOI18N
                }
            }
        }
        try {
            if (wsdlUrl.toExternalForm().startsWith("http://") || wsdlUrl.toExternalForm().startsWith("https://")) { //NOI18N
                InputSource source = new InputSource(wsdlUrl.toExternalForm());
                options.addWSDL(source);
            } else { // wsdl is in local file
                options.addWSDL(new File(wsdlUrl.getFile()));
            }
            options.compatibilityMode = WsimportOptions.EXTENSION;

            if (packageName != null) {
                options.defaultPackage = packageName;
            }
            if (catalog != null) {
                NbCatalogManager manager = new NbCatalogManager(null);
                manager.setCatalogFiles(catalog.toExternalForm());
                manager.setUseStaticCatalog(false);
                manager.setVerbosity(4);
                entityResolver = new NbCatalogResolver(manager);
                options.entityResolver = entityResolver;
            }

            options.parseBindings(new IdeErrorReceiver(errorHandler));

            IdeErrorReceiver ideErrorReceiver = new IdeErrorReceiver(errorHandler);
            MetadataFinder finder = new MetadataFinder( new WSDLInternalizationLogic(),
                    options, ideErrorReceiver);
            finder.parseWSDL();
            ideWSDLModeler =
                    new WSDLModeler(options, ideErrorReceiver, finder);
            Model tmpModel = ideWSDLModeler.buildModel();

            if (tmpModel != null) {
                WsdlModel oldWsdlModel = wsdlModel;
                wsdlModel = new WsdlModel(tmpModel);
                fireWsdlModelChanged(oldWsdlModel, wsdlModel);
                creationException = null;
            } else {
                WsdlModel oldWsdlModel = wsdlModel;
                wsdlModel = null;
                if (oldWsdlModel != null) {
                    fireWsdlModelChanged(oldWsdlModel, null);
                }
                SAXParseException parseError = null;
                if (errorHandler instanceof CatchFirstErrorHandler) {
                    parseError = ((CatchFirstErrorHandler) errorHandler).getFirstError();
                    creationException = parseError;
                }
                if (parseError == null) {
                    creationException = new Exception(NbBundle.getMessage(WsdlModeler.class, "ERR_CannotGenerateModel", wsdlUrl.toExternalForm()));
                }
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "WsdlModeler.generateWsdlModel", creationException); //NOI18N
            }
        } catch (Throwable ex) {
            wsdlModel = null;
            SAXParseException parseError = null;
            if (errorHandler instanceof CatchFirstErrorHandler) {
                parseError = ((CatchFirstErrorHandler) errorHandler).getFirstError();
                creationException = parseError;
            }
            if (parseError == null) {
                creationException = ex;
            }
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "WsdlModeler.generateWsdlModel", ex); //NOI18N
        }
    }

    private synchronized void addWsdlModelListener(WsdlModelListener listener) {
        // adding listener
        if (listener != null) {
            modelListeners.add(listener);
        }
    }

    private void fireModelCreated(WsdlModel model) {
        synchronized (modelListeners) {
            Iterator<WsdlModelListener> modelIter = modelListeners.iterator();
            while (modelIter.hasNext()) {
                WsdlModelListener l = modelIter.next();
                l.modelCreated(model);
            }
        }
        // Removing all listeners
        synchronized (this) {
            modelListeners.clear();
        }
    }

    private static class IdeErrorReceiver extends ErrorReceiver {

        private WsdlErrorHandler errorHandler;

        IdeErrorReceiver(WsdlErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public void error(Exception excptn) {
            super.error(excptn);
        }

        public void warning(SAXParseException ex) throws AbortException {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE,
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
            if (errorHandler != null) {
                try {
                    errorHandler.warning(ex);
                } catch (WsdlErrorHandler.AbortException abort) {
                    AbortException newEx = new AbortException();
                    newEx.initCause(abort);
                    throw newEx;
                }
            }
        }

        public void info(SAXParseException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE,
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
            if (errorHandler != null) {
                errorHandler.info(ex);
            }
        }

        public void fatalError(SAXParseException ex) throws AbortException {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE,
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
            if (errorHandler != null) {
                try {
                    errorHandler.fatalError(ex);
                } catch (WsdlErrorHandler.AbortException abort) {
                    AbortException newEx = new AbortException();
                    newEx.initCause(abort);
                    throw newEx;
                }
            }
        }

        public void error(SAXParseException ex) throws AbortException {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE,
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
            if (errorHandler != null) {
                try {
                    errorHandler.error(ex);
                } catch (WsdlErrorHandler.AbortException abort) {
                    AbortException newEx = new AbortException();
                    newEx.initCause(abort);
                    throw newEx;
                }
            }
        }

        @Override
        public void debug(SAXParseException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE,
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
            if (errorHandler != null) {
                errorHandler.info(ex);
            }
        }
    }

    public synchronized void addWsdlChangeListener(WsdlChangeListener wsdlChangeListener) {
        wsdlChangeListeners.add(wsdlChangeListener);
    }

    public synchronized void removeWsdlChangeListener(WsdlChangeListener wsdlChangeListener) {
        wsdlChangeListeners.remove(wsdlChangeListener);
    }

    private void fireWsdlModelChanged(WsdlModel oldWsdlModel, WsdlModel newWsdlModel) {
        for (WsdlChangeListener wsdlChangeListener : wsdlChangeListeners) {
            wsdlChangeListener.wsdlModelChanged(oldWsdlModel, newWsdlModel);
        }
    }

    private static class CatchFirstErrorHandler implements WsdlErrorHandler {

        private SAXParseException firstError;

        public void warning(SAXParseException ex) throws AbortException {
        }

        public void info(SAXParseException ex) {
        }

        public void fatalError(SAXParseException ex) throws AbortException {
            if (firstError == null) {
                firstError = ex;
            }
        }

        public void error(SAXParseException ex) throws AbortException {
            if (firstError == null) {
                firstError = ex;
            }
        }

        public SAXParseException getFirstError() {
            return firstError;
        }
    }
}
