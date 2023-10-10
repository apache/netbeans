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
package org.netbeans.api.visual.print;

import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.print.PageableScene;

/**
 * A class used to print a Scene to a printer. Multiple convenience methods are
 * provided to initiate the print with various constraints.
 * @author krichard
 */
public final class ScenePrinter {

    /**
     * Creates a new instance of a ScenePrinter object.
     */
    private ScenePrinter() {
    }

    /**
     * Print the scene exactly as it is. There is to be no scaling or special
     * formatting.
     * @param scene The Scene to be printed.
     */
    public static void print(Scene scene) {
        print(scene, null, ScaleStrategy.NO_SCALING);
    }

    /**
     * Print the Scene with provided PageFormat.
     * @param scene The Scene to be printed.
     * @param format format used for printing Scene.
     */
    public static void print(Scene scene, PageFormat format) {
        print(scene, format, ScaleStrategy.NO_SCALING);
    }

    /**
     * Print the Scene with the default PageFormat and the provided scale type.
     * @param scene The Scene to be printed.
     * @param scaleStrategy Value representing the scaling strategy to use for the printing.
     */
    public static void print(Scene scene, ScaleStrategy scaleStrategy) {
        print(scene, null, scaleStrategy);
    }

    /**
     * Print the Scene with specific scaling percentages for the horizontal and
     * vertical direction.
     * @param scene The Scene to be printed.
     * @param scaleX scaling percentage in the horizontal dimension.
     * @param scaleY scaling percentage in the vertical dimension.
     */
    public static void print(Scene scene, double scaleX, double scaleY) {
        print(scene, null, ScaleStrategy.SCALE_PERCENT, scaleX, scaleY, 
                false, false, null, null);
    }

    /**
     * Print the Scene with the provided format and scale type.
     * @param scene The Scene to be printed.
     * @param format Format used for printing Scene.
     * @param scaleStrategy Value representing the scaling strategy to use for the printing.
     */
    public static void print(Scene scene, PageFormat format, ScaleStrategy scaleStrategy) {
        print(scene, format, scaleStrategy, 1.0, 1.0, false, false, null, null);
    }

    /**
     * Print a specific section of the Scene according to the provided Format.
     * @param scene The Scene to be printed.
     * @param format Format used for printing Scene.
     * @param region The rectangle representing the are of the Scene to be printed.
     */
    public static void print(Scene scene, PageFormat format, Rectangle region) {
        print(scene, format, ScaleStrategy.NO_SCALING, 1.0, 1.0, false, false, region, null);
    }
    
    
    
