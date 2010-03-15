#!/bin/sh

if [ -z $1 ]
	then
		echo "Usage: install.sh target_directory"
		echo "Example: install.sh /usr/lib/dssh"
		exit
	fi

DESTDIR=$1

if [ -d dist ]
	then
		echo "Installing dssh from current directory to $DESTDIR"
	else
	if [ -d ../dist ]
		then
		echo "Installing dssh from parent directory to $DESTDIR"
		cd ..
		fi
	fi



mkdir -p $DESTDIR
cp -r dist jniconsole $DESTDIR
mkdir -p $DESTDIR/scripts
cp -r scripts/dssh* $DESTDIR/scripts/

mkdir -p /etc/dssh
if [ \! -e /etc/dssh/dssh.opts ]
	then
	echo "Creating /etc/dssh/dssh.opts"
	echo "export DSSHHOME=\"$DESTDIR\"" > /etc/dssh/dssh.opts
	if [ -e /System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin/java ]
		then
	echo 'export JAVABIN="/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin/java"' >> /etc/dssh/dssh.opts
		else
	echo 'export JAVABIN="java"' >> /etc/dssh/dssh.opts
	fi
	echo 'export JAVAOPTS="-Djava.library.path=$DSSHHOME/jniconsole/bin/"' >> /etc/dssh/dssh.opts
	echo 'export KEYSTOREOPTS="-Djavax.net.ssl.keyStore=$HOME/.dssh/keystore -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=$HOME/.dssh/keystore -Djavax.net.ssl.trustStorePassword=123456"' >> /etc/dssh/dssh.opts
	fi

. /etc/dssh/dssh.opts

if $JAVABIN -version 2>&1 |egrep -i "java version \"1\.[45]\." > /dev/null
        then
                echo "WARNING: You need Java 1.6, you have:"; echo
                $JAVABIN -version
		echo "You need to tweak /etc/dssh/dssh.opts to point to your Java 1.6 installation"
        fi


echo "Now you can either add $DESTDIR/scripts to your path or execute something like:"
echo "ln -s $DESTDIR/scripts/dssh $DESTDIR/scripts/dssh-create-keystore \\"
echo "   $DESTDIR/scripts/dssh-add $DESTDIR/scripts/dssh-agent /usr/local/bin"
echo

echo "Each user should start by running dssh-create-keystore script."
echo "Enjoy using DSSH!"
