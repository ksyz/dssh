all:
	echo "Usage: make [linux|linux_amd64|osx|osx64|freebsd7|solaris]"
clean:
	rm -f jniconsole.o ttysettings.o

osx:
	g++ -dynamiclib -DMACOSX -lgcc -o bin/libjniconsole-x86-macosx.jnilib -lcurses -I/Library/Java/Home/include jniconsole.cpp ttysettings.cpp

linux:
	g++ -shared -lcurses -DLINUX -I/usr/lib/jvm/java-6-sun/include/linux -I/usr/lib/jvm/java-6-sun/include -L/usr/lib/jvm/java-6-sun/lib -o bin/libjniconsole-x86-linux.so  jniconsole.cpp ttysettings.cpp

linux_amd64:
	g++ -shared -lcurses -DLINUX_amd64 -fPIC -I/usr/lib/jvm/java-6-sun/include/linux -I/usr/lib/jvm/java-6-sun/include -L/usr/lib/jvm/java-6-sun/lib -o bin/libjniconsole-amd64-linux.so  jniconsole.cpp ttysettings.cpp

osx64:
	g++ -m64 -dynamiclib -DMACOSX -lgcc -o bin/libjniconsole-amd64-macosx.dylib -lcurses -I/opt/jdk6_devpreview_r1/include  -I/opt/jdk6_devpreview_r1/include/darwin jniconsole.cpp ttysettings.cpp

freebsd7:
	g++ -shared -DFREEBSD -o bin/libjniconsole-x86-freebsd-7.so -lcurses -I/usr/local/jdk1.6.0/include -I/usr/local/jdk1.6.0/include/freebsd  jniconsole.cpp ttysettings.cpp

osol: solaris

solaris:
	g++ -c -shared -DSOLARIS -I/usr/jdk/latest/include -I/usr/jdk/latest/include/solaris/ jniconsole.cpp ttysettings.cpp
	gld -shared -o bin/libjniconsole-x86-solaris.so -lcurses  -lstdc++ *.o

