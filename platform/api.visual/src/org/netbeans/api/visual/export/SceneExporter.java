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
package org.netbeans.api.visual.export;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.visual.export.Scene2Image;

/**
 * This is a convenience class for exporting a Scene as an image file (jpeg, png, etc.).
 * There is an additional utility is this class to retrieve a list of WidgetPolygonalCoordinates.
 * This list holds widget-polygon tuples designed to be used to create HTML imagemaps. The 
 * following example code shows how this imagemap could be created.
 * <pre>{@code
ArrayList<WidgetPolygonalCoordinates> list = null ;
try {
    list = SceneExporter.createImageMap(scene, 
        imageFile, imageType, zoomType, 
        visibleAreaOnly, selectedOnly, quality, 
    horizontal, vertical, margin);
} catch (Exception e) {
    //Something bad happened
    return;
}
html.append("<img src=\"scene.jpg\" usemap=\"#WidgetMap\"/>\n");
String[] mapSections = new String[list.size()];
int count = 0;
for (WidgetPolygonalCoordinates wc: list) {
    Widget widget = wc.getWidget();
    String name = "no name";
    if (widget instanceof SimpleWidget) {
        name = ((SimpleWidget) widget).getUserObject().getLabel();
    } else if (widget instanceof SimpleConnectionWidget) {
        name = ((SimpleConnectionWidget) widget).getUserObject().getLabel();
    }
    String coordinateString = pointsToString(wc.getPolygon());
    mapSections[count++] = "<area shape=\"polygon\" coords=\"" + coordinateString + "\" href=\"#" + name + "\">";
    html.append("<li><a name=\"").append(name).append("\">");
    html.append(name).append("</a><br>\n");
    html.append(getRandomText(100)).append("</li>\n");
}
html.append("\n<map name=\"WidgetMap\">\n");
for (String s : mapSections) {
    html.append(s).append("\n");
}
html.append("</map>");
 * }</pre>
 * 
 * @author krichard
 */
public final class SceneExporter {

    /** 
     * Creates an instance of a SceneExporter object.
     * @return A new instance of a SceneExporter.
     */
    private SceneExporter() {
    }

    /**
     * Designed to create an HTML image map from an exported image. Each Layer in
     * the Scene provided to the constructor is iterated over and a java.awt.Polygon
     * is created surrounding each Widget contained in the layers. A WidgetPolygonalCoordinates
     * is created to hold the Widget and Polygon.
     * @param scene The Scene to be exported as an image. The Widgets will be collected
     * from this scene and polygons create that wrap each widget. The coordinates 
     * of each polygon are stored in an instance of WidgetPolygonalCoordinates.
     * @param file The file used to store the exported image. If null, the method 
     * immediately returns null.
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
     * @param quality And integer value between 0-100. This is for JPG images only. Parameter is not used if
     * an image type other than jpg is selected.
     * @param width Directly sets the horizontal dimension of the exported image.
     * This is only used when the zoomType is ZoomType.CUSTOM_SIZE
     * @param height Directly sets the vertical dimension of the exported image.
     * This is only used when the zoomType is ZoomType.CUSTOM_SIZE.
     * @param margin The size of the clickable margin around each connection. This is
     * meant for defining the sensitivity of the links around the connection objects.
     * Note that the area around the node objects is not affected by this margin.
     * @return ArrayList of WidgetPolygonalCoordinates objects which are simply used
     * to hold a Widget and Polygon. From the calling class, the widget and coordinates
     * can be used to create a link for the widget on an HTML image map.
     * @throws java.io.IOException If for some reason the file cannot be written, 
     * an IOExeption will be thrown.
     */
    public static ArrayList<WidgetPolygonalCoordinates> createImageMap(Scene scene, File file, ImageType imageType, ZoomType zoomType,
            boolean visibleAreaOnly,
            boolean selectedOnly,
            int quality,
            int width,
            int height,
            int margin) throws IOException {

        if (scene == null || file == null) {
            return null;
        }

        Scene2Image s2i = new Scene2Image(scene, file);
        s2i.createImage(imageType, zoomType, visibleAreaOnly, selectedOnly, quality, width, height, true);

        return s2i.getSceneImageMapCoordinates(margin);
    }

    /**
     * Takes the Scene and writes an image file according to the constraints 
     * defined by the caller. This returns a BufferedImage of the Scene even if 
     * the file can not be written.
     * @param scene The Scene to be exported as an image.
     * @param file The file used to store the exported image. If null, then it is 
     * assumed that the raw image is to be returned only and not written to a file.
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
     * @param quality And integer value between 0-100. This is for JPG images only. Parameter is not used if
     * an image type other than jpg is selected.
     * @param width Directly sets the horizontal dimension of the exported image.
     * This is only used when the zoomType is ZoomType.CUSTOM_SIZE
     * @param height Directly sets the vertical dimension of the exported image.
     * This is only used when the zoomType is ZoomType.CUSTOM_SIZE.
     * @return image The raw image that was written to the file.
     * @throws java.io.IOException If for some reason the file cannot be written, 
     * an IOExeption will be thrown.
     */
    public static BufferedImage createImage(Scene scene, File file, ImageType imageType, ZoomType zoomType,
            boolean visibleAreaOnly,
            boolean selectedOnly,
            int quality,
            int width,
            int height) throws IOException {

        if (scene == null) {
            return null;
        }
        if (!scene.isValidated()) {
            if (scene.getView() != null) {
                scene.validate();
            } else {
                BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D emptyGraphics = emptyImage.createGraphics();
                scene.validate(emptyGraphics);
            }
        }
        Scene2Image s2i = new Scene2Image(scene, file);
        BufferedImage image = s2i.createImage(imageType, zoomType, visibleAreaOnly, selectedOnly, quality, width, height, false);

        return image;
    }

    /**
     * This enumeration holds the possible image formats that the SceneExporter 
     * can export to.
     */
    public enum ImageType {

        /**
         * Use this in the SceneExporter to set the export file type to JPG.
         */
        JPG,
        /**
         * Use this in the SceneExporter to set the export file type to PNG.
         */
        PNG
    }

    /**
     * This enumeration holds the zooming capabilities that the SceneExporter 
     * can export according to.
     */
    public enum ZoomType {

        /**
         * Used to set the horizontal and vertical sizes directly.
         */
        CUSTOM_SIZE,
        /**
         * Used when the objects in the scene are to be exported into an
         * image with the identical dimensions of the scene's visual window.
         */
        FIT_IN_WINDOW,
        /**
         * Used when the objects in the scene are to be exported into an
         * image scaled the same as the scene. 
         */
        CURRENT_ZOOM_LEVEL,
        /**
         * Used to export an image of the scene according to its boundary dimensions.
         */
        ACTUAL_SIZE
    }
}
