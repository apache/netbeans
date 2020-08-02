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

//
// window_ui.cc - Graphical User Interface object initialization functions.
//

#include "FreewayWindow.h"

static unsigned int blue = BLACK;
static unsigned int grey = WHITE;

void
FreewayWindow::objects_initialize(int app_argc, char **app_argv)
{

    shell = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    gtk_window_set_title(GTK_WINDOW(shell), "Freeway");
    gtk_window_set_resizable(GTK_WINDOW(shell), FALSE);

    g_signal_connect(G_OBJECT(shell), "destroy", G_CALLBACK(gtk_main_quit), NULL);

    // Initialize main window widgets
    control_objects = new FwyControlObjects(shell);
    control_objects->objects_initialize();
}

void 
FwyControlObjects::objects_initialize()
{
    GtkWidget *main_vbox;

    main_vbox = gtk_vbox_new(FALSE, 1);
    gtk_container_set_border_width(GTK_CONTAINER(main_vbox), 1);
    gtk_container_add(GTK_CONTAINER(shell), main_vbox);

    menu_objects = new FwyMenuObjects(getShellAsWidget());
    menu_objects->objects_initialize();
    gtk_box_pack_start(GTK_BOX(main_vbox), menu_objects->getMenuBar(), FALSE, TRUE, 0);

    GtkWidget *fixed = gtk_fixed_new();
    gtk_container_add(GTK_CONTAINER(main_vbox), fixed);
    gslid_lab = gtk_label_new((const gchar *) "Gap between vehicles:");
    tslid_lab = gtk_label_new((const gchar *) "Time between updates:");
    time_lab = gtk_label_new((const gchar *) "Simulation time:");
    rand_lab = gtk_label_new((const gchar *) "Randomize?");
    gtk_fixed_put((GtkFixed *) fixed, gslid_lab, 20, 10);
    gtk_fixed_put((GtkFixed *) fixed, tslid_lab, 20, 30);
    gtk_fixed_put((GtkFixed *) fixed, time_lab, 400, 10);
    gtk_fixed_put((GtkFixed *) fixed, rand_lab, 400, 30);

    GtkObject *gap_adj = gtk_adjustment_new(0.0, 0.0, 101.0, 0.1, 1.0, 1.0);
    gap_slide = gtk_hscale_new(GTK_ADJUSTMENT(gap_adj));
    g_signal_connect(G_OBJECT(gap_slide), "value-changed", G_CALLBACK(gap_change), NULL);
    gtk_widget_set_size_request(GTK_WIDGET(gap_slide), 125, -1);
    gtk_scale_set_value_pos(GTK_SCALE(gap_slide), GTK_POS_RIGHT);
    gtk_range_set_update_policy(GTK_RANGE(gap_slide), GTK_UPDATE_DISCONTINUOUS);
    gtk_scale_set_draw_value((GtkScale *) gap_slide, FALSE);
    gtk_fixed_put((GtkFixed *) fixed, gap_slide, 175, 11);
    
    GtkObject *time_adj = gtk_adjustment_new(0.0, 0.0, 101.0, 0.1, 1.0, 1.0);
    g_signal_emit_by_name(G_OBJECT(time_adj), "changed");
    time_slide = gtk_hscale_new((GtkAdjustment *) time_adj);
    g_signal_connect(G_OBJECT(time_slide), "value-changed", G_CALLBACK(time_change), NULL);
    gtk_widget_set_size_request(GTK_WIDGET(time_slide), 125, -1);
    gtk_range_set_update_policy(GTK_RANGE(time_slide), GTK_UPDATE_DISCONTINUOUS);
    gtk_scale_set_draw_value((GtkScale *) time_slide, FALSE);
    gtk_fixed_put((GtkFixed *) fixed, time_slide, 175, 31);

    time_val = gtk_label_new("0:00:00.0");
    gtk_fixed_put((GtkFixed *) fixed, time_val, 510, 10);

    no_tog = gtk_radio_button_new_with_label_from_widget(NULL, "No");
    gtk_fixed_put((GtkFixed *) fixed, no_tog, 510, 30);
    g_signal_connect(G_OBJECT(no_tog), "clicked", G_CALLBACK(randx), 0);
    
    yes_tog = gtk_radio_button_new_with_label_from_widget((GtkRadioButton *) no_tog, "Yes    ");
    gtk_fixed_put((GtkFixed *) fixed, yes_tog, 560, 30);
    g_signal_connect(G_OBJECT(yes_tog), "clicked", G_CALLBACK(randx), (gpointer) 1);

    separator = gtk_hseparator_new();
    gtk_fixed_put((GtkFixed *) fixed, separator, 0, 50);

    zone_objects = new FwyZoneObjects(main_vbox);
    zone_objects->objects_initialize();
    
    GtkWidget *frame = gtk_frame_new(NULL);
    gtk_container_set_border_width(GTK_CONTAINER(frame), 6);
    drawing = gtk_drawing_area_new();
    gtk_container_add(GTK_CONTAINER(frame), drawing);
    gtk_drawing_area_size((GtkDrawingArea *) drawing, 200, 150);
    gtk_container_add(GTK_CONTAINER(main_vbox), frame);

    fixed = gtk_fixed_new();
    gtk_container_set_border_width(GTK_CONTAINER(fixed), 6);
    statuslab = gtk_label_new("");
    gtk_fixed_put((GtkFixed *) fixed, statuslab, 10, 0);
    
    quit = gtk_button_new_with_label("Quit");
    gtk_fixed_put((GtkFixed *) fixed, quit, 575,  0);
    gtk_container_add(GTK_CONTAINER(main_vbox), fixed);
    g_signal_connect(G_OBJECT(quit), "clicked", G_CALLBACK(fw_quit), 0);

    gtk_widget_show_all(shell);
}

