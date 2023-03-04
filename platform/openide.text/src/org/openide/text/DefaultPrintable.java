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
package org.openide.text;


import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.text.*;
import org.openide.*;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/** The class creates from an instance of AttributedCharacterIterator
 * a java.awt.print.Pageable object.
 *
 * @author Ales Novak
 */
final class DefaultPrintable extends Object implements Printable {
    /** Number of args. */
    private static final int ARG_SIZE = 3;
    
    /** Font for CancellationDialog */
    private static Font fontInstance;

    /**
     * for each item is created new LineBreakMeasurer
     */
    private AttributedCharacterIterator[] styledTexts;

    /** expected page */

    //    private int pageno;

    /** Start page */
    private int startPage = -1;

    /** created text layouts */
    private List<TextLayout> textLayouts;

    /** page indices to textLayouts list */
    private int[] pageIndices;

    /** pageIndices size */
    private int pageIndicesSize;

    /** iterator over textLayouts */
    private int currentLayout;

    /** curent styledText entry */
    private int currentStyledText;

    /** current LineBreakMeasurer */
    private LineBreakMeasurer lineBreakMeasurer;

    /** maximal page */
    private int maxPage;

    /** text layouts that starts new line (those that were not created thanks to wrapping) */
    private List<TextLayout> startLayouts;

    /** page to line indexes (page 5 starts with line 112) */
    private int[] lineIndices; // pageIndicesSize

    /** Arguments for page. */
    private Object[] pageArgs;

    /** Header of each page. */
    private MessageFormat header;

    /** Should be header printed? */
    private boolean printHeader;

    /** Bottom of each page. */
    private MessageFormat footer;

    /** Should be footer printed? */
    private boolean printFooter;

    /** CancellationPanel */
    private CancellationPanel cancellationPanel;

    /** Dialog */
    private Dialog cancellationDialog;

    /**
    * @param attrs an AttributedCharacterIterator
    * @param filename
    */
    private DefaultPrintable(AttributedCharacterIterator[] iter, String filename) {
        if ((iter == null) || (iter.length == 0)) {
            throw new IllegalArgumentException();
        }
        
        // bugfix for sun.awt.Bidi line 250
        replaceEmptyIterators(iter);

        styledTexts = iter;

        //        pageno = 0;
        textLayouts = new ArrayList<TextLayout>(100); // 100 lines
        pageIndices = new int[50];
        pageIndicesSize = 0;
        currentLayout = 0;
        currentStyledText = 0;
        lineBreakMeasurer = null;
        maxPage = Integer.MAX_VALUE;

        startLayouts = new ArrayList<TextLayout>(10); // 10 lines
        lineIndices = new int[pageIndices.length];

        pageArgs = new Object[ARG_SIZE];
        pageArgs[2] = filename;
        pageArgs[1] = new Date(System.currentTimeMillis());

        header = new MessageFormat(getHeaderFormat());
        printHeader = !getHeaderFormat().equals(""); // NOI18N
        footer = new MessageFormat(getFooterFormat());
        printFooter = !getFooterFormat().equals(""); // NOI18N
    }

    /**
    * @param doc printed document
    */
    public DefaultPrintable(Document doc) {
        this(getIterators(doc), getFilename(doc));
    }

    /**
     * Prints a page.
     *
     * @param g a Graphics
     * @param pf a PageFormat
     * @param pageNo Which page?
     */
    public int print(Graphics g, PageFormat pf, int pageNo)
    throws PrinterException {
        boolean processDummy = false;

        if (startPage == -1) {
            processDummy = true;
            startPage = pageNo;
        }

        if (processDummy) {
            for (int i = 0; i < startPage; i++) {
                // XXX #21245 Processes dummy pages (first pages to not print).
                // PENDING shuold be made better+faster way to skip
                // processing of such pages (then this - hot fix).
                printImpl(g, pf, i, false);
            }
        }

        return printImpl(g, pf, pageNo, true);
    }

