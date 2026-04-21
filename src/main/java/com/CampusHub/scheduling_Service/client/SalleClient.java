package com.CampusHub.scheduling_Service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "campushub-salle-service")
public interface SalleClient {
    @GetMapping("/api/salles/{id}")
    SalleDTO getSalleById(@PathVariable("id") Long id);
}
