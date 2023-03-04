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
package org.netbeans.modules.visual.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import org.netbeans.api.visual.print.ScenePrinter.ScaleStrategy;
import org.netbeans.api.visual.widget.Scene;

/**
 * This is an implementation class for the public ScenePrinter class. The ScenePrinter
 * instantiates a PageableScene which is then sent to the PrinterJob.
 * 
 * @author krichard
 */
public class PageableScene implements Pageable, Printable {

    private double mScaleX = 1.0;
    private double mScaleY = 1.0;
    private final Scene scene;
    private int mNumPagesX;
    private int mNumPagesY;
    private int mNumPages;
    private PageFormat mFormat;
    private final boolean selectedOnly;
    private final boolean visibleOnly;
    private Rectangle region = null;

    /**
     * Creates an instance of a PageableScene used with a PrinterJob to print the 
     * Scene.
     * @param scene the Scene to be printed
     * @param format the format to which the printed scene will adhere (legal, 
     * letter, custom). If the format parameter is null, a default letter (8.5x11)
     * is created and used.
     * @param scaleType type of scaling to be done before printing. The options are<br/>
     * <ul>
     *   <li>NO_SCALING</li>
     *   <li>SCALE_CURRENT_ZOOM</li>
     *   <li>SCALE_PERCENT</li>
     *   <li>SCALE_TO_FIT</li>
     *   <li>SCALE_TO_FIT_X</li>
     *   <li>SCALE_TO_FIT_Y</li>
     * </ul>
     * @param scaleX Directly set the horizontal scale percentage. This parameter
     * is only used when the scale strategy is ScaleStrategy.SCALE_PERCENT. Otherwise
     * it is ignored.
     * @param scaleY Directly set the vertical scale percentage. This parameter
     * is only used when the scale strategy is ScaleStrategy.SCALE_PERCENT. Otherwise
     * it is ignored.
     */
    public PageableScene(Scene scene, PageFormat format, ScaleStrategy scaleType,
            double scaleX, double scaleY,
            boolean selectedOnly, boolean visibleOnly, Rectangle region) {

        this.scene = scene;
        this.selectedOnly = selectedOnly;
        this.visibleOnly = visibleOnly;
        this.region = region;

        double zoomFactor = scene.getZoomFactor();

        if (format == null) {
            format = new PageFormat();
        }

        setPageFormat(format);

        if (visibleOnly || region != null) {
            if (region == null) {
                region = scene.getView().getVisibleRect();
            }
            setSize(region.width, region.height);

        } else if (scaleType.equals(ScaleStrategy.SCALE_CURRENT_ZOOM)) {
            //note that SCALE_CURRENT_ZOOM means to use the zoomfactor provided
            //by the scene. Since setScaledSize calculates the new size by getting
            //the scene dimensions and multiplying it by the scale, then we send
            //1.0 since the scene size has already been adjusted by the zoomFactor.
            setScaledSize(1.0, 1.0);
        } else if (scaleType.equals(ScaleStrategy.NO_SCALING)) {
            //restore the correct number of pages
            //Is it true that the scene size does not decrease if the zoomfactor
            //is less than one??
            double z = zoomFactor > 1.0 ? 1.0 / zoomFactor : 1.0;
            setScaledSize(z, z);

            //reset the printing to the correct size
            mScaleX = 1.0;
            mScaleY = 1.0;
        } else if (scaleType.equals(ScaleStrategy.SCALE_TO_FIT_X)) {
            scaleToFitX();
        } else if (scaleType.equals(ScaleStrategy.SCALE_TO_FIT_Y)) {
            scaleToFitY();
        } else if (scaleType.equals(ScaleStrategy.SCALE_TO_FIT)) {
            scaleToFit(true);
        } else if (scaleType.equals(ScaleStrategy.SCALE_PERCENT)) {
            setScaledSize(scaleX, scaleY);
        }

    }

    /**
     * Paint to the printer.
     * @param graphics Graphics object for the PrinterJob.
     * @param pageFormat PageFormat to adhere to while printing.
     * @param pageIndex The current page number.
     * @return NO_SUCH_PAGE if the current page number is not valid and PAGE_EXISTS
     * otherwise.
     * @throws java.awt.print.PrinterException
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        if (mNumPagesX == 0) {
            return NO_SUCH_PAGE;
        }

        double originX = (pageIndex % mNumPagesX) * mFormat.getImageableWidth();
        double originY = (pageIndex / mNumPagesX) * mFormat.getImageableHeight();

        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        g2.translate(-originX, -originY);
        g2.scale(mScaleX, mScaleY);

        scene.paint(g2);

        return PAGE_EXISTS;

    }

    /**
     * Set the size of the document.
     * @param width the horizontal dimension.
     * @param height the vertical dimension.
     */
    protected void setSize(float width, float height) {


        if (mFormat.getImageableWidth() * mFormat.getImageableHeight() == 0) {
            return;
        }

        //making sure to round up. Meaning that this forces partial pages to be
        //included.
        mNumPagesX = (int) ((width + mFormat.getImageableWidth() - 1) / mFormat.getImageableWidth());
        mNumPagesY = (int) ((height + mFormat.getImageableHeight() - 1) / mFormat.getImageableHeight());

        mNumPages = mNumPagesX * mNumPagesY;
    }