    private int printImpl(Graphics g, PageFormat pf, int pageNo, boolean print)
    throws PrinterException {
        if (pageNo > maxPage) {
            closeDialog();

            return Printable.NO_SUCH_PAGE;
        } else if (pageNo < 0) {
            closeDialog();
            throw new IllegalArgumentException("Illegal page number=" + pageNo); // NOI18N
        }

        // stop if cancelled
        if ((g instanceof PrinterGraphics) && isCancelled(((PrinterGraphics) g).getPrinterJob())) {
            closeDialog();
            throw new PrinterAbortException();
        }

        if ((cancellationPanel == null) && (g instanceof PrinterGraphics)) {
            // [TODO] - commented out since the awt API does not allow proper handling
            // of the dialog - e.g when am I to close it?
            PrinterJob pJob = ((PrinterGraphics) g).getPrinterJob();
            createCancellationPanel(pJob);
        }

        if (cancellationPanel != null) {
            int pageNumber = (print ? pageNo : startPage);
            cancellationPanel.setPageno(pageNumber);
            packDialog();
        }

        // line numbers init
        int startLine = 0;
        int correction = 3; // magic - take pencil, paper, and start measuring.

        //        if (lineNumbers()) {  // not used

        /*
        lineNo = Integer.toString(startLine = page2Line(pageNo));
        // correction may not be ok
        if (startLine < 100) {
          lineNo = lineNo + "   ";
        } else {
          lineNo = lineNo + " ";
        }
        correction = g.getFontMetrics().stringWidth(lineNo);
        */

        //        }
        g.setColor(Color.black);

        final Graphics2D graphics = (Graphics2D) g;
        final Point2D.Float pen = new Point2D.Float(getImageableXPatch(pf), getImageableYPatch(pf));

        /*    System.out.println("PEN IS: " + pen);
            pen = new Point2D.Float((float) pf.getImageableWidth(),
                                    (float) pf.getImageableHeight()
                                   );
            System.out.println("END IS: " + pen);
            Paper paper = pf.getPaper();
            System.out.println("DEF IS: " + paper.getHeight() + ", " + paper.getWidth());
        */
        // header & footer init
        pageArgs[0] = new Integer(pageNo + 1); // pages numbered from 1

        float pageBreakCorrection = 0.0F;
        TextLayout headerString = null;
        TextLayout footerString = null;

        if (printHeader) {
            headerString = new TextLayout(header.format(pageArgs), getHeaderFont(), graphics.getFontRenderContext());

            pageBreakCorrection += (headerString.getAscent() +
            ((headerString.getDescent() + headerString.getLeading()) * 2));
        }

        if (printFooter) {
            footerString = new TextLayout(footer.format(pageArgs), getFooterFont(), graphics.getFontRenderContext());

            pageBreakCorrection += ((footerString.getAscent() * 2) + footerString.getDescent() +
            footerString.getLeading());
        }

        // for now suppose that getImageableWidthPatch(pf) is always
        // the same during the same print job
        final float wrappingWidth = (wrap() ? ((float) pf.getImageableWidth() - correction) : Float.MAX_VALUE);
        final float pageBreak = (((float) pf.getImageableHeight()) + ((float) pf.getImageableY())) -
            pageBreakCorrection;
        final FontRenderContext frCtx = graphics.getFontRenderContext();

        boolean pageExists = false;

        // page rendering
        for (
            TextLayout layout = layoutForPage(pageNo, wrappingWidth, frCtx); (pen.y < pageBreak);
                layout = nextLayout(wrappingWidth, frCtx)
        ) {
            if (layout == null) {
                maxPage = pageNo;

                break;
            }

            if (!pageExists) {
                // draw header
                if (printHeader && (headerString != null)) {
                    pen.y += headerString.getAscent();

                    float center = computeStart(
                            headerString.getBounds(), (float) pf.getImageableWidth(), getHeaderAlignment()
                        );
                    float dx = (headerString.isLeftToRight() ? center : (wrappingWidth - headerString.getAdvance() -
                        center));

                    if (print) {
                        headerString.draw(graphics, pen.x + dx, pen.y);
                    }

                    pen.y += ((headerString.getDescent() + headerString.getLeading()) * 2);
                }

                pageExists = true;
            }

            pen.y += (layout.getAscent() * getLineAscentCorrection());

            // line number handling
            //            if (lineNumbers() && isNewline(layout, startLine)) {

            /*
            lineNo = Integer.toString(++startLine); // + 1 -> lines starts from 1

            if (startLine < 100) {
              lineNo = lineNo + "   ";
            } else {
              lineNo = lineNo + " ";
            }

            // graphics.drawString(lineNo, (int) pen.x, (int) pen.y);
            TextLayout tl = new TextLayout(lineNo, lineNumbersFont(), graphics.getFontRenderContext());
            tl.draw(graphics, pen.x, pen.y);
            */

            //            }
            float dx = (layout.isLeftToRight() ? 0 : (wrappingWidth - layout.getAdvance()));

            if (print) {
                layout.draw(graphics, correction + pen.x + dx, pen.y);
            }

            pen.y += ((layout.getDescent() + layout.getLeading()) * getLineAscentCorrection());
        }

        // draw footer
        if (printFooter && pageExists && (footerString != null)) {
            pen.y = pageBreak;
            pen.y += (footerString.getAscent() * 2);

            float center = computeStart(footerString.getBounds(), (float) pf.getImageableWidth(), getFooterAlignment());
            float dx = (footerString.isLeftToRight() ? (0 + center) : (wrappingWidth - footerString.getAdvance() -
                center));

            if (print) {
                footerString.draw(graphics, pen.x + dx, pen.y);
            }
        }

        // stop if cancelled
        if ((g instanceof PrinterGraphics) && isCancelled(((PrinterGraphics) g).getPrinterJob())) {
            closeDialog();
            throw new PrinterAbortException();
        }

        // at least one layout draw?
        if (!pageExists) {
            closeDialog();

            return Printable.NO_SUCH_PAGE;
        } else {
            return Printable.PAGE_EXISTS;
        }
    }

