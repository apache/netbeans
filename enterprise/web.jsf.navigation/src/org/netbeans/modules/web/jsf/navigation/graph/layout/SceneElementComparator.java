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
            sortedElements.sort(new SceneElementComparator(scene));
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
