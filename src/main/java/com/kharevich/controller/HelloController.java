package com.kharevich.controller;

import com.kharevich.bean.Greeting;
import com.kharevich.component.EnvironmentComponent;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zeremit on 15.6.15.
 */
@RestController
public class HelloController {

    @Autowired
    private EnvironmentComponent environmentComponent;

    private static  final Logger logger = Logger.getLogger(HelloController.class);


    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    @ResponseBody
    public Greeting greeting(@RequestParam(value="name", required=false, defaultValue="World") String name) {
        logger.error("hello method");
        return new Greeting(counter.incrementAndGet(),
                String.format(template, environmentComponent.getUserName()));
    }
}