    /* for following two methods:
    * windows page setup dialog behaves incorrectly for LANDSCAPE format
    * the x coordinate is influenced by RIGHT margin instead of LEFT margin
    * the y coordinate ... BOTTOM instead of TOP
    * #3732
    */

    /** Patch for a bug in the PageFormat class
    * @param pf PageFormat
    * @return imageable x coordination for this page format
    */
    private float getImageableXPatch(PageFormat pf) {
        if (pf.getOrientation() == PageFormat.LANDSCAPE) {
            double ret = pf.getPaper().getHeight() - (pf.getImageableX() + pf.getImageableWidth());

            return (float) Math.round(ret);
        } else {
            return (float) pf.getImageableX();
        }
    }

    /** Patch for a bug in the PageFormat class
    * @param pf PageFormat
    * @return imageable y coordination for this page format
    */
    private float getImageableYPatch(PageFormat pf) {
        if (pf.getOrientation() == PageFormat.LANDSCAPE) {
            double ret = pf.getPaper().getWidth() - (pf.getImageableY() + pf.getImageableHeight());

            return (float) Math.round(ret);
        } else {
            return (float) pf.getImageableY();
        }
    }

    /** Translates given page number to line number.
    * @param pageNo
    * @return number of first line on the page
    * /
    private int page2Line(int pageNo) {
        if (pageNo == 0) {
            return 0;
        } else {
            return (pageNo == pageIndicesSize ? Math.max(startLayouts.size() - 1, 0) : lineIndices[pageNo]);
        }
    }
     */
    /**
    * @param tl a TextLayout
    * @param currentLine
    * @return <tt>true</tt> iff <tt>tl</tt> is a TextLayout that does not represent wrapped line
    */
    private boolean isNewline(TextLayout tl, int currentLine) {
        if (currentLine >= startLayouts.size()) {
            return false; // wrapping appeared
        } else {
            return startLayouts.get(currentLine) == tl;
        }
    }

    /** Computes alignment for a TextLayout with given bounds on the page with given width
    * and for given alignment policy.
    *
    * @param rect Bounds of a TextLayout
    * @param width page width
    * @param alignment one of @see PageSettings#LEFT @see PageSettings#CENTER @see PageSettings#RIGHT
    */
    private static float computeStart(Rectangle2D rect, float width, PrintPreferences.Alignment alignment) {
        float x;

        if (rect instanceof Rectangle2D.Float) {
            x = ((Rectangle2D.Float) rect).width;
        } else {
            x = (float) ((Rectangle2D.Double) rect).width;
        }

        if (x >= width) {
            return 0;
        }

        if (alignment == PrintPreferences.Alignment.LEFT) {
            return 0;
        } else if (alignment == PrintPreferences.Alignment.RIGHT) {        
            return (width - x);
        } else {
            return (width - x) / 2;
        }
    }

    /**
    * @param wrappingWidth width of the layout
    * @param frc for possible new instance of LineBreakMeasurer
    * @return next TextLayout that is to be rendered
    */
    private TextLayout nextLayout(float wrappingWidth, FontRenderContext frc) {
        TextLayout l;

        if (currentLayout == textLayouts.size()) {
            LineBreakMeasurer old = lineBreakMeasurer;
            LineBreakMeasurer measurer = getMeasurer(frc);

            if (measurer == null) {
                return null;
            }

            l = measurer.nextLayout(wrappingWidth);
            textLayouts.add(l);

            if (old != measurer) { // new line
                startLayouts.add(l);
            }
        } else {
            l = textLayouts.get(currentLayout);
        }

        currentLayout++; // advance to next

        return l;
    }

