package com.levelup.backend.controller;

import com.levelup.backend.dto.ContactMessageDto;
import com.levelup.backend.dto.CreateContactMessageRequest;
import com.levelup.backend.dto.UpdateContactMessageRequest;
import com.levelup.backend.service.ContactMessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class ContactMessageController {
    private final ContactMessageService service;

    @GetMapping
    public ResponseEntity<List<ContactMessageDto>> list(@RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String query) {
        return ResponseEntity.ok(service.list(status, query));
    }

    @PostMapping
    public ResponseEntity<ContactMessageDto> create(@Valid @RequestBody CreateContactMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ContactMessageDto> update(@PathVariable String id,
                                                    @Valid @RequestBody UpdateContactMessageRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }
}
