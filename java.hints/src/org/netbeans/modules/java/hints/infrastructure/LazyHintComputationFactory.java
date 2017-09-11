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

package org.netbeans.modules.java.hints.infrastructure;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public class LazyHintComputationFactory extends EditorAwareJavaSourceTaskFactory {
    
    private static Map<FileObject, List<Reference<CreatorBasedLazyFixList>>> file2Creators = new WeakHashMap<FileObject, List<Reference<CreatorBasedLazyFixList>>>();
    
    /** Creates a new instance of LazyHintComputationFactory */
    public LazyHintComputationFactory() {
        super(Phase.RESOLVED, Priority.LOW);
    }

    public CancellableTask<CompilationInfo> createTask(FileObject file) {
        return new LazyHintComputation(file);
    }
 
    private static void rescheduleImpl(FileObject file) {
        LazyHintComputationFactory f = Lookup.getDefault().lookup(LazyHintComputationFactory.class);
        
        if (f != null) {
            f.reschedule(file);
        }
    }
    
    public static void addToCompute(FileObject file, CreatorBasedLazyFixList list) {
        synchronized (LazyHintComputationFactory.class) {
            List<Reference<CreatorBasedLazyFixList>> references = file2Creators.get(file);
            
            if (references == null) {
                file2Creators.put(file, references = new ArrayList<Reference<CreatorBasedLazyFixList>>());
            }
            
            references.add(new WeakReference(list));
        }
        
        rescheduleImpl(file);
    }
    
    public static synchronized List<CreatorBasedLazyFixList> getAndClearToCompute(FileObject file) {
        List<Reference<CreatorBasedLazyFixList>> references = file2Creators.get(file);
        
        if (references == null) {
            return Collections.emptyList();
        }
        
        List<CreatorBasedLazyFixList> result = new ArrayList<CreatorBasedLazyFixList>();
        
        for (Reference<CreatorBasedLazyFixList> r : references) {
            CreatorBasedLazyFixList c = r.get();
            
            if (c != null) {
                result.add(c);
            }
        }
        
        references.clear();
        
        return result;
    }
}
