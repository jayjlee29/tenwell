package org.tenwell.identity.core.saml2;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SAMLEntityResolver implements EntityResolver {

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
            IOException {
        throw new SAXException("SAML request contains invalid elements. Possible XML External Entity (XXE) attack.");
    }


}
