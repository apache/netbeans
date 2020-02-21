/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.sendto.api;

import org.netbeans.modules.dlight.sendto.spi.Handler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.openide.util.NbBundle;

/**
 *
 */
public final class Configuration implements Cloneable, Comparable<Configuration> {

    private static final AtomicInteger lastID = new AtomicInteger();
    public static final String DISPLAY_NAME = "display_name"; // NOI18N
    public static final String HANDLER_ID = "script_handler_id"; // NOI18N
    // This id is not unique. (clones have the same id as an original)
    private final Integer id;
    private final Map<String, String> map = new HashMap<String, String>();
    private boolean modified = false;

    private Configuration(Integer id) {
        this.id = id;
        map.put(DISPLAY_NAME, NbBundle.getMessage(Configuration.class, "Configuration.default.DisplayName")); // NOI18N
    }

    public Configuration() {
        this(lastID.incrementAndGet());
    }

    @Override
    public Object clone() {
        Configuration clone = new Configuration(id);
        clone.map.putAll(map);
        return clone;
    }

    public Configuration copy() {
        Configuration copy = new Configuration();
        copy.map.putAll(map);
        copy.map.put(DISPLAY_NAME, map.get(DISPLAY_NAME) + "_copy"); // NOI18N
        return copy;
    }

    public boolean isModified() {
        return modified;
    }

    public String getName() {
        return map.get(DISPLAY_NAME);
    }

    public void setName(String name) {
        map.put(DISPLAY_NAME, name);
        modified = true;
    }

    public Handler getHandler() {
        return Handlers.getHandler(map.get(HANDLER_ID));
    }

    public String get(String key) {
        final String result = map.get(key);
        return result == null ? "" : result; // NOI18N
    }

    public void set(String key, String param) {
        map.put(key, param);
        modified = true;
    }

    public Integer getID() {
        return id;
    }

    public void setHandler(Handler handler) {
        map.put(HANDLER_ID, handler.getID());
        modified = true;
    }

    /*package*/ Map<String, String> getProperties() {
        return Collections.unmodifiableMap(map);
    }

    public void applyChanges() {
        getHandler().applyChanges(this);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public void setBoolean(String key, boolean param) {
        set(key, Boolean.toString(param));
    }

    @Override
    public int compareTo(Configuration that) {
        if (that == null) {
            return 1;
        }
        return this.getName().compareTo(that.getName());
    }
}
