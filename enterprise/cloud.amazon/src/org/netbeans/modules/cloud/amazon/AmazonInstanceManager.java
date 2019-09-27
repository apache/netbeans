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
package org.netbeans.modules.cloud.amazon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.openide.util.ChangeSupport;

/**
 * Manager of all Amazon accounts registered in the IDE (usually just one).
 */
public class AmazonInstanceManager {

    private static final String AMAZON_IP_NAMESPACE = "cloud.amazon"; // NOI18N
    
    private static final String PREFIX = "org.netbeans.modules.cloud.amazon."; // NOI18N
    private static final String KEY_ID = "access-key-id"; // NOI18N
    private static final String KEY = "secret-access-key"; // NOI18N
    private static final String REGION = "region"; // NOI18N
    private static final String CODE = "code"; // NOI18N
    
    private static AmazonInstanceManager instance;
    private List<AmazonInstance> instances = new ArrayList<AmazonInstance>();
    private ChangeSupport listeners;
    
    private static final Logger LOG = Logger.getLogger(AmazonInstanceManager.class.getSimpleName());
    
    
    public static synchronized AmazonInstanceManager getDefault() {
        if (instance == null) {
            instance = new AmazonInstanceManager();
        }
        return instance;
    }
    
    private AmazonInstanceManager() {
        listeners = new ChangeSupport(this);
        /*if (getAmazonInstanceNames().size() == 0) {
            store(new AmazonInstance("test1", "somekey", "somepwd"));
            store(new AmazonInstance("some2", "somekey2", "somepwd2"));
            store(new AmazonInstance("last3", "somekey3", "somepwd3"));
        }*/
        init();
    }
    
    private void init() {
       instances.addAll(load());
       notifyChange();
    }
    
    private void notifyChange() {
       listeners.fireChange();
    }

    public List<AmazonInstance> getInstances() {
        return instances;
    }
    
    public void add(AmazonInstance ai) {
        store(ai);
        instances.add(ai);
        notifyChange();
    }
    
    private void store(AmazonInstance ai) {
        InstanceProperties props = InstancePropertiesManager.getInstance().createProperties(AMAZON_IP_NAMESPACE);
        
        Keyring.save(PREFIX+KEY_ID+"."+ai.getName(), ai.getKeyId().toCharArray(), "Amazon Access Key ID"); // NOI18N
        Keyring.save(PREFIX+KEY+"."+ai.getName(), ai.getKey().toCharArray(), "Amazon Secret Access Key"); // NOI18N
        
        props.putString("name", ai.getName()); // NOI18N
        props.putString("region", ai.getRegionURL()); // NOI18N
        props.putString("code", ai.getRegionCode()); // NOI18N
    }
    
    
    private static List<AmazonInstance> load() {
        List<AmazonInstance> result = new ArrayList<AmazonInstance>();
        for(InstanceProperties props : InstancePropertiesManager.getInstance().getProperties(AMAZON_IP_NAMESPACE)) {
            String name = props.getString("name", null); // NOI18N
            assert name != null : "Instance without name";
            String region = props.getString(REGION, null); // NOI18N
            String code = props.getString(CODE, null); // NOI18N
            
            if(code == null) {
                AmazonRegion r = (AmazonRegion) AmazonRegion.findRegion(region);
                code = r.getCode();
            }
            
            char ch[] = Keyring.read(PREFIX+KEY_ID+"."+name);
            if (ch == null) {
                LOG.log(Level.WARNING, "no access key id found for "+name);
                continue;
            }
            String keyId = new String(ch);
            ch = Keyring.read(PREFIX+KEY+"."+name);
            if (ch == null) {
                LOG.log(Level.WARNING, "no secret access key found for "+name);
                continue;
            }
            String key = new String(ch);
            result.add(new AmazonInstance(name, keyId, key, region, code));
        }
        return result;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }

    void remove(AmazonInstance ai) {
        for (InstanceProperties props : InstancePropertiesManager.getInstance().getProperties(AMAZON_IP_NAMESPACE)) {
            if (ai.getName().equals(props.getString("name", null))) { // NOI18N
                props.remove();
                break;
            }
        }
        instances.remove(ai);
        notifyChange();
    }
}
