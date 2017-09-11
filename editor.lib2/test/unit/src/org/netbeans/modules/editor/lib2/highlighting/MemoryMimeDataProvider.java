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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.MimeDataProvider.class)
public final class MemoryMimeDataProvider implements MimeDataProvider {
    
    private static final HashMap<String, Lkp> CACHE = new HashMap<String, Lkp>();
    
    /** Creates a new instance of MemoryMimeDataProvider */
    public MemoryMimeDataProvider() {
    }

    public Lookup getLookup(MimePath mimePath) {
        return getLookup(mimePath.getPath(), true);
    }
    
    public static void addInstances(String mimePath, Object... instances) {
        assert mimePath != null : "Mime path can't be null";
        getLookup(mimePath, true).addInstances(instances);
    }
    
    public static void removeInstances(String mimePath, Object... instances) {
        assert mimePath != null : "Mime path can't be null";
        getLookup(mimePath, true).removeInstances(instances);
    }
    
    public static void reset(String mimePath) {
        if (mimePath == null) {
            synchronized (CACHE) {
                for(Lkp lookup : CACHE.values()) {
                    lookup.reset();
                }
            }
        } else {
            Lkp lookup = getLookup(mimePath, false);
            if (lookup != null) {
                lookup.reset();
            }
        }
    }
    
    private static Lkp getLookup(String mimePath, boolean create) {
        synchronized (CACHE) {
            Lkp lookup = CACHE.get(mimePath);
            if (lookup == null && create) {
                lookup = new Lkp();
                CACHE.put(mimePath, lookup);
            }
            return lookup;
        }
    }
    
    private static final class Lkp extends AbstractLookup {
        
        private ArrayList<Object> all = new ArrayList<Object>();
        private InstanceContent contents;
            
        public Lkp() {
            this(new InstanceContent());
        }
        
        private Lkp(InstanceContent ic) {
            super(ic);
            this.contents = ic;
        }
        
        public void addInstances(Object... instances) {
            all.addAll(Arrays.asList(instances));
            contents.set(all, null);
        }

        public void removeInstances(Object... instances) {
            ArrayList<Object> newAll = new ArrayList<Object>();
            
            loop:
            for(Object oo : all) {
                for(Object o : instances) {
                    if (o == oo) {
                        continue loop;
                    }
                }
                
                newAll.add(oo);
            }
            
            all = newAll;
            contents.set(all, null);
        }
        
        public void reset() {
            all.clear();
            contents.set(all, null);
        }
    } // End of Lkp class
}
