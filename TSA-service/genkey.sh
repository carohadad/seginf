#!/bin/sh

keytool -genkeypair -alias tsa -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore tsa.jks -validity 365 -ext EKU:c=timeStamping -storepass garantito -dname cn=tsa-service,ou=garantito,o=uba,c=ar
