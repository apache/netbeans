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

package org.netbeans.modules.java.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import javax.swing.UIManager;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LevelOfDetailsWidget;
import org.netbeans.api.visual.widget.Widget;
import static org.netbeans.modules.java.graph.Bundle.TIP_Primary;
import static org.netbeans.modules.java.graph.Bundle.TIP_Secondary;
import static org.netbeans.modules.java.graph.Bundle.TIP_VersionConflict;
import static org.netbeans.modules.java.graph.Bundle.TIP_VersionWarning;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_CONFLICT;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_NO_CONFLICT;
import static org.netbeans.modules.java.graph.DependencyGraphScene.VersionProvider.VERSION_POTENTIAL_CONFLICT;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
class EdgeWidget<I extends GraphNodeImplementation> extends ConnectionWidget {
    public static final int DISABLED = 0;
    public static final int GRAYED = 1;
    public static final int REGULAR = 2;
    public static final int HIGHLIGHTED = 3;
    public static final int HIGHLIGHTED_PRIMARY = 4;

    private static float[] hsbVals = new float[3];

    private GraphEdge<I> edge;
    private int state = REGULAR;
    private boolean isConflict;
    private int edgeConflictType;

    private LabelWidget conflictVersion;
    private Widget versionW;
    private Stroke origStroke;

    EdgeWidget(DependencyGraphScene<I> scene, GraphEdge<I> edge) {
        super(scene);
        this.edge = edge;
        origStroke = getStroke();
        setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        
        if(scene.supportsVersions()) {
            isConflict = scene.isConflict(edge.getTarget());
            edgeConflictType = getConflictType();

            updateVersionW(isConflict);
        }
    }
       
    private void updateVersionW (boolean isConflict) {
        DependencyGraphScene<I> scene = getDependencyGraphScene();
        assert scene.supportsVersions();
        
        GraphNode<I> targetNode = scene.getGraphNodeRepresentant(edge.getTarget());
        if (targetNode == null) {
            return;
        }
        
        int includedConflictType = targetNode.getConflictType(scene::isConflict, scene::compareVersions);

        if (versionW == null) {
            if (isConflict || includedConflictType != VERSION_NO_CONFLICT) {
                versionW = new LevelOfDetailsWidget(scene, 0.5, 0.7, Double.MAX_VALUE, Double.MAX_VALUE);
                conflictVersion = new LabelWidget(scene, scene.getVersion(edge.getTarget()));
                if (isConflict) {
                    Color c = getConflictColor(edgeConflictType);
                    if (c != null) {
                        conflictVersion.setForeground(c);
                    }
                }
                versionW.addChild(conflictVersion);
                addChild(versionW);
                setConstraint(versionW, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_RIGHT, 0.5f);
            }
        } else {
            if (!isConflict && includedConflictType == VERSION_NO_CONFLICT) {
                if (versionW.getParentWidget() == this) {
                    removeChild(versionW);
                } // else already removed??
            }
        }
    }

    public void setState (int state) {        
        this.state = state;
        updateAppearance(((DependencyGraphScene)getScene()).isAnimated());
    }

    /**
     * readable widgets are calculated  based on scene zoom factor when zoom factor changes, the readable scope should too
     */
    public void updateReadableZoom() {
        if (state == HIGHLIGHTED || state == HIGHLIGHTED_PRIMARY) {
            updateAppearance(false);
        }
    }

    private DependencyGraphScene<I> getDependencyGraphScene() {
        return (DependencyGraphScene<I>)getScene();
    }
    
    public void modelChanged () {
        DependencyGraphScene scene = getDependencyGraphScene();
        if(scene.supportsVersions()) {
            edgeConflictType = getConflictType();
            isConflict = scene.isConflict(edge.getTarget());
            // correction if some graph editing(fixing) was done
            if (isConflict && edgeConflictType == VERSION_NO_CONFLICT) {
                isConflict = false;
            }

            updateVersionW(isConflict);
        }
        updateAppearance(((DependencyGraphScene)getScene()).isAnimated());
    }

