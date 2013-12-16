#!/bin/sh
echo "El siguiente script utiliza la herramienta keytool"
echo "para la generacion de claves publica y privada."

echo -n "Ingrese el nombre con el cual quiere guardar su keystore: "
read keystoreName
#keytool -genkeypair -alias sinapuli -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore "$keystoreName.jks"

echo -n "Ingrese el nombre con el cual quiere guardar su clave publica: "
read publicKeyName
#keytool -export -keystore "$keystoreName.jks" -alias sinapuli -file "$publicKeyName.cer"

echo -n "Ingrese el nombre y la ubicacion del archivo que quiere firmar : "
read docPath

echo -n "Ingrese el nombre y la ubicacion del keystore : "
read keystorePath

echo -n "Ingrese la password de su keystore: "
read password

echo -n "Ingrese el nombre y la ubicacion un archivo en donde se guardara la firma : "
read outputPath

#java SignDocument "$docPath" "$keystorePath" "$password" "$outputPath"
java SignDocument "$docPath" "$keystoreName.jks" "$password" "$outputPath"          
