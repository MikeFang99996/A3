package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/game")
public class GameController {
    private final Game game;

    @Autowired
    public GameController(Game game) {
        this.game = game;
    }

    @PostMapping("/playTurn")
    @CrossOrigin(origins = "http://localhost:3000")
    public String playTurn() {
        game.playTurn();
        return "Turn played successfully.";
    }

    @GetMapping("/setup")
    @CrossOrigin(origins = "http://localhost:3000")
    public String startGame() {
        game.setup();
        return "Game started successfully.";
    }
}