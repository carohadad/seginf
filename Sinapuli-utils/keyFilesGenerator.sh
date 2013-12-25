#!/bin/bash
echo "El siguiente script utiliza herramientas java, tanto para"
echo "la generacion de claves publica y privada, como para su procesamiento."

echo ""
echo "Scripts para Manejo de claves en Sinapuli: "
echo "_________________________________________"
echo ""


genKeystore(){

	echo -n "Ingrese el nombre de nueva su keystore (se guardara como jks): "
	read keystoreName
	keytool -genkeypair -alias sinapuli -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore "$keystoreName.jks"

}


printCertificate(){

	echo -n "Ingrese el nombre del keystore: "
	read keystoreName
	keytool -keystore "$keystoreName.jks" -alias sinapuli -export -rfc

}



hashDocument(){

	echo -n "Ingrese el nombre del documento: "
	read docName
	echo -n "Ingrese el nombre del archivo de salida (se guardara como txt): "
	read outputName

	java HashDocument "$docName" "$outputName.txt"		

	echo "-------------------------------------------"
	echo "hash: "
	echo "-------------------------------------------"
	echo ""
        cat "$outputName.txt"
	echo ""
	echo ""
	echo ""

}

checkHashDocument(){

	echo -n "Ingrese el nombre del documento: "
	read docName
	echo -n "Ingrese el nombre del archivo con el hash: "
	read outputName

	java CheckHashDocument "$docName" "$outputName"
}


genSig(){

	echo -n "Ingrese el nombre del archivo de hash a firmar: "
	read nameOfFileToSign
	echo -n "Ingrese el nombre de su archivo keystore: "
	read keystore
	echo -n "Ingrese la password para el keystore: "
	read password
	echo -n "Ingrese el nombre del archivo de salida para la firma (se guardara como txt): "
	read sign
	echo -n "Ingrese el nombre del archivo de salida para su clave publica: "
	read publicKey

	java GenSig "$nameOfFileToSign" "$keystore" "$password" "$sign.txt" "$publicKey"
	
	echo "-------------------------------------------"
	echo "firma: "
	echo "-------------------------------------------"
	echo ""
        cat "$sign.txt"
	echo ""
	echo ""
	echo ""
}

verSig(){

	echo -n "Ingrese el nombre del archivo con la clave publica: "
	read publickeyfile
	echo -n "Ingrese el nombre del archivo firmado: "
	read signaturefile
	echo -n "Ingrese el nombre del archivo con el Hash a verificar: "
	read datafile

	java VerSig "$publickeyfile" "$signaturefile" "$datafile"

}


PS3="Seleccione una opcion: "
options=(
"Quiero generar mi keystore" 
"Quiero imprimir mi clave pública"
"Quiero Hashear un documento" 
"Quiero comparar un documento con un hash" 
"Quiero firmar un Hash" 
"Quiero verificar la firma de un Hash" 
"Salir")


select opt in "${options[@]}"
do
    case $opt in
        "Quiero generar mi keystore" )
            echo "eligio la opcion 1"
            genKeystore ;;
        "Quiero imprimir mi clave pública" )
            echo "eligio la opcion 2"
            printCertificate ;;
        "Quiero Hashear un documento" )
            echo "eligio la opcion 3"
            hashDocument ;;
        "Quiero comparar un documento con un hash" )
            echo "eligio la opcion 4"
            checkHashDocument ;;
        "Quiero firmar un Hash" )
            echo "eligio la opcion 5"
            genSig ;;
        "Quiero verificar la firma de un Hash" )
            echo "eligio la opcion 6"
            verSig ;;
        "Salir")
            break
            ;;
        *) echo invalid option;;
    esac
done
