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
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 * @author Tomas Mysik
 */
public abstract class BaseProjectPathSupport extends BasePathSupport {

    private final PropertyEvaluator evaluator;
    private final ReferenceHelper referenceHelper;
    private final AntProjectHelper antProjectHelper;

    public BaseProjectPathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper,
            AntProjectHelper antProjectHelper) {
        assert evaluator != null;
        assert referenceHelper != null;
        assert antProjectHelper != null;

        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
    }

    protected abstract boolean isWellKnownPath(String p);

    public Iterator<Item> itemsIterator(String propertyValue) {
        // XXX more performance friendly impl. would return a lazzy iterator
        return itemsList(propertyValue).iterator();
    }

    public Iterator<Item> itemsIterator(String... propertyValues) {
        // XXX more performance friendly impl. would return a lazzy iterator
        return itemsList(propertyValues).iterator();
    }

    public List<Item> itemsList(String propertyValue) {
        return itemsList(PropertyUtils.tokenizePath(propertyValue == null ? "" : propertyValue)); // NOI18N
    }

    public List<Item> itemsList(String... values) {
        List<Item> items = new ArrayList<>(values.length);
        for (String p : values) {
            Item item;
            if (isWellKnownPath(p)) {
                // some well know classpath
                item = Item.create(p);
            } else {
                File f = null;
                String eval = evaluator.evaluate(p);
                if (eval != null) {
                    f = antProjectHelper.resolveFile(eval);
                }
                if (f == null || !f.exists()) {
                    item = Item.createBroken(eval, p);
                } else {
                    item = Item.create(eval, p);
                }
            }
            items.add(item);
        }
        return items;
    }

    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     */
    public String[] encodeToStrings(Iterator<Item> classpath) {
        return encodeToStrings(classpath, true);
    }

    public String[] encodeToStrings(Iterator<Item> classpath, boolean createReferences) {
        return encodeToStrings(classpath, createReferences, true);
    }

    public String[] encodeToStrings(Iterator<Item> classpath, boolean createReferences, boolean withPathSeparator) {
        List<String> result = new ArrayList<>();
        while (classpath.hasNext()) {
            Item item = classpath.next();
            String reference = item.getReference();
            switch (item.getType()) {
                case FOLDER:
                    if (reference == null) {
                        // new file
                        File file = new File(item.getFilePath());
                        if (createReferences) {
                            // pass null as expected artifact type to always get file reference
                            reference = referenceHelper.createForeignFileReference(file, null);
                        } else {
                            // simply use file path
                            reference = file.getPath();
                        }
                        item.property = reference;
                    }
                    break;
                case CLASSPATH:
                    // noop
                    break;
                default:
                    assert false : "Unknown classpath type: " + item.getType();
            }
            if (reference != null) {
                result.add(reference);
            }
        }

        String[] items = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            if (withPathSeparator
                    && i < result.size() - 1) {
                items[i] = result.get(i) + ":"; // NOI18N
            } else {
                items[i] = result.get(i);
            }
        }
        return items;
    }

}
