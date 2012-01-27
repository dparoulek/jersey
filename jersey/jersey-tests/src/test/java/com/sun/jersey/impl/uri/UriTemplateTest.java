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

package com.sun.jersey.impl.uri;

import com.sun.jersey.api.uri.UriTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriTemplateTest extends TestCase {
    
    public UriTemplateTest(String testName) {
        super(testName);
    }
    
    public void testTemplateNames() {
        _testTemplateNames("{a}", "a");
        _testTemplateNames("{  a}", "a");
        _testTemplateNames("{  a  }", "a");
        _testTemplateNames("{a:}", "a");
        _testTemplateNames("{a :}", "a");
        _testTemplateNames("{a : }", "a");

        _testTemplateNames("http://example.org/{a}/{b}/",
                "a", "b");
        _testTemplateNames("http://example.org/page1#{a}",
                "a");
        _testTemplateNames("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "scheme", "20", "wilma", "a");
        _testTemplateNames("http://example.org/{a-b}",
                "a-b");
        _testTemplateNames("http://example.org?{p}",
                "p");
        _testTemplateNames("http://example.com/order/{c}/{c}/{c}/",
                "c", "c", "c");
    }

    void _testTemplateNames(String template, String... names) {
        UriTemplate t = new UriTemplate(template);
        _testTemplateNames(t.getTemplateVariables(), names);
    }

    void _testTemplateNames(List<String> regexNames, String... names) {
        assertEquals(names.length, regexNames.size());

        Iterator<String> i = regexNames.iterator();
        for(String name : names) {
            assertEquals(name, i.next());
        }
    }

    public void testMatching() {
        _testMatching("http://example.org/{a}/{b}/",
                "http://example.org/fred/barney/",
                "fred", "barney");
        _testMatching("http://example.org/page1#{a}",
                "http://example.org/page1#fred",
                "fred");
        _testMatching("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "https://this-is-spinal-tap.example.org?date=2008&option=fred",
                "https", "this-is-spinal-tap", "2008", "fred");
        _testMatching("http://example.org/{a-b}",
                "http://example.org/none%20of%20the%20above",
                "none%20of%20the%20above");
        _testMatching("http://example.org?{p}",
                "http://example.org?quote=to+bo+or+not+to+be",
                "quote=to+bo+or+not+to+be");
        _testMatching("http://example.com/order/{c}/{c}/{c}/",
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "cheeseburger", "cheeseburger", "cheeseburger");
        _testMatching("http://example.com/{q}",
                "http://example.com/hullo#world",
                "hullo#world");
        _testMatching("http://example.com/{e}/",
                "http://example.com/xxx/",
                "xxx");
    }

    public void testTemplateRegexes() {
        _testTemplateRegex("{a:}", "([^/]+?)");
        _testTemplateRegex("{a:.*}", "(.*)");
        _testTemplateRegex("{a:  .*}", "(.*)");
        _testTemplateRegex("{a:  .*  }", "(.*)");
        _testTemplateRegex("{a :  .*  }", "(.*)");
    }

    private void _testTemplateRegex(String template, String regex) {
        UriTemplate t = new UriTemplate(template);
        assertEquals(regex, t.getPattern().toString());
    }

    public void testRegexMatching() {
        _testMatching("{b: .+}",
                "1",
                "1");

        _testMatching("{b: .+}",
                "1/2/3",
                "1/2/3");

        _testMatching("http://example.org/{a}/{b: .+}",
                "http://example.org/fred/barney/x/y/z",
                "fred", "barney/x/y/z");

        _testMatching("{b: \\d+}",
                "1234567890",
                "1234567890");

        _testMatching("{a}/{b: .+}/{c}{d: (/.*)?}",
                "1/2/3/4",
                "1", "2/3", "4", "");

        _testMatching("{a}/{b: .+}/{c}{d: (/.*)?}",
                "1/2/3/4/",
                "1", "2/3", "4", "/");
    }

    public void testRegexMatchingWithNestedGroups() {
        _testMatching("{b: (\\d+)}",
                "1234567890",
                "1234567890");

        _testMatching("{b: (\\d+)-(\\d+)-(\\d+)}",
                "12-34-56",
                "12-34-56");

        _testMatching("{a: (\\d)(\\d*)}-{b: (\\d)(\\d*)}-{c: (\\d)(\\d*)}",
                "12-34-56",
                "12", "34", "56");
    }

    void _testMatching(String template, String uri, String... values) {
        UriTemplate t = new UriTemplate(template);
        Map<String, String> m = new HashMap<String, String>();
        
        System.out.println("TEMPLATE: " + template);
        System.out.println("REGEX: " + t.getPattern().getRegex());
        System.out.println("TEMPLATE NAMES: " + t.getTemplateVariables());
        
        boolean isMatch = t.match(uri, m);
        assertTrue(isMatch);
        assertEquals(values.length, t.getTemplateVariables().size());
        
        System.out.println("MAP: " + m);
        
        Iterator<String> names = t.getTemplateVariables().iterator();
        for (String value : values) {
            String mapValue = m.get(names.next());
            assertEquals(value, mapValue);
        }


        List<String> matchedValues = new ArrayList<String>();
        isMatch = t.match(uri, matchedValues);
        assertTrue(isMatch);
        assertEquals(values.length, matchedValues.size());

        System.out.println("LIST: " + matchedValues);

        for (int i = 0; i < values.length; i++){
            assertEquals(values[i], matchedValues.get(i));
        }


        MatchResult mr = t.getPattern().match(uri);
        assertNotNull(mr);
        assertEquals(values.length, mr.groupCount());
        assertEquals(uri, mr.group());
        assertEquals(uri, mr.group(0));
        assertEquals(0, mr.start());
        assertEquals(uri.length(), mr.end());
        assertEquals(0, mr.start(0));
        assertEquals(uri.length(), mr.end(0));
        for (int i = 1; i <= mr.groupCount(); i++) {
            assertEquals(values[i - 1], mr.group(i));            
            assertEquals(values[i - 1], uri.substring(mr.start(i), mr.end(i)));            
        }
    }
    
    public void testNullMatching() {
        Map<String, String> m = new HashMap<String, String>();

        UriTemplate t = UriTemplate.EMPTY;
        assertEquals(false, t.match("/", m));
        assertEquals(true, t.match(null, m));
        assertEquals(true, t.match("", m));

        t = new UriTemplate("/{v}");
        assertEquals(false, t.match(null, m));
        assertEquals(true, t.match("/one", m));
    }

    public void testOrder() {
        List<UriTemplate> l = new ArrayList<UriTemplate>();

        l.add(UriTemplate.EMPTY);
        l.add(new UriTemplate("/{a}"));
        l.add(new UriTemplate("/{a}/{b}"));
        l.add(new UriTemplate("/{a}/one/{b}"));

        Collections.sort(l, UriTemplate.COMPARATOR);

        assertEquals(new UriTemplate("/{a}/one/{b}").getTemplate(),
                l.get(0).getTemplate());
        assertEquals(new UriTemplate("/{a}/{b}").getTemplate(),
                l.get(1).getTemplate());
        assertEquals(new UriTemplate("/{a}").getTemplate(),
                l.get(2).getTemplate());
        assertEquals(UriTemplate.EMPTY.getTemplate(),
                l.get(3).getTemplate());
    }

    public void testSubstitutionArray() {
        _testSubstitutionArray("http://example.org/{a}/{b}/",
                "http://example.org/fred/barney/",
                "fred", "barney");
        _testSubstitutionArray("http://example.org/page1#{a}",
                "http://example.org/page1#fred",
                "fred");
        _testSubstitutionArray("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "https://this-is-spinal-tap.example.org?date=&option=fred",
                "https", "this-is-spinal-tap", "", "fred");
        _testSubstitutionArray("http://example.org/{a-b}",
                "http://example.org/none%20of%20the%20above",
                "none%20of%20the%20above");
        _testSubstitutionArray("http://example.org?{p}",
                "http://example.org?quote=to+bo+or+not+to+be",
                "quote=to+bo+or+not+to+be");
        _testSubstitutionArray("http://example.com/order/{c}/{c}/{c}/",
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "cheeseburger");
        _testSubstitutionArray("http://example.com/{q}",
                "http://example.com/hullo#world",
                "hullo#world");
        _testSubstitutionArray("http://example.com/{e}/",
                "http://example.com//",
                "");
    }

    void _testSubstitutionArray(String template, String uri, String... values) {
        UriTemplate t = new UriTemplate(template);

        assertEquals(uri, t.createURI(values));
    }

    public void testSubstitutionMap() {
        _testSubstitutionMap("http://example.org/{a}/{b}/",
                "http://example.org/fred/barney/",
                "a", "fred",
                "b","barney");
        _testSubstitutionMap("http://example.org/page1#{a}",
                "http://example.org/page1#fred",
                "a", "fred");
        _testSubstitutionMap("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "https://this-is-spinal-tap.example.org?date=&option=fred",
                "scheme", "https",
                "20", "this-is-spinal-tap",
                "wilma", "",
                "a", "fred");
        _testSubstitutionMap("http://example.org/{a-b}",
                "http://example.org/none%20of%20the%20above",
                "a-b", "none%20of%20the%20above");
        _testSubstitutionMap("http://example.org?{p}",
                "http://example.org?quote=to+bo+or+not+to+be",
                "p", "quote=to+bo+or+not+to+be");
        _testSubstitutionMap("http://example.com/order/{c}/{c}/{c}/",
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "c", "cheeseburger");
        _testSubstitutionMap("http://example.com/{q}",
                "http://example.com/hullo#world",
                "q", "hullo#world");
        _testSubstitutionMap("http://example.com/{e}/",
                "http://example.com//",
                "e", "");
    }

    void _testSubstitutionMap(String template, String uri, String... variablesAndvalues) {
        UriTemplate t = new UriTemplate(template);

        Map<String, String> variableMap = new HashMap<String, String>();
        for (int i = 0; i < variablesAndvalues.length; i+=2)
            variableMap.put(variablesAndvalues[i], variablesAndvalues[i+1]);

        assertEquals(uri, t.createURI(variableMap));
    }
}