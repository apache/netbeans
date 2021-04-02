Updating the Profiler's Native Interface
========================================

The profiler needs low-level access to the JVM running the profiled
application and therefore a native code interface library is
required. This library, written in C, uses JNI and a specific version
exists for each operating system and CPU. Rebuilding the native
libraries requires the following steps on each supported platform. See
below for more detailed and specific instructions. Rebuilding the
libraries should not need to be done in most cases.

1. Build the cross-platform (Java) part of the interface. This is the NB
   module `lib.profiler` (where this README file resides.)
2. Using the appropriate script, rebuild the native code library. These
   are located in the scripts subdirectory of the module.
3. Refresh the zip file which contains the libraries for all supported
   platforms.
4. Push changes to NetBeans git repo, and upload the zip file to the OSUOSL
   website. 

For other NetBeans developers, the normal build task will
automatically download and unpack the binaries - they don't need the
facilty to compile native code.

Windows
-------

Download the Build Tools for Visual Studio from Microsoft or the full
Visual Studio IDE. Set JDK_HOME to a 64-bit JDK (8 or 11). From an x64
Developer Prompt, run the script
`buildnative-windows64-16.bat`. Repeat with a 32-bit JDK, an x86
Developer Prompt, and `buildnative-windows-16.bat`.

The outputs will be in the `release\lib\deployed\jdk16\windows_amd64`
and `release\lib\deployed\jdk16\windows` directories.

Tested on Build Tools for Visual Studio 2019.

Linux
-----

Install a C compiler (steps vary according to the Distribution). Set
JDK_HOME to a JDK 8 or 11. From a shell, run the script
`buildnative-linux64` (for 64-bit distributions) or
`buildnative-linux` (for 32-bit distributions). The output will be in
the `release/lib/deployed/jdk16/linux-amd64` and
`release/lib/deployed/jdk16/linux` directories.

MacOS
-----

Install the command line tools or the XCode IDE. From a shell, run the
script `buildnative-mac` . The output will be in the
`release/lib/deployed/jdk16/mac` directory. Note that it is not
possible to build or run 32-bit software on recent versions of macOS.

Freshen the Zip File
--------------------

The zip file is in the `external` subdirectory and its contents are
under `release/lib`. So on Linux and macOS it is sufficient to cd to the
`release` subdirectory and run `zip -fr ../external/[NAME-OF-ZIP] ./lib`
where the zip name should be
`profiler-external-binaries-[VERSION].zip`

Uploading
---------

To prepare the zip file for uploading, first calculate its SHA1 hash.
Next, edit the file `binaries-list` in the external subdirectory so
that it looks like:

    # Licensed to the Apache Software Foundation (ASF) under one
    # (rest of the Apache license)
    [HASH] profiler-external-binaries-[VERSION].zip

Then edit the license file
`profiler-external-binaries-[VERSION]-license.txt` (just the version
will change.)  Next, change the file name in `build.xml`, in the
`-process.release.files` task.  Finally rename the zip file to
`[HASH]-profiler-external-binaries-[VERSION].zip`. This needs to be
uploaded to https://netbeans.osuosl.org/binaries/ - a member of the
NetBeans development team can do this.
