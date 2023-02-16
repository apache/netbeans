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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek
 */
public class LocalNBMsProvider implements UpdateProvider {
    private String name;
    private File [] nbms;
    private static final Logger err = Logger.getLogger (LocalNBMsProvider.class.getName());
    
    /** Creates a new instance of LocalNBMsProvider */
    public LocalNBMsProvider (String name, File... files) {
        this.nbms = files;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return getName ();
    }

    public String getDescription () {
        return null;
    }

    public Map<String, UpdateItem> getUpdateItems() {
        Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
        for (int i = 0; i < nbms.length; i++) {
            Map<String, UpdateItem> items;
            try {
                items = AutoupdateInfoParser.getUpdateItems (nbms [i]);
            } catch (IOException ex) {
                throw new RuntimeException (ex.getMessage(), ex);
            } catch (SAXException ex) {
                throw new RuntimeException (ex.getMessage(), ex);
            }
            assert items != null;
            if(items.size()!=1) {
                err.log(Level.INFO, "File " + nbms [i] + " contains not single items: " + items);
            }
            res.putAll(items);
        }
        return res;
    }

    public boolean refresh (boolean force) {
        assert false : "Not supported yet.";
        return false;
    }

    public CATEGORY getCategory() {
        return CATEGORY.COMMUNITY;
    }
}
