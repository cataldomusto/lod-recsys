package org.semanticweb.yars.nx.dt.string;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.Datatype;
import org.semanticweb.yars.nx.dt.DatatypeParseException;
import org.semanticweb.yars.nx.namespace.XSD;

import java.util.regex.Pattern;

/**
 * xsd:normalizedString datatype
 *
 * @author aidhog
 */
public class XSDNormalisedString extends Datatype<String> {
    public static final Resource DT = XSD.NORMALIZEDSTRING;
    public static final String REGEX = "[^\\t^\\r^\\n]*";
    private String _ns;

    public XSDNormalisedString(String s) throws DatatypeParseException {
        if (s == null)
            throw new DatatypeParseException("Null value passed.", 0);

        if (!Pattern.matches(REGEX, s))
            throw new DatatypeParseException("Lexical value does not correspond to regex " + REGEX + ".", s, DT, 2);
        _ns = s;
    }

    public static void main(String args[]) throws DatatypeParseException {
        XSDNormalisedString dec = new XSDNormalisedString("");
        System.err.println(dec.getCanonicalRepresentation());
        System.err.println(dec.getValue());
    }

    public String getCanonicalRepresentation() {
        return _ns;
    }

    public String getValue() {
        return _ns.replace("\n", " ");
    }
}