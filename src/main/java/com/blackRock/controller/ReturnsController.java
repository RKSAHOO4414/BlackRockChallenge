package com.blackRock.controller;

import com.blackRock.dto.ReturnsRequest;
import com.blackRock.dto.ReturnsResponse;
import com.blackRock.service.ReturnsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1/returns")
public class ReturnsController {

    @Autowired
    private ReturnsService returnsService;

    @PostMapping("/nps")
    public ResponseEntity<ReturnsResponse> calculateNPSReturns(
            @Valid @RequestBody ReturnsRequest request) {

        ReturnsResponse response = returnsService.calculateNPSReturns(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/index")
    public ResponseEntity<ReturnsResponse> calculateIndexReturns(
            @Valid @RequestBody ReturnsRequest request) {

        ReturnsResponse response = returnsService.calculateIndexReturns(request);
        return ResponseEntity.ok(response);
    }
}