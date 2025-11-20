package com.levelup.backend.controller;

import com.levelup.backend.dto.CreateUserAddressRequest;
import com.levelup.backend.dto.UpdateUserAddressRequest;
import com.levelup.backend.dto.UserAddressDto;
import com.levelup.backend.service.UserAddressService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{run}/addresses")
@RequiredArgsConstructor
public class UserAddressController {
    private final UserAddressService addressService;

    @GetMapping
    public ResponseEntity<List<UserAddressDto>> list(@PathVariable String run) {
        return ResponseEntity.ok(addressService.list(run));
    }

    @PostMapping
    public ResponseEntity<UserAddressDto> create(@PathVariable String run,
                                                 @Valid @RequestBody CreateUserAddressRequest request) {
        UserAddressDto created = addressService.create(run, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<UserAddressDto> update(@PathVariable String run,
                                                 @PathVariable String addressId,
                                                 @Valid @RequestBody UpdateUserAddressRequest request) {
        return ResponseEntity.ok(addressService.update(run, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<List<UserAddressDto>> delete(@PathVariable String run,
                                                       @PathVariable String addressId) {
        return ResponseEntity.ok(addressService.delete(run, addressId));
    }

    @PostMapping("/{addressId}/primary")
    public ResponseEntity<UserAddressDto> promote(@PathVariable String run,
                                                  @PathVariable String addressId) {
        return ResponseEntity.ok(addressService.promote(run, addressId));
    }
}
