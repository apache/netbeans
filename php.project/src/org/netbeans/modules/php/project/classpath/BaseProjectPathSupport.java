/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