    @Messages({
        "TIP_VersionConflict=Conflict with {0} version required by {1}",
        "TIP_VersionWarning=Warning, overridden by {0} version required by {1}",
        "TIP_Primary=Primary dependency path",
        "TIP_Secondary=Secondary dependency path"
    })
    @SuppressWarnings("fallthrough")
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "SF_SWITCH_FALLTHROUGH")
    void updateAppearance (boolean animated) {
        Color inactiveC = UIManager.getColor("textInactiveText");
        if (inactiveC == null) {
            inactiveC = Color.LIGHT_GRAY;
        }
        Color activeC = UIManager.getColor("textText");
        if (activeC == null) {
                activeC = Color.BLACK;
            }
        Color conflictC = getConflictColor(edgeConflictType);
        if (conflictC == null) {
            conflictC = activeC;
        }

        Stroke s = getDependencyGraphScene().getStroke(edge);     
        Stroke stroke = s != null ? s : origStroke;
        
        Color c = activeC;
        switch (state) {
            case REGULAR:
                c = edge.isPrimary() ? middleColor(activeC, inactiveC) : isConflict ? conflictC : inactiveC;
                break;
            case GRAYED:
                c = isConflict ? deriveColor(conflictC, 0.7f) : inactiveC;
                break;
            case HIGHLIGHTED_PRIMARY:
                stroke = new BasicStroke(3);
                // without break!
            case HIGHLIGHTED:
                c = isConflict ? conflictC : activeC;
                break;
        }
        
        if (state != DISABLED) {
            StringBuilder sb = new StringBuilder("<html>");
            if (isConflict) {
                DependencyGraphScene grScene = getDependencyGraphScene();
                assert grScene.supportsVersions();
                GraphNodeImplementation included = grScene.getGraphNodeRepresentant(
                        edge.getTarget()).getImpl();
                if (included == null) {
                    return;
                }
                String version = grScene.getVersion(included);
                GraphNodeImplementation parent = included.getParent();
                String requester = parent != null ? parent.getName() : "???";
                String confText = edgeConflictType == VERSION_CONFLICT ? TIP_VersionConflict(version, requester) : TIP_VersionWarning(version, requester);
                conflictVersion.setToolTipText(confText);
                sb.append(confText);
                sb.append("<br>");
            }
            sb.append("<i>");
            if (edge.isPrimary()) {
                sb.append(TIP_Primary());
            } else {
                sb.append(TIP_Secondary());
            }
            sb.append("</i>");
            sb.append("</html>");
            setToolTipText(sb.toString());
        } else {
            setToolTipText(null);
        }

        if (conflictVersion != null) {
            conflictVersion.setForeground(c);
            Font origF = getScene().getDefaultFont();
            conflictVersion.setFont(state == HIGHLIGHTED || state == HIGHLIGHTED_PRIMARY
                    ? NodeWidget.getReadable(getScene(), origF) : origF);
        }

        setVisible(state != DISABLED && ((DependencyGraphScene)getScene()).isVisible(edge));

        setStroke(stroke);
        
        DependencyGraphScene grScene = (DependencyGraphScene)getScene();
        if (animated) {
            grScene.getSceneAnimator().animateForegroundColor(this, c);
        } else {
            setForeground(c);
        }
    }

    private int getConflictType () {
        GraphNode<I> included = ((DependencyGraphScene)getScene()).getGraphNodeRepresentant(edge.getTarget());
        if (included == null) {
            return VERSION_NO_CONFLICT;
        }
        DependencyGraphScene<I> scene = getDependencyGraphScene();
        int ret = scene.compareVersions(edge.getTarget(), included.getImpl());
                                return ret > 0 ? 
                                        VERSION_CONFLICT : 
                                        ret < 0 ?
                                            VERSION_POTENTIAL_CONFLICT : 
                                            VERSION_NO_CONFLICT;        
    }

    private static Color getConflictColor (int conflictType) {
        return conflictType == VERSION_CONFLICT ? NodeWidget.CONFLICT
                : conflictType == VERSION_POTENTIAL_CONFLICT
                ? NodeWidget.WARNING : null;
    }

    /** Derives color from specified with saturation multiplied by given ratio.
     */
    static Color deriveColor (Color c, float saturationR) {
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbVals);
        hsbVals[1] = Math.min(1.0f, hsbVals[1] * saturationR);
        return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);
    }

    private static Color middleColor (Color c1, Color c2) {
        return new Color(
                (c1.getRed() + c2.getRed()) / 2,
                (c1.getGreen() + c2.getGreen()) / 2,
                (c1.getBlue() + c2.getBlue()) / 2);
    }

}
