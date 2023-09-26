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

package org.netbeans.swing.plaf.util;

import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderAdapter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.MetalTheme;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.net.URL;
import java.util.HashSet;
import java.util.StringTokenizer;
import javax.swing.plaf.metal.DefaultMetalTheme;

/** An extension of javax.swing.plaf.metal.DefaultMetalTheme which can read a an xml
 * file named <code>themes.xml</code> and apply the theme defined in XML.  NbTheme
 * stores its data in the <code>UIDefaults</code> instance available from the
 * look-and-feel manager (<code>UiManager.getLookAndFeel.getDefaults()</code>).
 * These means that theme files may also override per-component-type settings
 * stored there, so changes to look and feel deeper than those afforded by the
 * <code>MetalTheme</code> parent class are possible.
 * <P>
 * <code>NbTheme</code> supports seven kinds of data: Colors, Fonts, Integers,
 * Strings, Borders, Insets and Booleans, which are the major interesting data types typical
 * stored in <code>UIDefaults</code>.  For usage instructions and details,
 * see <A HREF="http://ui.netbeans.org/docs/ui/themes/themes.html">the themes
 * documentation</a> on the NetBeans web site.
 *
 * @author Jiri Mzourek, Tim Boudreau
 * @see http://ui.netbeans.org/docs/ui/themes/themes.html
 */
public class NbTheme extends DefaultMetalTheme implements org.xml.sax.DocumentHandler {
    
    /** The unqualified name for the theme file to use. */    
    public static final  String THEMEFILE_NAME = "themes.xml"; // NOI18N
    // legal xml tags for theme files
    private static final String THEMESET_TAG = "themeset"; // NOI18N
    private static final String ACTIVE_ATTR = "active"; // NOI18N
    private static final String THEME_TAG = "theme"; // NOI18N
    private static final String BOOL_TAG = "boolean"; // NOI18N
    private static final String DIM_TAG = "dimension"; // NOI18N
    private static final String FONT_TAG = "font"; // NOI18N
    private static final String INSETS_TAG = "insets";  // NOI18N
    private static final String ETCHEDBORDER_TAG = "etchedborder";
    private static final String EMPTYBORDER_TAG  = "emptyborder";
    private static final String BEVELBORDER_TAG = "bevelborder";
    private static final String LINEBORDER_TAG = "lineborder";
    // attributes recognized in various tags
    private static final String COLOR_ATTR = "color"; // NOI18N
    private static final String KEY_ATTR = "key"; // NOI18N
    private static final String METRIC_TAG = "metric"; // NOI18N
    private static final String STRING_TAG = "string"; // NOI18N
    private static final String NAME_ATTR = "name"; // NOI18N
    private static final String FONTSTYLE_ATTR = "style"; // NOI18N
    private static final String FONTSIZE_ATTR = "size"; // NOI18N
    private static final String VALUE_ATTR = "value";  //NOI18N
    private static final String WIDTH_ATTR = "width";  //NOI18N
    private static final String HEIGHT_ATTR = "height"; //NOI18N
    private static final String RED_ATTR = "r"; // NOI18N
    private static final String GREEN_ATTR = "g"; // NOI18N
    private static final String BLUE_ATTR = "b"; // NOI18N
    private static final String LEFT_ATTR = "left"; // NOI18N
    private static final String TOP_ATTR = "top"; // NOI18N
    private static final String RIGHT_ATTR = "right"; // NOI18N
    private static final String BOTTOM_ATTR = "bottom"; // NOI18N
    private static final String TYPE_ATTR = "type"; // NOI18N
    private static final String REFERENCE_ATTR = "reference"; // NOI18N
    // font styles
    private static final String FONTSTYLE_BOLD = "bold"; // NOI18N
    private static final String FONTSTYLE_ITALIC = "italic"; // NOI18N
    //border types
    private static final String TYPE_LOWERED = "lowered"; // NOI18N
    
    // keys used to store theme values in UIDefaults
    private static final String CONTROLFONT = "controlFont"; // NOI18N
    private static final String SYSTEMFONT = "systemFont"; // NOI18N
    private static final String USERFONT = "userFont"; // NOI18N
    private static final String MENUFONT = "menuFont"; // NOI18N
    private static final String WINDOWTITLEFONT = "windowTitleFont"; // NOI18N
    private static final String SUBFONT = "subFont"; // NOI18N
    
