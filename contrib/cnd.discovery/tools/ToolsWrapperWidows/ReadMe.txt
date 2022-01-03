Tool collection is a Cygwin with installed MinGW-64-32 tools.
I.e.:
- install Cygwin (or cygwin64).
- install additional packages mingw64-i686-gcc-core GCC for Win32 MinGW 64-32 (i686-w44-mingw32) toolchain (C) and
                              mingw64-i686-gcc-g++ GCC for Win32 MinGW 64-32 (i686-w44-mingw32) toolchain (C++)
In IDE:
- add Cygwin tool collection with name Cygwin-32
- change C and C++ tools on C:\cygwin64\bin\i686-w64-mingw32-gcc.exe and C:\cygwin64\bin\i686-w64-mingw32-g++.exe