    /**
     * Adjusts the scaling factors in both the horizontal and vertical directions
     * to garuntee that the Scene prints onto a single page.
     * @param useSymmetricScaling if true, the horizontal and vertical scaling
     * factors will be the same whereby preserving the current aspect ration. The
     * smallest of the two (horizontal and vertical) scaling factors is used for 
     * both.
     */
    private void scaleToFit(boolean useSymmetricScaling) {
        PageFormat format = getPageFormat();

        Rectangle componentBounds = scene.getView().getBounds();

        if (componentBounds.width * componentBounds.height == 0) {
            return;
        }
        double scaleX = format.getImageableWidth() / componentBounds.width;
        double scaleY = format.getImageableHeight() / componentBounds.height;

        if (scaleX < 1 || scaleY < 1) {

            if (useSymmetricScaling) {
                if (scaleX < scaleY) {
                    scaleY = scaleX;
                } else {
                    scaleX = scaleY;
                }
            }

            setSize((float) (componentBounds.width * scaleX), (float) (componentBounds.height * scaleY));
            setScaledSize(scaleX, scaleY);

        }
    }

    /**
     * Set the print size to fit a page in the horizontal direction. The
     * vertical is scaled equally but no garuntees are made on the page fit.
     */
    private void scaleToFitX() {
        PageFormat format = getPageFormat();
        Rectangle componentBounds = scene.getBounds();

        if (componentBounds.width == 0) {
            return;
        }

        double scaleX = format.getImageableWidth() / componentBounds.width;
        double scaleY = scaleX;
        if (scaleX < 1) {
            setSize((float) format.getImageableWidth(),
                    (float) (componentBounds.height * scaleY));
            setScaledSize(scaleX, scaleY);
        }
    }

    /**
     * Set the print size to fit a page in the verticle direction.
     * The horizontal is scaled equally but no garuntees are made on the page fit.
     */
    private void scaleToFitY() {
        PageFormat format = getPageFormat();
        Rectangle componentBounds = scene.getBounds();

        if (componentBounds.height == 0) {
            return;
        }

        double scaleY = format.getImageableHeight() / componentBounds.height;
        double scaleX = scaleY;
        if (scaleY < 1) {
            setSize((float) (componentBounds.width * scaleX), (float) format.getImageableHeight());
            setScaledSize(scaleX, scaleY);
        }
    }

    /**
     * Set the absolute scaling values for both the horizontal and vertical dimensions.
     * @param scaleX scaling percentage in the horizontal dimension.
     * @param scaleY scaling percentage in the vertical dimension.
     */
    private void setScaledSize(double scaleX, double scaleY) {
        mScaleX = scaleX;
        mScaleY = scaleY;

        Rectangle componentBounds = scene.getView().getBounds();
        setSize((float) (componentBounds.width * scaleX), (float) (componentBounds.height * scaleY));
    }

    /**
     * Returns the number of pages that the printing will use to print at the set
     * zoom level.
     * @return the number of pages that the printing will use to print at the set
     * zoom level.
     */
    public int getNumberOfPages() {
        return mNumPages;
    }

    /**
     * Returns the PageFormat that will be adhered to during printing. 
     * @return the PageFormat that will be adhered to during printing.
     */
    private PageFormat getPageFormat() {
        return mFormat;
    }

    /**
     * Set the PageFormat that will be adhered to during printing.
     * @param pageFormat the PageFormat that will be adhered to during printing.
     */
    private void setPageFormat(PageFormat pageFormat) {
        mFormat = pageFormat;
    }

    /**
     * Return the PageFormat for a specific page.
     * @param pageIndex the page number for which the PageFormat is sought.
     * @return the PageFormat that will be adhered to during printing the given page.
     * @throws java.lang.IndexOutOfBoundsException
     */
    public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
        if (pageIndex >= mNumPages) {
            throw new IndexOutOfBoundsException();
        }
        return getPageFormat();
    }

    /**
     * Return this class, which is the Printable object.
     * @param pageIndex the page number of the page to be printed.
     * @return this class, which is the Printable object.
     * @throws java.lang.IndexOutOfBoundsException
     */
    public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
        if (pageIndex >= mNumPages) {
            throw new IndexOutOfBoundsException();
        }

        return this;
    }
}
