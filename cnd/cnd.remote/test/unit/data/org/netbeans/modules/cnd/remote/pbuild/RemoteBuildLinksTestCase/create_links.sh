#!/bin/sh
cd makefile_proj_w_links
rm -rf lnk_file
ln -s orig_file lnk_file
rm -rf lnk_dir
ln -s orig_dir lnk_dir
rm -rf abs_lnk_file
ln -s `pwd`/lnk_file abs_lnk_file
cd ..
