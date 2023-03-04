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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Ajit Bhate
 * @author anjeleevich
 */
public class ImageLabelWidget extends Widget {
    
    private ImageWidget imageWidget;
    private LabelWidget labelWidget;
    private LabelWidget commentWidget;
    private TextFieldInplaceEditor editor = null;
    
    public ImageLabelWidget(Scene scene, Image image, String text) {
        this(scene, image, text, null, DEFAULT_GAP);
    }
    
    
    public ImageLabelWidget(Scene scene, Image image, String text,
            int hgap) {
        this(scene, image, text, null, hgap);
    }
    
    
    public ImageLabelWidget(Scene scene, Image image,
            String text, String comment) {
        this(scene, image, text, comment, DEFAULT_GAP);
    }
    
    
    public ImageLabelWidget(Scene scene, Image image,
            String label,
            String comment, int hgap) {
        super(scene);
        
        setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, hgap));
        
        setImage(image);
        setLabel(label);
        setComment(comment);
    }
    
    public final void setLabel(String label) {
        if(labelWidget==null) {
            labelWidget = new LabelWidget(getScene(),label);
            labelWidget.setUseGlyphVector(true);
            labelWidget.setAlignment(LabelWidget.Alignment.CENTER);
            addChild(imageWidget==null||getChildren().isEmpty()?0:1,labelWidget,1);
        } else {
            labelWidget.setLabel(label);
        }
        labelWidget.setVisible(label!=null);
    }
    
    public final void setImage(Image image) {
        if(imageWidget==null) {
            imageWidget = new ImageWidget(getScene(),image);
            addChild(0,imageWidget,1);
        } else {
            imageWidget.setImage(image);
            imageWidget.removeChildren();
        }
        imageWidget.setVisible(image!=null);
    }
    
    public final void setImage(Widget widget) {
        if(imageWidget==null) {
            imageWidget = new ImageWidget(getScene(),null);
            addChild(0,imageWidget,1);
        } else {
            imageWidget.setImage(null);
            imageWidget.removeChildren();
        }
        if(widget!=null) {
            imageWidget.addChild(widget);
        }
        imageWidget.setVisible(widget!=null);
    }
    
    public final void setComment(String comment) {
        if(commentWidget==null) {
            commentWidget = new LabelWidget(getScene(),comment);
            addChild(commentWidget,1);
            commentWidget.setPaintAsDisabled(true);
        } else {
            commentWidget.setLabel(comment);
        }
        commentWidget.setVisible(comment!=null);
    }
    
    public final String getLabel() {
        return labelWidget==null?null:labelWidget.getLabel();
    }
    
    public final Image getImage() {
        return imageWidget==null?null:imageWidget.getImage();
    }
    
    protected final Widget getImageWidget() {
        return imageWidget==null||imageWidget.getChildren().isEmpty()?null:
            imageWidget.getChildren().get(0);
    }
    
    public final String getComment() {
        return commentWidget==null?null:commentWidget.getLabel();
    }
    
    public final boolean isPaintAsDisabled() {
        if(labelWidget!=null) {
            return labelWidget.isPaintAsDisabled();
        }
        if(imageWidget!=null) {
            return imageWidget.isPaintAsDisabled();
        }
        if(commentWidget!=null) {
            return commentWidget.isPaintAsDisabled();
        }
        return false;
    }
    
    public final void setPaintAsDisabled(boolean flag) {
        if(labelWidget!=null) {
            labelWidget.setPaintAsDisabled(flag);
        }
        if(imageWidget!=null) {
            imageWidget.setPaintAsDisabled(flag);
        }
        if(commentWidget!=null) {
            commentWidget.setPaintAsDisabled(flag);
        }
    }
    
    public final void setLabelForeground(Color forground) {
        if(labelWidget!=null) {
            labelWidget.setForeground(forground);
        }
        if(commentWidget!=null) {
            commentWidget.setForeground(forground);
        }
    }
    
    public final void setLabelFont(Font font) {
        labelWidget.setFont(font);
    }
    
    public final void setLabelEditor(TextFieldInplaceEditor editor) {
        if (this.editor!=null) {
            throw new IllegalStateException("An editor is already specified.");
        }
        this.editor = editor;
        getActions().addAction(ActionFactory.createInplaceEditorAction(editor));
    }
    
    public final boolean isEditable() {
        return editor!=null && editor.isEnabled(this);
    }

    protected final LabelWidget getLabelWidget() {
        return labelWidget;
    }
    
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (previousState.isFocused() != state.isFocused()) {
            labelWidget.setBorder(state.isFocused()?BorderFactory.createDashedBorder
                    (getForeground(), 2, 2, true):BorderFactory.createEmptyBorder());
            revalidate(true);
        }
    }

    public static final int DEFAULT_GAP = 4;
    
    public abstract static class PaintableImageWidget extends Widget {
        
        private static final Stroke STROKE = new BasicStroke(1.0F, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

        private Shape shape;

        public PaintableImageWidget(Scene scene, Color color, 
                int preferredWidth, int preferredHeight) {
            super(scene);
            setPreferredSize(new Dimension(preferredWidth,preferredHeight));
            setForeground(color);
        }

        protected abstract Shape createImage(int width, int height);

        @org.netbeans.api.annotations.common.SuppressWarnings(" NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        protected final Shape getImage() {
            if (shape==null) {
                shape = createImage(getBounds().width, getBounds().height);
            }
            return shape;
        }

        protected Stroke getImageStroke() {
            return STROKE;
        }

        protected void paintWidget() {
            Graphics2D gr = getGraphics();
            Stroke previousStroke = gr.getStroke();
            Paint oldPaint = gr.getPaint();
            gr.setStroke(getImageStroke());
            gr.setPaint(getForeground());
            gr.draw(getImage());
            gr.setStroke(previousStroke);
            gr.setPaint(oldPaint);
        }
    }
}