    /**
     * This is the master print call. All other print calls in this class pass
     * through to here after setting the appropriate parameters. Note that the
     * scale type parameter take precedent over the horizontal and vertical scaling
     * percentages. Therefore, if the scale type was "SCALE_TO_FIT_Y", then the
     * values of horizontal and vertical scaling percentages would not be used.
     * @param scene The Scene to be printed.
     * @param format format used for printing Scene. If null then a new default 
     * PageFormat is created.
     * @param scaleStrategy value representing the how to scale the printing.
     * @param scaleX Directly set the horizontal scale percentage. This parameter
     * is only used when the scale strategy is ScaleStrategy.SCALE_PERCENT. Otherwise
     * it is ignored.
     * @param scaleY Directly set the vertical scale percentage. This parameter
     * is only used when the scale strategy is ScaleStrategy.SCALE_PERCENT. Otherwise
     * it is ignored.
     * @param selectedOnly Print only the objects from the Scene that have been 
     * selected. Note that in this case the Scene must be an instnace of an ObjectScene
     * since this is required to determine the selected objects.
     * @param visibleOnly Print only the object in the visible window.
     * @param region The rectangle representing the are of the Scene to be printed.
     * @param hiddenLayers Layer that are not to be printed. Might be used to
     * hide the background while printing.
     */
    public static void print(Scene scene, PageFormat format, ScaleStrategy scaleStrategy,
            double scaleX, double scaleY,
            boolean selectedOnly, boolean visibleOnly,
            Rectangle region, List<LayerWidget> hiddenLayers) {

        //quick return if the scene is null
        if (scene == null) return ;
        if (scaleStrategy == null) scaleStrategy = ScaleStrategy.NO_SCALING ;
        
        PageableScene ps = new PageableScene(scene, format,
                scaleStrategy, scaleX, scaleY,
                selectedOnly, visibleOnly, region);

        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPageable(ps);

        if (printJob.printDialog()) {
            
            try {
                //this can not go here because it hides all the widgets everytime.
                ArrayList<Widget> hiddenWidgets = new ArrayList<Widget>();
                
                if (hiddenLayers != null && hiddenLayers.size() > 0) {
                    for (Widget widget: hiddenLayers) {
                        widget.setVisible(false);
                        hiddenWidgets.add(widget);
                    }
                }
                
                //if selectedOnly is true then we need to hide all the non-selected
                //widgets. Note that if the widgets were already hidden due to hiding
                //a layer, they are not hidden again.
                if (selectedOnly) {
                    //in order to use getSelectedObject the scene must be an ObjectScene
                    if (scene instanceof ObjectScene) {
                        ObjectScene gScene = (ObjectScene) scene;
                        // hide unselected widget
                        HashSet<Object> invisible = new HashSet<>();
                        invisible.addAll(gScene.getObjects());
                        Set<?> selectedObjects = gScene.getSelectedObjects();
                        invisible.removeAll(selectedObjects);

                        for (Object o : invisible) {
                            Widget widget = gScene.findWidget(o);
                            if (widget != null && widget.isVisible()) {
                                widget.setVisible(false);
                                hiddenWidgets.add(widget);
                            }
                        }
                    }
                }

                //Similar to the selectedOnly, if visibleOnly is true we have to
                //hide the widgets not in the visible window. The reason for this
                //is because setting the size alone does not hide the widgets. Any
                //page that is printed will include all its contained widgets if
                //they are not hidden. Note also that this must work with any
                //Scene, not just the ObjectScene. Therefore, we have to gather
                //all the widgets.
                if (visibleOnly || region != null) {
                    
                    //if the region is null, then the user is not trying to print a
                    //region. The other way into this condition is if we need to
                    //to print the visibleArea. So we set the region to be the 
                    //visible area.
                    if (region == null)
                        region = scene.getView().getVisibleRect();
                    
                    List<Widget> allWidgets = new ArrayList<Widget>() ; 
                    for (Widget widget: scene.getChildren()) {
                            allWidgets.addAll(getAllNodeWidgets(widget, null));
                    }
                    
                    for (Widget widget : allWidgets) {
                        Rectangle widgetInSceneCoordinates = widget.convertLocalToScene(widget.getBounds()) ;
                        boolean included = region.contains(widgetInSceneCoordinates);
                        if (!included && widget.isVisible()) {
                            widget.setVisible(false);
                            hiddenWidgets.add(widget);
                        }
                    }
                }

                printJob.print();

                // restore widget visibility
                for (Widget w : hiddenWidgets) {
                    w.setVisible(true);
                }
                
                scene.validate();
                
            } catch (PrinterException pe) {
                System.out.println("Error printing: " + pe);
            }
        }

    }

    //this is necessary since we can print any generic scene. Meaning that we can't
    //simply do a getNodes.
    private static List<Widget> getAllNodeWidgets(Widget widget, List<Widget> widgetList) {
        if (widgetList == null) {
            widgetList = new ArrayList<Widget>();
        }

        if (widget instanceof LayerWidget && widget.getChildren().size() > 0) //widget has children
        {
            widgetList.addAll(widget.getChildren());

            for (Widget child : widget.getChildren()) {
                getAllNodeWidgets(child, widgetList);
            }
        }

        return widgetList;
    }

    /**
     * Scaling strategies to be used for printing a scene.
     */
    public enum ScaleStrategy {

        /**
         * Scale the printed scene to exactly the same scale level as the supplied 
         * scene.
         */
        SCALE_CURRENT_ZOOM,
        /**
         * Scale the printed scene by a supplied percent. This will scale the X and Y
         * dimensions equally.
         */
        SCALE_PERCENT,
        /**
         * Scale the printed scene to fit on a single page
         */
        SCALE_TO_FIT,
        /**
         * Determine the scale percentage necessay to fit on the horizontal page. The
         * vertical is scaled equally but no garuntees are made on the page fit.
         */
        SCALE_TO_FIT_X,
        /**
         * Determine the scale percentage necessay to fit on the vertical page. The
         * horizontal is scaled equally but no garuntees are made on the page fit. 
         */
        SCALE_TO_FIT_Y,
        /**
         * Print the Scene exactly as it would be if not scaled at all. Note that this
         * is different from SCALE_CURRENT_ZOOM where the page is printed as shown 
         * visually, including the zoom.
         */
        NO_SCALING
    }
}
