all:
	diff orig_file lnk_file
	echo "new line" >> orig_file
	diff orig_file lnk_file
	
	diff orig_dir/file_in_orig_dir lnk_dir/file_in_orig_dir
	echo "new line" >> orig_dir/file_in_orig_dir
	diff orig_dir/file_in_orig_dir lnk_dir/file_in_orig_dir

	diff orig_file abs_lnk_file
	echo "new line" >> orig_file
	diff orig_file abs_lnk_file

clean:
	
create_links:
	ln -s orig_file lnk_file
	ln -s orig_dir lnk_dir
	
remove_links:
	if [ -r lnk_file ]; then rm lnk_file; fi
	if [ -r lnk_dir ]; then rm lnk_dir; fi
