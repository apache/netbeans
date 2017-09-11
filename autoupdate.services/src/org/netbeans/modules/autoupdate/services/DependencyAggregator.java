/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jirka Rechtacek
 */
public class DependencyAggregator extends Object {
    private static Map<DependencyDecoratorKey, DependencyAggregator> key2dependency = new HashMap<DependencyDecoratorKey, DependencyAggregator> (11, 11);

    private Collection<ModuleInfo> depending = new HashSet<ModuleInfo>();
    private final DependencyDecoratorKey key;
    private static final Object LOCK = new Object();
    
    private DependencyAggregator (DependencyDecoratorKey key) {
        this.key = key;        
    }
    
    public static DependencyAggregator getAggregator (Dependency dep) {
        DependencyDecoratorKey key = new DependencyDecoratorKey (dep.getName (), dep.getType (), dep.getComparison ());
        synchronized(LOCK) {
            DependencyAggregator res = key2dependency.get (key);
            if (res == null) {
                res = new DependencyAggregator (key);
                key2dependency.put (key, res);
            }
            return res;
        }
    }
    
    public int getType () {
        return key.type;
    }
    
    public String getName () {
        return key.name;
    }
    
    public boolean addDependee(ModuleInfo dependee) {
        boolean result = false;
        synchronized (depending) {
            result = depending.add (dependee);
        }
        return result;
    }
    
    public Collection<ModuleInfo> getDependening() {
        return depending;
    }
    
    static void clearMaps() {
        key2dependency = new HashMap<DependencyDecoratorKey, DependencyAggregator> (11, 11);
    }
    
    @Override
    public String toString () {
        return "DependencyDecorator[" + key.toString () + "]";
    }
    
    public static Collection<UpdateUnit> getRequested (Dependency dep) {
        switch (dep.getType ()) {
            case Dependency.TYPE_MODULE :
                return Collections.singleton(UpdateManagerImpl.getInstance ().getUpdateUnit (dep.getName ()));
            case Dependency.TYPE_NEEDS :
            case Dependency.TYPE_REQUIRES :
            case Dependency.TYPE_RECOMMENDS :
                Collection<UpdateUnit> requestedUnits = new HashSet<UpdateUnit> ();
                Collection<ModuleInfo> installedProviders = UpdateManagerImpl.getInstance ().getInstalledProviders (dep.getName ());
                if (installedProviders.isEmpty ()) {
                    Collection<ModuleInfo> availableProviders = UpdateManagerImpl.getInstance ().getAvailableProviders (dep.getName ());
                    if (availableProviders.isEmpty ()) {
                        return null;
                    } else {
                        for (ModuleInfo mi : availableProviders) {
                            UpdateUnit availableUnit = UpdateManagerImpl.getInstance ().getUpdateUnit (mi.getCodeNameBase ());
                            if (availableUnit != null) {
                                requestedUnits.add(availableUnit);
                            }
                        }
                        return requestedUnits;
                    }
                } else {
                    for (ModuleInfo mi : installedProviders) {
                        UpdateUnit installedUnit = UpdateManagerImpl.getInstance ().getUpdateUnit (mi.getCodeNameBase ());
                        if (installedUnit != null) {
                            requestedUnits.add(installedUnit);
                        }
                    }
                    return requestedUnits;
                }
            case Dependency.TYPE_JAVA :
            case Dependency.TYPE_PACKAGE :
                break;
        }
        return null;
    }
    
    public static class DependencyDecoratorKey {
        private final String name;
        private final int type;//, comparison;
        private final int hashCode;
        public DependencyDecoratorKey (String name, int dependencyType, int comparison) {
            this.name = name;
            this.type = dependencyType;
            this.hashCode = 772067 ^ type ^ name.hashCode ();
            //this.comparison = comparison;
        }
        
        @Override
        public boolean equals (Object o) {
            if (o.getClass() != DependencyDecoratorKey.class) {
                return false;
            }

            DependencyDecoratorKey d = (DependencyDecoratorKey) o;

            return (type == d.type) && name.equals(d.name);
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString () {
        StringBuilder buf = new StringBuilder(100);
            buf.append ("Key[");
            
            if (type == Dependency.TYPE_MODULE) {
                buf.append("module "); // NOI18N
            } else if (type == Dependency.TYPE_PACKAGE) {
                buf.append("package "); // NOI18N
            } else if (type == Dependency.TYPE_REQUIRES) {
                buf.append("requires "); // NOI18N
            } else if (type == Dependency.TYPE_NEEDS) {
                buf.append("needs "); // NOI18N
            } else if (type == Dependency.TYPE_RECOMMENDS) {
                buf.append("recommends "); // NOI18N
            }

            buf.append(name);

            buf.append (']');

            return buf.toString();
        }
    }
}
