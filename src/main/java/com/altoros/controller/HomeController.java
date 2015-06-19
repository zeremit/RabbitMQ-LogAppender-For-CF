package com.altoros.controller;

import com.altoros.bean.Message;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.AmqpTemplate;

@Controller
public class HomeController {

    private static  final Logger logger = Logger.getLogger(HomeController.class);

    @Autowired
    AmqpTemplate amqpTemplate;

    @RequestMapping(value = "/")
    public String home(Model model) {
        model.addAttribute(new Message());
        return "WEB-INF/views/home.jsp";
    }

    @RequestMapping(value = "/publish", method=RequestMethod.POST)
    public String publish(Model model, Message message) {
        // Send a message to the "messages" queue
        logger.error(message.getValue());
        model.addAttribute("published", true);
        return home(model);
    }

    @RequestMapping(value = "/get", method=RequestMethod.POST)
    public String get(Model model) {
        // Receive a message from the "messages" queue
        String message = (String)amqpTemplate.receiveAndConvert("amqp-queue");
        String messageCF = (String)amqpTemplate.receiveAndConvert("log4j-queue");
        if (message != null)
            model.addAttribute("got", message);
        else
            model.addAttribute("got_queue_empty", true);
        if (messageCF != null)
            model.addAttribute("got_cf", messageCF);
        else
            model.addAttribute("got_queue_empty_cf", true);


        return home(model);
    }
}
