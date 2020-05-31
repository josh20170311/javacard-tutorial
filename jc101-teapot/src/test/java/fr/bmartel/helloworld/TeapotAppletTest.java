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

import static org.junit.Assert.*;

public class TeapotAppletTest extends JavaCardTest {

    @BeforeClass
    public static void setup() throws CardException {
        TestSuite.setup();
    }

    @Test
    public void selectTest() throws CardException {
        // 00 A4 04 00 06B00B51 11CA0100
        byte[] aid = {
                (byte) 0x06, (byte) 0xb0, (byte) 0x0b, (byte) 0x51,
                (byte) 0x11, (byte) 0xca, (byte) 0x01, (byte) 0x00
        };
        CommandAPDU c = new CommandAPDU(0x00, 0xa4, 0x04, 0x00);
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
    }

    @Test
    public void getTest() throws CardException {
        // B0 A1 00 00 00
        CommandAPDU c = new CommandAPDU(0xb0, 0xa1, 0x00, 0x00, 0x00);
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        // 49 20 61 6D 20 61 20 74 65 61 70 6F 74 20 67 69 6D 6D 65 20 73 6F 6D 65 20 74 65 61 20 70 6C 7A
        byte[] expected = {
                (byte) 0x49, (byte) 0x20, (byte) 0x61, (byte) 0x6D, (byte) 0x20, (byte) 0x61, (byte) 0x20, (byte) 0x74,
                (byte) 0x65, (byte) 0x61, (byte) 0x70, (byte) 0x6F, (byte) 0x74, (byte) 0x20, (byte) 0x67, (byte) 0x69,
                (byte) 0x6D, (byte) 0x6D, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x6F, (byte) 0x6D, (byte) 0x65,
                (byte) 0x20, (byte) 0x74, (byte) 0x65, (byte) 0x61, (byte) 0x20, (byte) 0x70, (byte) 0x6C, (byte) 0x7A
        };
        byte[] exp = {
                'I', ' ', 'a', 'm', ' ', 'a', ' ', 't', 'e', 'a', 'p', 'o', 't', ' ',
                'g', 'i', 'm', 'm', 'e', ' ', 's', 'o', 'm', 'e', ' ', 't', 'e', 'a', ' ', 'p', 'l', 'z'
        };
        assertEquals("I am a teapot gimme some tea plz", new String(response.getData(), StandardCharsets.US_ASCII));
        assertArrayEquals(exp, response.getData());
        assertArrayEquals(expected, response.getData());
    }

    @Test
    public void putTest() throws CardException {
        byte[] ene = {
                'E', 'n', 'e', ' ', 'm', 'e', 'n', 'e', ' ', 'm', 'u', 'h', ' ',
                'u', 'n', 'd', ' ', 'r', 'a', 'u', 's', ' ', 'b', 'i', 's', 't', ' ', 'd', 'u'
        };
        // B0 A2 00 00 1d 456e65206d656e65206d756820756e6420726175732062697374206475
        CommandAPDU c = new CommandAPDU(0xb0, 0xa2, 0x00, 0x00, ene);
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());

        CommandAPDU c2 = new CommandAPDU(0xb0, 0xa1, 0x00, 0x00, 0x00);
        ResponseAPDU response2 = transmitCommand(c2);
        assertEquals(0x9000, response2.getSW());
        assertEquals("Ene mene muh und raus bist du", new String(response2.getData(), StandardCharsets.US_ASCII));
        assertArrayEquals(ene, response2.getData());

        // reset the applet to the teapot
        byte[] tea = {
                'I', ' ', 'a', 'm', ' ', 'a', ' ', 't', 'e', 'a', 'p', 'o', 't', ' ',
                'g', 'i', 'm', 'm', 'e', ' ', 's', 'o', 'm', 'e', ' ', 't', 'e', 'a', ' ', 'p', 'l', 'z'
        };
        CommandAPDU c3 = new CommandAPDU(0xb0, 0xa2, 0x00, 0x00, tea);
        ResponseAPDU response3 = transmitCommand(c3);
        assertEquals(0x9000, response3.getSW());
    }
}
