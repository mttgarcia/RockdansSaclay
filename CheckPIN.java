package checkpin;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.JCSystem;
import javacard.framework.OwnerPIN;

public class CheckPIN extends Applet {

    public static final byte CLA_MONAPPLET = (byte) 0xB0;

    static final byte INS_CHECK_PIN = 0x00;
    static final byte INS_PRINT_SECRET = 0x01;
    static final byte INS_UPDATE_PIN = 0x02;
    static final byte INS_DEBUG = 0x03;

    private byte[] MESS_DEBUG = {'D','e','B','U','G',' '};
    
    private static final byte PIN_LENGTH = 0x02;
    private static final byte PIN_TRY_LIMIT = 0x03;
    private static final byte SECRET_LENGTH = 0x02;

    static final short SW_PIN_VERIFICATION_REQUIRED = 0x6301;
    static final short SW_PIN_VERIFICATION_FAILED = 0x6302;

    private static byte[] secret;
    private static OwnerPIN m_pin;
    
    //private byte[] buffer; BUG
        
    private CheckPIN(byte[] bArray, short bOffset, byte bLength) throws ISOException {
    	//Taille de l'aid
		byte aidLength = bArray[bOffset];
		//Control info (on s'en fout c'est 0 faut le mettre pour faire joli et propre)
		short controlLength = (short)(bArray[(short)(bOffset+1+aidLength)]&(short)0x00FF);
		//Taille des info
		short dataLength = (short)(bArray[(short)(bOffset+1+aidLength+1+controlLength)]&(short)0x00FF);
		
		//On vérifie que les paramètres mis à l'installation contiennent bien le pin et le secret
		if ((byte)dataLength != (byte)(PIN_LENGTH + SECRET_LENGTH)) {
	            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH );
		} 

		//gp -v --install CheckPIN221.cap --params 0102426f
		
		m_pin = new OwnerPIN(PIN_TRY_LIMIT, //Max d'essais de PIN
								PIN_LENGTH);//Taille du PIN
		//set un nouveau PIN
		m_pin.update(bArray, //tableau de byte contenant le nouveau PIN
					(short) (bOffset+1+aidLength+1+controlLength+1),//offset du tableau du nouveau PIN
					PIN_LENGTH); //Taille du PIN

		secret = new byte[(short)SECRET_LENGTH];
		Util.arrayCopyNonAtomic(bArray, //source
								(short) (bOffset+1+aidLength+1+controlLength+1+PIN_LENGTH),//offset de la source
								secret, //destination
								(short)0, //offset de la destination
								SECRET_LENGTH);//Taille de la copie
	
    }

    //byte[] bArray tableau contenant les paramètres d'installation
    //short bOffset l'offset du tableau
    //byte bLength Taille du tableau de paramètres d'installation (max 127)
    public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {

    	new CheckPIN(bArray, bOffset, bLength).register();
    }

    
    public void process(APDU apdu) throws ISOException {
    	//On récupére le buffer du apdu pour ensuite le modifier ou regarder ce qu'il y a dedans
		byte[] buffer = apdu.getBuffer();
	    
		if (this.selectingApplet()) return;
	    
	    //Si ce n'est pas le bon applet on sort une erreur
		if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
	            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		switch (buffer[ISO7816.OFFSET_INS]) {
		    
		case INS_DEBUG:
			//reset le pin
		    m_pin.reset();
			
		    Util.arrayCopyNonAtomic(MESS_DEBUG, //source
	                                    (short)0,//offset
	                                    buffer, //dest
	                                    (short)0,//offset
	                                    (short)MESS_DEBUG.length); //taille de la copie
		    Util.arrayCopyNonAtomic(secret, //source
	                                    (short)0,//offset
	                                    buffer,//dest
	                                    (short)MESS_DEBUG.length,//offset
	                                    (short)secret.length);//Taille de la copie
		    
		    //mettre le nombre essais de PIN possible dans le buffer juste après le message d'erreur et le secret
		    buffer[(short)(MESS_DEBUG.length+secret.length)] = (byte)m_pin.getTriesRemaining(); 
		    
		    //On envoie le buffer à l'offset 0 et de la taille du messafe de debut+secret+1
		    apdu.setOutgoingAndSend((short) 0, (short)(MESS_DEBUG.length+secret.length+1));

		    break;
		    
		case INS_CHECK_PIN:
			//Si le PIN est bon on sort
		    if (m_pin.isValidated()) {
				return;		    
		    }
		    
		    //Si le pin est bon on rentre dans le if
		    if (verify(apdu)) {
		    	//On met dans le buffer le nombre d'essais de PIN possible
				buffer[0] = m_pin.getTriesRemaining();
				//On envoie le buffer à l'offset 0 de la taille de 1 
				apdu.setOutgoingAndSend((short) 0, (short)1);
				return;
		    }

		    
		    ISOException.throwIt(SW_PIN_VERIFICATION_FAILED);
		    break;
		    
		case INS_PRINT_SECRET:

		    print_secret(apdu); 
		    break;

		default:
		    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
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
		boolean res = m_pin.check(buffer,//Le pin entré
								 (short) ISO7816.OFFSET_CDATA, //offset du pin
								 PIN_LENGTH);//La taille du PIN
		return res; //True or False
    }			


    static void print_secret(APDU apdu) throws ISOException {
    	//On modifie le buffer de l'apdu
		byte[] buffer = apdu.getBuffer();
		//Si le pin est valide on rentre dans le if
		if (m_pin.isValidated()) {
		    	
	    	    Util.arrayCopyNonAtomic(secret, //source
	    	    						(short)0, //offset source
	    	    						buffer, //dest
	    	    						(short)0, //offset dest
	    	    						SECRET_LENGTH); //taille de la copie
	    	    //Envoie du buffer à l'offset 0 de la taille du secret
	    	    apdu.setOutgoingAndSend((short) 0, (short) SECRET_LENGTH);
		    return;
	    	}
		//Si le pin n'est pas valide exception  
	    ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		
	    }
}
