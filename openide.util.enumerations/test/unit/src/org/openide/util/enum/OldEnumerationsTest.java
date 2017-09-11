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

package org.openide.util.enum;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openide.util.EnumerationsTest;
import org.openide.util.EnumerationsTest.QueueProcess;

/** Implement factory methods from EnumerationsTest, shares the same tests
 * with EnumerationsTest.
 *
 * @author Jaroslav Tulach
 */
public class OldEnumerationsTest extends EnumerationsTest {

    /** Creates a new instance of EnumerationsTest */
    public OldEnumerationsTest(String testName) {
        super(testName);
    }
    
    protected Enumeration singleton(Object obj) {
        return new SingletonEnumeration(obj);
    }
    
    protected Enumeration convert(Enumeration en, final Map map) {
        return new AlterEnumeration(en) {
            protected Object alter(Object o) {
                return map.get(o);
            }
        };
    }
    
    protected Enumeration removeDuplicates(Enumeration en) {
        return new RemoveDuplicatesEnumeration(en);
    }
    
    protected Enumeration removeNulls(Enumeration en) {
        return new FilterEnumeration(en);
    }
    
    protected Enumeration concat(Enumeration en1, Enumeration en2) {
        return new SequenceEnumeration(en1, en2);
    }
    
    protected Enumeration array(Object[] arr) {
        return new ArrayEnumeration(arr);
    }
    
    protected Enumeration filter(Enumeration en, final Set filter) {
        return new FilterEnumeration(en) {
            protected boolean accept(Object obj) {
                return filter.contains(obj);
            }
        };
    }
    protected Enumeration filter(Enumeration en, final QueueProcess filter) {
        en = new AlterEnumeration(en) {
            public Object alter(Object alter) {
                return filter.process(alter, null);
            }
        };
        
        return new FilterEnumeration(en);
    }
    
    protected Enumeration concat(Enumeration enumOfEnums) {
        return new SequenceEnumeration(enumOfEnums);
    }
    
    protected Enumeration empty() {
        return new EmptyEnumeration();
    }
    
    protected Enumeration queue(Collection init, final QueueProcess process) {
        final HashMap diff = new HashMap();
        
        class QEAdd extends QueueEnumeration implements Collection {
            protected void process(Object obj) {
                Object different = process.process(obj, this);
                if (different != obj) {
                    diff.put(obj, different);
                }
            }
            
            public boolean add(Object o) {
                put(o);
                return true;
            }
            
            public boolean addAll(Collection c) {
                put(c.toArray());
                return true;
            }
            
            public void clear() {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean contains(Object o) {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean containsAll(Collection c) {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean isEmpty() {
                throw new IllegalStateException("Unsupported");
            }
            
            public Iterator iterator() {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean remove(Object o) {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean removeAll(Collection c) {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean retainAll(Collection c) {
                throw new IllegalStateException("Unsupported");
            }
            
            public int size() {
                throw new IllegalStateException("Unsupported");
            }
            
            public Object[] toArray() {
                throw new IllegalStateException("Unsupported");
            }
            
            public Object[] toArray(Object[] a) {
                throw new IllegalStateException("Unsupported");
            }
        }
        QEAdd qe = new QEAdd();
        qe.put(init.toArray());
        
        class Change extends AlterEnumeration {
            public Change(Enumeration en) {
                super(en);
            }
            
            public Object alter(Object o) {
                if (diff.keySet().contains(o)) {
                    return diff.remove(o);
                }
                return o;
            }
        }
        
        return new Change(qe);
    }
    
}
