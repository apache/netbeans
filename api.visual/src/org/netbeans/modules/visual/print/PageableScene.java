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
