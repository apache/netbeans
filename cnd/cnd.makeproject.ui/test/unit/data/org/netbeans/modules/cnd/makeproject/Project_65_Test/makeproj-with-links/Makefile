bin_file=makeproj-with-links-bin

all:
	cc -g -c makeproj-with-links-main.c
	cc -g -c real_dir1/file2.c
	cc -o ${bin_file} makeproj-with-links-main.o file2.o
clean:
	rm -rf *.obj ${bin_file}