    //Keys below here are MetalTheme-specific and have no meaning on
    //non-Metal look and feels
    private static final String PRIMARY1 = "primary1"; // NOI18N
    private static final String PRIMARY2 = "primary2"; // NOI18N
    private static final String PRIMARY3 = "primary3"; // NOI18N
    private static final String SECONDARY1 = "secondary1"; // NOI18N
    private static final String SECONDARY2 = "secondary2"; // NOI18N
    private static final String SECONDARY3 = "secondary3"; // NOI18N
    private static final String WHITE = "white"; // NOI18N
    private static final String BLACK = "black"; // NOI18N

    private HashSet<String> activeThemes=null;
    private boolean inActiveTheme=false;
    private URL themeURL = null;
    
    private UIDefaults defaults;
    @Override
    public String getName(){ return "NetBeans XML Theme"; } // NOI18N
    /** Create a new instance of NBTheme */
    public NbTheme(URL themeURL, LookAndFeel lf) {
        this.themeURL = themeURL;
        defaults = lf.getDefaults();
//        initThemeDefaults();
        parseTheme();
        UIManager.getDefaults().putAll (defaults);
    }
    
    /** Add any custom UIDefault values for Metal L&F here */
    void initThemeDefaults () {
        defaults.put(PRIMARY1, new ColorUIResource(102, 102, 153)); 
        defaults.put(PRIMARY2, new ColorUIResource(153, 153, 204)); 
        defaults.put(PRIMARY3, new ColorUIResource(204, 204, 255)); 

        defaults.put(SECONDARY1, new ColorUIResource(102, 102, 102)); 
        defaults.put(SECONDARY2, new ColorUIResource(153, 153, 153)); 
        defaults.put(SECONDARY3, new ColorUIResource(204, 204, 204)); 
        
        defaults.put(WHITE, new ColorUIResource(255,255,255)); 
        defaults.put(BLACK, new ColorUIResource(0,0,0)); 
    }
    
    private void parseTheme(){
        try{
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            Parser p = new XMLReaderAdapter(factory.newSAXParser().getXMLReader());
            p.setDocumentHandler(this);
            String externalForm = themeURL.toExternalForm();
            InputSource is = new InputSource(externalForm);
            p.parse(is);
            activeThemes=null;  //dispose of now useless hashtable
            locator = null;
        }
        catch(java.io.IOException ie){
            System.err.println ("IO exception reading theme file"); //NOI18N
        } catch(org.xml.sax.SAXException se){
            System.err.println ("Error parsing theme file " + (locator != null ? "line " + locator.getLineNumber() : "")); //NOI18N
        } catch (ParserConfigurationException e) {
            System.err.println ("Couldn't create XML parser for theme file"); //NOI18N
        }
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
    }
    
    public void startDocument() throws org.xml.sax.SAXException {
    }
    
    public void startElement(java.lang.String p1,org.xml.sax.AttributeList atts) throws org.xml.sax.SAXException {
         //found the themeset?
        if (p1.equals(THEMESET_TAG)) {
            //break out the comma delimited list of active themes
            //and stores them in activeThemes hashset
            String themes = atts.getValue (ACTIVE_ATTR);
            if (themes != null) {
                StringTokenizer tok = new StringTokenizer (themes, ","); //NOI18N
                activeThemes = new HashSet<String> (tok.countTokens() + 1);
                while (tok.hasMoreTokens()) {
                    activeThemes.add(tok.nextToken().trim());
                }
            }
        }
        else{
            if (p1.equals(THEME_TAG) && (activeThemes != null)) {
                //see if the current theme is one of the active ones
                String themeName = atts.getValue (NAME_ATTR);
                inActiveTheme = activeThemes.contains(themeName);
            } else {
                if (inActiveTheme) {
                    if (handleReference(atts)) {
                        return;
                    }
                    if (p1.equals (COLOR_ATTR)) {
                        handleColor (atts);
                        return;
                    }
                    if (p1.equals (FONT_TAG)) {
                        handleFont (atts);
                        return;
                    }
                    if (p1.equals (EMPTYBORDER_TAG)) {
                        handleEmptyBorder (atts);
                        return;
                    }
                    if (p1.equals (METRIC_TAG)) {
                        handleMetric (atts);
                        return;
                    }
                    if (p1.equals (STRING_TAG)) {
                        handleString (atts);
                        return;
                    }
                    if (p1.equals (INSETS_TAG)) {
                        handleInsets (atts);
                        return;
                    }
                    if (p1.equals (BOOL_TAG)) {
                        handleBool (atts);
                        return;
                    }
                    if (p1.equals (DIM_TAG)) {
                        handleDim (atts);
                        return;
                    }
                    if (p1.equals (ETCHEDBORDER_TAG)) {
                        handleEtchedBorder (atts);
                        return;
                    }
                    if (p1.equals (LINEBORDER_TAG)) {
                        handleLineBorder (atts);
                        return;
                    }
                    if (p1.equals (BEVELBORDER_TAG)) {
                        handleBevelBorder (atts);
                        return;
                    }
                    System.err.println("UNRECOGNIZED " +
                            "THEME ENTRY " + p1 + "\" " + atts.toString()); //NOI18N
                }
            }
        }
    }
    
