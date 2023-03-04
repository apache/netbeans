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
package org.netbeans.modules.csl.navigation;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.csl.core.AbstractTaskFactory;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.openide.util.Lookup;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.*;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 *
 * @author Jan Lahoda, Petr Hrebejk
 */
public final class ClassMemberNavigatorSourceFactory extends AbstractTaskFactory {

    private static ClassMemberNavigatorSourceFactory instance = null;
    private ClassMemberPanelUI ui;
    private PropertyChangeListener listener;
    private Lookup context;
    
    public static synchronized ClassMemberNavigatorSourceFactory getInstance() {
        if (instance == null) {
            instance = new ClassMemberNavigatorSourceFactory();
        }
        return instance;
    }
    
    private ClassMemberNavigatorSourceFactory() {
        super(true); // XXX: Phase.ELEMENTS_RESOLVED, Priority.LOW
    }

    @Override
    public Collection<? extends SchedulerTask> createTasks(Language l, Snapshot snapshot) {
        return Collections.singleton(new ProxyElementScanningTask(CSLNavigatorScheduler.class));
    }

    public synchronized void setLookup(Lookup l, ClassMemberPanelUI ui) {
        this.ui = ui;
        this.context = l;
        firePropertyChangeEvent();
    }

    public void firePropertyChangeEvent() {
        if (listener != null) {
            listener.propertyChange(null);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        listener = l;
    }

    Lookup getContext() {
        return context;
    }

    private final class ProxyElementScanningTask extends IndexingAwareParserResultTask<ParserResult> {
        private final CancelSupportImplementation cancel = SchedulerTaskCancelSupportImpl.create(this);
        private ElementScanningTask task = null;
        private Class<? extends Scheduler> clazz;

        public ProxyElementScanningTask(Class<? extends Scheduler> c) {
            super(TaskIndexingMode.ALLOWED_DURING_SCAN);
            this.clazz = c;
        }

        private ElementScanningTask getTask() {
            synchronized (ClassMemberNavigatorSourceFactory.this) {
                if (task == null && ui != null) {
                    task = ui.getTask();
                }
                return task;
            }
        }

        public @Override void cancel() {
            final ElementScanningTask t = getTask();
            if (t != null) {
                t.cancel();
            }
        }

        @Override
        public void run(ParserResult result, SchedulerEvent event) {
            SpiSupportAccessor.getInstance().setCancelSupport(cancel);
            try {
                final ElementScanningTask t = getTask();
                if (t != null) {
                    t.run(result, event);
                }
            } finally {
                SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
            }
        }

        public @Override int getPriority() {
            return Integer.MAX_VALUE;
        }

        public @Override Class<? extends Scheduler> getSchedulerClass() {
            return clazz;
        }
    };
}
