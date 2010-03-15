#!/bin/sh

VERSION=`grep -A 1 'public static String getVersion()' src/dssh/Main.java | tail -n 1|sed -e 's/.*return "\(.*\)";.*/\1/'`
echo "Building package for dssh version ${VERSION}"
DESTDIR=/tmp/dssh-${VERSION}
DESTDIRWITHSRC=/tmp/dssh-${VERSION}-src

if [ -d dist ]
	then
		echo "Creating dssh from current directory to $DESTDIR"
	else
	if [ -d ../dist ]
		then
		echo "Creating dssh from parent directory to $DESTDIR"
		cd ..
		fi
	fi



mkdir -p $DESTDIR
cp -r dist $DESTDIR
rm -f $DESTDIR/dist/README.txt
mkdir -p $DESTDIR/doc
cp -r doc/introduction.* $DESTDIR/doc
cp -r doc/dssh-example.bsh* $DESTDIR/doc
mkdir -p $DESTDIR/jniconsole/bin
cp -r jniconsole/bin/* $DESTDIR/jniconsole/bin/
mkdir -p $DESTDIR/scripts
cp -r scripts/dssh* $DESTDIR/scripts/
cp -r scripts/install.sh $DESTDIR/scripts/

# create source version
mkdir -p $DESTDIRWITHSRC
cp -r * $DESTDIRWITHSRC
rm -fr $DESTDIRWITHSRC/dist/README.txt $DESTDIRWITHSRC/build
cp -r ../trilead-ssh/patches $DESTDIRWITHSRC/trilead-ssh-patches
find $DESTDIRWITHSRC -type d -name .svn -exec rm -rf {} \; > /dev/null 2>&1

cd /tmp
tar czf dssh-${VERSION}.tar.gz dssh-${VERSION}
tar czf dssh-${VERSION}-src.tar.gz dssh-${VERSION}-src
rm -rf /tmp/dssh-${VERSION} /tmp/dssh-${VERSION}-src



ls -l /tmp/dssh-${VERSION}.tar.gz /tmp/dssh-${VERSION}-src.tar.gz
