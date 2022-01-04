Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

Description:
	Freeway simulates traffic flow on a typical California highway.
	Individual vehicles make decisions about how fast and where to
	drive based on the conditions around them.

	The user can control some of the simulation parameters:
	* Speed limit in the three zones
	* Distance between cars
	* Simulation speed

Supported Platforms:
    Solaris:
        The primary platform during the GTK port was Solaris 10. 
        GtkFreeway builds with either Developer Studio or GNU tool chains.

    OpenSolaris:
	 Additional required packages:
          SUNWgnome-common-devel
          SUNWxinc
          SUNWxorg-headers
        GtkFreeway builds with either Developer Studio or GNU tool chains.

    Linux:
        Linux development was done on Ubuntu 7.10.

    Windows:
        Currently only tested on Windows XP using MinGW toolchain and the all-in-one
        GTK bundle from http://www.gtk.org/download-windows.html. Note that this
        bundle explicitly states it *only* compiles with the MinGW toolchain (it
        specifically does *not* compile with the Cygwin toolchain).

        While its likely that this demo will compile with Cygwin libraries and
        the toolchain, it has not been verified and is not officially supported.

Unsupported Platforms:
    Currently, the most important platform we'd like to support (and don't) is Mac
    OSX. We don't support the Mac because there is no supported version of the GTK+
    libraries for the Mac. There is a project in-progress to provide GTK+ on the Mac.
    
    In general, the gating factor is GTK+. If you can supply the correct set of
    libraries and tools (including pkg-config and the gtk+-2.0 packages) then its
    unlikely the GtkFreeway demo won't build.

Disclaimer:
        Freeway was written in 1991 as an XView application, converted to a Motif
        application and has subsequently been included in all Visual WorkShop and
        Developer Studio releases. In 2008 it was ported from Motif to GTK+ 2 in an
        effort to modernize it. However, while it was converted to GTK, no effort
        was made to complete partially implemented features. There appear to be
        hooks for things not currently working. Since the demo program is so old,
        its original authors and plans are long gone. Much of the C++ programming
        is also based on the original 1991 development and does not make use of
        more modern keywords and constructs (such as templates and consts).
