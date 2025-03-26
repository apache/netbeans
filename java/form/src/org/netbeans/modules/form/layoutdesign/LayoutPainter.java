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
package org.netbeans.modules.form.layoutdesign;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;
import static org.netbeans.modules.form.layoutdesign.VisualState.GapInfo;

/**
 * This class takes care of painting layout information for selected components
 * (anchors, alignment, gaps).
 * 
 * @author Tomas Pavek, Jan Stola
 */
public class LayoutPainter implements LayoutConstants {
    private LayoutModel layoutModel;
    private VisualState visualState;

    private Map<LayoutComponent, Collection<GapInfo>> paintedGaps; // cached last painted gaps
    private boolean rootSelection;
    private Collection<LayoutComponent> componentsOfPaintedGaps;

    private Image linkBadgeBoth = null;
    private Image linkBadgeHorizontal = null;
    private Image linkBadgeVertical = null;

    private static final int BOTH_DIMENSIONS = 2;

    private Icon warningIcon;

    private static boolean PAINT_RES_GAP_MIN_SIZE;

    LayoutPainter(LayoutModel layoutModel, VisualState visualState) {
        this.layoutModel = layoutModel;
        this.visualState = visualState;
        layoutModel.addListener(new LayoutModel.Listener() {
            @Override
            public void layoutChanged(LayoutEvent ev) {
                int type = ev.getType();
                // cached data about painted gaps can survive simple change
                if (type != LayoutEvent.INTERVAL_SIZE_CHANGED
                        && type != LayoutEvent.INTERVAL_PADDING_TYPE_CHANGED) {
                    if (paintedGaps != null) {
                        paintedGaps.clear();
                    }
                    componentsOfPaintedGaps = null;
                }
            }
        });
    }

    /**
     * Paints layout information (anchor links and alignment in groups) for
     * given components.
     * @param selectedComponents Components selected in the designer.
     */
    void paintComponents(Graphics2D g, Collection<LayoutComponent> selectedComponents, boolean paintAlignment) {
        Shape originalClip = g.getClip();
        LayoutComponent parent = null;
        for (LayoutComponent comp : selectedComponents) {
            if (isDesignRootSelected(comp)) {
                continue;
            }
            if (paintAlignment) {
                if (comp.getParent() != parent) {
                    if (parent != null) {
                        g.setClip(originalClip);
                    }
                    parent = comp.getParent();
                    Shape clip = visualState.getComponentVisibilityClip(parent);
                    if (clip != null) {
                        g.clip(clip);
                    }
                }
                paintSelectedComponent(g, comp, HORIZONTAL);
                paintSelectedComponent(g, comp, VERTICAL);
            }
            if (LayoutComponent.isUnplacedComponent(comp)) {
                paintUnplacedWarningImage(g, comp);
            }
        }
        if (parent != null) {
            g.setClip(originalClip);
        }
    }

    private void paintSelectedComponent(Graphics2D g, LayoutComponent component, int dimension) {
        LayoutInterval interval = component.getLayoutInterval(dimension);
        LayoutInterval parent = interval.getParent();
        if (parent == null) {
            return; // workaround of bug 224402: repaint may happen before failed change is undone
        }
        if (component.isLinkSized(HORIZONTAL) || component.isLinkSized(VERTICAL)) {
            paintLinks(g, component);
        }
        // Paint baseline alignment
        if (interval.getAlignment() == BASELINE) {
            int oppDimension = (dimension == HORIZONTAL) ? VERTICAL : HORIZONTAL;
            LayoutRegion region = parent.getCurrentSpace();
            int x = region.positions[dimension][BASELINE];
            int y1 = region.positions[oppDimension][LEADING];
            int y2 = region.positions[oppDimension][TRAILING];
            if ((y1 != LayoutRegion.UNKNOWN) && (y2 != LayoutRegion.UNKNOWN)) {
                if (dimension == HORIZONTAL) {
                    g.drawLine(x, y1, x, y2);
                } else {
                    g.drawLine(y1, x, y2, x);
                }
            }
        }
        int lastAlignment = -1;
        do {
            if (parent.getType() == SEQUENTIAL) {
                int alignment = LayoutInterval.getEffectiveAlignment(interval);
                int index = parent.indexOf(interval);
                int start, end;
                switch (alignment) {
                    case LEADING:
                        start = 0;
                        end = index;
                        lastAlignment = LEADING;
                        break;
                    case TRAILING:
                        start = index + 1;
                        end = parent.getSubIntervalCount();
                        lastAlignment = TRAILING;
                        break;
                    default: switch (lastAlignment) {
                        case LEADING: start = 0; end = index; break;
                        case TRAILING: start = index+1; end = parent.getSubIntervalCount(); break;
                        default: start = 0; end = parent.getSubIntervalCount(); break;
                    }
                }
                for (int i=start; i<end; i++) {
                    LayoutInterval candidate = parent.getSubInterval(i);
                    if (candidate.isEmptySpace()) {
                        paintAlignment(g, candidate, dimension, LayoutInterval.getEffectiveAlignment(candidate));
                    }
                }
            } else {
                int alignment = interval.getAlignment();
                if (!LayoutInterval.wantResizeInLayout(interval)) {
                    lastAlignment = alignment;
                }
                paintAlignment(g, interval, dimension, lastAlignment);
            }
            interval = parent;
            parent = interval.getParent();
        } while (parent != null);
    }

