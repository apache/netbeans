/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
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
