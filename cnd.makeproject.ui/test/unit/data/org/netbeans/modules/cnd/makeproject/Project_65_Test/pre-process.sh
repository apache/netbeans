#!/bin/sh
cd makeproj-with-links/real_dir1
rm file2.c
ln -s ../real_dir2/file2.c file2.c
