/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.oauth.signature;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.cert.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;


/**
 * An OAuth signature method that implements RSA-SHA1.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class RSA_SHA1 implements OAuthSignatureMethod {

    public static final String NAME = "RSA-SHA1";

    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    
    private static final String KEY_TYPE = "RSA";

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE";

    public RSA_SHA1() {
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * Generates the RSA-SHA1 signature of OAuth request elements.
     *
     * @param elements the combined OAuth elements to sign.
     * @param secrets the secrets object containing the private key for generating the signature.
     * @return the OAuth signature, in base64-encoded form.
     * @throws InvalidSecretException if the supplied secret is not valid.
     */
    @Override
    public String sign(String elements, OAuthSecrets secrets) throws InvalidSecretException {
    
        Signature sig;

        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae);
        }

        byte[] decodedPrivKey;

        try {
            decodedPrivKey = Base64.decode(secrets.getConsumerSecret());
        }
        catch (IOException ioe) {
            throw new InvalidSecretException("invalid consumer secret");
        }

        KeyFactory keyf;

        try {
            keyf = KeyFactory.getInstance(KEY_TYPE);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae);
        }

        EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivKey);
        
        RSAPrivateKey rsaPrivKey;

        try {
            rsaPrivKey = (RSAPrivateKey) keyf.generatePrivate(keySpec);
        }
        catch (InvalidKeySpecException ikse) {
            throw new IllegalStateException(ikse);
        }

        try {
            sig.initSign(rsaPrivKey);
        }
        catch (InvalidKeyException ike) {
            throw new IllegalStateException(ike);
        }

        try {
            sig.update(elements.getBytes());
        }
        catch (SignatureException se) {
            throw new IllegalStateException(se);
        }

        byte[] rsasha1;

        try {
            rsasha1 = sig.sign();
        }
        catch (SignatureException se) {
            throw new IllegalStateException(se);
        }

        return Base64.encode(rsasha1);
    }

    /**
     * Verifies the RSA-SHA1 signature of OAuth request elements.
     *
     * @param elements OAuth elements signature is to be verified against.
     * @param secrets the secrets object containing the public key for verifying the signature.
     * @param signature base64-encoded OAuth signature to be verified.
     * @throws InvalidSecretException if the supplied secret is not valid.
     */
    @Override
    public boolean verify(String elements, OAuthSecrets secrets, String signature) throws InvalidSecretException {

        Signature sig;

        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae);
        }

        RSAPublicKey rsaPubKey = null;

        String tmpkey = secrets.getConsumerSecret();
        if (tmpkey.startsWith(BEGIN_CERT)) {
            try {
                Certificate cert = null;
                ByteArrayInputStream bais = new ByteArrayInputStream(tmpkey.getBytes());
                BufferedInputStream bis = new BufferedInputStream(bais);
                CertificateFactory certfac = CertificateFactory.getInstance("X.509");
                while (bis.available() > 0) {
                    cert = certfac.generateCertificate(bis);
                }
                rsaPubKey = (RSAPublicKey) cert.getPublicKey();
            } catch (IOException ex) {
                Logger.getLogger(RSA_SHA1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateException ex) {
                Logger.getLogger(RSA_SHA1.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }


        byte[] decodedSignature;

        try {
            decodedSignature = Base64.decode(signature);
        }
        catch (IOException ioe) {
            return false;
        }

        try {
            sig.initVerify(rsaPubKey);
        }
        catch (InvalidKeyException ike) {
            throw new IllegalStateException(ike);
        }

        try {
            sig.update(elements.getBytes());
        }
        catch (SignatureException se) {
            throw new IllegalStateException(se);
        }
        
        try {
            return sig.verify(decodedSignature);
        }
        catch (SignatureException se) {
            throw new IllegalStateException(se);
        }
    }
}
