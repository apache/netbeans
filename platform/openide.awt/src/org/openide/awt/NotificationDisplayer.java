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

package org.openide.awt;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Creates and shows clickable notifications in the main status line.
 *
 * @since 7.6
 *
 * @author S. Aubrecht
 */
public abstract class NotificationDisplayer {

    /**
     * Priority of Notification
     */
    public static enum Priority {
        HIGH(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/openide/awt/resources/priority_high.png"))), //NOI18N
        NORMAL(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/openide/awt/resources/priority_normal.png"))), //NOI18N
        LOW(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/openide/awt/resources/priority_low.png"))), //NOI18N
        /** Priority that shows the notification without details.
         * Details shall be shown only later, per user request.
         * @since 7.18
         */
        SILENT(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/openide/awt/resources/priority_silent.png"))); //NOI18N

        private final Icon icon;

        private Priority(Icon icon) {
            this.icon = icon;
        }

        /**
         * Returns priority icon
         *
         * @since 7.58
         */
        public Icon getIcon() {
            return icon;
        }
    }

    /**
     * Category of Notification, displayed in Notifications TC. Use one of the defaults categories (INFO, WARNING, ERROR) or create a custom category.
     *
     * To create a custom add following code to your <code>layer.xml</code>:
     * <pre>
     * {@code
     * <folder name="Notification">
     *      <folder name="Category">
     *          <file name="categoryA.instance">
     *              <attr name="instanceCreate" methodvalue="org.openide.awt.NotificationDisplayer.createCategory"/>
     *
     *              <attr name="localizingBundle" stringvalue="org.modules.mymodule.Bundle"/> <!-- Bundle file with used keys-->
     *              <attr name="categoryName" stringvalue="categoryA_name/> <!-- Actual category name-->
     *              <attr name="diplayNameKey" stringvalue="categoryA_displayName_key"/> <!-- Key to the Bundle file-->
     *              <attr name="descriptionKey" stringvalue="categoryA_description_key"/> <!-- Key to the Bundle file-->
     *          </file>
     *      </folder>
     * </folder>
     * }
     * </pre>
     *
     * @since 7.58
     */
    public static final class Category implements Comparable<Category>{

        public static final Category INFO = new Category("default_category_info", NbBundle.getMessage(NotificationDisplayer.class, "INFO_CATEGORY"), //NOI18N
                NbBundle.getMessage(NotificationDisplayer.class, "INFO_CATEGORY_DESCRIPTION")); //NOI18N
        public static final Category WARNING = new Category("default_category_warning", NbBundle.getMessage(NotificationDisplayer.class, "WARNING_CATEGORY"), //NOI18N
                NbBundle.getMessage(NotificationDisplayer.class, "WARNING_CATEGORY_DESCRIPTION")); //NOI18N
        public static final Category ERROR = new Category("default_category_error", NbBundle.getMessage(NotificationDisplayer.class, "ERROR_CATEGORY"), //NOI18N
                NbBundle.getMessage(NotificationDisplayer.class, "ERROR_CATEGORY_DESCRIPTION")); //NOI18N

        private final String name;
        private final String displayName;
        private final String description;
        private int index;

        Category(String name, String displayName, String description) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
        }

        /**
         * Returns category name - unique id
         *
         * @since 7.58
         */
        public String getName() {
            return name;
        }

         /**
         * Returns category display name
         *
         * @since 7.58
         */
        public String getDisplayName() {
            return displayName;
        }

        void setIndex(int index) {
            this.index = index;
        }

        /**
         * Returns category description
         *
         * @since 7.58
         */
        public String getDescription() {
            return description;
        }

        @Override
        public int compareTo(Category other) {
            return index - other.index;
        }

        /**
         * Returns all available categories
         *
         * @since 7.58
         */
        public static List<Category> getCategories() {
            return NotificationCategoryFactory.getInstance().getCategories();
        }

        static List<Category> getDefaultCategories() {
            List<Category> defaultCategories = new ArrayList<Category>();
            defaultCategories.add(ERROR);
            defaultCategories.add(WARNING);
            defaultCategories.add(INFO);
            return defaultCategories;
        }
    }

    /**
     * Looks for an implementation in global Lookup, if none is found then it falls
     * back to a primitive implementation which displays the notifications in main 
     * window's status line.
     * @return Default implementation.
     */
    public static NotificationDisplayer getDefault() {
        NotificationDisplayer res = Lookup.getDefault().lookup(NotificationDisplayer.class);
        if( null == res ) {
            Logger.getLogger( NotificationDisplayer.class.getName()).log( Level.INFO, "No NotificationDisplayer implementation available."); //NOI18N
            res = new SimpleNotificationDisplayer();
        }
        return res;
    }

    /**
     * Create and show new notification with the default priority.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param detailsText Detailed description of the notification. If detailsAction
     * is non-null then this text will be presented as a clickable link. Html is
     * not supported, any html tags will be escaped.
     * @param detailsAction Action to invoke when user click details text or null.
     * @return New notification.
     */
    public Notification notify( String title, Icon icon,
            String detailsText, ActionListener detailsAction ) {
        return notify( title, icon, detailsText, detailsAction, Priority.NORMAL);
    }

    /**
     * Create and show new notification.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param detailsText Detailed description of the notification. If detailsAction
     * is non-null then this text will be presented as a clickable link. Html is
     * not supported, any html tags will be escaped.
     * @param detailsAction Action to invoke when user click details text or null.
     * @param priority Notification priority
     * @return New notification.
     */
    public abstract Notification notify( String title, Icon icon,
            String detailsText, ActionListener detailsAction, Priority priority);

    /**
     * Create and show new notification.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param detailsText Detailed description of the notification. If detailsAction
     * is non-null then this text will be presented as a clickable link. Html is
     * not supported, any html tags will be escaped.
     * @param detailsAction Action to invoke when user click details text or null.
     * @param priority Notification priority
     * @param category Notification category.
     * @return New notification.
     * @since 7.58
     */
    public Notification notify(String title, Icon icon,
            String detailsText, ActionListener detailsAction, Priority priority, Category category) {
        return notify(title, icon, detailsText, detailsAction, priority);
    }

    /**
     * Create and show new notification.
     *
     * @param title Notification title. Html is not supported, any html tags will be escaped.
     * @param icon Notification icon
     * @param detailsText Detailed description of the notification. If detailsAction
     * is non-null then this text will be presented as a clickable link. Html is
     * not supported, any html tags will be escaped.
     * @param detailsAction Action to invoke when user click details text or null.
     * @param priority Notification priority
     * @param categoryName Notification category name, refers to a custom category created in e.g. layer.xml.
     * @return New notification.
     * @since 7.58
     */
    public Notification notify(String title, Icon icon,
            String detailsText, ActionListener detailsAction, Priority priority, String categoryName) {
        return notify(title, icon, detailsText, detailsAction, priority, NotificationCategoryFactory.getInstance().getCategory(categoryName));
    }

    /**
     * Create and show new notification with customized content.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param balloonDetails Component that will show below notification title 
     * in a balloon.
     * @param popupDetails Component that will show below notification title
     * in notifications list. 
     * @param priority Notification priority.
     * @return New notification.
     */
    public abstract Notification notify( String title, Icon icon,
            JComponent balloonDetails, JComponent popupDetails, Priority priority);

    /**
     * Create and show new notification with customized content.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param balloonDetails Component that will show below notification title
     * in a balloon.
     * @param popupDetails Component that will show below notification title
     * in notifications list.
     * @param priority Notification priority.
     * @param category Notification category.
     * @return New notification.
     * @since 7.58
     */
    public Notification notify(String title, Icon icon,
            JComponent balloonDetails, JComponent popupDetails, Priority priority, Category category) {
        return notify(title, icon, balloonDetails, popupDetails, priority);
    }

    /**
     Create and show new notification with customized content.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param balloonDetails Component that will show below notification title
     * in a balloon.
     * @param popupDetails Component that will show below notification title
     * in notifications list.
     * @param priority Notification priority.
     * @param categoryName Notification category name, refers to a custom category created in e.g. layer.xml.
     * @return New notification.
     * @since 7.58
     */
    public Notification notify(String title, Icon icon,
            JComponent balloonDetails, JComponent popupDetails, Priority priority, String categoryName) {
        return notify(title, icon, balloonDetails, popupDetails, priority, NotificationCategoryFactory.getInstance().getCategory(categoryName));
    }

    static Category createCategory(Map<String, String> attrs) {
        return NotificationCategoryFactory.create(attrs);
    }

    /**
     * Simple implementation of NotificationDisplayer which shows the notifications
     * on the main status line.
     */
    private static class SimpleNotificationDisplayer extends NotificationDisplayer {

        @Override
        public Notification notify(String title, Icon icon, String detailsText, ActionListener detailsAction, Priority priority) {
            return notify( title + " - " + detailsText, priority );
        }

        @Override
        public Notification notify(String title, Icon icon, JComponent balloonDetails, JComponent popupDetails, Priority priority) {
            return notify( title, priority );
        }

        private Notification notify( String text, Priority priority ) {
            int importance = 1;
            switch( priority ) {
                case HIGH:
                    importance = 100;
                    break;
                case NORMAL:
                    importance = 50;
                    break;
                case LOW:
                case SILENT:
                    importance = 1;
                    break;
            }
            Message msg = StatusDisplayer.getDefault().setStatusText( text, importance );
            return new NotificationImpl(msg);
        }
    }

    private static class NotificationImpl extends Notification {

        private final Message msg;

        public NotificationImpl( Message msg ) {
            this.msg = msg;
        }

        @Override
        public void clear() {
            msg.clear(0);
        }
    }
}
