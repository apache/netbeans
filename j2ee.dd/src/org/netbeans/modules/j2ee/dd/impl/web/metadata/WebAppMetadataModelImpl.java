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
