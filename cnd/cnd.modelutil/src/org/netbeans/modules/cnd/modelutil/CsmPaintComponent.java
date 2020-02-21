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

package org.netbeans.modules.cnd.modelutil;
import java.util.Iterator;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.netbeans.swing.plaf.LFCustoms;

/**
 *
 * after JCPaintComponent
 */

public abstract class CsmPaintComponent extends JPanel {

    DrawState drawState = new DrawState();

    protected Font drawFont;

    private static final int ICON_WIDTH = 16;
    private static final int ICON_TEXT_GAP = 5;

    private int fontHeight;

    private int ascent;

    private FontMetrics fontMetrics;

    protected boolean isSelected;

   // private String text;

    private ArrayList<PostfixString> postfixes;

    private static final String THROWS = " throws "; // NOI18N


    private static final String[] frequentWords = new String[] {
        "", " ", "[]", "(", ")", ", ", "String", THROWS // NOI18N
    };

    private final static Color KEYWORD_COLOR = Color.gray;
    private final static Color POSTFIX_COLOR = Color.lightGray;

    private Icon icon;

    protected int modifiers = 0;

    private static Border BORDER = BorderFactory.createEmptyBorder(0, 3, 0, 3);
    private static LayoutManager layout = new FlowLayout();
    public CsmPaintComponent(){
        super(layout, true);
        setBorder(BORDER);
        postfixes = new ArrayList<PostfixString>();
    }

    public void setSelected(boolean isSelected){
        this.isSelected = isSelected;
    }

    protected boolean isSelected(){
        return isSelected;
    }

    @Override
    public void paintComponent(Graphics g) {
        // clear background
        Color postfixColor = LFCustoms.shiftColor(POSTFIX_COLOR);
        g.setColor(getBackground());
        java.awt.Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        draw(g);

        if(!postfixes.isEmpty()) {
            drawString(g, " (", postfixColor); // NOI18N
            Iterator<PostfixString> iter = postfixes.iterator();
            while(iter.hasNext()) {
                iter.next().Draw(g);
                if(iter.hasNext()) {
                    drawString(g, ",  ", postfixColor); // NOI18N
                }
            }
            drawString(g, ")", postfixColor); // NOI18N
        }
    }

    public void appendPostfix(String text, Color c, int font) {
        postfixes.add(new PostfixString(text, c, font));
    }

    public void removePostfixes() {
        postfixes.clear();
    }

    public boolean hasPostfixes() {
        return !postfixes.isEmpty();
    }

    /** IMPORTANT:
     * when implemented => have to update toString!
     */
    abstract protected void draw(Graphics g);

    /**
     * returns string representation of paint item
     * IMPORTANT: have to be in sync with draw() method
     */
    @Override
    abstract public String toString();

    protected void setIcon(Icon icon){
        this.icon = icon;
    }

    protected Icon getIcon(){
        return icon;
    }


    /** Draw the icon if it is valid for the given type.
     * Here the initial drawing assignments are also done.
     */
    protected void drawIcon(Graphics g, Icon icon) {
        Insets i = getInsets();
        if (i != null) {
            drawState.drawX = i.left;
            drawState.drawY = i.top;
        } else {
            drawState.drawX = 0;
            drawState.drawY = 0;
        }

        if (icon != null) {
            if (g != null) {
                icon.paintIcon(this, g, drawState.drawX, drawState.drawY);
            }
            drawState.drawHeight = Math.max(fontHeight, icon.getIconHeight());
        } else {
            drawState.drawHeight = fontHeight;
        }
        drawState.drawX += ICON_WIDTH + ICON_TEXT_GAP;
        if (i != null) {
            drawState.drawHeight += i.bottom;
        }
        drawState.drawHeight += drawState.drawY;
        drawState.drawY += ascent;
    }

    protected void drawString(Graphics g, CharSequence s){
        drawString(g, s, false);
    }

    /** Draw string using the foreground color */
    protected void drawString(Graphics g, CharSequence s, boolean strike) {
        if (g != null) {
            g.setColor(getForeground());
        }
        drawStringToGraphics(g, s, null, strike);
    }


    /** Draw string with given color which is first possibly modified
     * by calling getColor() method to care about selection etc.
     */
    protected void drawString(Graphics g, CharSequence s, Color c) {
        if (g != null) {
            g.setColor(getColor(s, c));
        }
        drawStringToGraphics(g, s);
    }

