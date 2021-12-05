#!/bin/sh

###################################################################
# Auto-importer builder script
# 
# Generates a file in src/main/resources/autoimports containing
# fully-qualified class names for a set of files. To mark a batch
# of files for creation, create a ".autoimport" file in the 
# top-level directory you want to query. If only certain files
# should be matched, add a regex pattern to that .autoimport file.
# The name of the generated output file will be the path to the
# .autoimport file converted to a java classpath
# i.e. com/redfish/handlers/.autoimport => com.redfish.handlers
###################################################################

projectroot=$(git rev-parse --show-toplevel) # Use git to get project root
source_root=$projectroot/src/main/java # Source code directory (intended to be used for .java files)
merge_base=$(git merge-base @ origin/main) # Gets ID of last commit to main so we know what all has changed.
master_list=$projectroot/autoimport_list.txt # Holds list of .autoimport file locations
output_directory=$projectroot/src/main/resources/autoimports # Holds the generated import lists

for file in $(git diff $merge_base --name-only --diff-filter=ADR); do # Use git-diff --name-only to get list of changed files (ADR = added/deleted/renamed)
	if [ "$(basename $file)" = ".autoimport" ]; then # If the file is an ".autoimport" marker
		find $source_root -name ".autoimport" -printf "%P\n" > $master_list # Rebuild the master auto-import list
		break # And exit the loop
	fi
done

# If the auto-import list exists (may not if t)here are no .autoimport tags
if [ -f $master_list ]; then
	mkdir -p $output_directory # Create autoimport output directory

	while read importfile; do # Read each line from autoimport_lists.txt
		package=$(dirname $importfile) # Pull slash-separated package name (directory holding .autoimport file)
		path_to_package=$source_root/$package # Absolute path
		if [ -n "$(git diff $merge_base --name-only --diff-filter=ADR $path_to_package)" ]; then # If some file in that directory (or any subdirectory) was added/deleted/renamed
			valid_file_patterns=$(cat $source_root/$importfile) # Read and store search pattern from .autoimport file
			output_file=$output_directory/$(echo $package | sed -e s@/@.@g) # Sed replaces slashes with "."s (package com/redfish => com.redfish)
			find $path_to_package -name "$valid_file_patterns" -printf "$package/%P\n" | sed -e s@/@.@g | sed -e s/.java$// > $output_file # Use search pattern and parent directory to rebuild import list
		fi
	done < $master_list
fi