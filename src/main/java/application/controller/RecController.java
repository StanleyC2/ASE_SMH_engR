package application.controller;

import application.model.Response;
import application.model.User;
import application.service.RecService;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rec")
public class RecController {

    @Autowired
    private RecService recService;

    @PostMapping("/roommates/personality")
    public ResponseEntity<?> getResponses(@RequestBody String token,
                                          @RequestBody List<Integer> responses) {
        try {
            final Response response = recService.addOrReplaceResponse(token, responses);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/roommates/recommendation")
    public ResponseEntity<?> getRoommateRecommendations(@RequestBody String token) {
        try {
            final List<User> response = recService.recommendRoommates(token);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
