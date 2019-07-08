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
package org.openide.text;

import org.openide.options.ContextSystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Font;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** Settings for output window.
*
* @author Ales Novak
*/
public final class PrintSettings extends ContextSystemOption {
    // final because overrides Externalizable methods

    /** serialVersionUID */
    static final long serialVersionUID = -9102470021814206818L;

    /** Constant for center position of page header. */
    public static final int CENTER = 0x1;

    /** Constant for right position of page header. */
    public static final int RIGHT = 0x2;

    /** Constant for left position of page header. */
    public static final int LEFT = 0x0;

    /** Property name of the wrap property */
    public static final String PROP_PAGE_FORMAT = "pageFormat"; // NOI18N

    /** Property name of the wrap property */
    public static final String PROP_WRAP = "wrap"; // NOI18N

    /** Property name of the header format  property */
    public static final String PROP_HEADER_FORMAT = "headerFormat"; // NOI18N

    /** Property name of the footer format property */
    public static final String PROP_FOOTER_FORMAT = "footerFormat"; // NOI18N

    /** Property name of the header font property */
    public static final String PROP_HEADER_FONT = "headerFont"; // NOI18N

    /** Property name of the footer font property */
    public static final String PROP_FOOTER_FONT = "footerFont"; // NOI18N

    /** Property name of the header alignment property */
    public static final String PROP_HEADER_ALIGNMENT = "headerAlignment"; // NOI18N

    /** Property name of the footer alignment property */
    public static final String PROP_FOOTER_ALIGNMENT = "footerAlignment"; // NOI18N

    /** Property name of the line ascent correction property */
    public static final String PROP_LINE_ASCENT_CORRECTION = "lineAscentCorrection"; // NOI18N
    private static final String HELP_ID = "editing.printing"; // !!! NOI18N


    public String displayName() {
        return NbBundle.getMessage(PrintSettings.class, "CTL_Print_settings");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }

    /** @return an instance of PageFormat
    * The returned page format is either previously set by
    * PageSetupAction or is acquired as a default PageFormat
    * from supported PrinterJob
    */
    public static PageFormat getPageFormat(PrinterJob pj) {
        return PrintPreferences.getPageFormat(pj);
    }

    /** @deprecated Use {@link #getPageFormat(PrinterJob)} instead. */
    @Deprecated
    public PageFormat getPageFormat() {
        return getPageFormat(PrinterJob.getPrinterJob());
    }

    /** sets page format */
    public void setPageFormat(PageFormat pf) {
        PrintPreferences.setPageFormat(pf);
        firePropertyChange(PROP_PAGE_FORMAT, null, pf);
    }

    public boolean getWrap() {
        return PrintPreferences.getWrap();
    }

    public void setWrap(boolean b) {
        PrintPreferences.setWrap(b);
        firePropertyChange(PROP_WRAP, (b ? Boolean.FALSE : Boolean.TRUE), (b ? Boolean.TRUE : Boolean.FALSE));
    }

    public String getHeaderFormat() {
        return PrintPreferences.getHeaderFormat();
    }

    public void setHeaderFormat(String s) {
        PrintPreferences.setHeaderFormat(s);
        firePropertyChange(PROP_HEADER_FORMAT, null, s);
    }

    public String getFooterFormat() {
        return PrintPreferences.getFooterFormat();
    }

    public void setFooterFormat(String s) {
        PrintPreferences.setFooterFormat(s);
        firePropertyChange(PROP_FOOTER_FORMAT, null, s);
    }

    public Font getHeaderFont() {
        return PrintPreferences.getHeaderFont();
    }

    public void setHeaderFont(Font f) {
        PrintPreferences.setHeaderFont(f);
        firePropertyChange(PROP_HEADER_FONT, null, f);
    }

    public Font getFooterFont() {
        return PrintPreferences.getFooterFont();
    }

    public void setFooterFont(Font f) {
        PrintPreferences.setFooterFont(f);
        firePropertyChange(PROP_FOOTER_FONT, null, f);
    }

    public int getHeaderAlignment() {
        return fromAlignment(PrintPreferences.getHeaderAlignment());
    }

