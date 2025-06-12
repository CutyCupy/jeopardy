package de.ciupka.jeopardy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GetController {

    @GetMapping("/gamemaster")
    public String getGameMaster() {
        return "gamemaster.html";
    }
}