    private void paintUnplacedWarningImage(Graphics2D g, LayoutComponent comp) {
        LayoutRegion region = comp.getCurrentSpace();
        Rectangle rect = region.toRectangle(new Rectangle());
        Icon icon = getWarningIcon();
        icon.paintIcon(null, g, rect.x+rect.width-icon.getIconWidth(), rect.y);
    }

    private Icon getWarningIcon() {
        if (warningIcon == null) {
            warningIcon = ImageUtilities.loadIcon("org/netbeans/modules/form/layoutsupport/resources/warning.png"); // NOI18N
        }
        return warningIcon;
    }
    
    private void paintLinks(Graphics2D g, LayoutComponent component) {
        if ((component.isLinkSized(HORIZONTAL)) && (component.isLinkSized(VERTICAL))) {
            Map<Integer,List<String>> linkGroupsH = layoutModel.getLinkSizeGroups(HORIZONTAL);            
            Map<Integer,List<String>> linkGroupsV = layoutModel.getLinkSizeGroups(VERTICAL);
            Integer linkIdH = new Integer(component.getLinkSizeId(HORIZONTAL));
            Integer linkIdV = new Integer(component.getLinkSizeId(VERTICAL));
            
            List<String> lH = linkGroupsH.get(linkIdH);
            List<String> lV = linkGroupsV.get(linkIdV);

            Set<String> merged = new HashSet<String>(); 
            for (int i=0; i < lH.size(); i++) {
                merged.add(lH.get(i));
            }
            for (int i=0; i < lV.size(); i++) {
                merged.add(lV.get(i));
            }

            Iterator<String> mergedIt = merged.iterator();
            while (mergedIt.hasNext()) {
                String id = mergedIt.next();
                LayoutComponent lc = layoutModel.getLayoutComponent(id);
                LayoutInterval interval = lc.getLayoutInterval(HORIZONTAL);
                LayoutRegion region = interval.getCurrentSpace();
                Image badge = null;
                if ((lV.contains(id)) && (lH.contains(id))) {
                    badge = getLinkBadge(BOTH_DIMENSIONS);
                } else {
                    if (lH.contains(lc.getId())) {
                        badge = getLinkBadge(HORIZONTAL);
                    }
                    if (lV.contains(lc.getId())) {
                        badge = getLinkBadge(VERTICAL);
                    }
                }
                int x = region.positions[HORIZONTAL][TRAILING] - region.size(HORIZONTAL) / 4  - (badge.getWidth(null) / 2);
                int y = region.positions[VERTICAL][LEADING] - (badge.getHeight(null));
                g.drawImage(badge, x, y, null);
            }
        } else {
            int dimension = (component.isLinkSized(HORIZONTAL)) ? HORIZONTAL : VERTICAL;
            Map map =  layoutModel.getLinkSizeGroups(dimension);
            
            Integer linkId = new Integer(component.getLinkSizeId(dimension));
            List l = (List)map.get(linkId);
            Iterator mergedIt = l.iterator();
            
            while (mergedIt.hasNext()) {
                String id = (String)mergedIt.next();
                LayoutComponent lc = layoutModel.getLayoutComponent(id);
                LayoutInterval interval = lc.getLayoutInterval(dimension);
                LayoutRegion region = interval.getCurrentSpace();
                Image badge = getLinkBadge(dimension);
                int x = region.positions[HORIZONTAL][TRAILING] - region.size(HORIZONTAL) / 4 - (badge.getWidth(null) / 2);
                int y = region.positions[VERTICAL][LEADING] - (badge.getHeight(null));
                g.drawImage(badge, x, y, null);
            }
        }
    }
    
    private Image getLinkBadge(int dimension) {
        if (dimension == (BOTH_DIMENSIONS)) {
            if (linkBadgeBoth == null) {
                linkBadgeBoth = ImageUtilities.loadImage("org/netbeans/modules/form/resources/sameboth.png"); //NOI18N
            }
            return linkBadgeBoth;
        }
        if (dimension == HORIZONTAL) {
            if (linkBadgeHorizontal == null) {
                linkBadgeHorizontal = ImageUtilities.loadImage("org/netbeans/modules/form/resources/samewidth.png"); //NOI18N
            }
            return linkBadgeHorizontal;
        }
        if (dimension == VERTICAL) {
            if (linkBadgeVertical == null) {
                linkBadgeVertical = ImageUtilities.loadImage("org/netbeans/modules/form/resources/sameheight.png"); //NOI18N
            }
            return linkBadgeVertical;
        }
        return null;
    }
    
