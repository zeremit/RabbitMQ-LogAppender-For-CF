package com.kharevich.controller;

import com.kharevich.bean.Greeting;
import com.kharevich.component.EnvironmentComponent;
import com.kharevich.component.RabbitMQAppender;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {


    @Autowired
    private EnvironmentComponent environmentComponent;

    private static  final Logger logger = Logger.getLogger(GreetingController.class);

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    @ResponseBody
    public Greeting greeting(@RequestParam(value="name", required=false, defaultValue="World") String name) {
        logger.error("greeting method");
        return new Greeting(counter.incrementAndGet(),
                String.format(template, environmentComponent.getUserName()));
    }

    @RequestMapping("/env")
    public void env(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("System Environment:");
        for (Map.Entry<String, String> envvar : System.getenv().entrySet()) {
            out.println(envvar.getKey() + ": " + envvar.getValue());
        }
    }

    @RequestMapping(value = "/greeting1", method = RequestMethod.GET)
    @ResponseBody
    public Greeting getCustomerPumpMsgs() {
        CloudFactory cloudFactory = new CloudFactory();
        return new Greeting(counter.incrementAndGet(),
                String.format(template, "name"));
    }
}