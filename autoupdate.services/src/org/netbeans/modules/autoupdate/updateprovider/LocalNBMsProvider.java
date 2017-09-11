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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
            Map<String, UpdateItem> items = null;
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
            for (String id : items.keySet ()) {
                res.put (id, items.get (id));
            }
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
