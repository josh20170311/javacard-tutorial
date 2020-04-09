package fr.bmartel.helloworld;

import javacard.framework.*;
import javacard.security.MessageDigest;

public class HelloWorld extends Applet {

    private static final byte[] hello = {'H', 'e', 'l', 'l', 'o'};
    private static final byte[] space = {' '};
    private static final byte[] exclamationMark = {'!'};

    private static final byte INS_HELLO  = 0x32;
    private static final byte INS_SHA256 = 0x42;

    private static byte[] selected;

    public static void install(byte[] buffer, short offset, byte length) {
        (new HelloWorld()).register();
    }

    public void process(APDU apdu) {
        short len = apdu.setIncomingAndReceive();
        byte[] buf = apdu.getBuffer();

        switch (buf[ISO7816.OFFSET_INS]) {
            // select
            case ISO7816.INS_SELECT:
                Util.arrayCopy(selected, (byte) 0, buf, ISO7816.OFFSET_CDATA, (byte) 1);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA, (byte) 1);
                break;
             // hello + received bytes
            case INS_HELLO:
                byte[] response = new byte[ (short) (hello.length + len + 2)]; // 'hello' + ' ' + incoming payload + '!'
                // TODO check lengths for buffer overflows!
                // TODO write some convenience methods
                Util.arrayCopy(hello, (byte) 0, response, (byte) 0, (byte) hello.length);
                Util.arrayCopy(space, (byte) 0, response, (byte) hello.length, (byte) space.length);
                Util.arrayCopy(buf, ISO7816.OFFSET_CDATA, response, (byte) (hello.length + 1), (byte) len);
                Util.arrayCopy(exclamationMark, (byte) 0, response, (byte) (hello.length + 1 + len), (byte) exclamationMark.length);
                Util.arrayCopy(response, (byte) 0, buf, ISO7816.OFFSET_CDATA, (byte) (hello.length + 2 + len));
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA, (byte) (hello.length + 2 + len));
                break;
            // MessageDigest.ALG_SHA_256
            case INS_SHA256:
                // TODO error handling, e.g. what to do in case of CryptoException thrown?
                // get data from incoming
                byte[] cleartext = new byte[len];
                Util.arrayCopy(buf, ISO7816.OFFSET_CDATA, cleartext, (byte) 0, (byte) len);
                // hash it
                byte[] ciphertext = new byte[32];
                MessageDigest md = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
                md.doFinal(cleartext, (byte) 0, len, ciphertext, (byte) 0);
                // send outgoing
                Util.arrayCopy(ciphertext, (byte) 0, buf, ISO7816.OFFSET_CDATA, (byte) 32);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA, (byte) 32);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    // run on Applet select, i.e. INS = 'a4'
    public boolean select() {
        selected = new byte[]{(byte) 0xa4};
        return true;
    }
}