    private boolean handleReference(org.xml.sax.AttributeList atts) throws SAXException  {
        String key = atts.getValue (KEY_ATTR);
        String reference = atts.getValue(REFERENCE_ATTR);
        if (reference != null) {
            Object res = defaults.get(reference);
            if (res != null) {
                defaults.put(key, res);
                return true;
            }
        }
        return false;
    }
    
    private final void handleFont (org.xml.sax.AttributeList atts) throws SAXException  {
        String key = atts.getValue (KEY_ATTR);
        String fontname = atts.getValue (NAME_ATTR);
        String fontstylename = atts.getValue (FONTSTYLE_ATTR);
        int fontsize = intFromAttr (atts, FONTSIZE_ATTR);
        int fontstyle = Font.PLAIN;
        if(fontstylename.equals(FONTSTYLE_BOLD)) {
            fontstyle = Font.BOLD;
        } else {
            if(fontstylename.equals(FONTSTYLE_ITALIC)) fontstyle = Font.ITALIC;
        }
        
        FontUIResource resource = new FontUIResource (fontname, fontstyle, fontsize);
        defaults.put (key, resource);
    }
        
    private final void handleColor (org.xml.sax.AttributeList atts) throws SAXException  {
        int r = intFromAttr (atts, RED_ATTR);
        int g = intFromAttr (atts, GREEN_ATTR);
        int b = intFromAttr (atts, BLUE_ATTR);
        String key = atts.getValue(KEY_ATTR);
        ColorUIResource resource = new ColorUIResource (r,g,b);
        defaults.put (key, resource);
    }

    private final void handleMetric (org.xml.sax.AttributeList atts) throws SAXException  {
        String key = atts.getValue(KEY_ATTR);
        Integer resource = Integer.valueOf (atts.getValue(VALUE_ATTR));
        defaults.put (key, resource);
    }
    
    private final void handleString (org.xml.sax.AttributeList atts) throws SAXException  {
        String key = atts.getValue (KEY_ATTR);
        String resource = atts.getValue (VALUE_ATTR);
        defaults.put (key, resource);
    }

    private final void handleBool (org.xml.sax.AttributeList atts) throws SAXException  {
        String key = atts.getValue (KEY_ATTR);
        Boolean resource = Boolean.valueOf (key);
        defaults.put (key, resource);
    }
    
    private final void handleDim (org.xml.sax.AttributeList atts) throws SAXException  {
        String key = atts.getValue (KEY_ATTR);
        int width = intFromAttr (atts, WIDTH_ATTR);
        int height = intFromAttr (atts, HEIGHT_ATTR);
        DimensionUIResource resource = new DimensionUIResource (width, height);
        defaults.put (key, resource);
    }
    
    private final void handleInsets (org.xml.sax.AttributeList atts) throws SAXException  {
        String key = atts.getValue (KEY_ATTR);
        int top = intFromAttr (atts, TOP_ATTR);
        int left = intFromAttr (atts, LEFT_ATTR);
        int bottom = intFromAttr (atts, BOTTOM_ATTR);
        int right = intFromAttr (atts, RIGHT_ATTR);
        InsetsUIResource resource = new InsetsUIResource (top, left, bottom,
          right);
        defaults.put (key, resource);
    }

    private final void handleEtchedBorder (org.xml.sax.AttributeList atts) {
        String key = atts.getValue (KEY_ATTR);
        int i = EtchedBorder.LOWERED;
        String type = atts.getValue (TYPE_ATTR);
        if (type != null) 
            i = type.equals (TYPE_LOWERED) ? EtchedBorder.LOWERED : EtchedBorder.RAISED;
        BorderUIResource.EtchedBorderUIResource resource = new BorderUIResource.EtchedBorderUIResource (i);
        defaults.put (key, resource);
    }
    
