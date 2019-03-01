package part;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;

public class Participant extends Applet {

    public static final byte CLA_MONAPPLET = (byte) 0xB0;
    //Instruction
    static final byte INS_PAYER = 0x01;
    static final byte INS_AFFICHER_CREDIT = 0x02;
    static final byte INS_RECEVOIR = 0x03;
    //Exception
    static final short SW_SOLDE_INSSUFISANT = 0x6301;
    static final short SW_PIN_VERIFICATION_REQUIRED = 0x6302;
    static final short SW_PIN_VERIFICATION_FAILED = 0x6303;
    //Taille
    private static final byte PIN_LENGTH = 0x02;
    private static final byte NOM_LENGTH = 0x0c;
    private static final byte PRENOM_LENGTH = 0x0c;
    private static final byte NUM_PARTICIPANT_LENGTH = 0x05;
    // private static final byte PRIVATE_KEY_LENGTH = 0x20;
    // private static final byte SIGNATURE_CARTE_LENGTH "= 0x20;
    //PIN
    private static final byte PIN_TRY_LIMIT = 0x03;

    //Atribut
    private short credit;
    private static byte[] nom;
    private static byte[] prenom;
    private static byte[] num_participant;
    private static OwnerPIN pin;
    // private static byte[] signature_carte;
    // private static byte[] private_key;


    private Participant(byte bArray[], short bOffset, byte bLength) {
        //Taille de l'aid
        byte aidLength = bArray[bOffset];
        //Control info (on s'en fout c'est 0 faut le mettre pour faire joli et propre)
        short controlLength = (short)(bArray[(short)(bOffset+1+aidLength)]&(short)0x00FF);
        //Taille des info 
        short dataLength = (short)(bArray[(short)(bOffset+1+aidLength+1+controlLength)]&(short)0x00FF);
        if ((byte)dataLength != (byte)(PIN_LENGTH + PRENOM_LENGTH + NOM_LENGTH + NUM_PARTICIPANT_LENGTH)){//+SIGNATURE_CARTE_LENGTH+PRIVATE_KEY_LENGTH)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH );
        } 

        //Initialisation du PIN
        pin = new OwnerPIN(PIN_TRY_LIMIT, //Max d'essais de PIN
                                PIN_LENGTH);//Taille du PIN
        pin.update(bArray,//Tab
                (short) (bOffset+1+aidLength+1+controlLength+1),//Offset
                PIN_LENGTH);//Taille du pin

        //Initialisation du nom
        nom = new byte[(short)NOM_LENGTH];
        Util.arrayCopyNonAtomic(bArray,//source
                                    (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH),//offset de source
                                    nom,//dest
                                    (short)0,//offset de dest
                                    NOM_LENGTH);//Taille de la copie

        //Initialisation du prenom
        prenom = new byte[(short)PRENOM_LENGTH];
        Util.arrayCopyNonAtomic(bArray,//source
                                    (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH),//offset de source
                                    prenom,//dest
                                    (short)0,//offset de dest
                                    PRENOM_LENGTH);//Taille de la copie

        //Initialisation du numero participant
        num_participant = new byte[(short)NUM_PARTICIPANT_LENGTH];
        Util.arrayCopyNonAtomic(bArray,//source
                                    (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH+PRENOM_LENGTH),//offset de source
                                    prenom,//dest
                                    (short)0,//offset de dest
                                    NUM_PARTICIPANT_LENGTH);//Taille de la copie

        // //Initialisation de la signature de la carte
        // signature_carte = new byte[(short)SIGNATURE_CARTE_LENGTH];
        // Util.arrayCopyNonAtomic(bArray,//source
        //                             (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH+PRENOM_LENGTH+NUM_PARTICIPANT_LENGTH),//offset de source
        //                             signature_carte,//dest
        //                             (short)0,//offset de dest
        //                             SIGNATURE_CARTE_LENGTH);//Taille de la copie 

        // //Initialisation de la clé privée
        // private_key = new byte[(short)PRIVATE_KEY_LENGTH];
        // Util.arrayCopyNonAtomic(bArray,//source
        //                             (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH+PRENOM_LENGTH+NUM_PARTICIPANT_LENGTH+SIGNATURE_CARTE_LENGTH),//offset de source
        //                             private_key,//dest
        //                             (short)0,//offset de dest
        //                             PRIVATE_KEY_LENGTH);//Taille de la copie  

        credit = 500;


    }

    public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
        new Participant(bArray,bOffset,bLength).register();
    }

    public void process(APDU apdu) throws ISOException {

        byte[] buffer = apdu.getBuffer();

        if (this.selectingApplet()) return;

        if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        if (verify(apdu)){
            switch (buffer[ISO7816.OFFSET_INS]) {

                case INS_AFFICHER_CREDIT:
                    buffer[0] = (byte)(credit>>8);
                    buffer[1] = (byte)(credit&(short)0x00FF);
                    apdu.setOutgoingAndSend((short) 0, (short) 2);
                    break;

                case INS_PAYER:
                    if(buffer[0] > credit) payer(apdu);
                    break;

                case INS_RECEVOIR:
                    //recevoir(apdu);

                default:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        }
    }

    public void payer(APDU apdu){
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive(); //Récupere le buffer
        credit -= buffer[0];
        return;
    }

    static boolean verify(APDU apdu) throws ISOException {
        //On récupére le buffer du apdu et on le met dans cette variable
        byte[] buffer = apdu.getBuffer();
        //Si le PIN est plus grand que demander on sort une erreur
        if (buffer[ISO7816.OFFSET_LC] != PIN_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH );
        }
        //On récupère ce que le buffer de l'apdu a
        apdu.setIncomingAndReceive(); //Récupere le buffer
        //True si le PIN est valide
        boolean res = pin.check(buffer,//Le pin entré
                                 (short) ISO7816.OFFSET_CDATA, //offset du pin
                                 PIN_LENGTH);//La taille du PIN
        return res; //True or False
    }
}
