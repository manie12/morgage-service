package io.bank.mortgage.web;

import com.bank.mortgage.application.model.Application;
import com.bank.mortgage.application.model.DecisionRequest;
import com.bank.mortgage.application.model.NewApplicationRequest;
import com.bank.mortgage.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Application> createApplication(@Valid @RequestBody NewApplicationRequest request,
                                                         @AuthenticationPrincipal UserDetails currentUser) {
        Application newApplication = applicationService.createApplication(request, currentUser);
        return new ResponseEntity<>(newApplication, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable UUID id,
                                                          @AuthenticationPrincipal UserDetails currentUser) {
        Application application = applicationService.getApplicationById(id, currentUser);
        return ResponseEntity.ok(application);
    }

    @GetMapping
    public ResponseEntity<Page<Application>> getAllApplications(@PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<Application> applications = applicationService.getAllApplications(pageable);
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/{id}/decision")
    public ResponseEntity<Application> makeDecision(@PathVariable UUID id,
                                                    @Valid @RequestBody DecisionRequest request,
                                                    @AuthenticationPrincipal UserDetails currentUser) {
        Application updatedApplication = applicationService.makeDecision(id, request, currentUser);
        return ResponseEntity.ok(updatedApplication);
    }
}