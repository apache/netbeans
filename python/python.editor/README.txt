Python Editor
tor@netbeans.org, Jan 12 2009

This document is intended to be useful for developers working on the Python
support in NetBeans.

First, make sure you have visited
   http://wiki.netbeans.org/Python
which is the prominent place for information about the Python support.

All the modules in NetBeans related to Python development have
"python" as a directory prefix. In addition, there is the
"o.jython" and "o.jython.distro" modules which wrap Jython in
two different ways:
- o.jython wraps Jython for in-IDE usage as a parser. It is never
  used to execute Jython code (and therefore doesn't need the bundled
  Python libraries etc).
- o.jython.distro wraps Jython as a complete binary installation. This
  is used (by default) for executing Python code. Note however that
  users are free to point to their own Python installations, so
  o.jython.distro is -optional-.

BUILDING PYTHON

See the 
http://wiki.netbeans.org/NbPythonHgBuild
document for details.

Essentially, after building NetBeans, just run
$ ant -f python.editor/build-python.xml clean everything
to build all the Python support. 

(Perhaps Python will be enabled by default in the builds by the
time you read this. If so, just build NetBeans without any extra
flags and it should be included).

RUNNING TESTS

The continuous Python build already does this:
http://deadlock.netbeans.org/hudson/job/python/

It ends up calling the "runtests" target in build-python.xml
as explained above.

However, in practice, when working on the Python editor, just
right click on the project and choose "Test" in the project's
context menu which will run all the Python tests.

FIXING TESTS

Let's say you've changed the behavior, such that for example
the offsets in the AstOffsetTest have changed - now the
test is failing because the unit tests are asserting that
the pretty printed output of the datastructures match the
old recorded behavior. 

To fix this, -delete- the golden file in question and run the
test again. This will generate a new golden file based on the
current data structure. Running the test a second time should
now pass.   Make sure you also -diff- the golden file
and convince yourself that all changes in the file are
correct.

UPDATING JYTHON

Jython is used in two different ways:
1) As a Python platform to execute users code
2) As the internal parser for Python

These two are built separately; o.jython.distro contains
the (optional) Jython execution distribution, and o.jython
contains the (in IDE module only) Jython parser.

UPDATING JYTHON DISTRO

To update Jython, go to the
   o.jython.distro
directory. It has an "external/jython-2.5.zip" file. 

Note that this file is -not- under direct version control.
After you update it (described next), you need to update
the external/binaries-list file. It contains a line like this:

57EA08DD3FAE37FD757E8E25D9409B9CA9A56313 jython-2.5.zip

The number on the left is a hash of the contents of the
file. When you have updated this file the contents will
no longer match this key. You need to take your update
file and go to

http://hg.netbeans.org/binaries/upload

and upload the file. (Log in with your Mercurial user name
and commit password to get access to the page). Once you
have uploaded your new jython-2.5.zip file, it will display
a new hash value for you. Replace the entry in binaries-list
with this new value.

(((You can also generate the key yourself while testing things
out: I use a script like this:

#!/bin/sh
openssl dgst -sha1 $1 | awk '{ print toupper($2) }'

which will display the hash for the file named as an argument
to the script. NOTE NOTE NOTE: This is useful to update
binaries-list during development when you are updating the
bits, building everything, running tests etc. But
DON'T FORGET TO UPLOAD THE FILE WHEN YOU ARE DONE! )))


To update jython-2.5.zip, run the "buildJython.py" script
in o.jython.distro. Take a look at the file - it has a couple
of configurable parameters at the top - most notably the
Jython repository you want to grab a snapshot from.

You may want to grab a stable snapshot - until recently we used
https://jython.svn.sourceforge.net/svnroot/jython/tags/Release_2_5beta0/jython
for example. Or you may be wanting to grab a new trunk build to
pick up some bug fix:
https://jython.svn.sourceforge.net/svnroot/jython/trunk/jython

The script will check out the sources and zip them up into
external/. At this point you should update your binaries-list
as describe above.

UPDATING JYTHON PARSER

The Jython parser is currently using a separate version of Jython
than the one used for execution (in particular, we have a version
that can handle some Python 2.6 constructs - it can parse them,
but not exeute them!). This version is built manually by you.
The final result lives in o.jython/external/jython-parser.jar
which you can update by running the update_jython.sh script
in the same directory. It has some configurable parameters at
the top. Run the script, then rebuild the module, then
rebuild python.editor and run all test. Don't forget to also
upload the updated jython parser file as described under
updating the jython distro above.

If the python editor unit tests pass, great, check in your
updates. If not, proceed as described under FIXING TESTS above.

UPDATING THE INDEXED DATA

The Python Editor has a persistent store of information extracted
from the user's source code, libraries and python platform. The
format is determined by PythonIndexer.java in the python.editor
module. If you change PythonIndexer incompatibly, you also need
to update its version number (indicated by the getIndexVersion()
method). This will force a refresh when users use your new version
of the IDE and they open their sources -- if NetBeans doesn't
find data for the given version number, it will generate it.

Note however that we also have preindexed data. This is data which
has been indexed in advance, and the -binary result- zipped up.
This is the case for big files like pythonstubs-2_6_1.egg.
We don't want to index these for every user - it's done ahead,
by you, not during the build. If you change the index version,
you must update the preindexed data. Doing that is simple - run
the external/UPDATE.zsh script in python.editor. You may not
have zsh on your system, but the script is simple so you can read
through it and see the commands you have to issue. You will
basically run NetBeans with some special startup flags and 
parameters, and then when it is finished run some commands to
zip up its resulting pre-indexed data files and place them
into build directories.

The Python preindexed data lives in python.editor/external/preindexed.zip

UPDATING THE CORE LIBRARIES

We have code completion etc. for builtin Python libraries - these
have no corresponding .py files. For example, take the "int" data type.

To simulate support for these, I am including a file named
pythonstubs-2_6_1.egg. That's because the interfaces are based on the
Python 2.6.1 support.

Python 2.6 ships with .rst files which document many of the
functions and classes in Python. These are all included, and
indexed directly by PythonIndexer. 

Unfortunately, not all the APIs are included. For that reason, I am
also generating a file named stub_missing.rst. This includes
definitions for missing classes or missing attributes of classes
that are present. Unlike the existing .rst files, I generate these
files by running some Python code inside an interpreter, and
this code dumps out the available symbols it finds for some of
the key classes (using things like "dir(5.0)" to find the names 
of the attributes available on a float, and using "x=5.0; print x.__doc__"
to extract documentation to get signatures etc).

The Python script to achieve this is extract_rst.py in python.editor/external.
There is also a "dump_apis.py" script there which can be used
to quickly dump the set of available names for an interpreter.
I used that script, run on different versions of Python, to determine
which methods/attributes/functions were added in various versions,
and copied this data into the extract_rst.py script.

To generate pythonstubs-2_6_1.egg I downloaded Python 2.6.1, went to 
its Doc/library subdirectory, and zipped up all the .rst files there.
(Note also that I had copied in the stub_missing.rst file that I
generated as above. If updating to a new version of Python, you may
be able to just use the existing version from pythonstubs-2_6_1.egg.)
After zipping up the .rst files and stub_missing.rst (into the top
level folder) I copied this into python.editor external and 
updated the index (see UPDATING THE INDEXED DATA). To ensure that
when users update to this version of the IDE with a new
pythonstubs-2_6_1.egg get current data, make sure you
increase the index version described above.