    public void setHeaderAlignment(int alignment) {
        PrintPreferences.setHeaderAlignment(toAlignment(alignment));
        firePropertyChange(PROP_HEADER_ALIGNMENT, null, new Integer(alignment));
    }
    
    private PrintPreferences.Alignment toAlignment(int alignment) {
        PrintPreferences.Alignment retval = PrintPreferences.Alignment.CENTER;
        switch(alignment) {
            case CENTER:
                retval = PrintPreferences.Alignment.CENTER;
                break;
            case LEFT:
                retval = PrintPreferences.Alignment.LEFT;
                break;
            case RIGHT:
                retval = PrintPreferences.Alignment.RIGHT;
                break;                            
        }
        return retval;
    }
    
    private int fromAlignment(PrintPreferences.Alignment alignment) {
        int retval = CENTER;
        if (PrintPreferences.Alignment.CENTER.equals(alignment)) {
            retval = CENTER;
        } else if (PrintPreferences.Alignment.LEFT.equals(alignment)) {
            retval = LEFT;
        } else if (PrintPreferences.Alignment.RIGHT.equals(alignment)) {
            retval = RIGHT;
        }
        return retval;
    }
    

    public int getFooterAlignment() {
        return fromAlignment(PrintPreferences.getFooterAlignment());
    }

    public void setFooterAlignment(int alignment) {
        PrintPreferences.setFooterAlignment(toAlignment(alignment));
        firePropertyChange(PROP_FOOTER_ALIGNMENT, null, new Integer(alignment));
    }

    /** Getter for lineAscentCorrection property. */
    public float getLineAscentCorrection() {
        return PrintPreferences.getLineAscentCorrection();
    }

    /** Setter for lineAscentCorrection property.
    * @param correction the correction
    * @exception IllegalArgumentException if <tt>correction</tt> is less than 0.
    */
    public void setLineAscentCorrection(float correction) {
        PrintPreferences.setLineAscentCorrection(correction);
        firePropertyChange(PROP_LINE_ASCENT_CORRECTION, null, new Float(correction));
    }

    public void writeExternal(ObjectOutput obtos) throws IOException {
        // no-op, nobody should be externalizing this option, but if they do
        // just do not store anything
    }
    
    public void readExternal(ObjectInput obtis) throws IOException, ClassNotFoundException {
        // no-op, to ignore previously serialized options
    }
    
    /** Property editor for alignment properties */
    public static class AlignmentEditor extends java.beans.PropertyEditorSupport {
        private String sCENTER;
        private String sRIGHT;
        private String sLEFT;
        private String[] tags = new String[] {
                sLEFT = NbBundle.getMessage(PrintSettings.class, "CTL_LEFT"),
                sCENTER = NbBundle.getMessage(PrintSettings.class, "CTL_CENTER"),
                sRIGHT = NbBundle.getMessage(PrintSettings.class, "CTL_RIGHT")
            };

        public String[] getTags() {
            return tags;
        }

        public String getAsText() {
            return tags[((Integer) getValue()).intValue()];
        }

        public void setAsText(String s) {
            if (s.equals(sLEFT)) {
                setValue(new Integer(0));
            } else if (s.equals(sCENTER)) {
                setValue(new Integer(1));
            } else {
                setValue(new Integer(2));
            }
        }
    }

    /** Property editor for PageFormat instances */
    public static class PageFormatEditor extends java.beans.PropertyEditorSupport {
        /** No text */
        public String getAsText() {
            return null;
        }

        /* @return <tt>true</tt> */
        public boolean supportsCustomEditor() {
            return true;
        }

        /**
        * @return <tt>null</tt> Shows pageDialog, however.
        */
        public java.awt.Component getCustomEditor() {
            PageFormat pf = (PageFormat) getValue();
            PrinterJob pj = PrinterJob.getPrinterJob();
            PageFormat npf = pj.pageDialog(pf);

            //setValue(npf);
            ((PrintSettings)PrintSettings.findObject(PrintSettings.class)).setPageFormat((PageFormat) npf.clone());
            pj.cancel();

            return null;
        }
    }
}