    private void paintAlignment(Graphics2D g, LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval parent = interval.getParent();
        boolean baseline = parent.isParallel() && (parent.getGroupAlignment() == BASELINE);
        LayoutRegion group = parent.getCurrentSpace();
        int opposite = (dimension == HORIZONTAL) ? VERTICAL : HORIZONTAL;
        int x1, x2, y;
        if (interval.isEmptySpace()) {
            int index = parent.indexOf(interval);
            int[] ya, yb;
            boolean x1group, x2group;
            if (index == 0) {
                x1 = group.positions[dimension][baseline ? BASELINE : LEADING];
                ya = visualIntervalPosition(parent, opposite, LEADING);
                x1group = LayoutInterval.getFirstParent(interval, PARALLEL).getParent() != null;
            } else {
                LayoutInterval x1int = parent.getSubInterval(index-1);
                if (x1int.isParallel() && (x1int.getGroupAlignment() == BASELINE)) {
                    x1 = x1int.getCurrentSpace().positions[dimension][BASELINE];
                } else {
                    if (x1int.isEmptySpace()) return;
                    x1 = x1int.getCurrentSpace().positions[dimension][TRAILING];
                }
                ya = visualIntervalPosition(x1int, opposite, TRAILING);
                x1group = x1int.isGroup();
            }
            if (index + 1 == parent.getSubIntervalCount()) {
                x2 = group.positions[dimension][baseline ? BASELINE : TRAILING];
                yb = visualIntervalPosition(parent, opposite, TRAILING);
                x2group = LayoutInterval.getFirstParent(interval, PARALLEL).getParent() != null;
            } else {
                LayoutInterval x2int = parent.getSubInterval(index+1);
                if (x2int.isParallel() && (x2int.getGroupAlignment() == BASELINE)) {
                    x2 = x2int.getCurrentSpace().positions[dimension][BASELINE];
                } else {
                    if (x2int.isEmptySpace()) return;
                    x2 = x2int.getCurrentSpace().positions[dimension][LEADING];
                }
                yb = visualIntervalPosition(x2int, opposite, LEADING);
                x2group = x2int.isGroup();
            }
            if ((x1 == LayoutRegion.UNKNOWN) || (x2 == LayoutRegion.UNKNOWN)) return;
            int y1 = Math.min(ya[1], yb[1]);
            int y2 = Math.max(ya[0], yb[0]);
            y = (y1 + y2)/2;
            if ((ya[1] < yb[0]) || (yb[1] < ya[0])) {
                // no intersection
                if (dimension == HORIZONTAL) {
                    g.drawLine(x1, ya[0], x1, y);
                    g.drawLine(x1, ya[0], x1, ya[1]);
                    g.drawLine(x2, yb[0], x2, y);
                    g.drawLine(x2, yb[0], x2, yb[1]);
                } else {
                    g.drawLine(ya[0], x1, y, x1);
                    g.drawLine(ya[0], x1, ya[1], x1);
                    g.drawLine(yb[0], x2, y, x2);
                    g.drawLine(yb[0], x2, yb[1], x2);
                }
            } else {
                if (dimension == HORIZONTAL) {
                    if (x1group) g.drawLine(x1, ya[0], x1, ya[1]);
                    if (x2group) g.drawLine(x2, yb[0], x2, yb[1]);
                } else {
                    if (x1group) g.drawLine(ya[0], x1, ya[1], x1);
                    if (x2group) g.drawLine(yb[0], x2, yb[1], x2);
                }
            }
        } else {
            LayoutRegion child = interval.getCurrentSpace();
            if ((alignment == LEADING) || (alignment == TRAILING)) {
                x1 = group.positions[dimension][baseline ? BASELINE : alignment];
                if (interval.isParallel() && (interval.getAlignment() == BASELINE)) {
                    x2 = child.positions[dimension][BASELINE];
                } else {
                    x2 = child.positions[dimension][alignment];
                }
            } else {
                return;
            }
            if ((x1 == LayoutRegion.UNKNOWN) || (x2 == LayoutRegion.UNKNOWN)) return;
            int[] pos = visualIntervalPosition(parent, opposite, alignment);
            y = (pos[0] + pos[1])/2;
            int xa = group.positions[dimension][LEADING];
            int xb = group.positions[dimension][TRAILING];
            if (parent.getParent() != null) {
                if (dimension == HORIZONTAL) {
                    if (alignment == LEADING) {
                        g.drawLine(xa, pos[0], xa, pos[1]);
                    } else if (alignment == TRAILING) {
                        g.drawLine(xb, pos[0], xb, pos[1]);
                    }
                } else {
                    if (alignment == LEADING) {
                        g.drawLine(pos[0], xa, pos[1], xa);
                    } else if (alignment == TRAILING) {
                        g.drawLine(pos[0], xb, pos[1], xb);
                    }
                }
            }
        }
        // Avoid overload of EQ when current space is incorrectly calculated.
        if ((x2 - x1 > 1) && (Math.abs(y) <= Short.MAX_VALUE)
            && (Math.abs(x1) <= Short.MAX_VALUE) && (Math.abs(x2) <= Short.MAX_VALUE)) {
            int x, angle;            
            if (alignment == LEADING) {
                x = x1;
                angle = 180;
            } else {
                x = x2;
                angle = 0;
            }
            x2--;
            int diam = Math.min(4, x2-x1);
            Stroke oldStroke = g.getStroke();
            Stroke dottedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {1, 1}, 0);
            g.setStroke(dottedStroke);
            if (dimension == HORIZONTAL) {
                g.drawLine(x1, y, x2, y);
                angle += 90;
            } else {
                g.drawLine(y, x1, y, x2);
                int temp = x; x = y; y = temp;
            }
            g.setStroke(oldStroke);
            if ((alignment == LEADING) || (alignment == TRAILING)) {
                Object hint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.fillArc(x-diam, y-diam, 2*diam, 2*diam, angle, 180);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, hint);
            }
        }
    }
    
    private int[] visualIntervalPosition(LayoutInterval interval, int dimension, int alignment) {
        int min = Short.MAX_VALUE;
        int max = Short.MIN_VALUE;
        if (interval.isParallel() && (interval.getGroupAlignment() != BASELINE)) {
            Iterator iter = interval.getSubIntervals();
            while (iter.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)iter.next();
                int imin, imax;
                int oppDim = (dimension == HORIZONTAL) ? VERTICAL : HORIZONTAL;
                if (LayoutInterval.isPlacedAtBorder(subInterval, oppDim, alignment)) {
                    if (subInterval.isParallel()) {
                        int[] ipos = visualIntervalPosition(subInterval, dimension, alignment);
                        imin = ipos[0]; imax = ipos[1];
                    } else if (!subInterval.isEmptySpace()) {
                        LayoutRegion region = subInterval.getCurrentSpace();
                        imin = region.positions[dimension][LEADING];
                        imax = region.positions[dimension][TRAILING];                        
                    } else {
                        imin = min; imax = max;
                    }
                } else {
                    imin = min; imax = max;
                }
                if (min > imin) min = imin;
                if (max < imax) max = imax;
            }
        }
        if (!interval.isParallel() || (min == Short.MAX_VALUE)) {
            LayoutRegion region = interval.getCurrentSpace();
            min = region.positions[dimension][LEADING];
            max = region.positions[dimension][TRAILING];
        }
        return new int[] {min, max};
    }

    // -----

    // Rules for painting gaps
    // 1) In a base situation the gaps are painted for the selected component:
    //   a) The gaps around the component in its parent layout.
    //   b) All gaps inside the component if it is a container.
    // 2) If some gap is selected then only gaps from its container are painted.
    //    So e.g. if a gap is selected inside a container, the outer gaps next
    //    the container are not painted. They'll be painted again when clicking
    //    in the container on a place without a gap or component.
    // 3) If a gap next to a selected component is selected, which also selects
    //    the container, then only gaps of the previosly selected components are
    //    painted, not all gaps of the container.

    Collection<GapInfo> getPaintedGapsForContainer(LayoutComponent container) {
        return paintedGaps != null ? paintedGaps.get(container) : null;
    }

    /**
     * @param selectedComponents selected components to paint the gaps for, all
     *        must have the same parent (otherwise empty collection should be passed)
     * @param selectedGap gap to paint as selected (or null)
     */
    void paintGaps(Graphics2D g, Collection<LayoutComponent> selectedComponents, GapInfo selectedGap) {
        if (selectedComponents == null || selectedComponents.isEmpty()) {
            if (paintedGaps != null) {
                paintedGaps.clear();
            }
            rootSelection = false;
            componentsOfPaintedGaps = null;
            return;
        }

        LayoutComponent parent = null;
        boolean root = true; // root can't paint its surrounding gaps in parent
        for (LayoutComponent selComp : selectedComponents) {
            if (!isDesignRootSelected(selComp)) {
                root = false;
                break;
            }
        }
        boolean newGaps = newGapsForNewSelection(selectedComponents, selectedGap, root);
        if (newGaps) {
            rootSelection = root;
            if (paintedGaps == null) {
                paintedGaps = new HashMap<LayoutComponent, Collection<GapInfo>>();
            } else {
                paintedGaps.clear();
            }
            componentsOfPaintedGaps = new ArrayList(selectedComponents);
        }

        // part 1: paint gaps inside selected components that are containers
        for (LayoutComponent comp : componentsOfPaintedGaps) {
            if (!rootSelection && parent == null) {
                parent = comp.getParent();
            }
            if (comp.isLayoutContainer()) {
                Collection<GapInfo> gaps = visualState.getContainerGaps(comp);
                if (gaps == null) {
                    continue; // bug #213936: moved container might be not built yet
                }
                for (GapInfo gapInfo : gaps) {
                    if (!newGaps && gapInfo.paintRect != null) {
                        break; // everything is cached and up-to-date
                    }
                    setPaintRectForGap(gapInfo);
                }
                if (paintedGaps.get(comp) == null) {
                    paintedGaps.put(comp, gaps);
                }
                if (selectedComponents.contains(comp)) {
                    paintGapsInContainer(g, comp, gaps, selectedGap, null);
                } // Don't paint inner gaps of this component if an outer gap has
                  // been selected for it in parent. Still have the gaps ready for
                  // the case the component is directly selected again.
            }
        }
        // part 2: paint gaps for selected components in their parent
        if (parent != null) {
            Collection<GapInfo> cached = paintedGaps.get(parent);
            Collection<GapInfo> gaps = (cached != null) ? cached : new LinkedList<GapInfo>();
            Map<LayoutInterval, GapInfo> gapMap = null;
            for (LayoutComponent comp : componentsOfPaintedGaps) {
                Collection<GapInfo> componentGaps = visualState.getComponentGaps(comp);
                if (componentGaps == null) {
                    continue; // bug #224531 workaround, maybe the component's container not built yet after move?
                }
                boolean cachedUpToDate = false;
                for (GapInfo gapInfo : componentGaps) {
                    if (cached != null && gapInfo.paintRect != null) {
                        cachedUpToDate = true;
                        break; // everything is up-to-date
                    }
                    setPaintRectForGap(gapInfo);
                    // There can be multiple GapInfo objects for one gap if multiple
                    // components are selected. Compute their union rectangle for visualization.
                    GapInfo paintRep = (gapMap != null) ? gapMap.get(gapInfo.gap) : null;
                    if (paintRep != null) {
                        expandOrtPaintRect(paintRep, gapInfo);
                    } else {
                        if (gapMap == null) {
                            gapMap = new HashMap<LayoutInterval, GapInfo>();
                        }
                        gapMap.put(gapInfo.gap, gapInfo);
                        if (cached == null) {
                            gaps.add(gapInfo);
                        }
                    }
                }
                if (cachedUpToDate) {
                    break;
                }
            }
            if (cached == null) {
                paintedGaps.put(parent, gaps);
            }
            if (selectedGap == null || gaps.contains(selectedGap)) {
                paintGapsInContainer(g, parent, gaps, selectedGap, null);
            } // Don't paint outer gaps if there is a selected inner gap in this
              // container. Still keep the gaps ready for the case the gap is
              // unselected by clicking elsewhere in the container.
        }
    }

    private boolean isDesignRootSelected(LayoutComponent component) {
        if (component.getParent() != null) {
            Collection<GapInfo> gapsInParent = visualState.getComponentGaps(component);
            return gapsInParent == null || gapsInParent.isEmpty();
            // or visualState.isComponentInDesignView(component.getParent()) ?
        } else {
            return true;
        }
    }

    private boolean newGapsForNewSelection(Collection<LayoutComponent> selectedComponents,
                                           GapInfo selectedGap, boolean root) {
        if (componentsOfPaintedGaps == null || componentsOfPaintedGaps.isEmpty()) {
            return true; // nothing cached so far
        }
        if (selectedComponents.size() == componentsOfPaintedGaps.size()) {
            boolean sameContentAndOrder = true;
            Iterator<LayoutComponent> it1 = selectedComponents.iterator();
            Iterator<LayoutComponent> it2 = componentsOfPaintedGaps.iterator();
            while (it1.hasNext()) {
                if (it1.next() != it2.next()) {
                    sameContentAndOrder = false;
                    break;
                }
            }
            if (sameContentAndOrder) { // same components selected
                return root != rootSelection; // true if the design root changed
            }
        }
        // component selection changed
        if (selectedGap == null) {
            return true;
        }
        // Check for special case when clicked on a gap next to a selected
        // component. Even though the container gets selected in such case,
        // we don't want to paint all container gaps, but only the gaps of
        // previously selected component (i.e. same gaps as so far). We remember
        // the components in 'componentsOfPaintedGaps' list.
        LayoutComponent maybeParent = null;
        for (LayoutComponent selComp : selectedComponents) {
            if (selComp.isLayoutContainer()) {
                for (LayoutComponent prevComp : componentsOfPaintedGaps) {
                    if (prevComp.getParent() == selComp) {
                        maybeParent = selComp;
                        break;
                    }
                }
                if (maybeParent != null) {
                    break;
                }
            }
        }
        if (maybeParent == null) {
            return true; // not the case, new selection is not parent of previously selected component
        }
        Collection<GapInfo> prevGaps = (paintedGaps != null) ? paintedGaps.get(maybeParent) : null;
        if (prevGaps == null || !prevGaps.contains(selectedGap)) {
            return true; // not the case, the selected gap does not belong to previously selected component
        }
        return false; // no change in painted gaps
    }

    private static void setPaintRectForGap(GapInfo gapInfo) {
        Rectangle r;
        if (gapInfo.dimension == HORIZONTAL) {
            r = new Rectangle(gapInfo.position, gapInfo.ortPositions[LEADING],
                    gapInfo.currentSize, gapInfo.ortPositions[TRAILING] - gapInfo.ortPositions[LEADING]);
        } else {
            r = new Rectangle(gapInfo.ortPositions[LEADING], gapInfo.position,
                    gapInfo.ortPositions[TRAILING] - gapInfo.ortPositions[LEADING], gapInfo.currentSize);
        }
        gapInfo.paintRect = r;
    }

    private static void expandOrtPaintRect(GapInfo gapInfo, GapInfo exp) {
        Rectangle r = gapInfo.paintRect;
        Rectangle er = exp.paintRect;
        if (exp.dimension == HORIZONTAL) {
            if (er.y < r.y) {
                r.height += r.y - er.y;
                r.y = er.y;
            }
            if (er.y + er.height > r.y + r.height) {
                r.height = er.y + er.height - r.y;
            }
        } else {
            if (er.x < r.x) {
                r.width += r.x - er.x;
                r.x = er.x;
            }
            if (er.x + er.width > r.x + r.width) {
                r.width = er.x + er.width - r.x;
            }
        }
    }

    private void paintGapsInContainer(Graphics2D g, LayoutComponent container,
                     Collection<GapInfo> gaps, GapInfo selectedGap, Rectangle selectDragRect) {
        if (gaps == null || gaps.isEmpty()) {
            return;
        }

        Shape originalClip = g.getClip();
        Shape containerClip = visualState.getComponentVisibilityClip(container);
        if (containerClip != null) {
            // avoid painting over components and out of container boundaries (clipped)
            g.clip(visualState.getClipForGapPainting(gaps, containerClip));
        }
        Color originalColor = g.getColor();
        boolean paintSelectedGap = false;
        for (GapInfo gapInfo : gaps) {
            if (gapInfo == selectedGap) {
                paintSelectedGap = true;
            } else {
                paintGap(g, gapInfo, false);
            }
        }
        if (paintSelectedGap) {
            if (selectDragRect == null) {
                paintGap(g, selectedGap, true);
                paintGapResizeHandles(g, selectedGap);
            } else {
                g.setClip(originalClip); // dragged gap can paint over components and everything
                paintDraggedGap(g, selectDragRect, selectedGap.dimension, LayoutInterval.canResize(selectedGap.gap));
            }
        }
        g.setClip(originalClip);
        g.setColor(originalColor);
    }

    // called from dragger
    void paintGapResizing(Graphics2D g, GapInfo resGap, Rectangle resRect) {
        if (paintedGaps == null || paintedGaps.isEmpty()) {
            return;
        }
        LayoutComponent parent = null;
        for (LayoutComponent comp : componentsOfPaintedGaps) {
            if (parent == null) {
                parent = comp.getParent();
            }
            if (comp.isLayoutContainer()) {
                Collection<GapInfo> gaps = visualState.getContainerGaps(comp);
                if (gaps.contains(resGap)) {
                    parent = comp;
                    break;
                }
            }
        }
        if (parent != null) { // we only paint gaps of the container where the gap is being resized
            Collection<GapInfo> gaps = paintedGaps.get(parent);
            paintGapsInContainer(g, parent, gaps, resGap, resRect);
        }
    }

    private static void paintGap(Graphics2D g, GapInfo gapInfo, boolean selected) {
        int x1 = gapInfo.paintRect.x;
        int y1 = gapInfo.paintRect.y;
        int w1 = gapInfo.paintRect.width;
        int h1 = gapInfo.paintRect.height;
        if (x1 < -Short.MAX_VALUE || x1 > 2*Short.MAX_VALUE
                || y1 < -Short.MAX_VALUE || y1 > 2*Short.MAX_VALUE
                || w1 < 0 || h1 < 0
                || w1 > 2*Short.MAX_VALUE || h1 > 2*Short.MAX_VALUE) {
            return; // avoid painting overload if current space is incorrectly calculated
        }
        int x2, y2, w2, h2;
        if (gapInfo.dimension == HORIZONTAL) {
            w2 = gapInfo.minSize;
            x2 = x1 + ((w1 - w2) / 2);
            if (h1 >= 4) {
                h1 -= 2;
                y1 += 1;
            }
            y2 = y1;
            h2 = h1;
        } else {
            h2 = gapInfo.minSize;
            y2 = y1 + ((h1 - h2) / 2);
            if (w1 >= 4) {
                w1 -= 2;
                x1 += 1;
            }
            x2 = x1;
            w2 = w1;
        }
        boolean differentMinSize = (w1 != w2 || h1 != h2);
        boolean resizing = LayoutInterval.canResize(gapInfo.gap);
        if (differentMinSize || (resizing && !PAINT_RES_GAP_MIN_SIZE)) {
            g.setColor(getResizingGapColor(selected));
            g.fillRect(x1+1, y1+1, w1-1, h1-1);
        }
        if (w2 > 0 && h2 > 0 && (!resizing || PAINT_RES_GAP_MIN_SIZE)) {
            g.setColor(differentMinSize ? getMinGapColor(selected) : getFixedGapColor(selected));
            g.fillRect(x2+1, y2+1, w2-1, h2-1);
        }
        if (resizing) {
            g.setColor(getSawColor(selected));
            if (gapInfo.dimension == HORIZONTAL && h1 > 4) { // paint a horizontal "saw"
                int count = h1 / 120 + 1;
                int step = h1 / count;
                for (int by=y1+step/2; count > 0; by+=step, count--) {
                    int d = h1 > 40 ? 4 : (h1 > 12 ? 3 : 2);
                    int d1 = -d;
                    int d2 = d;
                    for (int x=PAINT_RES_GAP_MIN_SIZE ? x2 : x1+w1/2;
                            x-4 >= x1;
                            x-=4, d1*=-1, d2*=-1) {
                        g.drawLine(x, by+d1, x-4, by+d2);
                    }
                    d1 = -d;
                    d2 = d;
                    for (int x=PAINT_RES_GAP_MIN_SIZE ? x2+w2-1 : x1+w1/2, xx=x1+w1;
                            x+4 < xx;
                            x+=4, d1*=-1, d2*=-1) {
                        g.drawLine(x, by+d1, x+4, by+d2);
                    }
                }
            } else if (w1 > 4) { // paint a vertical "saw"
                int count = w1 / 120 + 1;
                int step = w1 / count;
                for (int bx=x1+step/2; count > 0; bx+=step, count--) {
                    int d = w1 > 40 ? 4 : (w1 > 8 ? 3 : 2);
                    int d1 = -d;
                    int d2 = d;
                    for (int y=PAINT_RES_GAP_MIN_SIZE ? y2 : y1+h1/2;
                            y-4 >= y1;
                            y-=4, d1*=-1, d2*=-1) {
                        g.drawLine(bx+d1, y, bx+d2, y-4);
                    }
                    d1 = -d;
                    d2 = d;
                    for (int y=PAINT_RES_GAP_MIN_SIZE ? y2+h2-1 : y1+h1/2, yy=y1+h1;
                            y+4 < yy;
                            y+=4, d1*=-1, d2*=-1) {
                        g.drawLine(bx+d1, y, bx+d2, y+4);
                    }
                }
            }
        }
        g.setColor(getGapBorderColor(selected));
        g.drawRect(x1, y1, w1-1, h1-1);
    }

    private static void paintDraggedGap(Graphics2D g, Rectangle gapRect, int dimension, boolean resizing) {
        int x = gapRect.x;
        int y = gapRect.y;
        int w = gapRect.width;
        int h = gapRect.height;
        if (dimension == HORIZONTAL) {
            if (h >= 4) {
                h -= 2;
                y += 1;
            }
        } else {
            if (w >= 4) {
                w -= 2;
                x += 1;
            }
        }
        Color originalColor = g.getColor();
        Composite originalComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g.setColor(resizing ? getResizingGapColor(true) : getFixedGapColor(true));
        g.fillRect(x+1, y+1, w-1, h-1);
        g.setColor(getGapBorderColor(true));
        g.drawRect(x, y, w-1, h-1);
        g.setComposite(originalComposite);
        g.setColor(originalColor);
    }

    private static final Color[] HANDLE_COLORS = { new Color(255, 255, 243), //new Color(253, 253, 252),
                                                   new Color(242, 238, 230), //new Color(244, 247, 246),
                                                   new Color(221, 217, 209) }; // new Color(223, 230, 234)
    private static final int HANDLE_WIDTH = HANDLE_COLORS.length;

    private void paintGapResizeHandles(Graphics2D g, GapInfo gapInfo) {
        Rectangle r = gapInfo.paintRect;
        if (gapInfo.dimension == HORIZONTAL && r.width > 5) { // horizontal gap - paint vertical handles
            if (gapInfo.resizeLeading) {
                paintGapResizeHandle(g, r.x+1, r.y, r.height, VERTICAL, LEADING);
            }
            if (gapInfo.resizeTrailing) {
                paintGapResizeHandle(g, r.x+r.width-HANDLE_WIDTH-1, r.y, r.height, VERTICAL, TRAILING);
            }
        } else if (gapInfo.dimension == VERTICAL && r.height > 5) { // vertical gap - paint horizontal handles
            if (gapInfo.resizeLeading) {
                paintGapResizeHandle(g, r.x, r.y+1, r.width, HORIZONTAL, LEADING);
            }
            if (gapInfo.resizeTrailing) {
                paintGapResizeHandle(g, r.x, r.y+r.height-HANDLE_WIDTH-1, r.width, HORIZONTAL, TRAILING);
            }
        }
    }

    static int pointOnResizeHandler(GapInfo gapInfo, Point p) {
        Rectangle r = gapInfo.paintRect;
        if (r != null) {
            if (gapInfo.dimension == HORIZONTAL) { // horizontal gap - vertical handle
                int ll = r.x-1; // TODO subtract more if the width is really small
                int lt = r.x+HANDLE_WIDTH+2;
                int tl = r.x+r.width-HANDLE_WIDTH-3;
                int tt = r.x+r.width; // TODO add more if the width is really small
                if (gapInfo.resizeTrailing) {
                    if (tl < r.x + r.width/2) {
                        if (gapInfo.resizeLeading) {
                            tl = r.x + r.width/2;
                        } else if (tl < r.x) {
                            tl = r.x;
                        }
                    }
                    if (pointInArea(p, tl, tt, r.y-1, r.y+r.height+1)) {
                        return TRAILING;
                    }
                }
                if (gapInfo.resizeLeading) {
                    if (lt > r.x + r.width/2) {
                        if (gapInfo.resizeTrailing) {
                            lt = r.x + r.width/2;
                        } else if (lt > tt) {
                            lt = tt;
                        }
                    }
                    if (pointInArea(p, ll, lt, r.y-1, r.y+r.height+1)) {
                        return LEADING;
                    }
                }
            } else { // vertical gap - horizontal handle
                int ll = r.y-1; // TODO subtract more if the height is really small
                int lt = r.y+HANDLE_WIDTH+2;
                int tl = r.y+r.height-HANDLE_WIDTH-3;
                int tt = r.y+r.height; // TODO add more if the height is really small
                if (gapInfo.resizeTrailing) {
                    if (tl < r.y + r.height/2) {
                        if (gapInfo.resizeLeading) {
                            tl = r.y + r.height/2;
                        } else if (tl < ll) {
                            tl = ll;
                        }
                    }
                    if (pointInArea(p, r.x-1, r.x+r.width+1, tl, tt)) {
                        return TRAILING;
                    }
                }
                if (gapInfo.resizeLeading) {
                    if (lt > r.y + r.height/2) {
                        if (gapInfo.resizeTrailing) {
                            lt = r.y + r.height/2;
                        } else if (lt > tt) {
                            lt = tt;
                        }
                    }
                    if (pointInArea(p, r.x-1, r.x+r.width+1, ll, lt)) {
                        return LEADING;
                    }
                }
            }
        }
        return -1;
    }

    private static boolean pointInArea(Point p, int x1, int x2, int y1, int y2) {
        return p.x >= x1 && p.x < x2 && p.y >= y1 && p.y < y2;
    }

    private void paintGapResizeHandle(Graphics2D g, int x, int y, int length, int dimension, int alignment) {
        int correction;
        if (length < 6) {
            correction = 6 - length;
        } else if (length < 10) {
            correction = length - 6;
        } else {
            correction = -4;
        }
        length += correction;
        int dx, dy;
        if (dimension == HORIZONTAL) {
            dx = 0; dy = 1;
            x -= correction / 2;
        } else {
            dx = 1; dy = 0;
            y -= correction / 2;
        }

        for (int i=0; i < HANDLE_WIDTH; i++) {
            g.setColor(HANDLE_COLORS[alignment == LEADING ? i : HANDLE_WIDTH-i-1]);
            int px = x + i*dx;
            int py = y + i*dy;
            g.drawLine(px, py, px + length*dy - dy, py + length*dx - dx);
        }
    }

    private static Color fixedGapColor;
    private static Color resGapColor;
    private static Color minGapColor;
    private static Color gapBorderColor;
    private static Color sawColor;

    static {
        fixedGapColor = UIManager.getColor( "nb.formdesigner.gap.fixed.color" ); //NOI18N
        if( null == fixedGapColor )
            fixedGapColor = new Color(220, 220, 220); // 208, 208, 208

        resGapColor = UIManager.getColor( "nb.formdesigner.gap.resizing.color" ); //NOI18N
        if( null == resGapColor )
            resGapColor = new Color(224, 224, 224);

        minGapColor = UIManager.getColor( "nb.formdesigner.gap.min.color" ); //NOI18N
        if( null == minGapColor )
            minGapColor = new Color(212, 212, 212); // 204, 204, 204

        gapBorderColor = UIManager.getColor( "nb.formdesigner.gap.border.color" ); //NOI18N
        if( null == gapBorderColor )
            gapBorderColor = new Color(200, 200, 200); // 192, 192, 192

        sawColor = UIManager.getColor( "nb.formdesigner.saw.color" ); //NOI18N
        if( null == sawColor )
            sawColor = new Color(208, 208, 208);
    }

    private static Color getFixedGapColor(boolean selected) {
        return selected ? selectedColor(fixedGapColor) : fixedGapColor;
    }

    private static Color getResizingGapColor(boolean selected) {
        return selected ? selectedColor(resGapColor) : resGapColor;
    }

    private static Color getMinGapColor(boolean selected) {
        return selected ? selectedColor(minGapColor) : minGapColor;
    }

    private static Color getGapBorderColor(boolean selected) {
        return selected ? selectedColor(gapBorderColor) : gapBorderColor;
    }

    private static Color getSawColor(boolean selected) {
        return selected ? selectedColor(sawColor) : sawColor;
    }

    private static Color selectedColor(Color c) {
        return transColor(c, 210, 270, 190); // 322, 256, 176
    }

    private static Color transColor(Color c, int tr, int tg, int tb) {
        return new Color(transColor(c.getRed(), tr),
                         transColor(c.getGreen(), tg),
                         transColor(c.getBlue(), tb));
    }

    private static int transColor(int c, int t) {
        double change = t / 256.0;
//        double changing = 1.0;
//        int result = (int) Math.round(((c * changing * change) + (c * (1.0 - changing))) * brighter);
        int result = (int) Math.round(c * change);
        if (result > 255) {
            result = 255;
        } else if (result < 0) {
            result = 0;
        }
        return result;
    }
}
