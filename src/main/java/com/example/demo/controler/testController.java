package com.example.demo.controler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping (value = "")
public class testController {

    @GetMapping(value = "/test")
    public String hello () {
        return "Hello";
    }
    
}
