package com.sun.jersey.samples.mandel

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;


object MandelServer {
    private def getPort(defaultPort : Int) = {
        val port = System.getProperty("jersey.test.port");;

        if (null != port)
            try  {
            Integer.parseInt(port);
            } catch {
                case ex: NumberFormatException => defaultPort;
            }
        else
            defaultPort;
    }

    private def getBaseURI() = {
        UriBuilder.fromUri("http://localhost/").port(getPort(9998)).
            path("mandelbrot").build();
    }

    val BASE_URI = getBaseURI();

    def startServer() = {
        val rc = new PackagesResourceConfig("com.sun.jersey.samples.mandel");

        System.out.println("Starting grizzly...");
        GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
    }

    def main(args: Array[String]) {
        val httpServer = startServer();

        println("Server running");
        println("Visit: " + BASE_URI + "/(-2.2,-1.2),(0.8,1.2)");
        println("The query parameter 'limit' specifies the limit on number of iterations");
        println("to determine if a point on the complex plain belongs to the mandelbrot set");
        println("The query parameter 'imageSize' specifies the maximum size of the image");
        println("in either the horizontal or virtical direction");
        println("Hit return to stop...");
        System.in.read();
        println("Stopping server");
        httpServer.stop();
        println("Server stopped");
        System.exit(0);
    }
}
