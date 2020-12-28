# Script which dumps out the signatures for core classes. Run on
# multiple versions of Python to discover API additions/removals.
# Can generate a list for extract_rst like this:
# diff 2.5 2.6 | grep ">" | awk 'BEGIN { printf "[" } {printf "\"" $2 "\", "} END { print "]"}'
# (where 2.5 and 2.6 are the output files from running this program on multiple platforms)
import sys
def dump(obj, class_name):
    for attr in dir(obj):
        print class_name + "." + attr

#print "# APIs in " + sys.version.split(" ")[0]

dump(5, "int")
dump(5.0, "float")
dump(5L, "long")
dump(1==1, "bool")
dump(complex(5, 1), "complex")
dump([], "list")
dump({}, "dict")
dump((1, 2), "tuple")
dump("s", "str")
dump(u"s", "unicode")

