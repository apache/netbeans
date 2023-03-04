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

import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 *
 * @author Martin Entlicher
 */
public final class DecorationRenderOptions {

    private ThemableDecorationAttachmentRenderOptions after;
    private Either<String, ThemeColor> backgroundColor;
    private ThemableDecorationAttachmentRenderOptions before;
    private String border;
    private Either<String, ThemeColor> borderColor;
    private String borderRadius;
    private String borderSpacing;
    private String borderStyle;
    private String borderWidth;
    private Either<String, ThemeColor> color;
    private String cursor;
    private ThemableDecorationRenderOptions dark;
    private String fontStyle;
    private String fontWeight;
    private Either<String, URI> gutterIconPath;
    private String gutterIconSize;
    private boolean isWholeLine;
    private String letterSpacing;
    private ThemableDecorationRenderOptions light;
    private String opacity;
    private String outline;
    private Either<String, ThemeColor> outlineColor;
    private String outlineStyle;
    private String outlineWidth;
    private Either<String, ThemeColor> overviewRulerColor;
    private OverviewRulerLane overviewRulerLane;
    //private DecorationRangeBehavior rangeBehavior;
    private String textDecoration;

    @Pure
    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("after", after);
        b.add("backgroundColor", backgroundColor);
        b.add("before", before);
        b.add("border", border);
        b.add("borderColor", borderColor);
        b.add("borderRadius", borderRadius);
        b.add("borderSpacing", borderSpacing);
        b.add("borderStyle", borderStyle);
        b.add("borderWidth", borderWidth);
        b.add("color", color);
        b.add("cursor", cursor);
        b.add("dark", dark);
        b.add("fontStyle", fontStyle);
        b.add("fontWeight", fontWeight);
        b.add("gutterIconPath", gutterIconPath);
        b.add("gutterIconSize", gutterIconSize);
        b.add("isWholeLine", isWholeLine);
        b.add("letterSpacing", letterSpacing);
        b.add("light", light);
        b.add("opacity", opacity);
        b.add("outline", outline);
        b.add("outlineColor", outlineColor);
        b.add("outlineStyle", outlineStyle);
        b.add("outlineWidth", outlineWidth);
        b.add("overviewRulerColor", overviewRulerColor);
        b.add("overviewRulerLane", overviewRulerLane);
        b.add("textDecoration", textDecoration);
        return b.toString();
    }

    public ThemableDecorationAttachmentRenderOptions getAfter() {
        return after;
    }

    public void setAfter(ThemableDecorationAttachmentRenderOptions after) {
        this.after = after;
    }

    public Either<String, ThemeColor> getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Either<String, ThemeColor> backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public ThemableDecorationAttachmentRenderOptions getBefore() {
        return before;
    }

    public void setBefore(ThemableDecorationAttachmentRenderOptions before) {
        this.before = before;
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

    public String getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(String borderRadius) {
        this.borderRadius = borderRadius;
    }

    public String getBorderSpacing() {
        return borderSpacing;
    }

    public void setBorderSpacing(String borderSpacing) {
        this.borderSpacing = borderSpacing;
    }

    public String getBorderStyle() {
        return borderStyle;
    }

    public void setBorderStyle(String borderStyle) {
        this.borderStyle = borderStyle;
    }

    public String getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(String borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Either<String, ThemeColor> getColor() {
        return color;
    }

    public void setColor(Either<String, ThemeColor> color) {
        this.color = color;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public ThemableDecorationRenderOptions getDark() {
        return dark;
    }

    public void setDark(ThemableDecorationRenderOptions dark) {
        this.dark = dark;
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

    public Either<String, URI> getGutterIconPath() {
        return gutterIconPath;
    }

    public void setGutterIconPath(Either<String, URI> gutterIconPath) {
        this.gutterIconPath = gutterIconPath;
    }

    public String getGutterIconSize() {
        return gutterIconSize;
    }

    public void setGutterIconSize(String gutterIconSize) {
        this.gutterIconSize = gutterIconSize;
    }

    public boolean isIsWholeLine() {
        return isWholeLine;
    }

    public void setIsWholeLine(boolean isWholeLine) {
        this.isWholeLine = isWholeLine;
    }

    public String getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(String letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    public ThemableDecorationRenderOptions getLight() {
        return light;
    }

    public void setLight(ThemableDecorationRenderOptions light) {
        this.light = light;
    }

    public String getOpacity() {
        return opacity;
    }

    public void setOpacity(String opacity) {
        this.opacity = opacity;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public Either<String, ThemeColor> getOutlineColor() {
        return outlineColor;
    }

    public void setOutlineColor(Either<String, ThemeColor> outlineColor) {
        this.outlineColor = outlineColor;
    }

    public String getOutlineStyle() {
        return outlineStyle;
    }

    public void setOutlineStyle(String outlineStyle) {
        this.outlineStyle = outlineStyle;
    }

    public String getOutlineWidth() {
        return outlineWidth;
    }

    public void setOutlineWidth(String outlineWidth) {
        this.outlineWidth = outlineWidth;
    }

    public Either<String, ThemeColor> getOverviewRulerColor() {
        return overviewRulerColor;
    }

    public void setOverviewRulerColor(Either<String, ThemeColor> overviewRulerColor) {
        this.overviewRulerColor = overviewRulerColor;
    }

    public OverviewRulerLane getOverviewRulerLane() {
        return overviewRulerLane;
    }

    public void setOverviewRulerLane(OverviewRulerLane overviewRulerLane) {
        this.overviewRulerLane = overviewRulerLane;
    }

    public String getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(String textDecoration) {
        this.textDecoration = textDecoration;
    }

    
}
