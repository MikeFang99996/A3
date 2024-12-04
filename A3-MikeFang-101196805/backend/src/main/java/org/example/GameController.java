package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000") // Adjust the origin to match your frontend's address
public class GameController {
    private final Game game;

    @Autowired
    public GameController(Game game) {
        this.game = game;
        game.setup();
    }

    @PostMapping("/playTurn")
    public String playTurn() {
        game.playTurn();
        return "Turn played successfully.";
    }

    @GetMapping("/start")
    public String startGame() {
        game.setup();
        return "Game started successfully.";
    }
}