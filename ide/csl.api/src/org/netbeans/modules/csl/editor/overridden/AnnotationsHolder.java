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
package org.netbeans.modules.csl.editor.overridden;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationsHolder implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(AnnotationsHolder.class.getName());
    private static final RequestProcessor WORKER = new RequestProcessor(AnnotationsHolder.class.getName(), 1, false, false);
    private static final Map<DataObject, AnnotationsHolder> file2Annotations = new HashMap<DataObject, AnnotationsHolder>();
    
    public static synchronized AnnotationsHolder get(FileObject file) {
        try {
            DataObject od = DataObject.find(file);
            AnnotationsHolder a = file2Annotations.get(od);

            if (a != null) {
                return a;
            }

            EditorCookie.Observable ec = od.getLookup().lookup(EditorCookie.Observable.class);
            
            if (ec == null) {
                return null;
            }
            
            file2Annotations.put(od, a = new AnnotationsHolder(od, ec));
            
            return a;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            
            return null;
        }
    }
    
    private final DataObject file;
    private final EditorCookie.Observable ec;
    
    private AnnotationsHolder(DataObject file, EditorCookie.Observable ec) {
        this.file = file;
        this.ec   = ec;
        this.annotations = new ArrayList<IsOverriddenAnnotation>();
        
        ec.addPropertyChangeListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                checkForReset();
            }
        });
        
        Logger.getLogger("TIMER").log(Level.FINE, "Overridden AnnotationsHolder", new Object[] {file.getPrimaryFile(), this}); //NOI18N
     }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (EditorCookie.Observable.PROP_OPENED_PANES.endsWith(evt.getPropertyName()) || evt.getPropertyName() == null) {
            checkForReset();
        }
    }
    
    private void checkForReset() {
        assert SwingUtilities.isEventDispatchThread();
        
        if (ec.getOpenedPanes() == null) {
            //reset:
            synchronized (AnnotationsHolder.class) {
                file2Annotations.remove(file);
            }
            
            setNewAnnotations(Collections.<IsOverriddenAnnotation>emptyList());
            ec.removePropertyChangeListener(this);
        }
    }

    private final List<IsOverriddenAnnotation> annotations;
    
    public void setNewAnnotations(final List<IsOverriddenAnnotation> as) {
        Runnable doAttachDetach = new Runnable() {
            @Override public void run() {
                List<IsOverriddenAnnotation> toRemove;
                List<IsOverriddenAnnotation> toAdd;

                synchronized (AnnotationsHolder.this) {
                    toRemove = new ArrayList<IsOverriddenAnnotation>(annotations);
                    toAdd = new ArrayList<IsOverriddenAnnotation>(as);

                    annotations.clear();
                    annotations.addAll(as);
                }

                for (IsOverriddenAnnotation a : toRemove) {
                    a.detachImpl();
                }

                for (IsOverriddenAnnotation a : toAdd) {
                    a.attach();
                }
            }
        };
        
        //to serialize the requests, as these can come not only from ComputeAnnotations, but also from checkForReset():
        //(might be possible to cancel all pending, but not currently running, requests, but that would require keeping the Task)
        WORKER.submit(doAttachDetach);
    }
    
    public synchronized List<IsOverriddenAnnotation> getAnnotations() {
        return new ArrayList<IsOverriddenAnnotation>(annotations);
    }
}
