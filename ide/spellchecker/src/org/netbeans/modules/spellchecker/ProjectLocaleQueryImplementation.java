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
package org.netbeans.modules.spellchecker;

import java.util.Locale;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spellchecker.spi.LocaleQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.spellchecker.spi.LocaleQueryImplementation.class, position=1000)
public class ProjectLocaleQueryImplementation implements LocaleQueryImplementation {

    /** Creates a new instance of ProjectLocaleQueryImplementation */
    public ProjectLocaleQueryImplementation() {
    }

    public Locale findLocale(FileObject file) {
        Project p = FileOwnerQuery.getOwner(file);
        
        if (p != null) {
            LocaleQueryImplementation i = (LocaleQueryImplementation) p.getLookup().lookup(LocaleQueryImplementation.class);
            
            if (i != null) {
                return i.findLocale(file);
            }
        }
        
        return null;
    }
    
}
