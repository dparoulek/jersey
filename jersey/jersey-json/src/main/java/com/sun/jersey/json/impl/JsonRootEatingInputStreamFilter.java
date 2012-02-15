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

import java.io.IOException;
import java.io.InputStream;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public class JsonRootEatingInputStreamFilter extends FilteringInputStream {

    private JsonParser jsonParser;
    private JsonGenerator jsonGenerator;
    private int depth;
    private BufferingInputOutputStream buffers;

    public JsonRootEatingInputStreamFilter(InputStream inputStream) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        this.jsonParser = jsonFactory.createJsonParser(inputStream);
        this.buffers = new BufferingInputOutputStream();
        this.jsonGenerator = jsonFactory.createJsonGenerator(this.buffers, JsonEncoding.UTF8);
        this.depth = 0;
    }

    protected byte[] nextBytes() throws IOException {
        if (!jsonParser.hasCurrentToken()) {
            jsonParser.nextToken();
        }

        final JsonToken token = jsonParser.getCurrentToken();

        if ((depth == 0) && (token == JsonToken.START_OBJECT)) {
            jsonParser.nextToken();
            return nextBytes();
        }

        if ((depth == 0) && (token == JsonToken.FIELD_NAME)) {
            depth++;
            jsonParser.nextToken();
            return nextBytes();
        }

        if ((depth == 1) && ((token == JsonToken.END_OBJECT) || (token == JsonToken.END_ARRAY))) {
            jsonParser.nextToken();
            return null;
        }

        jsonGenerator.copyCurrentEvent(jsonParser);
        jsonGenerator.flush();
        jsonParser.nextToken();

        if ((token == JsonToken.START_ARRAY) || (token == JsonToken.START_OBJECT)) {
            depth++;
        } else if ((token == JsonToken.END_ARRAY) || (token == JsonToken.END_OBJECT)) {
            depth--;
        }

        return buffers.nextBytes();
    }

    @Override
    public int available() throws IOException {
        return super.available()/* + buffers.available()*/;
    }


}
