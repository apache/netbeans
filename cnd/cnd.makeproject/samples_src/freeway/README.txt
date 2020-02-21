#
# Copyright (c) 2009-2010, Oracle and/or its affiliates. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice,
#   this list of conditions and the following disclaimer.
#
# * Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
#
# * Neither the name of Oracle nor the names of its contributors
#   may be used to endorse or promote products derived from this software without
#   specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
# THE POSSIBILITY OF SUCH DAMAGE.
#

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
