package org.semanticweb.yars.nx.dt.numeric;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.Datatype;
import org.semanticweb.yars.nx.dt.DatatypeParseException;
import org.semanticweb.yars.nx.namespace.XSD;

import java.util.regex.Pattern;

/**
 * xsd:short datatype
 *
 * @author aidhog
 */
public class XSDShort extends Datatype<Short> {
    public static final Resource DT = XSD.SHORT;
    public static final String REGEX = "[+-]?[0-9]*";
    private short _s;

    public XSDShort(String s) throws DatatypeParseException {
        if (s == null || s.isEmpty())
            throw new DatatypeParseException("Null value passed.", 0);

        if (!Pattern.matches(REGEX, s))
            throw new DatatypeParseException("Lexical value does not correspond to regex " + REGEX + ".", s, DT, 2);

        try {
            if (!s.startsWith("+"))
                _s = Short.parseShort(s);
            else _s = Short.parseShort(s.substring(1));
        } catch (NumberFormatException e) {
            throw new DatatypeParseException("Error parsing short: " + e.getMessage() + ".", s, DT, 4);
        }
    }

    public static void main(String args[]) throws DatatypeParseException {
        XSDShort dec = new XSDShort("-32767");
        System.err.println(dec.getCanonicalRepresentation());
    }

    public String getCanonicalRepresentation() {
        return Short.toString(_s);
    }

    public Short getValue() {
        return _s;
    }
}