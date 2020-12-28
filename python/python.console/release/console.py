from optparse import OptionParser
parser = OptionParser()
parser.add_option("-p", "--plain",
                  action="store_true", dest="plain", default=False,
                  help="don't Use IPython for Interactive console")
(options, args)= parser.parse_args()
print( "NbPython console")
use_plain = options.plain
try:
    if use_plain:
        raise ImportError
    # Ipython console
    import IPython
    # Explicitly pass an empty list as arguments, because otherwise IPython
    # would use sys.argv from this script.
    shell = IPython.Shell.IPShell(argv=[])
    shell.mainloop()
except ImportError:
  # Plan Python console
  import code
  code.interact()
  
