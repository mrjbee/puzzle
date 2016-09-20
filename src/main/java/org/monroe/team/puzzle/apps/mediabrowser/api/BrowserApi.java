package org.monroe.team.puzzle.apps.mediabrowser.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BrowserApi {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Test get(@RequestParam(value="name", defaultValue="World") String name) {
        Test test = new Test(name);
        return test;
    }

}
