# NetBeans wrapper script around coverage.py:
# We run coverage.py, and when it is done, postprocess its
# output to write it in a simple format we can read (the native
# data written by coverage.py is marshalled python binary data structures
# which we don't want to parse directly since they are highly tied to
# the specific interpreter implementation.)
# TODO: Consider using metaprogramming to hook into coverage.py at a deeper level
# and directly modify the dumper routine such that we can emit the data
# in our desired format in the first place rather than writing, reading and
# rewriting as we're doing right now. This would also let us record some more
# information we'd be interested in, such as the inferred statement count.

import sys
import atexit

if len(sys.argv) < 4:
    sys.exit()

input = sys.argv.pop(1)
output = sys.argv.pop(1)
coverage_py_file   = sys.argv.pop(1)

# Shutdown hook: Convert coverage.py data to a suitable format for NetBeans
@atexit.register
def convert_to_nb_format():
    import marshal

    output_file = open(output, 'wb')

    c1_dict = marshal.load(open(input, 'rb'))

    for k in c1_dict.keys():
        output_file.write(k.__str__())
        output_file.write('\n')
        x = c1_dict.get(k)
        output_file.write(x.keys().__str__())
        output_file.write('\n')

    output_file.close()


# Run coverage.py
execfile(coverage_py_file)
