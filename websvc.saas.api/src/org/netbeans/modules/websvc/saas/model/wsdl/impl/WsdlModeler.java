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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
