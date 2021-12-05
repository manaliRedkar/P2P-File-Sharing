#!/bin/sh
# ^ Lets you know this is a shell script

# -z checks if a string is empty
# "$(which mvn)" stores the filepath of the mvn executable to a string, or creates an empty string if no executable is found.
# Put together we get this comand checks if the filepath for the mvn executabl exists.
if [ -z "$(which mvn)" ]
then
	brew install maven
else
	echo "Maven is installed (run 'brew install mvn' if this is not true)."
fi