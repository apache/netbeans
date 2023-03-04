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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.widget.Scene;

/**
 * Class ExpanderWidget provides a simple icon widget for controlling the
 * expanded/collapsed state of another widget. This widget can be added to
 * any widget, and given an instance of ExpandableWidget, it can make that
 * object expand or collapse each time the icon is clicked by the user.
 * It is up to the ExpandableWidget implementation to determine how the
 * size of the widget is altered. This class provides one or more methods
 * for altering the widget size.
 *
 * @author Ajit Bhate
 * @author radval
 * @author Nathan Fiedler
 */
public class ExpanderWidget extends ButtonWidget {

    /** Cache of the expanded state of ExpandableWidget instances. This
     * is used to restore the original state of an expandable if it is
     * created again, say as a result of an undo/redo operation. */
    private static Map<Object, Boolean> expandedCache;
    /** The expandable (content) widget. */
    private ExpandableWidget expandable;
    static {
        expandedCache = new WeakHashMap<Object, Boolean>();
    }

    /**
     * Creates a new instance of ExpanderWidget.
     *
     * @param  scene       the Scene to contain this widget.
     * @param  expandable  the expandable widget this expander will control.
     * @param  expanded    true if widget is initially expanded, false if collapsed.
     */
    public ExpanderWidget(Scene scene, ExpandableWidget expandable, boolean expanded) {
        super(scene, (String) null);
        this.expandable = expandable;
        setImage(new ExpanderImageWidget(scene, true, 8, 30));
        setSelectedImage(new ExpanderImageWidget(scene, false, 8, 30));
        setSelected(expanded);
        setRoundedBorder(0, 4, 4, null);
        setAction(new AbstractAction() {

            public void actionPerformed(ActionEvent arg0) {
                ExpanderWidget.this.expandable.setExpanded(!ExpanderWidget.this.expandable.isExpanded());
            }
        });
    }

    /**
     * Retrieve the former expanded state of the given expandable. If
     * the expandable state was not cached (or the cache has been cleaned
     * by the garbage collector), this method returns the value of the
     * <code>def</code> parameter.
     *
     * @param  expandable  the ExpandableWidget to query.
     * @param  def         default value for the expanded state.
     * @return  true if expanded, false if collapsed.
     */
    public static boolean isExpanded(ExpandableWidget expandable, boolean def) {
        Boolean val = expandedCache.get(expandable.hashKey());
        return val != null ? val : def;
    }

    /**
     * Set the expanded state of the widget.
     *
     * @param  expanded  true to expand, false to collapse.
     */
    @Override
    public void setSelected(boolean expanded) {
        // Save the state of the expandable in case it gets recreated later.
        expandedCache.put(expandable.hashKey(), expanded);
        super.setSelected(expanded);
        revalidate(true);
    }

    private static class ExpanderImageWidget extends ImageLabelWidget.PaintableImageWidget {

        private static final Stroke STROKE = new BasicStroke(2.5F, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

        private double cos;

        public ExpanderImageWidget(Scene scene, boolean up, int size, int degrees) {
            super(scene, BORDER_COLOR, size, size);
            double radians = Math.PI * degrees / 180.0;
            cos = Math.cos(radians / 2.0);
            if (!up) {
                cos = -cos;
            }
        }

        protected Stroke getImageStroke() {
            return STROKE;
        }

        protected Shape createImage(int width, int height) {
            GeneralPath path = new GeneralPath();
            float y1 = (float) (height+cos*height/2) / 2;
            float y2 = (float) (height-cos*height/2) / 2;
            path.moveTo(width / 2f, y1);
            path.lineTo(0, y2);
            path.moveTo(width / 2f, y1);
            path.lineTo(width, y2);
            return path;
        }
    }
}
