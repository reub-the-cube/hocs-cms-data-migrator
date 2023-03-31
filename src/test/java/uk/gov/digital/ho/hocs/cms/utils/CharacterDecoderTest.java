package uk.gov.digital.ho.hocs.cms.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class CharacterDecoderTest {

    CharacterDecoder characterDecoder;
    ByteBuffer byteBuffer;
    byte[] encodedBytes;

    @BeforeEach
    void setup() throws CharacterCodingException {
        characterDecoder = new CharacterDecoder();
        Charset charset = Charset.forName("windows-1252");
        CharsetEncoder encoder = charset.newEncoder();
        byteBuffer = encoder.encode(CharBuffer.wrap("a string\n"));
        encodedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encodedBytes);
    }

    @Test
    @DisplayName("Check system file encoding")
    void testSystemFileEncoding() {
        String encoding = System.getProperty("file.encoding");
        assertEquals("UTF-8", encoding);
    }

    @Test
    @DisplayName("Check system character encoding")
    void testSystemCharacterEncoding() {
        String encoding = Charset.defaultCharset().toString();
        assertEquals("UTF-8", encoding);
    }


    @Test
    @DisplayName("Decode special windows-1252 characters")
    void testDecodeSpecialWindowsChar() throws UnsupportedEncodingException, CharacterCodingException {
        byte[] encodedChars = new byte[] {(byte) 0xfb, (byte) 0xfc, (byte) 0xe6, (byte) 0xc6, (byte) 0xb6};
        String noConversion = encodedChars.toString();
        String conversion = (new String(encodedChars,0,encodedChars.length,"Windows-1252"));
        byte[] encodedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encodedBytes);
        String convertedChars = characterDecoder.decodeWindows1252Charset(encodedChars);
        assertEquals("ûüæÆ¶", convertedChars);
        assertEquals(conversion, convertedChars);
        assertNotEquals(convertedChars, noConversion);
    }

    @Test
    @DisplayName("Decode standard characters")
    void testDecodeStandardAsciiChars() throws CharacterCodingException {
        String convertedChars = characterDecoder.decodeWindows1252Charset(encodedBytes);
        assertEquals("a string\n", convertedChars);
    }
}