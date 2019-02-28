from random import *
from ecdsa import SigningKey, NIST384p
import os

taille_name = 24
taille_numpart = 5

r = open("num_participant.txt","r")
num_part = r.read()
r.close()
w = open("num_participant.txt","w")
compteur = int(num_part)+1
if (len(num_part)<taille_numpart):
	for i in range(taille_numpart-len(num_part)):
		num_part = num_part+'0'

w.write(str(compteur))
r.close()
w.close()


pin = []
for i in range(4):
	alea = randint(0,9)
	pin.append(str(alea))

pin = ''.join(pin)
print 'Le pin de cette carte est le :',pin

print 'Entrez le nom :'
nom = raw_input()
nom = nom.encode("hex").replace("0x",'')
if (len(nom)<taille_name):
	for i in range(taille_name-len(nom)):
		nom = nom + '0'

#print nom
print 'Entrez le prenom :'
prenom = raw_input()
prenom = prenom.encode("hex").replace("0x",'')
if (len(prenom)<taille_name):
	for i in range(taille_name-len(prenom)):
		prenom = prenom + '0'

#print prenom

print pin+nom+prenom+num_part
os.system('gp -v --install Participant221.cap --params'+pin+nom+prenom+num_part) #Marche pas avec gp seulement avec les appels system, faut trouver autre chose