    protected void drawString(Graphics g, CharSequence s, Color c, Font font, boolean strike) {
        if (g != null) {
            g.setColor(getColor(s, c));
            g.setFont(font);
        }
        drawStringToGraphics(g, s, font,  strike);
        if (g != null) {
            g.setFont(drawFont);
        }

    }

    protected void drawTypeName(Graphics g, String s, Color c) {
        if (g == null) {
            drawString(g, "   "); // NOI18N
            drawString(g, s, c);
        } else {
            int w = getWidth() - getWidth(s) - drawState.drawX;
            int spaceWidth = getWidth(" "); // NOI18N
            if (w > spaceWidth * 2) {
                drawState.drawX = getWidth() - 2 * spaceWidth - getWidth(s);
            } else {
                drawState.drawX = getWidth() - 2 * spaceWidth - getWidth(s) - getWidth("...   "); // NOI18N
                g.setColor(getBackground());
                g.fillRect(drawState.drawX, 0, getWidth() - drawState.drawX, getHeight());
                drawString(g, "...   ", c); // NOI18N
            }
            drawString(g, s, c);
        }
    }

    protected void drawStringToGraphics(Graphics g, CharSequence s) {
        drawStringToGraphics(g, s, null, false);
    }

    protected void drawStringToGraphics(Graphics g, CharSequence s, Font font, boolean strike) {
        String str = s != null ? s.toString() : "";
        if (g != null) {
            if (!strike){
                g.drawString(str, drawState.drawX, drawState.drawY);
            }else{
                Graphics2D g2 = ((Graphics2D)g);
                AttributedString strikeText = new AttributedString(str);
                strikeText.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                strikeText.addAttribute(TextAttribute.FONT, g.getFont());
                g2.drawString(strikeText.getIterator(), drawState.drawX, drawState.drawY);
            }
        }
        drawState.drawX += getWidth(str, font);
    }

    protected int getWidth(String s) {
        return fontMetrics.stringWidth(s);
    }

    protected int getWidth(String s, Font font) {
        if (font == null) {
            return getWidth(s);
        }
        return getFontMetrics(font).stringWidth(s);
    }

    protected Color getColor(CharSequence s, Color defaultColor) {
        return isSelected ? getForeground()
        : defaultColor;
    }

