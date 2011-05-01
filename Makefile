CC=javac
CFLAGS=-g -Xlint:deprecation
SRC=src
LIB=lib
RES=resources
BINDIR=bin
LSTF=temp.txt
IMGDIR=$(RES)/images
MANIFEST=$(RES)/Manifest.txt
VERSIONFILE=$(RES)/version.txt
VERSION=`cat $(VERSIONFILE)`
SCRIPTS=scripts
NAME=RSBot
DIST=RSBot.jar
ACCOUNTS=$(HOME)/.$(shell echo $(NAME) | tr '[A-Z]' '[a-z]')acct
INSTALLDIR=$(HOME)/$(NAME)

.PHONY: all Bot Scripts mostlyclean clean remove

all: Bundle

Bot:
	@if [ ! -d $(BINDIR) ]; then mkdir $(BINDIR); fi
	$(CC) $(CFLAGS) -d $(BINDIR) `find $(SRC) -name \*.java`

Scripts: mostlyclean Bot
	$(CC) $(CFLAGS) -cp $(BINDIR) $(SCRIPTS)/*.java
	
Bundle: Scripts
	@rm -fv $(LSTF)
	@cp $(MANIFEST) $(LSTF)
	@echo "Specification-Version: \"$(VERSION)\"" >> $(LSTF)
	@echo "Implementation-Version: \"$(VERSION)\"" >> $(LSTF)
	@if [ -e $(DIST) ]; then rm -fv $(DIST); fi
	jar cfm $(DIST) $(LSTF) -C $(BINDIR) . $(VERSIONFILE) $(IMGDIR)/* $(RES)/*.bat $(RES)/*.sh
	@rm -f $(LSTF)

mostlyclean:
	@rm -fv $(SCRIPTS)/*.class

clean: mostlyclean
	@rm -fv $(DIST)
	@rm -rfv $(BINDIR)

remove:
	@if [ -e "$(ACCOUNTS)" ]; then rm -fv "$(ACCOUNTS)"; fi
	@if [ -d "$(INSTALLDIR)" ]; then rm -rfv "$(INSTALLDIR)"; fi
