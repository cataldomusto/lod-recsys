package org.semanticweb.yars.nx.dt.binary;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.Datatype;
import org.semanticweb.yars.nx.dt.DatatypeParseException;
import org.semanticweb.yars.nx.namespace.XSD;

import java.util.regex.Pattern;

/**
 * Represents the xsd:hexBinary datatype
 *
 * @author aidhog
 */

public class XSDHexBinary extends Datatype<String> {
    public static final Resource DT = XSD.HEXBINARY;
    public static final String REGEX = "([0-9a-fA-F]{2})*";
    private String _h;

    public XSDHexBinary(String s) throws DatatypeParseException {
        if (!Pattern.matches(REGEX, s))
            throw new DatatypeParseException("Lexical value does not correspond to regex " + REGEX + ".", s, DT, 2);
        _h = s;
    }

    public static void main(String args[]) throws DatatypeParseException {
        XSDHexBinary hex = new XSDHexBinary("098acbcDF087123D");
        System.err.println(hex.getCanonicalRepresentation());
    }

    public String getValue() {
        return _h;
    }

    public String getCanonicalRepresentation() {
        return _h.toUpperCase();
    }
}
