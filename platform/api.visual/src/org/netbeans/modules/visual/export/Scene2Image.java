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
package org.netbeans.modules.visual.export;

import org.netbeans.api.visual.export.WidgetPolygonalCoordinates;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import org.netbeans.api.visual.export.SceneExporter.ImageType;
import org.netbeans.api.visual.export.SceneExporter.ZoomType;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Implementation class for SceneExporter. The SceneExporter calls either the export method
 * with an ImageEncoding parameter to set the file export type or it calls 
 * getSceneImageMapCoordinates.
 * @author krichard
 */
public class Scene2Image {

    private final File file;
    private final Scene scene;
    private Set<?> selectedObjects;
    private int imageHeight;
    private int imageWidth;
    private double scale;

    /**
     * Creates an instance of a Scene2Image object.
     * @param scene the Scene to be exported as an image.
     * @param file the file to which the image is to be saved. There is no extension 
     * check done on the file name. Meaning that a "png" image may be saved with a "txt" 
     * extension. 
     */
    public Scene2Image(Scene scene, File file) {
        this.scene = scene;
        this.file = file;
    }

    /**
     * Calculates the coordinates of individual polygons which fully encompass each
     * Widget. These coordinates can be used to create HTML image maps. Note that
     * a call to getSceneImageMapCoordinates will first export the Scene in the established
     * format.
     * @param margin The size of the clickable margin around each widget. This is
     * meant for defining the sensitivity of the links around the connection objects.
     * @return list of WidgetPolygonalCoordinates. The WidgetPolygonalCoordinates 
     * is simply a class to hold the Widget and Polygon together.
     */
    public ArrayList<WidgetPolygonalCoordinates> getSceneImageMapCoordinates(int margin) {

        ArrayList<WidgetPolygonalCoordinates> list = new ArrayList<WidgetPolygonalCoordinates>();
        List<Widget> layers = scene.getChildren();

        boolean oneLayer = false; //in case select widget option is true. This is a flag
        // to only pass through one layer.

        for (Widget layer : layers) {

            if (oneLayer) {
                break;
            }

            List<Widget> widgets = layer.getChildren();
            if (this.selectedObjects != null) { //this means that there is an ObjectScene
                //and that certain widgets are selected.
                ObjectScene oScene = (ObjectScene) scene;
                widgets.clear();
                for (Object o : selectedObjects) {
                    widgets.add(oScene.findWidget(o));
                }
                oneLayer = true;

            }

            for (Widget w : widgets) {
                Polygon polygon = new Polygon();

                //if it is a connection widget, then get the control points
                //and build the polygon to surround the connection.
                if (w instanceof ConnectionWidget) {
                    List<Point> controlPoints = ((ConnectionWidget) w).getControlPoints();
                    int numPoints = controlPoints.size();
                    if (numPoints == 0) {
                        continue;
                    }

                    Point start = controlPoints.get(0);
                    Point finish = controlPoints.get(controlPoints.size() - 1);

                    Point currentPoint = start;

                    int rise = 1, run = 1;
                    //start by creating a boundary along the bottom or right.
                    //note that the last point uses the slope calculated in
                    //the previous iteration.
                    for (int i = 0; i < numPoints; i++) {
                        int x;
                        int y;
                        Point nextPoint = null;

                        if (i + 1 < numPoints) {
                            nextPoint = controlPoints.get(i + 1);

                            rise = currentPoint.y - nextPoint.y;
                            run = currentPoint.x - nextPoint.x;
                        }

                        x = currentPoint.x;
                        y = currentPoint.y;

                        int xMargin = 0;
                        int yMargin = 0;

                        if (rise == 0) //connector segment is horizontal
                        {
                            yMargin = margin;
                        } else if (run == 0)//connector segment is vertical
                        {
                            xMargin = margin;
                        } else {
                            int sign = (rise * run) / Math.abs(rise * run); // either 1 or -1

                            xMargin = sign * margin;
                            yMargin = margin;
                        }

                        polygon.addPoint((int) (x * scale + xMargin), (int) (y * scale + yMargin));

                        currentPoint = nextPoint;
                    }

                    currentPoint = finish;

                    //come back around the top or left
                    for (int i = numPoints - 1; i >= 0; i--) {

                        int x;
                        int y;

                        Point prevPoint = null;

                        if (i - 1 >= 0) {
                            prevPoint = controlPoints.get(i - 1);
                            rise = currentPoint.y - prevPoint.y;
                            run = currentPoint.x - prevPoint.x;
                        }

                        x = currentPoint.x;
                        y = currentPoint.y;

                        int xMargin = 0;
                        int yMargin = 0;

                        if (rise == 0) //connector segment is horizontal
                        {
                            yMargin = -margin;
                        } else if (run == 0)//connector segment is vertical
                        {
                            xMargin = -margin;
                        } else {
                            int sign = (rise * run) / Math.abs(rise * run); // either 1 or -1

                            xMargin = sign * margin;
                            yMargin = -margin;
                        }

                        polygon.addPoint((int) (x * scale + xMargin), (int) (y * scale + yMargin));

                        currentPoint = prevPoint;
                    }

                } else {
                    Point p0 = w.getLocation();

                    Rectangle r = w.getPreferredBounds();
                    int width = (int) (r.width * scale);
                    int height = (int) (r.height * scale);
                    int x = (int) (p0.x * scale);
                    int y = (int) (p0.y * scale);

                    polygon.addPoint(x - 1, y - 1);
                    polygon.addPoint(x + width + 1, y - 1);
                    polygon.addPoint(x + width + 1, y + height + 1);
                    polygon.addPoint(x - 1, y + height + 1);
                }

                list.add(new WidgetPolygonalCoordinates(w, polygon));

            }

        }

        return list;
    }

