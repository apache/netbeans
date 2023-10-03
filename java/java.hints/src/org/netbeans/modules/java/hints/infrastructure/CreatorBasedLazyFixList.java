/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.util.TreePath;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class CreatorBasedLazyFixList implements LazyFixList {
    
    private PropertyChangeSupport pcs;
    private boolean computed;
    private boolean computing;
    private List<Fix> fixes;
    
    private FileObject file;
    private String diagnosticKey;
    private String diagnosticMessage;
    private int offset;
    private final Collection<ErrorRule> c;
    private final Map<Class, Data> class2Data;
    
    /** Creates a new instance of CreatorBasedLazyFixList */
    public CreatorBasedLazyFixList(FileObject file, String diagnosticKey, String diagnosticMessage, int offset, Collection<ErrorRule> c, Map<Class, Data> class2Data) {
        this.pcs = new PropertyChangeSupport(this);
        this.file = file;
        this.diagnosticKey = diagnosticKey;
        this.diagnosticMessage = diagnosticMessage;
        this.offset = offset;
        this.c = c;
        this.class2Data = class2Data;
        this.fixes = Collections.<Fix>emptyList();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    public boolean probablyContainsFixes() {
        return true;
    }
    
    public synchronized List<Fix> getFixes() {
        if (!computed && !computing) {
            LazyHintComputationFactory.addToCompute(file, this);
            computing = true;
        }
        return fixes;
    }
    
    public synchronized boolean isComputed() {
        return computed;
    }
    
    private ErrorRule<?> currentRule;
    
    private synchronized void setCurrentRule(ErrorRule currentRule) {
        this.currentRule = currentRule;
    }
    
    public void compute(CompilationInfo info, AtomicBoolean cancelled) {
        synchronized (this) {
            //resume:
            if (this.computed) {
                return ; //already done.
            }
        }
        
        List<Fix> fixes = new ArrayList<Fix>();
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        
        for (ErrorRule rule : c) {
            if (cancelled.get()) {
                //has been canceled, the computation was not finished:
                return ;
            }
            
            setCurrentRule(rule);
            
            try {
                Data data = class2Data.get(rule.getClass());
                
                if (data == null) {
                    class2Data.put(rule.getClass(), data = new Data());
                }

                if ("compiler.err.proc.messager".equals(diagnosticKey)) { //NOI18N
                    data.setData(diagnosticMessage);
                }
                
                List<Fix> currentRuleFixes = rule.run(info, diagnosticKey, offset, path, data);
                
                if (currentRuleFixes == CANCELLED) {
                    cancelled.set(true);
                    return ;
                }
                
                if (currentRuleFixes != null) {
                    fixes.addAll(currentRuleFixes);
                }
            } finally {
                setCurrentRule(null);
            }
        }
        
        if (cancelled.get()) {
            //has been canceled, the computation was not finished:
            return ;
        }
        
        synchronized (this) {
            this.fixes    = fixes;
            this.computed = true;
        }
        
        pcs.firePropertyChange(PROP_FIXES, null, null);
        pcs.firePropertyChange(PROP_COMPUTED, null, null);
    }
    
    public void cancel() {
        synchronized (this) {
            if (currentRule != null) {
                currentRule.cancel();
            }
        }
    }
    
    public static final List<Fix> CANCELLED = Collections.unmodifiableList(new LinkedList<Fix>());
    
}
