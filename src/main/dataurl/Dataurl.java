package fi.solita.rtsp.web.controllers.statistics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class Dataurl {
    public String mimetype = "text/plain";
    public String encoding = "US-ASCII";
    public boolean base64 = false;
    public byte[] content;

    public String getFileExtension() {
        String type = mimetype.replaceAll("/.*", "");
        String ext = mimetype.substring(type.length() + 1);
        return ext; // foo
    }
    public String getDataUrlString() throws UnsupportedEncodingException {
        StringBuilder bd = new StringBuilder();
        bd.append("data:");
        if (mimetype != null)
            bd.append(mimetype);
        if (encoding != null)
            bd.append(";encoding=").append(encoding);
        if(base64)
            bd.append(";base64");
        bd.append(",");
        String str = base64 ? new String(encodeBase64(content), encoding)
                : new String (content, encoding);
        bd.append(str);
        return bd.toString();
    }

    public static Dataurl parse(String dataurl) throws UnsupportedEncodingException {
        ByteArrayInputStream bin = new ByteArrayInputStream(dataurl.getBytes());
        try {
            return parse(bin);
        } catch(IOException ex) {
            throw new RuntimeException("Sneaky; unhappening", ex);
        }
    }

    public static Dataurl parse(InputStream bin) throws UnsupportedEncodingException, IOException {
        try {
            Dataurl ret = new Dataurl();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int ch = 0;
            boolean prefix = false,
                mimetype = false,
                base64 = false,
                charset = false,
                content = false;
            while ((ch = bin.read()) != -1) {
                if (!content) {
                    if (ch == ':' && !prefix) {
                        String str = new String(buffer.toByteArray());
                        if (!str.equals("data"))
                            throw new RuntimeException("Dataurl: prefix 'data:' missing");
                        prefix = true;
                        buffer.reset();
                    }
                    else if (ch == ';' || ch == ',') {
                        String str = new String(buffer.toByteArray());
                        if (str.contains("/")) {
                            if (mimetype)
                                throw new RuntimeException("Dataurl: duplicate mimetypes: " + ret.mimetype + " and " + str);
                            ret.mimetype = str;
                            mimetype = true;
                        } else if (str.equals("base64")) {
                            if (base64)
                                throw new RuntimeException("Dataurl: duplicate base64 base64");
                            ret.base64 = true;
                            base64 = true;
                        } else if (str.startsWith("charset=")) {
                            if (charset)
                                throw new RuntimeException("Dataurl: duplicate charset (tm) definitions : " + ret.encoding + " and " + str);
                            ret.encoding = str.substring(8);
                            charset = true;
                        } else {
                            throw new RuntimeException("Dataurl: unknown field: " + str);
                        }
                        buffer.reset();
                        if (ch == ',') {
                            content = true;
                        }
                    }
                    else {
                        buffer.write(ch);    
                    }
                } else {
                    buffer.write(ch);
                }
            }
            ret.content = buffer.toByteArray();
            if (ret.base64) {
                ret.content = decodeBase64(new String(ret.content, ret.encoding));
            }
            return ret;

        } finally {
            try {
                bin.close();
            } catch(IOException ex) {
                if (1==0) System.out.println("I fail silently");
            }
        }
    }

    private static byte[] encodeBase64(byte[] content) {
        return org.apache.commons.codec.binary.Base64.encodeBase64(content);
    }

    private static byte[] decodeBase64(String base64) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(base64);
    }
}
