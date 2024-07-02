=== NetBeans SVG Icon Management Script ===

It is now increasingly common for NetBeans to run on Windows, Linux, or MacOS
machines with so-called "HiDPI" screens, aka. "Retina" screens in the Apple
world. These screens have about twice the physical pixel density of traditional
screens, making it necessary to scale GUI graphics up by some amount, e.g. 150%
or 200% (depending on OS and OS-level user settings), in order to remain
readable. Since Java 9, this scaling is done automatically by AWT by means of a
scaling default transform in each Component's Graphics2D instances. This makes
text sharp on HiDPI screens, but leaves bitmap icons blurry.

As of June 2024, NetBeans works quite well on HiDPI screens [1]. One item that
will remain forever "in progress" is the conversion of old bitmap icons (PNG and
GIF files) to scalable SVG versions. Most of the window system related icons,
like minimize, maximize, dropdown, etc. are already handled with scalable
painting code in NetBeans [2] and FlatLAF [3]. Besides these, NetBeans has about
2000 unique bitmap icon files, in color, with about 300 appearing commonly in
the user interface. As of June 2024, 67 of the most common icons have been
redrawn [4] as SVG files, and are committed in this repo.

The SVG icons drawn to date were made in Adobe Illustrator, in the file
"nb_vector_icons.ai" which is found in this directory. The Illustrator file
uses the artboard feature to name each icon for export, and contains two layers:
one containing the old bitmap icon that is used as a template for each icon (the
"Bitmap" layer), and one containing the new vector graphics icons
("New Vector"). If you don't have Adobe Illustrator installed, see
"nb_vector_icons_pdf_preview_240613.pdf" for an export in PDF format for
illustration purposes.

In addition to the SVG icons already drawn, the Illustrator file contains
already-prepared artboards for icons that should be drawn next, according to the
priority that was previously worked out [5]. These artboards are empty in the
"New Vector" layer, but have artboard names already defined and a corresponding
bitmap icon already placed in the "Bitmap" layer for reference. This provides a
good starting point for further work [6].

A style guide for SVG icons is written up here:
  https://cwiki.apache.org/confluence/display/NETBEANS/SVG+Icon+Style+Guide+and+Process
There is a tutorial video for drawing NetBeans icons in Adobe Illustrator here:
  https://vimeo.com/667860571

This folder contains a script, called IconTasks [7], that is used to find
duplicate icons, copy SVG files from Illustrator into the right locations in
the NetBeans sources, and generate a HTML page summary of all icons. The latest
generated HTML page summary has been committed at "IconScripts/icons.html" [8].

The IconTasks script reads the following files as input:
  * IconScripts/tables/icon-hashes.txt
      A complete list of GIF and PNG files in the NetBeans source repository,
      along with SHA256 hashes of each. This file was generated as follows:

        # While in the root of a cloned netbeans repo:
        git ls-files | grep -v '/test/' | grep '[.]png$' > images.txt
        git ls-files | grep -v '/test/' | grep '[.]gif$' >> images.txt
        while read in; do sha256sum "$in"; done < images.txt | sort > icon-hashes.txt

      As we don't anticipate new bitmap icons appearing in the repo, it is
      unlikely that these commands need to be run again, except to update file
      paths if NetBeans modules are renamed or moved around. (In which case the
      image paths would also have to be updated in mappings.tsv .)

  * IconScripts/tables/ready-artboards.txt
      The artboard names of SVG icons that are ready to be used and copied into
      appropriate locations in the NetBeans repository. (Excludes icons that
      haven't been drawn yet, or which should not be used because they are part
      of a series of similar icons that are not all complete yet.)

  * IconScripts/tables/mappings.tsv
      A two-column tab-separated file, where the value in the first column
      states the artboard name of an icon, and the value in the second column
      is the relative path of a PNG or GIF file icon in the NetBeans repo that
      should be mapped to this name. This list of mappings allows multiple
      similar-looking bitmap icons to be mapped to the same artboard name and
      thus SVG file. The generated icons.html file visualizes these mappings.

      For each artboard, the first mentioned mapping is to the bitmap that
      should serve as the "template" for the SVG icon (i.e. the bitmap that
      is pasted into the "Old Bitmaps" layer in the Illustrator file).

      When the IconTasks script is run, mappings.tsv will be updated with
      additional mappings that are found by matching the exact hash codes of
      icons. IconTasks will also reorder rows to keep mappings for each
      artboard together, and to list ready artboards first.

  * IconScripts/tables/illustrator_exports/icon_*.svg
      If there are SVG files in the illustrator_exports folder, they are assumed
      to have been exported from Illustrator and will be copied into appropriate
      locations in the NetBeans repo. See
      "Process for Exporting SVG Files from Adobe Illustrator" below.

      If there are no SVG files in this directory, the IconTasks script will
      only generate the icons.html page and update mappings.tsv, without
      copying SVG files. The script will verify the presence of SVG files in
      the expected locations in this case.

  * Additionally, the IconTasks script will verify the location of referenced
    bitmap icons and parse their resolutions for the HTML page summary. The HTML
    page summary also includes bitmap icons that have not been explicitly mapped
    to an artboard name, in a final "(no assigned artboard)" section.

