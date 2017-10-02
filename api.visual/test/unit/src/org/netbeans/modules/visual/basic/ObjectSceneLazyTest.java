/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.visual.basic;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.junit.NbTestCase;

/**
 * Tests behaviour of ObjectScene with lazy widgets.
 *
 * @author sdedic
 */
@SuppressWarnings("RedundantStringConstructorCall")
public class ObjectSceneLazyTest extends NbTestCase  {
    static class S extends ObjectScene {
        public ObjectState findObjectState(Object o) throws IllegalArgumentException {
            return super.findObjectState(o);
        }
    }
    
    private S scene;
    
    public ObjectSceneLazyTest(String name) {
        super(name);
    }
    
    static class SceneAdapter implements ObjectSceneListener {
        int added;
        int removed;
        int stateChanged;
        int selChanged;
        int highlightChanged;
        int hoverChanged;
        int focusChanged;
        ObjectState state;
        Object expectedObject;

        public SceneAdapter(Object expectedObject) {
            this.expectedObject = expectedObject;
        }
        
        @Override
        public void objectAdded(ObjectSceneEvent event, Object addedObject) {
            added++;
        }

        @Override
        public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
            removed++;
        }

        @Override
        public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
            assertSame(expectedObject, changedObject);
            state = newState;
            stateChanged++;
        }

        @Override
        public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
            selChanged++;
        }

        @Override
        public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
            highlightChanged++;
        }

        @Override
        public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
            hoverChanged++;
        }

        @Override
        public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
            focusChanged++;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        scene = new S();
    }
    
    /**
     * Attempts to focus a lazy object - nonexistent object without widgets
     */
    public void testHighlightLazyObject() throws Exception {
        Object item = new String("lazyItem");
        
        SceneAdapter ad = new SceneAdapter(item) {
            @Override
            public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
                super.highlightingChanged(event, previousHighlighting, newHighlighting);
                assertEquals(1, newHighlighting.size());
                assertSame(item, newHighlighting.iterator().next());
            }
        };
        scene.addObjectSceneListener(ad, ObjectSceneEventType.values());
        
        assertNull(scene.getObjectState(item));
        assertFalse(scene.findObjectState(item).isHighlighted());
        
        scene.setHighlightedObjects(Collections.singleton(item));
        
        assertEquals(0, ad.added);
        assertEquals(0, ad.removed);
        assertEquals(0, ad.focusChanged);
        assertEquals(1, ad.highlightChanged);
        assertEquals(0, ad.hoverChanged);
        assertEquals(1, ad.stateChanged);
        
        assertEquals(1, scene.getHighlightedObjects().size());
        assertSame(item, scene.getHighlightedObjects().iterator().next());
        assertTrue(scene.getObjectState(item).isHighlighted());
    }
    
    /**
     * Attempts to focus a lazy object - nonexistent object without widgets
     */
    public void testHoverLazyObject() throws Exception {
        Object item = new String("lazyItem");
        
        SceneAdapter ad = new SceneAdapter(item) {
            @Override
            public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
                super.hoverChanged(event, previousHoveredObject, newHoveredObject);
                assertSame(item, newHoveredObject);
            }
        };
        scene.addObjectSceneListener(ad, ObjectSceneEventType.values());
        
        assertNull(scene.getObjectState(item));
        assertFalse(scene.findObjectState(item).isHovered());
        
        scene.setHoveredObject(item);
        
        assertEquals(0, ad.added);
        assertEquals(0, ad.removed);
        assertEquals(0, ad.focusChanged);
        assertEquals(0, ad.highlightChanged);
        assertEquals(1, ad.hoverChanged);
        assertEquals(1, ad.stateChanged);
        
        assertSame(item,scene.getHoveredObject());
        assertTrue(scene.getObjectState(item).isHovered());
    }
    
    /**
     * Attempts to focus a lazy object - nonexistent object without widgets
     */
    public void testFocusLazyObject() throws Exception {
        Object item = new String("lazyItem");
        
        SceneAdapter ad = new SceneAdapter(item) {
            @Override
            public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
                super.focusChanged(event, previousFocusedObject, newFocusedObject); 
                assertSame(item, newFocusedObject);
            }
        };
        scene.addObjectSceneListener(ad, ObjectSceneEventType.values());
        
        assertNull(scene.getObjectState(item));
        assertFalse(scene.findObjectState(item).isFocused());
        
        scene.setFocusedObject(item);
        
        assertEquals(0, ad.added);
        assertEquals(0, ad.removed);
        assertEquals(1, ad.focusChanged);
        assertEquals(0, ad.highlightChanged);
        assertEquals(0, ad.hoverChanged);
        assertEquals(1, ad.stateChanged);
        
        assertSame(item,scene.getFocusedObject());
        assertTrue(scene.getObjectState(item).isFocused());
    }
    
    /**
     * Checks that a lazy object can be selected
     */
    public void testSelectLazyObject() throws Exception {
        Object item = new String("lazyItem");
        
        SceneAdapter ad = new SceneAdapter(item) {
            @Override
            public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
                super.selectionChanged(event, previousSelection, newSelection);
                assertEquals(1, newSelection.size());
                assertSame(item, newSelection.iterator().next());
            }
        };
        scene.addObjectSceneListener(ad, ObjectSceneEventType.values());
        
        assertNull(scene.getObjectState(item));
        assertFalse(scene.findObjectState(item).isSelected());
        
        scene.setSelectedObjects(Collections.singleton(item));
        
        assertEquals(0, ad.added);
        assertEquals(0, ad.removed);
        assertEquals(0, ad.focusChanged);
        assertEquals(0, ad.highlightChanged);
        assertEquals(0, ad.hoverChanged);
        assertEquals(1, ad.stateChanged);
        
        assertEquals(1, scene.getSelectedObjects().size());
        assertSame(item, scene.getSelectedObjects().iterator().next());
        assertTrue(scene.getObjectState(item).isSelected());
    }
    
    /**
     * Lazy object should have no widget mapping
     */
    public void testLazyObjectHasNoMapping() throws Exception {
        Object item = new String("lazyItem");
        scene.setSelectedObjects(Collections.singleton(item));
        scene.setHighlightedObjects(Collections.singleton(item));
        scene.setHoveredObject(item);
        scene.setFocusedObject(item);
        
        assertFalse(scene.getObjects().contains(item));
    }

    /**
     * Checks that lazy object will maintain the state even if
     * a widget is registered
     */
    public void testRegisterWidgetTransfersState() throws Exception {
        Object item = new String("lazyItem");
        scene.setSelectedObjects(Collections.singleton(item));
        scene.setHighlightedObjects(Collections.singleton(item));
        scene.setHoveredObject(item);
        scene.setFocusedObject(item);
        
        Widget w = new LabelWidget(scene);
        scene.addObject(item, w);
        
        ObjectState s = scene.getObjectState(item);
        assertTrue(s.isSelected());
        assertTrue(s.isHighlighted());
        assertTrue(s.isHovered());
        assertTrue(s.isFocused());
        
        ObjectState ws = w.getState();
        assertTrue(ws.isSelected());
        assertTrue(ws.isHighlighted());
        assertTrue(ws.isHovered());
        assertTrue(ws.isFocused());
    }
    
    public void testRegularRemoveObject() throws Exception {
        Object item = new String("lazyItem");
        scene.setSelectedObjects(Collections.singleton(item));
        scene.setHighlightedObjects(Collections.singleton(item));
        scene.setHoveredObject(item);
        scene.setFocusedObject(item);
        
        Widget w = new LabelWidget(scene);
        scene.addObject(item, w);

        ObjectState s = scene.getObjectState(item);
        assertTrue(s.isSelected());
        assertTrue(s.isHighlighted());
        assertTrue(s.isHovered());
        assertTrue(s.isFocused());

        SceneAdapter ad = new SceneAdapter(item);
        scene.addObjectSceneListener(ad, ObjectSceneEventType.values());
        
        scene.removeObject(item);
        assertEquals(0, ad.added);
        assertEquals(1, ad.removed);

        assertEquals(1, ad.focusChanged);
        assertEquals(1, ad.highlightChanged);
        assertEquals(1, ad.hoverChanged);
        assertEquals(1, ad.selChanged);
        assertEquals(4, ad.stateChanged);
        
        assertNull(scene.getHoveredObject());
        assertNull(scene.getFocusedObject());
        assertTrue(scene.getSelectedObjects().isEmpty());
        assertTrue(scene.getHighlightedObjects().isEmpty());
        
    }
    
    public void testRemoveObjectMapping() {
        Object item = new String("lazyItem");
        scene.setSelectedObjects(Collections.singleton(item));
        scene.setHighlightedObjects(Collections.singleton(item));
        scene.setHoveredObject(item);
        scene.setFocusedObject(item);
        
        Widget w = new LabelWidget(scene);
        scene.addObject(item, w);

        ObjectState s = scene.getObjectState(item);
        assertTrue(s.isSelected());
        assertTrue(s.isHighlighted());
        assertTrue(s.isHovered());
        assertTrue(s.isFocused());

        SceneAdapter ad = new SceneAdapter(item);
        scene.addObjectSceneListener(ad, ObjectSceneEventType.values());
        
        scene.removeObjectMapping(item);
        assertEquals(0, ad.added);
        assertEquals(1, ad.removed);

        assertEquals(0, ad.focusChanged);
        assertEquals(0, ad.highlightChanged);
        assertEquals(0, ad.hoverChanged);
        assertEquals(0, ad.selChanged);
        assertEquals(0, ad.stateChanged);

        s = scene.getObjectState(item);
        assertTrue(s.isSelected());
        assertTrue(s.isHighlighted());
        assertTrue(s.isHovered());
        assertTrue(s.isFocused());
        
        assertSame(item, scene.getHoveredObject());
        assertSame(item, scene.getFocusedObject());
        assertSame(item, scene.getSelectedObjects().iterator().next());
        assertSame(item, scene.getHighlightedObjects().iterator().next());
        
    }
}
