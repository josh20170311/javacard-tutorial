package fr.bmartel.helloworld;

import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HelloWorldTest extends JavaCardTest {

//    private final static byte[] hello = {(byte) 0x48, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f};
    private final static byte[] hello = {'H', 'e', 'l', 'l', 'o'};
//    private final static byte[] world = {(byte) 0x57, (byte) 0x6f, (byte) 0x72, (byte) 0x6c, (byte) 0x64};
    private final static byte[] world = {'W', 'o', 'r', 'l', 'd'};
//    private final static byte[] specter =
//            {(byte) 0x53, (byte) 0x70, (byte) 0x65, (byte) 0x63, (byte) 0x74, (byte) 0x65, (byte) 0x72};
    private final static byte[] specter =
            {'S', 'p', 'e', 'c', 't', 'e', 'r'};
    private final static byte[] selected = {(byte) 0xa4};

    // >>> print(sha256("Hello Specter!".encode()).hexdigest())
    // a742085d2e645b66d09b7fee4ce9023e007c7c6b46ee7f1ba04efddf5ead5ed1
    // a7 42 08 5d 2e 64 5b 66 d0 9b 7f ee 4c e9 02 3e 00 7c 7c 6b 46 ee 7f 1b a0 4e fd df 5e ad 5e d1
    byte[] pythonHashedSpecterSHA256 = {
            (byte) 0xa7, (byte) 0x42, (byte) 0x08, (byte) 0x5d, (byte) 0x2e, (byte) 0x64, (byte) 0x5b, (byte) 0x66,
            (byte) 0xd0, (byte) 0x9b, (byte) 0x7f, (byte) 0xee, (byte) 0x4c, (byte) 0xe9, (byte) 0x02, (byte) 0x3e,
            (byte) 0x00, (byte) 0x7c, (byte) 0x7c, (byte) 0x6b, (byte) 0x46, (byte) 0xee, (byte) 0x7f, (byte) 0x1b,
            (byte) 0xa0, (byte) 0x4e, (byte) 0xfd, (byte) 0xdf, (byte) 0x5e, (byte) 0xad, (byte) 0x5e, (byte) 0xd1
    };


@BeforeClass
    public static void setup() throws CardException {
        TestSuite.setup();
    }

    @Test
    public void selectTest() throws CardException {
        CommandAPDU c = new CommandAPDU(0x00, 0xa4, 0x00, 0x00); // Select:  INS = 'a4'
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertArrayEquals(selected, response.getData());
        assertEquals((byte) -92, (byte) 0xa4); // java bytes are signed
    }

    @Test
    public void helloWorldTest() throws CardException {
        byte[] helloWorld = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!'};

        // send "World" ...
        CommandAPDU c = new CommandAPDU(0x00, 0x32, 0x00, 0x00, world, 12);
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        // get result "Hello World!"
        assertArrayEquals(helloWorld, response.getData());
        assertEquals("Hello World!", new String(response.getData(), StandardCharsets.US_ASCII));
    }

    @Test
    public void helloSpecterTest() throws CardException {
        // same with "Hello Specter!"
        byte[] helloSpecter = {'H', 'e', 'l', 'l', 'o', ' ', 'S', 'p', 'e', 'c', 't', 'e', 'r', '!'};

        CommandAPDU c = new CommandAPDU(0x00, 0x32, 0x00, 0x00, specter, 14);
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertArrayEquals(helloSpecter, response.getData());
        assertEquals("Hello Specter!", new String(response.getData(), StandardCharsets.US_ASCII));
    }

    @Test
    public void helloSpecterSHA256() throws CardException {
        // javacard.security.MessageDigest.ALG_SHA_256
        byte[] helloSpecter = {'H', 'e', 'l', 'l', 'o', ' ', 'S', 'p', 'e', 'c', 't', 'e', 'r', '!'};

        CommandAPDU c = new CommandAPDU(0x00, 0x42, 0x00, 0x00, helloSpecter, 32);
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] javaSeHashedSpecterSHA256 = digest.digest(
                "Hello Specter!".getBytes(StandardCharsets.UTF_8));
        System.out.println("javaSE:   " + new BigInteger(1, javaSeHashedSpecterSHA256).toString(16));
        System.out.println("python:   " + new BigInteger(1, pythonHashedSpecterSHA256).toString(16));
        System.out.println("javacard: " + new BigInteger(1, response.getData()).toString(16));
        assertArrayEquals(pythonHashedSpecterSHA256, response.getData());
        assertArrayEquals(javaSeHashedSpecterSHA256, pythonHashedSpecterSHA256);
    }
}
