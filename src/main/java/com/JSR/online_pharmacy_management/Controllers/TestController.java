package com.JSR.online_pharmacy_management.Controllers;


import com.JSR.online_pharmacy_management.Services.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping ("/cache-test/{id}")
    public String testCache(@PathVariable Long id) {
        return testService.cacheTest(id);
    }
}
