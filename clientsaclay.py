from smartcard.System import readers
r=readers()

#Création de la connexion
connection=r[0].createConnection()

#Connexion à la java card
connection.connect()

#Valeur des variables dans le rocksaclay.java
CLASS_APPLET = 0xB0 	#Valeur de la classe de l'applet
INS_GET_NOM = 0x00 	#Valeur de NOM
INS_GET_PRENOM = 0x01	#Valeur de PRENOM
INS_GET_CREDIT = 0x02	#Valeur de crédit
INS_GET_NUMPARTICIPANT = 0x03	#Valeur du numéro du participant
INS_SET_NOM = 0x10 	#Set du NOM
INS_SET_PRENOM = 0x20	#Set du PRENOM
INS_SET_NUMPARTICIPANT = 0x30
NOM_LENGHT = 0x10
NUMPARTICIPANT_LENGHT = 0x08

#Selection AID
data, sw1, sw2 = connection.transmit([0x00,0xA4,0x04,0x00,0x08,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x09])
#0x00 CLA
#0xA4 INS
#P1 0x04 
#P2 0x00
#Lc 0x08 Taille de l'AID
#Data Contient l'AID

print (hex(sw1),hex(sw2))




#Variable à set : nom/prenom/numeroparticipant
#Variable incrémentée : credit 
