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
package org.openide.filesystems;

import java.util.Arrays;
import javax.swing.Action;
import org.openide.modules.PatchFor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Retained compatibility with 8.0.1, bridges System actions to the 
 * fileset's Lookup.
 * 
 * @author sdedic
 */
@PatchFor(FileExtrasLkp.class)
public class CompatFileExtrasLkp extends AbstractLookup {
    private final InstanceContent ic;

    public CompatFileExtrasLkp() {
        this(new InstanceContent());
    }
    
    public CompatFileExtrasLkp(AbstractLookup.Content content) {
        super(content);
        this.ic = (InstanceContent)content;
    }
    
    FileExtrasLkp compat() {
        return (FileExtrasLkp)(Object)this;
    }
    
    FileSystemCompat fsCompat(FileSystem fs) {
        return (FileSystemCompat)(Object)fs;
    }

    @Override @SuppressWarnings("deprecation")
    protected void beforeLookup(Template<?> template) {
        if (Action.class.isAssignableFrom(template.getType())) {
            ic.set(Arrays.asList(
                    fsCompat(compat().fs).getActions(compat().set)), null);
        }
    }
}
