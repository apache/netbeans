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
package org.netbeans.modules.web.jsf.navigation.graph.layout;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.graph.layout.SceneElementComparator.SceneElement;

/**
 *
 * @author joelle
 */
public class SceneElementComparator implements Comparator<SceneElement> {

    private final PageFlowScene scene;

    public SceneElementComparator(PageFlowScene scene) {
        this.scene = scene;
    }

    @SuppressWarnings(value = "unchecked")
    public int compare(SceneElement s1, SceneElement s2) {
        assert s1 != null;
        assert s2 != null;

        Point p1 = s1.p;
        Point p2 = s2.p;

        assert p1 != null;
        assert p2 != null;

        PageFlowSceneElement e1 = s1.element;
        PageFlowSceneElement e2 = s2.element;

        /* if (p1 == null || p2 == null) {
        throw new IllegalArgumentException("Can not compare null value");
        }*/

        if (p1.x == p2.x) {
            if (p1.y == p2.y) {
                return scene.getIdentityCode(s1.element).compareTo(scene.getIdentityCode(s2.element));
            }
            return p1.y - p2.y;
        }
        return p1.x - p2.x;
    }

    public static class SceneElement {

        public PageFlowSceneElement element;
        public Point p;

        public SceneElement(PageFlowSceneElement element, Point p) {
            this.p = p;
            this.element = element;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                throw new IllegalArgumentException("Can not compare null object");
            }
            if (!(obj instanceof SceneElement)) {
                return false;
            }
            SceneElement e = (SceneElement) obj;
            if (!(element.equals(e.element) && p.equals(e.p))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.element != null ? this.element.hashCode() : 0);
            hash = 53 * hash + (this.p != null ? this.p.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return element.toString() + ":" + p.toString();
        }
    }

    public static PageFlowSceneElement getNextSelectableElement(PageFlowScene scene, boolean reverse, boolean nodesSelectable, boolean edgesSelectable, boolean pinsSelectable) {
        List<Object> selectedObjs = new ArrayList<Object>(scene.getSelectedObjects());
        List<Object> objs = new ArrayList<Object>();
        List<SceneElement> sortedElements = new ArrayList<SceneElement>();

        if (nodesSelectable) {
            objs.addAll(scene.getNodes());
        }
        if (edgesSelectable) {
            objs.addAll(scene.getEdges());
        }
        if (pinsSelectable) {
            objs.addAll(scene.getPins());
            /* Remove all the page pins */
            for (Page page : scene.getNodes()) {
                Pin pin = scene.getDefaultPin(page);
                sortedElements.remove(pin);
                objs.remove(pin);
            }
        }

        for (Object obj : objs) {
            Point p = scene.findWidget(obj).getLocation();
            SceneElement se = new SceneElement((PageFlowSceneElement) obj, p);
            sortedElements.add(se);
        }
        SceneElement mySelectedSceneElement = null;
        for (Object selObj : selectedObjs) {
            mySelectedSceneElement = new SceneElement((PageFlowSceneElement) selObj, scene.findWidget(selObj).getLocation());
            if (sortedElements.contains(mySelectedSceneElement)) {
                break;
            }
        }

        PageFlowSceneElement nextElement = null;
        if (!sortedElements.isEmpty()) {
            Collections.sort(sortedElements, new SceneElementComparator(scene));
            if (reverse) {
                Collections.reverse(sortedElements);
            }
            if (mySelectedSceneElement != null && sortedElements.contains(mySelectedSceneElement)) {
                int index = sortedElements.indexOf(mySelectedSceneElement);
                /*System.out.println("Index: " + index + " Selected Element: " + mySelectedSceneElement.element);*/
                if (sortedElements.size() > index + 1) {
                    nextElement = sortedElements.get(index + 1).element;
                } else {
                    return null; // Let me know if you are on the last one so I can send focus to the scene.
                }
            }
            if (nextElement == null) {
                nextElement = sortedElements.get(0).element;
            }
        }
        return nextElement;
    }
}
