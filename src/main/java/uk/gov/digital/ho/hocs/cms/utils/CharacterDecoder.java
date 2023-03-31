package uk.gov.digital.ho.hocs.cms.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

@Component
@Slf4j
public class CharacterDecoder {

    public String decodeWindows1252Charset(byte[] bytes) {
        if (bytes == null) bytes = "".getBytes();
        Charset charset = Charset.forName("windows-1252");
        CharsetDecoder decoder = charset.newDecoder();
        String result = null;
        try {
            ByteBuffer bbuf = ByteBuffer.wrap(bytes);
            CharBuffer cbuf = decoder.decode(bbuf);
            result = cbuf.toString();
        } catch (CharacterCodingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
