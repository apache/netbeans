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

package org.netbeans.modules.php.project.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.php.project.ui.options.PhpOptions;

/**
 * @author Petr Hrebejk, Tomas Mysik
 */
public final class GlobalIncludePathSupport extends BasePathSupport {
    private static final GlobalIncludePathSupport INSTANCE = new GlobalIncludePathSupport();

    private GlobalIncludePathSupport() {
    }

    public static GlobalIncludePathSupport getInstance() {
        return INSTANCE;
    }

    public Iterator<Item> itemsIterator() {
        // XXX more performance friendly impl. would return a lazzy iterator
        return itemsList().iterator();
    }

    public List<Item> itemsList() {
        String[] pe = PhpOptions.getInstance().getPhpGlobalIncludePathAsArray();
        List<Item> items = new ArrayList<>(pe.length);
        for (String p : pe) {
            Item item = null;
            File f = new File(p);
            if (!f.exists()) {
                item = Item.createBroken(p, p);
            } else {
                item = Item.create(p, p);
            }
            items.add(item);
        }
        return items;
    }

    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     */
    public String[] encodeToStrings(Iterator<Item> classpath) {
        List<String> result = new ArrayList<>();
        while (classpath.hasNext()) {
            Item item = classpath.next();
            result.add(item.getFilePath());
        }

        String[] items = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            if (i < result.size() - 1) {
                items[i] = result.get(i) + ":"; // NOI18N
            } else  {
                items[i] = result.get(i);
            }
        }
        return items;
    }
}