static GtkItemFactoryEntry menu_items[] = {
    { (gchar *) "/_File",             NULL,	NULL,	     0, (gchar *) "<Branch>"},
    { (gchar *) "/File/_Load...",     (gchar *) "<Ctrl>L",	G_CALLBACK(file_load),   0, (gchar *) "<StockItem>", GTK_STOCK_OPEN},
    { (gchar *) "/File/_Save",        NULL,	G_CALLBACK(file_save),   0, (gchar *) "<StockItem>", GTK_STOCK_SAVE},
    { (gchar *) "/File/Save _As...",  NULL,	G_CALLBACK(file_saveas), 0, (gchar *) "<Item>"},
    { (gchar *) "/File/_Close File",  NULL,	G_CALLBACK(file_close),  0, (gchar *) "<Item>"},
    { (gchar *) "/File/Separator",    NULL,	NULL,	     0, (gchar *) "<Separator>"},
    { (gchar *) "/File/_Quit",        NULL,	G_CALLBACK(fw_quit),     0, (gchar *) "<Item>"},
    { (gchar *) "/_Actions",          NULL,	NULL,	     0, (gchar *) "<Branch>"},
    { (gchar *) "/Actions/S_tart",    (gchar *) "<Ctrl>S",	G_CALLBACK(fwy_start),   0, (gchar *) "<Item>"},
    { (gchar *) "/Actions/_Stop",     (gchar *) "<Ctrl>Q",	G_CALLBACK(fwy_stop),    0, (gchar *) "<Item>"},
    { (gchar *) "/_Reset",            NULL,	NULL,	     0, (gchar *) "<Branch>"},
    { (gchar *) "/Reset/_Clear Wrecks", NULL,	(GtkItemFactoryCallback) reset_clear, 0, (gchar *) "<Item>"},
    { (gchar *) "/Reset/_Reset",      NULL,	(GtkItemFactoryCallback) reset_reset, 0, (gchar *) "<Item>"},
    { (gchar *) "/_Help",             NULL,	NULL,	     0, (gchar *) "<LastBranch>"},
    { (gchar *) "/Help/_About...",    NULL,	G_CALLBACK(help_about),  0, (gchar *) "<Item>"},
};
static gint nmenu_items = sizeof(menu_items) / sizeof(menu_items[0]);

void
FwyMenuObjects::objects_initialize()
{
    GtkItemFactory *item_factory;
    GtkAccelGroup *accel_group = gtk_accel_group_new();

    item_factory = gtk_item_factory_new(GTK_TYPE_MENU_BAR,
	    "<FreewayMain>", accel_group);
    gtk_item_factory_create_items(item_factory, nmenu_items, menu_items, NULL);
    menubar = gtk_item_factory_get_widget(item_factory, "<FreewayMain>");
}

void
FwyZoneObjects::objects_initialize()
{
    GtkWidget *hbox = gtk_hbox_new(FALSE, 1);
    gtk_container_set_border_width(GTK_CONTAINER(hbox), 6);
    gtk_container_add(GTK_CONTAINER(getWidget()), hbox);
    for (int i = 0; i < NZONES; i++) {
        GtkWidget *fixed = gtk_fixed_new();
        z[i].spop = gtk_combo_box_new_text();
        z[i].spzoneu = gtk_label_new(NULL);
        z[i].spzonel = gtk_label_new(NULL);
        gtk_fixed_put((GtkFixed *) fixed, z[i].spop, 20, 5);
        gtk_fixed_put((GtkFixed *) fixed, z[i].spzoneu, 80, 5);
        gtk_fixed_put((GtkFixed *) fixed, z[i].spzonel, 80, 20);
        for (int j = 0; j < NSPEEDS; j++) {
            gtk_combo_box_append_text((GtkComboBox *) z[i].spop, SPEED_STR[j]);
            gtk_combo_box_set_active((GtkComboBox *) z[i].spop, 1);
            g_signal_connect(G_OBJECT(z[i].spop), "changed", G_CALLBACK(zone_speed), (gpointer) (gint64) (i + 1));
        }
        GtkWidget *sep = gtk_vseparator_new();
        gtk_fixed_put((GtkFixed *) fixed, sep, 100, 40);
        gtk_container_add(GTK_CONTAINER(hbox), fixed);
    }
}
