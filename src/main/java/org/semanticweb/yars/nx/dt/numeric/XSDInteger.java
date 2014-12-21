package org.semanticweb.yars.nx.dt.numeric;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.Datatype;
import org.semanticweb.yars.nx.dt.DatatypeParseException;
import org.semanticweb.yars.nx.namespace.XSD;

import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * xsd:integer datatype
 *
 * @author aidhog
 */
public class XSDInteger extends Datatype<BigInteger> {
    public static final Resource DT = XSD.INTEGER;
    public static final String REGEX = "[+-]?[0-9]*";
    private BigInteger _bi;

    public XSDInteger(String s) throws DatatypeParseException {
        if (s == null || s.isEmpty())
            throw new DatatypeParseException("Null value passed.", 0);

        if (!Pattern.matches(REGEX, s))
            throw new DatatypeParseException("Lexical value does not correspond to regex " + REGEX + ".", s, DT, 20);

        try {
            if (!s.startsWith("+"))
                _bi = new BigInteger(s);
            else _bi = new BigInteger(s.substring(1));
        } catch (NumberFormatException e) {
            throw new DatatypeParseException("Error parsing BigInteger: " + e.getMessage() + ".", s, DT, 21);
        }
    }

    public static void main(String args[]) throws DatatypeParseException {
        XSDInteger dec = new XSDInteger("-1876");
        System.err.println(dec.getCanonicalRepresentation());
    }

    public String getCanonicalRepresentation() {
        return _bi.toString();
    }

    public BigInteger getValue() {
        return _bi;
    }
}