NetBeans Configuration File Initializer
=======================================

This is a small, graphical, standalone Swing utility with no dependencies, to
set up the `netbeans.conf` file on first use.

Rationale
---------

Particularly on Linux, the default appearance of NetBeans 11 (and many releases
before) is [awful](https://timboudreau.com/files/screen/08-05-2019_07-03-04.png) -
on large displays, microscopic font-sizes, and antialiasing usually disabled.
Users new to NetBeans are most likely to simply conclude it is unusable and
uninstall it in this state - an embarrassment to the community that develops
NetBeans, and furthering "NetBeans is dead" rumors.

A few command-line arguments to NetBeans can result in a 
[much-improved appearance](https://timboudreau.com/files/screen/08-08-2019_13-22-28.png).

For a new user of NetBeans, how to fix this situation - manually editing a
not-well-documented configuration file in a shell - is non-obvious and unlikely
to be discovered.

The sad reality is that _it is impossible for the JDK to get this right_.  For
a variety of historical and technical reasons, the 
[Extended Display Identification Data or EDID](https://en.wikipedia.org/wiki/Extended_Display_Identification_Data)
provided by monitors is usually wrong.  So it is impossible for the operating
system to actually know the _physical size_ of a monitor in order to determine
an appropriate font size.

Similarly, a Unix-like operating system *may* provide correct antialiasing hints,
if the user or distro has set them up - but many do not, at least not in a way
that the JDK can detect them.

If that isn't enough, what methods `java.awt.Toolkit` has for determining 
resolution are unreliable:

 * `getScreenResolution()` does not handle multiple monitors _at all_, it simply
picks one
 * `getScreenResolution()`'s return value will increment every time `xrandr` is
used to reduce and then restore screen size
 * `getScreenResolution()`' is going to get the resolution from the (usually wrong)
EDID of _some_ attached monitor, which will almost always be hard-coded to 96 DPI.


The Solution
------------

The obvious solution - and the only workable one - is to ask the user.  But since
this involves technical questions about red-green-blue pixel ordering and other
minutae of displays that most users will not be familiar with, this is better
handled by showing them examples and having them pick the one they like best.

For some things, we can pick initial defaults that will usually be correct, but
there is no subsititute for the user confirming what looks good to them.

That's what this small application does, in the following steps:

 * (if multiple monitors are present) - position the window on the monitor they use for coding
   * We pick the largest monitor available as the initial GraphicsConfiguration for the window
 * Ask them if this is a very large monitor, a traditionally-sized monitor, or a laptop screen
 * Specify whether it is a flat-panel display (LCD antialiasing options should be available) or not
(only off/on/GASP antialiasing should be available)
   * We pick the initial default value using two heuristics:
     * The chassis type, as reported by DMI information on Linux, if available - notebook = LCD
     * The aspect ratio - if not 4:3 or 5:4 (1280x1024), it is very probably an LCD
     * The answer to the laptop screen question
     * If neither can be determined, default to LCD simply because they are more common
 * Ask them to pick a font
 * Show examples of all available antialiasing options and to pick the one that looks best
 * Show examples of various font sizes and ask the user to pick one
     * The initial default is based on earlier answers about screen size

Additionally, since some other commonly useful tweaks can be made, we offer a few
additional options:

 * Setting the `-Xmx` and `-Xms` values, defaulting to 1/4 of OS-reported memory up to a 2Gb maximum
   * On Linux, high values will also turn on the G1 garbage collector (only if no GC settings are
already present) and transparent hugepage support, which improve performance
 * Set the user name system property (so `@author` tags in Javadoc will have a real name, not a login name)
 * Use UTF-8 as the default encoding for source files (only shown if the system encoding is *not* UTF-8)
 * Move the status line to the left of the menu bar to save screen real-estate
 * Use the JDK's OpenGL graphics back end
 * Enable logging to the console (useful for module development)


Implementation Details
----------------------

This application needs to look good and be readable on _a misconfigured system
where we literally do not yet know what will look good_.  As such, it errs on
the side of larger fonts and traditional grayscale antialiasing.

Settings that it can change, when already present, are parsed from the original
configuration file.

The parser and rewriter are comment- and order-preserving, so minimal changes
are made to the configuration file.

Settings which are useless on the target operating system are removed - for
example, `-J-Dapple.awt.useScreenMenuBar=true` is useless on Linux which does
not run the JDK for Mac OS.

On rewriting the configuration file, a single variable is added - `hw_sig`, which
contains a hash of the available screen sizes, memory size and operating system.
At present, the presence or absence of it can be used by the launcher to determine
if this application should be run before starting NetBeans; in the long run, it
could be used (at runtime?) to detect if the hardware configuration has changed
and it should be suggested to the user to re-run it.

Command-Line Arguments
--------------------

The following is displayed by running with `--help` as well:

```
Usage: java -jar conf-file-configurer.jar [--addfonts / -a] <value> [--crt / -c] [--file / -f] <value> [--help / -h] [--nongui / -n] [--pretend / -p]

    --addfonts / -a <comma-delimited-font-list> : Include additional fonts in the UI's choices
    --crt / -c                                  : Suppress options for LCD monitors.
    --file / -f <file>                          : The configuration file to process.  Optional in GUI mode.
    --nongui / -n                               : Run in non-GUI mode, making a best effort to come up with sane, conservative settings and writing them.
    --pretend / -p                              : Don't really write any files
```

Exit Codes
---------

Should we ever want to invoke this from a menu item within the IDE, these are
needed for determining exit status (and need for a restart):

 * 0 - success, config file was modified
 * 1 - exit code for running with `--help`
 * 2 - exit code for bad command-line arguments
 * 10 - exit due to user cancellation

