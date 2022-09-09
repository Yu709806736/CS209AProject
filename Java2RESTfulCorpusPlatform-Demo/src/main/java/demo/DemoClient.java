package demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import util.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DemoClient {
    public static void main(String[] args) throws IOException {
        // Before your study you can read :http://www.ruanyifeng.com/blog/2011/09/restful.html to understand
        // the RESTful.
        // Read http://www.ruanyifeng.com/blog/2014/05/restful_api.html to know more detail how the RESTful works.

        // This is a small quickstart guide of using Apache HttpComponent fluent APIs to perform web requests.
        // You can read more at: https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fluent.html
        // Before moving on, please make sure `DemoServer` is running on localhost:7002

        String endpoint = "http://localhost:7002";

        // To get from a certain url, just use the `Get` method.
        String responseString = Request.Get(endpoint+"/").execute().returnContent().asString();
        System.out.println("Content at / : "+ responseString);

        // To get from a certain url, just use the `Get` method.
        responseString = Request.Get(endpoint+"/hi").execute().returnContent().asString();
        System.out.println("Content at /hi : "+ responseString);

        //
        String name = "Adam";
        responseString = Request.Get(endpoint+"/greet"+"/"+name).execute().returnContent().asString();
        System.out.println("Content at /greet : "+ responseString);

        // You can also read json from response, with a little help from ObjectMapper
        responseString = Request.Get(endpoint+"/jsonSample").execute().returnContent().asString();
        System.out.println("Content at /jsonSample : " + responseString);
        ObjectMapper objectMapper = new ObjectMapper();
        Course course = objectMapper.readValue(responseString, Course.class);
        System.out.println(course);

        // To read our predefined `Response`, you might need to covert it to a map
        responseString = Request.Get(endpoint+"/response/success").execute().returnContent().asString();
        System.out.println("Content at /success : " + responseString);
        Map<String, Object> successResponse = (Map<String, Object>)objectMapper.readValue(responseString, Map.class);
        Map<String, Object> result = (Map<String, Object>)successResponse.get("result");
        System.out.println("Score is "+result.get("score"));

        responseString = Request.Get(endpoint+"/response/failure").execute().returnContent().asString();
        System.out.println("Content at /failure : " + responseString);
        successResponse = (Map<String, Object>)objectMapper.readValue(responseString, Map.class);
        result = (Map<String, Object>)successResponse.get("result");
        System.out.println("Score is "+result.get("score"));

        // Beside `Get`, you can also perform `Post`
        String postContent = "Greeting from client!";
        byte[] postBytes = postContent.getBytes(StandardCharsets.UTF_8);
        responseString = Request.Post(endpoint+"/bodySample").bodyByteArray(postBytes).execute().returnContent().asString();
        System.out.println("main.Server response: "+ responseString);

        // That's the end of the client demo. Hope this helps!
    }
}
