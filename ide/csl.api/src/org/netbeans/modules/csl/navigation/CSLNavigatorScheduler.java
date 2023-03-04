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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.parsing.api.ParserManager;

import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author David Strupl inspired by Jan Jancura
 */
@ServiceProvider(service=Scheduler.class)
public class CSLNavigatorScheduler extends Scheduler {

    private RequestProcessor requestProcessor; 

    public CSLNavigatorScheduler () {
        ClassMemberNavigatorSourceFactory.getInstance().addPropertyChangeListener (new AListener ());
        refresh ();
    }

    private void refresh () {
        if (requestProcessor == null) {
            requestProcessor = new RequestProcessor ("CSLNavigatorScheduler"); // NOI18N
        }
        requestProcessor.post (new Runnable () {
            @Override
            public void run () {
                ClassMemberNavigatorSourceFactory f = ClassMemberNavigatorSourceFactory.getInstance();
                if (f != null && f.getContext() != null) {
                    final FileObject fileObject = f.getContext().lookup(FileObject.class);
                    if (fileObject != null && fileObject.isValid() && ParserManager.canBeParsed(fileObject.getMIMEType())) {
                        final Source source = Source.create (fileObject);
                        if (source != null) {
                            schedule (source, new SchedulerEvent (CSLNavigatorScheduler.this) {});
                            return;
                        }
                    }
                }
                schedule(null, null);
            }
        });
    }
    
    @Override
    public String toString () {
        return "CSLNavigatorScheduler"; //NOI18N
    }

    @Override
    protected SchedulerEvent createSchedulerEvent (SourceModificationEvent event) {
        if (event.getModifiedSource () == getSource())
            return new SchedulerEvent (this) {};
        return null;
    }
    
    private class AListener implements PropertyChangeListener {
    
        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            refresh ();
        }
    }
}
