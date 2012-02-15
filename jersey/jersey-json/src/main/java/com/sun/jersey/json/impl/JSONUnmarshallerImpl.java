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

package com.sun.jersey.json.impl;

import com.sun.jersey.api.json.JSONConfiguration;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public class JSONUnmarshallerImpl extends BaseJSONUnmarshaller implements Unmarshaller {

    public JSONUnmarshallerImpl(JAXBContext jaxbContext, JSONConfiguration jsonConfig) throws JAXBException {
        super (jaxbContext, jsonConfig);
    }
    
    // Unmarshaller
    
    public Object unmarshal(File file) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(file);
    }

    public Object unmarshal(InputStream inputStream) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(inputStream);
    }
    
    public Object unmarshal(Reader reader) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(reader);
    }

    public Object unmarshal(URL url) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(url);
    }

    public Object unmarshal(InputSource inputSource) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(inputSource);
    }

    public Object unmarshal(Node node) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(node);
    }

    public <T> JAXBElement<T> unmarshal(Node node, Class<T> type) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(node, type);
    }

    public Object unmarshal(Source source) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(source);
    }

    public <T> JAXBElement<T> unmarshal(Source source, Class<T> type) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(source, type);
    }

    public Object unmarshal(XMLStreamReader xmlStreamReader) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(xmlStreamReader);
    }

    public <T> JAXBElement<T> unmarshal(XMLStreamReader xmlStreamReader, Class<T> type) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(xmlStreamReader, type);
    }

    public Object unmarshal(XMLEventReader xmlEventReader) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(xmlEventReader);
    }

    public <T> JAXBElement<T> unmarshal(XMLEventReader xmlEventReader, Class<T> type) throws JAXBException {
        return this.jaxbUnmarshaller.unmarshal(xmlEventReader, type);
    }

    public UnmarshallerHandler getUnmarshallerHandler() {
        return this.jaxbUnmarshaller.getUnmarshallerHandler();
    }

    public void setValidating(boolean validating) throws JAXBException {
        this.jaxbUnmarshaller.setValidating(validating);
    }

    public boolean isValidating() throws JAXBException {
        return this.jaxbUnmarshaller.isValidating();
    }

    public void setEventHandler(ValidationEventHandler validationEventHandler) throws JAXBException {
        this.jaxbUnmarshaller.setEventHandler(validationEventHandler);
    }

    public ValidationEventHandler getEventHandler() throws JAXBException {
        return this.jaxbUnmarshaller.getEventHandler();
    }

    public void setProperty(String key, Object value) throws PropertyException {
        this.jaxbUnmarshaller.setProperty(key, value);
    }

    public Object getProperty(String key) throws PropertyException {
        return this.jaxbUnmarshaller.getProperty(key);
    }

    public void setSchema(Schema schema) {
        this.jaxbUnmarshaller.setSchema(schema);
    }

    public Schema getSchema() {
        return this.jaxbUnmarshaller.getSchema();
    }

    public void setAdapter(XmlAdapter xmlAdapter) {
        this.jaxbUnmarshaller.setAdapter(xmlAdapter);
    }

    public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter) {
        this.jaxbUnmarshaller.setAdapter(type, adapter);
    }

    public <A extends XmlAdapter> A getAdapter(Class<A> type) {
        return this.jaxbUnmarshaller.getAdapter(type);
    }

    public void setAttachmentUnmarshaller(AttachmentUnmarshaller attachmentUnmarshaller) {
        this.jaxbUnmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);
    }

    public AttachmentUnmarshaller getAttachmentUnmarshaller() {
        return this.jaxbUnmarshaller.getAttachmentUnmarshaller();
    }

    public void setListener(Listener listener) {
        this.jaxbUnmarshaller.setListener(listener);
    }

    public Listener getListener() {
        return this.jaxbUnmarshaller.getListener();
    }
}
