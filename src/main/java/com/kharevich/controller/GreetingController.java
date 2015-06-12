package com.kharevich.controller;

import com.kharevich.bean.Greeting;
import com.kharevich.component.EnvironmentComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {


    @Autowired
    private EnvironmentComponent environmentComponent;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    @ResponseBody
    public Greeting greeting(@RequestParam(value="name", required=false, defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, environmentComponent.getUserName()));
    }

    @RequestMapping(value = "/greeting1", method = RequestMethod.GET)
    @ResponseBody
    public Greeting getCustomerPumpMsgs() {

        return new Greeting(counter.incrementAndGet(),
                String.format(template, "name"));
    }
}