Running the IconTasks script
* The IconTasks script is a Java application that should be run without
  arguments with nbbuild/IconScripts/hidpi-icons as the current working
  directory. To do this:
  1) Open nbbuild/IconScripts/hidpi-icons as a Maven project in NetBeans
  2) Do a clean build of the project.
  3) Open IconTasks.java
  4) Invoke Run->Run File (Shift+F6).

Process for Exporting SVG Files from Adobe Illustrator
1) Open the "nb_vector_icons.ai" file in Adobe Illustrator.

   For new icons, each icon needs to already be in its own named artboard, sized
   to the correct size in pixels. Verify that horizontal and vertical lines are
   aligned to pixel boundaries. See the style guide here:
    https://cwiki.apache.org/confluence/display/NETBEANS/SVG+Icon+Style+Guide+and+Process

2) Save the Illustrator file to a temporary file, to avoid accidentally
   overwriting the original during subsequent steps.
3) Delete the "Old Bitmaps" layer. (This is important, otherwise the old
   bitmap icon will be embedded into each exported SVG file.)
4) From the "File" menu, click "Save a Copy". Select a folder, use the following
   settings, and click OK:

     File name: icon (will be prefixed to the artboard names; mandatory)
     Folder: nbbuild/IconScripts/tables/illustrator_exports
             (Select the above subfolder in the NetBeans repo.)
     Save as type: SVG
     Use artboards: Check and select "All".

5) In the SVG export options dialog that shows up, enter the following settings,
   which have been tested and are known to work with NetBeans' SVG loader
   implementation:

     SVG Profiles: SVG 1.1
     Type: Convert to outline
     CSS Properties: "Style elements"
     Uncheck "Include Unused Graphic Styles"
     Decimal Places: 3
     Encoding: UTF-8
     Responsive: Disabled

6) Run the IconTasks script. It will copy the SVG files to the various
   required locations in the NetBeans repo, with an Apache License header
   automatically appended. See "Running the IconTasks script" above.

(The IconTasks script and the generated HTML page replaces the Google Sheets
spreadsheet that was once used to track icon mappings.)

Foonotes
[1] See
    https://cwiki.apache.org/confluence/display/NETBEANS/HiDPI+%28Retina%29+improvements
    for an overview of past HiDPI-related work on NetBeans.
[2] https://github.com/apache/netbeans/pull/859
    https://github.com/apache/netbeans/pull/2966
[3] E.g. https://github.com/apache/netbeans/tree/master/platform/o.n.swing.laf.flatlaf/src/org/netbeans/swing/laf/flatlaf/ui
[4] https://github.com/apache/netbeans/pull/2387
    https://github.com/apache/netbeans/pull/2937
    https://github.com/apache/netbeans/pull/7463
[5] https://issues.apache.org/jira/browse/NETBEANS-2605
    https://people.csail.mit.edu/ebakke/misc/netbeans-icons/prioritized.html
[6] Venus Chung & Peter Cheung have a draft of many of the remaining icons with
    defined artboard names. Eirik Bakke was corresponding with them in 2022, and
    gathered consent to donate the work from Peter but was not yet able to get
    in touch with Venus. So we haven't made use of this work yet.
[7] hidpi-icons/src/main/java/org/netbeans/build/icons/IconTasks.java
[8] See https://people.csail.mit.edu/ebakke/misc/netbeans-icons-240612.html
    for a version of this page that is hosted externally (for ease of linking
    during email discussions etc.)
