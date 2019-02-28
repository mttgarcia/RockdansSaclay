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
        public static final short SW_SOLDE_INSSUFISANT = 0x6301;
        static final short SW_PIN_VERIFICATION_REQUIRED = 0x6302;
        static final short SW_PIN_VERIFICATION_FAILED = 0x6303;
        //Taille
        private static final byte PIN_LENGTH = 0x02;
        private static final byte NOM_LENGTH = 0x0c;
        private static final byte PRENOM_LENGTH = 0x0c;
        private static final byte NUM_PARTICIPANT_LENGTH = 0x05;
        //PIN
        private static final byte PIN_TRY_LIMIT = 0x03;

        //Atribut
        private byte credit;
        private byte[] nom;
        private byte[] prenom;
        private OwnerPIN pin;

        private Participant(byte bArray[], short bOffset, byte bLength) {
            //Taille de l'aid
            byte aidLength = bArray[bOffset];
            //Control info (on s'en fout c'est 0 faut le mettre pour faire joli et propre)
            short controlLength = (short)(bArray[(short)(bOffset+1+aidLength)]&(short)0x00FF);
            //Taille des info 
            short dataLength = (short)(bArray[(short)(bOffset+1+aidLength+1+controlLength)]&(short)0x00FF);
            if ((byte)dataLength != (byte)(NUM_PARTICIPANT_LENGTH + PRENOM_LENGTH + NOM_LENGTH + PIN_LENGTH)) {
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
            prenom = new byte[(short)NOM_LENGTH];
            Util.arrayCopyNonAtomic(bArray,//source
                                        (short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH+NOM_LENGTH),//offset de source
                                        prenom,//dest
                                        (short)0,//offset de dest
                                        PRENOM_LENGTH);//Taille de la copie

            credit = 50;
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

            switch (buffer[ISO7816.OFFSET_INS]) {

                case INS_AFFICHER_CREDIT:
                    buffer[0] = credit;
                    apdu.setOutgoingAndSend((short) 0, (short) 1);
                    break;

                case INS_PAYER:
                    if(buffer[0] > credit) payer(apdu);
                    break;

                default:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        }

        public void payer(APDU apdu){
            byte[] buffer = apdu.getBuffer();
            apdu.setIncomingAndReceive(); //Récupere le buffer
            credit -= buffer[0];
            return;
        }
    }
