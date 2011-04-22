#!/bin/sh

name=RSBot


echo Please close all instances of $name before continuing.
echo Note: this removes ALL account information, screenshots and scripts.


read -r -p "Remove all $name files from your system? [Y/n] " response

case $response in
    [yY][eE][sS]|[yY]) 
      echo $name will be deleted
      ;;
    *)
      echo Quitting...
      exit
      ;;
esac


echo Removing accounts files...

cd ~/

if [ -e ".rsbotacct" ]
  then
    rm -rf ".rsbotacct"
fi

echo Removing program directory...

if [ -d "$name" ]
  then
    rm -rf "$name"
fi

echo Completed.