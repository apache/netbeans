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

package org.netbeans.modules.j2ee.dd.impl.web.metadata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * @author Andrei Badea
 * @author Petr Slechta
 */
public class WebAppMetadataModelImpl implements MetadataModelImplementation<WebAppMetadata> {

    private final MetadataUnit metadataUnit;
    private AnnotationModelHelper helper;
    private WebAppMetadata metadata;
    private final Object myLock = new Object();
    private AtomicBoolean isReady = new AtomicBoolean(false);

    private static final RequestProcessor RP = new RequestProcessor();

    public static WebAppMetadataModelImpl create(MetadataUnit metadataUnit) {
        WebAppMetadataModelImpl result = new WebAppMetadataModelImpl(metadataUnit);
        result.initialize();
        return result;
    }

    private WebAppMetadataModelImpl(MetadataUnit metadataUnit) {
        this.metadataUnit = metadataUnit;
        createMetadata();
    }

    private void createMetadata(){
        Runnable runnable = new Runnable(){

            public void run() {
                synchronized (myLock) {
                    metadata = new WebAppMetadataImpl(metadataUnit,
                            WebAppMetadataModelImpl.this);
                    myLock.notifyAll();
                    isReady.set( true);
                }
            }
        };
        RP.post(runnable);
    }
    
    private WebAppMetadata getMetadata(){
        synchronized (myLock) {
            while ( metadata == null ){
                try {
                    myLock.wait();
                }
                catch(InterruptedException e){
                    /*
                     *  Still need not null metadata.
                     *  Ignore exception and go to next iteration. 
                     */
                }
            }
            return metadata;
        }
    }

    private void initialize() {
        metadataUnit.addPropertyChangeListener(new DDListener());
    }

    AnnotationModelHelper getHelper() {
        if (helper == null) {
            ClasspathInfo cpi = ClasspathInfo.create(metadataUnit.getBootPath(), metadataUnit.getCompilePath(), metadataUnit.getSourcePath());
            helper = AnnotationModelHelper.create(cpi);
        }
        return helper;
    }

    public <R> R runReadAction(final MetadataModelAction<WebAppMetadata, R> action) throws IOException {
        return getHelper().runJavaSourceTask(new Callable<R>() {
            public R call() throws Exception {
                return action.run( getMetadata());
            }
        });
    }

    public boolean isReady() {
        if ( getHelper().isJavaScanInProgress() ){
            return false;
        }
        return isReady.get();
    }

    public <R> Future<R> runReadActionWhenReady(final MetadataModelAction<WebAppMetadata, R> action) throws IOException {
        return getHelper().runJavaSourceTaskWhenScanFinished(new Callable<R>() {
            public R call() throws Exception {
                return action.run( getMetadata());
            }
        });
    }

    private final class DDListener implements PropertyChangeListener, Callable<Void> {

        public void propertyChange(PropertyChangeEvent evt) {
            if (!MetadataUnit.PROP_DEPLOYMENT_DESCRIPTOR.equals(evt.getPropertyName())) {
                return;
            }
            try {
                getHelper().runJavaSourceTask(this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            // XXX send change event
        }
        public Void call() throws IOException {
            return null;
        }
    }
}
