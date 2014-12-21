package org.semanticweb.yars.nx.dt.numeric;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.Datatype;
import org.semanticweb.yars.nx.dt.DatatypeParseException;
import org.semanticweb.yars.nx.namespace.XSD;

import java.util.regex.Pattern;

/**
 * xsd:byte datatype
 *
 * @author aidhog
 */
public class XSDByte extends Datatype<Byte> {
    public static final Resource DT = XSD.BYTE;
    public static final String REGEX = "[+-]?[0-9]*";
    private byte _b;

    public XSDByte(String s) throws DatatypeParseException {
        if (s == null || s.isEmpty())
            throw new DatatypeParseException("Null value passed.", 0);

        if (!Pattern.matches(REGEX, s))
            throw new DatatypeParseException("Lexical value does not correspond to regex " + REGEX + ".", s, DT, 20);

        try {
            if (!s.startsWith("+"))
                _b = Byte.parseByte(s);
            else _b = Byte.parseByte(s.substring(1));
        } catch (NumberFormatException e) {
            throw new DatatypeParseException("Error parsing byte: " + e.getMessage() + ".", s, DT, 21);
        }
    }

    public static void main(String args[]) throws DatatypeParseException {
        XSDByte dec = new XSDByte("-67");
        System.err.println(dec.getCanonicalRepresentation());
    }

    public String getCanonicalRepresentation() {
        return Byte.toString(_b);
    }

    public Byte getValue() {
        return _b;
    }
}