/*
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 */
package com.restfully.shop.test;

import com.restfully.shop.domain.Customer;
import com.restfully.shop.domain.Customers;
import com.restfully.shop.domain.LineItem;
import com.restfully.shop.domain.Link;
import com.restfully.shop.domain.Order;
import com.restfully.shop.domain.Product;
import com.restfully.shop.domain.Products;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:pavel.bucek@sun.com">Pavel Bucek</a>
 */
public class ShoppingTest {

    private String getJettyPort() {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if(port != null)
            return port;

        else return "9095"; // default
    }

    protected Map<String, Link> processLinkHeaders(ClientResponse response) {
        List<String> linkHeaders = (List<String>) response.getHeaders().get("Link");
        Map<String, Link> links = new HashMap<String, Link>();
        for (String header : linkHeaders) {
            Link link = Link.valueOf(header);
            links.put(link.getRelationship(), link);
        }
        return links;
    }

    @Test
    public void testPopulateDB() throws Exception {
        Client c = new Client();
        String url = "http://localhost:" + getJettyPort() + "/shop";
        WebResource wr = c.resource(url);

        ClientResponse response = wr.head();
        Map<String, Link> shoppingLinks = processLinkHeaders(response);

        System.out.println("** Populate Products");
        wr = c.resource(shoppingLinks.get("products").getHref());

        Product product = new Product();
        product.setName("iPhone");
        product.setCost(199.99);
        response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, product);
        Assert.assertEquals(201, response.getStatus());

        product = new Product();
        product.setName("MacBook Pro");
        product.setCost(3299.99);
        response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, product);
        Assert.assertEquals(201, response.getStatus());

        product = new Product();
        product.setName("iPod");
        product.setCost(49.99);
        response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, product);
        Assert.assertEquals(201, response.getStatus());

    }

    @Test
    public void testCreateOrder() throws Exception {
        Client c = new Client();

        String url = "http://localhost:" + getJettyPort() + "/shop";
        WebResource wr = c.resource(url);
        ClientResponse response = wr.head();
        Map<String, Link> shoppingLinks = processLinkHeaders(response);

        System.out.println("** Buy an iPhone for Bill Burke");
        System.out.println();
        System.out.println("** First see if Bill Burke exists as a customer");
        wr = c.resource(shoppingLinks.get("customers").getHref());
        Customers customers = wr.queryParam("firstName", "Bill").queryParam("lastName", "Burke").get(Customers.class);
        Customer customer = null;
        if (customers.getCustomers().size() > 0) {
            System.out.println("- Found a Bill Burke in the database, using that");
            customer = customers.getCustomers().iterator().next();
        } else {
            System.out.println("- Cound not find a Bill Burke in the database, creating one.");
            customer = new Customer();
            customer.setFirstName("Bill");
            customer.setLastName("Burke");
            customer.setStreet("222 Dartmouth Street");
            customer.setCity("Boston");
            customer.setState("MA");
            customer.setZip("02115");
            customer.setCountry("USA");
            wr = c.resource(shoppingLinks.get("customers").getHref());
            response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, customer);
            Assert.assertEquals(201, response.getStatus());
            String uri = (String) response.getHeaders().getFirst("Location");

            wr = c.resource(uri);
            customer = wr.get(Customer.class);
        }

        System.out.println();
        System.out.println("Search for iPhone in the Product database");
        wr = c.resource(shoppingLinks.get("products").getHref());
        Products products = wr.queryParam("name", "iPhone").get(Products.class);
        Product product = null;
        if (products.getProducts().size() > 0) {
            System.out.println("- Found iPhone in the database.");
            product = products.getProducts().iterator().next();
        } else {
            throw new RuntimeException("Failed to find an iPhone in the database!");
        }

        System.out.println();
        System.out.println("** Create Order for iPhone");
        LineItem item = new LineItem();
        item.setProduct(product);
        item.setQuantity(1);
        Order order = new Order();
        order.setTotal(product.getCost());
        order.setCustomer(customer);
        order.setDate(new Date().toString());
        order.getLineItems().add(item);
        wr = c.resource(shoppingLinks.get("orders").getHref());
        response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, order);
        Assert.assertEquals(201, response.getStatus());

        System.out.println();
        System.out.println("** Show all orders.");
        wr = c.resource(shoppingLinks.get("orders").getHref());
        String xml = wr.get(String.class);
        System.out.println(xml);
    }
}
