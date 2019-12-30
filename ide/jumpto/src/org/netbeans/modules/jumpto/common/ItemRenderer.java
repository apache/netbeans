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
package org.netbeans.modules.jumpto.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.swing.ButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class ItemRenderer<T> extends DefaultListCellRenderer implements ChangeListener {

    public static interface Convertor<T> {
        String getName(T item);
        String getHighlightText(T item);
        String getOwnerName(T item);
        String getProjectName(T item);
        String getFilePath(T item);
        Icon getItemIcon(T item);
        Icon getProjectIcon(T item);
        boolean isFromCurrentProject(T item);
    }

    public static final class Builder<T> {
        private final JList list;
        private final ButtonModel caseSensitive;
        private final Convertor<T> convertor;
        private String separatorPattern;
        private ButtonModel colorPrefered;

        private Builder(
            @NonNull final JList<T> list,
            @NonNull final ButtonModel caseSensitive,
            @NonNull final Convertor<T> convertor) {
            this.list = list;
            this.caseSensitive = caseSensitive;
            this.convertor = convertor;
        }

        @NonNull
        public ItemRenderer<T> build() {
            return new ItemRenderer<T>(
                    list,
                    caseSensitive,
                    colorPrefered,
                    convertor,
                    separatorPattern);
        }

        @NonNull
        public Builder setCamelCaseSeparator(@NullAllowed final String separatorPattern) {
            this.separatorPattern = separatorPattern;
            return this;
        }

        @NonNull
        public Builder setColorPreferedProject(@NullAllowed final ButtonModel colorPrefered) {
            this.colorPrefered = colorPrefered;
            return this;
        }

        @NonNull
        public static <T> Builder<T> create(
            @NonNull final JList<T> list,
            @NonNull final ButtonModel caseSensitive,
            @NonNull final Convertor<T> convertor) {
            return new Builder<T>(list, caseSensitive, convertor);
        }
    }

    @StaticResource
    private static final String SAMPLE_ITEM_ICON = "org/netbeans/modules/jumpto/type/sample.png";
    private static final int DARKER_COLOR_COMPONENT = 15;
    private static final int LIGHTER_COLOR_COMPONENT = 80;
    private final GoToSettings.HighlightingMode highlightMode;
    private final HighlightingNameFormatter nameFormater;
    private final String mainProjectName = EntityComparator.getMainProjectName();
    private final Convertor<T> convertor;
    private final MyPanel<T> rendererComponent;
    private final JLabel jlName;
    private final JLabel jlOwner = new JLabel();
    private final JLabel jlPrj = new JLabel();
    private final Color fgColor;
    private final Color fgColorLighter;
    private final Color bgColor;
    private final Color bgColorDarker;
    private final Color bgSelectionColor;
    private final Color fgSelectionColor;
    private final Color bgColorGreener;
    private final Color bgColorDarkerGreener;
    private final JList jList;
    private final ButtonModel caseSensitive;
    private final ButtonModel colorPrefered;

    private Class<T> clzCache;

    private ItemRenderer(
            @NonNull final JList<T> list,
            @NonNull final ButtonModel caseSensitive,
            @NullAllowed final ButtonModel colorPrefered,
            @NonNull final Convertor<T> convertor,
            @NullAllowed final String separatorPattern) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        jList = list;
        this.caseSensitive = caseSensitive;
        this.colorPrefered = colorPrefered;
        this.convertor = convertor;
        final GoToSettings hs = GoToSettings.getDefault();
        highlightMode = hs.getHighlightingMode();
        nameFormater = createNameFormatter(hs.getHighlightingType(), separatorPattern);
        this.jlName = new JLabel();
        resetNameLabel();
        Container container = list.getParent();
        if ( container instanceof JViewport ) {
            ((JViewport)container).addChangeListener(this);
            stateChanged(new ChangeEvent(container));
        }
        rendererComponent = new MyPanel<>(convertor);
        rendererComponent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets (0,0,0,7);
        rendererComponent.add( jlName, c);
        jlOwner.setOpaque(false);
        jlOwner.setFont(list.getFont());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets (0,0,0,7);
        rendererComponent.add( jlOwner, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        rendererComponent.add( jlPrj, c);


        jlPrj.setOpaque(false);
        jlPrj.setFont(list.getFont());


        jlPrj.setHorizontalAlignment(RIGHT);
        jlPrj.setHorizontalTextPosition(LEFT);

        // setFont( list.getFont() );
        fgColor = list.getForeground();
        fgColorLighter = new Color(
                               Math.min( 255, fgColor.getRed() + LIGHTER_COLOR_COMPONENT),
                               Math.min( 255, fgColor.getGreen() + LIGHTER_COLOR_COMPONENT),
                               Math.min( 255, fgColor.getBlue() + LIGHTER_COLOR_COMPONENT)
                              );

        bgColor = new Color( list.getBackground().getRGB() );
        bgColorDarker = new Color(
                                Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                        );
        bgSelectionColor = list.getSelectionBackground();
        fgSelectionColor = list.getSelectionForeground();
        bgColorGreener = new Color(
                                    Math.abs(bgColor.getRed() - 20),
                                    Math.min(255, bgColor.getGreen() + 10 ),
                                    Math.abs(bgColor.getBlue() - 20) );


        bgColorDarkerGreener = new Color(
                                Math.abs(bgColorDarker.getRed() - 35),
                                Math.min(255, bgColorDarker.getGreen() + 5 ),
                                Math.abs(bgColorDarker.getBlue() - 35) );
    }

    @NonNull
    @Override
    public Component getListCellRendererComponent(
            @NonNull final JList list,
            @NullAllowed final Object value,
            final int index,
            final boolean isSelected,
            final boolean hasFocus) {

            int height = list.getFixedCellHeight();
            int width = list.getFixedCellWidth() - 1;
            width = width < 200 ? 200 : width;

            Dimension size = new Dimension( width, height );
            rendererComponent.setMaximumSize(size);
            rendererComponent.setPreferredSize(size);
            resetNameLabel();
            if ( isSelected ) {
                jlName.setForeground(fgSelectionColor);
                jlOwner.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);
                rendererComponent.setBackground(bgSelectionColor);
            } else {
                jlName.setForeground(fgColor);
                jlOwner.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }

            final T item = dynamic_cast(value);
            if (item != null) {
                jlName.setIcon(convertor.getItemIcon(item));
                final String formattedName;
                if (shouldHighlight(isSelected)) {
                    formattedName = highlight(
                            convertor.getName(item),
                            convertor.getHighlightText(item),
                            caseSensitive.isSelected(),
                            isSelected? fgSelectionColor : fgColor);
                } else {
                    formattedName = convertor.getName(item);
                }
                jlName.setText(formattedName);
                jlOwner.setText(convertor.getOwnerName(item));
                setProjectName(jlPrj, convertor.getProjectName(item));
                jlPrj.setIcon(convertor.getProjectIcon(item));
                if (!isSelected) {
                    final boolean cprj = colorPrefered != null &&
                            colorPrefered.isSelected() &&
                            convertor.isFromCurrentProject(item);
                    final Color bgc =  index % 2 == 0 ?
                        (cprj ? bgColorGreener : bgColor ) :
                        (cprj ? bgColorDarkerGreener : bgColorDarker );
                    jlName.setBackground(bgc);  //Html does not support transparent bg
                    rendererComponent.setBackground(bgc);
                }
		rendererComponent.setItem(item);
            } else {
                jlName.setText(String.valueOf(value));
            }
            return rendererComponent;
        }

    @Override
    public void stateChanged(@NonNull final ChangeEvent event) {
        final JViewport jv = (JViewport)event.getSource();
        jlName.setText( "Sample" ); // NOI18N
        jlName.setIcon(ImageUtilities.loadImageIcon(SAMPLE_ITEM_ICON, false));
        jList.setFixedCellHeight(jlName.getPreferredSize().height);
        jList.setFixedCellWidth(jv.getExtentSize().width);
    }

    @CheckForNull
    @SuppressWarnings("unchecked")
    private T dynamic_cast (
            @NullAllowed final Object obj) {
        if (clzCache == null) {
            for (Type type : convertor.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType && Convertor.class == ((ParameterizedType)type).getRawType()) {
                    clzCache = (Class<T>) ((ParameterizedType)type).getActualTypeArguments()[0];
                    break;
                }
            }
        }
        return clzCache != null && clzCache.isInstance(obj) ?
            clzCache.cast(obj) :
            null;
    }

    private void setProjectName(JLabel jlPrj, String projectName) {
        if(isMainProject(projectName)) {
            jlPrj.setText(getBoldText(projectName));
        } else {
            jlPrj.setText(projectName);
        }
    }

    private String getBoldText(String text) {
        StringBuilder sb = new StringBuilder("<html><b>"); // NOI18N
        sb.append(text);
        sb.append("</b></html>"); // NOI18N
        return sb.toString();
    }

    private boolean isMainProject(String projectName) {
        return projectName != null && projectName.equals(mainProjectName) ?
            true : false;
    }

    private static class MyPanel<T> extends JPanel {

        private final Convertor<T> convertor;
	private T item;

        MyPanel(@NonNull final Convertor<T> convertor) {
            this.convertor = convertor;
        }

	void setItem(final  T item) {
	    this.item = item;
	    // since the same component is reused for dirrerent list itens,
	    // null the tool tip
	    putClientProperty(TOOL_TIP_TEXT_KEY, null);
	}

	@Override
	public String getToolTipText() {
	    // the tool tip is gotten from the descriptor
	    // and cached in the standard TOOL_TIP_TEXT_KEY property
	    String text = (String) getClientProperty(TOOL_TIP_TEXT_KEY);
	    if( text == null ) {
                if(this.item != null) {
                    text = convertor.getFilePath(item);
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }

    private void resetNameLabel() {
        final Font font = jList.getFont();
        jlName.setFont(font);
        jlName.setOpaque(false);
    }
    
    private boolean shouldHighlight(final boolean selectedItem) {
        switch (highlightMode) {
            case NONE:
                return false;
            case ACTIVE:
                return selectedItem;
            case ALL:
                return true;
            default:
                throw new IllegalArgumentException(String.valueOf(selectedItem));
        }
    }
    
    @NonNull
    private String highlight(
            @NonNull final String name,
            @NonNull final String highlightText,
            final boolean caseSensitive,
            @NonNull final Color color) {
        return new StringBuilder("<html><nobr>").   //NOI18N
                append(nameFormater.formatName(
                        UiUtils.htmlize(name),
                        UiUtils.htmlize(highlightText),
                        caseSensitive,
                        color)).
                append("</nobr>").  //NOI18N
                toString();
    }
    
    private static HighlightingNameFormatter createNameFormatter(
            @NonNull final GoToSettings.HighlightingType type,
            @NonNull final String separatorPattern) {
        switch (type) {
            case BACKGROUND:
                Color back = new Color(236,235,163);
                Color front = Color.BLACK;
                final FontColorSettings colors = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
                if (colors != null) {
                    final AttributeSet attrs = colors.getFontColors("mark-occurrences");  //NOI18N
                    if (attrs != null) {
                        Object o = attrs.getAttribute(StyleConstants.Background);
                        if (o instanceof Color) {
                            back = (Color) o;
                        }
                        o = attrs.getAttribute(StyleConstants.Foreground);
                        if (o instanceof Color) {
                            front = (Color) o;
                        }
                    }
                }
                return HighlightingNameFormatter.Builder.create().
                        setCamelCaseSeparator(separatorPattern).
                        buildColorFormatter(back, front);
            case BOLD:
                return HighlightingNameFormatter.Builder.create().
                    setCamelCaseSeparator(separatorPattern).
                    buildBoldFormatter();
            default:
                throw new IllegalStateException(String.valueOf(type));
        }
    }
}
