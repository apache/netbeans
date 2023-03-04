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

package org.apache.tools.ant.module.api.support;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.Element;

/**
 * Convenience utilities for working with Ant scripts.
 * @since org.apache.tools.ant.module/3 3.31
 */
public class AntScriptUtils {

    private AntScriptUtils() {}

    /**
     * Loads a file believed to be an Ant script.
     * @param script a file assumed to be an Ant script
     * @return a handle for it (never null but may or may not be parsable)
     */
    public static AntProjectCookie antProjectCookieFor(FileObject script) {
        try {
            DataObject d = DataObject.find(script);
            AntProjectCookie apc = d.getCookie(AntProjectCookie.class);
            if (apc != null) {
                return apc;
            }
        } catch (DataObjectNotFoundException e) {
            assert false : e;
        }
        // AntProjectDataLoader probably not installed, e.g. from a unit test.
        // Or may not be recognized as an Ant script (e.g. root <project> element has no attributes).
        synchronized (antProjectCookies) {
            AntProjectCookie apc = antProjectCookies.get(script);
            if (apc == null) {
                apc = new AntProjectSupport(script);
                antProjectCookies.put(script, apc);
            }
            return apc;
        }
    }
    private static final Map<FileObject,AntProjectCookie> antProjectCookies = new WeakHashMap<FileObject,AntProjectCookie>();

    /**
     * Finds the name of an Ant script.
     * @param script Ant script to inspect
     * @return name of the Ant script as specified in the <code>name</code> attribute of
     *    the <code>project</code> element, or null if the file is not a valid Ant script
     *    or the script is anonymous
     */
    public static String getAntScriptName(FileObject script) {
        AntProjectCookie apc = antProjectCookieFor(script);
        Element projEl = apc.getProjectElement();
        if (projEl == null) {
            return null;
        }
        String name = projEl.getAttribute("name"); // NOI18N
        // returns "" if no such attribute
        return name.length() > 0 ? name : null;
    }

    /**
     * Finds the names of callable targets in an Ant script.
     * @param script Ant script to inspect
     * @return list of target names, sorted (by locale)
     * @throws IOException if the script cannot be inspected
     */
    public static List<String> getCallableTargetNames(FileObject script) throws IOException {
        AntProjectCookie apc = antProjectCookieFor(script);
        Set<TargetLister.Target> allTargets = TargetLister.getTargets(apc);
        SortedSet<String> targetNames = new TreeSet<String>(Collator.getInstance());
        for (TargetLister.Target target : allTargets) {
            if (target.isOverridden()) {
                // Cannot call it directly.
                continue;
            }
            if (target.isInternal()) {
                // Should not be called from outside.
                continue;
            }
            targetNames.add(target.getName());
        }
        return new ArrayList<String>(targetNames);
    }

}
