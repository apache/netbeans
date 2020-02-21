/*
 * Copyright (c) 2009-2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef	FREEWAY_WINDOW_H
#define	FREEWAY_WINDOW_H

#include <gtk/gtk.h>
#include <stdio.h>
#include <stdlib.h>

const unsigned long WHITE = 0;
const unsigned long BLACK = 1;

class FreewayWindow;
class VinfoWindow;
class FileWindow;
class HelpWindow;
class FwyControlObjects;
class FwyZoneObjects;
class FwyMenuObjects;
class VinfoControlObjects;

extern FreewayWindow   window;

class FreewayWindow {
public:
    void objects_initialize(int, char **);
    GtkWindow *getShell() { return GTK_WINDOW(shell); }

    GtkWidget *shell;

    GtkWidget *topform;
    GtkWidget *controlform;
    FwyControlObjects *control_objects;

    GtkWidget *separator2;
};

class FwyControlObjects {
public:
    FwyControlObjects(GtkWidget *_shell) { shell = _shell; }
    void objects_initialize();
    GtkWidget *getShellAsWidget() { return shell; }
    GtkWidget *getDrawingArea() { return drawing; }
    GtkWidget *getStatusArea() { return statuslab; }
    GtkWidget *getGapSlider() { return gap_slide; }
    GtkWidget *getTimeSlider() { return time_slide; }
    GtkWidget *getYesToggle() { return yes_tog; }
    GtkWidget *getTimeValueWidget() { return time_val; }
    
    FwyMenuObjects *menu_objects;
    FwyZoneObjects *zone_objects;

protected:
    GtkWidget *shell;
    GtkWidget *gslid_lab;
    GtkWidget *gap_slide;
    GtkWidget *tslid_lab;
    GtkWidget *time_slide;
    GtkWidget *time_lab;
    GtkWidget *time_val;
    GtkWidget *rand_lab;
    GtkWidget *rantog;
    GtkWidget *no_tog;
    GtkWidget *yes_tog;
    GtkWidget *separator;
    GtkWidget *spform;
    GtkWidget *drawing;
    GtkWidget *statuslab;
    GtkWidget *quit;
};

const int NZONES = 3;

// This is the declaration of "array of speeds expressed as char *s"
gchar *const SPEED_STR[] = {
   (gchar *) "75",
   (gchar *) "65",
   (gchar *) "55",
   (gchar *) "45",
   (gchar *) "35",
   (gchar *) "25",
   (gchar *) "15",
   (gchar *) "5",
}; 
const int NSPEEDS = sizeof (SPEED_STR) / sizeof( gchar *);
const int MAX_SPEED = 75;
const int MIN_SPEED = 5;
const int DEFAULT_SPEED = 65;

class FwyZoneObjects {
public:
    FwyZoneObjects(GtkWidget *_vbox) { vbox = _vbox; }
    void objects_initialize();
    GtkWidget *getWidget() { return vbox; }

    GtkWidget *vbox;
    struct Zone {
        GtkWidget *spop;        // speed selection combo box
        GtkWidget *spzonel;     // lowest speed in zone
        GtkWidget *spzoneu;     // highest speed in zone
    } z[NZONES];
};

class FwyMenuObjects {
public:
    FwyMenuObjects(GtkWidget *_window) { window = _window; }
    void objects_initialize();
    GtkWidget *getWindow() { return window; }
    GtkWidget *getMenuBar() { return menubar; }

    GtkWidget *window;
    GtkWidget *menubar;
};


/*
	Call-back functions
*/
extern void file_load();
extern void file_save();
extern void file_saveas();
extern void file_close();
extern void fwy_start();
extern void fwy_stop();
extern void reset_clear(GtkWidget *, gpointer );
extern void reset_reset(GtkWidget *, gpointer );
extern void help_about();
extern void gap_change(GtkWidget *, gpointer ); // uses call_data
extern void time_change(GtkWidget *, gpointer ); // uses call_data
extern void randx(GtkWidget *, gpointer ); // uses client_data
extern void zone_speed(GtkWidget *, gpointer ); // uses client_data
extern void fw_quit();
extern void help_close(GtkWidget *, gpointer );
extern void popup_destroyed(GtkWidget *, gpointer );
extern void show_message(const gchar *);

#endif /* FREEWAY_WINDOW_H */
