package org.tenwell.identity.core.saml2;

import org.apache.xml.security.c14n.Canonicalizer;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.validation.ValidationException;
import org.tenwell.identity.core.exception.IdentityException;

import javax.xml.namespace.QName;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SAMLSSOSigner {

    public static boolean validateXMLSignature(RequestAbstractType request, X509Credential credential) throws IdentityException {

        boolean isSignatureValid = false;

        if (request.getSignature() != null) {
            try {
                SignatureValidator validator = new SignatureValidator(credential);
                validator.validate(request.getSignature());
                isSignatureValid = true;
            } catch (ValidationException e) {
                throw new IdentityException("Signature Validation Failed for the SAML Assertion : Signature is " +
                                            "invalid.", e);
            }
        }
        return isSignatureValid;
    }

    public static SignableXMLObject setSignature(SignableXMLObject signableXMLObject, String signatureAlgorithm, String
            digestAlgorithm, X509Credential cred) throws IdentityException {

        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(cred);
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        KeyInfo keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        X509Data data = (X509Data) buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
        X509Certificate cert = (X509Certificate) buildXMLObject(X509Certificate.DEFAULT_ELEMENT_NAME);

        String value;
        try {
            value = org.apache.xml.security.utils.Base64.encode(cred.getEntityCertificate().getEncoded());
        } catch (CertificateEncodingException e) {
            throw new IdentityException("Error occurred while retrieving encoded cert", e);
        }

        cert.setValue(value);
        data.getX509Certificates().add(cert);
        keyInfo.getX509Datas().add(data);
        signature.setKeyInfo(keyInfo);

        signableXMLObject.setSignature(signature);
        ((SAMLObjectContentReference) signature.getContentReferences().get(0)).setDigestAlgorithm(digestAlgorithm);

        List<Signature> signatureList = new ArrayList<Signature>();
        signatureList.add(signature);

        MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(signableXMLObject);

        try {
            marshaller.marshall(signableXMLObject);
        } catch (MarshallingException e) {
            throw new IdentityException("Unable to marshall the request", e);
        }

        org.apache.xml.security.Init.init();
        try {
            Signer.signObjects(signatureList);
        } catch (SignatureException e) {
            throw new IdentityException("Error occurred while signing request", e);
        }

        return signableXMLObject;
    }

    /**
     * Builds SAML Elements
     *
     * @param objectQName
     * @return
     * @throws IdentityException
     */
    private static XMLObject buildXMLObject(QName objectQName) throws IdentityException {
        XMLObjectBuilder builder =
                org.opensaml.xml.Configuration.getBuilderFactory()
                        .getBuilder(objectQName);
        if (builder == null) {
            throw new IdentityException("Unable to retrieve builder for object QName " +
                                        objectQName);
        }
        return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(),
                                   objectQName.getPrefix());
    }
}
