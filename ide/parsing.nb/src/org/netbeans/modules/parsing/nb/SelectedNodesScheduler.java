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

package org.netbeans.modules.parsing.nb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;


/**
 *
 * @author Jan Jancura
 */
@ServiceProvider(service=Scheduler.class)
public class SelectedNodesScheduler extends Scheduler {



    public SelectedNodesScheduler () {
        TopComponent.getRegistry ().addPropertyChangeListener (new AListener ());
        refresh ();
    }

    private RequestProcessor requestProcessor;

    private void refresh () {
        if (requestProcessor == null)
            requestProcessor = new RequestProcessor ("SelectedNodesScheduler");
        requestProcessor.post (new Runnable () {
            public void run () {
                final Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
                if (nodes.length == 1) {
                    final DataObject dataObject = nodes [0].getLookup ().lookup (DataObject.class);
                    if (dataObject != null && dataObject.isValid()) {
                        final FileObject fileObject = dataObject.getPrimaryFile ();
                        if (fileObject.isValid() && ParserManager.canBeParsed(fileObject.getMIMEType())) {
                            final Source source = Source.create (fileObject);
                            if (source != null) {
                                schedule (source, new SchedulerEvent (SelectedNodesScheduler.this) {});
                                return;
                            }
                        }
                    }
                }

                schedule(null, null);
            }
        });
    }
    
    @Override
    public String toString () {
        return "SelectedNodesScheduller"; //NOI18N
    }

    @Override
    protected SchedulerEvent createSchedulerEvent (SourceModificationEvent event) {
        if (event.getModifiedSource () == getSource())
            return new SchedulerEvent (this) {};
        return null;
    }
    
    private class AListener implements PropertyChangeListener {
    
        public void propertyChange (PropertyChangeEvent evt) {
            if (evt.getPropertyName () == null ||
                evt.getPropertyName ().equals (TopComponent.Registry.PROP_ACTIVATED_NODES)
            ) {
                refresh ();
            }
        }
    }
}
