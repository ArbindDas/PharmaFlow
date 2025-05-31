package com.JSR.PharmaFlow.Controllers;


import com.JSR.PharmaFlow.Services.TestService;
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

    @GetMapping("/print")
    public String getPrint(){
        return new String("get print");
    }
}
