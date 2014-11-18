
all: encrypted.enc

encrypted.tar.gz: ~/.m2/settings.xml
	tar Ccf ~/.m2 $@ settings.xml

encrypted.enc: encrypted.tar.gz
	openssl aes-256-cbc -k $(shell cat ~/Dropbox/seed-sha1) -in $< -out $@ -a
	-rm -f $<

clean:
	rm -f encrypted.*
