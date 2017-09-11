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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