    /** Sets @see #currentLayout variable then calls nextLayout.
    * @param pageNo searched page
    * @param wrappingWidth width of the layout
    * @param frc for possible new instance of LineBreakMeasurer
    * @return next TextLayout that is to be rendered
    */
    private TextLayout layoutForPage(int pageNo, float wrappingWidth, FontRenderContext frc) {
        if (pageNo > (pageIndicesSize + 1)) {
            throw new IllegalArgumentException(
                "Page number " + pageNo // NOI18N
                 +" is bigger than array size " + (pageIndicesSize + 1)
            ); // NOI18N
        }

        // first request for a page  // pageNo==3 -> fourth page to print
        if (pageNo == pageIndicesSize) {
            // small array?
            if (pageIndicesSize >= pageIndices.length) {
                pageIndices = increaseArray(pageIndices);
                lineIndices = increaseArray(lineIndices);
            }

            // layouts for given page starts at:
            pageIndices[pageIndicesSize] = Math.max(textLayouts.size() - 1, 0);

            // remember - in the for loop above last layout is not printed
            // if page breaks
            lineIndices[pageIndicesSize++] = Math.max(startLayouts.size() - 1, 0);
        }

        currentLayout = pageIndices[pageNo]; // set the iterator

        return nextLayout(wrappingWidth, frc); // iterate
    }

    /** Called only if new TextLayouts are in need.
    * @param frc is used for possible new LineBreakMeasurer instance
    * @return current LineBreakMeasurer or <tt>null</tt> if no is available.
    */
    private LineBreakMeasurer getMeasurer(FontRenderContext frc) {
        if (lineBreakMeasurer == null) { // first page to print
            lineBreakMeasurer = new LineBreakMeasurer(styledTexts[currentStyledText], frc);

            // no layouts available in this measurer?
        } else if (lineBreakMeasurer.getPosition() >= styledTexts[currentStyledText].getEndIndex()) {
            // next measurer is not available?
            if (currentStyledText == (styledTexts.length - 1)) {
                return null; // everything is printed
            } else { // use next styledTexts entry
                lineBreakMeasurer = new LineBreakMeasurer(styledTexts[++currentStyledText], frc);
            }
        }

        return lineBreakMeasurer;
    }

    // ------------------ options -----------------

    /** @return true iff wrapping is on*/
    private static boolean wrap() {
        return PrintPreferences.getWrap();
    }

    /** @return String describing header */
    private static String getHeaderFormat() {
        return PrintPreferences.getHeaderFormat();
    }

    /** @return String describing footer */
    private static String getFooterFormat() {
        return PrintPreferences.getFooterFormat();
    }

    /** @return font for header */
    private static Font getHeaderFont() {
        return PrintPreferences.getHeaderFont();
    }

    /** @return font for footer */
    private static Font getFooterFont() {
        return PrintPreferences.getFooterFont();
    }

    /** @return an alignment constant for footer */
    private static PrintPreferences.Alignment getFooterAlignment() {
        return PrintPreferences.getFooterAlignment();
    }

    /** @return an alignment constant for header */
    private static PrintPreferences.Alignment getHeaderAlignment() {
        return PrintPreferences.getHeaderAlignment();
    }

    /** @return a line ascent correction */
    private static float getLineAscentCorrection() {
        return PrintPreferences.getLineAscentCorrection();
    }

    /** @return false */
    private static boolean lineNumbers() {
        return false;
    }

    // not used
    private static Font lineNumbersFont() {
        return new Font("Courier", java.awt.Font.PLAIN, 6); // NOI18N
    }

    // ----------------- options end --------------