    private void storeWidth(String s) {
        fontMetrics.stringWidth(s);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);

        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        ascent = fontMetrics.getAscent();
        for (int i = 0; i < frequentWords.length; i++) {
            storeWidth(frequentWords[i]);
        }
        drawFont = font;
    }

    protected Font getDrawFont(){
        return drawFont;
    }

    @Override
    public Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawState.drawX += i.right;
        }
        if (drawState.drawX > getMaximumSize().width) {
            drawState.drawX = getMaximumSize().width;
        }
        return new Dimension(drawState.drawX, drawState.drawHeight);
    }

    public void setModifiers(int modifiers){
        this.modifiers = modifiers;
    }

    public int getModifiers(){
        return modifiers;
    }

    DrawState getDrawState() {
        return drawState;
    }

    void setDrawState(DrawState drawState) {
        this.drawState = drawState;
    }

    //.................. INNER CLASSES .......................

    private class PostfixString {
        private String text;
        private Color c;
        private int fontStyle;

        public PostfixString(String text, Color c, int fontStyle) {
            this.text = text;
            this.c = c;
            this.fontStyle = fontStyle;
        }

        public PostfixString(String text, int fontStyle) {
            this(text, LFCustoms.shiftColor(CsmPaintComponent.POSTFIX_COLOR), fontStyle);
        }

        void Draw(Graphics g) {
            CsmPaintComponent.this.drawString(g, text, c, new Font(getDrawFont().getName(),
                                                                   getDrawFont().getStyle() | fontStyle,
                                                                   getDrawFont().getSize()),
                                               false);

        }
    }

    private static class DrawState {
        int drawX, drawY;
        int drawHeight;

        public DrawState() {
            drawX = drawY = drawHeight = 0;
        }
    }

    public static class NamespacePaintComponent extends CsmPaintComponent{

        private String pkgName;
        private boolean displayFullNamespacePath;
        private Color NAMESPACE_COLOR = LFCustoms.shiftColor(new Color(64,255,64));

        public NamespacePaintComponent(){
            super();
        }

        public void setNamespaceName(String pkgName){
            this.pkgName = pkgName;
        }

        public void setDisplayFullNamespacePath(boolean displayFullNamespacePath){
            this.displayFullNamespacePath = displayFullNamespacePath;
        }

        @Override
        protected void draw(Graphics g) {
            // IMPORTANT:
            // when updated => have to update toString!
            drawIcon(g, getIcon());
            String name = pkgName;
            if (!displayFullNamespacePath) {
                name = name.substring(name.lastIndexOf('.') + 1);
            }
            drawString(g, name,	    NAMESPACE_COLOR);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            return pkgName;
        }
    }

    public static class NamespaceAliasPaintComponent extends CsmPaintComponent{

        private String aliasName;
        private Color NAMESPACE_COLOR = LFCustoms.shiftColor(new Color(64,255,64));

        public NamespaceAliasPaintComponent(){
            super();
        }

        public void setAliasName(String aliasName){
            this.aliasName = aliasName;
        }

        @Override
        protected void draw(Graphics g) {
            // IMPORTANT:
            // when updated => have to update toString!
            drawIcon(g, getIcon());
            drawString(g, aliasName,	    NAMESPACE_COLOR);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            return aliasName;
        }
    }

    public static class EnumPaintComponent extends CsmPaintComponent {

        String formatEnumName;
        private Color ENUM_COLOR = LFCustoms.shiftColor(new Color(255,64,64));
        private boolean displayFQN;

        public void EnumPaintComponent(String formatEnumName){
            this.formatEnumName = formatEnumName;
        }

        protected Color getColor(){
            return ENUM_COLOR;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, formatEnumName, getColor(), null, strike);
        }

        public void setFormatEnumName(String formatEnumName){
            this.formatEnumName = formatEnumName;
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            return formatEnumName;
        }
    }

    public static class EnumeratorPaintComponent extends CsmPaintComponent {

        String formatEnumeratorName;
        private Color ENUMERATOR_COLOR = LFCustoms.shiftColor(new Color(64,64,255));
        private boolean displayFQN;

        public void EnumeratorPaintComponent(String formatEnumeratorName){
            this.formatEnumeratorName = formatEnumeratorName;
        }

        protected Color getColor(){
            return ENUMERATOR_COLOR;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, formatEnumeratorName, getColor(), null, strike);
        }

        public void setFormatEnumeratorName(String formatEnumeratorName){
            assert(formatEnumeratorName != null);
            this.formatEnumeratorName = formatEnumeratorName;
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            return formatEnumeratorName;
        }
    }

    public static class ClassPaintComponent extends CsmPaintComponent{

        String formatClassName;
        private Color CLASS_COLOR = LFCustoms.shiftColor(new Color(255,64,64));
        private boolean displayFQN;

        public void setFormatClassName(String formatClassName){
            this.formatClassName = formatClassName;
        }

        protected Color getColor(){
            return CLASS_COLOR;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, formatClassName, getColor(), null, strike);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            return formatClassName;
        }
    }


    public static class TypedefPaintComponent extends CsmPaintComponent{

        String formatTypedefName;
        private final Color TYPEDEF_COLOR = new Color(46,146,199);//CsmFontColorManager.instance().getColor(FontColorProvider.Entity.TYPEDEF); //new Color(64,64,255).darker().darker().darker();
        //private boolean displayFQN;

        public void setFormatTypedefName(String formatTypedefName){
            this.formatTypedefName = formatTypedefName;

        }

        protected Color getColor(){
            return TYPEDEF_COLOR;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, formatTypedefName, getColor(), null, strike);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            return formatTypedefName;
        }
    }

    public static class StructPaintComponent extends ClassPaintComponent{

        private Color STRUCT_COLOR = LFCustoms.shiftColor(new Color(255,64,64));

        @Override
        protected Color getColor(){
            return STRUCT_COLOR;
        }

        public StructPaintComponent(){
            super();
        }
    }

    public static class UnionPaintComponent extends ClassPaintComponent{

        private Color UNION_COLOR = LFCustoms.shiftColor(new Color(255,64,64));

        @Override
        protected Color getColor(){
            return UNION_COLOR;
        }

        public UnionPaintComponent(){
            super();
        }
    }

    public static class FieldPaintComponent extends CsmPaintComponent{
        private Color FIELD_COLOR = new Color(79,155,27);//CsmFontColorManager.instance().getColor(FontColorProvider.Entity.CLASS_FIELD);//new Color(64,64,255).darker();
        protected String typeName;
        protected Color typeColor;
        protected String fldName;

        private boolean drawTypeAsPrefix = false;

        public FieldPaintComponent() {
            super();
        }

        public Color getNameColor() {
            return FIELD_COLOR;
        }

        @Override
        public void setName(String fldName){
            this.fldName= fldName;
        }

        public void setTypeColor(Color typeColor){
            this.typeColor = typeColor;
        }

        public void setTypeName(String typeName){
            this.typeName = typeName;
        }

        public Color getTypeColor(){
            return this.typeColor;
        }

        public String getTypeName(){
            return this.typeName;
        }

        public void setDrawTypeAsPrefix(boolean asPrefix) {
            this.drawTypeAsPrefix = asPrefix;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            //int level = CsmUtilities.getLevel(modifiers);
            drawIcon(g, getIcon());

            if (drawTypeAsPrefix) {
                drawString(g, getTypeName(), getTypeColor(), null, strike);
                drawString(g, " ", strike); // NOI18N
            }
            if ((modifiers & CsmUtilities.LOCAL_MEMBER_BIT) != 0){
                // it is local field, draw as bold
                drawString(g, fldName, getNameColor(), new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), strike);
            }else{
                drawString(g, fldName, getNameColor() , null, strike);
            }
            if (!drawTypeAsPrefix) {
                drawTypeName(g, getTypeName(), getTypeColor());
            }
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            if (drawTypeAsPrefix) {
                buf.append(typeName);
                buf.append(' ');
            }
            buf.append(fldName);
            if (!drawTypeAsPrefix) {
                buf.append(' ');
                buf.append(typeName);
            }
            return buf.toString();
        }
    }

    public static class LocalVariablePaintComponent extends FieldPaintComponent {
        private Color VARIABLE_COLOR = LFCustoms.shiftColor(new Color(255,64,64));

        public LocalVariablePaintComponent(){
            super();
            this.modifiers |= CsmUtilities.LOCAL_MEMBER_BIT | this.modifiers;
        }

        @Override
        public Color getNameColor() {
            return VARIABLE_COLOR;
        }

        @Override
        public void setModifiers(int modifiers) {
            super.setModifiers(modifiers | CsmUtilities.LOCAL_MEMBER_BIT);
        }
    }

    public static class FileLocalVariablePaintComponent extends FieldPaintComponent {
        private Color VARIABLE_COLOR = LFCustoms.shiftColor(new Color(64,64,255));

        public FileLocalVariablePaintComponent(){
            super();
        }

        @Override
        public Color getNameColor() {
            return VARIABLE_COLOR;
        }
    }

    public static class GlobalVariablePaintComponent extends FieldPaintComponent {
        private Color VARIABLE_COLOR = LFCustoms.shiftColor(new Color(64,64,255));

        public GlobalVariablePaintComponent(){
            super();
        }

        @Override
        public Color getNameColor() {
            return VARIABLE_COLOR;
        }
    }

    public static class MacroPaintComponent extends CsmPaintComponent{
        private Color MACRO_NAME_COLOR = new Color(46,146,199);//CsmFontColorManager.instance().getColor(FontColorProvider.Entity.DEFINED_MACRO);//new Color(64,255,64).darker().darker();
        private Color MACRO_PARAMETER_NAME_COLOR = LFCustoms.shiftColor(new Color(227, 166, 74));//Color.magenta.darker();
        private List<CharSequence> params = null;
        private String name;

        public MacroPaintComponent(){
            super();
        }

        @Override
        public String getName(){
            return name;
        }

        @Override
        public void setName(String name){
            this.name = name;
        }

        public void setParams(List<CharSequence> params){
            this.params = params;
        }

        protected List<CharSequence> getParamList(){
            return params;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, getName(), MACRO_NAME_COLOR, null, strike);
            drawParameterList(g, getParamList(), strike);
        }

        protected void drawParameterList(Graphics g, List<CharSequence> prmList, boolean strike) {
            if (prmList == null || prmList.size()==0){
                return;
            }
            drawString(g, "(", strike); // NOI18N
            for (Iterator<CharSequence> it = prmList.iterator(); it.hasNext();) {
                drawString(g, it.next(), MACRO_PARAMETER_NAME_COLOR, null, strike);
                if (it.hasNext()) {
                    drawString(g, ", ", strike); // NOI18N
                }
            }
            drawString(g, ")", strike); // NOI18N
        }

        protected String toStringParameterList(List<CharSequence> prmList) {
            if (prmList == null || prmList.size()==0){
                return "";
            }
            StringBuilder buf = new StringBuilder();
            buf.append('('); // NOI18N
            for (Iterator<CharSequence> it = prmList.iterator(); it.hasNext();) {
                buf.append(it.next());
                if (it.hasNext()) {
                    buf.append(", "); // NOI18N
                }
            }
            buf.append(')'); // NOI18N
            return buf.toString();
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            //macro name
            buf.append(getName());
            //macro params
            buf.append(toStringParameterList(getParamList()));
            return buf.toString();
        }
    }

    public static class TemplateParameterPaintComponent extends CsmPaintComponent{
        private Color PARAMETER_NAME_COLOR = LFCustoms.getTextFgColor();
        private List<CharSequence> params = null;
        private String name;

        public TemplateParameterPaintComponent(){
            super();
        }

        @Override
        public String getName(){
            return name;
        }

        @Override
        public void setName(String name){
            this.name = name;
        }

        public void setParams(List<CharSequence> params){
            this.params = params;
        }

        protected List<CharSequence> getParamList(){
            return params;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, getName(), PARAMETER_NAME_COLOR, null, strike);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            //macro name
            buf.append(getName());
            return buf.toString();
        }
    }

    public static class LabelPaintComponent extends  CsmPaintComponent {
        private Color LABEL_NAME_COLOR = LFCustoms.getTextFgColor();
        private List<CharSequence> params = null;
        private String name;

        public LabelPaintComponent(){
            super();
        }

        @Override
        public String getName(){
            return name;
        }

        @Override
        public void setName(String name){
            this.name = name;
        }

        public void setParams(List<CharSequence> params){
            this.params = params;
        }

        protected List<CharSequence> getParamList(){
            return params;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            drawIcon(g, getIcon());
            drawString(g, getName(), LABEL_NAME_COLOR, null, strike);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            //macro name
            buf.append(getName());
            return buf.toString();
        }
    }

    public static class ConstructorPaintComponent extends CsmPaintComponent{

        private Color CONSTRUCTOR_COLOR = LFCustoms.shiftColor(Color.orange);
        private Color PARAMETER_NAME_COLOR = LFCustoms.shiftColor(new Color(227, 166, 74));//Color.magenta.darker();
        private List<ParamStr> params = new ArrayList<>();
        private List<ExceptionStr> excs = new ArrayList<>();
        private String name;

        public ConstructorPaintComponent(){
            super();
        }

        public int getMethodModifiers(){
            return modifiers;
        }

        @Override
        public String getName(){
            return name;
        }

        @Override
        public void setName(String name){
            this.name = name;
        }

        public void setParams(List<ParamStr> params){
            this.params = params;
        }

        public void setExceptions(List<ExceptionStr> excs){
            this.excs = excs;
        }

        protected List<ParamStr> getParamList(){
            return params;
        }

        protected List<ExceptionStr> getExceptionList(){
            return excs;
        }

        protected void drawParameter(Graphics g, ParamStr prm) {
            drawParameter(g, prm, false);
        }

        protected void drawParameter(Graphics g, ParamStr prm, boolean strike) {
            String parmName = prm.getName();
            String simpleTypeName = prm.getSimpleTypeName();
            int indexOfSqr = simpleTypeName.indexOf('['); // NOI18N
            if(indexOfSqr == -1) {
                drawString(g, simpleTypeName, prm.getTypeColor(), null, strike);
                if (parmName != null && parmName.length() > 0) {
                    drawString(g, " ", strike); // NOI18N
                    drawString(g, parmName, PARAMETER_NAME_COLOR, null, strike);
                }
            } else {
                drawString(g, simpleTypeName.substring(0, indexOfSqr), prm.getTypeColor(), null, strike);
                if (parmName != null && parmName.length() > 0) {
                    drawString(g, " ", strike); // NOI18N
                    drawString(g, parmName, PARAMETER_NAME_COLOR, null, strike);
                }
                drawString(g, simpleTypeName.substring(indexOfSqr), prm.getTypeColor(), null, strike);
            }
        }

        protected void drawParameterList(Graphics g, List<ParamStr> prmList) {
            drawParameterList(g, prmList, false);
        }

        protected void drawParameterList(Graphics g, List<ParamStr> prmList, boolean strike) {
            drawString(g, "(", strike); // NOI18N
            for (Iterator<ParamStr> it = prmList.iterator(); it.hasNext();) {
                drawParameter(g, it.next(), strike);
                if (it.hasNext()) {
                    drawString(g, ", ", strike); // NOI18N
                }
            }
            drawString(g, ")", strike); // NOI18N
        }

        protected void drawExceptions(Graphics g, List<ExceptionStr> exc, boolean strike) {
            if (exc.size() > 0) {
                drawString(g, THROWS, LFCustoms.shiftColor(KEYWORD_COLOR), null, strike);
                for (Iterator<ExceptionStr> it = exc.iterator(); it.hasNext();) {
                    ExceptionStr ex = it.next();
                    drawString(g, ex.getName(), ex.getTypeColor(), null, strike);
                    if (it.hasNext()) {
                        drawString(g, ", ", strike); // NOI18N
                    }

                }
            }
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            //int level = CsmUtilities.getLevel(getModifiers());
            drawIcon(g, getIcon());
            drawString(g, getName(), CONSTRUCTOR_COLOR, null, strike);
            drawParameterList(g, getParamList(), strike);
            drawExceptions(g, getExceptionList(), strike);
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            //constructor name
            buf.append(getName());
            //constructor params
            buf.append(toStringParameterList(getParamList()));
            //constructor exceptions
            buf.append(toStringExceptions(getExceptionList()));
            return buf.toString();
        }

        protected String toStringParameter(ParamStr prm) {
            StringBuilder builder = new StringBuilder();
            String parmName = prm.getName();
            String simpleTypeName = prm.getSimpleTypeName();
            int indexOfSqr = simpleTypeName.indexOf('['); // NOI18N
            if(indexOfSqr == -1) {
                builder.append(simpleTypeName);
                if (parmName != null && parmName.length() > 0) {
                    builder.append(" ").append(parmName); // NOI18N
                }
            } else {
                builder.append(simpleTypeName.substring(0, indexOfSqr));
                if (parmName != null && parmName.length() > 0) {
                    builder.append(" ").append(parmName); // NOI18N
                }
                builder.append(simpleTypeName.substring(indexOfSqr));
            }
            return builder.toString();
        }

        protected String toStringParameterList(List<ParamStr> prmList) {
            StringBuilder buf = new StringBuilder();
            buf.append('('); // NOI18N
            for (Iterator<ParamStr> it = prmList.iterator(); it.hasNext();) {
                buf.append(toStringParameter(it.next()));
                if (it.hasNext()) {
                    buf.append(", "); // NOI18N
                }
            }
            buf.append(')'); // NOI18N
            return buf.toString();
        }

        protected String toStringExceptions(List<ExceptionStr> exc) {
            StringBuilder buf = new StringBuilder();
            if (exc.size() > 0) {
                buf.append(THROWS);
                for (Iterator<ExceptionStr> it = exc.iterator(); it.hasNext();) {
                    ExceptionStr ex = it.next();
                    buf.append(ex.getName());
                    if (it.hasNext()) {
                        buf.append(", "); // NOI18N
                    }

                }
            }
            return buf.toString();
        }
    }

    public static class MethodPaintComponent extends ConstructorPaintComponent {

        //private Color PARAMETER_NAME_COLOR = Color.magenta.darker();
        private Color METHOD_COLOR = LFCustoms.getTextFgColor();
        private String typeName;
        private Color typeColor;
        private boolean drawTypeAsPrefix = false;

        public MethodPaintComponent(){
            super();
        }

        public Color getNameColor() {
            return METHOD_COLOR;
        }

        public String getTypeName(){
            return typeName;
        }

        public Color getTypeColor(){
            return typeColor;
        }

        public void setTypeName(String typeName){
            this.typeName = typeName;
        }

        public void setTypeColor(Color typeColor){
            this.typeColor = typeColor;
        }

        public void setDrawTypeAsPrefix(boolean asPrefix) {
            this.drawTypeAsPrefix = asPrefix;
        }

        @Override
        protected void draw(Graphics g){
            // IMPORTANT:
            // when updated => have to update toString!
            boolean strike = false;
            //int level = CsmUtilities.getLevel(getModifiers());
            drawIcon(g, getIcon());

            if (drawTypeAsPrefix) {
                drawString(g, getTypeName(), getTypeColor(), null, strike);
                drawString(g, " ", strike); // NOI18N
            }
            if ((getModifiers() & CsmUtilities.LOCAL_MEMBER_BIT) != 0){
                drawString(g, getName(), getNameColor() , new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), strike);
            }else{
                drawString(g, getName(), getNameColor(), null, strike);
            }
            drawParameterList(g, getParamList(), strike);
//            drawExceptions(g, getExceptionList(), strike);
            if (!drawTypeAsPrefix) {
                drawTypeName(g, getTypeName(), getTypeColor());
            }
        }

        /**
         * returns string representation of paint item
         * IMPORTANT: have to be in sync with draw() method
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            if (drawTypeAsPrefix) {
                //return type
                buf.append(getTypeName());
                buf.append(' '); // NOI18N
            }
            //method name
            buf.append(getName());
            //method params
            buf.append(toStringParameterList(getParamList()));
            //method exceptions
//            buf.append(toStringExceptions(getExceptionList()));
            if (!drawTypeAsPrefix) {
                //return type
                buf.append(' '); // NOI18N
                buf.append(getTypeName());
            }
            return buf.toString();
        }
    }

    public static class FileLocalFunctionPaintComponent extends MethodPaintComponent {
        private Color FUN_COLOR = LFCustoms.getTextFgColor();

        public FileLocalFunctionPaintComponent(){
            super();
        }

        @Override
        public Color getNameColor() {
            return FUN_COLOR;
        }
    }

    public static class GlobalFunctionPaintComponent extends MethodPaintComponent {
        private Color FUN_COLOR = LFCustoms.getTextFgColor();

        public GlobalFunctionPaintComponent(){
            super();
        }

        @Override
        public Color getNameColor() {
            return FUN_COLOR;
        }
    }

    public static class CsmPaintComponentWrapper extends CsmPaintComponent {
        private CsmPaintComponent comp;
        private boolean drawTypeAsPrefix = true;

        public CsmPaintComponentWrapper() {
            super();
        }

        public void setCsmComponent(CsmPaintComponent comp) {
            this.comp = comp;
        }

        @Override
        protected void draw(Graphics g) {
            if (comp != null) {
                if (drawTypeAsPrefix) {
                    if (comp instanceof CsmPaintComponent.FieldPaintComponent) {
                        ((CsmPaintComponent.FieldPaintComponent)comp).setDrawTypeAsPrefix(true);
                    } else if (comp instanceof CsmPaintComponent.MethodPaintComponent) {
                        ((CsmPaintComponent.MethodPaintComponent)comp).setDrawTypeAsPrefix(true);
                    }
                }
                comp.draw(g);
                if (drawTypeAsPrefix) {
                    if (comp instanceof CsmPaintComponent.FieldPaintComponent) {
                        ((CsmPaintComponent.FieldPaintComponent)comp).setDrawTypeAsPrefix(false);
                    } else if (comp instanceof CsmPaintComponent.MethodPaintComponent) {
                        ((CsmPaintComponent.MethodPaintComponent)comp).setDrawTypeAsPrefix(false);
                    }
                }
                setDrawState(comp.getDrawState());
            }
        }

        @Override
        public void setFont(Font font) {
            super.setFont(font);
            if (comp != null) {
                comp.setFont(font);
            }
        }

        @Override
        public String toString() {
            if (comp != null) {
                return comp.toString();
            }
            return "";
        }
    }

    public static class StringPaintComponent extends CsmPaintComponent {

        private String str;

        public void setString(String str){
            this.str = str;
        }

        @Override
        protected void draw(Graphics g){
            drawIcon(g, null);
            drawString(g, str, LFCustoms.getTextFgColor());
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
