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
package org.netbeans.modules.java.lsp.server.protocol;

import java.net.URI;
import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 *
 * @author Martin Entlicher
 */
public final class ThemableDecorationAttachmentRenderOptions {

    private Either<String, ThemeColor> backgroundColor;
    private String border;
    private Either<String, ThemeColor> borderColor;
    private Either<String, ThemeColor> color;
    private Either<String, URI> contentIconPath;
    private String contentText;
    private String fontStyle;
    private String fontWeight;
    private String height;
    private String margin;
    private String textDecoration;
    private String width;

    public Either<String, ThemeColor> getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Either<String, ThemeColor> backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBorder() {
        return border;
    }

    public void setBorder(String border) {
        this.border = border;
    }

    public Either<String, ThemeColor> getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Either<String, ThemeColor> borderColor) {
        this.borderColor = borderColor;
    }

    public Either<String, ThemeColor> getColor() {
        return color;
    }

    public void setColor(Either<String, ThemeColor> color) {
        this.color = color;
    }

    public Either<String, URI> getContentIconPath() {
        return contentIconPath;
    }

    public void setContentIconPath(Either<String, URI> contentIconPath) {
        this.contentIconPath = contentIconPath;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getMargin() {
        return margin;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

    public String getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(String textDecoration) {
        this.textDecoration = textDecoration;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("backgroundColor", backgroundColor);
        b.add("border", border);
        b.add("borderColor", borderColor);
        b.add("color", color);
        b.add("contentIconPath", contentIconPath);
        b.add("contentText", contentText);
        b.add("fontStyle", fontStyle);
        b.add("fontWeight", fontWeight);
        b.add("height", height);
        b.add("margin", margin);
        b.add("textDecoration", textDecoration);
        b.add("width", width);
        return b.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.backgroundColor);
        hash = 79 * hash + Objects.hashCode(this.border);
        hash = 79 * hash + Objects.hashCode(this.borderColor);
        hash = 79 * hash + Objects.hashCode(this.color);
        hash = 79 * hash + Objects.hashCode(this.contentIconPath);
        hash = 79 * hash + Objects.hashCode(this.contentText);
        hash = 79 * hash + Objects.hashCode(this.fontStyle);
        hash = 79 * hash + Objects.hashCode(this.fontWeight);
        hash = 79 * hash + Objects.hashCode(this.height);
        hash = 79 * hash + Objects.hashCode(this.margin);
        hash = 79 * hash + Objects.hashCode(this.textDecoration);
        hash = 79 * hash + Objects.hashCode(this.width);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ThemableDecorationAttachmentRenderOptions other = (ThemableDecorationAttachmentRenderOptions) obj;
        if (!Objects.equals(this.border, other.border)) {
            return false;
        }
        if (!Objects.equals(this.contentText, other.contentText)) {
            return false;
        }
        if (!Objects.equals(this.fontStyle, other.fontStyle)) {
            return false;
        }
        if (!Objects.equals(this.fontWeight, other.fontWeight)) {
            return false;
        }
        if (!Objects.equals(this.height, other.height)) {
            return false;
        }
        if (!Objects.equals(this.margin, other.margin)) {
            return false;
        }
        if (!Objects.equals(this.textDecoration, other.textDecoration)) {
            return false;
        }
        if (!Objects.equals(this.width, other.width)) {
            return false;
        }
        if (!Objects.equals(this.backgroundColor, other.backgroundColor)) {
            return false;
        }
        if (!Objects.equals(this.borderColor, other.borderColor)) {
            return false;
        }
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (!Objects.equals(this.contentIconPath, other.contentIconPath)) {
            return false;
        }
        return true;
    }

    
}
