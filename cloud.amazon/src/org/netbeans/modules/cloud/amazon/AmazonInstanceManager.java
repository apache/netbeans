/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
    }
    
    
    private static List<AmazonInstance> load() {
        List<AmazonInstance> result = new ArrayList<AmazonInstance>();
        for(InstanceProperties props : InstancePropertiesManager.getInstance().getProperties(AMAZON_IP_NAMESPACE)) {
            String name = props.getString("name", null); // NOI18N
            assert name != null : "Instance without name";
            String region = props.getString(REGION, null); // NOI18N
            
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
            result.add(new AmazonInstance(name, keyId, key, region));
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
