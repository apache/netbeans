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
package org.netbeans.modules.cloud.oracle.adm;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
public class KnowledgeBaseItem extends OCIItem implements URLProvider{
    
    private static Map<OCID, Collection<Reference<KnowledgeBaseItem>>> itemInstances = new HashMap<>();
    
    protected final Date timeUpdated;
    protected final String compartmentId;
    
    public KnowledgeBaseItem(OCID id, String compartmentId, String displayName, Date timeUpdated) {
        super(id, displayName);
        this.timeUpdated = timeUpdated;
        this.compartmentId = compartmentId;
        registerItem();
    }
    
    public String getCompartmentId() {
        return compartmentId;
    }
    
    void registerItem() {
        synchronized (KnowledgeBaseItem.class) {
            itemInstances.computeIfAbsent(getKey(), x -> new ArrayList<>()).add(new WeakReference<>(this));
        }
    }
    
    @Override
    public URL getURL() {
        try {
            return new URL("https://cloud.oracle.com/adm/knowledgeBases/" + getKey().getValue());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public int maxInProject() {
        return 1;
    }
    
    static Collection<KnowledgeBaseItem> findKnownInstances(OCID ocid) {
        Collection<KnowledgeBaseItem> items = new ArrayList<>();
        
        synchronized (KnowledgeBaseItem.class) {
            Collection<Reference<KnowledgeBaseItem>> refItems = itemInstances.get(ocid);
            if (refItems == null) {
                return Collections.emptyList();
            }
            for (Iterator<Reference<KnowledgeBaseItem>> it = refItems.iterator(); it.hasNext(); ) {
                Reference<KnowledgeBaseItem> r = it.next();
                KnowledgeBaseItem i = r.get();
                if (i == null) {
                    it.remove();
                } else {
                    items.add(i);
                }
            }
        }
        return items;
    }
}