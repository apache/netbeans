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
package org.netbeans.spi.dashboard;

import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.Action;
import javax.swing.JComponent;
import org.openide.util.ImageUtilities;

/**
 * Elements that can be provided by a dashboard widget from
 * {@link DashboardWidget#elements(org.netbeans.spi.dashboard.DashboardDisplayer.Panel)}.
 */
public sealed abstract class WidgetElement {

    private WidgetElement() {
    }

    /**
     * A normal text element.
     *
     * @param text element text
     * @return text element
     */
    public static TextElement text(String text) {
        return new TextElement(TextElement.Kind.NORMAL, text);
    }

    /**
     * An aside text element. Text that is less important or provides additional
     * information. May be rendered muted or smaller by the displayer.
     *
     * @param text element text
     * @return text element
     */
    public static TextElement aside(String text) {
        return new TextElement(TextElement.Kind.ASIDE, text);
    }

    /**
     * A text element providing a label for an unavailable resource (eg. no
     * recent files, network unavailable). May be rendered differently by the
     * displayer.
     *
     * @param text element text
     * @return text element
     */
    public static TextElement unavailable(String text) {
        return new TextElement(TextElement.Kind.UNAVAILABLE, text);
    }

    /**
     * A sub-heading text element used to group other elements. May be rendered
     * larger by the displayer.
     *
     * @param text element text
     * @return text element
     */
    public static TextElement subheading(String text) {
        return new TextElement(TextElement.Kind.SUBHEADING, text);
    }

    /**
     * An image element. The resource path should be one suitable for passing to
     * {@link ImageUtilities#loadImage(java.lang.String, boolean)}. The resource
     * will be localized.
     *
     * @param resourcePath path to image
     * @return image element
     */
    public static ImageElement image(String resourcePath) {
        return new ImageElement(resourcePath);
    }

    /**
     * An action element. The action will normally be rendered as a button by
     * the displayer.
     *
     * @param action button action
     * @return action element
     */
    public static ActionElement action(Action action) {
        return new ActionElement(action, false, true);
    }

    /**
     * An action element. The action will normally be rendered as a button by
     * the displayer. Hints that the displayer should not use any icon set on
     * the action.
     *
     * @param action button action
     * @return action element
     */
    public static ActionElement actionNoIcon(Action action) {
        return new ActionElement(action, false, false);
    }

    /**
     * An action element. The action will normally be rendered as a hyperlink by
     * the displayer.
     *
     * @param action link action
     * @return action element
     */
    public static ActionElement actionLink(Action action) {
        return new ActionElement(action, true, true);
    }

    /**
     * An action element. The action will normally be rendered as a hyperlink by
     * the displayer. Hints that the displayer should not use any icon set on
     * the action.
     *
     * @param action link action
     * @return action element
     */
    public static ActionElement actionLinkNoIcon(Action action) {
        return new ActionElement(action, true, false);
    }

    /**
     * A link to be opened in the default browser or viewer. The link will
     * normally be rendered as a hyperlink.
     *
     * @param text link text
     * @param link link destination
     * @return link element
     */
    public static LinkElement link(String text, URI link) {
        return new LinkElement(text, link, false);
    }

    /**
     * A link to be opened in the default browser or viewer. The link will
     * normally be rendered as a button.
     *
     * @param text link text
     * @param link link destination
     * @return link element
     */
    public static LinkElement linkButton(String text, URI link) {
        return new LinkElement(text, link, true);
    }

    /**
     * A separator element.
     *
     * @return separator element
     */
    public static SeparatorElement separator() {
        return new SeparatorElement();
    }

    /**
     * An element wrapping a Swing component supplier. This should only be used
     * where the other elements cannot provide the required functionality. Some
     * displayers may ignore component elements. The supplier must create a new
     * component whenever requested.
     *
     * @param componentSupplier component supplier
     * @return component element
     */
    public static ComponentElement component(Supplier<JComponent> componentSupplier) {
        return new ComponentElement(componentSupplier);
    }

    /**
     * Text element.
     */
    public static final class TextElement extends WidgetElement {

        /**
         * The type of text element.
         */
        public enum Kind {
            NORMAL, ASIDE, SUBHEADING, UNAVAILABLE
        }

        private final Kind kind;
        private final String text;

        TextElement(Kind kind, String text) {
            this.kind = Objects.requireNonNull(kind);
            this.text = Objects.requireNonNull(text);
        }

        /**
         * Element text.
         *
         * @return text
         */
        public String text() {
            return text;
        }

