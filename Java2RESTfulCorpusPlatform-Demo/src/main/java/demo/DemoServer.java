package demo;

import io.javalin.Javalin;
import io.javalin.http.Context;
import util.FailureCause;
import util.FailureResponse;
import util.Response;
import util.SuccessResponse;

public class DemoServer {
    public static void main(String[] args) {
        // This is a small quickstart guide to RESTful server with Javalin framework.
        // It should have covered all the Javalin features you will be using in this project.
        // Read more about Javalin at: https://javalin.io/documentation

        // Initialize Javalin main.Server on port 7002
        Javalin app = Javalin.create().start(7002);

        // Handles a Get request at route /
        app.get("/", ctx -> {
            // ctx stands for context
            // ctx.result method set the response body
            ctx.result("main.Server Demo");
        });
        // By the way, `->` uses Lambda Expression
        // You can read more about Lambda Expression here: https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html

        // You can also use methods to handle requests, using Method Reference (Class::Method)
        app.get("/hi", DemoServer::handleHi);
        // or go with the more traditional way
        // app.get("/hi", ctx -> DemoServer.handleHi(ctx));

        // You can read from path parameters
        app.get("/greet/:name", ctx -> {
            // just like a map
            String name = ctx.pathParam("name");
            System.out.println("Name is "+ name);
            ctx.result("Hello, "+name);
        });



        // even return objects in JSON
        app.get("/jsonSample", ctx -> {
           // requires ctx.json method
           ctx.json(new Course("Java2", 4));
        });

        // In this project, you will use our predefined Response class
        app.get("/response/success", ctx -> {
            // If success, use SuccessResponse
            Response response = new SuccessResponse();
            // and put the items into the response by using getResult().put
            // To those curious, it's operating Jackson's JSON node underneath.
            response.getResult().put("score", "100");
            // finally send out the response
            ctx.json(response);
        });

        app.get("/response/failure", ctx -> {
            // If failed, use Failure Response
            // You may customize response code and failure message
            Response response1 = new FailureResponse(233,"failure");
            // Or use the predefined ones
            Response response2 = new FailureResponse(FailureCause.ALREADY_EXIST);

            ctx.json(response2);
        });

        // As well as read from request body in a post request
        app.post("/bodySample", ctx -> {
            // as String
            String bodyString = ctx.body();
            ctx.result(bodyString);

            // or as byte[]
//            byte[] bytes = ctx.bodyAsBytes();
//
//            ctx.result("received byte length: "+bytes.length);
        });

        // That's the end of the server demo. Hope this helps!
    }


    private static void handleHi(Context ctx) {
        ctx.result("Hi!");
    }
}

