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
import java.util.logging.Logger;
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
    private int offset;
    private final Collection<ErrorRule> c;
    private final Map<Class, Data> class2Data;
    
    /** Creates a new instance of CreatorBasedLazyFixList */
    public CreatorBasedLazyFixList(FileObject file, String diagnosticKey, int offset, Collection<ErrorRule> c, Map<Class, Data> class2Data) {
        this.pcs = new PropertyChangeSupport(this);
        this.file = file;
        this.diagnosticKey = diagnosticKey;
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
