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
package com.sun.jersey.core.header.reader;

import java.text.ParseException;
import static com.sun.jersey.core.header.GrammarUtil.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
/* package */ final class HttpHeaderReaderImpl extends HttpHeaderReader {

    private String header;

    private boolean processComments;

    private int index;

    private int length;

    private Event event;

    private String value;

    public HttpHeaderReaderImpl(String header, boolean processComments) {
        this.header = (header == null) ? "" : header;
        this.processComments = processComments;
        this.index = 0;
        this.length = this.header.length();
    }

    public HttpHeaderReaderImpl(String header) {
        this(header, false);
    }

    @Override
    public boolean hasNext() {
        return skipWhiteSpace();
    }

    @Override
    public boolean hasNextSeparator(char separator, boolean skipWhiteSpace) {
        if (skipWhiteSpace) {
            skipWhiteSpace();
        }

        if (index >= length) {
            return false;
        }

        char c = header.charAt(index);
        return (TYPE_TABLE[c] == SEPARATOR)
                ? c == separator : false;
    }

    @Override
    public String nextSeparatedString(char startSeparator, char endSeparator) throws ParseException {
        nextSeparator(startSeparator);
        final int start = index;
        for (; index < length; index++) {
            if (header.charAt(index) == endSeparator) {
                break;
            }
        }

        if (start == index) {
            // no token between separators
            throw new ParseException("No characters between the separators " +
                    "'" + startSeparator + "' and '" + endSeparator + "'", index);
        } else if (index == length) {
            // no end separator
            throw new ParseException("No end separator '" + endSeparator + "'", index);
        }

        event = Event.Token;
        return value = header.substring(start, index++);
    }

    @Override
    public Event next() throws ParseException {
        return event = process(getNextCharacter(true));
    }

    @Override
    public Event next(boolean skipWhiteSpace) throws ParseException {
        return event = process(getNextCharacter(skipWhiteSpace));
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public String getEventValue() {
        return value;
    }

    @Override
    public String getRemainder() {
        return (index < length) ? header.substring(index) : null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    private boolean skipWhiteSpace() {
        for (; index < length; index++) {
            if (!isWhiteSpace(header.charAt(index))) {
                return true;
            }
        }

        return false;
    }

    private char getNextCharacter(boolean skipWhiteSpace) throws ParseException {
        if (skipWhiteSpace) {
            skipWhiteSpace();
        }

        if (index >= length) {
            throw new ParseException("End of header", index);
        }

        return header.charAt(index);
    }

    private Event process(char c) throws ParseException {
        if (c > 127) {
            index++;
            return Event.Control;
        }

        switch (TYPE_TABLE[c]) {
            case TOKEN: {
                final int start = index;
                for (index++; index < length; index++) {
                    if (!isToken(header.charAt(index))) {
                        break;
                    }
                }
                value = header.substring(start, index);
                return Event.Token;
            }
            case QUOTED_STRING:
                processQuotedString();
                return Event.QuotedString;
            case COMMENT:
                if (!processComments) {
                    throw new ParseException("Comments are not allowed", index);
                }

                processComment();
                return Event.Comment;
            case SEPARATOR:
                index++;
                value = String.valueOf(c);
                return Event.Separator;
            case CONTROL:
                index++;
                value = String.valueOf(c);
                return Event.Control;
            default:
                // White space
                throw new ParseException("White space not allowed", index);
        }
    }

    private void processComment() throws ParseException {
        boolean filter = false;
        int nesting;
        int start;
        for (start = ++index  , nesting = 1;
                nesting > 0 && index < length;
                index++) {
            char c = header.charAt(index);
            if (c == '\\') {
                index++;
                filter = true;
            } else if (c == '\r') {
                filter = true;
            } else if (c == '(') {
                nesting++;
            } else if (c == ')') {
                nesting--;
            }
        }
        if (nesting != 0) {
            throw new ParseException("Unbalanced comments", index);
        }

        value = (filter)
                ? filterToken(header, start, index - 1)
                : header.substring(start, index - 1);
    }

    private void processQuotedString() throws ParseException {
        boolean filter = false;
        for (int start = ++index; index < length; index++) {
            char c = this.header.charAt(index);
            if (c == '\\') {
                index++;
                filter = true;
            } else if (c == '\r') {
                filter = true;
            } else if (c == '"') {
                value = (filter)
                        ? filterToken(header, start, index)
                        : header.substring(start, index);

                index++;
                return;
            }
        }

        throw new ParseException("Unbalanced quoted string", index);
    }
}