        /**
         * Element kind.
         *
         * @return kind
         */
        public Kind kind() {
            return kind;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.text);
            hash = 89 * hash + Objects.hashCode(this.kind);
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
            final TextElement other = (TextElement) obj;
            if (!Objects.equals(this.text, other.text)) {
                return false;
            }
            return this.kind == other.kind;
        }

        @Override
        public String toString() {
            return "TextElement{" + "kind=" + kind + ", text=" + text + '}';
        }

    }

    /**
     * Image element.
     */
    public static final class ImageElement extends WidgetElement {

        private final String resourcePath;

        ImageElement(String resourcePath) {
            this.resourcePath = Objects.requireNonNull(resourcePath);
        }

        /**
         * Image resource path.
         *
         * @return image path
         */
        public String resourcePath() {
            return resourcePath;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.resourcePath);
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
            final ImageElement other = (ImageElement) obj;
            return Objects.equals(this.resourcePath, other.resourcePath);
        }

        @Override
        public String toString() {
            return "ImageElement{" + "resourcePath=" + resourcePath + '}';
        }

    }

    /**
     * Action element.
     */
    public static final class ActionElement extends WidgetElement {

        private final Action action;
        private final boolean link;
        private final boolean icon;

        ActionElement(Action action, boolean link, boolean icon) {
            this.action = Objects.requireNonNull(action);
            this.link = link;
            this.icon = icon;
        }

        /**
         * Element action.
         *
         * @return action
         */
        public Action action() {
            return action;
        }

        /**
         * Hint whether to render as hyperlink rather than button.
         *
         * @return render as link
         */
        public boolean asLink() {
            return link;
        }

        /**
         * Hint whether to use the action icon (if supported).
         *
         * @return show icon
         */
        public boolean showIcon() {
            return icon;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + Objects.hashCode(this.action);
            hash = 29 * hash + (this.link ? 1 : 0);
            hash = 29 * hash + (this.icon ? 1 : 0);
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
            final ActionElement other = (ActionElement) obj;
            if (this.link != other.link) {
                return false;
            }
            if (this.icon != other.icon) {
                return false;
            }
            return Objects.equals(this.action, other.action);
        }

        @Override
        public String toString() {
            return "ActionElement{" + "action=" + action + ", link=" + link + ", icon=" + icon + '}';
        }

    }

    /**
     * Link element.
     */
    public static final class LinkElement extends WidgetElement {

        private final String text;
        private final URI link;
        private final boolean button;

        LinkElement(String text, URI link, boolean button) {
            this.text = Objects.requireNonNull(text);
            this.link = Objects.requireNonNull(link);
            this.button = button;
        }

        /**
         * Text to render for link.
         *
         * @return link text
         */
        public String text() {
            return text;
        }

        /**
         * Link to open when clicked.
         *
         * @return link
         */
        public URI link() {
            return link;
        }

        /**
         * Hint whether to render the link as a button rather than hyperlink.
         *
         * @return as button
         */
        public boolean asButton() {
            return button;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + Objects.hashCode(this.text);
            hash = 23 * hash + Objects.hashCode(this.link);
            hash = 23 * hash + (this.button ? 1 : 0);
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
            final LinkElement other = (LinkElement) obj;
            if (this.button != other.button) {
                return false;
            }
            if (!Objects.equals(this.text, other.text)) {
                return false;
            }
            return Objects.equals(this.link, other.link);
        }

        @Override
        public String toString() {
            return "LinkElement{" + "text=" + text + ", link=" + link + ", button=" + button + '}';
        }

    }

    /**
     * Separator element.
     */
    public static final class SeparatorElement extends WidgetElement {

        SeparatorElement() {

        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SeparatorElement;
        }

        @Override
        public int hashCode() {
            return SeparatorElement.class.hashCode();
        }

        @Override
        public String toString() {
            return "SeparatorElement{}";
        }

    }

    /**
     * Component element. See caveats on use mentioned at {@link #component()}.
     */
    public static final class ComponentElement extends WidgetElement {

        private final Supplier<JComponent> componentSupplier;

        ComponentElement(Supplier<JComponent> componentSupplier) {
            this.componentSupplier = Objects.requireNonNull(componentSupplier);
        }

        /**
         * Component supplier.
         *
         * @return component supplier
         */
        public Supplier<JComponent> componentSupplier() {
            return componentSupplier;
        }

        /**
         * Convenience method to call the supplier to create the component.
         *
         * @return newly created component
         */
        public JComponent component() {
            return componentSupplier.get();
        }

        @Override
        public String toString() {
            return "ComponentElement{" + "componentSupplier=" + componentSupplier + '}';
        }

    }

}