    /** Creates new AttributedCharacterIterator for plain text.
     *
     * @return an AttributedCharacterIterator
     */
    private static AttributedCharacterIterator[] getIterators(Document doc) {
        if (doc instanceof NbDocument.Printable) {
            return ((NbDocument.Printable) doc).createPrintIterators();
        }

        // load options
        java.awt.Font f = new java.awt.Font("Courier", java.awt.Font.PLAIN, 8); // NOI18N

        AttributedCharacters achs = null;
        char[] chars = null;
        List<AttributedCharacterIterator> iterators = new ArrayList<AttributedCharacterIterator>(300);

        try {
            String document = doc.getText(0, doc.getLength());

            // now chars are filled from the document
            int firstCharInDoc = 0;

            for (int i = 0; i < document.length(); i++) { // search for new lines

                if (document.charAt(i) == '\n') {
                    chars = new char[i - firstCharInDoc + 1];
                    document.getChars(firstCharInDoc, chars.length + firstCharInDoc, chars, 0);
                    achs = new AttributedCharacters();
                    achs.append(chars, f, Color.black);
                    iterators.add(achs.iterator()); // new iterator for new line
                    firstCharInDoc = i + 1;
                }
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }

        AttributedCharacterIterator[] iters = new AttributedCharacterIterator[iterators.size()];
        iterators.toArray(iters);

        return iters;
    }

    /**
    * @param doc
    * @return filename from which the document is loaded.
    */
    private static String getFilename(Document doc) {
        String ret = (String) doc.getProperty(javax.swing.text.Document.TitleProperty);

        return ((ret == null) ? "UNKNOWN" : ret); // NOI18N
    }

    /** Doubles given array. The old one is then copied into the new one.
    * @return new int array
    */
    private static int[] increaseArray(int[] old) {
        int[] ret = new int[2 * old.length];
        System.arraycopy(old, 0, ret, 0, old.length);

        return ret;
    }

    // ------------------ cancellation dialog ---------------------------

    /** Creates cancellation dialog.
    * @param job PrinterJob
    */
    private void createCancellationPanel(final PrinterJob job) {
        cancellationPanel = new CancellationPanel(job);

        DialogDescriptor ddesc = new DialogDescriptor(
                cancellationPanel, NbBundle.getMessage(PrintPreferences.class, "CTL_Print_cancellation"), false,
                new Object[] { NbBundle.getMessage(PrintPreferences.class, "CTL_Cancel") },
                NbBundle.getMessage(PrintPreferences.class, "CTL_Cancel"), DialogDescriptor.BOTTOM_ALIGN, null,
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        setCancelled(job);
                        closeDialog();
                    }
                }
            );
        setDialog(DialogDisplayer.getDefault().createDialog(ddesc));
    }

    /** Closes cancellationDialog */
    void closeDialog() {
        if (cancellationDialog != null) {
            cancellationDialog.setVisible(false);
            cancellationDialog.dispose();
        }
    }

    /** @param <tt>d</tt> New value of <tt>cancellationDialog</tt>*/
    void setDialog(Dialog d) {
        d.setVisible(true);
        d.pack();
        cancellationDialog = d;
    }

    /** packs the dialog */
    void packDialog() {
        if (cancellationDialog != null) {
            cancellationDialog.pack();
        }
    }

    /** Marks this job as cancelled.
    * @param <tt>job</tt>
    */
    void setCancelled(PrinterJob job) {
        job.cancel();
    }

    /** @return <tt>true</tt> iff the job was cancelled */
    boolean isCancelled(PrinterJob job) {
        return job.isCancelled();
    }

    /** Replaces an empty iterator by an iterator conatining one space. */
    private static void replaceEmptyIterators(AttributedCharacterIterator[] iters) {
        for (int i = 0; i < iters.length; i++) {
            AttributedCharacterIterator achit = iters[i];

            if (achit.getBeginIndex() == achit.getEndIndex()) {
                AttributedCharacters at = new AttributedCharacters();
                at.append(' ', getFontInstance(), Color.white);
                iters[i] = at.iterator();
            }
        }
    }

    /** @return cached font instance */
    static Font getFontInstance() {
        if (fontInstance == null) {
            fontInstance = new java.awt.Font("Dialog", java.awt.Font.PLAIN, 14); // NOI18N
        }

        return fontInstance;
    }

    /** CancellationPanel class allows user to cancel current PrinterJob */
    static final class CancellationPanel extends javax.swing.JPanel {
        static final long serialVersionUID = -6419253408585188541L;

        /** Label indicating progress. */
        private final javax.swing.JLabel printProgress;

        /** Format of displayed text. */
        private final MessageFormat format;

        /** Parameters for <tt>format</tt>. */
        private final Object[] msgParams;

        /**
        * @param <tt>job</tt> PrinterJob
        * @exception IllegalArgumentException is thrown if <tt>job</tt> is <tt>null</tt>.
        */
        public CancellationPanel(PrinterJob job) {
            if (job == null) {
                throw new IllegalArgumentException();
            }

            format = new MessageFormat(NbBundle.getMessage(PrintPreferences.class, "CTL_Print_progress"));
            msgParams = new Object[1];

            setLayout(new java.awt.BorderLayout());
            setBorder(new javax.swing.border.EmptyBorder(12, 12, 0, 12));
            printProgress = new javax.swing.JLabel();
            printProgress.setHorizontalAlignment(javax.swing.JLabel.CENTER);
            add(printProgress);
        }

        /** Advances progress.
        * @param <tt>pageno</tt> Page number that was printed.
        */
        public void setPageno(int pageno) {
            msgParams[0] = new Integer(pageno + 1);
            printProgress.setText(format.format(msgParams));
            getAccessibleContext().setAccessibleDescription(printProgress.getText());
        }
    }
}
