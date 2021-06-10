package com.demo.universalconverter.controller;


import com.demo.universalconverter.service.Converter;
import com.demo.universalconverter.dto.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController

public class HttpHandler {
//    @Autowired
    private Converter converter;

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @PostMapping(value = "/convert", consumes = "application/json; charset=utf-8")
    public ResponseEntity<String> convert(@RequestBody Request request) {
        return converter.convert(request.getFrom(), request.getTo());
    }
}