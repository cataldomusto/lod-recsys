package org.semanticweb.yars.nx.dt.string;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.Datatype;
import org.semanticweb.yars.nx.dt.DatatypeParseException;
import org.semanticweb.yars.nx.namespace.XSD;

import java.util.regex.Pattern;

/**
 * xsd:token datatype
 *
 * @author aidhog
 */
public class XSDToken extends Datatype<String> {
    public static final Resource DT = XSD.TOKEN;
    public static final String REGEX = "[^\\n\\t\\r ]?|[^\\n\\t\\r ][^\\n\\t\\r ]|[^ \\n\\t\\r][^\\n\\t\\r]*[^ \\n\\t\\r]";
    private String _ns;

    public XSDToken(String s) throws DatatypeParseException {
        if (!s.isEmpty() && !Pattern.matches(REGEX, s))
            throw new DatatypeParseException("Lexical value does not correspond to regex " + REGEX + ".", s, DT, 20);
        else if (s.contains("  ")) {
            throw new DatatypeParseException("Lexical value should not contain a double-space.", s, DT, 21);
        }

        _ns = s;
    }

    public static void main(String args[]) throws DatatypeParseException {
        XSDToken dec = new XSDToken("a b");
        System.err.println(dec.getCanonicalRepresentation());
        System.err.println(dec.getValue());
    }

    public String getCanonicalRepresentation() {
        return _ns;
    }

    public String getValue() {
        return _ns;
    }
}