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
