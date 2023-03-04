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
package org.netbeans.modules.websvc.saas.model.wsdl.impl;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXParseException;

/**
 *
 * @author mkuchtiak
 */
public class WsdlModeler {

    private volatile WsdlModel wsdlModel;
    private URL wsdlUrl;
    private URL[] bindings;
    private URL catalog;
    private EntityResolver entityResolver;
    private Set<String> bindingFiles;
    private String packageName;
    private List<WsdlModelListener> modelListeners;
    RequestProcessor.Task task, task1;
    int listenersSize;
    protected Properties properties;
    private volatile Throwable creationException;

    /** Creates a new instance of WsdlModeler */
    WsdlModeler(URL wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
        modelListeners = Collections.synchronizedList(new ArrayList<WsdlModelListener>());
        task = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                generateWsdlModel();
                fireModelCreated(wsdlModel);
            }
        }, true);

    }

    void setWsdlUrl(URL url) {
        wsdlUrl = url;
    }

    public URL getWsdlUrl() {
        return wsdlUrl;
    }

    public WsdlModel getWsdlModel() {
        return wsdlModel;
    }

    public WsdlModel getAndWaitForWsdlModel() {
        if (getWsdlModel() == null) {
            generateWsdlModel();
        }
        return wsdlModel;
    }

    public void generateWsdlModel(WsdlModelListener listener, final WsdlErrorHandler errorHandler) {
        RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                generateWsdlModel(errorHandler);
                fireModelCreated(wsdlModel);
            }
        }, true);
        addWsdlModelListener(listener);
        task.run();
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
            task.schedule(0);
        } else {
            addWsdlModelListener(listener);
            if (task.isFinished()) {
                task.schedule(0);
            }
        }
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Throwable getCreationException() {
        return creationException;
    }

    private void generateWsdlModel() {
        this.generateWsdlModel(new CatchFirstErrorHandler());
    }

    private void generateWsdlModel(WsdlErrorHandler errorHandler) {
        try {

            FileObject wsdlFO = FileUtil.toFileObject(FileUtil.normalizeFile(new File(wsdlUrl.toURI())));
            ModelSource ms = Utilities.getModelSource(wsdlFO, false);
            WSDLModel model = WSDLModelFactory.getDefault().getModel(Utilities.getModelSource(wsdlFO, false));
            if (model == null) {
                SAXParseException parseError = null;
                if (errorHandler instanceof CatchFirstErrorHandler) {
                    parseError = ((CatchFirstErrorHandler) errorHandler).getFirstError();
                    creationException = parseError;
                }
                if (parseError == null) {
                    creationException = new Exception(NbBundle.getMessage(WsdlModeler.class, "ERR_CannotGenerateModel", wsdlUrl.toExternalForm()));
                }
            } else {
                synchronized( this ) { 
                    wsdlModel = new WsdlModel(model);
                }
            }


        } catch (URISyntaxException ex) {
            wsdlModel = null;
            SAXParseException parseError = null;
            if (errorHandler instanceof CatchFirstErrorHandler) {
                parseError = ((CatchFirstErrorHandler) errorHandler).getFirstError();
                creationException = parseError;
            }
            if (parseError == null) {
                creationException = ex;
            }
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

    private class CatchFirstErrorHandler implements WsdlErrorHandler {

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