    /**
     * Takes the Scene and writes an image file according to the constraints defined 
     * by the caller. This returns a BufferedImage of the Scene even if the file can 
     * not be written.
     * @param imageType The image type to be exported for the image map.
     * @param zoomType Defines the strategy
     * by which to set the exporting scale factor. Note that certain parameters are
     * nullified by the choice of ZoomType. For instance, if ZoomType.CUSTOM_SIZE is
     * not chosen, then the width and height parameters are not used.
     * @param visibleAreaOnly Eliminates all zoom features. If true, the exported
     * image will be a created from the visible area of the scene.
     * @param selectedOnly Create an image including only the objects selected on
     * the scene. Note that this feature requires that the scene is an instance of
     * an ObjectScene since it is the implementation that allows for object selection.
     * @param quality And integer value between 0-100. This is for JPG images only. 
     * Parameter is not used if an image type other than jpg is selected.
     * @param width Directly sets the horizontal dimension of the exported image.
     * This is only used when the zoomType is ZoomType.CUSTOM_SIZE
     * @param height Directly sets the vertical dimension of the exported image.
     * This is only used when the zoomType is ZoomType.CUSTOM_SIZE.
     * @param createImageMap If true, the necessary steps are taken to setup the
     * sequential call to getSceneImageMapCoordinates.
     * @return image The raw image that was written to the file.
     * @throws java.io.IOException If for some reason the file cannot be written, 
     * an IOExeption will be thrown.
     */
    public BufferedImage createImage(ImageType imageType, ZoomType zoomType,
            boolean visibleAreaOnly,
            boolean selectedOnly,
            int quality,
            int width,
            int height,
            boolean createImageMap) throws IOException {

        double _scale = scene.getZoomFactor();

        Rectangle sceneRec = scene.getPreferredBounds();
        Rectangle viewRect = scene.getView() != null ? scene.getView().getVisibleRect() : sceneRec;

        BufferedImage bufferedImage;
        Graphics2D g;
        ArrayList<Widget> hiddenWidgets = new ArrayList<Widget>();

        int _imageWidth = sceneRec.width;
        int _imageHeight = sceneRec.height;

        Set<?> _selectedObjects = null;

        if (selectedOnly) {
            //in order to use getSelectedObject the scene must be an ObjectScene
            if (scene instanceof ObjectScene) {

                ObjectScene gScene = (ObjectScene) scene;
                // hide unselected widget
                HashSet<Object> invisible = new HashSet<Object>();
                invisible.addAll(gScene.getObjects());
                _selectedObjects = gScene.getSelectedObjects();
                invisible.removeAll(_selectedObjects);

                for (Object o : invisible) {
                    Widget widget = gScene.findWidget(o);
                    if (widget != null && widget.isVisible()) {
                        widget.setVisible(false);
                        hiddenWidgets.add(widget);
                    }
                }
            }
        }

        if (visibleAreaOnly) {
            _imageWidth = viewRect.width;
            _imageHeight = viewRect.height;
        } else {
            switch (zoomType) {
                case CUSTOM_SIZE:
                    _imageWidth = width;
                    _imageHeight = height;
                    _scale = Math.min((double) width / (double) sceneRec.width,
                            (double) height / (double) sceneRec.height);
                    break;
                case FIT_IN_WINDOW:
                    _scale = Math.min((double) viewRect.width / (double) sceneRec.width,
                            (double) viewRect.height / (double) sceneRec.height);
                    _imageWidth = (int) ((double) sceneRec.width * _scale);
                    _imageHeight = (int) ((double) sceneRec.height * _scale);
                    break;
                case CURRENT_ZOOM_LEVEL:
                    _imageWidth = (int) (sceneRec.width * scene.getZoomFactor());
                    _imageHeight = (int) (sceneRec.height * scene.getZoomFactor());
                    break;
                case ACTUAL_SIZE:
                    _imageWidth = sceneRec.width;
                    _imageHeight = sceneRec.height;
                    _scale = 1.0;
                    break;
            }
        }

        //Note that the field variable are being set to method local variable. This
        //is for the call to getSceneImageMapCoordinates that will come since
        //createImageMap is true.
        if (createImageMap) {
            this.selectedObjects = _selectedObjects;
            this.imageHeight = _imageHeight;
            this.imageWidth = _imageWidth;
            this.scale = _scale;
        }

        bufferedImage = new BufferedImage(_imageWidth, _imageHeight, BufferedImage.TYPE_INT_RGB);
        g = bufferedImage.createGraphics();
        g.translate(0, 0);
        g.scale(_scale, _scale);
        scene.paint(g);

        // restore widget visibility
        for (Widget w : hiddenWidgets) {
            w.setVisible(true);
        }

        if (file != null) {
            FileImageOutputStream fo = new FileImageOutputStream(file);

            if (imageType == ImageType.JPG) {
                Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer = iter.next();

                ImageWriteParam iwp = writer.getDefaultWriteParam();
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (quality > 100) {
                    quality = 100;
                }
                if (quality < 0) {
                    quality = 0;
                }
                iwp.setCompressionQuality(quality / 100);
                writer.setOutput(fo);
                IIOImage image = new IIOImage(bufferedImage, null, null);
                writer.write(null, image, iwp);

                writer.dispose();
            } else {
                ImageIO.write(bufferedImage, "" + imageType, fo);
            }
            
            try {
                fo.close();
            } catch(IOException ioe) {
            }
        }

        return bufferedImage;


    }
}