    private final void handleBevelBorder (org.xml.sax.AttributeList atts) {
        String key = atts.getValue (KEY_ATTR);
        int i = BevelBorder.LOWERED;
        String type = atts.getValue (TYPE_ATTR);
        if (type != null) 
            i = type.equals (TYPE_LOWERED) ? BevelBorder.LOWERED : BevelBorder.RAISED;
        BorderUIResource.BevelBorderUIResource resource = new BorderUIResource.BevelBorderUIResource (i);
        defaults.put (key, resource);
    }
    
    private final void handleEmptyBorder (org.xml.sax.AttributeList atts) throws SAXException {
        String key = atts.getValue (KEY_ATTR);
        int top = intFromAttr (atts, TOP_ATTR);
        int left = intFromAttr (atts, LEFT_ATTR);
        int bottom = intFromAttr (atts, BOTTOM_ATTR);
        int right = intFromAttr (atts, RIGHT_ATTR);
        BorderUIResource.EmptyBorderUIResource resource = new BorderUIResource.EmptyBorderUIResource (top, left, bottom, right);
        defaults.put (key, resource);
    }
    
    private final void handleLineBorder (org.xml.sax.AttributeList atts) throws SAXException {
        String key = atts.getValue (KEY_ATTR);
        int r = intFromAttr (atts, RED_ATTR);
        int g = intFromAttr (atts, GREEN_ATTR);
        int b = intFromAttr (atts, BLUE_ATTR);
        int width = 1;
        if (atts.getValue(WIDTH_ATTR) != null) {
            width = intFromAttr (atts, WIDTH_ATTR);
        }
        Color c = new Color (r,g,b);
        BorderUIResource.LineBorderUIResource resource = new BorderUIResource.LineBorderUIResource (c);
        defaults.put (key, resource);
    }
    
    private final int intFromAttr (final org.xml.sax.AttributeList atts, final String key) throws SAXException {
        try {
            return Integer.valueOf (atts.getValue (key)).intValue();
        } catch (NumberFormatException nfe) {
            throw new SAXException (atts.getValue(key) + " is not an integer");
        }
    }
    
    public void characters(char[] p1,int p2,int p3) throws org.xml.sax.SAXException {
    }

    Locator locator = null;
    public void setDocumentLocator(org.xml.sax.Locator locator) {
        this.locator = locator;
    }
    
    public void endElement(java.lang.String p1) throws org.xml.sax.SAXException {
        if(p1.equals(THEME_TAG)){
            inActiveTheme=false;
        }
    }
    
    public void ignorableWhitespace(char[] p1,int p2,int p3) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(java.lang.String p1,java.lang.String p2) throws org.xml.sax.SAXException {
    }

    private final ColorUIResource getColor(String key) {
        return (ColorUIResource) defaults.get (key);
    }
    
    private final FontUIResource getFont(String key) {
        return (FontUIResource) defaults.get (key);
    }

    @Override
    public FontUIResource getControlTextFont() { return getFont (CONTROLFONT); }
    @Override
    public FontUIResource getSystemTextFont() { return getFont (SYSTEMFONT); }
    @Override
    public FontUIResource getUserTextFont() { return getFont (USERFONT); }
    @Override
    public FontUIResource getMenuTextFont() { return getFont (MENUFONT); }
    @Override
    public FontUIResource getWindowTitleFont() { return getFont (WINDOWTITLEFONT); }
    @Override
    public FontUIResource getSubTextFont() { return getFont (SUBFONT); }
    @Override
    protected ColorUIResource getPrimary1() { return getColor (PRIMARY1); }
    @Override
    protected ColorUIResource getPrimary2() { return getColor (PRIMARY2); }
    @Override
    protected ColorUIResource getPrimary3() { return getColor (PRIMARY3); }
    @Override
    protected ColorUIResource getSecondary1() { return getColor (SECONDARY1); }
    @Override
    protected ColorUIResource getSecondary2() { return getColor (SECONDARY2); }
    @Override
    protected ColorUIResource getSecondary3() { return getColor (SECONDARY3); }
    @Override
    protected ColorUIResource getWhite() { return getColor (WHITE); }
    @Override
    protected ColorUIResource getBlack() { return getColor (BLACK); }
    
}
